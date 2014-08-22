/*
 * (c) Copyright 2011,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the TallyMapLong class.
 *
 * @author Simon Hunt
 */
public class TallyMapLongTest {

    private static enum Fruit { APPLE, ORANGE, BANANA, PEAR }

    private TallyMapLong<Fruit> tally;

    @Before
    public void before() {
        tally = new TallyMapLong<Fruit>();
        checkCounts(0, 0, 0, 0);
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        print(tally);
        checkCounts(0, 0, 0, 0);
        checkTotalHigh(0, null);
    }

    @Test(expected = NullPointerException.class)
    public void nullKey() {
        print(EOL + "nullKey()");
        assertEquals(AM_NEQ, 0, tally.get(null));
    }

    @Test
    public void incDecNullKeys() {
        print(EOL + "incDecNullKeys()");
        assertEquals(AM_NEQ, 0, tally.inc(null));
        assertEquals(AM_NEQ, 0, tally.dec(null));
        assertEquals(AM_NEQ, 0, tally.inc(null, 5));
        assertEquals(AM_NEQ, 0, tally.dec(null, 5));
        checkTotalHigh(0, null);
    }

    @Test
    public void someFruit() {
        print(EOL + "someFruit()");
        tally.inc(Fruit.APPLE);
        tally.inc(Fruit.APPLE);
        tally.inc(Fruit.PEAR);
        print(tally);
        checkCounts(2, 0, 0, 1);
        checkKeys(Fruit.APPLE, Fruit.PEAR);
        checkTotalHigh(3, Fruit.PEAR);
        assertTrue(AM_HUH, tally.toString().contains("APPLE=2"));
        assertTrue(AM_HUH, tally.toString().contains("PEAR=1"));

        tally.inc(Fruit.APPLE, 4);
        tally.inc(Fruit.BANANA, 7);
        checkCounts(6, 0, 7, 1);
        checkKeys(Fruit.APPLE, Fruit.PEAR, Fruit.BANANA);
        checkTotalHigh(14, Fruit.PEAR);

        tally.dec(Fruit.APPLE, 6);
        tally.dec(Fruit.PEAR);
        checkCounts(0, 0, 7, 0);
        checkKeys(Fruit.BANANA);
        checkTotalHigh(7, Fruit.BANANA);
    }

    @Test
    public void negativeFruit() {
        print(EOL + "negativeFruit()");
        tally.dec(Fruit.BANANA);
        tally.dec(Fruit.BANANA);
        tally.dec(Fruit.BANANA);
        tally.dec(Fruit.ORANGE);
        print(tally);
        checkCounts(0, -1, -3, 0);
        checkKeys(Fruit.BANANA, Fruit.ORANGE);
        checkTotalHigh(-4, Fruit.BANANA);
    }

    private void checkCounts(long apples, long oranges, long bananas, long pears) {
        assertEquals(AM_NEQ, apples, tally.get(Fruit.APPLE));
        assertEquals(AM_NEQ, oranges, tally.get(Fruit.ORANGE));
        assertEquals(AM_NEQ, bananas, tally.get(Fruit.BANANA));
        assertEquals(AM_NEQ, pears, tally.get(Fruit.PEAR));
    }

    private void checkKeys(Fruit... expected) {
        Set<Fruit> exp = new HashSet<Fruit>();
        exp.addAll(Arrays.asList(expected));

        Set<Fruit> keys = tally.getKeys();
        for (Fruit f: Fruit.values()) {
            if (keys.contains(f) && !exp.contains(f))
                fail("Unexpected key in map: " + f);
            if (!keys.contains(f) && exp.contains(f))
                fail("Expected key NOT in map: " + f);
        }
    }

    private void checkTotalHigh(long total, Fruit high) {
        assertEquals(AM_NEQ, total, tally.getTotal());
        assertEquals(AM_NEQ, high, tally.getHighestKey());
    }

}
