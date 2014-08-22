/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.cache.CacheableDataType;

/**
 * Forms the basis of datatype classes represented by unsigned values.
 *
 * @author Simon Hunt
 */
public abstract class UnsignedId extends CacheableDataType {

    private static final long serialVersionUID = 7855594746759075142L;

    private static final int DEC = 10;
    private static final int HEX = 16;
    private static final int OX_LEN = 2;

    /** Error message for bad identifier. */
    protected static final String E_BAD = "Bad id: ";
    /** Error message for out of range number. */
    protected static final String E_OOR = "Out of range id: ";
    /** Error message when byte array is null. */
    protected static final String E_NULL_BYTES = "Byte array cannot be null";
    /** Error message when String is null. */
    protected static final String E_NULL_STR = "String cannot be null";

    /** Lowest valid value for an id. */
    public static final int MIN_VALUE = 0;

    /** Returns the int equivalent to the specified string.
     * The string is parsed as a base-10 value, unless it
     * has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the string
     * @return the equivalent int
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    protected static int parseIntStr(String s) {
        int id;
        try {
            id = s.startsWith("0x")
                    ? Integer.parseInt(s.substring(OX_LEN), HEX)
                    : Integer.parseInt(s, DEC);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(E_BAD + s, nfe);
        }
        return id;
    }

    /** Returns the long equivalent to the specified string.
     * The string is parsed as a base-10 value, unless it
     * has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the string
     * @return the equivalent long
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    protected static long parseLongStr(String s) {
        long id;
        try {
            id = s.startsWith("0x")
                    ? Long.parseLong(s.substring(OX_LEN), HEX)
                    : Long.parseLong(s, DEC);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(E_BAD + s, nfe);
        }
        return id;
    }

}
