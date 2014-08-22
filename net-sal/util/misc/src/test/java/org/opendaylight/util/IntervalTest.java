/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import org.opendaylight.util.junit.EqualityTester;
import org.opendaylight.util.junit.SerializabilityTester;

/**
 * Test for {@link org.opendaylight.util.Interval}
 */
public class IntervalTest {

    @Test
    public void testConstruction() {
        Interval<Integer> interval = new Interval<Integer>(Integer.valueOf(0),
                                                           Integer.valueOf(1),
                                                           Interval.Type.CLOSED);
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(1), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.CLOSED, interval.getType());

        try {
            interval = new Interval<Integer>(Integer.valueOf(0),
                                             Integer.valueOf(1), null);
            Assert.fail("Invalid interval type, exception expected");
        } catch (Exception e) {
            Assert.assertTrue(NullPointerException.class.isInstance(e));
        }

        for (Interval.Type type : Arrays
            .asList(Interval.Type.OPEN, Interval.Type.CLOSED,
                    Interval.Type.LEFT_CLOSED_RIGHT_OPEN,
                    Interval.Type.LEFT_OPEN_RIGHT_CLOSED)) {
            try {
                interval = new Interval<Integer>(null, null, type);
                Assert.fail("Invalid limits for type " + interval.getType()
                        + ", exception expected");
            } catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }

            try {
                interval = new Interval<Integer>(null, Integer.valueOf(1), type);
                Assert.fail("Invalid leftpoint for type " + interval.getType()
                        + ", exception expected");
            } catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }

            try {
                interval = new Interval<Integer>(Integer.valueOf(1), null, type);
                Assert.fail("Invalid rightpoint for type " + interval.getType()
                        + ", exception expected");
            } catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }

            try {
                interval = new Interval<Integer>(Integer.valueOf(1),
                                                 Integer.valueOf(0), type);
                Assert.fail("Invalid interval for type " + interval.getType()
                        + ", exception expected");
            } catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }
        }

        for (Interval.Type type : Arrays
            .asList(Interval.Type.LEFT_OPEN_RIGHT_UNBOUNDED,
                    Interval.Type.LEFT_CLOSED_RIGHT_UNBOUNDED)) {
            try {
                interval = new Interval<Integer>(null, null, type);
                Assert.fail("Invalid leftpoint for type " + interval.getType()
                        + ", exception expected");
            } catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }
        }

        for (Interval.Type type : Arrays
            .asList(Interval.Type.LEFT_UNBOUNDED_RIGHT_OPEN,
                    Interval.Type.LEFT_UNBOUNDED_RIGHT_CLOSED)) {
            try {
                interval = new Interval<Integer>(null, null, type);
                Assert.fail("Invalid rightpoint for type " + interval.getType()
                        + ", exception expected");
            } catch (Exception e) {
                Assert.assertTrue(IllegalArgumentException.class.isInstance(e));
            }
        }
    }

    @Test
    public void testValueOfOpen() {
        Interval<Integer> interval = Interval.valueOfOpen(Integer.valueOf(0),
                                                          Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.OPEN, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertFalse(interval.contains(Integer.valueOf(0)));
        Assert.assertFalse(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfClosed() {
        Interval<Integer> interval = Interval.valueOfClosed(Integer.valueOf(0),
                                                            Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.CLOSED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfLeftClosedRightOpen() {
        Interval<Integer> interval = Interval
            .valueOfLeftClosedRightOpen(Integer.valueOf(0), Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.LEFT_CLOSED_RIGHT_OPEN,
                            interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertFalse(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfLeftOpenRightClosed() {
        Interval<Integer> interval = Interval
            .valueOfLeftOpenRightClosed(Integer.valueOf(0), Integer.valueOf(9));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.LEFT_OPEN_RIGHT_CLOSED,
                            interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertFalse(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfLeftOpenRightUnbounded() {
        Interval<Integer> interval = Interval
            .valueOfLeftOpenRightUnbounded(Integer.valueOf(0));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertNull(interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.LEFT_OPEN_RIGHT_UNBOUNDED,
                            interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(10)));

        Assert.assertFalse(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfLeftClosedRightUnbounded() {
        Interval<Integer> interval = Interval
            .valueOfLeftClosedRightUnbounded(Integer.valueOf(0));
        Assert.assertEquals(Integer.valueOf(0), interval.getLeftEndpoint());
        Assert.assertNull(interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.LEFT_CLOSED_RIGHT_UNBOUNDED,
                            interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(-1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfLeftUnboundedRightOpen() {
        Interval<Integer> interval = Interval
            .valueOfLeftUnboundedRightOpen(Integer.valueOf(9));
        Assert.assertNull(interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.LEFT_UNBOUNDED_RIGHT_OPEN,
                            interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertFalse(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfLeftUnboundedRightClosed() {
        Interval<Integer> interval = Interval
            .valueOfLeftUnboundedRightClosed(Integer.valueOf(9));
        Assert.assertNull(interval.getLeftEndpoint());
        Assert.assertEquals(Integer.valueOf(9), interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.LEFT_UNBOUNDED_RIGHT_CLOSED,
                            interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(-1)));
        Assert.assertFalse(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testValueOfUnbounded() {
        Interval<Integer> interval = Interval.valueOfUnbounded();
        Assert.assertNull(interval.getLeftEndpoint());
        Assert.assertNull(interval.getRightEndpoint());
        Assert.assertEquals(Interval.Type.UNBOUNDED, interval.getType());

        Assert.assertTrue(interval.contains(Integer.valueOf(1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(-1)));
        Assert.assertTrue(interval.contains(Integer.valueOf(10)));

        Assert.assertTrue(interval.contains(Integer.valueOf(0)));
        Assert.assertTrue(interval.contains(Integer.valueOf(9)));
    }

    @Test
    public void testCreateOpen() {
        Interval<Integer> interval = Interval.createOpen(null, null);
        Assert.assertEquals(Interval.Type.UNBOUNDED, interval.getType());

        Integer start = Integer.valueOf(1);

        interval = Interval.createOpen(start, null);
        Assert.assertEquals(Interval.Type.LEFT_OPEN_RIGHT_UNBOUNDED,
                            interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());

        Integer end = Integer.valueOf(2);

        interval = Interval.createOpen(null, end);
        Assert.assertEquals(Interval.Type.LEFT_UNBOUNDED_RIGHT_OPEN,
                            interval.getType());
        Assert.assertEquals(end, interval.getRightEndpoint());

        interval = Interval.createOpen(start, end);
        Assert.assertEquals(Interval.Type.OPEN, interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());
        Assert.assertEquals(end, interval.getRightEndpoint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCreateOpen() {
        Interval.createOpen(Integer.valueOf(2), Integer.valueOf(1));
    }

    @Test
    public void testCreateClosed() {
        Interval<Integer> interval = Interval.createClosed(null, null);
        Assert.assertEquals(Interval.Type.UNBOUNDED, interval.getType());

        Integer start = Integer.valueOf(1);

        interval = Interval.createClosed(start, null);
        Assert.assertEquals(Interval.Type.LEFT_CLOSED_RIGHT_UNBOUNDED,
                            interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());

        Integer end = Integer.valueOf(2);

        interval = Interval.createClosed(null, end);
        Assert.assertEquals(Interval.Type.LEFT_UNBOUNDED_RIGHT_CLOSED,
                            interval.getType());
        Assert.assertEquals(end, interval.getRightEndpoint());

        interval = Interval.createClosed(start, end);
        Assert.assertEquals(Interval.Type.CLOSED, interval.getType());
        Assert.assertEquals(start, interval.getLeftEndpoint());
        Assert.assertEquals(end, interval.getRightEndpoint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCreateClosed() {
        Interval.createClosed(Integer.valueOf(2), Integer.valueOf(1));
    }

    @Test
    public void testIsEmpty() {
        Interval<Integer> interval = Interval.valueOfOpen(Integer.valueOf(0),
                                                          Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.valueOfClosed(Integer.valueOf(0),
                                          Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.valueOfLeftClosedRightOpen(Integer.valueOf(0),
                                                       Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.valueOfLeftOpenRightClosed(Integer.valueOf(0),
                                                       Integer.valueOf(0));
        Assert.assertTrue(interval.isEmpty());

        interval = Interval.valueOfLeftOpenRightUnbounded(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.valueOfLeftClosedRightUnbounded(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.valueOfLeftUnboundedRightOpen(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.valueOfLeftUnboundedRightClosed(Integer.valueOf(0));
        Assert.assertFalse(interval.isEmpty());

        interval = Interval.valueOfUnbounded();
        Assert.assertFalse(interval.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        Interval<Integer> baseObjToTest = Interval.valueOfClosed(Integer
            .valueOf(0), Integer.valueOf(1));
        Interval<Integer> equalsToBase1 = Interval.valueOfClosed(Integer
            .valueOf(0), Integer.valueOf(1));
        Interval<Integer> equalsToBase2 = Interval.valueOfClosed(Integer
            .valueOf(0), Integer.valueOf(1));
        Interval<Integer> unequalToBase1 = Interval.valueOfClosed(Integer
            .valueOf(-1), Integer.valueOf(1));
        Interval<Integer> unequalToBase2 = Interval.valueOfClosed(Integer
            .valueOf(0), Integer.valueOf(2));
        Interval<Integer> unequalToBase3 = Interval.valueOfOpen(Integer
            .valueOf(0), Integer.valueOf(1));

        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1,
                                             equalsToBase2, unequalToBase1);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1,
                                             equalsToBase2, unequalToBase2);
        EqualityTester.testEqualsAndHashCode(baseObjToTest, equalsToBase1,
                                             equalsToBase2, unequalToBase3);
    }

    @Test
    public void testSerializability() {
        SerializabilityTester.testSerialization(Interval.valueOfClosed(Integer
            .valueOf(0), Integer.valueOf(3)));
    }
}
