/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the Subnet class.
 *
 * @author Simon Hunt
 */
public class SubnetTest {

    private static final int B = 256;

    private Subnet subnet;
    private Subnet other;
    private IpAddress address;
    private SubnetMask mask;
    private IpRange range;
    private String cidr;

    // == TESTS GO HERE ==

    @Test
    public void basic() {
        print(EOL + "basic()");
        subnet = Subnet.valueOf("192.168.0.0/24");
        print(subnet.toDebugString());
        address = subnet.getAddress();
        mask = subnet.getMask();
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.0.0"), address);
        assertEquals(AM_NEQ, SubnetMask.MASK_255_255_255_0, mask);
        assertEquals(AM_NEQ, 24, mask.getOneBitCount());
    }

    private static final byte[] BA_ADDRESS_1 = { 15, 32, 200-B, 0};
    private static final byte[] BA_MASK_1 = { 255-B, 255-B, 248-B, 0};
    private static final byte[] BA_ADDRESS_2 = { 15, 23, 200-B, 0};
    private static final byte[] BA_MASK_2 = { 255-B, 255-B, 240-B, 0};

    private static final IpAddress ADDRESS_1 = IpAddress.valueOf(BA_ADDRESS_1);
    private static final SubnetMask MASK_1 = SubnetMask.valueOf(BA_MASK_1);
    private static final IpAddress ADDRESS_2 = IpAddress.valueOf(BA_ADDRESS_2);
    private static final SubnetMask MASK_2 = SubnetMask.valueOf(BA_MASK_2);

    private static final String STR_SUBNET_1 = "15.32.200.0/21";
    private static final IpAddress SUBNET_1_FIRST = IpAddress.valueOf("15.32.200.0");
    private static final IpAddress SUBNET_1_LAST = IpAddress.valueOf("15.32.207.255");

    private static final String STR_SUBNET_2 = "15.23.200.0/20";

    @Test
    public void equivalence() {
        print(EOL + "equivalence()");
        subnet = Subnet.valueOf(ADDRESS_1, MASK_1);
        print(subnet.toDebugString());
        other = Subnet.valueOf(STR_SUBNET_1);
        print(other.toDebugString());
        verifyEqual(subnet, other);
        // bonus points....
        assertSame(AM_NSR, subnet, other);

        subnet = Subnet.valueOf(ADDRESS_2, MASK_2);
        print(subnet.toDebugString());
        other = Subnet.valueOf(STR_SUBNET_2);
        print(other.toDebugString());
        verifyEqual(subnet, other);
        // bonus points....
        assertSame(AM_NSR, subnet, other);

        other = Subnet.valueOf(STR_SUBNET_1);
        verifyNotEqual(subnet, other);
    }

    @Test
    public void comparison() {
        print(EOL + "comparison()");
        // differing addresses, same subnet mask
        subnet = Subnet.valueOf("15.23.200.0/20");
        other = Subnet.valueOf("15.33.200.0/20");
        assertTrue(AM_A_NLT_B, subnet.compareTo(other) < 0);
        assertTrue(AM_B_NGT_A, other.compareTo(subnet) > 0);

        // same addresses, differing subnet mask
        subnet = Subnet.valueOf("15.23.200.0/20");
        other = Subnet.valueOf("15.23.200.0/21");
        assertTrue(AM_A_NLT_B, subnet.compareTo(other) < 0);
        assertTrue(AM_B_NGT_A, other.compareTo(subnet) > 0);
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullAddress() {
         Subnet.valueOf(null, SubnetMask.MASK_255_255_255_0);
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullMask() {
         Subnet.valueOf(IpAddress.LOOPBACK_IPv4, null);
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullAddressAndMask() {
         Subnet.valueOf(null, null);
    }

    @Test
    public void valueOfMismatchedFamilies() {
        print(EOL + "valueOfMismatchedFamilies()");
        IpAddress ip4 = IpAddress.valueOf("15.23.45.0");
        IpAddress ip6 = IpAddress.valueOf("0:0:ff::1234");
        SubnetMask sm4 = SubnetMask.MASK_255_255_255_0;
        SubnetMask sm6 = SubnetMask.fromCidr("::/96");
        print(ip4);
        print(ip6);
        print(sm4);
        print(sm6);

        try {
            Subnet.valueOf(ip4, sm6);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains("family mismatch"));
        }

        try {
            Subnet.valueOf(ip6, sm4);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains("family mismatch"));
        }
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullCidr() {
        Subnet.valueOf(null);
    }

    private static final String[] BAD_CIDRS = {
            "1234/2",
            "1.2.3.4/9/12",
            "15.23.0.0/xyyzy",
            "abcd/12",
            "15.14.13.12/-2",
            "15.14.13.12/7",
            "15.14.13.12/32",
            "15.14.13.12/300",
            "fe::/129",
            "::/1",
    };

    @Test
    public void badCidrs() {
        print(EOL + "badCidrs()");
        for (String s: BAD_CIDRS) {
            try {
                subnet = Subnet.valueOf(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("caught -> " + e);
                assertTrue(AM_WREXMSG, e.getMessage().startsWith(Subnet.E_MALFORMED));
            }
        }
    }

    @Test
    public void equivRange() {
        print(EOL + "equivRange()");
        subnet = Subnet.valueOf(STR_SUBNET_1);
        print(subnet.toDebugString());
        range = subnet.getEquivalentIpRange();
        assertEquals(AM_NEQ, SUBNET_1_FIRST, range.first());
        assertEquals(AM_NEQ, SUBNET_1_LAST, range.last());

        // even if the 'seed' address has a non-zero host portion, this should be fixed when the subnet
        // is created
        cidr = "ff::1234:abcd/112";
        subnet = Subnet.valueOf(cidr);
        print("ORIGINAL: " + cidr);
        print("ACTUAL  : " + subnet.toDebugString());
        assertEquals(AM_NEQ, subnet.getAddress(), subnet.getEquivalentIpRange().first());
        assertEquals(AM_NEQ, subnet.getAddress(), IpAddress.valueOf("ff::1234:0000"));
    }

    @Test
    public void subnetContainsAddresses() {
        print(EOL + "subnetContainsAddresses()");
        subnet = Subnet.valueOf(STR_SUBNET_1);
        print(subnet.toDebugString());
        assertTrue(AM_HUH, subnet.contains(SUBNET_1_FIRST));
        assertTrue(AM_HUH, subnet.contains(SUBNET_1_LAST));
        assertTrue(AM_HUH, subnet.contains(IpAddress.valueOf("15.32.203.1")));
        assertFalse(AM_HUH, subnet.contains(IpAddress.valueOf("15.32.234.1")));
    }

    @Test
    public void subnetContainsRanges() {
        print(EOL + "subnetContainsRanges()");
        subnet = Subnet.valueOf(STR_SUBNET_1);    //  "15.32.200.0/21"
        print(subnet.toDebugString());
        range = subnet.getEquivalentIpRange();

        assertTrue(AM_HUH, subnet.contains(range)); // contains its own equivalent range
        IpRange yay = IpRange.valueOf("15.32.201.*");
        print("YAY: " + yay.toDebugString());
        assertTrue(AM_HUH, subnet.contains(yay));
        IpRange nay =IpRange.valueOf("15.32.207-208.*");
        print("NAY: " + nay.toDebugString());
        assertFalse(AM_HUH, subnet.contains(nay));
    }

    private static final String[] IPv4_SUBNETS = {
            "16.12.12.0/21",
            "16.12.14.55/21",
            "16.12.15.2/21",
            "16.12.13.255/21",
    };

    private static final String[] IPv6_SUBNETS = {
            "bad:d00d::1234:5678/104",
            "bad:d00d::12ff:0000/104",
            "bad:d00d::1259:f1d0/104",
            "bad:d00d::1231:aaaa/104",
    };

    @Test
    public void addressCanonicalization() {
        print(EOL + "addressCanonicalization()");
        checkSubnetAddressCanon("15.37.12.123", "255.255.255.0", "15.37.12.0");
        checkSubnetAddressCanon("15.37.15.214", "255.255.254.0", "15.37.14.0");
        checkSubnetAddressCanon("fe::1:1234:5678", "ffff:ffff:ffff:ffff:ffff:ffff::", "fe::1:0:0");

        verifyEquivalence(IPv4_SUBNETS);
        verifyEquivalence(IPv6_SUBNETS);
    }

    private void verifyEquivalence(String[] cidrs) {
        IpAddress ip = Subnet.valueOf(cidrs[0]).getAddress();
        print(EOL + "  verifying... " + ip);
        for (String s: cidrs) {
            IpAddress ip2 = Subnet.valueOf(s).getAddress();
            print("  " + ip2 + " <--- " + s);
            assertSame(AM_NSR, ip, ip2);
        }

    }

    private void checkSubnetAddressCanon(String seed, String mask, String expected) {
        address = IpAddress.valueOf(seed);
        subnet = Subnet.valueOf(address, SubnetMask.valueOf(mask));
        IpAddress expectedAddress = IpAddress.valueOf(expected);
        print("Orig IP: " + address);
        print("Subnet : " + subnet.toDebugString());
        assertEquals("wrong address", expectedAddress, subnet.getAddress());
    }


    @Test
    public void broadcastAddress() {
        print(EOL + "broadcastAddress()");

        // with CIDR notation
        checkBroadcast("15.37.32.0/21", "15.37.39.255");
        checkBroadcast("17.37.32.0/24", "17.37.32.255");

        // with implied subnet mask
        checkBroadcast("12.33.33.33", "12.255.255.255");
        checkBroadcast("130.44.44.44", "130.44.255.255");
        checkBroadcast("200.1.2.0", "200.1.2.255");
    }

    private void checkBroadcast(String subnetStr, String broadcastStr) {
        print(" verifying... " + subnetStr + " -> " + broadcastStr);
        subnet = Subnet.valueOf(subnetStr);
        print(subnet.toDebugString());
        IpAddress bc = subnet.getBroadcastAddress();
        print(bc);
        assertEquals("unexpected Broadcast address", IpAddress.valueOf(broadcastStr), bc);
    }
}
