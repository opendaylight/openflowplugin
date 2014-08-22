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
 * This class maintains a map of (integer) counts.
 * Note that this class is not synchronized.
 *
 * @param <K> the key type
 *
 * @author Simon Hunt
 * @see TallyMapLong
 */
public class TallyMap<K> {

    // TreeMap maintains the keys in sort order
    private Map<K, Integer> tally = new TreeMap<K, Integer>();

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
    public int inc(K key) {
        return modifyCount(key, 1);
    }

    /**
     * Increment the count associated with the specified key by the given
     * amount. If key is null, 0 is returned.
     *
     * @param key the key
     * @param amount the amount to increment by
     * @return the value of the count after we have incremented it
     */
    public int inc(K key, int amount) {
        return modifyCount(key, amount);
    }

    /**
     * Decrement the count associated with the specified key by 1.
     * If key is null, 0 is returned.
     *
     * @param key the key
     * @return the value of the count after we have decremented it
     */
    public int dec(K key) {
        return modifyCount(key, -1);
    }

    /**
     * Decrement the count associated with the specified key by the given
     * amount. If key is null, 0 is returned.
     *
     * @param key the key
     * @param amount the amount to decrement by
     * @return the value of the count after we have decremented it
     */
    public int dec(K key, int amount) {
        return modifyCount(key, -amount);
    }

    /** Private method to do the dirty work.
     *
     * @param key the key
     * @param amount the amount by which to adjust the count
     * @return the new value
     */
    private int modifyCount(K key, int amount) {
        if (key == null)
            return 0;

        Integer j = tally.get(key);
        int i = j==null ? 0 : j;
        i += amount;
        if (i != 0)
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
    public int get(K key) {
        Integer i = tally.get(key);
        return i==null ? 0 : i;
    }

    /**
     * Returns the key set for the backing map. Take care, because changes
     * to the map will change the contents of your set and vice versa!
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
    public int getTotal() {
        int total = 0;
        for (int i: tally.values())
            total += i;
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
        for (K k: tally.keySet())
            highest = k;
        return highest;
    }
}
