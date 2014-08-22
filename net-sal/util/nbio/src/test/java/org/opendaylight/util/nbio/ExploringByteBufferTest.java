/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio;

import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * A set of unit tests to explore and understand the {@link ByteBuffer} class.
 *
 * @author Simon Hunt
 */
public class ExploringByteBufferTest {
    private static final String FMT_EX = "EX> {}";

    private static final byte BYTE_FE = (byte) 0xfe;
    private static final short SHORT_FOOD = (short) 0xf00d;
    private static final int INT_CAFEBABE = 0xcafebabe;
    private static final long LONG_BADFOOD = 0xbadf00d;
    private static final byte[] ARRAY_1234 = {0x01, 0x02, 0x03, 0x04};

    private ByteBuffer bb;

    private void verifyPosLimCap(int expPos, int expLim, int expCap) {
        print(bb);
        assertEquals(AM_NEQ, expPos, bb.position());
        assertEquals(AM_UXS, expLim, bb.limit());
        assertEquals(AM_UXS, expCap, bb.capacity());
    }

    private void verifyRemaining(int expRem) {
        assertEquals(AM_NEQ, expRem, bb.remaining());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        bb = ByteBuffer.allocate(20);
        verifyPosLimCap(0, 20, 20);

        bb.put(BYTE_FE);
        verifyPosLimCap(1, 20, 20);

        bb.putShort(SHORT_FOOD);
        verifyPosLimCap(3, 20, 20);

        bb.putInt(INT_CAFEBABE);
        verifyPosLimCap(7, 20, 20);

        bb.putLong(LONG_BADFOOD);
        verifyPosLimCap(15, 20, 20);

        bb.put(ARRAY_1234);
        verifyPosLimCap(19, 20, 20);

        // try putting more than we have room for
        try {
            bb.putShort(SHORT_FOOD);
            fail(AM_NOEX);
        } catch (BufferOverflowException e) {
            print(FMT_EX, e);
        }

        // switch to "output" mode
        bb.flip();
        verifyPosLimCap(0, 19, 20);

        byte b = bb.get();
        verifyPosLimCap(1, 19, 20);
        assertEquals(AM_NEQ, BYTE_FE, b);

        short s = bb.getShort();
        verifyPosLimCap(3, 19, 20);
        assertEquals(AM_NEQ, SHORT_FOOD, s);

        int i = bb.getInt();
        verifyPosLimCap(7, 19, 20);
        assertEquals(AM_NEQ, INT_CAFEBABE, i);

        long l = bb.getLong();
        verifyPosLimCap(15, 19, 20);
        assertEquals(AM_NEQ, LONG_BADFOOD, l);

        byte[] a = new byte[ARRAY_1234.length];
        bb.get(a);
        verifyPosLimCap(19, 19, 20);
        assertArrayEquals(AM_NEQ, ARRAY_1234, a);

        // er.. let's rewind a bit and read those bytes as an int
        bb.position(bb.position() - 4);
        int ai = bb.getInt();
        verifyPosLimCap(19, 19, 20);
        assertEquals(AM_NEQ, 0x01020304, ai);

        // try and get beyond the boundary
        try {
            short tooMuch = bb.getShort();
            fail(AM_NOEX);
        } catch (BufferUnderflowException e) {
            print(FMT_EX, e);
        }
    }

    @Test
    public void compactOrClear() {
        print("compactOrClear()");
        bb = ByteBuffer.allocate(10);
        verifyPosLimCap(0, 10, 10);
        verifyRemaining(10);
        bb.putShort(SHORT_FOOD);
        verifyPosLimCap(2, 10, 10);
        verifyRemaining(8);
        bb.putShort(SHORT_FOOD); // double rations
        verifyPosLimCap(4, 10, 10);
        verifyRemaining(6);

        // switch to reading (draining) mode
        bb.flip();
        verifyPosLimCap(0, 4, 10);
        verifyRemaining(4);
        short ration = bb.getShort();
        assertEquals(AM_NEQ, SHORT_FOOD, ration);
        verifyPosLimCap(2, 4, 10); // leaves 2 unread bytes
        verifyRemaining(2);
        bb.compact(); // shifts unread bytes to [0..n-1], ready to write
        verifyPosLimCap(2, 10, 10);
        verifyRemaining(8);

        // more food, but this time consume it all
        bb.putShort(SHORT_FOOD);
        verifyPosLimCap(4, 10, 10);
        verifyRemaining(6);
        bb.flip();
        verifyPosLimCap(0, 4, 10);
        verifyRemaining(4);
        int doubleRation = bb.getInt();
        verifyPosLimCap(4, 4, 10);
        verifyRemaining(0);

        // compacting an empty buffer is equivalent to clearing...
        bb.compact();
        verifyPosLimCap(0, 10, 10);
    }
}
