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
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for ICMPv4Type.
 *
 * @author Simon Hunt
 */
public class ICMPv4TypeTest {

    private ICMPv4Type it;

    private static final Integer[] UNKNOWNS = {1, 2, 7};
    private static final Set<Integer> UNKNOWNS_SET =
            new HashSet<Integer>(Arrays.asList(UNKNOWNS));

    private static final int[] RESERVED = {
        // first, last(inclusive)
            20, 29,
            42, 255,
    };

    private boolean expReserved(int code) {
        boolean result = false;
        for (int i=0; i<RESERVED.length; i+=2) {
            if (code >= RESERVED[i] && code <= RESERVED[i+1])
                result = true;
        }
        return result;
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        for (int code=0; code<256; code++) {
            it = ICMPv4Type.valueOf(code);
            print("{} -> {}", code, it);
            assertEquals(AM_NEQ, UNKNOWNS_SET.contains(code), it.isUnknown());
            assertEquals(AM_NEQ, expReserved(code), it.isReserved());
        }
    }

    @Test
    public void echoRep() {
        print(EOL + "echoRep()");
        it = ICMPv4Type.valueOf("echo_rep");
        print(it);
        assertSame(AM_NSR, ICMPv4Type.ECHO_REP, it);
        it = ICMPv4Type.valueOf(0);
        print(it);
        assertSame(AM_NSR, ICMPv4Type.ECHO_REP, it);
    }

    //=== Check for exceptions ===

    @Test (expected=NullPointerException.class)
    public void valueOfStringNull() {
        ICMPv4Type.valueOf(null);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringEmptyString() {
        ICMPv4Type.valueOf("");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringXyyzy() {
        ICMPv4Type.valueOf("xyyzy");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringUnknown() {
        ICMPv4Type.valueOf("Unknown");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooSmall() {
        ICMPv4Type.valueOf(-1);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooBig() {
        ICMPv4Type.valueOf(256);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooSmallMin() {
        ICMPv4Type.valueOf(Integer.MIN_VALUE);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooBigMax() {
        ICMPv4Type.valueOf(Integer.MAX_VALUE);
    }

}
