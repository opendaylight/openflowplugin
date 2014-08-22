/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.junit.TestTools;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the MacRange class.
 *
 * @author Simon Hunt
 */
public class MacRangeTest {

    private MacRange range;

    private static final long MAC_ADDRESS_SPACE_LONG =
            256L * 256L * 256L * 256L * 256L * 256L;
    private static final BigInteger MAC_ADDRESS_SPACE =
            BigInteger.valueOf(MAC_ADDRESS_SPACE_LONG);

    private static final String RANGE_SPEC = "ff:fe:01:00:ab:80-ff";
    private static final MacAddress FIRST_MAC = mac("ff:fe:01:00:ab:80");
    private static final MacAddress LAST_MAC = mac("ff:fe:01:00:ab:ff");
    private static final BigInteger MAC_COUNT = BigInteger.valueOf(128L);
    private static final long MAC_COUNT_L = 128L;
    private static final int MAC_COUNT_I = 128;

    private static final String FULL_RANGE = "*:*:*:*:*:*";
    private static final MacAddress MAC_ZEROS = mac("00:00:00:00:00:00");
    private static final MacAddress MAC_STARS = mac("ff:ff:ff:ff:ff:ff");

    // convenience
    private static MacAddress mac(String s) {
        return MacAddress.valueOf(s);
    }

    // == TESTS GO HERE ==

    @Test
    public void basic() {
        print(EOL + "basic()");
        range = MacRange.valueOf(RANGE_SPEC);
        print(range.toDebugString());
        assertEquals(AM_NEQ, FIRST_MAC, range.first());
        assertEquals(AM_NEQ, LAST_MAC, range.last());
        assertEquals(AM_NEQ, MAC_COUNT, range.size());
        assertEquals(AM_NEQ, MAC_COUNT_L, range.sizeAsLong());
        assertEquals(AM_NEQ, MAC_COUNT_I, range.sizeAsInt());
    }

    @Test
    public void limits() {
        print(EOL + "limits()");
        range = MacRange.valueOf(FULL_RANGE);
        print(range.toDebugString());
        assertEquals(AM_NEQ, MAC_ZEROS, range.first());
        assertEquals(AM_NEQ, MAC_STARS, range.last());
        assertEquals(AM_NEQ, MAC_ADDRESS_SPACE, range.size());
        assertEquals(AM_NEQ, MAC_ADDRESS_SPACE_LONG, range.sizeAsLong());
        assertEquals(AM_NEQ, -1, range.sizeAsInt());
    }

    @Test
    public void largeNumberOfAddresses() {
        print(EOL + "largeNumberOfAddresses()");
        String spec = "ff:00-03:f0-ff:*:*:*";
        int expected = 4 * 16 * 256 * 256 * 256;
        range = MacRange.valueOf(spec);
        print(range.toDebugString());
        assertEquals(AM_NEQ, expected, range.sizeAsInt());
    }

    @Test
    public void iterator() {
        print(EOL + "iterator()");
        String spec = "ff:fe:00:00:24-26:7d-7f";
        range = MacRange.valueOf(spec);
        Iterator<MacAddress> it = range.iterator();
        List<MacAddress> capture = new ArrayList<MacAddress>(range.sizeAsInt());
        while (it.hasNext()) {
            MacAddress m = it.next();
            print("  " + m);
            capture.add(m);
        }
        assertEquals(AM_UXS, range.sizeAsInt(), capture.size());
        assertEquals(WMAC, mac("ff:fe:00:00:24:7d"), capture.get(0));
        assertEquals(WMAC, mac("ff:fe:00:00:24:7e"), capture.get(1));
        assertEquals(WMAC, mac("ff:fe:00:00:24:7f"), capture.get(2));

        assertEquals(WMAC, mac("ff:fe:00:00:25:7d"), capture.get(3));
        assertEquals(WMAC, mac("ff:fe:00:00:25:7e"), capture.get(4));
        assertEquals(WMAC, mac("ff:fe:00:00:25:7f"), capture.get(5));

        assertEquals(WMAC, mac("ff:fe:00:00:26:7d"), capture.get(6));
        assertEquals(WMAC, mac("ff:fe:00:00:26:7e"), capture.get(7));
        assertEquals(WMAC, mac("ff:fe:00:00:26:7f"), capture.get(8));

    }

    private static final String WMAC = "Wrong MAC address";

    @Test
    public void random() {
        print(EOL + "random()");
        range = MacRange.valueOf("00:00:00:bb-bc:*:*");
        for (int i=0; i<10; i++)
            print(range.random());
    }


    @Test
    public void commaSeparatedRanges() {
        print(EOL + "commaSeparatedRanges()");
        final String spec = "ff:00:00:01:02:b0-b3," +
                            "ee:00:00:05:06:c0-c5," +
                            "dd:00:00:09:0a:d0-d7";
        List<MacRange> ranges = MacRange.createRanges(spec);
        print(ranges);
        assertEquals(AM_UXS, 3, ranges.size());
        MacRange rff = ranges.remove(0);
        MacRange ree = ranges.remove(0);
        MacRange rdd = ranges.remove(0);

        checkRange(rff, "ff:00:00:01:02:b0", "ff:00:00:01:02:b3", 4);
        checkRange(ree, "ee:00:00:05:06:c0", "ee:00:00:05:06:c5", 6);
        checkRange(rdd, "dd:00:00:09:0a:d0", "dd:00:00:09:0a:d7", 8);

        // and back again
        List<MacRange> copy = new ArrayList<MacRange>();
        copy.add(rff);
        copy.add(ree);
        copy.add(rdd);
        String stringCopy = MacRange.rangeListToString(copy);
        print(EOL + "Reconstituted string: " + stringCopy);
        // note hex is UPPER case...
        assertEquals(AM_NEQ, spec.toUpperCase(Locale.getDefault()), stringCopy);
    }

    private void checkRange(MacRange range, String firstMac, String lastMac,
                            int macCount) {
        print(EOL + range.toDebugString());
        assertEquals(AM_NEQ, mac(firstMac), range.first());
        assertEquals(AM_NEQ, mac(lastMac), range.last());
        assertEquals(AM_UXS, macCount, range.sizeAsInt());
        Iterator<MacAddress> it = range.iterator();
        while (it.hasNext())
            print("  " + it.next());
    }

    @Test
    public void equalsHashCode() {
        print(EOL + "equalsHashCode()");
        range = MacRange.valueOf("ff:ee:dd:cc:bb:00-ff");
        MacRange range2 = MacRange.valueOf("ff:ee:dd:cc:bb:*");
        MacRange rangeX = MacRange.valueOf("ff:ee:dd:cb:bb:*");

        TestTools.verifyEqual(range, range2);
        TestTools.verifyNotEqual(range, rangeX);
        TestTools.verifyNotEqual(range2, rangeX);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        String specA = "ff:fe:00:00:01-03:*";
        String specB = "ff:fe:00:00:02-03:*";
        String specC = "ff:fe:00:00:02-04:*";

        MacRange ra = MacRange.valueOf(specA);
        MacRange rb = MacRange.valueOf(specB);
        MacRange rc = MacRange.valueOf(specC);

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

    private static final String R1 = "ff:fe:00:00:ab:*";
    private static final String R2 = "FF:FE:00:00:AB:00-FF";

    private static final String IN_R1 = "FF:FE:00:00:AB:42";
    private static final String OUT_R1 = "FF:FE:00:00:AA:42";

    @Test
    public void cacheable() {
        print(EOL + "cacheable()");
        MacRange one = MacRange.valueOf(R1);
        MacRange two = MacRange.valueOf(R2);

        print(one.toDebugString());
        print(two.toDebugString());
        assertEquals(AM_NEQ, one, two);
        assertSame(AM_NSR, one, two);
    }

    @Test
    public void contains() {
        print(EOL + "contains()");
        range = MacRange.valueOf(R1);
        assertTrue("MAC should be within range", range.contains(mac(IN_R1)));
        assertFalse("MAC should not be within range", range.contains(mac(OUT_R1)));
    }

    @Test
    public void emptyRangeLists() {
        print(EOL + "emptyRangeLists()");
        String ranges = MacRange.rangeListToString(null);
        assertEquals("Result not empty string", "", ranges);
        List<MacRange> rangeList = new ArrayList<MacRange>();
        ranges = MacRange.rangeListToString(rangeList);
        assertEquals("Result not empty string", "", ranges);
    }

    @Test
    public void fromPrefix() {
        print(EOL + "fromPrefix()");
        MacPrefix p = MacPrefix.valueOf("11:22:33:44:55");
        assertEquals(AM_UXS, 5, p.size());
        range = MacRange.valueOf(p);
        assertEquals(AM_NEQ, mac("11:22:33:44:55:00"), range.first());
        assertEquals(AM_NEQ, mac("11:22:33:44:55:FF"), range.last());
        assertEquals(AM_NEQ, 256, range.sizeAsInt());
    }

}
