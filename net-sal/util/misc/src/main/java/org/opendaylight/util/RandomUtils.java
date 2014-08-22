/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Provides useful utilities revolving around randomness.
 *
 * @author Simon Hunt
 */
public final class RandomUtils {

    // no instantiation
    private RandomUtils() { }

    // our random number generator, seeded with the time of class load.
    private static final Random rand = new Random(new Date().getTime());

    /** Randomly selects and returns one item from the given list.
     *
     * @param <T> the item type
     * @param choices the list of choices
     * @return a randomly selected item
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the parameter is an empty list
     */
    public static <T> T select(List<T> choices) {
        final int n = choices.size();
        if (n < 1)
            throw new IllegalArgumentException("empty list");
        return choices.get(rand.nextInt(n));
    }

    /** Selects an integer at random, bounded by the specified range of
     * low (inclusive) to high (exclusive). {@code low} must be greater or
     * equal to zero; {@code high} must be greater than {@code low}.
     *
     * For example, invoking the following:
     * <pre>
     *      int r = RandomUtils.intFromRange(5, 10);
     * </pre>
     * will randomly return one of {@code 5, 6, 7, 8, 9}.
     *
     * @param low the lowest value (inclusive)
     * @param high the highest value (exclusive)
     * @return an int from within the given range
     * @throws IllegalArgumentException if low &lt; 0 or high &lt;= low
     */
    public static int intFromRange(int low, int high) {
        if (low < 0)
            throw new IllegalArgumentException(E_LOW_NEGATIVE);
        if (high <= low)
            throw new IllegalArgumentException(E_HIGH_LE_LOW);
        return rand.nextInt(high-low) + low;
    }

    // package private for unit test access
    static final String E_LOW_NEGATIVE = "low parameter is less than zero";
    static final String E_HIGH_LE_LOW = "high parameter equal to or less than low parameter";
}
