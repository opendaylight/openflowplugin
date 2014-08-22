/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ByteUtils;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Represents a switch port number (unsigned 32-bit).
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code BigPortNumber} is done via the static
 * methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of port numbers is presented in an intuitive order.
 *
 * @see PortNumber
 * @author Simon Hunt
 */
public final class BigPortNumber extends U32Id implements Comparable<BigPortNumber> {

    /**
     * Constructs an instance of big port number.
     *
     * @param port the port number
     */
    private BigPortNumber(long port) {
        super(port);
    }


    /** 
     * Implements the Comparable interface, to return port numbers in
     * natural order.
     *
     * @param o the other port number
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(BigPortNumber o) {
        return this.id == o.id ? 0 : (this.id > o.id ? 1 : -1);
    }

    /** 
     * Returns an object that represents the big port number
     * defined by the specified long.
     *
     * @param port the port number
     * @return a big port number instance
     * @throws IllegalArgumentException if the port number is invalid
     */
    public static BigPortNumber valueOf(long port) {
        rangeCheck(port);
        return new BigPortNumber(port);
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(long)}. 
     * By statically importing this method, more concise code can be written.
     * The following two statements are equivalent:
     * <pre>
     *     BigPortNumber p = BigPortNumber.valueOf(5);
     *     BigPortNumber p = bpn(5);
     * </pre>
     *
     * @param port the port number
     * @return a big port number instance
     * @throws IllegalArgumentException if the port number is invalid
     */
    public static BigPortNumber bpn(long port) {
        return valueOf(port);
    }

    /** 
     * Returns an object that represents the big port number
     * defined by the specified string. The string is parsed
     * as a base-10 integer, unless it has a "0x" prefix, in which
     * case it is parsed as a hex number.
     *
     * @param s the port number as a string
     * @return a big port number instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static BigPortNumber valueOf(String s) {
        long port = parseLongStr(s);
        rangeCheck(port);
        return new BigPortNumber(port);
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(String)}. 
     * By statically importing this method, more concise code can be written.
     * The following two statements are equivalent:
     * <pre>
     *     BigPortNumber p = BigPortNumber.valueOf("0xa");
     *     BigPortNumber p = bpn("0xa");
     * </pre>
     *
     * @param s the port number as a string
     * @return a big port number instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static BigPortNumber bpn(String s) {
        return valueOf(s);
    }

    /** 
     * Returns an object that represents the port number
     * defined by the specified byte array. The array is expected to be
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded id
     * @return a big port number instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 4 bytes long
     */
    public static BigPortNumber valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU32(bytes, 0));
    }
    
    /**
     * Reads 4 bytes from the specified byte buffer and creates a big port 
     * number instance from their value.
     * 
     * @param buffer the buffer from which to read bytes
     * @return a big port number instance
     * @throws BufferUnderflowException the buffer does not have sufficient
     *         number of bytes remaining
     */
    public static BigPortNumber valueFrom(ByteBuffer buffer) {
        // TODO: Figure out if we can use get():int instead
        byte bytes[] = new byte[LENGTH_IN_BYTES];
        buffer.get(bytes);
        return new BigPortNumber(ByteUtils.getU32(bytes, 0));
    }

    // TODO: public static final BigPortNumber NONE ...
}
