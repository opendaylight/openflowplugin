/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit Tests for OxmClass.
 *
 * @author Simon Hunt
 */
public class OxmClassTest extends AbstractTest {

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (OxmClass oxm: OxmClass.values())
            print(oxm);
        assertEquals(AM_UXCC, 7, OxmClass.values().length);
    }

    private static final int[] UNKNOWN = { -3, 5, 11, 0x7fff, 0x8001, 0xfffe};

    @Test
    public void unknownCodes() {
        print(EOL + "unknownCodes()");
        for (ProtocolVersion pv: PV_0123) {
            print(pv);
            for (int code: UNKNOWN) {
                OxmClass oxm = OxmClass.decode(code, pv);
                print(FMT_PV_CODE_ENUM, pv, Integer.toHexString(code), oxm);
                assertEquals(AM_NEQ, OxmClass.UNKNOWN, oxm);
            }
        }
    }

    private void verifyCode(int code, ProtocolVersion pv, OxmClass exp) {
        try {
            OxmClass oxm = OxmClass.decode(code, pv);
            print(FMT_PV_CODE_ENUM, pv, Integer.toHexString(code), oxm);
            if (exp == null)
                fail(AM_NOEX);
            assertEquals(AM_NEQ, exp, oxm);
        } catch (VersionMismatchException vme) {
            print(FMT_EX, vme);
            if (exp != null)
                fail(AM_UNEX_MISMATCH);
        }
    }

    @Test
    public void codesV0() {
        print(EOL + "codesV0()");
        verifyCode(0x0000, V_1_0, null);
        verifyCode(0x0001, V_1_0, null);
        verifyCode(0x8000, V_1_0, null);
        verifyCode(0xffff, V_1_0, null);
    }

    @Test
    public void codesV1() {
        print(EOL + "codesV1()");
        verifyCode(0x0000, V_1_1, null);
        verifyCode(0x0001, V_1_1, null);
        verifyCode(0x8000, V_1_1, null);
        verifyCode(0xffff, V_1_1, null);
    }

    @Test
    public void codesV2() {
        print(EOL + "codesV2()");
        verifyCode(0x0000, V_1_2, OxmClass.NXM_0);
        verifyCode(0x0001, V_1_2, OxmClass.NXM_1);
        verifyCode(0x8000, V_1_2, OxmClass.OPENFLOW_BASIC);
        verifyCode(0xffff, V_1_2, OxmClass.EXPERIMENTER);
    }

    @Test
    public void codesV3() {
        print(EOL + "codesV3()");
        verifyCode(0x0000, V_1_3, OxmClass.NXM_0);
        verifyCode(0x0001, V_1_3, OxmClass.NXM_1);
        verifyCode(0x8000, V_1_3, OxmClass.OPENFLOW_BASIC);
        verifyCode(0xffff, V_1_3, OxmClass.EXPERIMENTER);
    }

    @Test
    public void reserved() {
        print(EOL + "reserved()");
        assertFalse(AM_HUH, OxmClass.NXM_0.isReservedClass());
        assertFalse(AM_HUH, OxmClass.NXM_1.isReservedClass());
        assertTrue(AM_HUH, OxmClass.OPENFLOW_BASIC.isReservedClass());
        assertTrue(AM_HUH, OxmClass.EXPERIMENTER.isReservedClass());
    }
}
