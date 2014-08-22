/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.device.*;
import org.opendaylight.net.link.*;
import org.opendaylight.net.model.Device;
import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.ModelEvent;
import org.opendaylight.net.topology.TopologyData;
import org.opendaylight.net.topology.TopologySupplier;
import org.opendaylight.net.topology.TopologySupplierService;
import org.opendaylight.net.topology.TopologySuppliersBroker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.opendaylight.net.topology.compute.impl.TopoTestUtils.*;
import static org.junit.Assert.*;
import static org.opendaylight.net.link.LinkEvent.Type.LINK_REMOVED;

/**
 * Test of the default topo supplier.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologySupplierTest {

    private static final long WAIT = 2000;

    private TestTopologySupplier dts = new TestTopologySupplier();
    private FakeDeviceService fds = new FakeDeviceService();
    private FakeLinkService fls = new FakeLinkService();
    private FakeSupplierService fss = new FakeSupplierService();
    private FakeBrokerService fbs = new FakeBrokerService();

    @Before
    public void setUp() {
        dts.deviceService = fds;
        dts.linkService = fls;
        dts.broker = fbs;
    }

    @After
    public void tearDown() {
        dts.deactivate();
        assertNull("device listener should be unregistered", fds.listener);
        assertNull("link listener should be unregistered", fls.listener);
        assertNull("topo supplier should be unregistered", fbs.supplier);
    }

    private void validate(int vc, int ec, int cc) {
        assertEquals("incorrect vertex count", vc, fss.data.graph().getVertices().size());
        assertEquals("incorrect edge count", ec, fss.data.graph().getEdges().size());
        assertEquals("incorrect cluster count", cc, fss.data.clusters().size());
    }

    @Test
    public void basics() {
        fds.devices = devices(device("1"), device("2"), device("3"), device("4"));
        fls.links = links(link("1", "2"), link("2", "1"),
                          link("3", "2"), link("2", "3"),
                          link("1", "4"), link("4", "1"),
                          link("3", "4"), link("4", "3"));

        dts.activate();

        assertNotNull("device listener should be registered", fds.listener);
        assertNotNull("link listener should be registered", fls.listener);
        assertNotNull("supplier should be registered", fbs.supplier);

        fss.waitForIt();
        validate(4, 8, 1);
    }

    @Test
    public void eventTriggered() {
        basics();

        fss.reset();
        fds.devices = devices(device("1"), device("2"), device("3"), device("4"));
        fls.links = links(link("3", "2"), link("2", "3"),
                          link("1", "4"), link("4", "1"),
                          link("3", "4"), link("4", "3"));

        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("1", "2")));
        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("2", "1")));
        fss.waitForIt();
        validate(4, 6, 1);
    }

    @Test
    public void clusterSplit() {
        basics();

        fss.reset();
        fds.devices = devices(device("1"), device("2"), device("3"), device("4"));
        fls.links = links(link("3", "2"), link("2", "3"),
                          link("1", "4"), link("4", "1"));

        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("1", "2")));
        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("2", "1")));
        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("3", "4")));
        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("4", "3")));
        fss.waitForIt();
        validate(4, 4, 2);
    }

    @Test
    public void deviceAdded() {
        basics();

        fss.reset();
        fds.devices = devices(device("1"), device("2"), device("3"), device("4"), device("5"));
        fls.links = links(link("3", "2"), link("2", "3"),
                          link("1", "4"), link("4", "1"),
                          link("3", "4"), link("4", "3"));

        fls.listener.event(new DefaultLinkEvent(LINK_REMOVED, link("1", "2")));
        fds.listener.event(new DefaultDeviceEvent(DeviceEvent.Type.DEVICE_ADDED, device("5"), null));
        fss.waitForIt();
        validate(5, 6, 2);
    }

    // Instrumentation for the supplier code under test.
    private class TestTopologySupplier extends DefaultTopologySupplier {
    }


    // Fake device service implementation
    private class FakeDeviceService extends DeviceServiceAdapter {

        Iterator<Device> devices;
        DeviceListener listener;

        @Override
        public Iterator<Device> getDevices() {
            return devices;
        }

        @Override
        public void addListener(DeviceListener listener) {
            assertNull("listener already exists", this.listener);
            this.listener = listener;
        }

        @Override
        public void removeListener(DeviceListener listener) {
            assertSame("incorrect listener", this.listener, listener);
            this.listener = null;
        }
    }

    // Fake link service implementation
    private class FakeLinkService extends LinkServiceAdapter {

        Iterator<Link> links;
        LinkListener listener;

        @Override
        public Iterator<Link> getLinks() {
            return links;
        }

        @Override
        public void addListener(LinkListener listener) {
            assertNull("listener already exists", this.listener);
            this.listener = listener;
        }

        @Override
        public void removeListener(LinkListener listener) {
            assertSame("incorrect listener", this.listener, listener);
            this.listener = null;
        }
    }

    // Fake broker service
    private class FakeBrokerService implements TopologySuppliersBroker {

        TopologySupplier supplier;

        @Override
        public TopologySupplierService registerSupplier(TopologySupplier supplier) {
            this.supplier = supplier;
            return fss;
        }

        @Override
        public void unregisterSupplier(TopologySupplier supplier) {
            assertSame("incorrect supplier", this.supplier, supplier);
            this.supplier = null;
        }

        @Override
        public Set<TopologySupplier> getSuppliers() {
            Set<TopologySupplier> s = new HashSet<>();
            if (supplier != null)
                s.add(supplier);
            return s;
        }
    }

    // Fake topo supplier service
    private class FakeSupplierService implements TopologySupplierService {

        int count = 0;
        TopologyData data;
        List<ModelEvent> reasons;

        @Override
        public void submit(TopologyData data, List<ModelEvent> reasons) {
            synchronized (this) {
                count++;
                this.data = data;
                this.reasons = reasons;
                notifyAll();
            }
        }

        void waitForIt() {
            synchronized (this) {
                if (data == null)
                    try {
                        wait(WAIT);
                        assertEquals("incorrect invocation count", 1, count);
                    } catch (InterruptedException e) {
                        fail("timed out waiting for topology");
                    }
            }
        }

        void reset() {
            count = 0;
            data = null;
            reasons = null;
        }
    }

}
