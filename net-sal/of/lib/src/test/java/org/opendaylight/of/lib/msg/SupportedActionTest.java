/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
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
import static org.opendaylight.of.lib.msg.SupportedAction.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for SupportedAction.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public class SupportedActionTest extends BitmappedEnumTest<SupportedAction> {

    @Override
    protected Set<SupportedAction> decodeBitmap(int bitmap, ProtocolVersion pv) {
        return SupportedAction.decodeBitmap(bitmap, pv);
    }

    @Override
    protected int encodeBitmap(Set<SupportedAction> flags, ProtocolVersion pv) {
        return SupportedAction.encodeBitmap(flags, pv);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (SupportedAction a: SupportedAction.values())
            print(a);
        assertEquals(AM_UXCC, 12, SupportedAction.values().length);
    }

    @Test
    public void v10Codec() {
        print(EOL + "v10Codec()");
        verifyBit(V_1_0, 0x1, OUTPUT);
        verifyBit(V_1_0, 0x2, SET_VLAN_VID);
        verifyBit(V_1_0, 0x4, SET_VLAN_PCP);
        verifyBit(V_1_0, 0x8, STRIP_VLAN);
        verifyBit(V_1_0, 0x10, SET_DL_SRC);
        verifyBit(V_1_0, 0x20, SET_DL_DST);
        verifyBit(V_1_0, 0x40, SET_NW_SRC);
        verifyBit(V_1_0, 0x80, SET_NW_DST);
        verifyBit(V_1_0, 0x100, SET_NW_TOS);
        verifyBit(V_1_0, 0x200, SET_TP_SRC);
        verifyBit(V_1_0, 0x400, SET_TP_DST);
        verifyBit(V_1_0, 0x800, ENQUEUE);
        verifyNaU32(V_1_0, 0x1000);
    }

    @Test
    public void v11Codec() {
        print(EOL + "v11Codec()");
        verifyNaU32(V_1_1, 0x1);
    }

    @Test
    public void v12Codec() {
        print(EOL + "v12Codec()");
        verifyNaU32(V_1_2, 0x1);
    }

    @Test
    public void v13Codec() {
        print(EOL + "v13Codec()");
        verifyNaU32(V_1_3, 0x1);
    }

    @Test
    public void samples() {
        print(EOL + "samples()");
        verifyBitmappedFlags(V_1_0, 0x9, OUTPUT, STRIP_VLAN);
    }
}
