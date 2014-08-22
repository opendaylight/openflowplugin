/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for ProtocolVersion enumeration.
 *
 * @author Simon Hunt
 */
public class ProtocolVersionTest extends AbstractTest {

    private static final byte V10_BYTE = 0x01;
    private static final byte V11_BYTE = 0x02;
    private static final byte V12_BYTE = 0x03;
    private static final byte V13_BYTE = 0x04;

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (ProtocolVersion v: ProtocolVersion.values()) {
            print(v);
        }
        assertEquals(AM_UXCC, 4, ProtocolVersion.values().length);
    }

    @Test
    public void ver10() throws DecodeException {
        assertEquals(AM_NEQ, V_1_0, decode(V10_BYTE));
        assertEquals(AM_NEQ, V10_BYTE, V_1_0.code());
    }

    @Test
    public void ver11() throws DecodeException {
        assertEquals(AM_NEQ, V_1_1, decode(V11_BYTE));
        assertEquals(AM_NEQ, V11_BYTE, V_1_1.code());
    }

    @Test
    public void ver12() throws DecodeException {
        assertEquals(AM_NEQ, V_1_2, decode(V12_BYTE));
        assertEquals(AM_NEQ, V12_BYTE, V_1_2.code());
    }

    @Test
    public void ver13() throws DecodeException {
        assertEquals(AM_NEQ, V_1_3, decode(V13_BYTE));
        assertEquals(AM_NEQ, V13_BYTE, V_1_3.code());
    }

    @Test(expected = DecodeException.class)
    public void decodeBad0() throws DecodeException {
        decode((byte) 0x00);
    }

    @Test(expected = DecodeException.class)
    public void decodeBad5() throws DecodeException {
        decode((byte) 0x05);
    }

    @Test
    public void lt() {
        assertFalse(AM_HUH, V_1_0.lt(V_1_0));
        assertTrue(AM_HUH, V_1_0.lt(V_1_1));
        assertTrue(AM_HUH, V_1_0.lt(V_1_2));
        assertTrue(AM_HUH, V_1_0.lt(V_1_3));

        assertFalse(AM_HUH, V_1_1.lt(V_1_0));
        assertFalse(AM_HUH, V_1_1.lt(V_1_1));
        assertTrue(AM_HUH, V_1_1.lt(V_1_2));
        assertTrue(AM_HUH, V_1_1.lt(V_1_3));

        assertFalse(AM_HUH, V_1_2.lt(V_1_0));
        assertFalse(AM_HUH, V_1_2.lt(V_1_1));
        assertFalse(AM_HUH, V_1_2.lt(V_1_2));
        assertTrue(AM_HUH, V_1_2.lt(V_1_3));

        assertFalse(AM_HUH, V_1_3.lt(V_1_0));
        assertFalse(AM_HUH, V_1_3.lt(V_1_1));
        assertFalse(AM_HUH, V_1_3.lt(V_1_2));
        assertFalse(AM_HUH, V_1_3.lt(V_1_3));
    }
    @Test
    public void le() {
        assertTrue(AM_HUH, V_1_0.le(V_1_0));
        assertTrue(AM_HUH, V_1_0.le(V_1_1));
        assertTrue(AM_HUH, V_1_0.le(V_1_2));
        assertTrue(AM_HUH, V_1_0.le(V_1_3));

        assertFalse(AM_HUH, V_1_1.le(V_1_0));
        assertTrue(AM_HUH, V_1_1.le(V_1_1));
        assertTrue(AM_HUH, V_1_1.le(V_1_2));
        assertTrue(AM_HUH, V_1_1.le(V_1_3));

        assertFalse(AM_HUH, V_1_2.le(V_1_0));
        assertFalse(AM_HUH, V_1_2.le(V_1_1));
        assertTrue(AM_HUH, V_1_2.le(V_1_2));
        assertTrue(AM_HUH, V_1_2.le(V_1_3));

        assertFalse(AM_HUH, V_1_3.le(V_1_0));
        assertFalse(AM_HUH, V_1_3.le(V_1_1));
        assertFalse(AM_HUH, V_1_3.le(V_1_2));
        assertTrue(AM_HUH, V_1_3.le(V_1_3));
    }

    @Test
    public void gt() {
        assertFalse(AM_HUH, V_1_0.gt(V_1_0));
        assertFalse(AM_HUH, V_1_0.gt(V_1_1));
        assertFalse(AM_HUH, V_1_0.gt(V_1_2));
        assertFalse(AM_HUH, V_1_0.gt(V_1_3));

        assertTrue(AM_HUH, V_1_1.gt(V_1_0));
        assertFalse(AM_HUH, V_1_1.gt(V_1_1));
        assertFalse(AM_HUH, V_1_1.gt(V_1_2));
        assertFalse(AM_HUH, V_1_1.gt(V_1_3));

        assertTrue(AM_HUH, V_1_2.gt(V_1_0));
        assertTrue(AM_HUH, V_1_2.gt(V_1_1));
        assertFalse(AM_HUH, V_1_2.gt(V_1_2));
        assertFalse(AM_HUH, V_1_2.gt(V_1_3));

        assertTrue(AM_HUH, V_1_3.gt(V_1_0));
        assertTrue(AM_HUH, V_1_3.gt(V_1_1));
        assertTrue(AM_HUH, V_1_3.gt(V_1_2));
        assertFalse(AM_HUH, V_1_3.gt(V_1_3));
    }

    @Test
    public void ge() {
        assertTrue(AM_HUH, V_1_0.ge(V_1_0));
        assertFalse(AM_HUH, V_1_0.ge(V_1_1));
        assertFalse(AM_HUH, V_1_0.ge(V_1_2));
        assertFalse(AM_HUH, V_1_0.ge(V_1_3));

        assertTrue(AM_HUH, V_1_1.ge(V_1_0));
        assertTrue(AM_HUH, V_1_1.ge(V_1_1));
        assertFalse(AM_HUH, V_1_1.ge(V_1_2));
        assertFalse(AM_HUH, V_1_1.ge(V_1_3));

        assertTrue(AM_HUH, V_1_2.ge(V_1_0));
        assertTrue(AM_HUH, V_1_2.ge(V_1_1));
        assertTrue(AM_HUH, V_1_2.ge(V_1_2));
        assertFalse(AM_HUH, V_1_2.ge(V_1_3));

        assertTrue(AM_HUH, V_1_3.ge(V_1_0));
        assertTrue(AM_HUH, V_1_3.ge(V_1_1));
        assertTrue(AM_HUH, V_1_3.ge(V_1_2));
        assertTrue(AM_HUH, V_1_3.ge(V_1_3));
    }

    @Test
    public void latest() {
        assertEquals(AM_NEQ, V_1_3, ProtocolVersion.latest());
    }

    private void checkVerStr(ProtocolVersion pv, String expStr) {
        print("{} => {}", pv, pv.toDisplayString());
        assertEquals(AM_NEQ, expStr, pv.toDisplayString());
        assertEquals(AM_NEQ, pv, ProtocolVersion.fromString(expStr));
    }

    @Test
    public void testToStringFromString() {
        print(EOL + "testToStringFromString()");
        checkVerStr(V_1_0, "1.0.0");
        checkVerStr(V_1_1, "1.1.0");
        checkVerStr(V_1_2, "1.2");
        checkVerStr(V_1_3, "1.3.0");
    }

    private static final String[] NOT_VER = {
            null, "", "1", "1.0", "0.0.0",
            "1.3.1", // TODO - Eventually, we want this to return V_1_3
                     // But currently, ProtocolVersion.fromString("1.3.1")
                     // returns null;
    };

    @Test
    public void unmatchedStr() {
        print(EOL + "unmatchedStr()");
        for (String s: NOT_VER) {
            ProtocolVersion pv = ProtocolVersion.fromString(s);
            print("not a version match: {} => {}", s, pv);
            assertNull(AM_HUH, pv);
        }
    }

    private void checkMax(ProtocolVersion exp, ProtocolVersion... makeSet) {
        Set<ProtocolVersion> set =
                new HashSet<ProtocolVersion>(Arrays.asList(makeSet));
        ProtocolVersion max = ProtocolVersion.max(set);
        print("{} => Max: {}", set, max);
        assertEquals(AM_NEQ, exp, max);
    }

    @Test
    public void maxFunction() {
        print(EOL + "maxFunction()");
        assertNull(AM_HUH, ProtocolVersion.max(null));
        Set<ProtocolVersion> set = new HashSet<ProtocolVersion>();
        assertNull(AM_HUH, ProtocolVersion.max(set));
        checkMax(V_1_0, V_1_0);
        checkMax(V_1_1, V_1_1);
        checkMax(V_1_2, V_1_2);
        checkMax(V_1_3, V_1_3);
        checkMax(V_1_1, PV_01);
        checkMax(V_1_2, PV_012);
        checkMax(V_1_3, PV_0123);
        checkMax(V_1_2, PV_12);
        checkMax(V_1_3, PV_123);
        checkMax(V_1_3, PV_23);
    }
}
