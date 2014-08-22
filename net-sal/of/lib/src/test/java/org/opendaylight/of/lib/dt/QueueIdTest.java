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
 * Unit tests for QueueId.
 *
 * @author Simon Hunt
 */
public class QueueIdTest extends U32IdTest {

    public static final String ID_MAX_STR_HEX_PLUS = "0xffffffff(ALL)";

    private QueueId qid;
    private QueueId qidAlt;

    @Test
    public void min() {
        qid = QueueId.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, qid.toLong());
        assertEquals(AM_NEQ, ID_MIN_STR_HEX_PLUS, qid.toString());
        qidAlt = QueueId.valueOf(ID_MIN_STR_DEC);
        assertSame(AM_NSR, qid, qidAlt);
        qidAlt = QueueId.valueOf(ID_MIN_STR_HEX);
        assertSame(AM_NSR, qid, qidAlt);
    }

    @Test
    public void low() {
        qid = QueueId.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, qid.toLong());
        assertEquals(AM_NEQ, ID_LOW_STR_HEX_PLUS, qid.toString());
        qidAlt = QueueId.valueOf(ID_LOW_STR_DEC);
        assertSame(AM_NSR, qid, qidAlt);
        qidAlt = QueueId.valueOf(ID_LOW_STR_HEX);
        assertSame(AM_NSR, qid, qidAlt);
    }

    @Test
    public void high() {
        qid = QueueId.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, qid.toLong());
        assertEquals(AM_NEQ, ID_HIGH_STR_HEX_PLUS, qid.toString());
        qidAlt = QueueId.valueOf(ID_HIGH_STR_DEC);
        assertSame(AM_NSR, qid, qidAlt);
        qidAlt = QueueId.valueOf(ID_HIGH_STR_HEX);
        assertSame(AM_NSR, qid, qidAlt);
    }

    @Test
    public void max() {
        qid = QueueId.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, qid.toLong());
        assertEquals(AM_NEQ, ID_MAX_STR_HEX_PLUS, qid.toString());
        qidAlt = QueueId.valueOf(ID_MAX_STR_DEC);
        assertSame(AM_NSR, qid, qidAlt);
        qidAlt = QueueId.valueOf(ID_MAX_STR_HEX);
        assertSame(AM_NSR, qid, qidAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringId() {
        qid = QueueId.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayId() {
        qid = QueueId.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayId() {
        qid = QueueId.valueOf(new byte[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayId() {
        qid = QueueId.valueOf(new byte[5]);
    }

    @Test
    public void fromBytesHigh() {
        qid = QueueId.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, qid.toLong());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = QueueId.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void under() {
        qid = QueueId.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void over() {
        qid = QueueId.valueOf(ID_OVER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void overStr() {
        qid = QueueId.valueOf(ID_OVER_STR_DEC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void fooeyErrorMsg() {
        qid = QueueId.valueOf(FOOEY);
    }

    @Test
    public void compare() {
        print(EOL + "compare()");
        int count = UNSORTED.length;
        QueueId[] qids = new QueueId[count];
        for (int i=0; i<count; i++) {
            qids[i] = QueueId.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(qids));
        Arrays.sort(qids);
        print("Sorted...");
        print(Arrays.toString(qids));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], qids[i].toLong());
        }
    }

    private static final String SOME_VAL = "11101";
    
    @Test
    public void convenience() {
        assertEquals(AM_NEQ, QueueId.valueOf(SOME_VAL), QueueId.qid(SOME_VAL));
    }
}
