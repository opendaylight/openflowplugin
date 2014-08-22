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
public abstract class U16IdTest extends UnsignedIdTest {

    public static final int ID_OVER = 65536;
    public static final String ID_OVER_STR_DEC = "65536";

    public static final int ID_MAX = 65535;
    public static final String ID_MAX_STR_DEC = "65535";
    public static final String ID_MAX_STR_HEX = "0xffff";

    // most significant bit set
    public static final int ID_HIGH = 43969;
    public static final String ID_HIGH_STR_DEC = "43969";
    public static final String ID_HIGH_STR_HEX = "0xabc1";
    public static final byte[] ID_HIGH_BYTES = {0xab-B, 0xc1-B};

    @Test(expected = IllegalArgumentException.class)
    public void rangeUnder() {
        U16Id.rangeCheck(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeOver() {
        U16Id.rangeCheck(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeOverStr() {
        int val = UnsignedId.parseIntStr(ID_OVER_STR_DEC);
        U16Id.rangeCheck(val);
    }

    @Test
    public void rangeCheckOk() {
        U16Id.rangeCheck(ID_MIN);
        U16Id.rangeCheck(ID_LOW);
        U16Id.rangeCheck(ID_HIGH);
        U16Id.rangeCheck(ID_MAX);
    }

    protected static final int[] UNSORTED = {
            5,
            3,
            200,
            127,
            42,
            65000,
            199,
            254,
            213,
            32111,
            8765,
    };

    protected static final int[] SORTED = {
            3,
            5,
            42,
            127,
            199,
            200,
            213,
            254,
            8765,
            32111,
            65000,
    };

}

