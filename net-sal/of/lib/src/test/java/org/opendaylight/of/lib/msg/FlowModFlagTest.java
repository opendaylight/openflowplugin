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
import org.opendaylight.of.lib.BitmappedEnumTest;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.junit.TestTools;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.FlowModFlag.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for FlowModFlag.
 *
 * @author Simon Hunt
 */
public class FlowModFlagTest extends BitmappedEnumTest<FlowModFlag> {
    @Override
    protected Set<FlowModFlag> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return FlowModFlag.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<FlowModFlag> flags, ProtocolVersion pv) {
        return FlowModFlag.encodeBitmap(flags, pv);
    }

    private static boolean strictDecodeSetting;

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
        for (FlowModFlag f: FlowModFlag.values())
            print(f);
        assertEquals(AM_UXCC, 6, FlowModFlag.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, SEND_FLOW_REM);
        verifyBit(V_1_0, 0x2, CHECK_OVERLAP);
        verifyBit(V_1_0, 0x4, EMERG);
        // NOTE: RESET_COUNTS is 0x4 (valid as EMERG), so use a dummy code
        verifyNaBit(V_1_0, 0x8000, RESET_COUNTS);
        verifyNaBit(V_1_0, 0x8, NO_PACKET_COUNTS);
        verifyNaBit(V_1_0, 0x10, NO_BYTE_COUNTS);
        verifyNaU16(V_1_0, 0x8);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        verifyBit(V_1_1, 0x1, SEND_FLOW_REM);
        verifyBit(V_1_1, 0x2, CHECK_OVERLAP);
        verifyNaBit(V_1_1, 0x4, EMERG);
        verifyNaBit(V_1_1, 0x8000, RESET_COUNTS);
        verifyNaBit(V_1_1, 0x8, NO_PACKET_COUNTS);
        verifyNaBit(V_1_1, 0x10, NO_BYTE_COUNTS);
        verifyNaU16(V_1_1, 0x4);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        verifyBit(V_1_2, 0x1, SEND_FLOW_REM);
        verifyBit(V_1_2, 0x2, CHECK_OVERLAP);
        verifyNaBit(V_1_2, 0x8000, EMERG);
        verifyBit(V_1_2, 0x4, RESET_COUNTS);
        verifyNaBit(V_1_2, 0x8, NO_PACKET_COUNTS);
        verifyNaBit(V_1_2, 0x10, NO_BYTE_COUNTS);
        verifyNaU16(V_1_2, 0x8);
    }

    @Test
    public void v13Decode() {
        print(EOL + "v13Decode()");
        verifyBit(V_1_3, 0x1, SEND_FLOW_REM);
        verifyBit(V_1_3, 0x2, CHECK_OVERLAP);
        verifyNaBit(V_1_3, 0x8000, EMERG);
        verifyBit(V_1_3, 0x4, RESET_COUNTS);
        verifyBit(V_1_3, 0x8, NO_PACKET_COUNTS);
        verifyBit(V_1_3, 0x10, NO_BYTE_COUNTS);
        verifyNaU16(V_1_3, 0x20);
    }

    @Test
    public void sampler() {
        print(EOL + "sampler()");
        verifyBitmappedFlags(V_1_0, 0x6, EMERG, CHECK_OVERLAP);
        verifyBitmappedFlags(V_1_1, 0x3, SEND_FLOW_REM, CHECK_OVERLAP);
        verifyBitmappedFlags(V_1_2, 0x7,
                SEND_FLOW_REM, CHECK_OVERLAP, RESET_COUNTS);
        verifyBitmappedFlags(V_1_3, 0x1c,
                RESET_COUNTS, NO_PACKET_COUNTS, NO_BYTE_COUNTS);
    }

    // from a version of the Comware(?) switch that returned these bits:
    private static final int BAD_MAP = 0x58a;

    @Test(expected = VersionMismatchException.class)
    public void badBitsThrowsAnException() {
        Set<FlowModFlag> flags = FlowModFlag.decodeBitmap(BAD_MAP, V_1_3);
    }

    @Test
    public void badBitsIgnored() {
        MessageFactory.setStrictMessageParsing(false);
        Set<FlowModFlag> flags = FlowModFlag.decodeBitmap(BAD_MAP, V_1_3);
        assertEquals(TestTools.AM_UXS, 2, flags.size());
        assertTrue("!chkOver", flags.contains(FlowModFlag.CHECK_OVERLAP));
        assertTrue("!noPktCts", flags.contains(FlowModFlag.NO_PACKET_COUNTS));
        MessageFactory.setStrictMessageParsing(true);

    }

}
