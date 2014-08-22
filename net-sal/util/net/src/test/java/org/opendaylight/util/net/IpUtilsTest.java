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
 * This JUnit test class tests the IpUtils class.
 *
 * @author Simon Hunt
 */
public class IpUtilsTest {

    private static final int[] GOOD_SEEDS = { 0, 1, 2, 15, 100, 249, 250, 255 };
    private static final int[] BAD_SEEDS = {Integer.MIN_VALUE, -77, -1,
                                            256, 257, 3000, Integer.MAX_VALUE};

    @Test
    public void repeatedIp4() {
        print(EOL + "repeatedIp4()");

        for (int seed: GOOD_SEEDS) {
            IpAddress ip = IpUtils.getRepeatedByteIpAddressV4(seed);
            print("  seed=" + seed + ", IP=" + ip);
            checkAllBytesMatch(seed, ip.toByteArray());
        }
    }

    @Test
    public void repeatedIp6() {
        print(EOL + "repeatedIp6()");

        for (int seed: GOOD_SEEDS) {
            IpAddress ip = IpUtils.getRepeatedByteIpAddressV6(seed);
            print("  seed=" + seed + ", IP=" + ip);
            checkAllBytesMatch(seed, ip.toByteArray());
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
    public void badIpSeedValues() {
        print (EOL + "badIpSeedValues()");

        for (int seed: BAD_SEEDS) {
            // check IPv4
            try {
                IpUtils.getRepeatedByteIpAddressV4(seed);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                assertTrue(AM_HUH, iae.getMessage().contains("0..255"));
            } catch (Exception e) {
                fail(AM_WREX);
            }

            // repeat for IPv6
            try {
                IpUtils.getRepeatedByteIpAddressV6(seed);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                assertTrue(AM_HUH, iae.getMessage().contains("0..255"));
            } catch (Exception e) {
                fail(AM_WREX);
            }

            print("correctly threw out " + seed);
        }
    }

    private static final String IP_SPEC = "15.29.36-37.1-255";

    @Test(expected = IllegalArgumentException.class)
    public void ipsNegCount() {
        IpUtils.getRandomIps(IP_SPEC, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ipsZeroCount() {
        IpUtils.getRandomIps(IP_SPEC, 0);
    }

    @Test(expected = NullPointerException.class)
    public void ipsNullSpec() {
        IpUtils.getRandomIps((String)null, 1);
    }

    private static final String[] BAD_IP_SPECS_V4 = {
            "",
            "0",
            "0.0",
            "0.0.0",
            "0.0.0.0.0",
            "1.2.3.-4",
    };

    @Test
    public void ipsBadSpecs() {
        print(EOL + "ipsBadSpecs()");
        for (String s: BAD_IP_SPECS_V4) {
            try {
                IpUtils.getRandomIps(s, 3);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                print(iae.getMessage());
            }
        }
    }

    private static final String[] GOOD_IP_SPECS_V4 = {
            "0.0.0.0",
            "15.29.36-37.1-255",
            "15-16.1.2.71-79",
    };


    private static final int LIST_SIZE = 200;
    private static final int PRINT_SIZE = 10;

    @Test
    public void ipsGoodSpecsV4() {
        print(EOL + "ipsGoodSpecsV4()");
        for (String s: GOOD_IP_SPECS_V4) {
            print(EOL + "  For spec [" + s + "]");
            List<IpAddress> randomIps = IpUtils.getRandomIps(s, LIST_SIZE);
            assertEquals(AM_UXS, LIST_SIZE, randomIps.size());
            int i = 0;
            for (IpAddress ip : randomIps) {
                i++;
                if (i < PRINT_SIZE) {
                    print("      " + ip);
                } else if (i == PRINT_SIZE) {
                    print("      ...");
                }
                assertEquals(AM_NEQ, IpAddress.Family.IPv4, ip.getFamily());
            }
        }
    }

    private static final String[] GOOD_IP_SPECS_V6 = {
            "FFFF:0000:0000:0000:0000:0000:0000-0003:0F11-0F22",
            "FF:FAB::100-1AA",
            "::2-ffff",
            "1234:5678:9abc:def0:1234:5678:9abc:def0-ffff",
    };

    @Test
    public void ipsGoodSpecsV6() {
        print(EOL + "ipsGoodSpecsV6()");
        for (String s: GOOD_IP_SPECS_V6) {
            print(EOL + "  For spec [" + s + "]");
            List<IpAddress> randomIps = IpUtils.getRandomIps(s, LIST_SIZE);
            assertEquals(AM_UXS, LIST_SIZE, randomIps.size());
            int i = 0;
            for (IpAddress ip : randomIps) {
                i++;
                if (i < PRINT_SIZE) {
                    print("      " + ip);
                } else if (i == PRINT_SIZE) {
                    print("      ...");
                }
                assertEquals(AM_NEQ, IpAddress.Family.IPv6, ip.getFamily());
            }
        }
    }


    private static final String[] BAD_IP_SPECS_V6 = {
            "ff::0::0",
            ":::",
    };

    @Test
    public void ipsBadSpecsV6() {
        print(EOL + "ipsBadSpecsV6()");
        for (String s: BAD_IP_SPECS_V6) {
            print("    for bad spec [" + s + "]");
            try {
                IpUtils.getRandomIps(s, 1);
                fail(AM_NOEX);
            } catch (IllegalArgumentException iae) {
                print("        " + iae.getMessage());
            }
        }
    }

    private static final String IPV6_STAR_SPEC = "BAD::7:0-3:*";

    @Test
    public void ipv6StarTest() {
        print(EOL + "ipv6StarTest()");
        print(" attempting the following spec: " + IPV6_STAR_SPEC);
        List<IpAddress> randomIps = IpUtils.getRandomIps(IPV6_STAR_SPEC, 200);
        assertEquals(AM_UXS, LIST_SIZE, randomIps.size());
        int i = 0;
        for (IpAddress ip : randomIps) {
            i++;
            if (i < PRINT_SIZE) {
                print("      " + ip);
            } else if (i == PRINT_SIZE) {
                print("      ...");
            }
            assertEquals(AM_NEQ, IpAddress.Family.IPv6, ip.getFamily());
        }
    }



}
