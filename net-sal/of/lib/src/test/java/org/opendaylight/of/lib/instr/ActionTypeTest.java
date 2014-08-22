/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.MessageFactory;

import java.util.Set;

import static junit.framework.Assert.*;
import static org.opendaylight.of.lib.AbstractCodeBasedCodecChecker.EXPECT_EXCEPTION;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.instr.ActionType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link ActionType}.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class ActionTypeTest extends AbstractCodeBasedEnumTest<ActionType> {

    private static final AbstractCodeBasedCodecChecker<ActionType> CHECKER =
            new AbstractCodeBasedCodecChecker<ActionType>() {
        @Override
        protected Set<ActionType> decodeFlags(int bitmap, ProtocolVersion pv) {
            return ActionType.decodeFlags(bitmap, pv);
        }

        @Override
        protected int encodeFlags(Set<ActionType> flags, ProtocolVersion pv) {
            return ActionType.encodeFlags(flags, pv);
        }
    };

    @Override
    protected ActionType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return ActionType.decode(code, pv);
    }

    private static final ActionType[] EXPECT_VMM = {};

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
        for (ActionType t: ActionType.values())
            print(t);
        assertEquals(AM_UXCC, 17, ActionType.values().length);
    }

    private static final int[] UNKNOWN = { -1, 28, 200, 0xfffe, 0x10000 };

    @Test
    public void unknownCodes() {
        print(EOL + "unknownCodes()");
        for (ProtocolVersion pv: PV_23)
            for (int code: UNKNOWN) {
                try {
                    ActionType t = ActionType.decode(code, pv);
                    print(FMT_PV_CODE_ENUM, pv, code, t);
                    fail(AM_NOEX);
                } catch (DecodeException e) {
                    print(FMT_EX, e);
                }
            }
    }

    private void verifyCode(int code, ProtocolVersion pv, ActionType expType) {
        try {
            ActionType decoded = ActionType.decode(code, pv);
            print(FMT_PV_CODE_ENUM, pv, code, decoded);
            if (expType == null)
                fail(AM_NOEX);
            assertEquals(AM_NEQ, expType, decoded);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
            if (expType != null)
                fail(AM_UNEX_MISMATCH);
        } catch (DecodeException e) {
            print(e);
            fail(AM_UNEX);
        }
    }

    private void verifyVmm(int code, ProtocolVersion pv) {
        try {
            ActionType.decode(code, pv);
            fail(AM_NOEX);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }
    private void verifyCodeNull(int code, ProtocolVersion pv, boolean expDE) {
        // no exception is expected to be thrown - just a null return value
        try {
            ActionType decoded = ActionType.decode(code, pv);
            print(FMT_PV_CODE_ENUM, pv, code, decoded);
            assertNull(AM_HUH, decoded);
        } catch (DecodeException e) {
            if (expDE)
                print(FMT_EX, e);
            else {
                print(e);
                fail(AM_UNEX);
            }
        }
    }

    @Test
    public void codesV0() {
        print(EOL + "codesV0");
        verifyVmm(0, V_1_0);
        verifyVmm(1, V_1_0);
        verifyVmm(2, V_1_0);
        verifyVmm(3, V_1_0);
        verifyVmm(4, V_1_0);
        verifyVmm(5, V_1_0);
        verifyVmm(6, V_1_0);
        verifyVmm(7, V_1_0);
        verifyVmm(8, V_1_0);
        verifyVmm(9, V_1_0);
        verifyVmm(10, V_1_0);
        verifyVmm(11, V_1_0);
        verifyVmm(12, V_1_0);
        verifyVmm(15, V_1_0);
        verifyVmm(16, V_1_0);
        verifyVmm(17, V_1_0);
        verifyVmm(18, V_1_0);
        verifyVmm(19, V_1_0);
        verifyVmm(20, V_1_0);
        verifyVmm(21, V_1_0);
        verifyVmm(22, V_1_0);
        verifyVmm(23, V_1_0);
        verifyVmm(24, V_1_0);
        verifyVmm(25, V_1_0);
        verifyVmm(26, V_1_0);
        verifyVmm(27, V_1_0);
        verifyVmm(0xffff, V_1_0);
    }

    @Test
    public void codesV1() {
        print(EOL + "codesV1");
        verifyVmm(0, V_1_0);
        verifyVmm(1, V_1_0);
        verifyVmm(2, V_1_0);
        verifyVmm(3, V_1_0);
        verifyVmm(4, V_1_0);
        verifyVmm(5, V_1_0);
        verifyVmm(6, V_1_0);
        verifyVmm(7, V_1_0);
        verifyVmm(8, V_1_0);
        verifyVmm(9, V_1_0);
        verifyVmm(10, V_1_0);
        verifyVmm(11, V_1_0);
        verifyVmm(12, V_1_0);
        verifyVmm(15, V_1_0);
        verifyVmm(16, V_1_0);
        verifyVmm(17, V_1_0);
        verifyVmm(18, V_1_0);
        verifyVmm(19, V_1_0);
        verifyVmm(20, V_1_0);
        verifyVmm(21, V_1_0);
        verifyVmm(22, V_1_0);
        verifyVmm(23, V_1_0);
        verifyVmm(24, V_1_0);
        verifyVmm(25, V_1_0);
        verifyVmm(26, V_1_0);
        verifyVmm(27, V_1_0);
        verifyVmm(0xffff, V_1_0);
    }

    @Test
    public void codesV2() {
        print(EOL + "codesV2");
        verifyCode(0, V_1_2, OUTPUT);
        verifyCodeNull(1, V_1_2, true);
        verifyCodeNull(2, V_1_2, true);
        verifyCodeNull(3, V_1_2, true);
        verifyCodeNull(4, V_1_2, true);
        verifyCodeNull(5, V_1_2, true);
        verifyCodeNull(6, V_1_2, true);
        verifyCodeNull(7, V_1_2, true);
        verifyCodeNull(8, V_1_2, true);
        verifyCodeNull(9, V_1_2, true);
        verifyCodeNull(10, V_1_2, true);
        verifyCode(11, V_1_2, COPY_TTL_OUT);
        verifyCode(12, V_1_2, COPY_TTL_IN);
        verifyCode(15, V_1_2, SET_MPLS_TTL);
        verifyCode(16, V_1_2, DEC_MPLS_TTL);
        verifyCode(17, V_1_2, PUSH_VLAN);
        verifyCode(18, V_1_2, POP_VLAN);
        verifyCode(19, V_1_2, PUSH_MPLS);
        verifyCode(20, V_1_2, POP_MPLS);
        verifyCode(21, V_1_2, SET_QUEUE);
        verifyCode(22, V_1_2, GROUP);
        verifyCode(23, V_1_2, SET_NW_TTL);
        verifyCode(24, V_1_2, DEC_NW_TTL);
        verifyCode(25, V_1_2, SET_FIELD);
        verifyCodeNull(26, V_1_2, true);
        verifyCodeNull(27, V_1_2, true);
        verifyCode(0xffff, V_1_2, EXPERIMENTER);
    }

    @Test
    public void codesV3() {
        print(EOL + "codesV3");
        verifyCode(0, V_1_3, OUTPUT);
        verifyCodeNull(1, V_1_3, true);
        verifyCodeNull(2, V_1_3, true);
        verifyCodeNull(3, V_1_3, true);
        verifyCodeNull(4, V_1_3, true);
        verifyCodeNull(5, V_1_3, true);
        verifyCodeNull(6, V_1_3, true);
        verifyCodeNull(7, V_1_3, true);
        verifyCodeNull(8, V_1_3, true);
        verifyCodeNull(9, V_1_3, true);
        verifyCodeNull(10, V_1_3, true);
        verifyCode(11, V_1_3, COPY_TTL_OUT);
        verifyCode(12, V_1_3, COPY_TTL_IN);
        verifyCode(15, V_1_3, SET_MPLS_TTL);
        verifyCode(16, V_1_3, DEC_MPLS_TTL);
        verifyCode(17, V_1_3, PUSH_VLAN);
        verifyCode(18, V_1_3, POP_VLAN);
        verifyCode(19, V_1_3, PUSH_MPLS);
        verifyCode(20, V_1_3, POP_MPLS);
        verifyCode(21, V_1_3, SET_QUEUE);
        verifyCode(22, V_1_3, GROUP);
        verifyCode(23, V_1_3, SET_NW_TTL);
        verifyCode(24, V_1_3, DEC_NW_TTL);
        verifyCode(25, V_1_3, SET_FIELD);
        verifyCode(26, V_1_3, PUSH_PBB);
        verifyCode(27, V_1_3, POP_PBB);
        verifyCode(0xffff, V_1_3, EXPERIMENTER);
    }

    // === Code to Bitmap

    private static final int BIT_OUTPUT = 1; // 1 << 0

    private static final int BIT_COPY_TTL_OUT = 1 << 11;
    private static final int BIT_COPY_TTL_IN = 1 << 12;
    private static final int BIT_SET_MPLS_TTL = 1 << 15;
    private static final int BIT_DEC_MPLS_TTL = 1 << 16;
    private static final int BIT_PUSH_VLAN = 1 << 17;
    private static final int BIT_POP_VLAN = 1 << 18;
    private static final int BIT_PUSH_MPLS = 1 << 19;
    private static final int BIT_POP_MPLS = 1 << 20;
    private static final int BIT_SET_QUEUE = 1 << 21;
    private static final int BIT_GROUP = 1 << 22;
    private static final int BIT_SET_NW_TTL = 1 << 23;
    private static final int BIT_DEC_NW_TTL = 1 << 24;

    private static final int BIT_SET_FIELD = 1 << 25;

    private static final int BIT_PUSH_PBB = 1 << 26;
    private static final int BIT_POP_PBB = 1 << 27;

    private static final int BITMAP_10_ALL = 0x00000001;
    private static final int BITMAP_11_ALL = 0x01ff9801;
    private static final int BITMAP_12_ALL = 0x03ff9801;
    private static final int BITMAP_13_ALL = 0x0fff9801;

    private static final int BITMAP_JUNK = 0xe0;

    private static final int BITMAP_OUT_GRP_SFD_PBB_JUNK =
            BIT_OUTPUT | BIT_GROUP | BIT_SET_FIELD | BIT_POP_PBB | BITMAP_JUNK;

    private static final String MSG_BAD_BITS = "Bad bits: 0xa4000e1";

    @Test
    public void encodeFlags10() {
        print(EOL + "encodeFlags10()");
        CHECKER.checkEncode(V_1_0, BIT_OUTPUT, OUTPUT);
        CHECKER.checkEncode(V_1_0, BITMAP_10_ALL, OUTPUT);

        // ensure failure for flags that are not valid for this version
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, COPY_TTL_OUT);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, COPY_TTL_IN);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, SET_MPLS_TTL);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, DEC_MPLS_TTL);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, PUSH_VLAN);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, POP_VLAN);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, PUSH_MPLS);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, POP_MPLS);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, SET_QUEUE);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, GROUP);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, SET_NW_TTL);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, DEC_NW_TTL);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, SET_FIELD);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, PUSH_PBB);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, POP_PBB);
    }

    @Test
    public void decodeFlags10() {
        print(EOL + "decodeFlags10()");
        CHECKER.checkDecode(V_1_0, BIT_OUTPUT, OUTPUT);
        CHECKER.checkDecode(V_1_0, BITMAP_10_ALL, OUTPUT);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_0, BIT_COPY_TTL_OUT, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_COPY_TTL_IN, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_SET_MPLS_TTL, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_DEC_MPLS_TTL, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_PUSH_VLAN, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_POP_VLAN, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_PUSH_MPLS, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_POP_MPLS, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_SET_QUEUE, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_GROUP, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_SET_NW_TTL, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_DEC_NW_TTL, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_SET_FIELD, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_PUSH_PBB, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_POP_PBB, EXPECT_VMM);
    }

    @Test
    public void encodeFlags11() {
        print(EOL + "encodeFlags11()");
        CHECKER.checkEncode(V_1_1, BIT_OUTPUT, OUTPUT);
        CHECKER.checkEncode(V_1_1, BIT_COPY_TTL_OUT, COPY_TTL_OUT);
        CHECKER.checkEncode(V_1_1, BIT_COPY_TTL_IN, COPY_TTL_IN);
        CHECKER.checkEncode(V_1_1, BIT_SET_MPLS_TTL, SET_MPLS_TTL);
        CHECKER.checkEncode(V_1_1, BIT_DEC_MPLS_TTL, DEC_MPLS_TTL);
        CHECKER.checkEncode(V_1_1, BIT_PUSH_VLAN, PUSH_VLAN);
        CHECKER.checkEncode(V_1_1, BIT_POP_VLAN, POP_VLAN);
        CHECKER.checkEncode(V_1_1, BIT_PUSH_MPLS, PUSH_MPLS);
        CHECKER.checkEncode(V_1_1, BIT_POP_MPLS, POP_MPLS);
        CHECKER.checkEncode(V_1_1, BIT_SET_QUEUE, SET_QUEUE);
        CHECKER.checkEncode(V_1_1, BIT_GROUP, GROUP);
        CHECKER.checkEncode(V_1_1, BIT_SET_NW_TTL, SET_NW_TTL);
        CHECKER.checkEncode(V_1_1, BIT_DEC_NW_TTL, DEC_NW_TTL);
        CHECKER.checkEncode(V_1_1, BITMAP_11_ALL,
                OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, SET_MPLS_TTL, DEC_MPLS_TTL,
                PUSH_VLAN, POP_VLAN, PUSH_MPLS, POP_MPLS, SET_QUEUE, GROUP,
                SET_NW_TTL, DEC_NW_TTL);

        // ensure failure for flags that are not valid for this version
        CHECKER.checkEncode(V_1_1, EXPECT_EXCEPTION, SET_FIELD);
        CHECKER.checkEncode(V_1_1, EXPECT_EXCEPTION, PUSH_PBB);
        CHECKER.checkEncode(V_1_1, EXPECT_EXCEPTION, POP_PBB);
    }

    @Test
    public void decodeFlags11() {
        print(EOL + "decodeFlags11()");
        CHECKER.checkDecode(V_1_1, BIT_OUTPUT, OUTPUT);
        CHECKER.checkDecode(V_1_1, BIT_COPY_TTL_OUT, COPY_TTL_OUT);
        CHECKER.checkDecode(V_1_1, BIT_COPY_TTL_IN, COPY_TTL_IN);
        CHECKER.checkDecode(V_1_1, BIT_SET_MPLS_TTL, SET_MPLS_TTL);
        CHECKER.checkDecode(V_1_1, BIT_DEC_MPLS_TTL, DEC_MPLS_TTL);
        CHECKER.checkDecode(V_1_1, BIT_PUSH_VLAN, PUSH_VLAN);
        CHECKER.checkDecode(V_1_1, BIT_POP_VLAN, POP_VLAN);
        CHECKER.checkDecode(V_1_1, BIT_PUSH_MPLS, PUSH_MPLS);
        CHECKER.checkDecode(V_1_1, BIT_POP_MPLS, POP_MPLS);
        CHECKER.checkDecode(V_1_1, BIT_SET_QUEUE, SET_QUEUE);
        CHECKER.checkDecode(V_1_1, BIT_GROUP, GROUP);
        CHECKER.checkDecode(V_1_1, BIT_SET_NW_TTL, SET_NW_TTL);
        CHECKER.checkDecode(V_1_1, BIT_DEC_NW_TTL, DEC_NW_TTL);
        CHECKER.checkDecode(V_1_1, BITMAP_11_ALL,
                OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, SET_MPLS_TTL, DEC_MPLS_TTL,
                PUSH_VLAN, POP_VLAN, PUSH_MPLS, POP_MPLS, SET_QUEUE, GROUP,
                SET_NW_TTL, DEC_NW_TTL);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_1, BIT_SET_FIELD, EXPECT_VMM);
        CHECKER.checkDecode(V_1_1, BIT_PUSH_PBB, EXPECT_VMM);
        CHECKER.checkDecode(V_1_1, BIT_POP_PBB, EXPECT_VMM);
    }

    @Test
    public void encodeFlags12() {
        print(EOL + "encodeFlags12()");
        CHECKER.checkEncode(V_1_2, BIT_OUTPUT, OUTPUT);
        CHECKER.checkEncode(V_1_2, BIT_COPY_TTL_OUT, COPY_TTL_OUT);
        CHECKER.checkEncode(V_1_2, BIT_COPY_TTL_IN, COPY_TTL_IN);
        CHECKER.checkEncode(V_1_2, BIT_SET_MPLS_TTL, SET_MPLS_TTL);
        CHECKER.checkEncode(V_1_2, BIT_DEC_MPLS_TTL, DEC_MPLS_TTL);
        CHECKER.checkEncode(V_1_2, BIT_PUSH_VLAN, PUSH_VLAN);
        CHECKER.checkEncode(V_1_2, BIT_POP_VLAN, POP_VLAN);
        CHECKER.checkEncode(V_1_2, BIT_PUSH_MPLS, PUSH_MPLS);
        CHECKER.checkEncode(V_1_2, BIT_POP_MPLS, POP_MPLS);
        CHECKER.checkEncode(V_1_2, BIT_SET_QUEUE, SET_QUEUE);
        CHECKER.checkEncode(V_1_2, BIT_GROUP, GROUP);
        CHECKER.checkEncode(V_1_2, BIT_SET_NW_TTL, SET_NW_TTL);
        CHECKER.checkEncode(V_1_2, BIT_DEC_NW_TTL, DEC_NW_TTL);
        CHECKER.checkEncode(V_1_2, BIT_SET_FIELD, SET_FIELD);
        CHECKER.checkEncode(V_1_2, BITMAP_12_ALL,
                OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, SET_MPLS_TTL, DEC_MPLS_TTL,
                PUSH_VLAN, POP_VLAN, PUSH_MPLS, POP_MPLS, SET_QUEUE, GROUP,
                SET_NW_TTL, DEC_NW_TTL, SET_FIELD);

        // ensure failure for flags that are not valid for this version
        CHECKER.checkEncode(V_1_2, EXPECT_EXCEPTION, PUSH_PBB);
        CHECKER.checkEncode(V_1_2, EXPECT_EXCEPTION, POP_PBB);
    }

    @Test
    public void decodeFlags12() {
        print(EOL + "decodeFlags12()");
        CHECKER.checkDecode(V_1_2, BIT_OUTPUT, OUTPUT);
        CHECKER.checkDecode(V_1_2, BIT_COPY_TTL_OUT, COPY_TTL_OUT);
        CHECKER.checkDecode(V_1_2, BIT_COPY_TTL_IN, COPY_TTL_IN);
        CHECKER.checkDecode(V_1_2, BIT_SET_MPLS_TTL, SET_MPLS_TTL);
        CHECKER.checkDecode(V_1_2, BIT_DEC_MPLS_TTL, DEC_MPLS_TTL);
        CHECKER.checkDecode(V_1_2, BIT_PUSH_VLAN, PUSH_VLAN);
        CHECKER.checkDecode(V_1_2, BIT_POP_VLAN, POP_VLAN);
        CHECKER.checkDecode(V_1_2, BIT_PUSH_MPLS, PUSH_MPLS);
        CHECKER.checkDecode(V_1_2, BIT_POP_MPLS, POP_MPLS);
        CHECKER.checkDecode(V_1_2, BIT_SET_QUEUE, SET_QUEUE);
        CHECKER.checkDecode(V_1_2, BIT_GROUP, GROUP);
        CHECKER.checkDecode(V_1_2, BIT_SET_NW_TTL, SET_NW_TTL);
        CHECKER.checkDecode(V_1_2, BIT_DEC_NW_TTL, DEC_NW_TTL);
        CHECKER.checkDecode(V_1_2, BIT_SET_FIELD, SET_FIELD);
        CHECKER.checkDecode(V_1_2, BITMAP_12_ALL,
                OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, SET_MPLS_TTL, DEC_MPLS_TTL,
                PUSH_VLAN, POP_VLAN, PUSH_MPLS, POP_MPLS, SET_QUEUE, GROUP,
                SET_NW_TTL, DEC_NW_TTL, SET_FIELD);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_2, BIT_PUSH_PBB, EXPECT_VMM);
        CHECKER.checkDecode(V_1_2, BIT_POP_PBB, EXPECT_VMM);
    }

    @Test
    public void encodeFlags13() {
        print(EOL + "encodeFlags13()");
        CHECKER.checkEncode(V_1_3, BIT_OUTPUT, OUTPUT);
        CHECKER.checkEncode(V_1_3, BIT_COPY_TTL_OUT, COPY_TTL_OUT);
        CHECKER.checkEncode(V_1_3, BIT_COPY_TTL_IN, COPY_TTL_IN);
        CHECKER.checkEncode(V_1_3, BIT_SET_MPLS_TTL, SET_MPLS_TTL);
        CHECKER.checkEncode(V_1_3, BIT_DEC_MPLS_TTL, DEC_MPLS_TTL);
        CHECKER.checkEncode(V_1_3, BIT_PUSH_VLAN, PUSH_VLAN);
        CHECKER.checkEncode(V_1_3, BIT_POP_VLAN, POP_VLAN);
        CHECKER.checkEncode(V_1_3, BIT_PUSH_MPLS, PUSH_MPLS);
        CHECKER.checkEncode(V_1_3, BIT_POP_MPLS, POP_MPLS);
        CHECKER.checkEncode(V_1_3, BIT_SET_QUEUE, SET_QUEUE);
        CHECKER.checkEncode(V_1_3, BIT_GROUP, GROUP);
        CHECKER.checkEncode(V_1_3, BIT_SET_NW_TTL, SET_NW_TTL);
        CHECKER.checkEncode(V_1_3, BIT_DEC_NW_TTL, DEC_NW_TTL);
        CHECKER.checkEncode(V_1_3, BIT_SET_FIELD, SET_FIELD);
        CHECKER.checkEncode(V_1_3, BIT_PUSH_PBB, PUSH_PBB);
        CHECKER.checkEncode(V_1_3, BIT_POP_PBB, POP_PBB);
        CHECKER.checkEncode(V_1_3, BITMAP_13_ALL,
                OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, SET_MPLS_TTL, DEC_MPLS_TTL,
                PUSH_VLAN, POP_VLAN, PUSH_MPLS, POP_MPLS, SET_QUEUE, GROUP,
                SET_NW_TTL, DEC_NW_TTL, SET_FIELD, POP_PBB, PUSH_PBB);
    }

    @Test
    public void decodeFlags13() {
        print(EOL + "decodeFlags13()");
        CHECKER.checkDecode(V_1_3, BIT_OUTPUT, OUTPUT);
        CHECKER.checkDecode(V_1_3, BIT_COPY_TTL_OUT, COPY_TTL_OUT);
        CHECKER.checkDecode(V_1_3, BIT_COPY_TTL_IN, COPY_TTL_IN);
        CHECKER.checkDecode(V_1_3, BIT_SET_MPLS_TTL, SET_MPLS_TTL);
        CHECKER.checkDecode(V_1_3, BIT_DEC_MPLS_TTL, DEC_MPLS_TTL);
        CHECKER.checkDecode(V_1_3, BIT_PUSH_VLAN, PUSH_VLAN);
        CHECKER.checkDecode(V_1_3, BIT_POP_VLAN, POP_VLAN);
        CHECKER.checkDecode(V_1_3, BIT_PUSH_MPLS, PUSH_MPLS);
        CHECKER.checkDecode(V_1_3, BIT_POP_MPLS, POP_MPLS);
        CHECKER.checkDecode(V_1_3, BIT_SET_QUEUE, SET_QUEUE);
        CHECKER.checkDecode(V_1_3, BIT_GROUP, GROUP);
        CHECKER.checkDecode(V_1_3, BIT_SET_NW_TTL, SET_NW_TTL);
        CHECKER.checkDecode(V_1_3, BIT_DEC_NW_TTL, DEC_NW_TTL);
        CHECKER.checkDecode(V_1_3, BIT_SET_FIELD, SET_FIELD);
        CHECKER.checkDecode(V_1_3, BIT_PUSH_PBB, PUSH_PBB);
        CHECKER.checkDecode(V_1_3, BIT_POP_PBB, POP_PBB);
        CHECKER.checkDecode(V_1_3, BITMAP_13_ALL,
                OUTPUT, COPY_TTL_OUT, COPY_TTL_IN, SET_MPLS_TTL, DEC_MPLS_TTL,
                PUSH_VLAN, POP_VLAN, PUSH_MPLS, POP_MPLS, SET_QUEUE, GROUP,
                SET_NW_TTL, DEC_NW_TTL, SET_FIELD, PUSH_PBB, POP_PBB);
    }

    @Test
    public void badBits10WithStrictParsing() {
        print(EOL + "badBits10WithStrictParsing()");
        try {
            ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_0);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void badBits10WithNonStrictParsing() {
        print(EOL + "badBits10WithNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<ActionType> flags =
                ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_0);
        print(flags);
        verifyFlags(flags, OUTPUT);
        MessageFactory.setStrictMessageParsing(true);
    }

    @Test
    public void badBits11WithStrictParsing() {
        print(EOL + "badBits11WithStrictParsing()");
        try {
            ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_1);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void badBits11WithNonStrictParsing() {
        print(EOL + "badBits11WithNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<ActionType> flags =
                ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_1);
        print(flags);
        verifyFlags(flags, OUTPUT, GROUP);
        MessageFactory.setStrictMessageParsing(true);
    }

    @Test
    public void badBits12WithStrictParsing() {
        print(EOL + "badBits12WithStrictParsing()");
        try {
            ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_2);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void badBits12WithNonStrictParsing() {
        print(EOL + "badBits12WithNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<ActionType> flags =
                ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_2);
        print(flags);
        verifyFlags(flags, OUTPUT, GROUP, SET_FIELD);
        MessageFactory.setStrictMessageParsing(true);
    }

    @Test
    public void badBits13WithStrictParsing() {
        print(EOL + "badBits13WithStrictParsing()");
        try {
            ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_3);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void badBits13WithNonStrictParsing() {
        print(EOL + "badBits13WithNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<ActionType> flags =
                ActionType.decodeFlags(BITMAP_OUT_GRP_SFD_PBB_JUNK, V_1_3);
        print(flags);
        verifyFlags(flags, OUTPUT, GROUP, SET_FIELD, POP_PBB);
        MessageFactory.setStrictMessageParsing(true);
    }

}
