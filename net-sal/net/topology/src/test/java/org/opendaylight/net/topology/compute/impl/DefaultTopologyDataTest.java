/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.junit.Test;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.TopologyCluster;
import org.opendaylight.net.topology.TopoVertex;

import static org.opendaylight.net.topology.compute.impl.TopoTestUtils.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of the default topo data descriptor.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologyDataTest {

    private DefaultTopologyData dtd = new DefaultTopologyData();

    private void validate(int vc, int ec, int cc) {
        assertEquals("incorrect vertex count", vc, dtd.graph().getVertices().size());
        assertEquals("incorrect edge count", ec, dtd.graph().getEdges().size());
        assertEquals("incorrect cluster count", cc, dtd.clusters().size());
    }

    // Validates number of paths from the given source
    private void assertPaths(String src, int pc) {
        assertEquals("incorrect path count for " + src, pc,
                     dtd.searchResults(DeviceId.valueOf(src)).paths().size());
    }

    // Validates number of paths for the given source and destination
    private void assertPaths(String src, String dst, int pc) {
        DeviceId did = DeviceId.valueOf(dst);
        DeviceId sid = DeviceId.valueOf(src);
        int c = 0;
        for (org.opendaylight.util.graph.Path p : dtd.searchResults(sid).paths())
            if (((TopoVertex) p.dst()).deviceId().equals(did))
                c++;
        assertEquals("incorrect path count for " + src + "/" + dst, pc, c);
    }

    @Test
    public void empty() {
        dtd.build(devices(), links());
        validate(0, 0, 0);
    }

    @Test
    public void singleDevice() {
        dtd.build(devices(device("1")), links());
        validate(1, 0, 1);
    }

    @Test
    public void twoDeviceTwoClusters() {
        dtd.build(devices(device("1"), device("2")), links());
        validate(2, 0, 2);
        assertPaths("1", 0);
    }

    @Test
    public void twoDeviceOneCluster() {
        dtd.build(devices(device("1"), device("2")),
                  links(link("1", "2"), link("2", "1")));
        validate(2, 2, 1);
        assertPaths("1", "2", 1);
        assertPaths("2", "1", 1);
    }

    @Test
    public void twoDeviceOneClusterDeviceDiscrepancy() {
        dtd.build(devices(device("1")),
                  links(link("1", "2"), link("2", "1")));
        validate(2, 2, 1);
        assertPaths("1", "2", 1);
        assertPaths("2", "1", 1);
    }

    @Test
    public void quad() {
        dtd.build(devices(device("1"), device("2"), device("3"), device("4")),
                  links(link("1", "2"), link("2", "1"),
                        link("3", "2"), link("2", "3"),
                        link("1", "4"), link("4", "1"),
                        link("3", "4"), link("4", "3")));
        validate(4, 8, 1);
        assertPaths("1", 4);
        assertPaths("2", 4);
        assertPaths("3", 4);
        assertPaths("4", 4);
        assertPaths("1", "2", 1);
        assertPaths("1", "3", 2);

        TopologyCluster tc = dtd.clusterFor(DeviceId.valueOf("1"));
        assertEquals("incorrect device count", 4, dtd.clusterDevices(tc).size());
        assertEquals("incorrect link count", 8, dtd.clusterLinks(tc).size());
    }

    @Test
    public void splitQuad() {
        dtd.build(devices(device("1"), device("2"), device("3"), device("4")),
                  links(link("3", "2"), link("2", "3"),
                        link("1", "4"), link("4", "1")));
        validate(4, 4, 2);
        assertPaths("1", 1);
        assertPaths("2", 1);
        assertPaths("3", 1);
        assertPaths("4", 1);
        assertPaths("1", "2", 0);
        assertPaths("1", "4", 1);

        TopologyCluster tc = dtd.clusterFor(DeviceId.valueOf("1"));
        assertEquals("incorrect device count", 2, dtd.clusterDevices(tc).size());
        assertEquals("incorrect link count", 2, dtd.clusterLinks(tc).size());
        assertTrue("incorrect toString", dtd.toString().contains("computeTimeNano="));
    }

    @Test
    public void timestamp() {
        long then = System.nanoTime();
        dtd.build(devices(device("1"), device("2"), device("3"), device("4")),
                  links(link("3", "2"), link("2", "3"),
                        link("1", "4"), link("4", "1")));
        long now = System.nanoTime();
        assertTrue("incorrect timestamp", then <= dtd.ts() && dtd.ts() <= now);
    }

    @Test(expected = IllegalStateException.class)
    public void rebuild() {
        dtd.build(devices(device("1")), links());
        dtd.build(devices(device("2")), links());
    }

}
