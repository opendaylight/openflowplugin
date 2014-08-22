/*
 * (C) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

/**
 * In computer science, a cache is a component that transparently stores data
 * so that future requests for that data can be served faster. The data that
 * is stored within a cache might be values that have been computed earlier or
 * duplicates of original values that are stored elsewhere. If requested data
 * is contained in the cache (cache hit), this request can be served by simply
 * reading the cache, which is comparatively faster. Otherwise (cache miss),
 * the data has to be recomputed or fetched from its original storage
 * location, which is comparatively slower. Hence, the greater the number of
 * requests that can be served from the cache, the faster the overall system
 * performance becomes.
 * <P>
 * Caches usually makes temporal and/or spatial considerations to clear
 * values.
 * <P>
 * Use the proxy pattern to incorporate a cache. The proxy would get values
 * from the cache and it would delegate in case the value is not contained in
 * the cache. Example: <code>
 * <pre>
 * public interface MyService {
 *     public MyCacheable getCacheable(MyKey key);
 * }
 * 
 * public class MyServiceCacheProxy implements MyService {
 *     private MyService delegate;
 *     private Cache cache;
 * 
 *     public MyServiceCacheProxy(MyService subject) {
 *         this.delegate = subject;
 *         this.cache = ...;
 *     }
 * 
 *     {@literal @}Override
 *     public MyCacheable getCacheable(MyKey key) {
 *         MyCacheable result = this.cache.get(key);
 *         if(result == null) {
 *             result = this.delegate.getCacheable(key);
 *             if(result != null) {
 *                 this.cache.put(key, result);
 *             }
 *         }
 *         return result;
 *     }
 * }
 * </pre>
 * </code>
 * 
 * @param <K> type of the key to identify a cached value. It should be an
 *        immutable type. It is critical this type implements equals() and
 *        hashCode() correctly.
 * @param <V> type of the cached value.
 * @author Fabiel Zuniga
 */
public interface Cache<K, V> {

    /**
     * Puts a value into the cache.
     * 
     * @param key key.
     * @param value value.
     */
    public void put(K key, V value);

    /**
     * Gets a value from the cache.
     * 
     * @param key value's key.
     * @return the value associated to the given key if it exists in the
     *         cache, {@code null} otherwise.
     */
    public V get(K key);

    /**
     * Invalidates a key. This will cause the key to be removed from the
     * cache. This method should be called when the object associated to the
     * given key is no longer valid (A new copy should be retrieved from the
     * model in the next read or the object no longer exists in the model).
     * Deletion of entries from the cache due space management should be
     * handled by the cache's implementation.
     * 
     * @param key key to invalidate.
     */
    public void invalidate(K key);

    /**
     * Clears the cache.
     */
    public void clear();
}
