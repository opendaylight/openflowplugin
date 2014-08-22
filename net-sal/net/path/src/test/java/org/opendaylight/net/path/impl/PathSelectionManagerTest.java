/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.path.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.host.HostServiceAdapter;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.topology.LinkWeight;
import org.opendaylight.net.topology.TopologyServiceAdapter;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.util.net.BigPortNumber.bpn;
import static org.opendaylight.util.net.IpAddress.ip;
import static org.opendaylight.util.net.MacAddress.mac;

/**
 * Suite of tests for the path selection service implementation.
 *
 * @author Thomas Vachuska
 */
public class PathSelectionManagerTest {

    private static final LinkWeight WEIGHT = new LinkWeight() {
        @Override public double weight(Link link) { return 3; }
    };

    private final PathSelectionManager psm = new PathSelectionManager();
    private final TestHostService ths = new TestHostService();
    private final TestTopologyService tts = new TestTopologyService();

    private static final SupplierId SID = new SupplierId("foobar");

    private static final InterfaceId IFID1 = InterfaceId.valueOf(bpn(1));
    private static final InterfaceId IFID2 = InterfaceId.valueOf(bpn(2));
    private static final InterfaceId IFID3 = InterfaceId.valueOf(bpn(3));

    private static final IpAddress IP1 = ip("12.34.56.78");
    private static final IpAddress IP2 = ip("99.88.77.66");
    private static final IpAddress IP3 = ip("99.88.77.11");
    private static final IpAddress IP4 = ip("99.88.77.22");

    private static final MacAddress MAC1 = mac("12:34:56:78:90:ab");
    private static final MacAddress MAC2 = mac("99:88:77:66:55:44");
    private static final MacAddress MAC3 = mac("ab:88:77:66:55:44");

    private static final HostId NID1 = HostId.valueOf(IP1, SegmentId.UNKNOWN);
    private static final HostId NID2 = HostId.valueOf(IP2, SegmentId.UNKNOWN);
    private static final HostId NID3 = HostId.valueOf(IP3, SegmentId.UNKNOWN);
    private static final HostId NID4 = HostId.valueOf(IP4, SegmentId.UNKNOWN);

    private static final DeviceId DID1 = DeviceId.valueOf("12:34:56:78:90:ab:cd:ef");
    private static final DeviceId DID2 = DeviceId.valueOf("43:21:56:78:90:ab:cd:ef");
    private static final DeviceId DID3 = DeviceId.valueOf("43:21:56:78:90:ab:cd:ef");

    private static final Interface IF1 =
            new DefaultInterface(IFID1, new DefaultInterfaceInfo(NID1, bpn(1)));
    private static final Interface IF2 =
            new DefaultInterface(IFID2, new DefaultInterfaceInfo(NID2, bpn(2)));
    private static final Interface IF3 =
            new DefaultInterface(IFID2, new DefaultInterfaceInfo(NID3, bpn(2)));

    private static final HostLocation LOC1 = new DefaultHostLocation(DID1, IFID1);
    private static final HostLocation LOC2 = new DefaultHostLocation(DID1, IFID2);
    private static final HostLocation LOC3 = new DefaultHostLocation(DID2, IFID1);
    private static final HostLocation LOC4 = new DefaultHostLocation(DID3, IFID1);

    private static final Host N1 = new DefaultHost(SID, NID1, IF1, MAC1, LOC1);
    private static final Host N2 = new DefaultHost(SID, NID2, IF2, MAC2, LOC2);
    private static final Host N3 = new DefaultHost(SID, NID3, IF3, MAC3, LOC3);
    private static final Host N4 = new DefaultHost(SID, NID4, IF3, MAC3, LOC4);

    @Before
    public void setUp() {
        psm.hostService = ths;
        psm.topologyService = tts;
        psm.activate();
    }

    @After
    public void tearDown() {
        psm.deactivate();
    }

    @Test
    public void isBroadcast() {
        assertTrue("broadcast should be allowed",
                   psm.isBroadcastAllowed(new DefaultConnectionPoint(DeviceId.valueOf("foo"),
                                                                     InterfaceId.NONE)));
    }

    @Test
    public void noPath() {
        ths.hosts.put(IP1, N1);
        ths.hosts.put(IP4, N4);
        assertTrue("paths should be empty", psm.getPaths(N1, N4).isEmpty());
        assertTrue("paths should be empty", psm.getPaths(N1, N4, WEIGHT).isEmpty());
    }

    @Test
    public void sameDevice() {
        ths.hosts.put(IP1, N1);
        ths.hosts.put(IP2, N2);

        Set<Path> paths = psm.getPaths(N1, N2);
        assertEquals("incorrect path count", 1, paths.size());
        assertEquals("incorrect path length", 2, paths.iterator().next().links().size());
        assertNull("no weight should have been used", tts.weight);

        paths = psm.getPaths(N1, N2, WEIGHT);
        assertEquals("incorrect path count", 1, paths.size());
        assertEquals("incorrect path length", 2, paths.iterator().next().links().size());
        assertNull("no weight should have been used", tts.weight);
    }

    @Test
    public void diffDevice() {
        ths.hosts.put(IP1, N1);
        ths.hosts.put(IP3, N3);
        DefaultLink link = new DefaultLink(new DefaultConnectionPoint(DID1, IFID3),
                                           new DefaultConnectionPoint(DID3, IFID3));
        List<Link> links = new ArrayList<>();
        links.add(link);

        tts.paths.add(new DefaultPath(links));

        Set<Path> paths = psm.getPaths(N1, N3);
        assertEquals("incorrect path count", 1, paths.size());
        assertEquals("incorrect path length", 3, paths.iterator().next().links().size());
        assertNull("no weight should have been used", tts.weight);

        paths = psm.getPaths(N1, N3, WEIGHT);
        assertEquals("incorrect path count", 1, paths.size());
        assertEquals("incorrect path length", 3, paths.iterator().next().links().size());
        assertNotNull("no weight should have been used", tts.weight);
    }

    // Test fixture for host service
    private class TestHostService extends HostServiceAdapter {
        final Map<IpAddress, Host> hosts = new HashMap<>();

        @Override
        public Host getHost(HostId hostId) {
            return hosts.get(hostId.ip());
        }
    }

    // Test fixture for topology service
    private class TestTopologyService extends TopologyServiceAdapter {

        final Set<Path> paths = new HashSet<>();
        private LinkWeight weight;

        @Override
        public Set<Path> getPaths(DeviceId src, DeviceId dst) {
            return paths;
        }

        @Override
        public Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight) {
            this.weight = weight;
            return paths;
        }

        @Override
        public boolean isBroadcastAllowed(ConnectionPoint point) {
            return true;
        }
    }
}
