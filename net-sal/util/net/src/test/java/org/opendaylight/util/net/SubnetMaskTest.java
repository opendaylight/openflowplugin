/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.IpAddress.Family;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the SubnetMask class.
 *
 * @author Simon Hunt
 */
public class SubnetMaskTest {

    private static final int B = 256;
    private static final byte[] FIRST_IPv4 = { 0xff-B, 0, 0, 0 };
    private static final byte[] FIRST_IPv6 = { 0xff-B, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    /** Private iterator to zip through the valid subnet mask values for IPv4 */
    private static class SubnetIterator implements Iterator<SubnetMask> {

        byte[] bytes;
        int idx;
        int bit;

        SubnetIterator(Family f) {
            bytes = f == Family.IPv4 ? FIRST_IPv4.clone() : FIRST_IPv6.clone();
            idx = 1;
            bit = 0;
        }

        @Override
        public boolean hasNext() {
            return idx < bytes.length;
        }

        @Override
        public SubnetMask next() {
            byte[] array = bytes.clone(); // copy before we modify
            bytes[idx] = SubnetMask.BYTE_ME[bit++];
            if (bit == 8) {
                bit = 0;
                idx++;
            }
            return SubnetMask.valueOf(array);
        }

        @Override
        public void remove() { throw new UnsupportedOperationException(); }
    }


    // === Tests

    private SubnetMask other;
    private SubnetMask mask;

    @Test
    public void basic() {
        print(EOL + "basic()");
        print("IPv4");
        SubnetIterator it = new SubnetIterator(Family.IPv4);
        while (it.hasNext())
            print(it.next());
        print("IPv6");
        it = new SubnetIterator(Family.IPv6);
        while (it.hasNext())
            print(it.next());
    }

    @Test
    public void commonValues() {
        print (EOL + "commonValues()");

        other = SubnetMask.valueOf("255.255.0.0");
        print(other);
        assertSame(AM_NSR, SubnetMask.MASK_255_255_0_0, other);

        other = SubnetMask.valueOf("255.255.248.0");
        print(other);
        assertSame(AM_NSR, SubnetMask.MASK_255_255_248_0, other);

        other = SubnetMask.valueOf("255.255.255.0");
        print(other);
        assertSame(AM_NSR, SubnetMask.MASK_255_255_255_0, other);
    }

    private static final String[] BAD_MASKS = {
            "",
            "1.2.3.4",
            "1.255.255.0",
            "255.255.127.0",
            "255.255.255.24",
            "255.255.255.25",
            "255.255.255.255",
            "255.128.0.3",
    };

    @Test
    public void badMasks() {
        print(EOL + "badMasks()");
        for (String s: BAD_MASKS) {
            print("  trying: " + s);
            try {
                other = SubnetMask.valueOf(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("    >> " + e);
            } catch (Exception e) {
                fail(AM_WREX);
            }
        }
    }

    @Test
    public void equality() {
        other = SubnetMask.valueOf("255.255.0.0");
        mask = SubnetMask.valueOf(new byte[] {-1, -1, 0, 0});
        verifyEqual(other, mask);
        // bonus points...
        assertSame(AM_NSR, other, mask);

        mask = SubnetMask.valueOf("255.248.0.0");
        verifyNotEqual(other, mask);
    }


    @Test
    public void toByteArrayAndBack() {
        print(EOL + "toByteArrayAndBack()");
        for (Family family: Family.values()) {
            print(" Family: " + family + "...");
            SubnetIterator it = new SubnetIterator(family);
            while(it.hasNext()) {
                other = it.next();
                print(other.toDebugString() + " ...");
                byte[] bytes = other.toByteArray();
                print("  " + Arrays.toString(bytes));
                SubnetMask m = SubnetMask.valueOf(bytes);
                assertSame(AM_NEQ, other, m);
            }
        }
    }

    @Test
    public void comparison() {
        mask = SubnetMask.valueOf("255.248.0.0");
        other = SubnetMask.valueOf("255.255.0.0");
        assertTrue(AM_A_NLT_B, mask.compareTo(other) < 0);
        assertTrue(AM_B_NGT_A, other.compareTo(mask) > 0);
    }

    @Test
    public void fromOneBits() {
        print(EOL + "fromOneBits()");
        print("IPv4");
        for (int n=SubnetMask.MIN_ONE_BITS_ALLOWED; n<=SubnetMask.MAX_ONE_BITS_IN_IPv4; n++) {
            other = SubnetMask.valueOf(n, Family.IPv4);
            print("  [" + n + " bits] -> " + other);
            assertEquals(AM_NEQ, n, other.getOneBitCount());
        }
        print("IPv6");
        for (int n=SubnetMask.MIN_ONE_BITS_ALLOWED; n<=SubnetMask.MAX_ONE_BITS_IN_IPv6; n++) {
            other = SubnetMask.valueOf(n, Family.IPv6);
            print("  [" + n + " bits] -> " + other);
            assertEquals(AM_NEQ, n, other.getOneBitCount());
        }
    }

    @Test
    public void fromCidr() {
        print(EOL + "fromCidr()");
        assertEquals("wrong mask", SubnetMask.MASK_255_255_0_0, SubnetMask.fromCidr("129.4.0.0/16"));
        assertEquals("wrong mask", SubnetMask.MASK_255_255_255_0, SubnetMask.fromCidr("192.168.0.0/24"));
        assertEquals("wrong mask", SubnetMask.MASK_255_255_248_0, SubnetMask.fromCidr("15.37.0.0/21"));
    }

    @Test
    public void networkAndHost() {
        print(EOL + "networkAndHost()");
        IpAddress ip = IpAddress.valueOf("15.31.27.230");
        assertEquals(AM_NSR, IpAddress.valueOf("15.31.0.0"), SubnetMask.MASK_255_255_0_0.networkPortion(ip));
        assertEquals(AM_NSR, IpAddress.valueOf("15.31.24.0"), SubnetMask.MASK_255_255_248_0.networkPortion(ip));
        assertEquals(AM_NSR, IpAddress.valueOf("15.31.27.0"), SubnetMask.MASK_255_255_255_0.networkPortion(ip));

        assertEquals(AM_NSR, IpAddress.valueOf("0.0.27.230"), SubnetMask.MASK_255_255_0_0.hostPortion(ip));
        assertEquals(AM_NSR, IpAddress.valueOf("0.0.3.230"), SubnetMask.MASK_255_255_248_0.hostPortion(ip));
        assertEquals(AM_NSR, IpAddress.valueOf("0.0.0.230"), SubnetMask.MASK_255_255_255_0.hostPortion(ip));
    }

    @Test
    public void badPortions() {
        print(EOL + "badPortions()");
        mask = SubnetMask.valueOf("255.255.248.0");
        other = SubnetMask.valueOf("ffff:ffff:fe00::");
        IpAddress ip4 = IpAddress.valueOf("129.12.30.13");
        IpAddress ip6 = IpAddress.valueOf("fe:24::1234");

        // family mismatches...
        try {
            mask.networkPortion(ip6);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        try {
            mask.hostPortion(ip6);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        try {
            other.networkPortion(ip4);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        try {
            other.hostPortion(ip4);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        // and nulls...
        try {
            mask.networkPortion(null);
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            print(" caught -> " + e);
        }

        try {
            mask.hostPortion(null);
            fail(AM_NOEX);
        } catch (NullPointerException e) {
            print(" caught -> " + e);
        }
    }

    @Test
    public void fromBitCountBadValues() {
        print(EOL + "fromBitCountBadValues()");
        try {
            SubnetMask.valueOf(7, Family.IPv4); // too few bits
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        try {
            SubnetMask.valueOf(7, Family.IPv6); // too few bits
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        try {
            SubnetMask.valueOf(32, Family.IPv4); // too many bits
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }

        try {
            SubnetMask.valueOf(128, Family.IPv6); // too many bits
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(" caught -> " + e);
        }
    }

    @Test
    public void toIpAndBack() {
        print(EOL + "toIpAndBack()");
        String spec = "255.255.248.0";
        mask = SubnetMask.valueOf(spec);
        IpAddress ip = IpAddress.valueOf(spec);
        IpAddress fromMask = mask.toIpAddress();
        assertEquals(AM_NSR, ip, fromMask);
    }
}
