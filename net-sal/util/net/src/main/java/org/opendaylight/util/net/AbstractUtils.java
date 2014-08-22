/*
 * (c) Copyright 2010-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

/**
 * Abstract base class for IpUtils and MacUtils
 *
 * @author Simon Hunt
 */
abstract class AbstractUtils {

    /** Exception Message: count must be greater than 1 */
    protected static final String COUNT_GE_ONE = "count must be 1 or greater";

    /** Helper method to create a byte array filled with a seed value.
     *
     * @param seed the seed value
     * @param nBytes the size of the byte array
     * @return the byte array
     */
    static byte[] getRepeatedByteArray(int seed, int nBytes) {
        if (seed < 0 || seed > 255)
            throw new IllegalArgumentException("seed must be 0..255");

        byte[] barr = new byte[nBytes];
        byte b = (byte) seed;
        for (int i=0; i<nBytes; i++) {
            barr[i] = b;
        }
        return barr;
    }

}
