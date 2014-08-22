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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for BufferId.
 *
 * @author Simon Hunt
 */
public class BufferIdTest extends U32IdTest {

    @SuppressWarnings("hiding")
    public static final long ID_MAX = 4294967294L;
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_DEC = "4294967294";
    @SuppressWarnings("hiding")
    public static final String ID_MAX_STR_HEX = "0xfffffffe";

    // NO_BUFFER values
    public static final long ID_NB = 4294967295L;
    public static final String ID_NB_STR_DEC = "4294967295";
    public static final String ID_NB_STR_HEX = "0xffffffff";
    public static final String ID_NB_STR_HEX_PLUS = "0xffffffff(NO_BUFFER)";


    private BufferId bid;
    private BufferId bidAlt;

    @Test
    public void min() {
        bid = BufferId.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, bid.toLong());
        assertEquals(AM_NEQ, ID_MIN_STR_HEX, bid.toString());
        bidAlt = BufferId.valueOf(ID_MIN_STR_DEC);
        assertEquals(AM_NSR, bid, bidAlt);
        bidAlt = BufferId.valueOf(ID_MIN_STR_HEX);
        assertEquals(AM_NSR, bid, bidAlt);
    }

    @Test
    public void low() {
        bid = BufferId.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, bid.toLong());
        assertEquals(AM_NEQ, ID_LOW_STR_HEX, bid.toString());
        bidAlt = BufferId.valueOf(ID_LOW_STR_DEC);
        assertEquals(AM_NSR, bid, bidAlt);
        bidAlt = BufferId.valueOf(ID_LOW_STR_HEX);
        assertEquals(AM_NSR, bid, bidAlt);
    }

    @Test
    public void high() {
        bid = BufferId.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, bid.toLong());
        assertEquals(AM_NEQ, ID_HIGH_STR_HEX, bid.toString());
        bidAlt = BufferId.valueOf(ID_HIGH_STR_DEC);
        assertEquals(AM_NSR, bid, bidAlt);
        bidAlt = BufferId.valueOf(ID_HIGH_STR_HEX);
        assertEquals(AM_NSR, bid, bidAlt);
    }

    @Test
    public void max() {
        bid = BufferId.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, bid.toLong());
        assertEquals(AM_NEQ, ID_MAX_STR_HEX, bid.toString());
        bidAlt = BufferId.valueOf(ID_MAX_STR_DEC);
        assertEquals(AM_NSR, bid, bidAlt);
        bidAlt = BufferId.valueOf(ID_MAX_STR_HEX);
        assertEquals(AM_NSR, bid, bidAlt);
    }

    @Test
    public void noBuffer() {
        bid = BufferId.valueOf(ID_NB);
        assertEquals(AM_NEQ, ID_NB, bid.toLong());
        assertEquals(AM_NEQ, ID_NB_STR_HEX_PLUS, bid.toString());
        bidAlt = BufferId.valueOf(ID_NB_STR_DEC);
        assertEquals(AM_NSR, bid, bidAlt);
        bidAlt = BufferId.valueOf(ID_NB_STR_HEX);
        assertEquals(AM_NSR, bid, bidAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringId() {
        bid = BufferId.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayId() {
        bid = BufferId.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayId() {
        bid = BufferId.valueOf(new byte[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayId() {
        bid = BufferId.valueOf(new byte[5]);
    }

    @Test
    public void fromBytesHigh() {
        bid = BufferId.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, bid.toLong());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = BufferId.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void under() {
        bid = BufferId.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void over() {
        bid = BufferId.valueOf(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overStr() {
        bid = BufferId.valueOf(ID_OVER_STR_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fooeyErrorMsg() {
            bid = BufferId.valueOf(FOOEY);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        int count = UNSORTED.length;
        BufferId[] bids = new BufferId[count];
        for (int i=0; i<count; i++) {
            bids[i] = BufferId.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(bids));
        Arrays.sort(bids);
        print("Sorted...");
        print(Arrays.toString(bids));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], bids[i].toLong());
        }
    }
    
    private static final String SOME_VALUE = "37";
    
    @Test
    public void convenience() {
        print(EOL + "convenience()");
        bid = BufferId.bid(SOME_VALUE);
        bidAlt = BufferId.valueOf(SOME_VALUE);
        assertEquals(AM_NEQ, bidAlt, bid);
    }

}
