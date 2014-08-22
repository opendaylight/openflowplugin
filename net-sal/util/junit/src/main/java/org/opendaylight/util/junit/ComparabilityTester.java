/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.junit;

import java.util.Comparator;

import junit.framework.Assert;

/**
 * Tester to test objects which implement Comparable.
 * 
 * @author Fabiel Zuniga
 */
public class ComparabilityTester {

    private ComparabilityTester() {

    }

    /**
     * Test comparison.
     * 
     * @param <T> Type of the comparable classes.
     * @param first Element first in order.
     * @param equallyInOrderToFirst Element equals in order to the first
     *        element.
     * @param second Element second in order.
     * @param third Element third in order.
     */
    public static <T extends Comparable<T>> void testComparison(T first, T equallyInOrderToFirst, T second, T third) {

        Comparator<T> comparator = new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                return o1.compareTo(o2);
            }

        };

        testComparison(first, equallyInOrderToFirst, second, third, comparator);
    }

    /**
     * Test comparison.
     * 
     * @param <T> Type of the comparable classes.
     * @param first Element first in order.
     * @param equallyInOrderToFirst Element equals in order to the first
     *        element.
     * @param second Element second in order.
     * @param third Element third in order.
     * @param comparator comparator.
     */
    public static <T> void testComparison(T first, T equallyInOrderToFirst, T second, T third, Comparator<T> comparator) {

        // Equals in order
        Assert.assertTrue("'Equal to' comparison broken", comparator.compare(first, equallyInOrderToFirst) == 0);

        // Less than
        Assert.assertTrue("'Less than' comparison broken", comparator.compare(first, second) < 0);

        // Greater than
        Assert.assertTrue("'Greater than' comparison broken", comparator.compare(second, first) > 0);

        // Transitive less than
        Assert.assertTrue("'Less than' transitive comparison broken", comparator.compare(first, second) < 0 && comparator.compare(second, third) < 0
                && comparator.compare(first, third) < 0);

        // Transitive greater than
        Assert.assertTrue("'Greater than' transitive comparison broken", comparator.compare(third, second) > 0 && comparator.compare(second, first) > 0
                && comparator.compare(third, first) > 0);
    }
}
