/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;


/**
 * A base class for unsigned 8-bit identifiers.
 *
 * @author Simon Hunt
 */
public abstract class U8Id extends UnsignedIntBasedId {

    private static final long serialVersionUID = 577150632930936164L;

    /** Highest valid value. */
    public static final int MAX_VALUE = 255;    // 2^8 - 1

    /** Constructs the id.
     *
     * @param id the id value
     */
    protected U8Id(int id) {
        super(id);
    }

    /** Returns this id as a byte.
     *
     * @return the id as a byte
     */
    public byte toByte() {
        return (byte) id;
    }

    /** Ensures that the given value is within range. If it is, the method
     * returns silently; if not, an exception is thrown.
     *
     * @param value the value to check
     * @throws IllegalArgumentException if the value is out of range
     */
    protected static void rangeCheck(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE)
            throw new IllegalArgumentException(E_OOR + value);
    }
}
