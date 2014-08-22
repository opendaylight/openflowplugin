/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.impl;

import org.opendaylight.net.topology.compute.impl.DefaultTopologyData;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.topology.LinkWeight;

import java.util.Set;

import static org.opendaylight.net.topology.compute.impl.TopoTestUtils.*;
import static org.opendaylight.util.net.BigPortNumber.bpn;
import static org.junit.Assert.*;

/**
 * Test of the default topo cluster descriptor.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologyTest {

    public static final SupplierId SID = new SupplierId("foo");

    public static final DeviceId D1 = DeviceId.valueOf("1");
    public static final DeviceId D2 = DeviceId.valueOf("2");
    public static final DeviceId D3 = DeviceId.valueOf("3");
    public static final DeviceId D4 = DeviceId.valueOf("4");
    public static final DeviceId D5 = DeviceId.valueOf("5");

    public static final InterfaceId P1 = InterfaceId.valueOf(bpn(1));
    public static final InterfaceId P2 = InterfaceId.valueOf(bpn(2));

    public static final LinkWeight weight = new LinkWeight() {
        @Override
        public double weight(Link link) {
            return link.src().elementId().equals(D4) ||
                    link.dst().elementId().equals(D4) ? 2.0 : 1.0;
        }
    };

    private DefaultTopology dt;

    @Before
    public void setUp() {
        long now = System.currentTimeMillis();
        DefaultTopologyData data = new DefaultTopologyData();

        data.build(devices(device("1"), device("2"), device("3"), device("4"),
                           device("5")),
                   links(link("1", "2"), link("2", "1"),
                         link("3", "2"), link("2", "3"),
                         link("1", "4"), link("4", "1"),
                         link("3", "4"), link("4", "3"))
        );

        dt = new DefaultTopology(SID, data, now);
        assertEquals("incorrect supplier", SID, dt.supplierId());
        assertEquals("incorrect time", now, dt.activeAt());
        assertEquals("incorrect device count", 5, dt.deviceCount());
        assertEquals("incorrect link count", 8, dt.linkCount());
        assertEquals("incorrect cluster count", 2, dt.clusterCount());
        assertEquals("incorrect backing data", data, dt.data());
    }

    @Test
    public void preComputedPaths() {
        Set<Path> paths = dt.getPaths(D1, D2);
        assertEquals("incorrect path count", 1, paths.size());

        paths = dt.getPaths(D1, D3);
        assertEquals("incorrect path count", 2, paths.size());

        paths = dt.getPaths(D1, D5);
        assertNull("no paths expected", paths);
    }

    @Test
    public void pathViability() {
        assertTrue("path should be viable", dt.isPathViable(D1, D2));
        assertFalse("path should not be viable", dt.isPathViable(D1, D5));
    }

    @Test
    public void onDemandPaths() {
        Set<Path> paths = dt.getPaths(D1, D3, weight);
        assertEquals("incorrect path count", 1, paths.size());
    }

    @Test
    public void isInfra() {
        assertTrue("should be infrastructure point",
                   dt.isInfrastructure(new DefaultConnectionPoint(D1, P1)));
        assertFalse("should not be infrastructure point",
                    dt.isInfrastructure(new DefaultConnectionPoint(D1, P2)));
    }

    @Test
    public void clusters() {
        Set<TopologyCluster> clusters = dt.getClusters();
        assertEquals("incorrect cluster count", 2, clusters.size());

        TopologyCluster c = dt.getCluster(D1);
        Set<DeviceId> devs = dt.getClusterDevices(c);
        assertEquals("incorrect cluster device count", 4, devs.size());
        assertTrue("cluster should contain D2", devs.contains(D2));
        assertFalse("cluster should not contain D5", devs.contains(D5));
    }

    @Test
    public void isBroadcast() {
//        assertFalse("should not be broadcast",
//                   dt.isBroadcastAllowed(new DefaultConnectionPoint(D1, P1)));
        assertTrue("should be broadcast",
                   dt.isBroadcastAllowed(new DefaultConnectionPoint(D2, P1)));
        assertTrue("should be broadcast",
                   dt.isBroadcastAllowed(new DefaultConnectionPoint(D3, P1)));
        assertTrue("should be broadcast",
                   dt.isBroadcastAllowed(new DefaultConnectionPoint(D4, P1)));
        assertTrue("should be broadcast",
                   dt.isBroadcastAllowed(new DefaultConnectionPoint(D5, P1)));
    }

}
