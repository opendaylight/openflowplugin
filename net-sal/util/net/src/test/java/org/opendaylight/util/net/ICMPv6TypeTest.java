/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Unit tests for ICMPv6Type
 *
 * @author Simon Hunt
 */
public class ICMPv6TypeTest {

    private ICMPv6Type it;

    @Test
    public void nbrSol() {
        print(EOL + "nbrSol()");
        it = ICMPv6Type.valueOf("nbr_sol");
        print(it);
        assertSame(AM_NSR, ICMPv6Type.NBR_SOL, it);
        it = ICMPv6Type.valueOf(135);
        print(it);
        assertSame(AM_NSR, ICMPv6Type.NBR_SOL, it);
    }

    @Test
    public void nbrAdv() {
        print(EOL + "nbrAdv()");
        it = ICMPv6Type.valueOf("NBR_ADV");
        print(it);
        assertSame(AM_NSR, ICMPv6Type.NBR_ADV, it);
        it = ICMPv6Type.valueOf(136);
        print(it);
        assertSame(AM_NSR, ICMPv6Type.NBR_ADV, it);
    }

    // see icmpv6Type.properties to confirm ranges
    private static final int[] UNK_THRESH = {
            0, 1, 5, 100, 102, 127, 155, 200, 202, 255, 256
    };

    @Test
    public void unknownRanges() {
        print(EOL + "unknownRanges()");
        boolean expUnknown = true;
        for (int i=0; i<UNK_THRESH.length-1; i++) {
            int start = UNK_THRESH[i];
            int end = UNK_THRESH[i+1];
            for (int code=start; code<end; code++) {
                it = ICMPv6Type.valueOf(code);
                print(it);
                assertEquals("unknown mismatch", expUnknown, it.isUnknown());
            }
            expUnknown = !expUnknown;
        }
    }

    private static final String[] HAAD_STR = {
            "HAAD_REQ : Home Agent Address Discovery Request Message",
            "haad_req : home agent address discovery request message",
            "haad_req",
            "HAAD_REQ",
            "Haad_Req",
    };

    @Test
    public void valueOfStringIgnoreCase() {
        print(EOL + "valueOfStringIgnoreCase()");
        for (String s: HAAD_STR) {
            it = ICMPv6Type.valueOf(s);
            print("{} --> {}", s, it);
        }
    }

    //=== Check for exceptions ===

    @Test (expected=NullPointerException.class)
    public void valueOfStringNull() {
        ICMPv6Type.valueOf(null);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringEmptyString() {
        ICMPv6Type.valueOf("");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringXyyzy() {
        ICMPv6Type.valueOf("xyyzy");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfStringUnknown() {
        ICMPv6Type.valueOf("Unknown");
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooSmall() {
        ICMPv6Type.valueOf(-1);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooBig() {
        ICMPv6Type.valueOf(256);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooSmallMin() {
        ICMPv6Type.valueOf(Integer.MIN_VALUE);
    }

    @Test (expected=IllegalArgumentException.class)
    public void valueOfIntTooBigMax() {
        ICMPv6Type.valueOf(Integer.MAX_VALUE);
    }

}
