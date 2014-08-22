/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.*;

import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.AbstractCodeBasedCodecChecker.EXPECT_EXCEPTION;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.PacketInReason.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link PacketInReason}.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class PacketInReasonTest extends AbstractCodeBasedEnumTest<PacketInReason> {

    private static final AbstractCodeBasedCodecChecker<PacketInReason> CHECKER =
            new AbstractCodeBasedCodecChecker<PacketInReason>() {
        @Override
        protected Set<PacketInReason> decodeFlags(int bitmap, ProtocolVersion pv) {
            return PacketInReason.decodeFlags(bitmap, pv);
        }

        @Override
        protected int encodeFlags(Set<PacketInReason> flags, ProtocolVersion pv) {
            return PacketInReason.encodeFlags(flags, pv);
        }
    };

    @Override
    protected PacketInReason decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return PacketInReason.decode(code, pv);
    }

    private static final PacketInReason[] EXPECT_VMM = {};

    @BeforeClass
    public static void classSetUp() {
        MessageFactory.setStrictMessageParsing(true);
    }

    @AfterClass
    public static void classTearDown() {
        MessageFactory.setStrictMessageParsing(false);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (PacketInReason r: PacketInReason.values())
            print(r);
        assertEquals(AM_UXCC, 3, PacketInReason.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, NO_MATCH);
        check(V_1_0, 1, ACTION);
        check(V_1_0, 2, null, true);
        check(V_1_0, 3, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, NO_MATCH);
        check(V_1_1, 1, ACTION);
        check(V_1_1, 2, null, true);
        check(V_1_1, 3, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, NO_MATCH);
        check(V_1_2, 1, ACTION);
        check(V_1_2, 2, INVALID_TTL);
        check(V_1_2, 3, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, NO_MATCH);
        check(V_1_3, 1, ACTION);
        check(V_1_3, 2, INVALID_TTL);
        check(V_1_3, 3, null);
    }

    // === Code to Bitmap

    private static final int BIT_NO_MATCH = 1; // 1 << 0
    private static final int BIT_ACTION = 1 << 1;
    private static final int BIT_INVALID_TTL = 1 << 2;

    private static final int BITMAP_ALL_10_11 = 0x3;
    private static final int BITMAP_ALL_12_13 = 0x7;

    private static final int JUNK = 0xbad0;
    private static final int BITMAP_ACT_TTL_JUNK =
            BIT_ACTION | BIT_INVALID_TTL | JUNK;
    private static final String MSG_BAD_BITS = "Bad bits: 0xbad6";

    @Test
    public void encodeFlags10() {
        print(EOL + "encodeFlags10()");
        CHECKER.checkEncode(V_1_0, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkEncode(V_1_0, BIT_ACTION, ACTION);
        CHECKER.checkEncode(V_1_0, BITMAP_ALL_10_11, NO_MATCH, ACTION);

        // ensure failure for flags that are not valid for this version
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, INVALID_TTL);
    }

    @Test
    public void decodeFlags10() {
        print(EOL + "decodeFlags10()");
        CHECKER.checkDecode(V_1_0, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkDecode(V_1_0, BIT_ACTION, ACTION);
        CHECKER.checkDecode(V_1_0, BITMAP_ALL_10_11, NO_MATCH, ACTION);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_0, BIT_INVALID_TTL, EXPECT_VMM);
    }

    @Test
    public void encodeFlags11() {
        print(EOL + "encodeFlags11()");
        CHECKER.checkEncode(V_1_1, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkEncode(V_1_1, BIT_ACTION, ACTION);
        CHECKER.checkEncode(V_1_1, BITMAP_ALL_10_11, NO_MATCH, ACTION);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkEncode(V_1_1, EXPECT_EXCEPTION, INVALID_TTL);
    }

    @Test
    public void decodeFlags11() {
        print(EOL + "decodeFlags11()");
        CHECKER.checkDecode(V_1_1, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkDecode(V_1_1, BIT_ACTION, ACTION);
        CHECKER.checkDecode(V_1_1, BITMAP_ALL_10_11, NO_MATCH, ACTION);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_1, BIT_INVALID_TTL, EXPECT_VMM);
    }

    @Test
    public void encodeFlags12() {
        print(EOL + "encodeFlags12()");
        CHECKER.checkEncode(V_1_2, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkEncode(V_1_2, BIT_ACTION, ACTION);
        CHECKER.checkEncode(V_1_2, BIT_INVALID_TTL, INVALID_TTL);
        CHECKER.checkEncode(V_1_2, BITMAP_ALL_12_13,
                NO_MATCH, ACTION, INVALID_TTL);
    }

    @Test
    public void decodeFlags12() {
        print(EOL + "decodeFlags12()");
        CHECKER.checkDecode(V_1_2, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkDecode(V_1_2, BIT_ACTION, ACTION);
        CHECKER.checkDecode(V_1_2, BIT_INVALID_TTL, INVALID_TTL);
        CHECKER.checkDecode(V_1_2, BITMAP_ALL_12_13,
                NO_MATCH, ACTION, INVALID_TTL);
    }

    @Test
    public void encodeFlags13() {
        print(EOL + "encodeFlags13()");
        CHECKER.checkEncode(V_1_3, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkEncode(V_1_3, BIT_ACTION, ACTION);
        CHECKER.checkEncode(V_1_3, BIT_INVALID_TTL, INVALID_TTL);
        CHECKER.checkEncode(V_1_3, BITMAP_ALL_12_13,
                NO_MATCH, ACTION, INVALID_TTL);
    }

    @Test
    public void decodeFlags13() {
        print(EOL + "decodeFlags13()");
        CHECKER.checkDecode(V_1_3, BIT_NO_MATCH, NO_MATCH);
        CHECKER.checkDecode(V_1_3, BIT_ACTION, ACTION);
        CHECKER.checkDecode(V_1_3, BIT_INVALID_TTL, INVALID_TTL);
        CHECKER.checkDecode(V_1_3, BITMAP_ALL_12_13,
                NO_MATCH, ACTION, INVALID_TTL);
    }

    @Test
    public void junkBitsThrowExceptionWithStrictParsing10() {
        print(EOL + "junkBitsThrowExceptionWithStrictParsing10()");
        try {
            PacketInReason.decodeFlags(BITMAP_ACT_TTL_JUNK, V_1_0);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void junkBitsIgnoredWithNonStrictParsing10() {
        print(EOL + "junkBitsIgnoredWithNonStrictParsing10()");
        MessageFactory.setStrictMessageParsing(false);
        Set<PacketInReason> flags =
                PacketInReason.decodeFlags(BITMAP_ACT_TTL_JUNK, V_1_0);
        print(flags);
        verifyFlags(flags, ACTION);
        MessageFactory.setStrictMessageParsing(true);
    }

    @Test
    public void junkBitsThrowExceptionWithStrictParsing13() {
        print(EOL + "junkBitsThrowExceptionWithStrictParsing13()");
        try {
            PacketInReason.decodeFlags(BITMAP_ACT_TTL_JUNK, V_1_3);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void junkBitsIgnoredWithNonStrictParsing13() {
        print(EOL + "junkBitsIgnoredWithNonStrictParsing13()");
        MessageFactory.setStrictMessageParsing(false);
        Set<PacketInReason> flags =
                PacketInReason.decodeFlags(BITMAP_ACT_TTL_JUNK, V_1_3);
        print(flags);
        verifyFlags(flags, ACTION, INVALID_TTL);
        MessageFactory.setStrictMessageParsing(true);
    }
}
