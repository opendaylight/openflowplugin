/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * This class defines unit tests for the {@link org.opendaylight.util.StringPool} class.
 *
 * @author Simon Hunt
 */
public class StringPoolTest {

    private static final int N_CHARS = 4;
    private static final int N_DIGITS = 4;

    private static final int LOTSA_SIZE = 2000;

    private StringPool sp;
    private Map<String, String> pool;


    @Before
    public void setUp() {
        sp = new StringPool();
        pool = sp.getPoolRef();
        assertEquals(AM_UXS, StringPool.DEFAULT_LIMIT, sp.getLimit());
    }

    private static final String ONE = "one";
    private static final String TWO = "two";
    private static final String ONETWO = ONE + TWO;

    @Test
    public void basic() {
        print(EOL + "basic()");
        print(sp);
        String oneA = sp.get(ONE);
        String oneB = sp.get("one");
        print(sp);
        assertSame(AM_NSR, oneA, oneB);
        assertEquals(AM_NEQ, oneA, ONE);
    }

    @Test
    public void basicAppendage() {
        print(EOL + "basicAppendage()");
        String twelveA = sp.get(ONE + TWO);
        String twelveB = sp.get(ONETWO);
        assertSame(AM_NSR, twelveA, twelveB);
    }

    @Test
    public void poolAdditions() {
        print(EOL + "poolAdditions()");
        assertEquals(AM_HUH, 0, pool.size());
        sp.get(ONE);
        assertEquals(AM_HUH, 1, pool.size());
        sp.get("o" + "n" + "e");
        assertEquals(AM_HUH, 1, pool.size());
    }

    // todo: add tests that confirm that the limit is being observed

    /** This test fills a large array (2000) with string-pooled strings randomly generated from a
     * small set (16) of possibilities. It then iterates across the possible string values, and
     * compares that string with each matching (.equals()) entry in the array, confirming that
     * both references point to the same string object.
     */
    @Test
    public void largeSetSmallPool() {
        print(EOL + "largeSetSmallPool()");
        // fill an array with random strings (constrained to a small set)
        final String[] lotsaStrings = new String[LOTSA_SIZE];
        for (int i=0; i<LOTSA_SIZE; i++) {
            String s = getRandomString();
            lotsaStrings[i] = sp.get(s);
        }

        // now check that each unique string reference is the same object
        for (int ci=0; ci<N_CHARS; ci++) {
            Character ch = (char) ('A' + ci);
            for (int ni=0; ni<N_DIGITS; ni++) {
                StringBuilder sb = new StringBuilder(ch.toString()).append(ni);
                String unique = sp.get(sb.toString());

                int matches = 0;
                for (int i=0; i<LOTSA_SIZE; i++) {
                    if (lotsaStrings[i].equals(unique)) {
                        matches++;
                        assertSame(AM_NSR, unique, lotsaStrings[i]);
                    }
                }
                print("Found " + matches + " occurances of '"+unique+"'");
            }
        }
    }


    /** This method returns a randomly generated string two characters in length. The first
     * character is a letter, the second is a digit; these are constrained in range by
     * {@link #N_CHARS} and {@link #N_DIGITS}.
     *
     * @return a randomly generated string
     */
    private String getRandomString() {
        int ci = (int) (Math.random() * N_CHARS);
        Character ch = (char) ('A' + ci);
        int ni = (int) (Math.random() * N_DIGITS);
        StringBuilder sb = new StringBuilder(ch.toString()).append(ni);
        return sb.toString();
    }

}
