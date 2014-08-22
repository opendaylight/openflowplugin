/*
 * (c) Copyright 2010,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class maintains a map of (long) counts.
 * Note that this class is not synchronized.
 *
 * @param <K> the key type
 *
 * @author Simon Hunt
 * @see TallyMap
 */
public class TallyMapLong<K> {

    // TreeMap maintains the keys in sort order
    private Map<K, Long> tally = new TreeMap<K, Long>();

    @Override
    public String toString() {
        return tally.toString();
    }

    /**
     * Increment the count associated with the specified key by 1.
     * If key is null, 0 is returned.
     *
     * @param key the key
     * @return the value of the count after we have incremented it
     */
    public long inc(K key) {
        return modifyCount(key, 1L);
    }

    /**
     * Increment the count associated with the specified key by the given
     * amount. If key is null, 0 is returned.
     *
     * @param key the key
     * @param amount the amount to increment by
     * @return the value of the count after we have incremented it
     */
    public long inc(K key, long amount) {
        return modifyCount(key, amount);
    }

    /**
     * Decrement the count associated with the specified key by 1.
     * If key is null, 0 is returned.
     *
     * @param key the key
     * @return the value of the count after we have decremented it
     */
    public long dec(K key) {
        return modifyCount(key, -1L);
    }

    /**
     * Decrement the count associated with the specified key by the given
     * amount. If key is null, 0 is returned.
     *
     * @param key the key
     * @param amount the amount to decrement by
     * @return the value of the count after we have decremented it
     */
    public long dec(K key, long amount) {
        return modifyCount(key, -amount);
    }

    /** private method to do the dirty work.
     *
     * @param key the key
     * @param amount the amount by which to adjust the count
     * @return the new value
     */
    private long modifyCount(K key, long amount) {
        if (key == null)
            return 0;

        Long j = tally.get(key);
        long i = j==null ? 0L : j;
        i += amount;
        if (i != 0L)
            tally.put(key, i);
        else
            tally.remove(key);
        return i;
    }

    /**
     * Returns the count associated with the specified key.
     * If key is null, 0 is returned.
     *
     * @param key the key
     * @return the value of the count
     */
    public long get(K key) {
        Long i = tally.get(key);
        return i==null ? 0L : i;
    }

    /**
     * Returns the key set for the backing map. Take care, because changes to
     * the map will change the contents of your set and vice versa!
     *
     * @return the backing map key set
     */
    public Set<K> getKeys() {
        return tally.keySet();
    }

    /**
     * Returns the sum of all values in the map; i.e.&nbsp; the Grand Total.
     *
     * @return the grand total
     */
    public long getTotal() {
        long total = 0L;
        for (long i: tally.values()) {
            total += i;
        }
        return total;
    }

    /**
     * Returns the key that is "highest" in the natural ordering of
     * the K class, and is also present (has a non-zero count) in the map.
     *
     * @return the highest key
     */
    public K getHighestKey() {
        K highest = null;
        for (K k: tally.keySet()) {
            highest = k;
        }
        return highest;
    }
}
