/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

/**
 * Provides some useful helper methods for compareTo() implementations.
 *
 * @author Simon Hunt
 */
public final class ComparisonUtils {

    /** Compares the given booleans and returns a value consistent with the
     * requirement for {@link Comparable#compareTo} such that {@code false}
     * is sorted before {@code true}.
     *
     * @param mine the boolean in my instance
     * @param other the boolean in the other instance
     * @return the result less than, equal to, or greater than 0, as appropriate
     */
    public static int compareBooleans(boolean mine, boolean other) {

        /*           __OTHER___
         *   MINE  |  F      T
         *   ------+-----------
         *    F    |  0     -1
         *    T    |  1      0
         */

        return (mine && !other) ? 1 : ((!mine && other) ? -1 : 0);
    }
}
