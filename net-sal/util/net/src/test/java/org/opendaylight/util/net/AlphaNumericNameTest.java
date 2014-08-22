/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
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
 * This class implements unit tests for {@link AlphaNumericName}.
 *
 * @author Simon Hunt
 */
public class AlphaNumericNameTest {

    @Test (expected = NullPointerException.class)
    public void valueOfNull() {
        AlphaNumericName.valueOf(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void valueOfEmptyString() {
        AlphaNumericName.valueOf("");
    }



    @Test
    public void uniqueRefs() {
        AlphaNumericName p1 = AlphaNumericName.valueOf("A11");
        StringBuilder sb = new StringBuilder("A").append(11);
        AlphaNumericName p2 = AlphaNumericName.valueOf(sb.toString());
        assertSame(AM_HUH, p1, p2);
        assertSame(AM_HUH, p1, AlphaNumericName.valueOf(S_A11));
    }


    private static final String S_A1 = "A1";
    private static final String S_A2 = "A2";
    private static final String S_A7 = "A7";
    private static final String S_A11 = "A11";
    private static final String S_A20 = "A20";

    private static final String[] UNSORTED = {
            S_A11,
            S_A7,
            S_A2,
            S_A1,
            S_A20,
    };

    private static final String[] SORTED = {
            S_A1,
            S_A2,
            S_A7,
            S_A11,
            S_A20,
    };

    @Test
    public void comparator() {
        for (int i=0; i<SORTED.length-1; i++) {
            AlphaNumericName a = AlphaNumericName.valueOf(SORTED[i]);
            assertTrue(AM_NSR, a.compareTo(a) == 0);

            for(int j=i+1; j<SORTED.length; j++) {
                AlphaNumericName b = AlphaNumericName.valueOf(SORTED[j]);
                assertTrue(AM_A_NLT_B, a.compareTo(b) < 0);
                assertTrue(AM_B_NGT_A, b.compareTo(a) > 0);
            }
        }
    }


    @Test
    public void sorting() {
        assertFalse(AM_HUH, Arrays.equals(UNSORTED, SORTED));

        AlphaNumericName[] startUnsorted = makeArray(UNSORTED);
        AlphaNumericName[] sorted = makeArray(SORTED);
        assertFalse(AM_HUH, Arrays.equals(startUnsorted, sorted));

        Arrays.sort(startUnsorted);
        assertTrue(AM_HUH, Arrays.equals(startUnsorted, sorted));
    }


    private AlphaNumericName[] makeArray(final String[] strings) {
        AlphaNumericName[] array = new AlphaNumericName[strings.length];
        int idx = 0;
        for (String s: strings) {
            array[idx++] = AlphaNumericName.valueOf(s);
        }
        return array;
    }

    private static final String SOME_VAL = "foo25";
            
    @Test
    public void convenience() {
        assertEquals(AM_NEQ, AlphaNumericName.valueOf(SOME_VAL),
                AlphaNumericName.ann(SOME_VAL));
    }
}
