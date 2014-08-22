/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.IpAddress.Family.IPv4;
import static org.opendaylight.util.net.IpAddress.Family.IPv6;
import static org.opendaylight.util.net.IpAddress.ip;
import static org.junit.Assert.*;

/**
 * This class implements unit tests for {@link IpAddress}.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
public class IpAddressTest {

    private static final int B = 256;

    private static final String S_V6_E1 = "FEDC:0:1::AB:23";
    private static final String S_V6_E1p = "fedc:0:1::ab:23";
    private static final String S_V6_E2 = "fedc:0:1:0::ab:23";
    private static final String S_V6_E2p = "FEDC:0:1:0::AB:23";
    private static final String S_V6_E3 = "FEDC:0000:0001:0000:0000:0000:00AB:0023";
    private static final byte[] B_V6_E4 = {0xfe-B, 0xdc-B, 0, 0, 0, 0x01, 0, 0,
                                           0, 0, 0, 0, 0, 0xab-B, 0, 0x23};

    @Test
    public void basicEquivalenceIPv6() {
        IpAddress ip1 = ip(S_V6_E1);
        IpAddress ip1p= ip(S_V6_E1p);
        IpAddress ip2 = ip(S_V6_E2);
        IpAddress ip2p= ip(S_V6_E2p);
        IpAddress ip3 = ip(S_V6_E3);
        IpAddress ip4 = ip(B_V6_E4);

        assertTrue(AM_HUH, ip1.getFamily() == IPv6);

        assertEquals(AM_NSR, ip1, ip2);
        assertEquals(AM_NSR, ip1, ip3);
        assertEquals(AM_NSR, ip1, ip4);

        print(EOL + "Basic Equivalence IPv6:");
        print("  toString()      " + ip1.toString());
        print("  toShortString() " + ip1.toShortString());
        print("  toFullString()  " + ip1.toFullString());
        print("  toByteArray()   " + Arrays.toString(ip1.toByteArray()));
    }

    private static final String S_V4_E1 = "15.32.95.230";
    private static final byte[] B_V4_E2 = new byte[] { 15, 32, 95, 230-B };

    @Test
    public void basicEquivalenceIPv4() {
        IpAddress ip1 = ip(S_V4_E1);
        IpAddress ip2 = ip(B_V4_E2);

        assertTrue(AM_HUH, ip1.getFamily() == IPv4);

        assertEquals(AM_NSR, ip1, ip2);
        print(EOL + "Basic Equivalence IPv4:");
        print("  toString()      " + ip1.toString());
        print("  toShortString() " + ip1.toShortString());
        print("  toFullString()  " + ip1.toFullString());
        print("  toByteArray()   " + Arrays.toString(ip1.toByteArray()));
    }


    //==================================================
    // === test boundary conditions for valueOf(byte[])

    @Test (expected = NullPointerException.class)
    public void valueOfByteArrayNull() {
        ip((byte[])null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfByteArrayZeroLength() {
        ip(new byte[0]);
    }

    /** Private predicate to return true if len is 4 or 16.
     *
     * @param len the value to test
     * @return true if len is 4 or 16
     */
    private boolean isGoodLength(int len) {
        return len==4 || len==16;
    }

    @Test
    public void valueOfByteArrayBadLengths() {
        print(EOL + "valueOfByteArrayBadLengths()");
        for (int len=1; len<20; len++) {
            byte[] bytes = new byte[len];
            try {
                IpAddress ip = ip(bytes);
                if (isGoodLength(len)) {
                    // perfect
                    print("  Good: " + ip);
                } else {
                    fail("Accepted bad length array! " + len);
                }

            } catch (IllegalArgumentException e) {
                if (isGoodLength(len)) {
                    fail("Did not accept good length array! " + len);
                }
            } catch (Exception e) {
                fail("Unexpected Exception: " + e);
            }
        }
    }


    //==================================================
    // === test boundary conditions for valueOf(String)

    @Test (expected = NullPointerException.class)
    public void valueOfStringNull() {
        ip((String)null);
    }

    private static final String[] UNACCEPTABLE_STRINGS = {
            "",
            "1",
            "1.2.3",
            "-1.2.3.4",
            "a.b.c.d",
            "256.2.3.4",
            "1.2.3.4.5",
            "1.2.3.",

            "1:2:3:4",
            "a:a:a:a:a:a:a",
            "b:b:b:b:b:b:b:b:",
            "b:b:b:b:b:b:b:b:b",
            "ff::2::1",
            ".",
            ":",

            "[aa::bb]:",
            "[aa::bb]:-1",
            "[aa::bb]:65536",
            "[aa::bb]:xyz",

            "1.2.3.4:",
            "1.2.3.4:-1",
            "1.2.3.4:65536",
            "1.2.3.4:abc",

            "a:b:c:d:aa:bb:cc:ddddd",
            "a:b:c:d:e:f:g:h",

            "aaaaa:1::3",
            "1::3:aaaaa",

            "1.2.3.4:popeye",
            "1.2.3.4:99999",

    };

    @Test
    public void valueOfStringBad() {
        for (String s: UNACCEPTABLE_STRINGS) {
            try {
                ip(s);
                fail("Apparently, \"" + s + "\" is an acceptable format");
            } catch (IllegalArgumentException e) {
                // perfect
            } catch (Exception e) {
                fail("Unexpected exception thrown: " + e);
            }
        }
    }

    //==================================================


    @Test
    public void valueOfStringIPv4() {
        IpAddress ip1 = ip("255.255.255.255");
        IpAddress ip2 = ip("255.255.255.255:0");
        IpAddress ip3 = ip("255.255.255.255:65535");
        assertEquals(AM_NSR, ip1, ip2);
        assertEquals(AM_NSR, ip1, ip3);
    }

    private static final String[] STR_IPv6_ALL_ZEROS = {
            "::",

            "::0",
            "0::",

            "::0:0",
            "0::0",
            "0:0::",

            "::0:0:0",
            "0::0:0",
            "0:0::0",
            "0:0:0::",

            "::0:0:0:0",
            "0::0:0:0",
            "0:0::0:0",
            "0:0:0::0",
            "0:0:0:0::",

            "::0:0:0:0:0",
            "0::0:0:0:0",
            "0:0::0:0:0",
            "0:0:0::0:0",
            "0:0:0:0::0",
            "0:0:0:0:0::",

            "::0:0:0:0:0:0",
            "0::0:0:0:0:0",
            "0:0::0:0:0:0",
            "0:0:0::0:0:0",
            "0:0:0:0::0:0",
            "0:0:0:0:0::0",
            "0:0:0:0:0:0::",

            // note: no leading or trailing '::' for 7 zeros
            "0::0:0:0:0:0:0",
            "0:0::0:0:0:0:0",
            "0:0:0::0:0:0:0",
            "0:0:0:0::0:0:0",
            "0:0:0:0:0::0:0",
            "0:0:0:0:0:0::0",

            "0:0:0:0:0:0:0:0",
    };

    @Test
    public void valueOfStringIPv6AllZeros() {
        IpAddress zero = ip(new byte[16]);

        assertEquals(AM_HUH, IPv6, zero.getFamily());

        for (String s: STR_IPv6_ALL_ZEROS) {
            IpAddress ip = ip(s);
            assertEquals(AM_NSR, zero, ip);
        }
    }

    @Test
    public void valueOfStringIPv6AllZerosWithPort() {
        IpAddress zero = ip(new byte[16]);

        assertEquals(AM_HUH, IPv6, zero.getFamily());

        for (String s: STR_IPv6_ALL_ZEROS) {
            String sport = "[" + s + "]:1234";
            IpAddress ip = ip(sport);
            assertEquals(AM_NSR, zero, ip);
        }
    }

    @Test
    public void valueOfStringIPv6MorePorts() {
        IpAddress base = ip("00FF:0BAD:0000:0000:0000:0000:0000:0001");

        IpAddress ip1 = ip("ff:bad::1");
        IpAddress ip2 = ip("ff:bad:0:0:0:0:0:1");
        IpAddress ip3 = ip("00ff:0bad:0000:0000:0000:0000:0000:0001");

        IpAddress ip1a = ip("[ff:bad::1]");
        IpAddress ip2a = ip("[ff:bad:0:0:0:0:0:1]");
        IpAddress ip3a = ip("[00ff:0bad:0000:0000:0000:0000:0000:0001]");

        IpAddress ip1b = ip("[ff:bad::1]:23");
        IpAddress ip2b = ip("[ff:bad:0:0:0:0:0:1]:45");
        IpAddress ip3b = ip("[00ff:0bad:0000:0000:0000:0000:0000:0001]:67");

        assertEquals(AM_NSR, base, ip1);
        assertEquals(AM_NSR, base, ip2);
        assertEquals(AM_NSR, base, ip3);

        assertEquals(AM_NSR, base, ip1a);
        assertEquals(AM_NSR, base, ip2a);
        assertEquals(AM_NSR, base, ip3a);

        assertEquals(AM_NSR, base, ip1b);
        assertEquals(AM_NSR, base, ip2b);
        assertEquals(AM_NSR, base, ip3b);
    }

    //=======================================================
    // === test boundary conditions for valueOf(InetAddress)

    @Test (expected = NullPointerException.class)
    public void valueOfInetAddressNull() {
        ip((InetAddress)null);
    }

    @Test
    public void valueOfInetAddress() {
        print(EOL + "valueOfInetAddress(): ");
        try {
            InetAddress ia = InetAddress.getLocalHost();
            print("  INET ADDRESS: " + ia);

            String ipAsString = ia.getHostAddress();
            byte[] ipAsBytes = ia.getAddress();

            IpAddress fromIa = ip(ia);
            IpAddress fromStr = ip(ipAsString);
            IpAddress fromBytes = ip(ipAsBytes);

            assertEquals(AM_NSR, fromIa, fromStr);
            assertEquals(AM_NSR, fromIa, fromBytes);

            print("  IpAddress: " + fromIa);

        } catch (UnknownHostException e) {
            fail("Couldn't get local host? " + e);
        }
    }


    //==================================================

    @Test
    public void valueFromV4() {
        ByteBuffer bb = ByteBuffer.wrap(B_V4_E2);
        IpAddress ia = IpAddress.valueFrom(bb, false);
        assertEquals(AM_NEQ, ip(B_V4_E2), ia);
    }

    @Test
    public void valueFromV6() {
        ByteBuffer bb = ByteBuffer.wrap(B_V6_E4);
        IpAddress ia = IpAddress.valueFrom(bb, true);
        assertEquals(AM_NEQ, ip(B_V6_E4), ia);
    }

    //=======================================================
    // === test other factory methods

    @Test (expected = NullPointerException.class)
    public void valueOfPortNull() {
        IpAddress.valueOfPort(null);
    }

    @Test
    public void valueOfPortBad() {
        for (String s: UNACCEPTABLE_STRINGS) {
            try {
                int port = IpAddress.valueOfPort(s);
                fail("Accepted bad format \"" + s + "\" (port == " + port + ")");
            } catch (IllegalArgumentException e) {
                // perfect!
            } catch (Exception e) {
                fail("Unexpected Exception: " + e);
            }
        }
    }

    @Test
    public void valueOfPort() {
        assertEquals(AM_HUH, 0, IpAddress.valueOfPort("15.30.45.60:0"));
        assertEquals(AM_HUH, 3, IpAddress.valueOfPort("15.30.45.60:3"));
        assertEquals(AM_HUH, 23, IpAddress.valueOfPort("0.0.0.0:23"));
        assertEquals(AM_HUH, 65535, IpAddress.valueOfPort("255.255.255.255:65535"));

        assertEquals(AM_HUH, 97, IpAddress.valueOfPort("[::abcd:ef01]:97"));
        assertEquals(AM_HUH, 40000, IpAddress.valueOfPort("[::]:40000"));
        assertEquals(AM_HUH, 40001, IpAddress.valueOfPort("[::1]:40001"));
    }


    @Test
    public void getLoopback() {
        IpAddress loop4 = IpAddress.LOOPBACK_IPv4;
        assertEquals(AM_HUH, "127.0.0.1", loop4.toString());
        assertTrue(AM_HUH, loop4.isLoopback());
        assertEquals(AM_HUH, IPv4, loop4.getFamily());

        final byte[] b4 = new byte[] { 127, 0, 0, 1 };
        assertArrayEquals(AM_HUH, b4, loop4.toByteArray());
        ByteBuffer bb = ByteBuffer.allocate(b4.length);
        loop4.intoBuffer(bb);
        assertArrayEquals(AM_HUH, b4, bb.array());

        IpAddress loop6 = IpAddress.LOOPBACK_IPv6;
        assertEquals(AM_HUH, "::1", loop6.toShortString());
        assertTrue(AM_HUH, loop6.isLoopback());
        assertEquals(AM_HUH, IPv6, loop6.getFamily());
        final byte[] b6 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        assertArrayEquals(AM_HUH, b6, loop6.toByteArray());
        bb = ByteBuffer.allocate(b6.length);
        loop6.intoBuffer(bb);
        assertArrayEquals(AM_HUH, b6, bb.array());
    }

    @Test
    public void augmentedLoopback() {
        print(EOL + "augmentedLoopback()");
        // just a smattering...
        for (int b1=0; b1<256; b1+=77) {
            for (int b2=7; b2<230; b2+=91) {
                for (int b3=35; b3<128; b3+=29) {
                    IpAddress ip = ip("127."+b1+"."+b2+"."+b3);
                    print("  verifying " + ip);
                    assertTrue("IP not loopback", ip.isLoopback());
                }
            }
        }
    }

    @Test
    public void getUndetermined() {
        IpAddress u4 = IpAddress.UNDETERMINED_IPv4;
        assertEquals(AM_HUH, "0.0.0.0", u4.toString());
        assertTrue(AM_HUH, u4.isUndetermined());
        assertEquals(AM_HUH, IPv4, u4.getFamily());
        final byte[] b4 = new byte[4]; // all zeros
        assertArrayEquals(AM_HUH, b4, u4.toByteArray());

        IpAddress u6 = IpAddress.UNDETERMINED_IPv6;
        assertEquals(AM_HUH, "::", u6.toShortString());
        assertTrue(AM_HUH, u6.isUndetermined());
        assertEquals(AM_HUH, IPv6, u6.getFamily());
        final byte[] b6 = new byte[16]; // all zeros
        assertArrayEquals(AM_HUH, b6, u6.toByteArray());
    }

    @Test
    public void toStringWithPort() {
        print(EOL + "toStringWithPort():");
        IpAddress ip = ip(S_V6_E1);
        String portStr = ip.toStringWithPort(3456);
        IpAddress ip2 = ip(portStr);
        int port = IpAddress.valueOfPort(portStr);
        print("  IP: " + ip);
        print("  with port: " + portStr);

        assertEquals(AM_NSR, ip, ip2);
        assertEquals(AM_HUH, 3456, port);
    }


    // these IP addresses are in increasing natural order
    private static final IpAddress ip1 = ip("15.33.1.100");
    private static final IpAddress ip2 = ip("15.44.255.1");
    private static final IpAddress ip3 = ip("20.3.200.30");
    private static final IpAddress ip4 = ip("20.3.200.200");
    private static final IpAddress ip5 = ip("20.11.200.200");
    private static final IpAddress ip6 = ip("111.1.1.1");
    private static final IpAddress ip7 = ip("111.1.2.1");
    private static final IpAddress ip8 = ip("111.1.13.1");

    private static final IpAddress[] sorted = new IpAddress[] {
        ip1, ip2, ip3, ip4, ip5, ip6, ip7, ip8,
    };

    @Test
    public void defensiveCopyIPv4() {
        print(EOL + "defensiveCopyIPv4():");
        String str = "15.23.255.1";
        byte[] bytes = new byte[] { 15, 23, 255-B, 1 };

        // make a copy, before we modify the original
        byte[] bytesOriginalCopy = bytes.clone();

        // create two IPs
        IpAddress ipFromStr = ip(str);
        IpAddress ipFromBytes = ip(bytes);

        // verify they are actually the same instance of IP address
        assertEquals(AM_NSR, ipFromStr, ipFromBytes);
        assertEquals(AM_HUH, str, ipFromBytes.toString());
        print("  Orig Array    : " + Arrays.toString(bytes));
        print("  Orig IP       : " + ipFromBytes);
        print("  Orig IP bytes : " + Arrays.toString(ipFromBytes.toByteArray()));

        // change one of our original values -- trying to corrupt the IP instance
        bytes[3] = 77;
        print(EOL + "  Changed Array : " + Arrays.toString(bytes));
        print("  Orig IP       : " + ipFromBytes);
        print("  Orig IP bytes : " + Arrays.toString(ipFromBytes.toByteArray()));

        // verify that the string representation of the ip-from-bytes did not change
        String strNotChanged = ipFromBytes.toString();
        assertEquals(AM_HUH, str, strNotChanged);

        // verify that the byte array of the ip-from-bytes did not change
        byte[] ipBytes = ipFromBytes.toByteArray();
        assertArrayEquals(AM_HUH, bytesOriginalCopy, ipBytes);
    }

    @Test
    public void defensiveCopyIPv6() {
        print(EOL + "testDefensiveCopyIPv6():");
        String str = "5::aaaa:bbbb";
        byte[] bytes = new byte[] {
                0x00,   0x05,
                0x00,   0x00,
                0x00,   0x00,
                0x00,   0x00,
                0x00,   0x00,
                0x00,   0x00,
                0xAA-B, 0xAA-B,
                0xBB-B, 0xBB-B,
        };

        // make a copy, before we modify the original
        byte[] bytesOriginalCopy = bytes.clone();

        // create two IPs
        IpAddress ipFromStr = ip(str);
        IpAddress ipFromBytes = ip(bytes);

        // verify they are actually the same instance of IP address
        assertEquals(AM_NSR, ipFromStr, ipFromBytes);
        print("  Orig Array    : " + Arrays.toString(bytes));
        print("  Orig IP       : " + ipFromBytes);
        print("  Orig IP bytes : " + Arrays.toString(ipFromBytes.toByteArray()));

        // change one of our original values -- trying to corrupt the IP instance
        bytes[0] = 0xFF-B;
        bytes[1] = 0xFF-B;
        print(EOL + "  Changed Array : " + Arrays.toString(bytes));
        print("  Orig IP       : " + ipFromBytes);
        print("  Orig IP bytes : " + Arrays.toString(ipFromBytes.toByteArray()));

        // verify that the byte array of the ip-from-bytes did not change
        byte[] ipBytes = ipFromBytes.toByteArray();
        assertArrayEquals(AM_HUH, bytesOriginalCopy, ipBytes);
    }

    @Test
    public void comparisonsOne() {
        print(EOL + "comparisonsOne():");
        for (int c=1; c< sorted.length; c++) {
            for (int i=0; i<c; i++) {
                print("  comparing: " + i + " to " + c);
                assertTrue(AM_HUH, sorted[i].compareTo(sorted[c]) < 0);
                assertTrue(AM_HUH, sorted[c].compareTo(sorted[i]) > 0);
                assertTrue(AM_HUH, sorted[i].compareTo(sorted[i]) == 0);
            }
        }
    }

    @Test
    public void comparisonsTwo() {
        print(EOL + "comparisonsTwo():");
        IpAddress x4 = ip("20.30.56.17");
        IpAddress y4 = ip("50.20.0.1");
        IpAddress x6 = ip("::2:ffee");
        IpAddress y6 = ip("::3:0011");
        IpAddress z6 = ip("ff00::3:2:1");
        IpAddress w6 = ip("::141e:3811"); // SAME AS x4 EXCEPT IPv6

        IpAddress[] sorted = new IpAddress[] {
                x6, y6, x4, w6, y4, z6,
        };

        for (int i=0; i<sorted.length-1; i++) {
            IpAddress a = sorted[i];
            for (int j=i+1; j<sorted.length; j++) {
                IpAddress b = sorted[j];
                print(a.compareTo(b) + " : " + a + " <=> " + b);
                assertTrue(AM_A_NLT_B, a.compareTo(b) < 0);
                assertTrue(AM_B_NGT_A, b.compareTo(a) > 0);
            }
        }
    }

    @Test (expected = IllegalArgumentException.class)
    public void toStringWithPortTooBig() {
        IpAddress ip = ip("12.13.14.15");
        print(ip.toStringWithPort(99999));
    }

    @Test (expected = IllegalArgumentException.class)
    public void toStringWithPortTooSmall() {
        IpAddress ip = ip("12.13.14.15");
        print(ip.toStringWithPort(-2));
    }

    @Test
    public void toStringWithPortNegOne() {
        IpAddress ip = ip("12.13.14.15");
        print(ip.toStringWithPort(-1));
        // NOTE: we have to allow this.. NO_PORT is represented by -1
    }

    /**
     * Verify the getInetAddress method returns a valid InetAddress instance.
     */
    @Test
    public void getInetAddress() {
        IpAddress ip4 = ip("10.1.2.3");
        assertEquals(AM_NEQ, "10.1.2.3", ip4.toInetAddress().getHostAddress());

        IpAddress ip6 = ip("ff00::3:2:1");
        assertEquals(AM_NEQ, "ff00:0:0:0:0:3:2:1", ip6.toInetAddress().getHostAddress());
    }

    @Test
    public void basicAddressClass() {
        print(EOL + "basicAddressClass()");
        checkAddressClass("15.2.12.5", IpAddress.AddressClass.A, SubnetMask.MASK_255_0_0_0);
        checkAddressClass("130.5.12.37", IpAddress.AddressClass.B, SubnetMask.MASK_255_255_0_0);
        checkAddressClass("200.34.56.78", IpAddress.AddressClass.C, SubnetMask.MASK_255_255_255_0);
        checkAddressClass("225.34.56.78", IpAddress.AddressClass.D, null);
        checkAddressClass("241.34.56.78", IpAddress.AddressClass.E, null);
        checkAddressClass("ffff::", IpAddress.AddressClass.CLASSLESS, null);
    }

    private void checkAddressClass(String ipStr, IpAddress.AddressClass expectedClass, SubnetMask expectedMask) {
        IpAddress.AddressClass cls = ip(ipStr).getAddressClass();
        SubnetMask mask = cls.getImpliedMask();
        print(" IP address: "+ ipStr + ", address class: " + cls + ", subnet mask: " + mask);
        assertEquals("wrong address class", expectedClass, cls);
        assertEquals("wrong implied mask", expectedMask, mask);
    }

    @Test
    public void reservedPredicate() {
        print(EOL + "reservedPredicate()");
        // IPv4 only, for the time being...
        for (IpAddress ip : IpUtils.getRandomIps("0-2.5.6.100-254", 30)) {
            boolean expected = ip.toByteArray()[0] == 0;
            print((expected ? "   " : "NOT") + " reserved: " + ip);
            assertEquals(AM_HUH, expected, ip.isReserved());
        }
    }

    @Test
    public void broadcastPredicate() {
        print(EOL + "broadcastPredicate()");
        // IPv4 only, for the time being...
        assertFalse(AM_HUH, ip("255.255.255.254").isBroadcast());
        assertTrue(AM_HUH, ip("255.255.255.255").isBroadcast());
        assertTrue(AM_HUH, IpAddress.BROADCAST_IPv4.isBroadcast());
    }

    private static final String[] MULTICAST_TEST = {
            "15.37.2.1",    null,
            "223.0.0.1",    null,
            "224.0.0.1",    "All Subnet Systems Multicast",
            "224.0.0.2",    "All Subnet Routers Multicast",
            "224.0.0.3",    "Unknown",
            "224.0.0.4",    "DVMRP Routers Multicast",
            "224.0.0.5",    "OSPF IGP All Routers Multicast",
            "239.255.255.255", "Unknown",
            "240.0.0.0",    null,
    };

    @Test
    public void multicast() {
        print(EOL + "multicast()");
        for (int idx=0; idx<MULTICAST_TEST.length; idx+=2) {
            IpAddress ip = ip(MULTICAST_TEST[idx]);
            String expectedName = MULTICAST_TEST[idx+1];
            boolean expected = expectedName!=null;  // expected result of isMulticast()

            String isMC = ip.isMulticast() ? "Multicast" : "NOT multicast";
            String name = ip.getMulticastName();
            String ns = name == null ? "" : name;
            print(ip + " " + isMC + " -> " + ns);
            assertEquals("bad isMulticast() result", expected, ip.isMulticast());
            assertEquals("bad getMulticastName() result", expectedName, ip.getMulticastName());
        }

    }

    private static final IpAddress LL_0 = ip("fe80::1234");
    private static final IpAddress LL_1 = ip("fe80::5678");
    private static final IpAddress LL_2 = ip("fe80::999");
    private static final IpAddress LL_3 = ip("fe80::ffff");
    private static final IpAddress LL_NOT1 = ip("fec1::fff");
    private static final IpAddress LL_NOT2 = ip("fe71::");
    private static final IpAddress LL_NOTIP4 = ip("10.1.1.1");

    private static final IpAddress LL_41 = ip("169.254.0.0"); // reserved but valid
    private static final IpAddress LL_42 = ip("169.254.255.255"); // reserved but valid
    private static final IpAddress LL_43 = ip("169.254.35.67");
    private static final IpAddress LL_NOT_41 = ip("168.254.35.67");
    private static final IpAddress LL_NOT_42 = ip("169.253.35.67");

    private void verifyLinkLocal(IpAddress ip, boolean expected) {
        print(ip + " -> " + ip.isLinkLocal());
        assertEquals(AM_HUH, expected, ip.isLinkLocal());
    }

    @Test
    public void linkLocal() {
        print(EOL + "linkLocal()");

        verifyLinkLocal(LL_0, true);
        verifyLinkLocal(LL_1, true);
        verifyLinkLocal(LL_2, true);
        verifyLinkLocal(LL_3, true);
        verifyLinkLocal(LL_NOT1, false);
        verifyLinkLocal(LL_NOT2, false);
        verifyLinkLocal(LL_NOTIP4, false);

        verifyLinkLocal(LL_41, true);
        verifyLinkLocal(LL_42, true);
        verifyLinkLocal(LL_43, true);
        verifyLinkLocal(LL_NOT_41, false);
        verifyLinkLocal(LL_NOT_42, false);

        verifyLinkLocal(IpAddress.BROADCAST_IPv4, false);
        verifyLinkLocal(IpAddress.LOOPBACK_IPv4, false);
        verifyLinkLocal(IpAddress.LOOPBACK_IPv6, false);
        verifyLinkLocal(IpAddress.UNDETERMINED_IPv4, false);
        verifyLinkLocal(IpAddress.UNDETERMINED_IPv6, false);
    }

    private static final String ILLEGAL_1 = "fe81::1234";
    private static final String ILLEGAL_2 = "fe80:0:0:1::1234";
    private static final String ILLEGAL_3 = "fe80:0:2::1234";
    private static final String ILLEGAL_4 = "fe80:3::1234";
    private static final String ILLEGAL_5 = "fea0::1234";
    private static final String GOOD_1 = "fe80::ffff:0:0:1234";
    private static final String GOOD_2 = "fe80::ffff:0:1234";
    private static final String GOOD_3 = "fe80::ffff:1234";


    @Test
    public void illegalLinkLocalAddresses() {
        print(EOL + "illegalLinkLocalAddresses()");
        validateIllegal(ILLEGAL_1, true);
        validateIllegal(ILLEGAL_2, true);
        validateIllegal(ILLEGAL_3, true);
        validateIllegal(ILLEGAL_4, true);
        validateIllegal(ILLEGAL_5, true);
        validateIllegal(GOOD_1, false);
        validateIllegal(GOOD_2, false);
        validateIllegal(GOOD_3, false);
    }

    private void validateIllegal(String ipStr, boolean expectException) {
        print("  " + ipStr);
        try {
            ip(ipStr);
            print("    no exception");
            if (expectException)
                fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("    "+e);
            if (!expectException) {
                fail("Exception not expected");
            }
        }
    }

}
