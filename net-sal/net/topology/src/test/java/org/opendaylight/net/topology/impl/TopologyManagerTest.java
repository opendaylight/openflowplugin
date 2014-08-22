/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.dispatch.impl.CoreEventDispatcher;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.topology.TopologyEvent;
import org.opendaylight.net.topology.TopologyListener;
import org.opendaylight.net.topology.TopologySupplier;
import org.opendaylight.net.topology.TopologySupplierService;
import org.opendaylight.net.topology.compute.impl.DefaultTopologyData;
import org.opendaylight.util.api.ServiceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.net.topology.compute.impl.TopoTestUtils.*;
import static org.opendaylight.net.topology.impl.DefaultTopologyTest.*;
import static org.opendaylight.util.junit.TestTools.delay;

/**
 * Test of the topology manager.
 *
 * @author Thomas Vachuska
 */
public class TopologyManagerTest {

    private final TopologyManager tm = new TopologyManager();
    private final CoreEventDispatcher eds = new CoreEventDispatcher();
    private final TopologySupplier supplier = new TestSupplier();
    private final DefaultTopologyData data = new DefaultTopologyData();
    private final List<ModelEvent> reasons = new ArrayList<>();
    private final TestListener listener = new TestListener();

    private TopologySupplierService tss;

    @Before
    public void setUp() {
        eds.activate();

        tm.dispatchService = eds;
        tm.activate();

        tss = tm.registerSupplier(supplier);
        assertEquals("incorrect supplier count", 1, tm.getSuppliers().size());
        assertTrue("supplier not registered", tm.getSuppliers().contains(supplier));

        tm.addListener(listener);
        assertEquals("incorrect listener count", 1, tm.getListeners().size());
        assertTrue("listener not registered", tm.getListeners().contains(listener));
    }

    @After
    public void tearDown() {
        tm.unregisterSupplier(supplier);
        assertEquals("incorrect supplier count", 0, tm.getSuppliers().size());
        assertFalse("supplier still registered", tm.getSuppliers().contains(supplier));

        tm.removeListener(listener);
        assertEquals("incorrect listener count", 0, tm.getListeners().size());
        assertFalse("listener still registered", tm.getListeners().contains(listener));

        tm.deactivate();
    }

    @Test
    public void submit() {
        long now = System.currentTimeMillis();
        data.build(devices(device("1"), device("2"), device("3"), device("4"),
                           device("5")),
                   links(link("1", "2"), link("2", "1"),
                         link("3", "2"), link("2", "3"),
                         link("1", "4"), link("4", "1"),
                         link("3", "4"), link("4", "3"))
        );
        tss.submit(data, reasons);

        Topology topo = tm.getTopology();
        assertEquals("incorrect supplier", SID, topo.supplierId());
        assertTrue("incorrect time", now <= topo.activeAt());
        assertEquals("incorrect device count", 5, topo.deviceCount());
        assertEquals("incorrect link count", 8, topo.linkCount());
        assertEquals("incorrect cluster count", 2, topo.clusterCount());
    }

    @Test
    public void outOfSequence() {
        DefaultTopologyData newData = new DefaultTopologyData();

        // Build in order
        data.build(devices(device("1"), device("2")), links(link("1", "2")));
        newData.build(devices(device("1"), device("2")),
                   links(link("1", "2"), link("2", "1")));

        // Submit out of order
        tss.submit(newData, reasons);
        tss.submit(data, reasons);

        // Validate that the old data did not superceede the new data
        Topology topo = tm.getTopology();
        assertEquals("incorrect link count", 2, topo.linkCount());
    }

    @Test
    public void eventDelivery() {
        submit();
        delay(100);
        assertEquals("incorrect event count", 1, listener.events.size());
    }

    @Test
    public void preComputedPaths() {
        submit();
        Set<Path> paths = tm.getPaths(D1, D2);
        assertEquals("incorrect path count", 1, paths.size());

        paths = tm.getPaths(D1, D3);
        assertEquals("incorrect path count", 2, paths.size());

        paths = tm.getPaths(D1, D5);
        assertNull("no paths expected", paths);
    }

    @Test
    public void pathViability() {
        submit();
        assertTrue("path should be viable", tm.isPathViable(D1, D2));
        assertFalse("path should not be viable", tm.isPathViable(D1, D5));
    }

    @Test
    public void onDemandPaths() {
        submit();
        Set<Path> paths = tm.getPaths(D1, D3, weight);
        assertEquals("incorrect path count", 1, paths.size());
    }

    @Test
    public void isInfra() {
        submit();
        assertTrue("should be infrastructure point",
                   tm.isInfrastructure(new DefaultConnectionPoint(D1, P1)));
        assertFalse("should not be infrastructure point",
                    tm.isInfrastructure(new DefaultConnectionPoint(D1, P2)));
    }

    @Test
    public void clusters() {
        submit();
        Set<TopologyCluster> clusters = tm.getClusters();
        assertEquals("incorrect cluster count", 2, clusters.size());

        TopologyCluster c = tm.getCluster(D1);
        Set<DeviceId> devs = tm.getClusterDevices(c);
        assertEquals("incorrect cluster device count", 4, devs.size());
        assertTrue("cluster should contain D2", devs.contains(D2));
        assertFalse("cluster should not contain D5", devs.contains(D5));
    }

    @Test
    public void isBroadcast() {
        submit();
//        assertFalse("should not be broadcast",
//                    tm.isBroadcastAllowed(new DefaultConnectionPoint(D1, P1)));
        assertTrue("should be broadcast",
                   tm.isBroadcastAllowed(new DefaultConnectionPoint(D2, P1)));
        assertTrue("should be broadcast",
                   tm.isBroadcastAllowed(new DefaultConnectionPoint(D3, P1)));
        assertTrue("should be broadcast",
                   tm.isBroadcastAllowed(new DefaultConnectionPoint(D4, P1)));
        assertTrue("should be broadcast",
                   tm.isBroadcastAllowed(new DefaultConnectionPoint(D5, P1)));
    }

    @Test(expected = ServiceNotFoundException.class)
    public void noData() {
        tm.getTopology();
    }

    // Fake supplier
    private class TestSupplier implements TopologySupplier {
        @Override
        public SupplierId supplierId() {
            return SID;
        }
    }

    // Interception listener
    private class TestListener implements TopologyListener {

        private final List<TopologyEvent> events = new ArrayList<>();

        @Override
        public void event(TopologyEvent event) {
            events.add(event);
        }
    }
}
