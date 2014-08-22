/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
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
import static org.opendaylight.of.lib.AbstractCodeBasedCodecChecker.EXPECT_EXCEPTION;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.GroupType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link GroupType}.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class GroupTypeTest extends AbstractCodeBasedEnumTest<GroupType> {

    private static final AbstractCodeBasedCodecChecker<GroupType> CHECKER =
            new AbstractCodeBasedCodecChecker<GroupType>() {
        @Override
        protected Set<GroupType> decodeFlags(int bitmap, ProtocolVersion pv) {
            return GroupType.decodeFlags(bitmap, pv);
        }

        @Override
        protected int encodeFlags(Set<GroupType> flags, ProtocolVersion pv) {
            return GroupType.encodeFlags(flags, pv);
        }
    };

    @Override
    protected GroupType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return GroupType.decode(code, pv);
    }

    private static final GroupType[] EXPECT_VMM = {};

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
        for (GroupType t: GroupType.values())
            print(t);
        assertEquals(AM_UXCC, 4, GroupType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        check(V_1_1, -1, null);
        check(V_1_1, 0, ALL);
        check(V_1_1, 1, SELECT);
        check(V_1_1, 2, INDIRECT);
        check(V_1_1, 3, FF);
        check(V_1_1, 4, null);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        check(V_1_2, -1, null);
        check(V_1_2, 0, ALL);
        check(V_1_2, 1, SELECT);
        check(V_1_2, 2, INDIRECT);
        check(V_1_2, 3, FF);
        check(V_1_2, 4, null);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, ALL);
        check(V_1_3, 1, SELECT);
        check(V_1_3, 2, INDIRECT);
        check(V_1_3, 3, FF);
        check(V_1_3, 4, null);
    }

    // === Code to Bitmap

    private static final int BIT_ALL = 1; // 1 << 0
    private static final int BIT_SELECT = 1 << 1;
    private static final int BIT_INDIRECT = 1 << 2;
    private static final int BIT_FF = 1 << 3;

    private static final int BITMAP_ALL_GROUP_TYPES = 0xf;
    private static final GroupType[] ALL_GROUP_TYPES = GroupType.values();

    private static final int BITMAP_SELECT_FF_WITH_JUNK = 0xfa;

    @Test
    public void encodeFlags10() {
        print(EOL + "encodeFlags10()");
        // ensure failure for flags that are not valid for this version
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, ALL);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, SELECT);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, INDIRECT);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, FF);
    }

    @Test
    public void decodeFlags10() {
        print(EOL + "decodeFlags10()");
        // ensure failure for bits that are not valid for this version
        CHECKER.checkDecode(V_1_0, BIT_ALL, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_SELECT, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_INDIRECT, EXPECT_VMM);
        CHECKER.checkDecode(V_1_0, BIT_FF, EXPECT_VMM);
    }

    @Test
    public void encodeFlags11() {
        print(EOL + "encodeFlags11()");
        CHECKER.checkEncode(V_1_1, BIT_ALL, ALL);
        CHECKER.checkEncode(V_1_1, BIT_SELECT, SELECT);
        CHECKER.checkEncode(V_1_1, BIT_INDIRECT, INDIRECT);
        CHECKER.checkEncode(V_1_1, BIT_FF, FF);
        CHECKER.checkEncode(V_1_1, BITMAP_ALL_GROUP_TYPES,
                ALL_GROUP_TYPES);
    }

    @Test
    public void decodeFlags11() {
        print(EOL + "decodeFlags11()");
        CHECKER.checkDecode(V_1_1, BIT_ALL, ALL);
        CHECKER.checkDecode(V_1_1, BIT_SELECT, SELECT);
        CHECKER.checkDecode(V_1_1, BIT_INDIRECT, INDIRECT);
        CHECKER.checkDecode(V_1_1, BIT_FF, FF);
        CHECKER.checkDecode(V_1_1, BITMAP_ALL_GROUP_TYPES,
                ALL_GROUP_TYPES);
    }

    @Test
    public void encodeFlags12() {
        print(EOL + "encodeFlags12()");
        CHECKER.checkEncode(V_1_2, BIT_ALL, ALL);
        CHECKER.checkEncode(V_1_2, BIT_SELECT, SELECT);
        CHECKER.checkEncode(V_1_2, BIT_INDIRECT, INDIRECT);
        CHECKER.checkEncode(V_1_2, BIT_FF, FF);
        CHECKER.checkEncode(V_1_2, BITMAP_ALL_GROUP_TYPES,
                ALL_GROUP_TYPES);
    }

    @Test
    public void decodeFlags12() {
        print(EOL + "decodeFlags12()");
        CHECKER.checkDecode(V_1_2, BIT_ALL, ALL);
        CHECKER.checkDecode(V_1_2, BIT_SELECT, SELECT);
        CHECKER.checkDecode(V_1_2, BIT_INDIRECT, INDIRECT);
        CHECKER.checkDecode(V_1_2, BIT_FF, FF);
        CHECKER.checkDecode(V_1_2, BITMAP_ALL_GROUP_TYPES,
                ALL_GROUP_TYPES);
    }

    @Test
    public void encodeFlags13() {
        print(EOL + "encodeFlags13()");
        CHECKER.checkEncode(V_1_3, BIT_ALL, ALL);
        CHECKER.checkEncode(V_1_3, BIT_SELECT, SELECT);
        CHECKER.checkEncode(V_1_3, BIT_INDIRECT, INDIRECT);
        CHECKER.checkEncode(V_1_3, BIT_FF, FF);
        CHECKER.checkEncode(V_1_3, BITMAP_ALL_GROUP_TYPES,
                ALL_GROUP_TYPES);
    }

    @Test
    public void decodeFlags13() {
        print(EOL + "decodeFlags13()");
        CHECKER.checkDecode(V_1_3, BIT_ALL, ALL);
        CHECKER.checkDecode(V_1_3, BIT_SELECT, SELECT);
        CHECKER.checkDecode(V_1_3, BIT_INDIRECT, INDIRECT);
        CHECKER.checkDecode(V_1_3, BIT_FF, FF);
        CHECKER.checkDecode(V_1_3, BITMAP_ALL_GROUP_TYPES,
                ALL_GROUP_TYPES);
    }


    @Test
    public void junkBitsThrowExceptionWhenStrictParsing() {
        print(EOL + "junkBitsThrowExceptionWhenStrictParsing()");
        try {
            GroupType.decodeFlags(BITMAP_SELECT_FF_WITH_JUNK, V_1_3);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "V_1_3 (strict decode) Bad bits: 0xfa",
                    e.getMessage());
        }
    }

    @Test
    public void junkBitsIgnoredWhenNonStrictParsing() {
        print(EOL + "junkBitsIgnoredWhenNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<GroupType> flags =
                GroupType.decodeFlags(BITMAP_SELECT_FF_WITH_JUNK, V_1_3);
        print(flags);
        verifyFlags(flags, SELECT, FF);
        MessageFactory.setStrictMessageParsing(true);
    }

}
