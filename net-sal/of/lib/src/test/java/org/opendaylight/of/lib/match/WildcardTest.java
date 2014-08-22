/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.BitmappedEnumTest;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.net.IpAddress;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.match.Wildcard.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for Wildcard.
 *
 * @author Simon Hunt
 */
public class WildcardTest extends BitmappedEnumTest<Wildcard> {

    @Override
    protected Set<Wildcard> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return Wildcard.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<Wildcard> flags, ProtocolVersion pv) {
        return Wildcard.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (Wildcard w : Wildcard.values())
            print(w);
        assertEquals(AM_UXCC, 14, Wildcard.values().length);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        verifyNaU32(V_1_3, 0x1);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        verifyNaU32(V_1_2, 0x1);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        verifyBit(V_1_1, 0x1, IN_PORT);
        verifyBit(V_1_1, 0x2, DL_VLAN);
        verifyBit(V_1_1, 0x4, DL_VLAN_PCP);
        verifyBit(V_1_1, 0x8, DL_TYPE);
        verifyBit(V_1_1, 0x10, NW_TOS);
        verifyBit(V_1_1, 0x20, NW_PROTO);
        verifyBit(V_1_1, 0x40, TP_SRC);
        verifyBit(V_1_1, 0x80, TP_DST);
        verifyBit(V_1_1, 0x100, MPLS_LABEL);
        verifyBit(V_1_1, 0x200, MPLS_TC);
        verifyNaU32(V_1_1, 0x400);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, IN_PORT);
        verifyBit(V_1_0, 0x2, DL_VLAN);
        verifyBit(V_1_0, 0x4, DL_SRC);
        verifyBit(V_1_0, 0x8, DL_DST);
        verifyBit(V_1_0, 0x10, DL_TYPE);
        verifyBit(V_1_0, 0x20, NW_PROTO);
        verifyBit(V_1_0, 0x40, TP_SRC);
        verifyBit(V_1_0, 0x80, TP_DST);
        verifyNaBit(V_1_0, 0x100);
        verifyNaBit(V_1_0, 0x200);
        verifyNaBit(V_1_0, 0x400);
        verifyNaBit(V_1_0, 0x800);
        verifyNaBit(V_1_0, 0x1000);
        verifyNaBit(V_1_0, 0x2000);
        verifyNaBit(V_1_0, 0x4000);
        verifyNaBit(V_1_0, 0x8000);
        verifyNaBit(V_1_0, 0x10000);
        verifyNaBit(V_1_0, 0x20000);
        verifyNaBit(V_1_0, 0x40000);
        verifyNaBit(V_1_0, 0x80000);
        verifyBit(V_1_0, 0x100000, DL_VLAN_PCP);
        verifyBit(V_1_0, 0x200000, NW_TOS);
        verifyNaU32(V_1_0, 0x400000);
    }

    @Test
    public void samples() {
        print(EOL + "samples()");
        verifyBitmappedFlags(V_1_0, 0x200041, NW_TOS, TP_SRC, IN_PORT);
        verifyBitmappedFlags(V_1_0, 0x100088, DL_VLAN_PCP, TP_DST, DL_DST);

        verifyBitmappedFlags(V_1_1, 0x45, TP_SRC, DL_VLAN_PCP, IN_PORT);
        verifyBitmappedFlags(V_1_1, 0x88, DL_TYPE, TP_DST);
    }

    // === Test encode/decode netmasks...

    @Test(expected = VersionMismatchException.class)
    public void decodeNetmaskNot13() {
        Wildcard.decodeNetmask(NW_SRC, 0, V_1_3);
    }

    @Test(expected = VersionMismatchException.class)
    public void decodeNetmaskNot12() {
        Wildcard.decodeNetmask(NW_SRC, 0, V_1_2);
    }

    @Test(expected = VersionMismatchException.class)
    public void decodeNetmaskNot11() {
        Wildcard.decodeNetmask(NW_SRC, 0, V_1_1);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeNetmaskNot13() {
        Wildcard.encodeNetmask(NW_SRC, EXP_NW_DST_MASK, 0, V_1_3);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeNetmaskNot12() {
        Wildcard.encodeNetmask(NW_SRC, EXP_NW_DST_MASK, 0, V_1_2);
    }

    @Test(expected = VersionMismatchException.class)
    public void encodeNetmaskNot11() {
        Wildcard.encodeNetmask(NW_SRC, EXP_NW_DST_MASK, 0, V_1_1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negIpMaskForBits() {
        Wildcard.getIpMaskForWildBits(-1);
    }

    private void checkIpBits(String expIpStr, int wildBits) {
        IpAddress mask = Wildcard.getIpMaskForWildBits(wildBits);
        print("wild bits: {} gives IP mask: {}", wildBits, mask);
        assertEquals(AM_NEQ, IpAddress.valueOf(expIpStr), mask);
    }

    private static final String[] MASKS = {
            "255.255.255.255",
            "255.255.255.254",
            "255.255.255.252",
            "255.255.255.248",
            "255.255.255.240",
            "255.255.255.224",
            "255.255.255.192",
            "255.255.255.128",
            "255.255.255.0",
            "255.255.254.0",
            "255.255.252.0",
            "255.255.248.0",
            "255.255.240.0",
            "255.255.224.0",
            "255.255.192.0",
            "255.255.128.0",
            "255.255.0.0",
            "255.254.0.0",
            "255.252.0.0",
            "255.248.0.0",
            "255.240.0.0",
            "255.224.0.0",
            "255.192.0.0",
            "255.128.0.0",
            "255.0.0.0",
            "254.0.0.0",
            "252.0.0.0",
            "248.0.0.0",
            "240.0.0.0",
            "224.0.0.0",
            "192.0.0.0",
            "128.0.0.0",
            "0.0.0.0",
    };

    @Test
    public void ipMasksForBits() {
        print(EOL + "ipMasksForBits()");
        for (int nWild = 0; nWild < 32; nWild++)
            checkIpBits(MASKS[nWild], nWild);

        for (int nWild = 32; nWild < 64; nWild++)
            checkIpBits("0.0.0.0", nWild);
    }

    // nw-src field : 8 => 255.255.255.0 : 0b001000
    // nw-dst field : 20 => 255.240.0.0 : 0b010100
    // Add in DL_VLAN_PCP (0x100000) and DL_VLAN (0x2) for good measure
    //
    // 3 2 1 0
    // 10987654321098765432109876543210
    // xx<NDST><NSRC>xxxxxxxx NW_DST NW_SRC
    // 101010000100000000010 => 0x150802 20 8
    // 110000011111100000010 => 0x183f02 32 63

    private static final int ENCODED_FLAGS = 0x100002;
    private static final int ENCODED = 0x150802;
    private static final int ENCODED_ALL_WILD = 0x183f02;
    private static final IpAddress EXP_NW_SRC_MASK = ip("255.255.255.0");
    private static final IpAddress EXP_NW_DST_MASK = ip("255.240.0.0");
    private static final IpAddress EXP_ALL_WILD = ip("0.0.0.0");

    private void checkDecodeNetMask(Wildcard w, int encoded, IpAddress exp) {
        IpAddress result = Wildcard.decodeNetmask(w, encoded, V_1_0);
        print("{} from {} => {}", w, hex(encoded), result);
        assertEquals(AM_NEQ, exp, result);
    }

    @Test
    public void decodeNetmask() {
        print(EOL + "decodeNetmask()");
        checkDecodeNetMask(NW_SRC, ENCODED, EXP_NW_SRC_MASK);
        checkDecodeNetMask(NW_DST, ENCODED, EXP_NW_DST_MASK);
        checkDecodeNetMask(NW_SRC, ENCODED_ALL_WILD, EXP_ALL_WILD);
        checkDecodeNetMask(NW_DST, ENCODED_ALL_WILD, EXP_ALL_WILD);
    }

    private void checkEncodeNetMask(int base, IpAddress src, IpAddress dst,
                                    int exp) {
        int result = Wildcard.encodeNetmask(NW_SRC, src, base, V_1_0);
        result = Wildcard.encodeNetmask(NW_DST, dst, result, V_1_0);
        print("Inserting SRC={} and DST={} => {}", src, dst, hex(result));
        assertEquals(AM_NEQ, exp, result);
    }

    @Test
    public void encodeNetmask() {
        print(EOL + "encodeNetmask()");
        verifyBitmappedFlags(V_1_0, ENCODED_FLAGS, DL_VLAN_PCP, DL_VLAN);
        checkEncodeNetMask(ENCODED_FLAGS, EXP_NW_SRC_MASK, EXP_NW_DST_MASK,
                           ENCODED);
        checkEncodeNetMask(0, EXP_ALL_WILD, EXP_EXACT, EXP_ENC_SRC_WILD);
        checkEncodeNetMask(0, EXP_EXACT, EXP_ALL_WILD, EXP_ENC_DST_WILD);
    }

    private static final IpAddress EXP_EXACT = ip("255.255.255.255");
    private static final int EXP_ENC_SRC_WILD = 0x2000;
    private static final int EXP_ENC_DST_WILD = 0x80000;

    private static final String[] BAD_MASKS = {
            "15.255.255.255", "255.255.0.1", "255.248.3.0", "253.0.0.0"
    };

    @Test
    public void badNetMasks() {
        print(EOL + "badNetMasks()");
        for (String s : BAD_MASKS) {
            try {
                int encoded = Wildcard.encodeNetmask(NW_SRC, ip(s), 0, V_1_0);
                print("Encoded: {}", hex(encoded));
                fail(AM_NOEX);
            } catch (IllegalArgumentException e) {
                print(FMT_EX, e);
            } catch (Exception e) {
                print(e);
                fail(AM_WREX);
            }
        }
    }

    private static final int NW_SRC_MULT = 1 << Wildcard.NW_SRC_SHIFT;
    private static final int NW_DST_MULT = 1 << Wildcard.NW_DST_SHIFT;
    private static final String FMT_NETMASK = "{} Netmask {} encodes as {}";

    @Test
    public void encodeSrcNetmask() {
        print(EOL + "encodeSrcNetmask()");
        for (int nWild = 0; nWild < 32; nWild++) {
            IpAddress ip = ip(MASKS[nWild]);
            int encoded = Wildcard.encodeNetmask(NW_SRC, ip, 0, V_1_0);
            print(FMT_NETMASK, NW_SRC, ip, hex(encoded));
            assertEquals(AM_NEQ, nWild * NW_SRC_MULT, encoded);
        }

        // wild should set just the top bit (equivalent to 32, shifted)
        int encoded = Wildcard.encodeNetmask(NW_SRC, EXP_ALL_WILD, 0, V_1_0);
        print(FMT_NETMASK, NW_SRC, EXP_ALL_WILD, hex(encoded));
        assertEquals(AM_NEQ, EXP_ENC_SRC_WILD, encoded);
    }

    @Test
    public void encodeDstNetmask() {
        print(EOL + "encodeDstNetmask()");
        for (int nWild = 0; nWild < 32; nWild++) {
            IpAddress ip = ip(MASKS[nWild]);
            int encoded = Wildcard.encodeNetmask(NW_DST, ip, 0, V_1_0);
            print(FMT_NETMASK, NW_DST, ip, hex(encoded));
            assertEquals(AM_NEQ, nWild * NW_DST_MULT, encoded);
        }

        // wild should set just the top bit (equivalent to 32, shifted)
        int encoded = Wildcard.encodeNetmask(NW_DST, EXP_ALL_WILD, 0, V_1_0);
        print(FMT_NETMASK, NW_DST, EXP_ALL_WILD, hex(encoded));
        assertEquals(AM_NEQ, EXP_ENC_DST_WILD, encoded);
    }

    @Test
    public void decodeV10Bitmap() {
        print(EOL + "decodeV10Bitmap()");
        Set<Wildcard> flags = Wildcard.decodeV10Bitmap(ENCODED, V_1_0);
        verifyFlags(flags, DL_VLAN, DL_VLAN_PCP);
    }

    @Test
    public void encodeV10Bitmap() {
        print(EOL + "encodeV10Bitmap()");
        Set<Wildcard> flags = new HashSet<Wildcard>();
        flags.add(DL_VLAN);
        flags.add(DL_VLAN_PCP);
        int encoded = Wildcard.encodeV10Bitmap(flags,
                EXP_NW_SRC_MASK, EXP_NW_DST_MASK, V_1_0);
        print("Flags {}, SRC {}, DST {} encodes to => {}", flags,
              EXP_NW_SRC_MASK, EXP_NW_DST_MASK, hex(encoded));
        assertEquals(AM_NEQ, ENCODED, encoded);
    }

    private static final IpAddress IP_255_255 = ip("255.255.0.0");

    @Test
    public void encodeV10NullNetMask() {
        print(EOL + "encodeV10NullNetMask()");

        // No src ip mask
        int encoded = Wildcard.encodeNetmask(Wildcard.NW_SRC, null, 0, V_1_0);
        print(hex(encoded));
        assertEquals(AM_NEQ, 0x0, encoded);

        // src ip mask
        encoded = Wildcard.encodeNetmask(Wildcard.NW_SRC, IP_255_255, 0, V_1_0);
        print(hex(encoded));
        assertEquals(AM_NEQ, 0x1000, encoded);

        // No dst ip mask
        encoded = Wildcard.encodeNetmask(Wildcard.NW_DST, null, 0, V_1_0);
        print(hex(encoded));
        assertEquals(AM_NEQ, 0x0, encoded);

        // dst ip mask
        encoded = Wildcard.encodeNetmask(Wildcard.NW_DST, IP_255_255, 0, V_1_0);
        print(hex(encoded));
        assertEquals(AM_NEQ, 0x40000, encoded);
    }
}
