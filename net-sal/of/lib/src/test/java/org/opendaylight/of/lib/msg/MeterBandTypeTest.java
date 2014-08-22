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
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MeterBandType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link MeterBandType}.
 *
 * @author Simon Hunt
 */
public class MeterBandTypeTest extends AbstractCodeBasedEnumTest<MeterBandType> {

    private static final AbstractCodeBasedCodecChecker<MeterBandType> CHECKER =
            new AbstractCodeBasedCodecChecker<MeterBandType>() {
                @Override
                protected Set<MeterBandType> decodeFlags(int bitmap, ProtocolVersion pv) {
                    return MeterBandType.decodeFlags(bitmap, pv);
                }

                @Override
                protected int encodeFlags(Set<MeterBandType> flags, ProtocolVersion pv) {
                    return MeterBandType.encodeFlags(flags, pv);
                }
            };

    @Override
    protected MeterBandType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        return MeterBandType.decode(code, pv);
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
        for (MeterBandType t : MeterBandType.values())
            print(t);
        assertEquals(AM_UXCC, 3, MeterBandType.values().length);
    }

    @Test
    public void decode10() {
        print(EOL + "decode10()");
        notSup(V_1_0);
    }

    @Test
    public void decode11() {
        print(EOL + "decode11()");
        notSup(V_1_1);
    }

    @Test
    public void decode12() {
        print(EOL + "decode12()");
        notSup(V_1_2);
    }

    @Test
    public void decode13() {
        print(EOL + "decode13()");
        check(V_1_3, -1, null);
        check(V_1_3, 0, null);
        check(V_1_3, 1, DROP);
        check(V_1_3, 2, DSCP_REMARK);
        check(V_1_3, 3, null);
        check(V_1_3, 0xffff, EXPERIMENTER);
    }

    // === Code to Bitmap
    private static final int BIT_DROP = 1 << 1;
    private static final int BIT_DSCP_REMARK = 1 << 2;
    private static final int BITMAP_ALLBITS = BIT_DROP | BIT_DSCP_REMARK;
    private static final int ZEROTH_BIT = 0x1;

    @Test
    public void encodeFlags13() {
        print(EOL + "encodeFlags13()");
        CHECKER.checkEncode(V_1_3, BIT_DROP, DROP);
        CHECKER.checkEncode(V_1_3, BIT_DSCP_REMARK, DSCP_REMARK);
        CHECKER.checkEncode(V_1_3, BITMAP_ALLBITS, DROP, DSCP_REMARK);
    }

    @Test
    public void decodeFlags13() {
        print(EOL + "decodeFlags13()");
        CHECKER.checkDecode(V_1_3, BIT_DROP, DROP);
        CHECKER.checkDecode(V_1_3, BIT_DSCP_REMARK, DSCP_REMARK);
        CHECKER.checkDecode(V_1_3, BITMAP_ALLBITS, DROP, DSCP_REMARK);
    }


    @Test
    public void zerothBitComplainsWithStrictParsing() {
        print(EOL + "zerothBitComplainsWithStrictParsing()");
        try {
            MeterBandType.decodeFlags(ZEROTH_BIT, V_1_3);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG, "V_1_3 (strict decode) Bad bits: 0x1",
                    e.getMessage());
        }
    }

    @Test
    public void zerothBitIgnoredWithNonStrictParsing() {
        print(EOL + "zerothBitIgnoredWithNonStrictParsing()");
        MessageFactory.setStrictMessageParsing(false);
        Set<MeterBandType> flags = MeterBandType.decodeFlags(ZEROTH_BIT, V_1_3);
        assertEquals(AM_UXS, 0, flags.size());
        MessageFactory.setStrictMessageParsing(true);
    }

}
