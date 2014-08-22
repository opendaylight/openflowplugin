/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a cache (map) that allows its values to be garbage collected
 * when all external references to the values have been dropped.
 * <p>
 * Note that this class intentionally does not implement the
 * full {@link java.util.Map} interface; It only provides {@link #get},
 * {@link #put} and {@link #size} methods.
 * <p>
 * When an instance of {@code WeakValueCache} is constructed, it requires
 * a {@link ReferenceQueue} as a parameter. That queue is where the "zombie"
 * {@link CachedValueWeakReference references} will be placed when values are
 * garbage collected. It is expected that a {@link CacheCleaner} is "attached"
 * to the queue to automatically remove entries containing zombie references
 * from the internal map.
 * <p>
 * These classes were designed this way to allow a common reference queue to
 * be used for related data types, and thus have one common background thread
 * do clean up for a number of caches, rather than one thread per cache.
 * <p>
 * The recommended usage pattern for these classes is as follows:
 * <p>
 * Suppose you have 3 datatypes: {@code Type1}, {@code Type2}, {@code Type3},
 * for which you want caches. Also suppose they all extend {@code SuperType}.
 * The {@code SuperType} class is a good place to define the common reference
 * queue with attached {@link CacheCleaner}:
 * <pre>
 * public abstract class SuperType {
 *     // lazily initialized reference queue
 *     private static ReferenceQueue&lt;SuperType&gt; refQ;
 *
 *     // allow subclasses to retrieve a reference to the queue
 *     // first invocation creates and starts the cleaner thread
 *     protected static synchronized ReferenceQueue&lt;SuperType&gt; getRefQ() {
 *         if (refQ == null) {
 *             refQ = new ReferenceQueue&lt;SuperType&gt;();
 *             new CacheCleaner(refQ).start();
 *         }
 *         return refQ;
 *     }
 * }
 * </pre>
 *
 * The datatype classes will look something like this:
 * <pre>
 * public final class Type1 extends SuperType {
 *     // private constructor(s) to control instance creation
 *     private Type1(final String str) {
 *         ...
 *     }
 *
 *     ...
 *
 *     // our self-trimming cache
 *     private static final WeakValueCache&lt;String, Type1&gt; cache =
 *                      new WeakValueCache&lt;String, Type1&gt;(getRefQ());
 *     ...
 *
 *     // PUBLIC API:
 *     public static Type1 valueOf(final String stringRep) {
 *         synchronized (cache) {
 *             Type1 t = cache.get(stringRep);
 *             if (t == null) {
 *                 t = new Type1(stringRep);
 *                 cache.put(stringRep, t);
 *             }
 *             return t;
 *         } // sync
 *     }
 * }
 * </pre>
 *
 * Note the synchronization on the cache object in the {@code valueOf} method.
 * This is necessary to make the "find or create" paradigm "atomic" with
 * respect to the cache. The {@link CacheCleaner} also synchronizes on the
 * cache during its work of removing zombie entries.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author Simon Hunt
 */
public final class WeakValueCache<K, V> {

    /** our reference queue, for clean up notifications from the GC. Note that
     * this queue is typed to superclasses of the mapped value type, so we can
     * use the same queue to handle multiple classes.
     */
    private final ReferenceQueue<? super V> refQ;

    /** our delegated map */
    private final Map<K, CachedValueWeakReference<K, V>> map;


    //=== CONSTRUCTORS =======================================================

    /** Constructs an empty weak value map with the specified reference queue
     * for GC'd value reference housekeeping.
     *
     * @param queue the reference queue
     * @throws NullPointerException if queue is null
     */
    public WeakValueCache(final ReferenceQueue<? super V> queue) {
        if (queue == null)
            throw new NullPointerException("queue cannot be null");

        refQ = queue;
        map = new ConcurrentHashMap<K, CachedValueWeakReference<K, V>>();
    }


    //=== PUBLIC API =========================================================

    /** Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @param key the key
     * @return the value mapped to that key, or null
     * @throws NullPointerException if key is null
     */
    public V get(final K key) {
        if (key == null)
            throw new NullPointerException("key cannot be null");

        CachedValueWeakReference<K, V> wr = map.get(key);

        if (wr == null) return null;

        V value = wr.get();
        if (value == null) wr.purgeSelf();
        return value;
    }


    /** Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value
     * is replaced.
     *
     * @param key the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     * @throws NullPointerException if key or value is null
     */
    public V put(final K key, final V value) {
        if (key == null)
            throw new NullPointerException("key cannot be null");
        if (value == null)
            throw new NullPointerException("null values cannot be mapped");

        CachedValueWeakReference<K, V> wrValue =
                new CachedValueWeakReference<K, V>(this, key, value, refQ);

        CachedValueWeakReference<K, V> wrReturned = map.put(key, wrValue);

        // NOTE: since wrReturned has ALREADY been removed from the map
        //       by put(), there is no purge necessary for zombies
        return (wrReturned != null) ? wrReturned.get() : null;
    }

    /** Returns the size of the map.
     * Note that GC'd-but-not-yet-cleaned-up entries are included in the count,
     * so the "actual" number of values remaining in the cache may be smaller
     * than the value reported here.
     *
     * @return the number of entries in the map
     */
    public int size() {
        return map.size();
    }

    /** This method should <b>only</b> be called by
     * {@link CachedValueWeakReference#purgeSelf()} to remove from the map
     * the entry corresponding to the zombie reference.
     *
     * @param key the key under which this entry is stored
     */
    // package private
    void removeEntry(final K key) {
        map.remove(key);
    }
}
