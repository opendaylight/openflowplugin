/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.host.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.dispatch.impl.TestEventDispatcher;
import org.opendaylight.net.host.HostEvent;
import org.opendaylight.net.host.HostListener;
import org.opendaylight.net.host.HostSupplier;
import org.opendaylight.net.host.HostSupplierService;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.opendaylight.net.host.HostEvent.Type.*;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.junit.TestTools.sizeOf;
import static org.opendaylight.util.net.IpAddress.ip;
import static org.opendaylight.util.net.MacAddress.mac;

/**
 * Test of the host tracking service implementation
 *
 * @author Thomas Vachuska
 */
public class HostManagerTest extends AbstractTest {

    private static final DeviceId DID1 = DeviceId.valueOf("1111");
    private static final DeviceId DID2 = DeviceId.valueOf("2222");

    private static final MacAddress MAC1 = mac("00:00:00:00:00:11");
    private static final MacAddress MAC2 = mac("00:00:00:00:00:22");

    private static final IpAddress IP1 = ip("12.34.56.78");
    private static final IpAddress IP2 = ip("12.34.56.99");

    private static final SegmentId SEG1 = SegmentId.valueOf(VlanId.vlan(1));
    private static final SegmentId SEG2 = SegmentId.valueOf(VlanId.vlan(2));

    private static final HostId HID1 = HostId.valueOf(IP1, SEG1);
    private static final HostId HID2 = HostId.valueOf(IP2, SEG1);

    private static final BigPortNumber P1 = BigPortNumber.bpn(1);
    private static final BigPortNumber P2 = BigPortNumber.bpn(2);

    private static final InterfaceId IF1 = InterfaceId.valueOf(P1);
    private static final InterfaceId IF2 = InterfaceId.valueOf(P2);

    private final HostManager hm = new HostManager();
    private final HostTestSupplier hts = new HostTestSupplier();

    private HostSupplierService hss;

    private final EventDispatchService eds = new TestEventDispatcher();

    @Before
    public void setUp() {
        hm.dispatchService = eds;
        hm.activate();
        hss = hm.registerSupplier(hts);
    }

    @After
    public void tearDown() {
        hm.deactivate();
    }

    @Test
    public void listeners() {
        title("listeners");
        HostListener listener = new TestListener();
        print(hm);
        assertEquals("no listeners expected", 0, hm.getListeners().size());

        hm.addListener(listener);
        print(hm);
        assertEquals("no listeners expected", 1, hm.getListeners().size());
        assertSame("not same ref", listener, hm.getListeners().iterator().next());

        hm.removeListener(listener);
        print(hm);
        assertEquals("no listeners expected", 0, hm.getListeners().size());
    }


    private void validateEvents(List<HostEvent> actual, HostEvent.Type... types) {
        assertEquals("incorrect event count", types.length, actual.size());
        int i = 0;
        for (HostEvent event : actual) {
            assertEquals("incorrect event type", types[i], event.type());
            i++;
        }
        actual.clear();
    }


    private Interface mkInterface(InterfaceId iid, HostId hid, BigPortNumber p) {
        return new DefaultInterface(iid, new DefaultInterfaceInfo(hid, p));
    }


    @Test
    public void addHost() {
        title("addHost");
        TestListener listener = new TestListener();
        hm.addListener(listener);
        print(hm);

        Interface netInterface = mkInterface(IF1, HID1, P1);
        HostLocation loc = new DefaultHostLocation(DID1, IF2);
        DefaultHostInfo dhi = new DefaultHostInfo(netInterface, MAC1, loc);
        assertEquals("incorrect host count", 0, sizeOf(hm.getHosts()));

        Host h = hss.createOrUpdateHost(HID1, dhi);
        print(hm);
        assertTrue("host not found", hm.getHost(HID1).equals(h));
        assertEquals("incorrect host count", 1, sizeOf(hm.getHosts()));
        validateEvents(listener.events, HOST_ADDED);
    }

    @Test
    public void updateHost() {
        title("updateHost");
        TestListener listener = new TestListener();
        hm.addListener(listener);
        print(hm);

        Interface netInterface = mkInterface(IF1, HID1, P1);
        HostLocation loc = new DefaultHostLocation(DID1, IF2);
        DefaultHostInfo dhi = new DefaultHostInfo(netInterface, MAC1, loc);
        Host h1 = hss.createOrUpdateHost(HID1, dhi);
        print(hm);
        assertEquals("incorrect host count", 1, sizeOf(hm.getHosts()));
        validateEvents(listener.events, HOST_ADDED);

        loc = new DefaultHostLocation(DID1, IF1);
        dhi = new DefaultHostInfo(netInterface, MAC1, loc);
        Host h2 = hss.createOrUpdateHost(HID1, dhi);
        print(hm);
        assertEquals("incorrect host count", 1, sizeOf(hm.getHosts()));
        assertSame("d1 and d2 should be same", h1, h2);
        validateEvents(listener.events, HOST_MOVED);
    }

    @Test
    public void removeHost() {
        title("removeHost");
        TestListener listener = new TestListener();
        hm.addListener(listener);
        print(hm);

        Interface netInterface = mkInterface(IF1, HID1, P1);
        HostLocation loc = new DefaultHostLocation(DID1, IF2);
        DefaultHostInfo dhi = new DefaultHostInfo(netInterface, MAC1, loc);
        Host h1 = hss.createOrUpdateHost(HID1, dhi);
        print(hm);
        assertEquals("incorrect host count", 1, sizeOf(hm.getHosts()));
        validateEvents(listener.events, HOST_ADDED);

        hss.removeHost(HID1);
        print(hm);
        assertEquals("incorrect device count", 0, sizeOf(hm.getHosts()));
        validateEvents(listener.events, HOST_REMOVED);
    }


    private class HostTestSupplier implements HostSupplier {
        private final SupplierId supplierId = new SupplierId("foobar");

        @Override
        public SupplierId supplierId() {
            return supplierId;
        }
    }

    private class TestListener implements HostListener {
        private final List<HostEvent> events = new ArrayList<>();

        @Override
        public void event(HostEvent event) {
            print(">>>> {}", event);
            events.add(event);
        }
    }
}
