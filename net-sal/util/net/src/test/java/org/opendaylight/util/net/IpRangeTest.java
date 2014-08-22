/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.junit.TestTools;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the IpRange class.
 *
 * @author Simon Hunt
 */
public class IpRangeTest {

    private static final String RANGE_SPEC = "192.168.1.1-12";
    private static final IpAddress FIRST_IP = IpAddress.valueOf("192.168.1.1");
    private static final IpAddress LAST_IP = IpAddress.valueOf("192.168.1.12");
    private static final BigInteger IP_COUNT = BigInteger.valueOf(12L);
    private static final long IP_COUNT_L = 12L;
    private static final int IP_COUNT_I = 12;

    private static final String IPv4_SPEC = "*.*.*.*";
    private static final IpAddress IPv4_ZEROS = IpAddress.valueOf("0.0.0.0");
    private static final IpAddress IPv4_STARS = IpAddress.valueOf("255.255.255.255");
    private static BigInteger IPv4_ADDRESS_SPACE;

    private static final String IPv6_SPEC = "*:*:*:*:*:*:*:*";
    private static final IpAddress IPv6_ZEROS = IpAddress.valueOf("::");
    private static final IpAddress IPv6_STARS = IpAddress.valueOf("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
    private static BigInteger IPv6_ADDRESS_SPACE;

    @BeforeClass
    public static void classSetUp() {
        long byteSquared = 256 * 256;
        IPv4_ADDRESS_SPACE = BigInteger.valueOf(byteSquared).multiply(BigInteger.valueOf(byteSquared));
        BigInteger ip4Squared = IPv4_ADDRESS_SPACE.multiply(IPv4_ADDRESS_SPACE);
        IPv6_ADDRESS_SPACE = ip4Squared.multiply(ip4Squared);
    }

    // == TESTS GO HERE ==
    private IpRange range;
    private Subnet subnet;

    @Test
    public void basic() {
        print(EOL + "basic()");
        range = IpRange.valueOf(RANGE_SPEC);
        print(range.toDebugString());
        assertEquals(AM_NEQ, FIRST_IP, range.first());
        assertEquals(AM_NEQ, LAST_IP, range.last());
        assertEquals(AM_NEQ, IP_COUNT, range.size());
        assertEquals(AM_NEQ, IP_COUNT_L, range.sizeAsLong());
        assertEquals(AM_NEQ, IP_COUNT_I, range.sizeAsInt());
    }

    @Test
    public void limitsV4() {
        print(EOL + "limitsV4()");
        range = IpRange.valueOf(IPv4_SPEC);
        print(range.toDebugString());
        assertEquals(AM_NEQ, IPv4_ZEROS, range.first());
        assertEquals(AM_NEQ, IPv4_STARS, range.last());
        assertEquals(AM_NEQ, IPv4_ADDRESS_SPACE, range.size());
        assertEquals(AM_NEQ, 256L * 256L * 256L * 256L, range.sizeAsLong());
        assertEquals(AM_NEQ, -1, range.sizeAsInt());
    }

    @Test
    public void limitsV6() {
        print(EOL + "limitsV6()");
        range = IpRange.valueOf(IPv6_SPEC);

        print(range.toDebugString());
        assertEquals(AM_NEQ, IPv6_ZEROS, range.first());
        assertEquals(AM_NEQ, IPv6_STARS, range.last());
        assertEquals(AM_NEQ, IPv6_ADDRESS_SPACE, range.size());
        assertEquals(AM_NEQ, -1L, range.sizeAsLong());
        assertEquals(AM_NEQ, -1, range.sizeAsInt());
    }

    @Test
    public void largeNumberOfAddresses() {
        print(EOL + "largeNumberOfAddresses()");
        String spec = "0-126.*.*.*";
        int expected = 127 * 256 * 256 * 256;
        range = IpRange.valueOf(spec);
        print(range.toDebugString());
        assertEquals(AM_NEQ, expected, range.sizeAsInt());
    }

    @Test
    public void iterator() {
        print(EOL + "iterator()");
        range = IpRange.valueOf(RANGE_SPEC);
        Iterator<IpAddress> it = range.iterator();
        List<IpAddress> capture = new ArrayList<IpAddress>(range.sizeAsInt());
        while(it.hasNext()) {
            IpAddress addr = it.next();
            print("  " + addr);
            capture.add(addr);
        }
        assertEquals(AM_UXS, range.sizeAsInt(), capture.size());
        assertEquals(WIP, IpAddress.valueOf("192.168.1.1"), capture.get(0));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.2"), capture.get(1));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.3"), capture.get(2));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.4"), capture.get(3));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.5"), capture.get(4));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.6"), capture.get(5));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.7"), capture.get(6));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.8"), capture.get(7));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.9"), capture.get(8));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.10"), capture.get(9));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.11"), capture.get(10));
        assertEquals(WIP, IpAddress.valueOf("192.168.1.12"), capture.get(11));
    }
    private static final String WIP = "Wrong IP address";

    @Test
    public void random() {
        print(EOL + "random()");
        range = IpRange.valueOf("15.37.21.*");
        for (int i=0; i<10; i++)
            print(range.random());
    }

    @Test
    public void ipIteratorAgain() {
        print(EOL + "ipIteratorAgain()");
        IpAddress ip;
        Iterator<IpAddress> it = IpRange.valueOf("15.2.1-2.244-246").iterator();

        assertTrue("hasNext incorrect", it.hasNext());
        print(ip = it.next());
        assertEquals("wrong IP", "15.2.1.244", ip.toString());

        assertTrue("hasNext incorrect", it.hasNext());
        print(ip = it.next());
        assertEquals("wrong IP", "15.2.1.245", ip.toString());

        assertTrue("hasNext incorrect", it.hasNext());
        print(ip = it.next());
        assertEquals("wrong IP", "15.2.1.246", ip.toString());

        assertTrue("hasNext incorrect", it.hasNext());
        print(ip = it.next());
        assertEquals("wrong IP", "15.2.2.244", ip.toString());

        assertTrue("hasNext incorrect", it.hasNext());
        print(ip = it.next());
        assertEquals("wrong IP", "15.2.2.245", ip.toString());

        assertTrue("hasNext incorrect", it.hasNext());
        print(ip = it.next());
        assertEquals("wrong IP", "15.2.2.246", ip.toString());

        assertFalse("hasNext incorrect", it.hasNext());
    }

    @Test
    public void ip6Iterator() {
        print(EOL + "ip6Iterator()");
        Iterator<IpAddress> it = IpRange.valueOf("ff::*:abcd").iterator();
        IpAddress lowest = it.next();
        assertEquals("wrong low IPv6", "00FF:0000:0000:0000:0000:0000:0000:ABCD", lowest.toFullString());
        int count = 1;
        IpAddress highest = lowest;
        while (it.hasNext()) {
            count ++;
            highest = it.next();
        }
        assertEquals("wrong count of IPv6 addresses", 65536, count);
        assertEquals("wrong high IPv6", "00FF:0000:0000:0000:0000:0000:FFFF:ABCD", highest.toFullString());
    }

    @Test
    public void pingSweep() {
        print(EOL + "pingSweep()");
        Iterator<IpAddress> it = IpRange.valueOf("15.29.37.*").iterator();
        IpAddress lowest = it.next();
        print(lowest);
        assertEquals("wrong low IP", "15.29.37.0", lowest.toString());
        int count = 1;
        IpAddress highest = lowest;
        while (it.hasNext()) {
            count ++;
            highest = it.next();
            print(highest);
        }
        assertEquals("wrong count of IP addresses", 256, count);
        assertEquals("wrong high IP", "15.29.37.255", highest.toString());
    }

    @Test
    public void commaSeparatedRanges() {
        print(EOL + "commaSeparatedRanges()");
        final String spec = "15.3.3.20-24,16.4.4.30-35,17.5.5.40-46";
        List<IpRange> ranges = IpRange.createRanges(spec);
        print(ranges);
        assertEquals(AM_UXS, 3, ranges.size());
        IpRange r15 = ranges.remove(0);
        IpRange r16 = ranges.remove(0);
        IpRange r17 = ranges.remove(0);

        checkRange(r15, "15.3.3.20", "15.3.3.24", 5);
        checkRange(r16, "16.4.4.30", "16.4.4.35", 6);
        checkRange(r17, "17.5.5.40", "17.5.5.46", 7);

        // let's go back again...
        List<IpRange> copy = new ArrayList<IpRange>();
        copy.add(r15);
        copy.add(r16);
        copy.add(r17);
        String stringCopy = IpRange.rangeListToString(copy);
        print(EOL + "reconstituted string: " + stringCopy);
        assertEquals(AM_NEQ, spec, stringCopy);
    }

    private void checkRange(IpRange range, String firstIp, String lastIp, int ipCount) {
        print(EOL + range.toDebugString());
        assertEquals(AM_NEQ, IpAddress.valueOf(firstIp), range.first());
        assertEquals(AM_NEQ, IpAddress.valueOf(lastIp), range.last());
        assertEquals(AM_UXS, ipCount, range.sizeAsInt());
        Iterator<IpAddress> it = range.iterator();
        while (it.hasNext()) {
            print("  " + it.next());
        }
    }

    @Test
    public void emptyRangeLists() {
        print(EOL + "emptyRangeLists()");
        String ranges = IpRange.rangeListToString(null);
        assertEquals("Result not empty string", "", ranges);
        List<IpRange> rangeList = new ArrayList<IpRange>();
        ranges = IpRange.rangeListToString(rangeList);
        assertEquals("Result not empty string", "", ranges);
    }

    @Test
    public void equalsHashCode() {
        print(EOL + "equalsHashCode()");
        range =          IpRange.valueOf("15.15-16.1-2.0-255");
        IpRange range2 = IpRange.valueOf("15.15-16.1-2.*");
        IpRange rangeX = IpRange.valueOf("15-16.15.1-2.*");
        
        TestTools.verifyEqual(range, range2);
        TestTools.verifyNotEqual(range, rangeX);
        TestTools.verifyNotEqual(range2, rangeX);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        String specA = "ff::0000-2000";
        String specB = "ff::0000-200f";
        String specC = "ff::0001-0003";

        IpRange ra = IpRange.valueOf(specA);
        IpRange rb = IpRange.valueOf(specB);
        IpRange rc = IpRange.valueOf(specC);

        assertTrue(AM_NEQ, ra.compareTo(ra) == 0);
        assertTrue(AM_A_NLT_B, ra.compareTo(rb) < 0);
        assertTrue(AM_B_NGT_A, rb.compareTo(ra) > 0);

        assertTrue(AM_NEQ, rb.compareTo(rb) == 0);
        assertTrue(AM_A_NLT_B, rb.compareTo(rc) < 0);
        assertTrue(AM_B_NGT_A, rc.compareTo(rb) > 0);

        assertTrue(AM_NEQ, rc.compareTo(rc) == 0);
        assertTrue(AM_A_NLT_B, ra.compareTo(rc) < 0);
        assertTrue(AM_B_NGT_A, rc.compareTo(ra) > 0);
    }

    @Test
    public void fromSubnet() {
        print(EOL + "fromSubnet()");

        checkConvert("192.37.2.0/21");
        checkConvert("15.0.0.0/18");
        checkConvert("fffe::1234:5678:0:0/106");
        checkConvert("ff::bad:d00d/120");
    }

    private void checkConvert(String s) {
        print("  verifying from seed: " + s);
        subnet = Subnet.valueOf(s);
        print(subnet.toDebugString());
        range = IpRange.valueOf(subnet);
        print(range.toDebugString());
        assertEquals(AM_NEQ, subnet.getAddress(), range.first());

        // now convert back again..
        Subnet other = range.getEquivalentSubnet();
        assertNotNull("expected subnet back again", other);
        verifyEqual(subnet, other);
        print(" Got the subnet back from the range: " + subnet + EOL);
    }

    @Test
    public void fromCidr() {
        print(EOL + "fromCidr()");
        range = IpRange.valueOf("192.168.4.0/23");
        print(range.toDebugString());
        assertEquals(AM_NEQ, 512, range.sizeAsInt());
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.4.0"), range.first());
        assertEquals(AM_NEQ, IpAddress.valueOf("192.168.5.255"), range.last());
    }


    @Test
    public void malformedSpecs() {
        print(EOL + "malformedSpecs()");
        try {
            IpRange.valueOf("1.2.3.4.5-6");
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("caught --> " + e);
            assertTrue(AM_HUH, e.getMessage().contains("Malformed IPv4 spec"));
        }

        try {
            IpRange.valueOf("f:e:d:c:b-d");
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("caught --> " + e);
            assertTrue(AM_HUH, e.getMessage().contains("Malformed IPv6 spec"));
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullSpecString() {
        IpRange.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullSpecSubnet() {
        IpRange.valueOf((Subnet)null);
    }

    @Test
    public void equivalentSubnets() {
        print(EOL + "equivalentSubnets()");
        range = IpRange.valueOf("15.23.12-13.*");
        print(range.toDebugString());

        subnet = range.getEquivalentSubnet();
        assertNotNull("failed to find equivalent subnet", subnet);
        print(subnet.toDebugString());

        verifyEqual(Subnet.valueOf("15.23.12.0/23"), subnet);
        verifyEqual(IpAddress.valueOf("15.23.12.0"), subnet.getAddress());
        verifyEqual(SubnetMask.valueOf("255.255.254.0"), subnet.getMask());


        range = IpRange.valueOf("15.23.47.1-100");
        print(range.toDebugString());
        subnet = range.getEquivalentSubnet();
        assertNull("not expecting to find equivalent subnet", subnet);
    }

    @Test
    public void equivalentSubnetsSplitHostXor() {
        print(EOL + "equivalentSubnetsSplitHostXor()");
        range = IpRange.valueOf("15.1.2.3-12");
        print(range.toDebugString());
        subnet = range.getEquivalentSubnet();
        print(subnet == null ? "null" : subnet.toDebugString());
        assertNull("not expecting to find equivalent subnet", subnet);
    }
}
