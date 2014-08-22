/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.supplier.of.device.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.opendaylight.net.device.DeviceSupplier;
import org.opendaylight.net.device.DeviceSupplierService;
import org.opendaylight.net.device.DeviceSuppliersBroker;
import org.opendaylight.net.facet.ManualDeviceIdentityHandler;
import org.opendaylight.net.facet.ManualIdentity;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.lib.dt.DataPathInfo;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.driver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.felix.scr.annotations.ReferenceCardinality.MANDATORY_UNARY;
import static org.apache.felix.scr.annotations.ReferencePolicy.DYNAMIC;
import static org.opendaylight.of.controller.OpenflowEventType.DATAPATH_DISCONNECTED;
import static org.opendaylight.of.controller.OpenflowEventType.DATAPATH_READY;
import static org.opendaylight.of.lib.msg.MessageType.PORT_STATUS;
import static org.opendaylight.util.CommonUtils.itemSet;

/**
 * Device supplier that uses OpenFlow controller.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
public class OpenFlowDeviceSupplier implements DeviceSupplier {

    private final Logger log = LoggerFactory.getLogger(OpenFlowDeviceSupplier.class);

    private static final String MSG_STARTED = "OpenFlowDeviceSupplier started";
    private static final String MSG_STOPPED = "OpenFlowDeviceSupplier stopped";

    private static final String MSG_CONNECT = "Switch {} connected";
    private static final String MSG_DISCONNECT = "Switch {} disconnected";
    private static final String E_QUEUE = "QueueEvent: {}";

    // FIXME: change this
    private static final String DEFAULT_DEVICE_TYPE_NAME = "mock";
    private final DefaultDeviceType mockDeviceType = new MockDefaultDeviceType();

    private final SupplierId SUPPLIER_ID =
            new SupplierId(getClass().getPackage().getName());

    @Reference(name = "ControllerService",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile ControllerService controllerService;

    @Reference(name = "DeviceSuppliersBroker",
               cardinality = MANDATORY_UNARY, policy = DYNAMIC)
    protected volatile DeviceSuppliersBroker broker;

    private DeviceSupplierService supplierService;
    private final DataPathListener switchListener = new SwitchListener();
    private final MessageListener portListener = new PortListener();


    @Override
    public SupplierId supplierId() {
        return SUPPLIER_ID;
    }

    @Activate
    public void activate() {
        controllerService.addDataPathListener(switchListener);
        controllerService.addMessageListener(portListener, itemSet(PORT_STATUS));
        supplierService = broker.registerSupplier(this);
        log.info(MSG_STARTED);
    }

    @Deactivate
    public void deactivate() {
        controllerService.removeDataPathListener(switchListener);
        controllerService.removeMessageListener(portListener);
        broker.unregisterSupplier(this);
        log.info(MSG_STOPPED);
    }


    // Interceptor for datapath events
    private class SwitchListener implements DataPathListener {
        @Override
        public void queueEvent(QueueEvent event) {
            log.warn(E_QUEUE, event);
        }

        @Override
        public void event(DataPathEvent event) {
            DeviceId deviceId = DeviceId.valueOf(event.dpid().toString());

            if (event.type() == DATAPATH_READY) {
                log.debug(MSG_CONNECT, deviceId);
                DataPathInfo dpi = controllerService.getDataPathInfo(event.dpid());
                if (dpi != null) {
                    createOrUpdateDevice(dpi, deviceId);
                }

            } else if (event.type() == DATAPATH_DISCONNECTED) {
                log.debug(MSG_DISCONNECT, deviceId);
                supplierService.setOnline(deviceId, false);
            }
        }
    }

    // Interceptor for datapath port events
    private class PortListener implements MessageListener {
        @Override
        public void queueEvent(QueueEvent event) {
            log.warn(E_QUEUE, event);
        }

        @Override
        public void event(MessageEvent event) {
            DataPathInfo dpi = controllerService.getDataPathInfo(event.dpid());
            if (dpi != null)
                updateInterfaces(DeviceId.valueOf(dpi.dpid().toString()), dpi);
        }
    }

    // Creates a new device from the supplied datapath infoD
    private void createOrUpdateDevice(DataPathInfo dpi, DeviceId deviceId) {
        URI uri = URI.create(UriSchemes.OPENFLOW + ":" + dpi.dpid());

        // Synthesize a device info for the mock device type
        DeviceInfo info = new DefaultDeviceInfo(mockDeviceType);

        // Get facet to inject information into the device info
        ManualIdentity mif = info.getFacet(ManualIdentity.class);

        // Use it to inject information from DataPathInfo
        mif.setVendor(dpi.manufacturerDescription());
        mif.setProductNumber(dpi.hardwareDescription());
        mif.setModel(dpi.hardwareDescription());
        mif.setFirmwareVersion(dpi.softwareDescription());
        mif.setSerialNumber(dpi.serialNumber());

        // Now create/update the device, its interfaces and mark it online
        Device device = supplierService.createOrUpdateDevice(deviceId, itemSet(uri), info);
        updateInterfaces(device.id(), dpi);
        supplierService.setOnline(deviceId, true);
    }

    // Creates or updates device interfaces from the supplied datapath info
    private void updateInterfaces(DeviceId deviceId, DataPathInfo dpi) {
        List<InterfaceInfo> infos = new ArrayList<>();
        for (Port port : dpi.ports()) {
            // We deliberately suppress local OpenFlow port
            if (port.getPortNumber() == Port.LOCAL)
                continue;

            DefaultInterfaceInfo info = new DefaultInterfaceInfo(deviceId, port.getPortNumber());
            info.name(port.getName()).mac(port.getHwAddress())
                    .state(decodeState(port));
            infos.add(info);
        }

        supplierService.updateInterfaces(deviceId, infos);
    }

    // Converts OF Port state to Interface state
    private Set<Interface.State> decodeState(Port p) {
        Set<Interface.State> ifState = new TreeSet<Interface.State>();
        if (p.isBlocked()) {
            ifState.add(Interface.State.BLOCKED);
        }
        if (p.isEnabled() && p.isLinkUp()) {
            ifState.add(Interface.State.UP);
        } else {
            ifState.add(Interface.State.DOWN);
        }
        return ifState;
    }

    // Temporary mock bypass of the device driver subsystem.
    private class MockDefaultDeviceType extends DefaultDeviceType {
        MockDefaultDeviceType() {
            super(null, DEFAULT_DEVICE_TYPE_NAME);
            addBinding(DeviceIdentity.class, DefaultDeviceIdentity.class);
            addBinding(ManualIdentity.class, ManualDeviceIdentityHandler.class);
        }
    }

}
