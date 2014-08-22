/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class facilitates the creation of mappings that, once built, are immutable.
 * It is designated a "safe" map, because a (possibly default) value will always be
 * returned, regardless of whether the specified key is null, or has not been mapped.
 * <p>
 * Reverse lookups are also possible. Given a value, one can retrieve either the
 * {@link #getFirstKey first key} that maps to the value, or, in the case where no such
 * mapping exists, the default key (which may be null),
 * or {@link #getAllKeys a set of all keys} that map to the value, or, in the case
 * where no such mapping exists, an empty set.
 * <p>
 * For example, suppose you have two enumerations:
 * <pre>
 * public enum Roman { I, II, III, IV, V, XX }
 * public enum Greek { ALPHA, BETA, GAMMA }
 * </pre>
 * And you want to create the following mapping:
 * <pre>
 *   I   --&gt;  ALPHA
 *   II  --&gt;  ALPHA
 *   III --&gt;  ALPHA
 *   IV  --&gt;  BETA
 *   V   --&gt;  GAMMA
 *   (default: ALPHA)
 * </pre>
 * This is done by employing the {@code Builder} inner-class as an intermediary, as follows:
 * <pre>
 * private static final SafeMap&lt;Roman,Greek&gt; map =
 *     new SafeMap.Builder&lt;Roman,Greek&gt;(Greek.ALPHA)
 *         .add(Roman.I, Greek.ALPHA)
 *         .add(Roman.II, Greek.ALPHA)
 *         .add(Roman.III, Greek.ALPHA)
 *         .add(Roman.IV, Greek.BETA)
 *         .add(Roman.V, Greek.GAMMA)
 *         .build();
 * </pre>
 * Note that the default value is specified as a paramter to the builder constructor
 * and that all required mappings are added in a chained expression, ending with
 * the {@code build()} call to create the map instance.
 * <p>
 * Now we can look up values:
 * <pre>
 * map.get(Roman.I);                // returns Greek.ALPHA
 * map.get(Roman.IV);               // returns Greek.BETA
 * map.get(Roman.XX);               // returns Greek.ALPHA
 * map.get(null);                   // returns Greek.ALPHA
 * map.getFirstKey(Greek.ALPHA)     // returns Roman.I
 * map.getAllKeys(Greek.ALPHA)      // returns Set&lt;Roman&gt; containing I, II, III
 * </pre>
 * <p>
 * Under the hood, this class uses a {@link TreeMap} so that entries are kept sorted by their keys.
 * Thus, when using {@link #getFirstKey} one should expect the key that comes earliest in the sort
 * order (declaration order for enumeration constants).
 *
 * @param <K> the type of keys maintained by this safe map
 * @param <V> the type of mapped values
 *
 * @author Simon Hunt
 */
public class SafeMap<K,V> {

    private final Map<K,V> map;
    private final V defaultValue;
    private final K defaultKey;

    /** Private constructor.
     *
     * @param builder the builder used to create the safe map
     */
    private SafeMap(Builder<K,V> builder) {
        map = new TreeMap<K,V>(builder.map);
        defaultValue = builder.defaultValue;
        defaultKey = builder.defaultKey;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[defaultValue=").append(defaultValue)
                .append(", defaultKey=").append(defaultKey).append(", ")
                .append(map).append("]");
        return sb.toString();
    }

    /** Returns the value mapped to the given key, or the default value if this
     * map contains no mapping for the key. The default value is also returned if
     * {@code null} is specified as the key.
     *
     * @param key the key
     * @return the mapped value
     */
    public V get(K key) {
        if (key==null)
            return defaultValue;
        V val = map.get(key);
        return val==null ? defaultValue : val;
    }

    /** Return the first key that is mapped to the given value, or the default key
     * if no such mapping exists.
     *
     * @param value the value
     * @return the first key mapped to that value
     */
    public K getFirstKey(V value) {
        for (Map.Entry<K,V> entry : map.entrySet()) {
            if (entry.getValue().equals(value))
                return entry.getKey();
        }
        return defaultKey;
    }

    /** Return all keys that are mapped to the given value, or an empty set
     * if no such mapping exists.
     *
     * @param value the value
     * @return the set of keys that map to this value
     */
    public Set<K> getAllKeys(V value) {
        Set<K> matches = new HashSet<K>();
        for (Map.Entry<K,V> entry : map.entrySet()) {
            if (entry.getValue().equals(value))
                matches.add(entry.getKey());
        }
        return matches;
    }

    /** Returns the default value for this map.
     *
     * @return the default value
     */
    public V getDefaultValue() {
        return defaultValue;
    }

    /** Returns the default key for this map.
     *
     * @return the default key
     */
    public K getDefaultKey() {
        return defaultKey;
    }

    /** Returns the number of elements in the map.
     *
     * @return the size of the map
     */
    public int size() {
        return map.size();
    }


    /** SafeMap builder class. Note that each method returns a reference to the
     * builder instance, so that the method calls can be chained.
     *
     * @param <K> the type of keys maintained by this safe map
     * @param <V> the type of mapped values
     */
    public static class Builder<K,V> {
        private K defaultKey;
        private final V defaultValue;
        private final Map<K,V> map = new TreeMap<K,V>();

        /** Builder constructor that requires a mandatory default value.
         *
         * @param defaultValue the required default value for the map
         * @throws NullPointerException if the default value is null
         */
        public Builder(V defaultValue) {
            if (defaultValue==null)
                throw new NullPointerException(E_NULL_DEFAULT);

            this.defaultValue = defaultValue;
        }

        /** An optional default key can be set on the map, to be returned when
         *  no mapping is found via {@link #getFirstKey}.
         *
         * @param defaultKey the default key
         * @return self
         */
        public Builder<K,V> defaultKey(K defaultKey) {
            this.defaultKey = defaultKey;
            return this;
        }

        /** Adds a key/value pair to the map.
         *
         * @param key the key
         * @param value the value
         * @return self
         * @throws NullPointerException if either parameter is null
         */
        public Builder<K,V> add(K key, V value) {
            if (key==null)
                throw new NullPointerException(E_NULL_KEY);
            if (value==null)
                throw new NullPointerException(E_NULL_VALUE);

            map.put(key, value);
            return this;
        }

        /** Builds the safe map instance from this builder instance.
         *
         * @return a safe map instance (immutable)
         */
        public SafeMap<K,V> build() {
            return new SafeMap<K,V>(this);
        }
    }

    // package private for unit test access
    static final String E_NULL_DEFAULT = "default value cannot be null";
    static final String E_NULL_KEY = "key cannot be null";
    static final String E_NULL_VALUE = "value cannot be null";
}
