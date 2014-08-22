/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 * Implements iterators that generate indices randomly according to a specified distribution.
 * For example:
 * <pre>
 * DistributedIndexGenerator gen = new DistributedIndexGenerator(30, 40);
 * </pre>
 * will create iterators that produce values from the set {0, 1, 2} randomly,
 * where the probabilities of occurrence are 30%, 40%, and 30% respectively.
 * <pre>
 * Iterator&lt;Integer&gt; it = gen.iterator(10);
 * </pre>
 * returns an iterator that will generate 10 such random values.
 */
public class DistributedIndexGenerator {

    private static final Random RANDOM = new Random();

    private final int[] percentages;
    private final float[] cutoff;

    /** Constructs a generator that uses the specified distribution. At least one percentage must
     * be specified, and the sum of all percentages specified must be less than 100.
     *
     * @param percentages the required percentage probabilities for indices 0, 1, ...
     */
    public DistributedIndexGenerator(int... percentages) {
        if (percentages.length < 1)
            throw new IllegalArgumentException("At least one percentage must be specified");

        this.percentages = percentages;
        this.cutoff = new float[percentages.length];

        // iterate across the percentages.. check they are within range, and the sum is less than 100
        int sum = 0;
        for (int i=0; i<percentages.length; i++) {
            if (percentages[i] < 1)
                throw new IllegalArgumentException("percentage ["+i+"] is less than 1");
            if (percentages[i] > 99)
                throw new IllegalArgumentException("percentage ["+i+"] is greater than 99");

            sum += percentages[i];
            if (sum > 99)
                throw new IllegalArgumentException("sum of percentages is greater than 99");
            cutoff[i] = sum / 100.0f;
        }
    }

    @Override
    public String toString() {
        return "[DistributedIndexGenerator: " + Arrays.toString(percentages) + "]";
    }

    /** Returns an iterator which will return the specified number of random indices drawn from the
     * distribution configured for this generator.
     *
     * @param count the number of random indices to return
     * @return an iterator
     */
    public Iterator<Integer> iterator(int count) {
        if (count < 1)
            throw new IllegalArgumentException("Count cannot be less than 1");
        return new IdxIter(count);
    }


    // iterator for the given number of items
    private class IdxIter implements Iterator<Integer>{

        private int count;

        private IdxIter(int count) {
            this.count = count;
        }

        @Override
        public boolean hasNext() {
            return count > 0;
        }

        @Override
        public Integer next() {
            float sample = RANDOM.nextFloat();
            int idx = 0;
            while (idx < cutoff.length && sample > cutoff[idx])
                idx++;
            count--;
            return idx;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
