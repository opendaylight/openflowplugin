/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.U32Id;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a buffer identifier (unsigned 32-bit) assigned by datapath.
 *
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code BufferId} is done via the static
 * methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of buffer IDs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class BufferId extends U32Id implements Comparable<BufferId> {

    private static final long serialVersionUID = -8131737649098289937L;

    /**
     * Constructs an instance of buffer id.
     *
     * @param id the buffer id
     */
    private BufferId(long id) {
        super(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        String name = SPECIAL.get(this);
        if (name != null)
            sb.append("(").append(name).append(")");
        return sb.toString();
    }

    /** 
     * Implements the Comparable interface, to return buffer IDs in
     * natural order.
     *
     * @param o the other buffer ID
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(BufferId o) {
        return this.id == o.id ? 0 : (this.id > o.id ? 1 : -1);
    }

    /** 
     * Returns an object that represents the buffer ID defined by the 
     * specified long value.
     *
     * @param bid the buffer ID
     * @return a BufferId instance
     * @throws IllegalArgumentException if the buffer ID is not u32
     */
    public static BufferId valueOf(long bid) {
        rangeCheck(bid);
        return new BufferId(bid);
    }

    /** 
     * Returns an object that represents the buffer ID defined by the 
     * specified string. The string is parsed as a base-10 integer, unless it 
     * has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the buffer ID as a string
     * @return a BufferId instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static BufferId valueOf(String s) {
        long bid = parseLongStr(s);
        rangeCheck(bid);
        return new BufferId(bid);
    }

    /**
     * Convenience method that invokes {@link #valueOf(String)}. This method
     * can be statically imported to make code more concise. For example, the
     * following two statements are equivalent:
     * <pre>
     *     BufferId b = BufferId.valueOf(23);
     *     BufferId b = bid(23);
     * </pre>
     * 
     * @param s the buffer ID as a string
     * @return a buffer ID instance
     */
    public static BufferId bid(String s) {
        return valueOf(s);
    }

    /** 
     * Returns an object that represents the buffer identifier
     * defined by the specified byte array. The array is expected to be
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded ID
     * @return a BufferId instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 4 bytes long
     */
    public static BufferId valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU32(bytes, 0));
    }
    // ======================================================================
    /** Denotes no buffer available on the switch. */
    public static final BufferId NO_BUFFER = valueOf(0xffffffffL);

    private static final Map<BufferId, String> SPECIAL =
            new HashMap<BufferId, String>(1);
    static {
        SPECIAL.put(NO_BUFFER, "NO_BUFFER");
    }

}
