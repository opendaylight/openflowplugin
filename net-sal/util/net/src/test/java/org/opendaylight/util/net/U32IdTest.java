/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

/**
 * A base class for unsigned 8-bit identifiers.
 *
 * @author Simon Hunt
 */
public abstract class U32IdTest extends UnsignedIdTest {

    public static final long ID_OVER = 65536L * 65536L;
    public static final String ID_OVER_STR_DEC = "4294967296";

    public static final long ID_MAX = 4294967295L;
    public static final String ID_MAX_STR_DEC = "4294967295";
    public static final String ID_MAX_STR_HEX = "0xffffffff";

    // most significant bit set
    public static final long ID_HIGH = 2435007850L;
    public static final String ID_HIGH_STR_DEC = "2435007850";
    public static final String ID_HIGH_STR_HEX = "0x9123456a";
    public static final byte[] ID_HIGH_BYTES = {0x91-B, 0x23, 0x45, 0x6a};

    @Test(expected = IllegalArgumentException.class)
    public void rangeUnder() {
        U32Id.rangeCheck(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeOver() {
        U32Id.rangeCheck(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeOverStr() {
        long val = UnsignedId.parseLongStr(ID_OVER_STR_DEC);
        U32Id.rangeCheck(val);
    }

    @Test
    public void rangeCheckOk() {
        U32Id.rangeCheck(ID_MIN);
        U32Id.rangeCheck(ID_LOW);
        U32Id.rangeCheck(ID_HIGH);
        U32Id.rangeCheck(ID_MAX);
    }

    protected static final long[] UNSORTED = {
            0x5,
            0x3,
            0x200,
            0x127,
            0x42,
            0x65000,
            0x199,
            0x254,
            0x213,
            0x32111,
            0x8765,
            0xfffacb,
            0xffdacb,
            0xaa04,
    };

    protected static final long[] SORTED = {
            0x3,
            0x5,
            0x42,
            0x127,
            0x199,
            0x200,
            0x213,
            0x254,
            0x8765,
            0xaa04,
            0x32111,
            0x65000,
            0xffdacb,
            0xfffacb,
    };


}
