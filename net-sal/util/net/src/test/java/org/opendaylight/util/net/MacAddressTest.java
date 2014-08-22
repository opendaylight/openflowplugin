/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.net.MacAddress.Format.*;
import static org.opendaylight.util.net.MacAddress.mac;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * This class implements unit tests for {@link MacAddress}.
 *
 * @author Simon Hunt
 */
public class MacAddressTest {

    private static final int B = 256;

    private static final String TBASIC_BASE = "00:00:4e:01:02:ff";
    private static final String[] TBASIC_STR = {
            "00:00:4e:01:02:ff",
            "00004e:0102ff",
            "00-00-4e-01-02-ff",
            "00004e-0102ff",
            "00004e0102ff",
            "00004E0102FF",
    };
    private static final long TBASIC_LONG = 0x00004e0102ff;
    private static final byte[] TBASIC_BYTES =
            { 0x00, 0x00, 0x4e, 0x01, 0x02, 0xff-B };
    private static final String TBASIC_ETHCO = "AMPEX CORPORATION";

    @Test
    public void basicEquivalence() {
        MacAddress base = mac(TBASIC_BASE);
        // test different string representations
        for (String s: TBASIC_STR)
            assertEquals(AM_NSR, base, mac(s));

        // from a long
        assertEquals(AM_NSR, base, mac(TBASIC_LONG));
        // from a byte array
        assertEquals(AM_NSR, base, mac(TBASIC_BYTES));
        // see ethernetCompanies.properties
        assertEquals(AM_HUH, TBASIC_ETHCO, base.getEthernetCompany());
    }

    @Test
    public void byteArrayFromString() {
        byte[] bytes = mac(TBASIC_BASE).toByteArray();
        assertArrayEquals(AM_HUH, TBASIC_BYTES, bytes);
        
        ByteBuffer bb = ByteBuffer.allocate(TBASIC_BYTES.length);
        mac(TBASIC_BASE).intoBuffer(bb);
        assertArrayEquals(AM_HUH, TBASIC_BYTES, bb.array());
    }

    //================================================
    // === test boundary conditions for valueOf(long)

    private static final String MAC_STR_ZERO        = "00:00:00:00:00:00";
    private static final String MAC_STR_ALL_FF      = "ff:ff:ff:ff:ff:ff";

    private static final byte[] MAC_BYTE_ALL_FF =
            new byte[] { -1, -1, -1, -1, -1, -1 };

    private static final long MAC_ZERO              = 0L;
    private static final long MAC_ALL_FF            = 281474976710655L;
    private static final long MAC_ONE_BIT_TOO_MANY  = 281474976710656L;

    @Test (expected = IllegalArgumentException.class)
    public void valueOfLongPlusOne() {
        mac(MAC_ONE_BIT_TOO_MANY); // one bit too big
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfLongMax() {
        mac(Long.MAX_VALUE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfLongNegOne() {
        mac(-1L);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfLongMin() {
        mac(Long.MIN_VALUE);
    }

    @Test
    public void valueOfLongLow() {
        assertEquals(AM_HUH, MAC_STR_ZERO, mac(MAC_ZERO).toString());
    }

    @Test
    public void valueOfLongHigh() {
        assertEquals(AM_HUH, MAC_STR_ALL_FF, mac(MAC_ALL_FF).toString());
    }


    //==================================================
    // === test boundary conditions for valueOf(String)

    @Test (expected = NullPointerException.class)
    public void valueOfStringNull() {
        mac((String)null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringEmptyString() {
        mac("");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringShortString() {
        mac("11:22:33:44:55");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringLongString() {
        mac("11:22:33:44:55:66:77");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBadString() {
        mac("11:22:33:4Q:55:66");
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfStringBadString2() {
        mac("xyzzy");
    }


    //==================================================
    // === test boundary conditions for valueOf(byte[])

    @Test (expected = NullPointerException.class)
    public void valueOfByteArrayNull() {
        mac((byte[])null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfByteArrayZeroLength() {
        mac(new byte[0]);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfByteArrayTooShort() {
        mac(new byte[3]);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfByteArrayTooLong() {
        mac(new byte[7]);
    }


    //==================================================

    @Test
    public void valueFrom() {
        ByteBuffer bb = ByteBuffer.wrap(SAMPLE_MAC);
        MacAddress ma = MacAddress.valueFrom(bb);
        assertEquals(AM_NEQ, MacAddress.valueOf(SAMPLE_MAC), ma);
    }

    //==================================================
    // === test different string representations

    private static final String M123_MC_L = "11:22:33:aa:bb:cc";
    private static final String M123_MC_U = "11:22:33:AA:BB:CC";
    private static final String M123_MD_L = "11-22-33-aa-bb-cc";
    private static final String M123_MD_U = "11-22-33-AA-BB-CC";
    private static final String M123_SC_L = "112233:aabbcc";
    private static final String M123_SC_U = "112233:AABBCC";
    private static final String M123_SD_L = "112233-aabbcc";
    private static final String M123_SD_U = "112233-AABBCC";
    private static final String M123_ND_L = "112233aabbcc";
    private static final String M123_ND_U = "112233AABBCC";

    private static final String[] M123_STR = {
            M123_MC_L, M123_MC_U,
            M123_MD_L, M123_MD_U,
            M123_SC_L, M123_SC_U,
            M123_SD_L, M123_SD_U,
            M123_ND_L, M123_ND_U,
    };
    private static final byte[] M123_BYTES = {
            0x11, 0x22, 0x33, 0xAA-B, 0xBB-B, 0xCC-B
    };

    @Test
    public void valueOfStringFormats() {
        MacAddress base = mac(M123_BYTES);
        print("valueOfStringFormats():");
        print(" BASE: " + base);
        print(" " + Arrays.toString(base.toByteArray()));
        for (String s: M123_STR)
            assertEquals(AM_NSR, base, mac(s));
    }

    @Test
    public void defaultFormat() {
        MacAddress abcfed = mac("aabbcc-ffeedd");
        assertEquals(AM_HUH, abcfed.toString(),
                abcfed.toFormattedString(MULTI_COLON));
    }

    @Test
    public void toFormattedString() {
        MacAddress base = mac(M123_BYTES);

        final String baseStr = base.toString();
        assertEquals(AM_HUH, baseStr, base.toFormattedString(MULTI_COLON));

        assertEquals(AM_HUH, M123_MC_L,
                base.toFormattedString(MULTI_COLON, true));
        assertEquals(AM_HUH, M123_MC_U,
                base.toFormattedString(MULTI_COLON, false));

        assertEquals(AM_HUH, M123_MD_L,
                base.toFormattedString(MULTI_DASH, true));
        assertEquals(AM_HUH, M123_MD_U,
                base.toFormattedString(MULTI_DASH, false));

        assertEquals(AM_HUH, M123_SC_L,
                base.toFormattedString(SINGLE_COLON, true));
        assertEquals(AM_HUH, M123_SC_U,
                base.toFormattedString(SINGLE_COLON, false));

        assertEquals(AM_HUH, M123_SD_L,
                base.toFormattedString(SINGLE_DASH, true));
        assertEquals(AM_HUH, M123_SD_U,
                base.toFormattedString(SINGLE_DASH, false));

        assertEquals(AM_HUH, M123_ND_L,
                base.toFormattedString(NO_DELIMITER, true));
        assertEquals(AM_HUH, M123_ND_U,
                base.toFormattedString(NO_DELIMITER, false));
    }

    @Test
    public void allFs() {
        MacAddress macFromLong = mac(MAC_ALL_FF);
        MacAddress macFromString = mac(MAC_STR_ALL_FF);
        MacAddress macFromBytes = mac(MAC_BYTE_ALL_FF);

        assertEquals(AM_NSR, macFromLong, macFromString);
        assertEquals(AM_NSR, macFromLong, macFromBytes);
        assertTrue(AM_HUH, macFromLong.isBroadcast());

        assertEquals(AM_HUH, MAC_ALL_FF, macFromLong.toLong());
        assertEquals(AM_HUH, MAC_STR_ALL_FF, macFromLong.toString());
        assertArrayEquals(AM_HUH, MAC_BYTE_ALL_FF, macFromLong.toByteArray());
        
        ByteBuffer bb = ByteBuffer.allocate(MAC_BYTE_ALL_FF.length);
        macFromLong.intoBuffer(bb);
        assertArrayEquals(AM_HUH, MAC_BYTE_ALL_FF, bb.array());
    }

    @Test
    public void convertValues() {
        MacAddress mac = mac(MAC_ALL_FF);   // start from a long
        byte[] asBytes = mac.toByteArray();
        assertArrayEquals(AM_HUH, MAC_BYTE_ALL_FF, asBytes);

        // let's change some bytes
        asBytes[0] = 1;
        asBytes[1] = 2;
        asBytes[2] = 4;

        MacAddress mac2 = mac(asBytes);
        assertEquals(AM_HUH, "01:02:04:ff:ff:ff", mac2.toString());
    }

    @Test
    public void defensiveCopy() {
        print(EOL + "testDefensiveCopy():");
        byte[] bytes = new byte[] { 0x00, 0x05, 0x99-B, 0x0a, 0x0b, 0x0c };

        // make a copy, before we modify the original
        byte[] bytesOriginalCopy = bytes.clone();

        // create a mac address
        MacAddress mac = mac(bytes);

        print("  Orig Array     : " + Arrays.toString(bytes));
        print("  Orig MAC       : " + mac);
        print("  Orig MAC bytes : " + Arrays.toString(mac.toByteArray()));

        // change some original values -- try to corrupt the MAC instance
        bytes[0] = 0xFF-B;
        bytes[1] = 0xFF-B;
        print(EOL + "  Changed Array : " + Arrays.toString(bytes));
        print("  Orig MAC       : " + mac);
        print("  Orig MAC bytes : " + Arrays.toString(mac.toByteArray()));

        // verify that the byte array of the MAC did not change
        byte[] macBytes = mac.toByteArray();
        assertArrayEquals(AM_HUH, bytesOriginalCopy, macBytes);
    }

    @Test
    public void noEthernetCoLookup() {
        // see ethernetCompanies.properties
        MacAddress eee = mac("eeeeee:eeeeee");
        String eeeCo = eee.getEthernetCompany();
        assertEquals(AM_HUH, "Unknown", eeeCo);
        print(EOL + eee + " : " + eeeCo);
    }

    @Test
    public void equality() {
        MacAddress ffs = mac(MAC_STR_ALL_FF);
        MacAddress oos = mac(MAC_STR_ZERO);
        print (EOL + ffs + EOL + oos);
        assertTrue(AM_HUH, ffs.equals(ffs));
        assertFalse(AM_HUH, ffs.equals(oos));
        assertFalse(AM_HUH, oos.equals(ffs));
        assertFalse(AM_HUH, oos.equals(Boolean.TRUE));
    }


    private static final String[] SORTED = new String[] {
            "000015-0000de",
            "000034-123456",
            "000100-3fbcdd",
            "122334-000d0d",
            "122334-009988",
            "122334-0a9988",
            "aabbcc-ddeeff",
            "ff0000-4d3d44",
    };

    private static final String[] UNSORTED = new String[] {
            "ff0000-4d3d44",
            "122334-0a9988",
            "000100-3fbcdd",
            "000015-0000de",
            "122334-000d0d",
            "000034-123456",
            "aabbcc-ddeeff",
            "122334-009988",
    };

    @Test
    public void comparisons() {
        assertFalse(AM_HUH, Arrays.equals(SORTED, UNSORTED));

        MacAddress[] sorted = makeArray(SORTED);
        MacAddress[] startUnsorted = makeArray(UNSORTED);
        assertFalse(AM_HUH, Arrays.equals(sorted, startUnsorted));
        Arrays.sort(startUnsorted);
        assertTrue(AM_HUH, Arrays.equals(sorted, startUnsorted));
    }

    private MacAddress[] makeArray(String[] strings) {
        MacAddress[] array = new MacAddress[strings.length];
        int idx = 0;
        for (String s: strings)
            array[idx++] = mac(s);
        return array;
    }

    private static final String[] SOME_PRIVATE_OUIS = {
            "000101",
            "00054f",
            "000578",
            "002419",
            "00bb3a",
    };

    @Test
    public void getPrivateCompanyNames() {
        print(EOL + "getPrivateCompanyNames()");
        String expected = "Private";
        for (String oui: SOME_PRIVATE_OUIS) {
            MacAddress mac = MacAddress.valueOf(oui + ":000000");
            String name = mac.getEthernetCompany();
            print("{} => {}", oui, name);
            assertEquals("Company not shown as private", expected, name);
        }
    }

    @Test
    public void getACompanyName() {
        print(EOL + "getACompanyName()");
        String expected = "cyberPIXIE, Inc.";
        String name = mac(new byte[] { 0x00, 0x04, 0x40, 0,0,0 } )
                .getEthernetCompany();
        print (name);
        assertEquals("wrong ethernet company name", expected, name);
    }

    private static final MacAddress BROAD = mac("ffffff-ffffff");
    private static final MacAddress MULTI = mac("112334-000d0d");
    private static final MacAddress UNI = mac("102334-000d0d");
    private static final MacAddress VRRP_VMAC = mac("00005e-000101");

    @Test
    public void broadcast() {
        print(EOL + "broadcast()");
        assertTrue(AM_HUH, BROAD.isBroadcast());
        assertFalse(AM_HUH, MULTI.isBroadcast());
        assertFalse(AM_HUH, UNI.isBroadcast());
    }

    @Test
    public void multicast() {
        print(EOL + "multicast()");
        assertTrue(AM_HUH, BROAD.isMulticast());
        assertTrue(AM_HUH, MULTI.isMulticast());
        assertFalse(AM_HUH, UNI.isMulticast());
    }

    @Test
    public void vrrpVmac() {
        print(EOL + "vrrpVmac()");
        assertTrue(AM_HUH, VRRP_VMAC.isVrrpVmac());
        assertFalse(AM_HUH, BROAD.isVrrpVmac());
        assertFalse(AM_HUH, MULTI.isVrrpVmac());
        assertFalse(AM_HUH, UNI.isVrrpVmac());
    }

    @Test
    public void toStringLowerCase() {
        MacAddress m = MacAddress.valueOf("0123456789ab");
        print("toStringLowerCase()");
        assertEquals(AM_NEQ, "01:23:45:67:89:ab", m.toString());
    }


    // want to test the speed at which these two alternate approaches to
    // creating a unique string key for a byte array happen...

    private String arrayKey1(byte[] b) {
        return Arrays.toString(b);
    }

    private String arrayKey2(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes)
            sb.append(b);
        return sb.toString();
    }

    private static final int KEY_ITERATIONS = 5000000; // 500M
    private static final byte[] SAMPLE_MAC = { 0, 1, 2, 3, 4, 5 };

    @Test
    public void byteArrayToStringSpeedTest() {
        assumeTrue(!isUnderCoverage() && !ignoreSpeedTests());
        print(EOL + "byteArrayToStringSpeedTest()");

        long start = System.currentTimeMillis();
        for (int i=0; i<KEY_ITERATIONS; i++)
            arrayKey1(SAMPLE_MAC);
        long duration1 = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        for (int i=0; i<KEY_ITERATIONS; i++)
            arrayKey2(SAMPLE_MAC);
        long duration2 = System.currentTimeMillis() - start;

        print("{} iterations...", KEY_ITERATIONS);
        print("Arrays.toString() : {}ms", duration1);
        print("Custom            : {}ms", duration2);
        long saved = duration1 - duration2;
        double percentSavings = ((double) saved) / ((double) duration1) * 100.0;
        print("Percent savings   : {}%", String.format("%.2f", percentSavings));
        assertTrue(percentSavings + "is less than 20% gain",
                   percentSavings > perfScale() * 20.0);
    }
    
    private static final String LINK_LOCAL_PREFIX = "01:80:c2:00:00:";
    private static final String[] LINK_LOCAL_MACS = {
            LINK_LOCAL_PREFIX + "0e",
            LINK_LOCAL_PREFIX + "03",
            LINK_LOCAL_PREFIX + "00",
    };
    private static final String[] NOT_LL_MACS = {
            LINK_LOCAL_PREFIX + "01",
            MAC_STR_ALL_FF,
            TBASIC_BASE,
    };
    
    @Test
    public void linkLocal() {
        for (String s: LINK_LOCAL_MACS) {
            MacAddress m = mac(s);
            print("{} => {}", m, m.toLong());
            assertTrue(AM_HUH, m.isLinkLocal());
        }
        for (String s: NOT_LL_MACS) {
            MacAddress m = mac(s);
            print(m);
            assertFalse(AM_HUH, m.isLinkLocal());
        }
    }
    
    private static final MacAddress[] LINK_LOCAL_CONSTANTS  = {
            MacAddress.LINK_LOCAL_0E,
            MacAddress.LINK_LOCAL_03,
            MacAddress.LINK_LOCAL_00,
    };                                            
            
    @Test
    public void linkLocalConstants() {
        for (int i=0; i< LINK_LOCAL_CONSTANTS.length; i++) {
            print("Link Local Constant: {}", LINK_LOCAL_CONSTANTS[i]);
            assertEquals(AM_NEQ, mac(LINK_LOCAL_MACS[i]), LINK_LOCAL_CONSTANTS[i]);
        }
    }
}
