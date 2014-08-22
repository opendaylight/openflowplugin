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
import static org.opendaylight.of.lib.msg.PortFeature.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for PortFeature.
 *
 * @author Simon Hunt
 */
public class PortFeatureTest extends BitmappedEnumTest<PortFeature> {

    @Override
    protected Set<PortFeature> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return PortFeature.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<PortFeature> flags, ProtocolVersion pv) {
        return PortFeature.encodeBitmap(flags, pv);
    }

    // TODO : refactor into superclass
    @Test
    public void basic() {
        print(EOL + "basic()");
        print(BASIC_HEADER);
        for (PortFeature f: PortFeature.values())
            print(FMT_ENUM_STRINGS, padName(f.name()), padName(f),
                    padDisplay(f.toDisplayString()));
        assertEquals(AM_UXCC, 16, PortFeature.values().length);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        v11v12v13Codec(V_1_3);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        v11v12v13Codec(V_1_2);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        v11v12v13Codec(V_1_1);
    }

    private void v11v12v13Codec(ProtocolVersion pv) {
        verifyBit(pv, 0x1, RATE_10MB_HD);
        verifyBit(pv, 0x2, RATE_10MB_FD);
        verifyBit(pv, 0x4, RATE_100MB_HD);
        verifyBit(pv, 0x8, RATE_100MB_FD);
        verifyBit(pv, 0x10, RATE_1GB_HD);
        verifyBit(pv, 0x20, RATE_1GB_FD);
        verifyBit(pv, 0x40, RATE_10GB_FD);
        verifyBit(pv, 0x80, RATE_40GB_FD);
        verifyBit(pv, 0x100, RATE_100GB_FD);
        verifyBit(pv, 0x200, RATE_1TB_FD);
        verifyBit(pv, 0x400, RATE_OTHER);
        verifyBit(pv, 0x800, COPPER);
        verifyBit(pv, 0x1000, FIBER);
        verifyBit(pv, 0x2000, AUTONEG);
        verifyBit(pv, 0x4000, PAUSE);
        verifyBit(pv, 0x8000, PAUSE_ASYM);
        verifyNaU32(pv, 0x10000);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, RATE_10MB_HD);
        verifyBit(V_1_0, 0x2, RATE_10MB_FD);
        verifyBit(V_1_0, 0x4, RATE_100MB_HD);
        verifyBit(V_1_0, 0x8, RATE_100MB_FD);
        verifyBit(V_1_0, 0x10, RATE_1GB_HD);
        verifyBit(V_1_0, 0x20, RATE_1GB_FD);
        verifyBit(V_1_0, 0x40, RATE_10GB_FD);
        verifyBit(V_1_0, 0x80, COPPER);
        verifyBit(V_1_0, 0x100, FIBER);
        verifyBit(V_1_0, 0x200, AUTONEG);
        verifyBit(V_1_0, 0x400, PAUSE);
        verifyBit(V_1_0, 0x800, PAUSE_ASYM);
        verifyNaU32(V_1_0, 0x1000);
        // chosen an NA bit position in the following
        verifyNaBit(V_1_0, 0x1000, RATE_40GB_FD);
        verifyNaBit(V_1_0, 0x1000, RATE_100GB_FD);
        verifyNaBit(V_1_0, 0x1000, RATE_1TB_FD);
        verifyNaBit(V_1_0, 0x1000, RATE_OTHER);
    }

    @Test
    public void samples() {
        print(EOL + "samples()");
        verifyBitmappedFlags(V_1_0, 0x4a0, RATE_1GB_FD, COPPER, PAUSE);
        verifyBitmappedFlags(V_1_0, 0x801, RATE_10MB_HD, PAUSE_ASYM);

        verifyBitmappedFlags(V_1_1, 0x4820, RATE_1GB_FD, COPPER, PAUSE);
        verifyBitmappedFlags(V_1_1, 0x8001, RATE_10MB_HD, PAUSE_ASYM);

        verifyBitmappedFlags(V_1_2, 0x4820, RATE_1GB_FD, COPPER, PAUSE);
        verifyBitmappedFlags(V_1_2, 0x8001, RATE_10MB_HD, PAUSE_ASYM);

        verifyBitmappedFlags(V_1_3, 0x4820, RATE_1GB_FD, COPPER, PAUSE);
        verifyBitmappedFlags(V_1_3, 0x8001, RATE_10MB_HD, PAUSE_ASYM);
    }
}
