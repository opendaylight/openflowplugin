/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the MacUtils class.
 *
 * @author Simon Hunt
 */
public class MacUtilsTest {

    private static final int[] GOOD_SEEDS = { 0, 1, 2, 15, 100, 249, 250, 255 };
    private static final int[] BAD_SEEDS = {Integer.MIN_VALUE, -77, -1,
                                            256, 257, 3000, Integer.MAX_VALUE};

    @Test
    public void repeatedMac() {
        print(EOL + "repeatedMac()");

        for (int seed: GOOD_SEEDS) {
            MacAddress mac = MacUtils.getRepeatedByteMacAddress(seed);
            print("  seed=" + seed + ", MAC=" + mac);
            checkAllBytesMatch(seed, mac.toByteArray());
        }
    }

    /** private helper method to check byte array
     *
      * @param expected the expected value of each byte
     * @param bytes the byte array
     */
    private void checkAllBytesMatch(int expected, byte[] bytes) {
        byte bExpected = (byte)expected;
        for (byte b: bytes) {
            assertEquals(AM_NEQ, bExpected, b);
        }
    }

    @Test
    public void badMacSeedValues() {
        print (EOL + "badMacSeedValues()");

        for (int seed: BAD_SEEDS) {
            try {
                MacUtils.getRepeatedByteMacAddress(seed);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                assertTrue(AM_HUH, iae.getMessage().contains("0..255"));
            } catch (Exception e) {
                fail(AM_WREX);
            }

            print("correctly threw out " + seed);
        }
    }

    private static final String MAC_SPEC = "ff:20:30:00-01:00-ff:00-ff";

    private static final String BAD_MAC_SPECS[] = {
            "",
            "00",
            "00:00",
            "00:00:11",
            "00:00:11:22",
            "00:00:11:22:33",
            "00:00:11:22:33:66:77",
    };

    @Test(expected = IllegalArgumentException.class)
    public void macsNegCount() {
        MacUtils.getRandomMacs(MAC_SPEC, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void macsZeroCount() {
        MacUtils.getRandomMacs(MAC_SPEC, 0);
    }

    @Test(expected = NullPointerException.class)
    public void macsNullSpec() {
        MacUtils.getRandomMacs((String)null, 1);
    }

    @Test
    public void macsBadSpecs() {
        print(EOL + "macsBadSpecs()");
        for (String s: BAD_MAC_SPECS) {
            try {
                MacUtils.getRandomMacs(s, 3);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                print(iae.getMessage());
            }
        }
    }

    @Test
    public void macsOneCount() {
        print(EOL + "macsOneCount()");
        List<MacAddress> macs = MacUtils.getRandomMacs(MAC_SPEC, 1);
        print(macs);
        assertEquals(AM_UXS, 1, macs.size());
        MacAddress m = macs.get(0);
        assertTrue(AM_HUH, m.toString().startsWith("ff:20:30:"));
    }

    @Test
    public void macsTenCount() {
        print(EOL + "macsTenCount()");
        List<MacAddress> macs = MacUtils.getRandomMacs(MAC_SPEC, 10);
        print(macs);
        assertEquals(AM_UXS, 10, macs.size());
        for (MacAddress m: macs) {
            print("  " + m);
            assertTrue(AM_HUH, m.toString().startsWith("ff:20:30:"));
        }
    }


}
