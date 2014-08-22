/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Cache to hold the entire data from the model. Data is never deleted unless
 * {@link Cache#invalidate(Object)} or {@link Cache#clear()} is called.
 * 
 * @param <K> type of the key to identify a cached value.
 * @param <V> type of the cached value.
 */
public class SmallDataCache<K, V> implements Cache<K, V> {

    private final Map<K, V> cache;
    private ReadWriteLock readWriteLock;

    /**
     * Creates a new {@link Cache}.
     */
    public SmallDataCache() {
        cache = new LinkedHashMap<K, V>();
        readWriteLock = new ReentrantReadWriteLock();
    }

    @Override
    public void put(K key, V value) {
        Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            cache.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V get(K key) {
        Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            return cache.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void invalidate(K key) {
        Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            cache.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        Lock lock = readWriteLock.writeLock();
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the cache's content.
     * 
     * @return the cache's content.
     */
    public Collection<V> getContent() {
        // This method is not part of the Cache interface because it doesn't
        // make sense for caches that just keep a subset of the data (When big
        // data is used).

        Lock lock = readWriteLock.readLock();
        lock.lock();
        try {
            return Collections.unmodifiableCollection(new LinkedList<V>(cache
                .values()));
        } finally {
            lock.unlock();
        }
    }
}
