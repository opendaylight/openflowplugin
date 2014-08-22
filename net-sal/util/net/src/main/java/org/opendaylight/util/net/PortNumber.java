/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.cache.WeakValueCache;

/**
 * Represents a port number (unsigned 16-bit).
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code PortNumber} is done via the static
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
 * @see BigPortNumber
 * @author Simon Hunt
 */
public final class PortNumber extends U16Id
                              implements Comparable<PortNumber> {

    private static final long serialVersionUID = 8621907774623832119L;

    /**
     * Constructs an instance of port number
     * @param port the port number
     */
    private PortNumber(int port) {
        super(port);
    }

    // === PRIVATE serialization ===

    private Object readResolve() throws ObjectStreamException {
        // when this is called, port has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    // === PUBLIC instance API ===

    /** Implements the Comparable interface, to return port numbers in
     * natural order.
     *
     * @param o the other port number
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(PortNumber o) {
        return this.id - o.id;
    }

    // === STATIC methods ===

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, PortNumber> cachedPorts =
            new WeakValueCache<String, PortNumber>(getRefQ());

    /** Ensures that all equivalent port number
     * encoding keys map to the same instance of PortNumber.
     * <p>
     * Note that this method is always called from inside a block
     * synchronized on {@link #cachedPorts}.
     *
     * @param p a newly constructed PortNumber (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique PortNumber instance
     */
    private static PortNumber intern(PortNumber p, String key) {
        final String canon = String.valueOf(p.id);
        PortNumber alreadyCached = cachedPorts.get(canon);
        PortNumber keeper = alreadyCached != null ? alreadyCached : p;
        cachedPorts.put(canon, keeper); // cached by normalized string rep
        cachedPorts.put(key, keeper); // cached by given key
        return keeper;
    }

    // === PUBLIC static API ===

    /** Returns an object that represents the port number
     * defined by the specified integer.
     *
     * @param port the port number
     * @return a PortNumber instance
     * @throws IllegalArgumentException if the port number is invalid
     */
    public static PortNumber valueOf(int port) {
        rangeCheck(port);
        final String key = String.valueOf(port);
        synchronized (cachedPorts) {
            PortNumber result = cachedPorts.get(key);
            return (result == null) ? intern(new PortNumber(port), key) : result;
        } // sync
    }

    /** Returns an object that represents the port number
     * defined by the specified string. The string is parsed
     * as a base-10 integer.
     *
     * @param portStr the port number as a string
     * @return a PortNumber instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static PortNumber valueOf(String portStr) {
        synchronized (cachedPorts) {
            // use the string itself as the key for the lookup
            PortNumber result = cachedPorts.get(portStr);
            if (result == null) {
                try {
                    int port = parseIntStr(portStr);
                    rangeCheck(port);
                    result = intern(new PortNumber(port), portStr);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(E_BAD + portStr, nfe);
                }
            }
            return result;
        } // sync
    }


    /** Returns an object that represents the port number
     * defined by the specified byte array. The array is expected to be
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded id
     * @return a PortNumber instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 2 bytes long
     */
    public static PortNumber valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU16(bytes, 0));
    }
}
