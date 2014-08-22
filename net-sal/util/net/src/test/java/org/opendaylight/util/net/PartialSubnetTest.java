/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the PartialSubnet class.
 *
 * @author Simon Hunt
 */
public class PartialSubnetTest {

    private static final int B = 256;

    private PartialSubnet partial;
    private PartialSubnet other;

    private Subnet subnet;
    private IpAddress address;
    private SubnetMask mask;

    private IpRange subnetRange;
    private IpRange range;

    // == TESTS GO HERE ==

    @Test
    public void basic() {
        print(EOL + "basic()");
        partial = PartialSubnet.valueOf("192.168.0.0/24,192.168.0.5-9");
        print(partial.toDebugString());
        subnet = partial.getSubnet();
        address = subnet.getAddress();
        mask = subnet.getMask();
        subnetRange = subnet.getEquivalentIpRange();
        range = partial.getRange();

        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.0.0"), address);
        assertEquals(AM_NEQ, SubnetMask.MASK_255_255_255_0, mask);
        assertEquals(AM_NEQ, 24, mask.getOneBitCount());
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.0.0"), subnetRange.first());
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.0.255"), subnetRange.last());
        assertEquals(AM_NEQ, 256, subnetRange.sizeAsInt());
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.0.5"), range.first());
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.0.9"), range.last());
        assertEquals(AM_NEQ, 5, range.sizeAsInt());
        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("192.168.0.7")));
        assertFalse(AM_HUH, partial.contains(IpAddress.valueOf("192.168.0.3")));
        assertFalse(AM_HUH, partial.contains(IpAddress.valueOf("192.168.0.11")));
    }

    private static final byte[] BA_ADDRESS_1 = { 15, 32, 200-B, 0};
    private static final byte[] BA_MASK_1 = { 255-B, 255-B, 248-B, 0};

    private static final IpAddress ADDRESS_1 = IpAddress.valueOf(BA_ADDRESS_1);
    private static final SubnetMask MASK_1 = SubnetMask.valueOf(BA_MASK_1);

    private static final String STR_SUBNET_1 = "15.32.200.0/21";
    private static final String STR_RANGE_1 = "15.32.201.57-60";
    private static final String STR_SUBNET_2 = "15.32.200.0/20";
    private static final String STR_RANGE_2 = "15.32.201.59-61";
    private static final String STR_PARTIAL_1_1 = STR_SUBNET_1 + "," + STR_RANGE_1;
    private static final String STR_PARTIAL_1_2 = STR_SUBNET_1 + "," + STR_RANGE_2;
    private static final String STR_PARTIAL_2_1 = STR_SUBNET_2 + "," + STR_RANGE_1;

    private static final Subnet SUBNET_1 = Subnet.valueOf(ADDRESS_1, MASK_1);
    private static final IpRange RANGE_1 = IpRange.valueOf(STR_RANGE_1);


    @Test
    public void equivalence() {
        print(EOL + "equivalence()");
        partial = PartialSubnet.valueOf(SUBNET_1, RANGE_1);
        print(partial.toDebugString());
        other = PartialSubnet.valueOf(STR_PARTIAL_1_1);
        print(other.toDebugString());
        verifyEqual(partial, other);
        // bonus points....
        assertSame(AM_NSR, partial, other);

        other = PartialSubnet.valueOf(STR_PARTIAL_1_2);
        print(EOL + other.toDebugString());
        verifyNotEqual(partial, other);

        other = PartialSubnet.valueOf(STR_PARTIAL_2_1);
        print(EOL + other.toDebugString());
        verifyNotEqual(partial, other);
    }

    @Test
    public void comparison() {
        print(EOL + "comparison()");
        // same subnet, different range
        partial = PartialSubnet.valueOf("15.1.0.0/16,15.1.2.*");
        other = PartialSubnet.valueOf("15.1.0.0/16,15.1.3.*");
        assertTrue(AM_A_NLT_B, partial.compareTo(other) < 0);
        assertTrue(AM_B_NGT_A, other.compareTo(partial) > 0);

        // different subnet, same range
        partial = PartialSubnet.valueOf("15.1.0.0/16,15.1.2.*");
        other = PartialSubnet.valueOf("15.1.0.0/17,15.1.2.*");
        assertTrue(AM_A_NLT_B, partial.compareTo(other) < 0);
        assertTrue(AM_B_NGT_A, other.compareTo(partial) > 0);
    }


    @Test(expected = NullPointerException.class)
    public void valueOfNullSubnet() {
         PartialSubnet.valueOf(null, RANGE_1);
    }

    @Test
    public void valueOfNullRange() {
        print(EOL + "valueOfNullRange()");
        // NOTE: null range is treated as a range that spans the whole subnet (degenerate case)
        partial = PartialSubnet.valueOf(SUBNET_1, (IpRange)null);
        print(partial.toDebugString());
        subnet = partial.getSubnet();
        range = partial.getRange();
        verifyEqual(subnet.getEquivalentIpRange(), range);
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullSubnetAndRange() {
         PartialSubnet.valueOf(null, (IpRange)null);
    }

    @Test
    public void valueOfNullList() {
        print(EOL + "valueOfNullList()");
        // NOTE: null list is treated as a range that spans the whole subnet (degenerate case)
        partial = PartialSubnet.valueOf(SUBNET_1, (List<IpRange>)null);
        print(partial.toDebugString());
        subnet = partial.getSubnet();
        range = partial.getRange();
        verifyEqual(subnet.getEquivalentIpRange(), range);
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullSubnetAndList() {
         PartialSubnet.valueOf(null, (List<IpRange>)null);
    }

    @Test
    public void mismatchedFamilies() {
        print(EOL + "mismatchedFamilies()");
        Subnet s4 = Subnet.valueOf("15.3.4.0/24");
        IpRange i4 = IpRange.valueOf("15.3.4.42-57");
        Subnet s6 = Subnet.valueOf("fe::/96");
        IpRange i6 = IpRange.valueOf("fe::2134:*");
        print(s4 + " ... " + i4);
        print(s6 + " ... " + i6);
        try {
            PartialSubnet.valueOf(s4,i6);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("caught -> " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains("mis-matched"));
        }
        try {
            PartialSubnet.valueOf(s6,i4);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("caught -> " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains("mis-matched"));
        }
    }

    @Test
    public void badSubrange() {
        print(EOL + "badSubrange()");
        IpRange badRange = IpRange.valueOf("15.32.200-208.*");
        print(SUBNET_1.toDebugString());
        print(badRange.toDebugString());
        try {
            partial = PartialSubnet.valueOf(SUBNET_1, badRange);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("  caught-> " + e);
            assertTrue(AM_HUH, e.getMessage().contains("falls outside the subnet"));
        }
    }

    @Test
    public void containsAddresses() {
        print(EOL + "containsAddresses()");
        partial = PartialSubnet.valueOf(STR_PARTIAL_1_1);
        print(partial.toDebugString());
        IpAddress yay = IpAddress.valueOf("15.32.201.58");
        print("YAY: " + yay);
        assertTrue(AM_HUH, partial.contains(yay));
        IpAddress nay = IpAddress.valueOf("15.32.201.1");
        print("NAY: " + nay);
        assertFalse(AM_HUH, partial.contains(nay));
    }

    //  "15.32.200.0/21,15.32.201.57-60"

    @Test
    public void containsRanges() {
        print(EOL + "containsRanges()");
        partial = PartialSubnet.valueOf(STR_PARTIAL_1_1);
        print(partial.toDebugString());
        IpRange yay = IpRange.valueOf("15.32.201.58-59");
        print("YAY: " + yay.toDebugString());
        assertTrue(AM_HUH, partial.contains(yay));
        IpRange nay = IpRange.valueOf("15.32.201.58-61");
        print("NAY: " + nay.toDebugString());
        assertFalse(AM_HUH, partial.contains(nay));
        nay = IpRange.valueOf("15.32.201.100-220");
        print("NAY: " + nay.toDebugString());
        assertFalse(AM_HUH, partial.contains(nay));
    }


    @Test(expected = NullPointerException.class)
    public void valueOfNullString() {
        PartialSubnet.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void valueOfNullSubnetSingleParam() {
        PartialSubnet.valueOf((Subnet)null);
    }

    private static final String[] BAD_SPECS = {
            "",
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
            "1,2",
            "ff::/2,*",
            "a,b,c",
            "ff::/96,-1",
            ",",
    };

    @Test
    public void badSpecs() {
        print(EOL + "badSpecs()");
        for (String s: BAD_SPECS) {
            try {
                partial = PartialSubnet.valueOf(s);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("caught -> " + e);
                assertTrue(AM_WREXMSG, e.getMessage().startsWith(PartialSubnet.E_MALFORMED));
            }
        }
    }

    private static final String[] WS_SPECS = {
            "192.1.1.0/24,192.1.1.5-10",
            "  192.1.1.0/24,192.1.1.5-10",
            " 192.1.1.0/24,192.1.1.5-10 ",
            "192.1.1.0/24,192.1.1.5-10  ",
    };

    @Test
    public void trimmingWhitespace() {
        print(EOL + "trimmingWhitespace()");
        partial = PartialSubnet.valueOf(WS_SPECS[0]);
        for (String s: WS_SPECS) {
            PartialSubnet ps = PartialSubnet.valueOf(s);
            verifyEqual(partial, ps);
        }

        // and check that whitespace *in* the string is bad
        try {
            partial = PartialSubnet.valueOf("192.1.1.0/24, 192.1.1.5-10");
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("caught-> " + e);
        }
    }

    @Test
    public void impliedRange() {
        print(EOL + "impliedRange()");
        partial = PartialSubnet.valueOf("1.2.3.240/29");
        print(partial.toDebugString());
        List<IpAddress> capture = new ArrayList<IpAddress>();
        Iterator<IpAddress> iter = partial.iterator();
        while(iter.hasNext()) {
            IpAddress ip = iter.next();
            print(" ITER> " + ip);
            capture.add(ip);
        }
        print("ITER DONE");
        assertEquals(AM_UXS, 8, capture.size());
        assertEquals(AM_HUH, IpAddress.valueOf("1.2.3.240"), capture.get(0));
        assertEquals(AM_HUH, IpAddress.valueOf("1.2.3.243"), capture.get(3));
        assertEquals(AM_HUH, IpAddress.valueOf("1.2.3.247"), capture.get(7));
    }

    @Test
    public void twoSubRanges() {
        print(EOL + "twoSubRanges()");
        partial = PartialSubnet.valueOf("10.11.12.0/24,10.11.12.30-39,10.11.12.60-69");
        print(partial.toDebugString());
        Subnet sn = Subnet.valueOf("10.11.12.0/24");
        IpRange r1 = IpRange.valueOf("10.11.12.30-39");
        IpRange r2 = IpRange.valueOf("10.11.12.60-69");
        verifyEqual(partial.getSubnet(), sn);
        verifyEqual(partial.getRange(), r1);
        verifyNotEqual(partial.getRange(), r2);
        assertEquals(AM_UXS, 2, partial.getRanges().size());
        verifyEqual(partial.getRanges().get(0), r1);
        verifyEqual(partial.getRanges().get(1), r2);

        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.30")));
        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.32")));
        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.39")));

        assertFalse(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.40")));
        assertFalse(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.52")));
        assertFalse(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.59")));

        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.60")));
        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.65")));
        assertTrue(AM_HUH, partial.contains(IpAddress.valueOf("10.11.12.69")));

        assertTrue(AM_HUH, partial.contains(IpRange.valueOf("10.11.12.34-37")));
        assertTrue(AM_HUH, partial.contains(IpRange.valueOf("10.11.12.62-65")));

        assertFalse(AM_HUH, partial.contains(IpRange.valueOf("10.11.12.34-65")));
    }

    @Test
    public void noRangesSpecified() {
        // NOTE: no ranges specified is accepted as shorthand for a single range that matches the entire subnet
        print(EOL + "noRangesSpecified()");
        subnet = Subnet.valueOf("1.2.3.4/24");
        List<IpRange> ranges = new ArrayList<IpRange>();
        partial = PartialSubnet.valueOf(subnet, ranges);
        print(partial.toDebugString());
        verifyEqual(partial.getSubnet().getEquivalentIpRange(), partial.getRange());
    }

    @Test
    public void degenerateForm() {
        print(EOL + "degenerateForm()");
        subnet = Subnet.valueOf("15.5.240.0/21");
        partial = PartialSubnet.valueOf(subnet);
        print(partial.toDebugString());
        verifyEqual(subnet.getEquivalentIpRange(), partial.getRange());
    }

    @Test
    public void compareSlightlyDifferent() {
        print(EOL + "compareSlightlyDifferent()");
        partial = PartialSubnet.valueOf("1.2.3.0/24,1.2.3.11-15,1.2.3.22-25,1.2.3.33-34");
        other   = PartialSubnet.valueOf("1.2.3.0/24,1.2.3.11-15,1.2.3.22-25");
        assertTrue(AM_A_NLT_B, other.compareTo(partial) < 0);
        assertTrue(AM_B_NGT_A, partial.compareTo(other) > 0);
    }

    private static final String SUBNET_PREFIX = "10.11.12.0/23,";
    private static final String[] BAD_RANGES = {
            "10.11.11.*",
            "10.11.12.1-5,10.11.12.37-40,10.11.13.50-60,10.11.13.60-70",
    };

    @Test
    public void badRangesTest() {
        for (String br: BAD_RANGES) {
            try {
                partial = PartialSubnet.valueOf(SUBNET_PREFIX + br);
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print("caught-> " + e);
            }
        }
    }

    @Test
    public void multiRangeIterator() {
        print(EOL + "multiRangeIterator()");
        String spec = "5.6.7.0/24,5.6.7.1-3,5.6.7.15-18,5.6.7.8-9,5.6.7.222";
        partial = PartialSubnet.valueOf(spec);
        print(partial.toDebugString());
        assertEquals(AM_UXS, 10, partial.sizeAsInt());
        assertEquals(AM_UXS, 10L, partial.sizeAsLong());
        assertEquals(AM_UXS, BigInteger.valueOf(10), partial.size());

        Iterator<IpAddress> iter = partial.iterator();

        try {
            iter.remove();
            fail(AM_NOEX);
        } catch (UnsupportedOperationException e) {
            print("caught-> " + e);
        }

        checkIter(iter, "5.6.7.1");
        checkIter(iter, "5.6.7.2");
        checkIter(iter, "5.6.7.3");
        checkIter(iter, "5.6.7.15");
        checkIter(iter, "5.6.7.16");
        checkIter(iter, "5.6.7.17");
        checkIter(iter, "5.6.7.18");
        checkIter(iter, "5.6.7.8");
        checkIter(iter, "5.6.7.9");
        checkIter(iter, "5.6.7.222");
        checkIter(iter, null);
    }

    private void checkIter(Iterator<IpAddress> iter, String ipStr) {
        checkIter("", iter, ipStr);
    }

    private void checkIter(String msg, Iterator<IpAddress> iter, String ipStr) {
        IpAddress nextIp;
        if (ipStr != null) {
            assertTrue("expected hasNext() to return true", iter.hasNext());
            nextIp = iter.next();
            print(" ITER "+msg+"> " + nextIp);
            verifyEqual(IpAddress.valueOf(ipStr), nextIp);
        } else {
            assertFalse("expected hasNext() to return false", iter.hasNext());
            nextIp = iter.next();
            print(" ITER "+msg+"> " + nextIp);
            assertNull("non-null", nextIp);
        }
    }

    @Test
    public void concurrentIterators() {
        print(EOL + "concurrentIterators()");
        String spec = "1.2.3.0/24,1.2.3.1-3,1.2.3.7-9";
        partial = PartialSubnet.valueOf(spec);
        print(partial.toDebugString());
        assertEquals(AM_UXS, 6, partial.sizeAsInt());

        Iterator<IpAddress> iterA = partial.iterator();
        checkIter("A", iterA, "1.2.3.1");
        checkIter("A", iterA, "1.2.3.2");

        Iterator<IpAddress> iterB = partial.iterator();
        checkIter("B", iterB, "1.2.3.1");
        checkIter("B", iterB, "1.2.3.2");
        checkIter("B", iterB, "1.2.3.3");
        checkIter("B", iterB, "1.2.3.7");
        checkIter("B", iterB, "1.2.3.8");

        checkIter("A", iterA, "1.2.3.3");
        checkIter("A", iterA, "1.2.3.7");

        checkIter("B", iterB, "1.2.3.9");
        checkIter("B", iterB, null);

        checkIter("A", iterA, "1.2.3.8");
        checkIter("A", iterA, "1.2.3.9");
        checkIter("A", iterA, null);
    }
}
