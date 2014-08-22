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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.PortReason.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link PortReason}.
 *
 * @author Radhika Hegde
 * @author Scott Simes
 * @author Simon Hunt
 */
public class PortReasonTest extends AbstractCodeBasedEnumTest<PortReason> {

    private static final AbstractCodeBasedCodecChecker<PortReason> CHECKER =
            new AbstractCodeBasedCodecChecker<PortReason>() {
        @Override
        protected Set<PortReason> decodeFlags(int bitmap, ProtocolVersion pv) {
            return PortReason.decodeFlags(bitmap, pv);
        }

        @Override
        protected int encodeFlags(Set<PortReason> flags, ProtocolVersion pv) {
            return PortReason.encodeFlags(flags, pv);
        }
    };

    @Override
    protected PortReason decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return PortReason.decode(code, pv);
    }

    private static final PortReason[] EXPECT_VMM = {};

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
        for (PortReason t: PortReason.values())
            print(t);
        assertEquals(AM_UXCC, 3, PortReason.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        check(V_1_0, -1, null);
        check(V_1_0, 0, ADD);
        check(V_1_0, 1, DELETE);
        check(V_1_0, 2, MODIFY);
        check(V_1_0, 3, null);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, ADD);
        check(V_1_1, 1, DELETE);
        check(V_1_1, 2, MODIFY);
        check(V_1_1, 3, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, ADD);
        check(V_1_2, 1, DELETE);
        check(V_1_2, 2, MODIFY);
        check(V_1_2, 3, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, ADD);
        check(V_1_3, 1, DELETE);
        check(V_1_3, 2, MODIFY);
        check(V_1_3, 3, null);
    }

    // === Code to Bitmap

    private static final int BIT_ADD = 1;  // 1 << 0
    private static final int BIT_DELETE = 1 << 1;
    private static final int BIT_MODIFY = 1 << 2;
    private static final int BITMAP_ALL_FLAGS = 0x7;
    private static final int BITMAP_ALL_FLAGS_WITH_JUNK = 0x37;

    @Test
    public void encodeFlags10() {
        print(EOL + "encodeFlags10()");
        CHECKER.checkEncode(V_1_0, BIT_ADD, ADD);
        CHECKER.checkEncode(V_1_0, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_0, BIT_MODIFY, MODIFY);
        CHECKER.checkEncode(V_1_0, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void decodeFlags10() {
        print(EOL + "decodeFlags10()");
        CHECKER.checkDecode(V_1_0, BIT_ADD, ADD);
        CHECKER.checkDecode(V_1_0, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_0, BIT_MODIFY, MODIFY);
        CHECKER.checkDecode(V_1_0, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void encodeFlags11() {
        print(EOL + "encodeFlags11()");
        CHECKER.checkEncode(V_1_1, BIT_ADD, ADD);
        CHECKER.checkEncode(V_1_1, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_1, BIT_MODIFY, MODIFY);
        CHECKER.checkEncode(V_1_1, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void decodeFlags11() {
        print(EOL + "decodeFlags11()");
        CHECKER.checkDecode(V_1_1, BIT_ADD, ADD);
        CHECKER.checkDecode(V_1_1, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_1, BIT_MODIFY, MODIFY);
        CHECKER.checkDecode(V_1_1, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void encodeFlags12() {
        print(EOL + "encodeFlags12()");
        CHECKER.checkEncode(V_1_2, BIT_ADD, ADD);
        CHECKER.checkEncode(V_1_2, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_2, BIT_MODIFY, MODIFY);
        CHECKER.checkEncode(V_1_2, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void decodeFlags12() {
        print(EOL + "decodeFlags12()");
        CHECKER.checkDecode(V_1_2, BIT_ADD, ADD);
        CHECKER.checkDecode(V_1_2, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_2, BIT_MODIFY, MODIFY);
        CHECKER.checkDecode(V_1_2, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void encodeFlags13() {
        print(EOL + "encodeFlags13()");
        CHECKER.checkEncode(V_1_3, BIT_ADD, ADD);
        CHECKER.checkEncode(V_1_3, BIT_DELETE, DELETE);
        CHECKER.checkEncode(V_1_3, BIT_MODIFY, MODIFY);
        CHECKER.checkEncode(V_1_3, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void decodeFlags13() {
        print(EOL + "decodeFlags13()");
        CHECKER.checkDecode(V_1_3, BIT_ADD, ADD);
        CHECKER.checkDecode(V_1_3, BIT_DELETE, DELETE);
        CHECKER.checkDecode(V_1_3, BIT_MODIFY, MODIFY);
        CHECKER.checkDecode(V_1_3, BITMAP_ALL_FLAGS, ADD, DELETE, MODIFY);
    }

    @Test
    public void junkBitsThrowExceptionWhenStrictParsing() {
        print(EOL + "junkBitsThrowExceptionWhenStrictParsing()");
        try {
            PortReason.decodeFlags(BITMAP_ALL_FLAGS_WITH_JUNK, V_1_3);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG, "V_1_3 (strict decode) Bad bits: 0x37",
                    e.getMessage());
        }
    }

    @Test
    public void junkBitsIgnoredWhenNonStrictParsing() {
        print(EOL + "junkBitsIgnoredWhenNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<PortReason> flags =
                PortReason.decodeFlags(BITMAP_ALL_FLAGS_WITH_JUNK, V_1_3);
        print(flags);
        verifyFlags(flags, ADD, DELETE, MODIFY);
        MessageFactory.setStrictMessageParsing(true);
    }

}
