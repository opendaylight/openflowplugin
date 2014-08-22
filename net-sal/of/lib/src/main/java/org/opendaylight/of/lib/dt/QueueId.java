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
 * Represents a queue identifier (unsigned 32-bit).
 * <p>
 * All constructors for this class are private.
 * Creating instances of {@code QueueId} is done via the static
 * methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of queue IDs is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class QueueId extends U32Id implements Comparable<QueueId> {

    private static final long serialVersionUID = -3598636810591365053L;

    /**
     * Constructs an instance of queue ID.
     *
     * @param id the queue ID
     */
    private QueueId(long id) {
        super(id);
    }

    //== Implementation note:
    //      We use default serialization to serialize the long ID.

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
     * Returns a string representation of the queue ID. Reserved values
     * will include the logical name of the queue.
     *
     * @return a string representation of the queue ID
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        String name = SPECIAL.get(this);
        sb.append("(").append(name != null ? name : toLong()).append(")");
        return sb.toString();
    }

    /** 
     * Implements the Comparable interface, to return queue IDs in 
     * natural order.
     *
     * @param o the other queue ID
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(QueueId o) {
        return this.id == o.id ? 0 : (this.id > o.id ? 1 : -1);
    }

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, QueueId> cachedIds =
            new WeakValueCache<String, QueueId>(getRefQ());

    /** 
     * Ensures that all equivalent queue ID encoding keys map to the 
     * same instance of QueueId.
     * <p>
     * Note that this method is always called from inside a block
     * synchronized on {@link #cachedIds}.
     *
     * @param qid a newly constructed queue ID (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique queue ID instance
     */
    private static QueueId intern(QueueId qid, String key) {
        final String canon = String.valueOf(qid.id);
        QueueId alreadyCached = cachedIds.get(canon);
        QueueId keeper = alreadyCached != null ? alreadyCached : qid;
        cachedIds.put(canon, keeper); // cached by normalized string rep
        cachedIds.put(key, keeper); // cached by given key
        return keeper;
    }


    /**
     * Returns an object that represents the queue ID defined by the 
     * specified long.
     *
     * @param qid the queue ID
     * @return the corresponding queue ID instance
     * @throws IllegalArgumentException if the queue ID is not u32
     */
    public static QueueId valueOf(long qid) {
        rangeCheck(qid);
        final String key = String.valueOf(qid);
        synchronized (cachedIds) {
            QueueId result = cachedIds.get(key);
            return (result == null) ? intern(new QueueId(qid), key) : result;
        } // sync
    }

    /** 
     * Returns an object that represents the queue ID defined by the 
     * specified string. The string is parsed as a base-10 integer, unless 
     * it has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the queue ID as a string
     * @return the corresponding queue ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static QueueId valueOf(String s) {
        synchronized (cachedIds) {
            // use the string itself as the key for the lookup
            QueueId result = cachedIds.get(s);
            if (result == null) {
                long qid = parseLongStr(s);
                rangeCheck(qid);
                result = intern(new QueueId(qid), s);
            }
            return result;
        } // sync
    }

    /**
     * Convenience method that returns the queue ID instance for the given 
     * string. This method simply delegates to {@link #valueOf(String)}. By
     * using a static import of this method, code can be written more concisely.
     * For example, the following two statements are equivalent: 
     * <pre>
     * QueueId q = QueueId.valueOf("4");
     * QueueId q = qid("4");
     * </pre>
     *
     * @param s the queue ID as a string
     * @return the corresponding queue ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static QueueId qid(String s) {
        return valueOf(s);
    }
    
    /** 
     * Returns an object that represents the queue ID defined by the 
     * specified byte array. The array is expected to be 
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded ID
     * @return the corresponding queue ID instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 4 bytes long
     */
    public static QueueId valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU32(bytes, 0));
    }

    // ======================================================================
    /** Wildcard queue used for queue stats requests; Since 1.0. */
    public static final QueueId ALL = valueOf(0xffffffffL);

    private static final Map<QueueId, String> SPECIAL =
            new HashMap<QueueId, String>(1);
    static {
        SPECIAL.put(ALL, "ALL");
    }

}
