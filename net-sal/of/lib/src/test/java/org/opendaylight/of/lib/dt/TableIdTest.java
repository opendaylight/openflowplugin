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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for TableId.
 *
 * @author Simon Hunt
 */
public class TableIdTest extends U8IdTest {

    // slight alteration of the notion of MAX
    public static final int ID_ALL = 255;
    public static final String ID_ALL_STR_DEC = "255";
    public static final String ID_ALL_STR_HEX = "0xff";
    public static final byte ID_ALL_BYTE = (byte) 0xff;

    @SuppressWarnings("hiding")
    public static final int ID_MAX = 254;
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_DEC = "254";
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_HEX = "0xfe";
    @SuppressWarnings("hiding")
    public static final byte ID_MAX_BYTE = (byte) 0xfe;


    private TableId tid;
    private TableId tidAlt;

    @Test
    public void min() {
        tid = TableId.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, tid.toInt());
        assertEquals(AM_NEQ, ID_MIN_STR_DEC, tid.toString());
        tidAlt = TableId.valueOf(ID_MIN_STR_DEC);
        assertSame(AM_NSR, tid, tidAlt);
        tidAlt = TableId.valueOf(ID_MIN_STR_HEX);
        assertSame(AM_NSR, tid, tidAlt);
    }

    @Test
    public void low() {
        tid = TableId.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, tid.toInt());
        assertEquals(AM_NEQ, ID_LOW_STR_DEC, tid.toString());
        tidAlt = TableId.valueOf(ID_LOW_STR_DEC);
        assertSame(AM_NSR, tid, tidAlt);
        tidAlt = TableId.valueOf(ID_LOW_STR_HEX);
        assertSame(AM_NSR, tid, tidAlt);
    }

    @Test
    public void high() {
        tid = TableId.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, tid.toInt());
        assertEquals(AM_NEQ, ID_HIGH_STR_DEC, tid.toString());
        tidAlt = TableId.valueOf(ID_HIGH_STR_DEC);
        assertSame(AM_NSR, tid, tidAlt);
        tidAlt = TableId.valueOf(ID_HIGH_STR_HEX);
        assertSame(AM_NSR, tid, tidAlt);
    }

    @Test
    public void max() {
        tid = TableId.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, tid.toInt());
        assertEquals(AM_NEQ, "254(MAX)", tid.toString());
        tidAlt = TableId.valueOf(ID_MAX_STR_DEC);
        assertSame(AM_NSR, tid, tidAlt);
        tidAlt = TableId.valueOf(ID_MAX_STR_HEX);
        assertSame(AM_NSR, tid, tidAlt);
    }

    @Test
    public void all() {
        tid = TableId.valueOf(ID_ALL);
        assertEquals(AM_NEQ, ID_ALL, tid.toInt());
        assertEquals(AM_NEQ, "255(ALL)", tid.toString());
        tidAlt = TableId.valueOf(ID_ALL_STR_DEC);
        assertSame(AM_NSR, tid, tidAlt);
        tidAlt = TableId.valueOf(ID_ALL_STR_HEX);
        assertSame(AM_NSR, tid, tidAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringBid() {
        tid = TableId.valueOf(null);
    }

    @Test
    public void fromByteMax() {
        tid = TableId.valueOf(ID_MAX_BYTE);
        assertEquals(AM_NEQ, ID_MAX, tid.toInt());
    }

    @Test
    public void toByteMax() {
        byte b = TableId.valueOf(ID_MAX).toByte();
        assertEquals(AM_NEQ, ID_MAX_BYTE, b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void under() {
        tid = TableId.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void over() {
        tid = TableId.valueOf(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overStr() {
        tid = TableId.valueOf(ID_OVER_STR_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fooeyErrorMsg() {
        tid = TableId.valueOf(FOOEY);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        int count = UNSORTED.length;
        TableId[] gids = new TableId[count];
        for (int i=0; i<count; i++) {
            gids[i] = TableId.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(gids));
        Arrays.sort(gids);
        print("Sorted...");
        print(Arrays.toString(gids));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], gids[i].toInt());
        }
    }
    
    private static final String SOME_VAL = "43";
    
    @Test
    public void convenience() {
        assertEquals(AM_NEQ, TableId.valueOf(SOME_VAL), TableId.tid(SOME_VAL));
    }

}
