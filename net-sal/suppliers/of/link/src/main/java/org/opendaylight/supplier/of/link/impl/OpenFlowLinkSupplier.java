/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.link.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.opendaylight.net.device.DeviceEvent;
import org.opendaylight.net.device.DeviceListener;
import org.opendaylight.net.device.DeviceService;
import org.opendaylight.net.link.LinkService;
import org.opendaylight.net.link.LinkSupplier;
import org.opendaylight.net.link.LinkSupplierService;
import org.opendaylight.net.link.LinkSuppliersBroker;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.of.controller.ControllerService;
import org.opendaylight.of.controller.ErrorEvent;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.OfmMutablePacketOut;
import org.opendaylight.of.lib.msg.OpenflowMessage;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.ProtocolId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;

import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;
import static org.opendaylight.net.device.DeviceEvent.Type.*;
import static org.opendaylight.net.model.Link.Type.DIRECT_LINK;
import static org.opendaylight.net.model.Link.Type.MULTIHOP_LINK;
import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.ADVISOR;
import static org.opendaylight.supplier.of.link.impl.BddpPacketBuilder.buildPacketOut;
import static org.opendaylight.util.CommonUtils.itemSet;
import static org.opendaylight.util.net.MacAddress.BROADCAST;
import static org.opendaylight.util.net.MacAddress.LINK_LOCAL_0E;

/**
 * Link supplier that uses OpenFlow controller.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class OpenFlowLinkSupplier implements LinkSupplier {

    private final Logger log = LoggerFactory.getLogger(OpenFlowLinkSupplier.class);

    private static final String MSG_STARTED = "OpenFlowLinkSupplier started";
    private static final String MSG_STOPPED = "OpenFlowLinkSupplier stopped";

    private static final int ALTITUDE = 12345;
    private static final String E_ERROR = "PacketListener: {}";

    private static final String E_SEND = "Unable to send BDDP packet out";
    private static final String NO_DATAPATH = "No datapath";

    // Various timer delays in milliseconds
    private static final long LINK_DISCOVERY_FREQUENCY = 30 * 1000;
    private static final long PRUNING_DELAY = 5 * 1000;

    private Timer timer;
    private final LinkRefresh linkRefreshTask = new LinkRefresh();

    private final SupplierId SUPPLIER_ID =
            new SupplierId(getClass().getPackage().getName());

    @Reference(name = "ControllerService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile ControllerService controllerService;

    @Reference(name = "LinkSuppliersBroker",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile LinkSuppliersBroker broker;

    @Reference(name = "DeviceService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile DeviceService deviceService;

    @Reference(name = "LinkService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile LinkService linkService;

    private LinkSupplierService supplierService;
    private BddpPacketStealer initFlowContrib;

    private final long controllerId;
    private final DeviceListener switchListener = new SwitchListener();
    private final SequencedPacketListener packetListener = new PacketListener();

    /**
     * Creates a link supplier with a controller id derived from current time.
     */
    public OpenFlowLinkSupplier() {
        controllerId = System.currentTimeMillis();
    }

    /**
     * Creates a link supplier with the supplier controller id.
     *
     * @param controllerId controller id
     */
    OpenFlowLinkSupplier(long controllerId) {
        this.controllerId = controllerId;
    }


    @Override
    public SupplierId supplierId() {
        return SUPPLIER_ID;
    }


    @Activate
    public void activate() {
        // Add listeners
        deviceService.addListener(switchListener);
        controllerService.addPacketListener(packetListener, ADVISOR, ALTITUDE,
                                            itemSet(ProtocolId.BDDP));

        // Provide an initial flow contributor
        initFlowContrib = new BddpPacketStealer();
        controllerService.registerInitialFlowContributor(initFlowContrib);

        // Register ourselves as a link supplier
        supplierService = broker.registerSupplier(this);

        // Schedule periodic re-discovery of links
        timer = new Timer("of-link-supplier", true);
        timer.schedule(linkRefreshTask, LINK_DISCOVERY_FREQUENCY,
                       LINK_DISCOVERY_FREQUENCY);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        timer.cancel();

        // Cancel all listener, contributor or supplier registrations
        deviceService.removeListener(switchListener);
        controllerService.removePacketListener(packetListener);
        broker.unregisterSupplier(this);
        controllerService.unregisterInitialFlowContributor(initFlowContrib);
        log.info(MSG_STOPPED);
    }

    // Emits link probes through the specified connection point
    private void emitLinkProbes(ConnectionPoint cp) {
        DeviceId devId = (DeviceId)cp.elementId();
        Device device = deviceService.getDevice(devId);
        Interface iface = deviceService.getInterface(devId, cp.interfaceId());

        if(device != null && iface != null)
            emitLinkProbes(device, iface);
    }

    // Emits link probes through the specified net interface
    private void emitLinkProbes(Device device, Interface netInterface) {
        DataPathInfo dpi = controllerService.getDataPathInfo(device.dpid());
        BigPortNumber portNumber = netInterface.id().port();
        MacAddress portMac = netInterface.mac();

        // Schedule emitter for link-local discovery packet and another one 
        //for broadcast discovery packet.
        timer.schedule(new Emitter(dpi.dpid(), portNumber,
                buildPacketOut(dpi, portNumber, portMac,
                        LINK_LOCAL_0E, controllerId)), 0);
        timer.schedule(new Emitter(dpi.dpid(), portNumber,
                buildPacketOut(dpi, portNumber, portMac,
                        BROADCAST, controllerId)), 0);
    }

    // Task for emitting link discovery packets
    private class Emitter extends TimerTask {

        private final DataPathId dpid;
        private final BigPortNumber portNumber;
        private final OpenflowMessage packetOut;

        Emitter(DataPathId dpid, BigPortNumber portNumber,
                OfmMutablePacketOut packetOut) {
            this.dpid = dpid;
            this.portNumber = portNumber;
            this.packetOut = packetOut.toImmutable();
        }

        @Override
        public void run() {
            try {
                controllerService.send(packetOut, dpid);
                log.debug("Emitted link probe on switch {} port {}", dpid, portNumber);
            } catch (OpenflowException e) {
                // Do not log messages that pertain to datapath being disconnected
                if (!e.getMessage().contains(NO_DATAPATH))
                    log.warn(E_SEND, e);
            }
        }
    }


    // TODO: revisit whether this is the correct way to prune or whether pruning should be offloaded from suppliers

    // Prunes link connecting to the specified net interface
    private void pruneLinks(Device device, Interface netInterface) {
        supplierService.removeAllLinks(new DefaultConnectionPoint(device.id(), netInterface.id()));
        log.debug("Pruned links on switch {} port {}", device.id(), netInterface);
    }

    // Prunes link connecting to the specified device
    private void pruneLinks(Device device) {
        supplierService.removeAllLinks(device.id());
        log.debug("Pruned links on switch {}", device);
    }

    // Creates or updates a link using the supplied BDDP descriptor.
    private void processLinkInformation(BddpPacketInfo bddpInfo) {
        log.debug("BDDP: {}", bddpInfo);

        Device srcDevice = deviceService.getDevice(bddpInfo.getSourceDpid());
        if (srcDevice == null)
            return;

        Device dstDevice = deviceService.getDevice(bddpInfo.getDestinationDpid());
        if (dstDevice == null)
            return;

        InterfaceId srcPort = InterfaceId.valueOf(bddpInfo.getSourcePort());
        ConnectionPoint src = new DefaultConnectionPoint(srcDevice.id(), srcPort);

        InterfaceId dstPort = InterfaceId.valueOf(bddpInfo.getDestinationPort());
        ConnectionPoint dst = new DefaultConnectionPoint(dstDevice.id(), dstPort);

        Link.Type type = bddpInfo.isLinkLocal() ? DIRECT_LINK : MULTIHOP_LINK;
        Link link = supplierService.createOrUpdateLink(src, dst, new DefaultLinkInfo(type));

        probeReverseLink(link);
    }

    // Kick off reverse link discovery if reverse link does not exist.
    private void probeReverseLink(Link link) {
        if (hasReverseLink(link))
            return;

        DeviceId deviceId = (DeviceId) link.dst().elementId();
        InterfaceId interfaceId = link.dst().interfaceId();
        Device device = deviceService.getDevice(deviceId);
        Interface netInterface = deviceService.getInterface(deviceId, interfaceId);
        if (device != null && netInterface != null)
            emitLinkProbes(device, netInterface);
    }

    // Determines whether a reverse direct link exists.
    private boolean hasReverseLink(Link link) {
        Set<Link> reverseLinks = linkService.getLinksFrom(link.dst());
        for (Link reverseLink : reverseLinks)
            if (reverseLink.src().equals(link.dst()) &&
                    reverseLink.type() == DIRECT_LINK)
                return true;
        return false;
    }


    // Interceptor for device & interface related events.
    private class SwitchListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            Device device = event.subject();

            if (isOpenFlowDevice(device)) {
                Interface netInterface = event.netInterface();

                if (interfaceWentUp(event))
                    emitLinkProbes(device, netInterface);
                else if (interfaceWentDown(event))
                    pruneLinks(device, netInterface);
                else if (deviceWentDown(event))
                    pruneLinks(device);
            }
        }

        // Determines whether the device is an OpenFlow device based on its URIs
        private boolean isOpenFlowDevice(Device device) {
            for (URI uri : device.managementURIs()) {
                if (uri.getScheme().equals(UriSchemes.OPENFLOW.toString()))
                    return true;
            }
            return false;
        }

        // Determines whether the event corresponds to an interface that went up
        private boolean interfaceWentUp(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            return (type == INTERFACE_STATE_CHANGED ||
                    type == INTERFACE_UPDATED ||
                    type == INTERFACE_ADDED) &&
                    event.netInterface().isEnabled();
        }

        // Determines whether the event corresponds to an interface that went down
        private boolean interfaceWentDown(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            return type == INTERFACE_REMOVED ||
                    ((type == INTERFACE_STATE_CHANGED || type == INTERFACE_UPDATED) &&
                            !event.netInterface().isEnabled());
        }

        private boolean deviceWentDown(DeviceEvent event) {
            DeviceEvent.Type type = event.type();
            return type == DEVICE_REMOVED ||
                    (type == DEVICE_AVAILABILITY_CHANGED && !event.subject().isOnline());
        }
    }

    // Packet interceptor
    private class PacketListener implements SequencedPacketListener {
        @Override
        public void event(MessageContext context) {
            BddpPacketInfo bddpInfo =
                    new BddpPacketInfo(context.srcEvent().dpid(),
                                       context.getPacketIn(),
                                       context.decodedPacket());
            if (bddpInfo.getControllerId() == controllerId)
                processLinkInformation(bddpInfo);
        }

        @Override
        public void errorEvent(ErrorEvent event) {
            log.warn(E_ERROR, event);
        }
    }

    // Task for executing link refresh
    private class LinkRefresh extends TimerTask {
        @Override
        public void run() {
            log.debug("Link refresh initiated...");
            pruneLinks();
            scanForLinks();
        }
    }

    // Scans existing links, verifies them and then prunes any stale ones
    private void pruneLinks() {
        long now = System.currentTimeMillis();

        // Get all links provided by this supplier
        Set<Link> links = getLinks();
        for (Link link : links)
            // Emit discovery link packet for each link source
            emitLinkProbes(link.src());

        // Schedule pruning of links that remain on probation
        timer.schedule(new LinkPruner(now, links), PRUNING_DELAY);
    }

    // Scans all un-tethered ports for links
    private void scanForLinks() {
        // Iterate over all interfaces of all on-line devices
        Iterator<Device> devices = deviceService.getDevices();
        while (devices.hasNext()) {
            Device device = devices.next();
            if (!device.isOnline())
                continue;

            // Get links that emanate from the device
            Set<Link> linksFromDevice = linkService.getLinksFrom(device.id());

            // Scan through all interfaces of a device
            for (Interface netInterface : deviceService.getInterfaces(device)) {
                ConnectionPoint cp = new DefaultConnectionPoint(device.id(), netInterface.id());
                // If there is no link leading out of the connection point emit a probe
                if (!hasLink(linksFromDevice, cp))
                    emitLinkProbes(cp);
            }
        }
    }

    // Returns true of the given connection point is found as a source of
    // one of the links in the set
    private boolean hasLink(Set<Link> linksFromDevice, ConnectionPoint cp) {
        for (Link link : linksFromDevice)
            if (link.src().interfaceId().equals(cp.interfaceId()))
                return true;
        return false;
    }

    // Returns the set of links provided by this supplier
    private Set<Link> getLinks() {
        Set<Link> links = new HashSet<>();
        Iterator<Link> it = linkService.getLinks();
        while (it.hasNext()) {
            Link link = it.next();
            if(link.supplierId().equals(supplierId()))
                links.add(link);
        }
        return links;
    }

    // Task to prune the specified set of link if their refresh falls before
    // a specified time limit
    private class LinkPruner extends TimerTask {

        private final long limit;
        private final Set<Link> links;

        public LinkPruner(long limit, Set<Link> links) {
            this.limit = limit;
            this.links = links;
        }

        @Override
        public void run() {
            for (Link link : links) {
                if (link.timestamp() < limit)
                    supplierService.removeLink(link.src(), link.dst());
            }
        }
    }

}
