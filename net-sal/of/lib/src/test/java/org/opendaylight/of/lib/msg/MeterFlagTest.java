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

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.msg.MeterFlag.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MeterFlag.
 *
 * @author Simon Hunt
 */
public class MeterFlagTest extends BitmappedEnumTest<MeterFlag> {

    @Override
    protected Set<MeterFlag> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return MeterFlag.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<MeterFlag> flags, ProtocolVersion pv) {
        return MeterFlag.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (MeterFlag f: MeterFlag.values())
            print(f);
        assertEquals(AM_UXCC, 4, MeterFlag.values().length);
    }

    private void v10v11v12Codec(ProtocolVersion pv) {
        // not supported before 1.3
        verifyNaU16(pv, 0x1);
        verifyNaBit(pv, 0x8000, KBPS);
        verifyNaBit(pv, 0x8000, PKTPS);
        verifyNaBit(pv, 0x8000, BURST);
        verifyNaBit(pv, 0x8000, STATS);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        v10v11v12Codec(V_1_0);
    }

    @Test
    public void v11Decode() {
        print(EOL + "v11Decode()");
        v10v11v12Codec(V_1_1);
    }

    @Test
    public void v12Decode() {
        print(EOL + "v12Decode()");
        v10v11v12Codec(V_1_2);
    }

    @Test
    public void v13Decode() {
        print(EOL + "v13Decode()");
        verifyBit(V_1_3, 0x1, KBPS);
        verifyBit(V_1_3, 0x2, PKTPS);
        verifyBit(V_1_3, 0x4, BURST);
        verifyBit(V_1_3, 0x8, STATS);
        verifyNaU16(V_1_3, 0x10);
    }

    @Test
    public void sampler() {
        print(EOL + "sampler()");
        verifyBitmappedFlags(V_1_3, 0xe, PKTPS, BURST, STATS);
        verifyBitmappedFlags(V_1_3, 0x5, KBPS, BURST);
    }
}
