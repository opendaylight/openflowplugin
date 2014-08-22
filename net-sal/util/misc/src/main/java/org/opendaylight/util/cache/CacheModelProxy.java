/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

/**
 * Proxy that integrates the cache to the model. The proxy gets values from
 * the cache and it delegates to the model in case the value is not contained
 * in the cache.
 * <p>
 * There is no need to call {@link Cache#put(Object, Object)} in
 * CacheModelProxy because the cache will be automatically updated by the
 * model when an object that doesn't belong to the cache is requested. If
 * {@link Cache#get(Object)} in CacheModelProxy returns {@code null} it means
 * the object does not belong to the cache nor the model.
 * 
 * @param <K> type of the key to identify a cached value. It should be an
 *        immutable type. It is critical this type implements equals() and
 *        hashCode() correctly.
 * @param <V> type of the cached value.
 */
public class CacheModelProxy<K, V> implements Cache<K, V> {

    private final Cache<K, V> cache;
    private final Model<K, V> model;

    /**
     * Creates a new {@link Cache}.
     * 
     * @param cache cache delegate.
     * @param model model.
     */
    public CacheModelProxy(Cache<K, V> cache, Model<K, V> model) {
        if (cache == null) {
            throw new NullPointerException("Cache cannot be null");
        }

        if (model == null) {
            throw new NullPointerException("model cannot be null");
        }

        this.cache = cache;
        this.model = model;
    }

    @Override
    public void put(K key, V value) {
        this.cache.put(key, value);
    }

    @Override
    public V get(K key) {
        /*
         * This method cannot be synchronized because the model could take
         * considerable time retrieving data. If several threads call this
         * method at the same time (loosely speaking) with the same key, the
         * model could be called several times requesting the same data.
         * However, adding synchronization to avoid calling the model multiple
         * times for the same key makes the solution too complicated and with
         * potential deadlocks.
         */
        V result = this.cache.get(key);
        if (result == null) {
            result = this.model.get(key);
            if (result != null) {
                this.cache.put(key, result);
            }
        }
        return result;
    }

    @Override
    public void invalidate(K key) {
        this.cache.invalidate(key);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    /**
     * Model.
     * 
     * @param <K> type of the key to identify a cached value. It should be an
     *        immutable type. It is critical this type implements equals() and
     *        hashCode() correctly.
     * @param <V> type of the cached value.
     */
    public static interface Model<K, V> {

        /**
         * Gets a value from the model.
         * 
         * @param key value's key.
         * @return the value associated to the given key if it exists in the
         *         model, {@code null} otherwise.
         */
        public V get(K key);
    }
}
