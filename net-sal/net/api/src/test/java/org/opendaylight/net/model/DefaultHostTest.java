/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.VlanId;

import java.util.List;

import static junit.framework.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.BigPortNumber.bpn;

/**
 * Unit testing for {@link DefaultHost}.
 *
 * @author Shaun Wackerly
 * @author Simon Hunt
 */
public class DefaultHostTest extends AbstractModelTest {

    private static final IpAddress IP = IpAddress.BROADCAST_IPv4;
    private static final SegmentId SEG_ID = SegmentId.valueOf(VlanId.valueOf(0));
    private static final HostId HOST_ID = HostId.valueOf(IP, SEG_ID);
    private static final String NAME = IP.toString();
    private static final SupplierId SUPP_ID = EasyMock.createMock(SupplierId.class);
    private static final Interface INTF = EasyMock.createMock(Interface.class);
    private static final MacAddress MAC = MacAddress.BROADCAST;
    private static final DeviceId DEV_ID = DeviceId.valueOf("fingerprint");
    private static final InterfaceId DEV_INTF = InterfaceId.valueOf(bpn(2));
    private static final HostLocation LOC = new DefaultHostLocation(DEV_ID, DEV_INTF);

    private DefaultHost dh;


    @Before
    public void setUp() {
        dh = new DefaultHost(HOST_ID);
    }

    @Test
    public void equals() {
        title("equals");
        Host h = new DefaultHost(HOST_ID);
        assertEquals(AM_NEQ, dh, h);
        assertEquals(AM_NEQ, dh.hashCode(), h.hashCode());
    }

    private void verifyIdIpSegIdNameAndType() {
        assertEquals(AM_NEQ, HOST_ID, dh.id());
        assertEquals(AM_NEQ, HOST_ID.ip(), dh.ip());
        assertEquals(AM_NEQ, HOST_ID.segmentId(), dh.segmentId());
        assertEquals(AM_NEQ, NAME, dh.name());
        assertEquals(AM_NEQ, DefaultHost.DEFAULT_TYPE, dh.type());
    }

    private void verifyIntfMacAndOptionalLoc(HostLocation expLoc) {
        assertEquals(AM_NEQ, INTF, dh.netInterface());
        assertEquals(AM_NEQ, MAC, dh.mac());
        assertEquals(AM_NEQ, expLoc, dh.location());
        if (expLoc == null) {
            assertEquals(AM_UXS, 0, dh.recentLocations().size());
        } else {
            assertEquals(AM_UXS, 1, dh.recentLocations().size());
            assertEquals(AM_NEQ, expLoc, dh.recentLocations().get(0));
        }

    }

    @Test
    public void basicConstructor() {
        title("basicConstructor");
        verifyIdIpSegIdNameAndType();
        assertNull(AM_HUH, dh.supplierId());
        assertNull(AM_HUH, dh.netInterface());
        assertNull(AM_HUH, dh.mac());
        assertNull(AM_HUH, dh.location());
        assertEquals(AM_UXS, 0, dh.recentLocations().size());

        dh.setNetInterface(INTF);
        dh.setMac(MAC);
        dh.setLocation(LOC);
        verifyIdIpSegIdNameAndType();
        verifyIntfMacAndOptionalLoc(LOC);
    }

    @Test
    public void fullConstructorWithLocation() {
        title("fullConstructorWithLocation");
        dh = new DefaultHost(SUPP_ID, HOST_ID, INTF, MAC, LOC);
        assertEquals(AM_NEQ, SUPP_ID, dh.supplierId());
        verifyIdIpSegIdNameAndType();
        verifyIntfMacAndOptionalLoc(LOC);
    }

    @Test
    public void fullConstructorWithUnspecifiedLocation() {
        title("fullConstructorWithUnspecifiedLocation");
        dh = new DefaultHost(SUPP_ID, HOST_ID, INTF, MAC, null);
        assertEquals(AM_NEQ, SUPP_ID, dh.supplierId());
        verifyIdIpSegIdNameAndType();
        verifyIntfMacAndOptionalLoc(null);
    }

    @Test (expected = NullPointerException.class)
    public void nullIdBasic() {
        new DefaultHost(null);
    }

    @Test (expected = NullPointerException.class)
    public void nullIdFull() {
        new DefaultHost(null, null, INTF, MAC, LOC);
    }

    private void verifyLocations(HostLocation... expLocs) {
        print(dh.recentLocations());
        // head of locations should be "the" location
        assertEquals(AM_NEQ, expLocs[0], dh.location());
        // validate all recent locations
        List<HostLocation> actlocs = dh.recentLocations();
        assertEquals(AM_UXS, expLocs.length, actlocs.size());
        for (int i=0; i<expLocs.length; i++)
            assertSame(AM_NSR, expLocs[i], actlocs.get(i));
    }

    @Test
    public void location() {
        title("location");
        DeviceId fooDev = DeviceId.valueOf("foo");
        DeviceId barDev = DeviceId.valueOf("bar");
        InterfaceId int1 = InterfaceId.valueOf(bpn(10));
        InterfaceId int2 = InterfaceId.valueOf(bpn(20));
        HostLocation loc1 = new DefaultHostLocation(fooDev, int1);
        HostLocation loc2 = new DefaultHostLocation(fooDev, int2);
        HostLocation loc2b = new DefaultHostLocation(fooDev, int2);
        HostLocation loc3 = new DefaultHostLocation(barDev, int2);

        // No location specified
        assertNull(AM_HUH, dh.location());
        assertEquals(AM_UXS, 0, dh.recentLocations().size());

        // First location
        dh.setLocation(loc1);
        verifyLocations(loc1);

        // Second location
        dh.setLocation(loc2);
        verifyLocations(loc2, loc1);

        // Duplicate second location
        dh.setLocation(loc2b);
        verifyLocations(loc2b, loc1);

        // Third location
        dh.setLocation(loc3);
        verifyLocations(loc3, loc2b, loc1);

        // First location again
        dh.setLocation(loc1);
        verifyLocations(loc1, loc3, loc2b, loc1);
    }

    private int expSizeOfRecentLocs(int idx) {
        return Math.min(idx, DefaultHost.MAX_LOC);
    }

    @Test
    public void maxLocations() {
        title("maxLocations");
        DeviceId fooDev = DeviceId.valueOf("foo");
        HostLocation locations[] = new HostLocation[DefaultHost.MAX_LOC*2];

        // Iterate over different locations, adding each and checking results
        for (int idx = 0; idx < locations.length; idx++) {
            HostLocation loc =
                    new DefaultHostLocation(fooDev, InterfaceId.valueOf(bpn(idx)));
            locations[idx] = loc;

            int expRecLocSize = expSizeOfRecentLocs(idx);
            assertEquals(AM_UXS, expRecLocSize, dh.recentLocations().size());

            dh.setLocation(loc);
            print(dh.recentLocations());

            expRecLocSize = expSizeOfRecentLocs(idx + 1);
            assertEquals(AM_UXS, expRecLocSize, dh.recentLocations().size());
            assertSame(AM_NSR, loc, dh.location());

            // Iterate over past locations, up to max
            for (int k = 0; k < expRecLocSize; k++)
                assertSame(AM_NSR, locations[idx - k], dh.recentLocations().get(k));
        }
    }
        
}
