/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ByteUtils;

/**
 * A base class for unsigned 32-bit identifiers.
 *
 * @author Simon Hunt
 */
public abstract class U32Id extends UnsignedLongBasedId {

    private static final long serialVersionUID = 4900687379705251493L;

    /** Highest valid value. */
    public static final long MAX_VALUE = (long) Integer.MAX_VALUE * 2 + 1;

    /** Length of the identifier when encoded as a byte array. */
    public static final int LENGTH_IN_BYTES = 4;

    /** Error message when byte array is incorrect length. */
    public static final String E_BYTES_BAD_LEN =
            "Byte array not 4 bytes in length";

    /** Constructs the id.
     *
     * @param id the id value
     */
    protected U32Id(long id) {
        super(id);
    }

    /** Returns this id as a 4-byte array.
     * Note that the bytes are in Network order; i.e. the most
     * significant byte is at index zero.
     *
     * @return the id as a byte array
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[LENGTH_IN_BYTES];
        ByteUtils.setU32(bytes, 0, id);
        return bytes;
    }

    /** Ensures that the given value is within range. If it is, the method
     * returns silently; if not, an exception is thrown.
     *
     * @param value the value to check
     * @throws IllegalArgumentException if the value is out of range
     */
    protected static void rangeCheck(long value) {
        if (value < MIN_VALUE || value > MAX_VALUE)
            throw new IllegalArgumentException(E_OOR + value);
    }
}
