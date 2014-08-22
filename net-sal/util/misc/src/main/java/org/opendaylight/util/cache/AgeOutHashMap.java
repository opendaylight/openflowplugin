/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import org.opendaylight.util.TimeUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a map of key/value pairs that will automatically age-out when not
 * write-accessed within a given number of milliseconds. The age-out parameter
 * is dynamically configurable, meaning that it can be changed after creation
 * of the map.
 * <p>
 * Note that this design uses a "passive" age-out model, meaning that aged-out
 * entries are only detected and removed as a side-effect of interaction with
 * the map. For example:
 * <pre>
 * AgeOutHashMap&lt;Foo, Bar&gt; map = new AgeOutHashMap&lt;Foo, Bar&gt;(100);
 * map.put(Foo.F1, Bar.B2);
 * // .. wait longer than 100ms for age-out ..
 * // At this point, the entry is still in the map, but is only (silently)
 * // removed when we attempt to access it:
 * Bar b = map.get(Foo.F1);
 * assertNull(b);
 * </pre>
 * Entries in the map can be "actively" aged-out by invoking the
 * {@link #prune()} method.
 *
 * @param <K> the key class
 * @param <V> the value class
 *
 * @author Simon Hunt
 */
// FIXME : Review for thread-safety issues

public class AgeOutHashMap<K, V> {
    /**
     * The default age-out (ms).
     */
    public static final long DEFAULT_AGE_OUT_MS = 5000;

    /**
     * The minimum age-out (ms) allowed.
     */
    public static final long MIN_AGE_OUT_MS = 10;

    static final String E_AGEOUT_TOO_SMALL = "age-out(ms) too small: ";

    // use time utils, so that efficient, fast unit tests can be written
    private static TimeUtils TIME = TimeUtils.getInstance();

    // value wrapper
    private class Wrapper {
        private final V wrapped;
        private long ts;

        private Wrapper(V value) {
            wrapped = value;
            ts = now();
        }

        private long age() {
            return now() - ts;
        }

        private boolean valid() {
             return age() < ageOutMs;
        }

        @Override
        public String toString() {
            String v = valid() ? "" : "*";
            return "[" + age() + v + "]:" + wrapped;
        }
    }

    // backing map
    private ConcurrentHashMap<K, Wrapper> map =
            new ConcurrentHashMap<K, Wrapper>();

    private long ageOutMs;
    private boolean ageOutDeadwoodOnly = false;

    /**
     * Constructs a map with the initial age-out set to the default
     * value of {@value #DEFAULT_AGE_OUT_MS} milliseconds.
     */
    public AgeOutHashMap() {
        ageOutMs = DEFAULT_AGE_OUT_MS;
    }

    /**
     * Constructs a map with the specified initial age-out value (ms).
     *
     * @param ageOutMs the initial age-out value
     * @throws IllegalArgumentException if ageOutMs is less than
     *          {@value #MIN_AGE_OUT_MS}
     */
    public AgeOutHashMap(long ageOutMs) {
        this(ageOutMs, false);
    }
    
    /**
     * Constructs a map with the specified initial age-out value (ms). 
     * During a call to public {@link #remove(Object)}, the
     * {@link #ageOutDeadwoodOnly} flag determines if
     * {@link #deadwood(Object)} is invoked.
     *
     * @param ageOutMs the initial age-out value
     * @param ageOutDeadwoodOnly the age-out deadwood only flag
     * @throws IllegalArgumentException if ageOutMs is less than
     *          {@value #MIN_AGE_OUT_MS}
     */
    public AgeOutHashMap(long ageOutMs, boolean ageOutDeadwoodOnly) {
        if (ageOutMs < MIN_AGE_OUT_MS)
            throw new IllegalArgumentException(E_AGEOUT_TOO_SMALL + ageOutMs);
        this.ageOutMs = ageOutMs;
        this.ageOutDeadwoodOnly = ageOutDeadwoodOnly;
    }

    @Override
    public String toString() {
        return "{age=" + ageOutMs + "ms,map=" + map + "}";
    }

    /**
     * Returns the current age-out value (ms).
     *
     * @return the current age-out value (ms)
     */
    public long getAgeOutMs() {
        return ageOutMs;
    }

    /**
     * Sets a new age-out value (ms) on the map. Any entries in the map older
     * than this new value are immediately pruned.
     *
     * @param ageOutMs the new age-out value (ms)
     */
    public void setAgeOut(long ageOutMs) {
        if (ageOutMs < MIN_AGE_OUT_MS)
            throw new IllegalArgumentException(E_AGEOUT_TOO_SMALL + ageOutMs);
        this.ageOutMs = ageOutMs;
        // take this opportunity to prune invalidated entries
        // NOTE: even though the new age-out value might be larger than the
        //       old value, we still want to prune, since there may be stale
        //       entries that have a timestamp older than both values
        prune();
    }

    /**
     * Returns the number of entries in the map. Note that this may be an
     * over-estimate of the true number if there are entries that have
     * aged-out but have not yet been pruned from the map. To get an accurate
     * count, invoke {@link #prune()} first.
     *
     * @return the number of map entries
     */
    public int size() {
        return map.size();
    }

    /**
     * Prunes aged-out entries from the map, returning the true map size.
     *
     * @return the map size after pruning
     */
    public int prune() {
        Set<V> removed = new HashSet<V>();
        Set<Map.Entry<K, Wrapper>> invalid =
                new HashSet<Map.Entry<K, Wrapper>>();

        for (Map.Entry<K, Wrapper> entry : map.entrySet())
            if (!entry.getValue().valid()) {
                invalid.add(entry);
                removed.add(entry.getValue().wrapped);
            }

        map.entrySet().removeAll(invalid);
        deadwood(removed);
        return map.size();
    }

    /**
     * Creates an entry in the map for the given key and value, returning the
     * old value if there was one; null otherwise.
     *
     * @param key the key
     * @param value the new value
     * @return the old value
     */
    public V put(K key, V value) {
        Wrapper newVal = new Wrapper(value);
        Wrapper oldVal = map.put(key, newVal);
        return extractValue(oldVal);
    }

    /**
     * Refreshes the timestamp for the map entry with the given key, if
     * it exists. This method returns {@code true} to indicate that the
     * entry exists and had its timestamp updated; {@code false} if there
     * was no entry for the given key.
     *
     * @param key the key
     * @return true, if the entry exists and was updated; false otherwise
     */
    public boolean touch(K key) {
        boolean result = false;
        Wrapper val = map.get(key);
        if (val != null) {
            if (val.valid()) {
                val.ts = now();
                result = true;
            } else {
                // TODO: review implication of multi-thread access
                // what happens if another thread TOUCHes/PUTs this key after
                // we pulled the value out and found it to be not valid, but
                // before we reach here...
                deadwood(map.remove(key).wrapped);
            }
        }
        return result;
    }

    /**
     * Refreshes the timestamp for the map entry with the given key, if it
     * exists, or creates a new entry. This method returns {@code true} to
     * indicate that the entry already existed and had its timestamp updated;
     * {@code false} if a new entry was created.
     *
     * @param key the key
     * @param value the value
     * @return true, if the entry existed and was simply refreshed; false, if
     *          a new entry was created
     */
    public boolean touchOrPut(K key, V value) {
        boolean result = false;
        Wrapper val = map.get(key);
        // TODO: review implication of multi-thread access
        if (val != null) {
            if (val.valid()) {
                // touch
                val.ts = now();
                result = true;
            } else {
                // put
                val = new Wrapper(value);
                map.put(key, val);
            }
        } else {
            // put
            val = new Wrapper(value);
            map.put(key, val);
        }
        return result;
    }

    /**
     * Returns the value in the map for the given key. If no such entry exists
     * in the map, null is returned.
     *
     * @param key the key
     * @return the mapped value
     */
    public V get(K key) {
        Wrapper val = map.get(key);
        return extractValue(key, val);
    }

    /**
     * Removes the entry from the map for the given key, returning its value.
     * If no such entry exists in the map, null is returned.
     * The {@link #deadwood(Object)} callback will be invoked if a value was
     * found and {@link #ageOutDeadwoodOnly} is not set.
     *
     * @param key the key
     * @return the (removed) value
     */
    public V remove(K key) {
        V result = null;
        Wrapper val = map.remove(key);
        if (val != null) {
            if (val.valid())
                result = val.wrapped;
            if (!ageOutDeadwoodOnly)
                deadwood(val.wrapped);
        }
        return result;
    }

    // === HELPER METHODS

    /**
     * Invoked when a value is silently removed from the map.
     * This default implementation does nothing.
     * <p>
     * This method is provided so that subclasses may override it and do
     * something interesting with the value just removed.
     *
     * @param value the value removed from the map
     */
    protected void deadwood(V value) { }

    /**
     * Invoked when a set of values are silently removed from the map.
     * This default implementation does nothing.
     * <p>
     * This method is provided so that subclasses may override it and do
     * something interesting with the values just removed.
     *
     * @param values the values removed from the map
     */
    protected void deadwood(Set<V> values) { }


    // extracts the wrapped value, as long as the wrapper hasn't aged-out
    private V extractValue(Wrapper wrapper) {
        return (wrapper != null && wrapper.valid()) ? wrapper.wrapped : null;
    }

    // extracts the wrapped value, as long as the wrapper hasn't aged-out;
    // if it has aged-out the entry is removed silently, before returning null.
    private V extractValue(K key, Wrapper wrapper) {
        V result = null;
        if (wrapper != null) {
            if (wrapper.valid())
                result = wrapper.wrapped;
            else
                deadwood(map.remove(key).wrapped);
        }
        return result;
    }

    // === package-private for unit test access

    // Returns the TimeUtils instance used to time stamp wrapped values
    TimeUtils time() {
        return TIME;
    }

    // return the current system time in millis
    long now() {
        return time().currentTimeMillis();
    }

    // return the given time formatted as hh:mm:ss.nnn
    String hmsn(long ts) {
        return time().hhmmssnnn(ts);
    }

}
