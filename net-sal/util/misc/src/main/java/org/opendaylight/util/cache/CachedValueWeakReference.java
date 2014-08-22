/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * This class extends WeakReference to allow us to encapsulate the cache
 * that contains this reference, and the key with which it is associated.
 *
 * @param <K> the type of keys maintained by the cache that holds these
 *           value references
 * @param <V> the type of cached values
 *
 * @author Simon Hunt
 */
public final class CachedValueWeakReference<K, V> extends WeakReference<V> {

    private final WeakValueCache<K, V> cache;
    private final K key;

    private volatile boolean purged = false;

    /**
     * Constructs a new cached value weak reference that refers to the
     * given object and is registered with the given queue.
     *
     * @param cache the cache that holds this reference
     * @param key the key associated with the reference
     * @param referent the object to which the new weak reference will refer
     * @param q the queue with which the reference is to be registered
     * @throws NullPointerException if any parameter is null
     */
    public CachedValueWeakReference(final WeakValueCache<K, V> cache,
                                    final K key,
                                    final V referent,
                                    final ReferenceQueue<? super V> q) {
        super(referent, q);

        if (cache == null)
            throw new NullPointerException("cache cannot be null");
        if (key == null)
            throw new NullPointerException("key cannot be null");
        if (referent == null)
            throw new NullPointerException("referent cannot be null");
        if (q == null)
            throw new NullPointerException("reference queue cannot be null");

        this.cache = cache;
        this.key = key;
    }

    /** Removes from the cache the corresponding map entry holding this
     * reference object.
     * <p>
     *  This method is called from the {@link CacheCleaner} as it processes
     *  zombie references from its queue, or by {@link WeakValueCache}'s
     *  {@link WeakValueCache#get get} and {@link WeakValueCache#put put}
     *  methods when they discover a GC'd-value-but-not-yet-removed-entry.
     */
    // package private
    void purgeSelf() {
        boolean shouldPurge = false;

        synchronized (this) {
            if (!purged) {
                purged = true;
                shouldPurge = true;
            }
        }

        if (shouldPurge) {
            synchronized (cache) {
                cache.removeEntry(key);
            }
        }
    }


    @Override
    public String toString() {
        return "[CVWR: key='" + key + "']";
    }
}
