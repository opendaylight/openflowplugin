/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.util.PrimitiveUtils;
import org.opendaylight.util.cache.WeakValueCache;
import org.opendaylight.util.net.U8Id;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a table identifier (unsigned 8-bit); Since 1.0.
 * Table IDs start at 0 and run through {@link #MAX}.
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code TableId} is done via the static
 * methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of table IDs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class TableId extends U8Id implements Comparable<TableId> {

    private static final long serialVersionUID = -5929102403776843608L;

    /**
     * Constructs an instance of table ID.
     *
     * @param id the table ID
     */
    private TableId(int id) {
        super(id);
    }

    //== Implementation note:
    //      We use default serialization to serialize the short id.
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
     * Implements the Comparable interface, to return table IDs in
     * natural order.
     *
     * @param o the other table ID
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(TableId o) {
        return this.id - o.id;
    }

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, TableId> cachedIds =
            new WeakValueCache<String, TableId>(getRefQ());

    /** 
     * Ensures that all equivalent table ID encoding keys map to the 
     * same instance of table ID.
     * <p>
     * Note that this method is always called from inside a block
     * synchronized on {@link #cachedIds}.
     *
     * @param tid a newly constructed table ID (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique table ID instance
     */
    private static TableId intern(TableId tid, String key) {
        final String canon = String.valueOf(tid.id);
        TableId alreadyCached = cachedIds.get(canon);
        TableId keeper = alreadyCached != null ? alreadyCached : tid;
        cachedIds.put(canon, keeper); // cached by normalized string rep
        cachedIds.put(key, keeper); // cached by given key
        return keeper;
    }

    /** 
     * Returns an object that represents the table ID defined by the 
     * specified integer.
     *
     * @param tid the table ID
     * @return the corresponding table ID instance
     * @throws IllegalArgumentException if the table ID is invalid
     */
    public static TableId valueOf(int tid) {
        rangeCheck(tid);
        final String key = String.valueOf(tid);
        synchronized (cachedIds) {
            TableId result = cachedIds.get(key);
            return (result == null) ? intern(new TableId(tid), key) : result;
        } // sync
    }

    /** 
     * Returns an object that represents the table ID defined by the 
     * specified string. The string is parsed as a base-10 integer, unless 
     * it has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the table ID as a string
     * @return the corresponding table ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static TableId valueOf(String s) {
        synchronized (cachedIds) {
            // use the string itself as the key for the lookup
            TableId result = cachedIds.get(s);
            if (result == null) {
                int tid = parseIntStr(s);
                rangeCheck(tid);
                result = intern(new TableId(tid), s);
            }
            return result;
        } // sync
    }

    /**
     * Convenience method that returns the table ID for the given string.
     * This method simply delegates to {@link #valueOf(String)}. By using a
     * static import of this method, code may be written more concisely. For
     * example, the following two statements are equivalent:
     * <pre>
     * TableId t = TableId.valueOf("2");    
     * TableId t = tid("2");    
     * </pre>
     *
     * @param s the table ID as a string
     * @return the corresponding table ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static TableId tid(String s) {
        return valueOf(s);
    }

    /** 
     * Returns an object that represents the table ID defined by the 
     * specified byte.
     *
     * @param b the encoded ID
     * @return the corresponding table ID instance
     */
    public static TableId valueOf(byte b) {
        return valueOf(PrimitiveUtils.fromU8(b));
    }

    // ======================================================================
    /** 
     * Denotes the last usable table ID number.
     * (Tables are numbered from 0 to {@code TableId.MAX}.)
     */
    public static final TableId MAX = valueOf(0xfe);
    /** Denotes <em>all</em> tables. */
    public static final TableId ALL = valueOf(0xff);

    private static final Map<TableId, String> SPECIAL =
            new HashMap<TableId, String>(2);
    static {
        SPECIAL.put(MAX, "MAX");
        SPECIAL.put(ALL, "ALL");
    }
}
