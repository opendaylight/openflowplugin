/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.PrimitiveUtils.*;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for PrimitiveUtils
 *
 * @author Simon Hunt
 */
public class PrimitiveUtilsTest {
    @Test
    public void fromU8zero() {
        assertEquals(AM_NEQ, 0, fromU8((byte) 0x0));
    }
    @Test
    public void fromU8max() {
        assertEquals(AM_NEQ, 255, fromU8((byte) 0xff));
    }
    @Test
    public void fromU8middlePositive() {
        assertEquals(AM_NEQ, 127, fromU8((byte) 0x7f));
    }
    @Test
    public void fromU8middleNegative() {
        assertEquals(AM_NEQ, 128, fromU8((byte) 0x80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toU8neg() {
        toU8((short) -1);
    }
    @Test(expected = IllegalArgumentException.class)
    public void toU8maxPlusOne() {
        toU8((short) 256);
    }

    @Test
    public void toU8zero() {
        assertEquals(AM_NEQ, (byte)0x0, toU8((short) 0));
    }
    @Test
    public void toU8max() {
        assertEquals(AM_NEQ, (byte)0xff, toU8((short) 255));
    }
    @Test
    public void toU8middlePositive() {
        assertEquals(AM_NEQ, (byte)0x7f, toU8((short) 127));
    }
    @Test
    public void toU8middleNegative() {
        assertEquals(AM_NEQ, (byte)0x80, toU8((short) 128));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toU8negInt() {
        toU8(-1);
    }
    @Test(expected = IllegalArgumentException.class)
    public void toU8maxPlusOneInt() {
        toU8(256);
    }

    @Test
    public void toU8zeroInt() {
        assertEquals(AM_NEQ, (byte)0x0, toU8(0));
    }
    @Test
    public void toU8maxInt() {
        assertEquals(AM_NEQ, (byte)0xff, toU8(255));
    }
    @Test
    public void toU8middlePositiveInt() {
        assertEquals(AM_NEQ, (byte)0x7f, toU8(127));
    }
    @Test
    public void toU8middleNegativeInt() {
        assertEquals(AM_NEQ, (byte)0x80, toU8(128));
    }

    // =========
    @Test
    public void fromU16zero() {
        assertEquals(AM_NEQ, 0, fromU16((short) 0x0));
    }
    @Test
    public void fromU16max() {
        assertEquals(AM_NEQ, 65535, fromU16((short) 0xffff));
    }
    @Test
    public void fromU16middlePositive() {
        assertEquals(AM_NEQ, 32767, fromU16((short) 0x7fff));
    }
    @Test
    public void fromU16middleNegative() {
        assertEquals(AM_NEQ, 32768, fromU16((short) 0x8000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toU16neg() {
        toU16(-1);
    }
    @Test(expected = IllegalArgumentException.class)
    public void toU16maxPlusOne() {
        toU16(65536);
    }

    @Test
    public void toU16zero() {
        assertEquals(AM_NEQ, (short)0x0, toU16(0));
    }
    @Test
    public void toU16max() {
        assertEquals(AM_NEQ, (short)0xffff, toU16(65535));
    }
    @Test
    public void toU16middlePositive() {
        assertEquals(AM_NEQ, (short)0x7fff, toU16(32767));
    }
    @Test
    public void toU16middleNegative() {
        assertEquals(AM_NEQ, (short)0x8000, toU16(32768));
    }

    // =========
    @Test
    public void fromU32zero() {
        assertEquals(AM_NEQ, 0, fromU32(0x0));
    }
    @Test
    public void fromU32max() {
        assertEquals(AM_NEQ, (long)65536*65536-1, fromU32(0xffffffff));
    }
    @Test
    public void fromU32middlePositive() {
        assertEquals(AM_NEQ, (long)65536*32768-1, fromU32(0x7fffffff));
    }
    @Test
    public void fromU32middleNegative() {
        assertEquals(AM_NEQ, (long)65536*32768, fromU32(0x80000000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toU32neg() {
        toU32(-1);
    }
    @Test(expected = IllegalArgumentException.class)
    public void toU32maxPlusOne() {
        toU32((long)65536*65536);
    }

    @Test
    public void toU32zero() {
        assertEquals(AM_NEQ, 0x0, toU32(0));
    }
    @Test
    public void toU32max() {
        assertEquals(AM_NEQ, 0xffffffff, toU32((long)65536*65536-1));
    }
    @Test
    public void toU32middlePositive() {
        assertEquals(AM_NEQ, 0x7fffffff, toU32((long)65536*32768-1));
    }
    @Test
    public void toU32middleNegative() {
        assertEquals(AM_NEQ, 0x80000000, toU32((long)65536*32768));
    }

    @Test
    public void sampleForJavaDoc() {
        print(EOL + "sampleForJavaDoc()");
        short s = 0x93;
        byte b = (byte) s;
        print("short is {}", s);
        print("byte is {}", b);
    }
    // =========

    // TODO: BigInteger tests on U64 conversions
}
