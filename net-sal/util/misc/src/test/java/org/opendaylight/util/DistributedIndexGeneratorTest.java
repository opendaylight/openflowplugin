/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;

/**
 * This JUnit test class tests the DistributedIndexGenerator class.
 *
 * @author Simon Hunt
 */
public class DistributedIndexGeneratorTest {

   @Test
    public void digNullArgs() {
        try {
            new DistributedIndexGenerator();
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertTrue(AM_HUH, e.getMessage().contains("At least one percentage must be specified"));
        }
    }

    @Test
    public void digNegArg() {
        try {
            new DistributedIndexGenerator(2, -1);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertTrue(AM_HUH, e.getMessage().contains("is less than 1"));
        }
    }

    @Test
    public void digBigArg() {
        try {
            new DistributedIndexGenerator(2, 3, 200);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertTrue(AM_HUH, e.getMessage().contains("is greater than 99"));
        }
    }

    @Test
    public void digMoreThan100() {
        try {
            new DistributedIndexGenerator(20, 30, 40, 50);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertTrue(AM_HUH, e.getMessage().contains("sum of percentages is greater than 99"));
        }
    }

    @Test
    public void digBasic() {
        print(EOL + "digBasic()");
        DistributedIndexGenerator gen = new DistributedIndexGenerator(30, 40);
        print(gen);
        try {
            gen.iterator(0);
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            assertTrue(AM_HUH, e.getMessage().contains("Count cannot be less than 1"));
        }


        Iterator<Integer> it = gen.iterator(5);
        List<Integer> capture = new ArrayList<Integer>(5);
        while (it.hasNext()) {
            int val = it.next();
            assertTrue("index out of range 0..2", val >= 0 && val <= 2);
            capture.add(val);
        }
        print(capture);
        assertEquals(AM_UXS, 5, capture.size());
    }


    private static final int LARGE_SAMPLE_SIZE = 10000;

    @Test
    public void digLargeSample() {
        print(EOL + "digLargeSample()");
        DistributedIndexGenerator gen = new DistributedIndexGenerator(10, 20);
        print(gen);
        int[] tally = new int[3]; // values are initialized to 0 by default
        Iterator<Integer> it = gen.iterator(LARGE_SAMPLE_SIZE);
        while (it.hasNext()) {
            tally[it.next()]++;
        }
        print("tally: " + Arrays.toString(tally));
        assertEquals("tally sum does not add up", LARGE_SAMPLE_SIZE, tally[0] + tally[1] + tally[2]);
        assertTrue("index 0 out of tolerance", checkTolerance(10, tally[0]));
        assertTrue("index 1 out of tolerance", checkTolerance(20, tally[1]));
        assertTrue("index 2 out of tolerance", checkTolerance(70, tally[2]));
    }

    private static final int tolerancePercent = 2;

    private boolean checkTolerance(int percent, int count) {
        int expectedCount = LARGE_SAMPLE_SIZE * percent / 100;
        int delta = LARGE_SAMPLE_SIZE * tolerancePercent / 100;
        int upperBound = expectedCount + delta;
        int lowerBound = expectedCount - delta;
        print("   {"+lowerBound+" .. "+count+" .. "+upperBound+"}");
        return count >= lowerBound && count <= upperBound;
    }
}
