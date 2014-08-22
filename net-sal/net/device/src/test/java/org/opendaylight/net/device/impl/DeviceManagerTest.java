/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.device.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.device.DeviceEvent;
import org.opendaylight.net.device.DeviceListener;
import org.opendaylight.net.device.DeviceSupplier;
import org.opendaylight.net.device.DeviceSupplierService;
import org.opendaylight.net.dispatch.impl.TestEventDispatcher;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.driver.*;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.net.device.DeviceEvent.Type.*;
import static org.opendaylight.of.lib.dt.DataPathId.dpid;
import static org.opendaylight.util.CommonUtils.itemSet;
import static org.opendaylight.util.junit.TestTools.sizeOf;

/**
 * Test of the device tracking service implementation.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
public class DeviceManagerTest {

    private static final TestDeviceType DEVICE_TYPE = new TestDeviceType();

    private static final DeviceId DID1 = DeviceId.valueOf("1111");
    private static final DeviceId DID2 = DeviceId.valueOf("2222");

    private static final IpAddress IP1 = IpAddress.valueOf("1.2.3.4");
    private static final IpAddress IP2 = IpAddress.valueOf("1.2.3.5");

    private static final DataPathId DPID1 = dpid("12:34:aa:bb:cc:dd:ee:ff");
    private static final DataPathId DPID2 = dpid("12:35:aa:bb:cc:dd:ee:ff");

    private static final URI URI1 = URI.create("of:" + DPID1.toString());
    private static final URI URI2 = URI.create("bar:1111");

    private static final BigPortNumber P1 = BigPortNumber.bpn(1);
    private static final BigPortNumber P2 = BigPortNumber.bpn(2);
    private static final BigPortNumber P3 = BigPortNumber.bpn(3);

    private final DeviceManager dm = new DeviceManager();
    private final DeviceTestSupplier dts = new DeviceTestSupplier();

    private DeviceSupplierService dss;

    private final EventDispatchService eds = new TestEventDispatcher();

    @Before
    public void setUp() {
        dm.dispatchService = eds;
        dm.activate();
        dss = dm.registerSupplier(dts);
    }

    @After
    public void tearDown() {
        dm.deactivate();
    }

    @Test
    public void listeners() {
        DeviceListener listener = new TestListener();
        assertEquals("no listeners expected", 0, dm.getListeners().size());
        dm.addListener(listener);
        assertEquals("no listeners expected", 1, dm.getListeners().size());
        dm.removeListener(listener);
        assertEquals("no listeners expected", 0, dm.getListeners().size());
    }


    private void validateEvents(List<DeviceEvent> actual, DeviceEvent.Type... types) {
        assertEquals("incorrect event count", types.length, actual.size());
        int i = 0;
        for (DeviceEvent event : actual) {
            assertEquals("incorrect event type", types[i], event.type());
            i++;
        }
        actual.clear();
    }

    @Test
    public void addDevice() {
        TestListener listener = new TestListener();
        dm.addListener(listener);

        DefaultDeviceInfo ddi = new DefaultDeviceInfo(DEVICE_TYPE);
        assertEquals("incorrect device count", 0, sizeOf(dm.getDevices()));

        Device d = dss.createOrUpdateDevice(DID1, itemSet(URI1), ddi);
        assertTrue("device not found", dm.getDevice(DID1).equals(d));
        assertEquals("incorrect device count", 1, sizeOf(dm.getDevices()));
        validateEvents(listener.events, DEVICE_ADDED);
    }

    @Test
    public void updateDevice() {
        TestListener listener = new TestListener();
        dm.addListener(listener);

        DefaultDeviceInfo ddi = new DefaultDeviceInfo(DEVICE_TYPE);
        Device d1 = dss.createOrUpdateDevice(DID1, itemSet(URI1), ddi);
        assertEquals("incorrect device count", 1, sizeOf(dm.getDevices()));
        validateEvents(listener.events, DEVICE_ADDED);

        Device d2 = dss.createOrUpdateDevice(DID1, itemSet(URI2), ddi);
        assertEquals("incorrect device count", 1, sizeOf(dm.getDevices()));
        assertSame("d1 and d2 should be same", d1, d2);
        validateEvents(listener.events, DEVICE_UPDATED);
    }

    @Test
    public void removeDevice() {
        TestListener listener = new TestListener();
        dm.addListener(listener);

        DefaultDeviceInfo ddi = new DefaultDeviceInfo(DEVICE_TYPE);
        Device d1 = dss.createOrUpdateDevice(DID1, itemSet(URI1), ddi);
        assertEquals("incorrect device count", 1, sizeOf(dm.getDevices()));
        validateEvents(listener.events, DEVICE_ADDED);

        dss.removeDevice(DID1);
        assertEquals("incorrect device count", 0, sizeOf(dm.getDevices()));
        validateEvents(listener.events, DEVICE_REMOVED);
    }

    @Test
    public void updateAvailability() {
        TestListener listener = new TestListener();
        dm.addListener(listener);

        DefaultDeviceInfo ddi = new DefaultDeviceInfo(DEVICE_TYPE);
        Device d1 = dss.createOrUpdateDevice(DID1, itemSet(URI1), ddi);
        assertEquals("incorrect device count", 1, sizeOf(dm.getDevices()));
        validateEvents(listener.events, DEVICE_ADDED);
        assertTrue("incorrect online state", dm.getDevice(DID1).isOnline());

        dss.setOnline(DID1, false);
        assertEquals("incorrect device count", 1, sizeOf(dm.getDevices()));
        validateEvents(listener.events, DEVICE_AVAILABILITY_CHANGED);
        assertFalse("incorrect online state", dm.getDevice(DID1).isOnline());
    }


    private void addTestDevices() {
        DefaultDeviceInfo ddi = new DefaultDeviceInfo(DEVICE_TYPE);
        ddi.set(CoreDevicePropertyKeys.IP_ADDRESS, IP1.toString());
        Device d = dss.createOrUpdateDevice(DID1, itemSet(URI1), ddi);

        ddi = new DefaultDeviceInfo(DEVICE_TYPE);
        ddi.set(CoreDevicePropertyKeys.IP_ADDRESS, IP2.toString());
        d = dss.createOrUpdateDevice(DID2, itemSet(URI2), ddi);
    }

    @Test
    public void getDevicesByIp() {
        Set<Device> devices = dm.getDevices(IP1);
        assertTrue("no devices expected", devices.isEmpty());

        addTestDevices();

        devices = dm.getDevices(IP1);
        assertEquals("incorrect device count", 1, devices.size());
        assertEquals("incorrect IP", IP1,
                     devices.iterator().next().info().getFacet(DeviceIdentity.class).getIpAddress());
    }

    @Test
    public void getDevicesByFacet() {
        addTestDevices();
        Iterator<Device> devices = dm.getDevices(DeviceIdentity.class);
        assertEquals("incorrect device count", 2, sizeOf(devices));
    }

    @Test
    public void getDevicesByDpid() {
        addTestDevices();
        Device device = dm.getDevice(DPID1);
        assertEquals("incorrect device", DPID1, device.dpid());
    }

    @Test
    public void getDevicesByURI() {
        addTestDevices();
        Device device = dm.getDevice(URI1);
        assertEquals("incorrect device", DPID1, device.dpid());
    }

    @Test
    public void setName() {
        addTestDevices();
        Device device = dm.getDevice(URI1);
        dm.setName(device, "Dingbat");

        device = dm.getDevice(URI1);
        assertEquals("incorrect name", "Dingbat", device.name());
    }

    @Test
    public void updateInterfaces() {
        TestListener listener = new TestListener();
        dm.addListener(listener);

        addTestDevices();
        listener.events.clear();

        // Let's start with port 1 and port 2; remember port 1 for later
        List<InterfaceInfo> infos = new ArrayList<>();
        DefaultInterfaceInfo dii = new DefaultInterfaceInfo(DID1, P1);
        infos.add(dii);
        infos.add(new DefaultInterfaceInfo(DID1, P2));
        dss.updateInterfaces(DID1, infos);
        validateEvents(listener.events, INTERFACE_ADDED, INTERFACE_ADDED);

        // Fetch device interfaces and make sure they're are as they should be
        Device device = dm.getDevice(DID1);
        List<Interface> netInterfaces = dm.getInterfaces(device);
        assertEquals("incorrect interface count", 2, netInterfaces.size());

        // Nix port 2 and add port 3, then update
        infos.remove(1);
        infos.add(new DefaultInterfaceInfo(DID1, P3));
        dss.updateInterfaces(DID1, infos);
        validateEvents(listener.events, INTERFACE_ADDED, INTERFACE_REMOVED);

        // Again, fetch device interfaces and make sure they're are as they should be
        device = dm.getDevice(DID1);
        netInterfaces = dm.getInterfaces(device);
        assertEquals("incorrect interface count", 2, netInterfaces.size());

        // Now let's change state of port 1 and update
        dii.state(itemSet(Interface.State.DOWN));
        dss.updateInterfaces(DID1, infos);
        validateEvents(listener.events, INTERFACE_STATE_CHANGED);

        // Validate that port 1 interface has the correct state
        Interface netInterface = dm.getInterface(DID1, InterfaceId.valueOf(P1));
        assertFalse("interface should be down", netInterface.isEnabled());
    }


    private static class TestDeviceType extends DefaultDeviceType {
        protected TestDeviceType() {
            super(null, "foo");
            addBinding(DeviceIdentity.class, DefaultDeviceIdentity.class);
        }
    }

    private class DeviceTestSupplier implements DeviceSupplier {
        private final SupplierId supplierId = new SupplierId("foobar");

        @Override
        public SupplierId supplierId() {
            return supplierId;
        }
    }

    private class TestListener implements DeviceListener {
        private final List<DeviceEvent> events = new ArrayList<>();

        @Override
        public void event(DeviceEvent event) {
            events.add(event);
        }
    }

}
