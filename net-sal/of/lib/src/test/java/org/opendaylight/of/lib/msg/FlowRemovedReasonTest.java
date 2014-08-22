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
import static org.opendaylight.of.lib.msg.FlowRemovedReason.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link FlowRemovedReason}.
 *
 * @author Sudheer Duggisetty
 * @author Scott Simes
 * @author Simon Hunt
 */
public class FlowRemovedReasonTest
        extends AbstractCodeBasedEnumTest<FlowRemovedReason> {

    private static final AbstractCodeBasedCodecChecker<FlowRemovedReason> CHECKER =
            new AbstractCodeBasedCodecChecker<FlowRemovedReason>() {
        @Override
        protected Set<FlowRemovedReason> decodeFlags(int bitmap, ProtocolVersion pv) {
            return FlowRemovedReason.decodeFlags(bitmap, pv);
        }

        @Override
        protected int encodeFlags(Set<FlowRemovedReason> flags, ProtocolVersion pv) {
            return FlowRemovedReason.encodeFlags(flags, pv);
        }
    };

    private static final FlowRemovedReason[] EXPECT_VMM = {};

    @Override
    protected FlowRemovedReason decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return FlowRemovedReason.decode(code, pv);
    }

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
        for (FlowRemovedReason r: FlowRemovedReason.values())
            print(r);
        assertEquals(AM_UXCC, 4, FlowRemovedReason.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, IDLE_TIMEOUT);
        check(V_1_0, 1, HARD_TIMEOUT);
        check(V_1_0, 2, DELETE);
        check(V_1_0, 3, null, true);
        check(V_1_0, 4, null);
        check(V_1_0, 5, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, IDLE_TIMEOUT);
        check(V_1_1, 1, HARD_TIMEOUT);
        check(V_1_1, 2, DELETE);
        check(V_1_1, 3, GROUP_DELETE);
        check(V_1_1, 4, null);
        check(V_1_1, 5, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, IDLE_TIMEOUT);
        check(V_1_2, 1, HARD_TIMEOUT);
        check(V_1_2, 2, DELETE);
        check(V_1_2, 3, GROUP_DELETE);
        check(V_1_2, 4, null);
        check(V_1_2, 5, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, IDLE_TIMEOUT);
        check(V_1_3, 1, HARD_TIMEOUT);
        check(V_1_3, 2, DELETE);
        check(V_1_3, 3, GROUP_DELETE);
        check(V_1_3, 4, null);
        check(V_1_3, 5, null);
    }

    // === Code to Bitmap

    private static final int BIT_IDLE_TIMEOUT = 1;   // 1 << 0
    private static final int BIT_HARD_TIMEOUT = 1 << 1;
    private static final int BIT_DELETE = 1 << 2;
    private static final int BIT_GROUP_DELETE = 1 << 3;

    private static final int BITMAP_ALL_10 = 0x7;
    private static final int BITMAP_ALL = 0xf;

    private static final int JUNK = 0x900;
    private static final int BITMAP_DEL_GDEL_JUNK =
            BIT_DELETE | BIT_GROUP_DELETE | JUNK;

    @Test
    public void encodeFlags10() {
        print(EOL + "encodeFlags10()");
        CHECKER.checkEncode(V_1_0, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkEncode(V_1_0, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkEncode(V_1_0, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_0, BITMAP_ALL_10,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE);

        // ensure failure for flags that are not valid for this version
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, GROUP_DELETE);
    }

    @Test
    public void decodeFlags10() {
        print(EOL + "decodeFlags10()");
        CHECKER.checkDecode(V_1_0, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkDecode(V_1_0, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkDecode(V_1_0, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_0, BITMAP_ALL_10,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE);

        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_0, BITMAP_ALL, EXPECT_VMM);
    }

    @Test
    public void encodeFlags11() {
        print(EOL + "encodeFlags11()");
        CHECKER.checkEncode(V_1_1, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkEncode(V_1_1, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkEncode(V_1_1, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_1, BIT_GROUP_DELETE, GROUP_DELETE);
        CHECKER.checkEncode(V_1_1, BITMAP_ALL,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE, GROUP_DELETE);
    }

    @Test
    public void decodeFlags11() {
        print(EOL + "decodeFlags11()");
        CHECKER.checkDecode(V_1_1, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkDecode(V_1_1, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkDecode(V_1_1, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_1, BIT_GROUP_DELETE, GROUP_DELETE);
        CHECKER.checkDecode(V_1_1, BITMAP_ALL,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE, GROUP_DELETE);
    }

    @Test
    public void encodeFlags12() {
        print(EOL + "encodeFlags12()");
        CHECKER.checkEncode(V_1_2, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkEncode(V_1_2, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkEncode(V_1_2, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_2, BIT_GROUP_DELETE, GROUP_DELETE);
        CHECKER.checkEncode(V_1_2, BITMAP_ALL,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE, GROUP_DELETE);
    }

    @Test
    public void decodeFlags12() {
        print(EOL + "decodeFlags12()");
        CHECKER.checkDecode(V_1_2, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkDecode(V_1_2, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkDecode(V_1_2, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_2, BIT_GROUP_DELETE, GROUP_DELETE);
        CHECKER.checkDecode(V_1_2, BITMAP_ALL,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE, GROUP_DELETE);
    }

    @Test
    public void encodeFlags13() {
        print(EOL + "encodeFlags13()");
        CHECKER.checkEncode(V_1_3, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkEncode(V_1_3, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkEncode(V_1_3, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_3, BIT_GROUP_DELETE, GROUP_DELETE);
        CHECKER.checkEncode(V_1_3, BITMAP_ALL,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE, GROUP_DELETE);
    }

    @Test
    public void decodeFlags13() {
        print(EOL + "decodeFlags13()");
        CHECKER.checkDecode(V_1_3, BIT_IDLE_TIMEOUT, IDLE_TIMEOUT);
        CHECKER.checkDecode(V_1_3, BIT_HARD_TIMEOUT, HARD_TIMEOUT);
        CHECKER.checkDecode(V_1_3, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_3, BIT_GROUP_DELETE, GROUP_DELETE);
        CHECKER.checkDecode(V_1_3, BITMAP_ALL,
                IDLE_TIMEOUT, HARD_TIMEOUT, DELETE, GROUP_DELETE);
    }

    private static final String MSG_BAD_BITS = "Bad bits: 0x90c";

    @Test
    public void junkBitsThrowExceptionInStrictMode10() {
        print(EOL + "junkBitsThrowExceptionInStrictMode10()");
        try {
            FlowRemovedReason.decodeFlags(BITMAP_DEL_GDEL_JUNK, V_1_0);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void junkBitsIgnoredInNonStrictMode10() {
        print(EOL + "junkBitsIgnoredInNonStrictMode10()");
        MessageFactory.setStrictMessageParsing(false);
        Set<FlowRemovedReason> flags =
                FlowRemovedReason.decodeFlags(BITMAP_DEL_GDEL_JUNK, V_1_0);
        print(flags);
        verifyFlags(flags, DELETE);
        MessageFactory.setStrictMessageParsing(true);
    }

    @Test
    public void junkBitsThrowExceptionInStrictMode13() {
        print(EOL + "junkBitsThrowExceptionInStrictMode13()");
        try {
            FlowRemovedReason.decodeFlags(BITMAP_DEL_GDEL_JUNK, V_1_3);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertTrue(AM_WREXMSG, e.getMessage().endsWith(MSG_BAD_BITS));
        }
    }

    @Test
    public void junkBitsIgnoredInNonStrictMode13() {
        print(EOL + "junkBitsIgnoredInNonStrictMode13()");
        MessageFactory.setStrictMessageParsing(false);
        Set<FlowRemovedReason> flags =
                FlowRemovedReason.decodeFlags(BITMAP_DEL_GDEL_JUNK, V_1_3);
        print(flags);
        verifyFlags(flags, DELETE, GROUP_DELETE);
        MessageFactory.setStrictMessageParsing(true);
    }

}
