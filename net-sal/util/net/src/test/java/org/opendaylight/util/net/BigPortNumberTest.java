/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This class defines unit tests for {@link BigPortNumber}.
 *
 * @author Simon Hunt
 */
public class BigPortNumberTest extends U32IdTest {

    private BigPortNumber bpn;
    private BigPortNumber bpnAlt;

    @Test
    public void portMin() {
        bpn = BigPortNumber.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, bpn.toLong());
        assertEquals(AM_NEQ, ID_MIN_STR_HEX, bpn.toString());
        bpnAlt = BigPortNumber.valueOf(ID_MIN_STR_DEC);
        assertEquals(AM_NSR, bpn, bpnAlt);
        bpnAlt = BigPortNumber.valueOf(ID_MIN_STR_HEX);
        assertEquals(AM_NSR, bpn, bpnAlt);
    }

    @Test
    public void portLow() {
        bpn = BigPortNumber.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, bpn.toLong());
        assertEquals(AM_NEQ, ID_LOW_STR_HEX, bpn.toString());
        bpnAlt = BigPortNumber.valueOf(ID_LOW_STR_DEC);
        assertEquals(AM_NSR, bpn, bpnAlt);
        bpnAlt = BigPortNumber.valueOf(ID_LOW_STR_HEX);
        assertEquals(AM_NSR, bpn, bpnAlt);
    }

    @Test
    public void portHigh() {
        bpn = BigPortNumber.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, bpn.toLong());
        assertEquals(AM_NEQ, ID_HIGH_STR_HEX, bpn.toString());
        bpnAlt = BigPortNumber.valueOf(ID_HIGH_STR_DEC);
        assertEquals(AM_NSR, bpn, bpnAlt);
        bpnAlt = BigPortNumber.valueOf(ID_HIGH_STR_HEX);
        assertEquals(AM_NSR, bpn, bpnAlt);
    }

    @Test
    public void portMax() {
        bpn = BigPortNumber.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, bpn.toLong());
        assertEquals(AM_NEQ, ID_MAX_STR_HEX, bpn.toString());
        bpnAlt = BigPortNumber.valueOf(ID_MAX_STR_DEC);
        assertEquals(AM_NSR, bpn, bpnAlt);
        bpnAlt = BigPortNumber.valueOf(ID_MAX_STR_HEX);
        assertEquals(AM_NSR, bpn, bpnAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringPort() {
        bpn = BigPortNumber.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayPort() {
        bpn = BigPortNumber.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayPort() {
        bpn = BigPortNumber.valueOf(new byte[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayPort() {
        bpn = BigPortNumber.valueOf(new byte[3]);
    }

    @Test
    public void fromBytesHigh() {
        bpn = BigPortNumber.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, bpn.toLong());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = BigPortNumber.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void portUnder() {
        bpn = BigPortNumber.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void portOver() {
        bpn = BigPortNumber.valueOf(ID_OVER);
    }

    @Test
    public void fooeyErrorMsg() {
        try {
            bpn = BigPortNumber.valueOf(FOOEY);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertEquals(AM_NEQ, UnsignedId.E_BAD + FOOEY, e.getMessage());
        }
    }

    @Test
    public void compare() {
        int count = UNSORTED.length;
        BigPortNumber[] ports = new BigPortNumber[count];
        for (int i=0; i<count; i++) {
            ports[i] = BigPortNumber.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(ports));
        Arrays.sort(ports);
        print("Sorted...");
        print(Arrays.toString(ports));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], ports[i].toLong());
        }
    }

    @Test
    public void eq() {
        bpn = BigPortNumber.valueOf(ID_HIGH);
        bpnAlt = BigPortNumber.valueOf(ID_HIGH_STR_HEX);
        verifyEqual(bpn, bpnAlt);
    }
    
    private static final long SOME_LONG = 10;
    private static final String SOME_STRING = "0xa";
            
    @Test
    public void convenienceLong() {
        bpn = BigPortNumber.valueOf(SOME_LONG);
        assertEquals(AM_NEQ, bpn, BigPortNumber.bpn(SOME_LONG));
        assertEquals(AM_NEQ, bpn, BigPortNumber.bpn(SOME_STRING));
    }

}
