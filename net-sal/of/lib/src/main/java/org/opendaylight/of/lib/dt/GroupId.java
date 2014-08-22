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
 * Represents a group identifier (unsigned 32-bit).
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code GroupId} is done via the static
 * methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of group IDs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class GroupId extends U32Id implements Comparable<GroupId> {

    private static final long serialVersionUID = 960012535818659294L;

    /**
     * Constructs an instance of group ID.
     *
     * @param id the group ID
     */
    private GroupId(long id) {
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

    /** 
     * Returns a string representation of the group ID. Reserved values
     * will include the logical name of the group.
     *
     * @return a string representation of the group ID
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        String name = SPECIAL.get(this);
        sb.append("(").append(name != null ? name : toLong()).append(")");
        return sb.toString();
    }

    /** 
     * Implements the Comparable interface, to return group IDs in
     * natural order.
     *
     * @param o the other group ID
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(GroupId o) {
        return this.id == o.id ? 0 : (this.id > o.id ? 1 : -1);
    }


    /** Our self-trimming cache. */
    private static final WeakValueCache<String, GroupId> cachedIds =
            new WeakValueCache<String, GroupId>(getRefQ());

    /** 
     * Ensures that all equivalent group ID encoding keys map to the same 
     * instance of GroupId.
     * <p>
     * Note that this method is always called from inside a block
     * synchronized on {@link #cachedIds}.
     *
     * @param gid a newly constructed group ID (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique group ID instance
     */
    private static GroupId intern(GroupId gid, String key) {
        final String canon = String.valueOf(gid.id);
        GroupId alreadyCached = cachedIds.get(canon);
        GroupId keeper = alreadyCached != null ? alreadyCached : gid;
        cachedIds.put(canon, keeper); // cached by normalized string rep
        cachedIds.put(key, keeper); // cached by given key
        return keeper;
    }


    /** 
     * Returns an object that represents the group ID defined by the 
     * specified long.
     *
     * @param gid the group ID expressed as a long
     * @return the corresponding group ID instance
     * @throws IllegalArgumentException if the group ID is not u32
     */
    public static GroupId valueOf(long gid) {
        rangeCheck(gid);
        final String key = String.valueOf(gid);
        synchronized (cachedIds) {
            GroupId result = cachedIds.get(key);
            return (result == null) ? intern(new GroupId(gid), key) : result;
        } // sync
    }

    /** 
     * Returns an object that represents the group ID defined by the 
     * specified string. The string is parsed as a base-10 integer, unless 
     * it has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the group ID as a string
     * @return the corresponding group ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static GroupId valueOf(String s) {
        synchronized (cachedIds) {
            // use the string itself as the key for the lookup
            GroupId result = cachedIds.get(s);
            if (result == null) {
                long gid = parseLongStr(s);
                rangeCheck(gid);
                result = intern(new GroupId(gid), s);
            }
            return result;
        } // sync
    }

    /**
     * Convenience method that returns the group ID instance for the given 
     * string representation. This method simply delegates to 
     * {@link #valueOf(String)}. By using a static import of this method, code
     * can be written more concisely. For example, the following two 
     * statements are equivalent:
     * <pre>
     * GroupId g = GroupId.valueOf("3");
     * GroupId g = gid("3");
     * </pre>
     * 
     * @param s the group ID as a string
     * @return the corresponding group ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static GroupId gid(String s) {
        return valueOf(s);
    }

    /** Returns an object that represents the group identifier
     * defined by the specified byte array. The array is expected to be
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded id
     * @return a GroupId instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 4 bytes long
     */
    public static GroupId valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU32(bytes, 0));
    }


    // ======================================================================
    /** Last usable group number; Since 1.1. */
    public static final GroupId MAX = valueOf(0xffffff00L);

    /** All groups, for group delete commands or group stats requests;
     * Since 1.1. */
    public static final GroupId ALL = valueOf(0xfffffffcL);

    /** Wildcard group used for flow stats requests; Since 1.1.
     * Selects all flows regardless of output group
     * (including flows with no output group).
     */
    public static final GroupId ANY = valueOf(0xffffffffL);

    private static final Map<GroupId, String> SPECIAL =
            new HashMap<GroupId, String>(3);
    static {
        SPECIAL.put(MAX, "MAX");
        SPECIAL.put(ALL, "ALL");
        SPECIAL.put(ANY, "ANY");
    }
}
