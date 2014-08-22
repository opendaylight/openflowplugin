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
 * This class defines unit tests for {@link PortNumber}.
 *
 * @author Simon Hunt
 */
public class PortNumberTest extends U16IdTest {

    private PortNumber pn;
    private PortNumber pnAlt;

    @Test
    public void portMin() {
        pn = PortNumber.valueOf(ID_MIN);
        assertEquals(AM_NEQ, ID_MIN, pn.toInt());
        assertEquals(AM_NEQ, ID_MIN_STR_DEC, pn.toString());
        pnAlt = PortNumber.valueOf(ID_MIN_STR_DEC);
        assertSame(AM_NSR, pn, pnAlt);
        pnAlt = PortNumber.valueOf(ID_MIN_STR_HEX);
        assertSame(AM_NSR, pn, pnAlt);
    }

    @Test
    public void portLow() {
        pn = PortNumber.valueOf(ID_LOW);
        assertEquals(AM_NEQ, ID_LOW, pn.toInt());
        assertEquals(AM_NEQ, ID_LOW_STR_DEC, pn.toString());
        pnAlt = PortNumber.valueOf(ID_LOW_STR_DEC);
        assertSame(AM_NSR, pn, pnAlt);
        pnAlt = PortNumber.valueOf(ID_LOW_STR_HEX);
        assertSame(AM_NSR, pn, pnAlt);
    }

    @Test
    public void portHigh() {
        pn = PortNumber.valueOf(ID_HIGH);
        assertEquals(AM_NEQ, ID_HIGH, pn.toInt());
        assertEquals(AM_NEQ, ID_HIGH_STR_DEC, pn.toString());
        pnAlt = PortNumber.valueOf(ID_HIGH_STR_DEC);
        assertSame(AM_NSR, pn, pnAlt);
        pnAlt = PortNumber.valueOf(ID_HIGH_STR_HEX);
        assertSame(AM_NSR, pn, pnAlt);
    }

    @Test
    public void portMax() {
        pn = PortNumber.valueOf(ID_MAX);
        assertEquals(AM_NEQ, ID_MAX, pn.toInt());
        assertEquals(AM_NEQ, ID_MAX_STR_DEC, pn.toString());
        pnAlt = PortNumber.valueOf(ID_MAX_STR_DEC);
        assertSame(AM_NSR, pn, pnAlt);
        pnAlt = PortNumber.valueOf(ID_MAX_STR_HEX);
        assertSame(AM_NSR, pn, pnAlt);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringPort() {
        pn = PortNumber.valueOf((String)null);
    }

    @Test(expected = NullPointerException.class)
    public void nullByteArrayPort() {
        pn = PortNumber.valueOf((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shortByteArrayPort() {
        pn = PortNumber.valueOf(new byte[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void longByteArrayPort() {
        pn = PortNumber.valueOf(new byte[3]);
    }

    @Test
    public void fromBytesHigh() {
        pn = PortNumber.valueOf(ID_HIGH_BYTES);
        assertEquals(AM_NEQ, ID_HIGH, pn.toInt());
    }

    @Test
    public void toBytesHigh() {
        byte[] bytes = PortNumber.valueOf(ID_HIGH).toByteArray();
        assertArrayEquals(AM_NEQ, ID_HIGH_BYTES, bytes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void portUnder() {
        pn = PortNumber.valueOf(ID_UNDER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void portOver() {
        pn = PortNumber.valueOf(ID_OVER);
    }

    @Test
    public void fooeyErrorMsg() {
        try {
            pn = PortNumber.valueOf(FOOEY);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertEquals(AM_NEQ, UnsignedId.E_BAD + FOOEY, e.getMessage());
        }
    }

    @Test
    public void compare() {
        int count = UNSORTED.length;
        PortNumber[] ports = new PortNumber[count];
        for (int i=0; i<count; i++) {
            ports[i] = PortNumber.valueOf(UNSORTED[i]);
        }
        print("Unsorted...");
        print(Arrays.toString(ports));
        Arrays.sort(ports);
        print("Sorted...");
        print(Arrays.toString(ports));
        for (int i=0; i<count; i++) {
            assertEquals(AM_NEQ, SORTED[i], ports[i].toInt());
        }
    }

    @Test
    public void eq() {
        pn = PortNumber.valueOf(ID_HIGH);
        pnAlt = PortNumber.valueOf(ID_HIGH_STR_HEX);
        verifyEqual(pn, pnAlt);
    }

}
