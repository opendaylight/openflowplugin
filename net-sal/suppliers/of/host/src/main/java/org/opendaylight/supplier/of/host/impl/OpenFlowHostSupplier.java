/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.host.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.opendaylight.net.host.HostSupplier;
import org.opendaylight.net.host.HostSupplierService;
import org.opendaylight.net.host.HostSuppliersBroker;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.topology.TopologyService;
import org.opendaylight.of.controller.ControllerService;
import org.opendaylight.of.controller.ErrorEvent;
import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.pkt.MessageContext;
import org.opendaylight.of.controller.pkt.SequencedPacketListener;
import org.opendaylight.of.lib.msg.OfmPacketIn;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;
import org.opendaylight.util.packet.Arp;
import org.opendaylight.util.packet.Ethernet;
import org.opendaylight.util.packet.Packet;
import org.opendaylight.util.packet.ProtocolId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;
import static org.opendaylight.of.controller.pkt.SequencedPacketListenerRole.ADVISOR;
import static org.opendaylight.util.CommonUtils.itemSet;
import static org.opendaylight.util.net.BigPortNumber.bpn;

/**
 * Host supplier that uses OpenFlow controller.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class OpenFlowHostSupplier implements HostSupplier {

    private final Logger log = LoggerFactory.getLogger(OpenFlowHostSupplier.class);

    private static final String MSG_STARTED = "OpenFlowHostSupplier started";
    private static final String MSG_STOPPED = "OpenFlowHostSupplier stopped";

    private static final int ALTITUDE = 1234;
    private static final String E_ERROR = "PacketListener: {}";

    private static final BigPortNumber P0 = bpn(0);
    private static final InterfaceId NIC_P0 = InterfaceId.valueOf(P0);

    private final SupplierId SUPPLIER_ID =
            new SupplierId(getClass().getPackage().getName());

    @Reference(name = "ControllerService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile ControllerService controllerService;

    @Reference(name = "TopologyService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile TopologyService topologyService;

    @Reference(name = "HostSuppliersBroker",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile HostSuppliersBroker broker;

    private HostSupplierService supplierService;

//    private PacketStealer initFlowContrib;

    private final SequencedPacketListener packetListener = new PacketListener();

    @Override
    public SupplierId supplierId() {
        return SUPPLIER_ID;
    }


    @Activate
    public void activate() {
        controllerService.addPacketListener(packetListener, ADVISOR, ALTITUDE,
                             itemSet(ProtocolId.ARP, ProtocolId.DHCP));

        // FIXME: implement this
//        initFlowContrib = new BddpPacketStealer();
//        controllerService.registerInitialFlowContributor(initFlowContrib);

        supplierService = broker.registerSupplier(this);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        controllerService.removePacketListener(packetListener);
        broker.unregisterSupplier(this);
        // FIXME: implement this
//        controllerService.unregisterInitialFlowContributor(initFlowContrib);
        log.info(MSG_STOPPED);
    }

    // Processes the specified ARP packet
    private void processArp(Packet packet, HostLocation location) {
        log.debug("ARP: {}", packet);

        Arp arp = packet.get(ProtocolId.ARP);
        IpAddress ip = arp.senderIpAddr();
        MacAddress mac = arp.senderMacAddr();

        SegmentId segId = extractSegmentId(packet);

        HostId hostId = HostId.valueOf(ip, segId);
        Interface nic = nic(hostId);
        DefaultHostInfo info = new DefaultHostInfo(nic, mac, location);
        supplierService.createOrUpdateHost(hostId, info);
    }

    private SegmentId extractSegmentId(Packet packet) {
        Ethernet eth = packet.get(ProtocolId.ETHERNET);
        return eth.vlanId() > 0
                ? SegmentId.valueOf(VlanId.valueOf(eth.vlanId()))
                : SegmentId.UNKNOWN;
        // TODO: consider digging the VLan ID out from the Match structure
        //  if packet is untagged (eth.vlanId == 0) - some switches do this?
    }

    // Creates a NIC descriptor for the specified host
    private Interface nic(HostId hostId) {
        return new DefaultInterface(NIC_P0, new DefaultInterfaceInfo(hostId, P0));
    }

    // Processes the specified DHCP packet
    private void processDhcp(Packet packet, HostLocation location) {
        log.debug("DHCP: {}", packet);
    }


    // Packet interceptor
    private class PacketListener implements SequencedPacketListener {
        @Override
        public void event(MessageContext context) {
            Packet packet = context.decodedPacket();

            // Avoid learning from packets on non-standard ports
            MessageEvent event = context.srcEvent();
            OfmPacketIn pktIn = (OfmPacketIn) event.msg();
            BigPortNumber port = pktIn.getInPort();
            if (!Port.isStandardPort(port, pktIn.getVersion()))
                return;

            // Identify the device that sent this packet
            DeviceId deviceId = DeviceId.valueOf(event.dpid().toString());

            // Avoid learning from packets on infrastructure ports
            HostLocation location = new DefaultHostLocation(deviceId, InterfaceId.valueOf(port));
            if (topologyService.isInfrastructure(location))
                return ;

            if (packet.has(ProtocolId.ARP))
                processArp(packet, location);
            else if (packet.has(ProtocolId.DHCP))
                processDhcp(packet, location);
        }

        @Override
        public void errorEvent(ErrorEvent event) {
            log.warn(E_ERROR, event);
        }
    }

}
