/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.junit.Test;
import org.opendaylight.of.lib.BitmappedEnumTest;
import org.opendaylight.of.lib.ProtocolVersion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.PortState.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for PortState.
 *
 * @author Simon Hunt
 */
public class PortStateTest extends BitmappedEnumTest<PortState> {

    @Override
    protected Set<PortState> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return PortState.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<PortState> flags, ProtocolVersion pv) {
        return PortState.encodeBitmap(flags, pv);
    }

    // TODO : refactor into superclass
    @Test
    public void basic() {
        print(EOL + "basic()");
        print(BASIC_HEADER);
        for (PortState ps: PortState.values())
            print(FMT_ENUM_STRINGS, padName(ps.name()), padName(ps),
                    padDisplay(ps.toDisplayString()));
        assertEquals(AM_UXCC, 7, PortState.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, LINK_DOWN, false);
        // not-applicable bits for the next 7 positions...
        verifyNaBit(V_1_0, 0x2, BLOCKED);
        verifyNaBit(V_1_0, 0x4, LIVE);
        verifyNaBit(V_1_0, 0x8);
        verifyNaBit(V_1_0, 0x10);
        verifyNaBit(V_1_0, 0x20);
        verifyNaBit(V_1_0, 0x40);
        verifyNaBit(V_1_0, 0x80);
        // next two bits (0x100, 0x200) interpreted as the STP field
        // verified in v10StpDecode()
        verifyNaU32(V_1_0, 0x400);
    }

    @Test
    public void v10StpDecode() {
        print(EOL + "v10StpDecode()");
        Set<PortState> flags = PortState.decodeBitmap(0x000, V_1_0);
        verifyFlags(flags, STP_LISTEN);
        flags = PortState.decodeBitmap(0x100, V_1_0);
        verifyFlags(flags, STP_LEARN);
        flags = PortState.decodeBitmap(0x200, V_1_0);
        verifyFlags(flags, STP_FORWARD);
        flags = PortState.decodeBitmap(0x300, V_1_0);
        verifyFlags(flags, STP_BLOCK);
    }

    private void checkStpFlag(PortState flag, int expValue) {
        Set<PortState> flags = new HashSet<PortState>();
        flags.add(flag);
        int bitmap = PortState.encodeBitmap(flags, V_1_0);
        print("STP to bitmap: {} -> {}", flags, hex(bitmap));
        assertEquals(AM_NEQ, expValue, bitmap);
    }

    @Test
    public void v10StpEncode() {
        print(EOL + "v10StpEncode()");
        checkStpFlag(STP_LISTEN, 0x000);
        checkStpFlag(STP_LEARN, 0x100);
        checkStpFlag(STP_FORWARD, 0x200);
        checkStpFlag(STP_BLOCK, 0x300);
    }

    private void checkStpMutex10(PortState... flags) {
        Set<PortState> flagSet = new HashSet<PortState>(Arrays.asList(flags));
        try {
            PortState.encodeBitmap(flagSet, V_1_0);
            fail(AM_NOEX);
        } catch (IllegalStateException ise) {
            print(FMT_EX, ise);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void v10StpMutex() {
        print(EOL + "v10StpMutex()");
        checkStpMutex10(STP_LISTEN, STP_LEARN);
        checkStpMutex10(STP_LISTEN, STP_FORWARD);
        checkStpMutex10(STP_LISTEN, STP_BLOCK);
        checkStpMutex10(STP_LEARN, STP_FORWARD);
        checkStpMutex10(STP_LEARN, STP_BLOCK);
        checkStpMutex10(STP_FORWARD, STP_BLOCK);

        checkStpMutex10(STP_LISTEN, STP_LEARN, STP_FORWARD);
        checkStpMutex10(STP_LISTEN, STP_LEARN, STP_BLOCK);
        checkStpMutex10(STP_LISTEN, STP_FORWARD, STP_BLOCK);
        checkStpMutex10(STP_LEARN, STP_FORWARD, STP_BLOCK);

        checkStpMutex10(STP_LISTEN, STP_LEARN, STP_FORWARD, STP_BLOCK);
    }

    private void v11v12v13Codec(ProtocolVersion pv) {
        verifyBit(pv, 0x1, LINK_DOWN);
        verifyBit(pv, 0x2, BLOCKED);
        verifyBit(pv, 0x4, LIVE);
        verifyNaU32(pv, 0x8);
        verifyNaBit(pv, 0x100, STP_LISTEN);  // (Yes, 0x100, just to test)
        verifyNaBit(pv, 0x100, STP_LEARN);
        verifyNaBit(pv, 0x200, STP_FORWARD);
        verifyNaBit(pv, 0x300, STP_BLOCK);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        v11v12v13Codec(V_1_1);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        v11v12v13Codec(V_1_2);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        v11v12v13Codec(V_1_3);
    }

    @Test
    public void sampler() {
        print(EOL + "sampler()");
        verifyBitmappedFlags(V_1_0, 0x000, STP_LISTEN);
        verifyBitmappedFlags(V_1_0, 0x001, LINK_DOWN, STP_LISTEN);
        verifyBitmappedFlags(V_1_0, 0x101, LINK_DOWN, STP_LEARN);
        verifyBitmappedFlags(V_1_0, 0x201, LINK_DOWN, STP_FORWARD);
        verifyBitmappedFlags(V_1_0, 0x301, LINK_DOWN, STP_BLOCK);

        verifyBitmappedFlags(V_1_1, 0x001, LINK_DOWN);
        verifyBitmappedFlags(V_1_1, 0x006, BLOCKED, LIVE);

        verifyBitmappedFlags(V_1_2, 0x007, LINK_DOWN, BLOCKED, LIVE);

        verifyBitmappedFlags(V_1_3, 0x005, LINK_DOWN, LIVE);
    }
}
