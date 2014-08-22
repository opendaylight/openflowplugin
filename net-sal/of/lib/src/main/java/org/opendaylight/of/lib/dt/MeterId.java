/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.cache.WeakValueCache;
import org.opendaylight.util.net.U32Id;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a meter identifier (unsigned 32-bit); Since 1.3.
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code MeterId} is done via the static
 * methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of meter IDs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class MeterId extends U32Id implements Comparable<MeterId> {

    private static final long serialVersionUID = -1898786225797424373L;

    /**
     * Constructs an instance of meter ID.
     *
     * @param id the meter ID
     */
    private MeterId(long id) {
        super(id);
    }

    //== Implementation note:
    //      We use default serialization to serialize the long id.
    private Object readResolve() throws ObjectStreamException {
        // when this is called, id has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
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
     * Implements the Comparable interface, to return meter IDs in
     * natural order.
     *
     * @param o the other meter ID
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(MeterId o) {
        return this.id == o.id ? 0 : (this.id > o.id ? 1 : -1);
    }

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, MeterId> cachedIds =
            new WeakValueCache<String, MeterId>(getRefQ());

    /** 
     * Ensures that all equivalent meter ID encoding keys map to the same 
     * instance of meter ID.
     * <p>
     * Note that this method is always called from inside a block
     * synchronized on {@link #cachedIds}.
     *
     * @param mid a newly constructed meter ID (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique meter ID instance
     */
    private static MeterId intern(MeterId mid, String key) {
        final String canon = String.valueOf(mid.id);
        MeterId alreadyCached = cachedIds.get(canon);
        MeterId keeper = alreadyCached != null ? alreadyCached : mid;
        cachedIds.put(canon, keeper); // cached by normalized string rep
        cachedIds.put(key, keeper); // cached by given key
        return keeper;
    }


    /** 
     * Returns an object that represents the meter ID defined by the 
     * specified long.
     *
     * @param mid the meter ID
     * @return the corresponding meter ID instance
     * @throws IllegalArgumentException if the meter ID is not u32
     */
    public static MeterId valueOf(long mid) {
        rangeCheck(mid);
        final String key = String.valueOf(mid);
        synchronized (cachedIds) {
            MeterId result = cachedIds.get(key);
            return (result == null) ? intern(new MeterId(mid), key) : result;
        } // sync
    }

    /** 
     * Returns an object that represents the meter ID defined by the 
     * specified string. The string is parsed as a base-10 integer, unless 
     * it has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the meter ID as a string
     * @return the corresponding meter ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static MeterId valueOf(String s) {
        synchronized (cachedIds) {
            // use the string itself as the key for the lookup
            MeterId result = cachedIds.get(s);
            if (result == null) {
                long mid = parseLongStr(s);
                rangeCheck(mid);
                result = intern(new MeterId(mid), s);
            }
            return result;
        } // sync
    }

    /**
     * Convenience method that returns the meter ID defined by the specified
     * string. This method simply delegates to {@link #valueOf(String)}.
     * By using a static import of this method, code may be written more
     * concisely. For example, the following two statements are equivalent:
     * <pre>
     * MeterId m = MeterId.valueOf("35");
     * MeterId m = mid("35");
     * </pre>
     * @param s the meter ID as a string
     * @return the corresponding meter ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static MeterId mid(String s) {
        return valueOf(s);
    }
    
    /** 
     * Returns an object that represents the meter identifier defined by 
     * the specified byte array. The array is expected to be 
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded ID
     * @return the corresponding meter ID instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 4 bytes long
     */
    public static MeterId valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU32(bytes, 0));
    }

    //=======================================================================
    /** Meters are designated by a number from 1 to MAX (0xffff0000). */
    public static final MeterId MAX = valueOf(0xffff0000L);
    /** Meter for slow datapath, if any. */
    public static final MeterId SLOWPATH = valueOf(0xfffffffdL);
    /** Meter for controller connection. */
    public static final MeterId CONTROLLER = valueOf(0xfffffffeL);
    /** Represents all meters for stat requests commands. */
    public static final MeterId ALL = valueOf(0xffffffffL);

    private static final Map<MeterId, String> SPECIAL =
            new HashMap<MeterId, String>();
    static {
        SPECIAL.put(MAX, "MAX");
        SPECIAL.put(SLOWPATH, "SLOWPATH");
        SPECIAL.put(CONTROLLER, "CONTROLLER");
        SPECIAL.put(ALL, "ALL");
    }
}
