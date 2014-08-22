/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import static java.lang.System.out;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.Random;

import org.opendaylight.util.junit.TestTools;

/**
 * Set of tools to simplify testing of sorts, etc.
 *
 * @author Thomas Vachuska
 */
public class Tools {

    public static final int BIG_SIZE = 1000000;
    
    /** Descending order comparator. */
    public static Comparator<Integer> DESCENDING = new Comparator<Integer>() {
        @Override public int compare(Integer a, Integer b) {
            return b - a;
        }
    };

    /** Ascending order comparator. */
    public static Comparator<Integer> ASCENDING = new Comparator<Integer>() {
        @Override public int compare(Integer a, Integer b) {
            return a - b;
        }
    };
    
    /**
     * Generates a sequence of prescribed length of random integers.
     * 
     * @param length length of sequence
     * @return array containing the sequence
     */
    public static Integer[] randomSequence(int length) {
        Integer[] sequence = new Integer[length];
        Random random = new Random();
        for (int i = 0; i < length; i++)
            sequence[i] = random.nextInt(length * 10);
        return sequence;
    }

    /**
     * Prints out the array of integers.
     * 
     * @param data array 
     * @param size size of array to be printed
     */
    public static void print(Integer data[], int size) {
        TestTools.print("[");
        for (int i = 0; i < size; i++) {
            TestTools.print(data[i]);
            if (i < size - 1)
                TestTools.print(", ");
        }
        out.println("]");
    }

    /**
     * Validates that the specified array is sorted relative to the comparator.
     * 
     * @param data array
     * @param size size of array to be validated
     * @param comparator comparator
     */
    public static <T> void validate(T[] data, int size, Comparator<T> comparator) {
        for (int i = 1; i < size; i++)
            assertTrue("incorrect item order",
                       comparator.compare(data[i-1], data[i]) <= 0);
    }
    
}
