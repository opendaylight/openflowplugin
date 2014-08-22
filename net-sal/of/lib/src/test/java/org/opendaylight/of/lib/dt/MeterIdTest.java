/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MeterId.
 *
 * @author Simon Hunt
 */
public class MeterIdTest extends U32IdTest {

    // MAX value for meter
    @SuppressWarnings("hiding")
    public static final long ID_MAX = 0xffff0000L;
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_DEC = "4294901760";
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_HEX = "0xffff0000";
    public static final String ID_MAX_TOSTR = "0xffff0000(MAX)";

    public static final long ID_SLOWPATH = 4294967293L;
    public static final String ID_SLOWPATH_STR_DEC = "4294967293";
    public static final String ID_SLOWPATH_STR_HEX = "0xfffffffd";
    public static final String ID_SLOWPATH_TOSTR = "0xfffffffd(SLOWPATH)";

    public static final long ID_CONTROLLER = 4294967294L;
    public static final String ID_CONTROLLER_STR_DEC = "4294967294";
    public static final String ID_CONTROLLER_STR_HEX = "0xfffffffe";
    public static final String ID_CONTROLLER_TOSTR = "0xfffffffe(CONTROLLER)";

    public static final long ID_ALL = 4294967295L;
    public static final String ID_ALL_STR_DEC = "4294967295";
    public static final String ID_ALL_STR_HEX = "0xffffffff";
    public static final String ID_ALL_TOSTR = "0xffffffff(ALL)";


    private MeterId mid;
    private MeterId midAlt;

    @Test
    public void min() {
        mid = MeterId.valueOf(ID_MIN);
        print(mid);
        assertEquals(AM_NEQ, ID_MIN, mid.toLong());
        assertEquals(AM_NEQ, ID_MIN_STR_HEX, mid.toString());
        midAlt = MeterId.valueOf(ID_MIN_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_MIN_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }

    @Test
    public void low() {
        mid = MeterId.valueOf(ID_LOW);
        print(mid);
        assertEquals(AM_NEQ, ID_LOW, mid.toLong());
        assertEquals(AM_NEQ, ID_LOW_STR_HEX, mid.toString());
        midAlt = MeterId.valueOf(ID_LOW_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_LOW_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }

    @Test
    public void high() {
        mid = MeterId.valueOf(ID_HIGH);
        print(mid);
        assertEquals(AM_NEQ, ID_HIGH, mid.toLong());
        assertEquals(AM_NEQ, ID_HIGH_STR_HEX, mid.toString());
        midAlt = MeterId.valueOf(ID_HIGH_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_HIGH_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }

    @Test
    public void max() {
        mid = MeterId.valueOf(ID_MAX);
        print(mid);
        assertEquals(AM_NEQ, ID_MAX, mid.toLong());
        assertEquals(AM_NEQ, ID_MAX_TOSTR, mid.toString());
        midAlt = MeterId.valueOf(ID_MAX_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_MAX_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }


    @Test
    public void slowpath() {
        mid = MeterId.valueOf(ID_SLOWPATH);
        print(mid);
        assertEquals(AM_NEQ, ID_SLOWPATH, mid.toLong());
        assertEquals(AM_NEQ, ID_SLOWPATH_TOSTR, mid.toString());
        midAlt = MeterId.valueOf(ID_SLOWPATH_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_SLOWPATH_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }


    @Test
    public void controller() {
        mid = MeterId.valueOf(ID_CONTROLLER);
        print(mid);
        assertEquals(AM_NEQ, ID_CONTROLLER, mid.toLong());
        assertEquals(AM_NEQ, ID_CONTROLLER_TOSTR, mid.toString());
        midAlt = MeterId.valueOf(ID_CONTROLLER_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_CONTROLLER_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }


    @Test
    public void all() {
        mid = MeterId.valueOf(ID_ALL);
        print(mid);
        assertEquals(AM_NEQ, ID_ALL, mid.toLong());
        assertEquals(AM_NEQ, ID_ALL_TOSTR, mid.toString());
        midAlt = MeterId.valueOf(ID_ALL_STR_DEC);
        assertSame(AM_NSR, mid, midAlt);
        midAlt = MeterId.valueOf(ID_ALL_STR_HEX);
        assertSame(AM_NSR, mid, midAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringId() {
        mid = MeterId.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayId() {
        mid = MeterId.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayId() {
        mid = MeterId.valueOf(new byte[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayId() {
        mid = MeterId.valueOf(new byte[5]);
    }

    @Test
    public void fromBytesHigh() {
        mid = MeterId.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, mid.toLong());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = MeterId.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void under() {
        mid = MeterId.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void over() {
        mid = MeterId.valueOf(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overStr() {
        mid = MeterId.valueOf(ID_OVER_STR_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fooeyErrorMsg() {
        mid = MeterId.valueOf(FOOEY);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        int count = UNSORTED.length;
        MeterId[] mids = new MeterId[count];
        for (int i=0; i<count; i++) {
            mids[i] = MeterId.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(mids));
        Arrays.sort(mids);
        print("Sorted...");
        print(Arrays.toString(mids));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], mids[i].toLong());
        }
    }

    private static final String SOME_VAL = "13";
    
    @Test
    public void convenience() {
        assertEquals(AM_NEQ, MeterId.valueOf(SOME_VAL), MeterId.mid(SOME_VAL));
    }
    
}
