/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This JUnit test class tests the RandomUtils class.
 *
 * @author Simon Hunt
 */
public class RandomUtilsTest {

    private static final String[] WORDS = {
       "Anonymous", "Banana", "Couch", "Develop", "Erstwhile", "Friendly", "Ghastly",
            "Horrendous", "Insipid", "Junk", "Kissable", "Leisure", "Mingle",
       "Notorious", "Opinionated", "Pleasant", "Questionable", "Relaxing", "Sultry",
            "Toxic", "Underhanded", "Victorious", "Westerly", "Xyzzy", "Yowl", "Zinger",
    };

    private List<String> strings;

    @Before
    public void beforeTest() {
        strings = Arrays.asList(WORDS);
    }

    @Test
    public void select() {
        print(EOL + "select()");
        // This could be tricky, because theoretically there is a chance of choosing the same
        // result more than once. But let's suppose that this won't happen more than twice
        Set<String> chosen = new HashSet<String>();
        for (int i=0; i<6; i++) {
            String s = RandomUtils.select(strings);
            chosen.add(s);
        }
        print(chosen);
        assertTrue("not enough differing strings", chosen.size() >= 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectFromEmptyList() {
        RandomUtils.select(Collections.<Object>emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void selectFromNull() {
        RandomUtils.select(null);
    }

    @Test
    public void intFromRange() {
        print(EOL + "intFromRange()");
        final int low = 7;
        final int high = 14;
        final int[] counts = new int[14];
        for (int i=0; i<2000; i++) {
            int index = RandomUtils.intFromRange(low, high);
            counts[index]++;
            assertTrue("result too small", index >= low);
            assertTrue("result too large", index < high);
        }
        for (int i=0; i< counts.length; i++)
            print("[" + i + "] " + counts[i]);
    }

    @Test
    public void intFromRangeLowTooLow() {
        print(EOL + "intFromRangeLowTooLow()");
        try {
            RandomUtils.intFromRange(-1, 5);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX>  " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains(RandomUtils.E_LOW_NEGATIVE));
        }
    }

    @Test
    public void intFromRangeHighTooLow() {
        print(EOL + "intFromRangeHighTooLow()");
        try {
            RandomUtils.intFromRange(0,-1);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX>  " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains(RandomUtils.E_HIGH_LE_LOW));
        }

        try {
            RandomUtils.intFromRange(5,5);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX>  " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains(RandomUtils.E_HIGH_LE_LOW));
        }

        try {
            RandomUtils.intFromRange(8,4);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print("EX>  " + e);
            assertTrue(AM_WREXMSG, e.getMessage().contains(RandomUtils.E_HIGH_LE_LOW));
        }
    }
}
