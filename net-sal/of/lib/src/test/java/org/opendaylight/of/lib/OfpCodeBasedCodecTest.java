/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;
import org.opendaylight.of.lib.msg.MessageFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.opendaylight.of.lib.AbstractCodeBasedCodecChecker.EXPECT_EXCEPTION;
import static org.opendaylight.of.lib.AbstractTest.FMT_EX;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the {@link OfpCodeBasedCodec}.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class OfpCodeBasedCodecTest {

    private static enum FooBar implements OfpCodeBasedEnum {
        FOO(0),     // since 1.0
        GOO(1),     // since 1.0; dropped 1.3
        ZOO(2),     // since 1.0
        BAR(4),     // since 1.1
        BAZ(5),     // since 1.3
        BOP(6),     // since 1.3
        BAM(7),     // since 1.3
        EXP(0xffff), // since 1.0
        ;

        private int code;

        FooBar(int code) {
            this.code = code;
        }

        private static final int[] MASKS = {
                0x07,   // 1.0
                0x17,   // 1.1
                0x17,   // 1.2
                0xf5,   // 1.3
        };

        @Override
        public int getCode(ProtocolVersion pv) {
            return code;
        }

        private static final FooBar[] MAPPABLE = new FooBar[values().length-1];

        static {
            Set<FooBar> all = new TreeSet<FooBar>(Arrays.asList(values()));
            all.remove(EXP);
            all.toArray(MAPPABLE);
        }

        private static final OfpCodeBasedCodec<FooBar> CODEC =
                new OfpCodeBasedCodec<FooBar>(MASKS, MAPPABLE);

        public static Set<FooBar> decodeFlags(int bitmap, ProtocolVersion pv) {
            return CODEC.decode(bitmap, pv);
        }

        public static int encodeFlags(Set<FooBar> flags, ProtocolVersion pv) {
            return CODEC.encode(flags, pv);
        }
    }

    private static final AbstractCodeBasedCodecChecker<FooBar> CHECKER =
        new AbstractCodeBasedCodecChecker<FooBar>() {
            @Override
            protected Set<FooBar> decodeFlags(int bitmap, ProtocolVersion pv) {
                return FooBar.decodeFlags(bitmap, pv);
            }

            @Override
            protected int encodeFlags(Set<FooBar> flags, ProtocolVersion pv) {
                return FooBar.encodeFlags(flags, pv);
            }
        };

    private static final int BITMAP_10 = 0x07;    // 0000 0111
    private static final int BITMAP_12 = 0x15;    // 0001 0101
    private static final int BITMAP_13 = 0xb4;    // 1011 0100

    private static final FooBar[] FLAGS_10 =
            {FooBar.FOO, FooBar.GOO, FooBar.ZOO};
    private static final FooBar[] FLAGS_12 =
            {FooBar.FOO, FooBar.ZOO, FooBar.BAR};
    private static final FooBar[] FLAGS_13 =
            {FooBar.ZOO, FooBar.BAR, FooBar.BAZ, FooBar.BAM};
    private static final FooBar[] EXPECT_VMM = {};

    @Test
    public void process10Set() {
        print(EOL + "process10Set()");
        CHECKER.checkDecode(V_1_0, BITMAP_10, FLAGS_10);
        CHECKER.checkEncode(V_1_0, BITMAP_10, FLAGS_10);
    }

    @Test
    public void process12Set() {
        print(EOL + "process12Set()");
        CHECKER.checkDecode(V_1_2, BITMAP_12, FLAGS_12);
        CHECKER.checkEncode(V_1_2, BITMAP_12, FLAGS_12);
    }

    @Test
    public void process13Set() {
        print(EOL + "process13Set()");
        CHECKER.checkDecode(V_1_3, BITMAP_13, FLAGS_13);
        CHECKER.checkEncode(V_1_3, BITMAP_13, FLAGS_13);
    }

    @Test
    public void processV12SetAsV10() {
        print(EOL + "processV12SetAsV10()");
        MessageFactory.setStrictMessageParsing(true);
        CHECKER.checkDecode(V_1_0, BITMAP_12, EXPECT_VMM);
        CHECKER.checkEncode(V_1_0, EXPECT_EXCEPTION, FLAGS_12);
        MessageFactory.setStrictMessageParsing(false);
    }

    @Test
    public void processV10SetAsV13() {
        print(EOL + "processV10SetAsV13()");
        MessageFactory.setStrictMessageParsing(true);
        CHECKER.checkDecode(V_1_3, BITMAP_10, EXPECT_VMM);
        CHECKER.checkEncode(V_1_3, EXPECT_EXCEPTION, FLAGS_10);
        MessageFactory.setStrictMessageParsing(false);
    }

    @Test
    public void constructor() {
        print(EOL + "constructor()");
        try {
            new OfpCodeBasedCodec<FooBar>(FooBar.MASKS, FooBar.values());
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX> " + e);
        }
    }

    @Test
    public void invalidCode() {
        print(EOL + "invalidCode()");
        CHECKER.checkEncode(V_1_3, EXPECT_EXCEPTION, true, FooBar.EXP);
    }

    //=== testing strict vs. non-strict parsing:

    private static final int MAP_0F = 0x0f;

    @Test
    public void strict10ShouldFail() {
        print(EOL + "strict10ShouldFail()");
        MessageFactory.setStrictMessageParsing(true);
        try {
            Set<FooBar> flags = FooBar.decodeFlags(MAP_0F, V_1_0);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_NEQ, "V_1_0 (strict decode) Bad bits: 0xf",
                    e.getMessage());
        }
        MessageFactory.setStrictMessageParsing(false);
    }

    @Test
    public void nonStrict10ShouldPass() {
        print(EOL + "nonStrict10ShouldPass()");
        Set<FooBar> flags = FooBar.decodeFlags(MAP_0F, V_1_0);
        verifySetContains(flags, FooBar.FOO, FooBar.GOO, FooBar.ZOO);
    }
}
