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
public abstract class U8IdTest extends UnsignedIdTest {

    public static final int ID_OVER = 256;
    public static final String ID_OVER_STR_DEC = "256";

    public static final int ID_MAX = 255;
    public static final String ID_MAX_STR_DEC = "255";
    public static final String ID_MAX_STR_HEX = "0xff";

    // most significant bit set
    public static final int ID_HIGH = 200;
    public static final String ID_HIGH_STR_DEC = "200";
    public static final String ID_HIGH_STR_HEX = "0xc8";

    @Test(expected = IllegalArgumentException.class)
    public void rangeUnder() {
        U8Id.rangeCheck(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeOver() {
        U8Id.rangeCheck(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rangeOverStr() {
        int val = UnsignedId.parseIntStr(ID_OVER_STR_DEC);
        U8Id.rangeCheck(val);
    }

    @Test
    public void rangeCheckOk() {
        U8Id.rangeCheck(ID_MIN);
        U8Id.rangeCheck(ID_LOW);
        U8Id.rangeCheck(ID_HIGH);
        U8Id.rangeCheck(ID_MAX);
    }

    protected static final int[] UNSORTED = {
            5,
            3,
            200,
            127,
            42,
            69,
            199,
            254,
            213,
    };
    protected static final int[] SORTED = {
            3,
            5,
            42,
            69,
            127,
            199,
            200,
            213,
            254,
    };


}
