/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extends the functionality of {@link AgeOutHashMap} to capture the values
 * that are aged-out during interaction with the map, making them available
 * for consumption by an external party.
 *
 * @param <K> the key class
 * @param <V> the value class
 *
 * @author Simon Hunt
 */
public class NotedAgeOutHashMap<K, V> extends AgeOutHashMap<K, V> {

    // somewhere to store the deadwood
    private final Set<V> woodpile =
            Collections.synchronizedSet(new HashSet<V>());

    /**
     * Constructs a map with the initial age-out set to the default
     * number of milliseconds.
     * @see AgeOutHashMap#DEFAULT_AGE_OUT_MS
     */
    public NotedAgeOutHashMap() {
    }

    /**
     * Constructs a map with the specified initial age-out value (ms).
     * An exception will be thrown if the given value is below the allowed
     * minimum.
     *
     * @param ageOutMs the initial age-out value
     * @throws IllegalArgumentException if ageOutMs is less than the allowed
     *          minimum
     * @see AgeOutHashMap#MIN_AGE_OUT_MS
     */
    public NotedAgeOutHashMap(long ageOutMs) {
        super(ageOutMs);
    }
    
    /**
     * Constructs a map with the specified initial age-out value (ms) and
     * age-out deadwood only flag.  An exception will be thrown if the given
     * value is below the allowed minimum.
     *
     * @param ageOutMs the initial age-out value
     * @param ageOutDeadwoodOnly the initial age-out deadwood only value
     * @throws IllegalArgumentException if ageOutMs is less than the allowed
     *          minimum
     * @see AgeOutHashMap#MIN_AGE_OUT_MS
     */
    public NotedAgeOutHashMap(long ageOutMs, boolean ageOutDeadwoodOnly) {
        super(ageOutMs, ageOutDeadwoodOnly);
    }

    @Override
    protected void deadwood(V value) {
        woodpile.add(value);
    }

    @Override
    protected void deadwood(Set<V> values) {
        woodpile.addAll(values);
    }

    /**
     * Returns the set of aged-out values; note that this is a destructive
     * read operation.
     *
     * @return the set of silently aged-out values
     */
    public Set<V> clearDeadwood() {
        Set<V> v = new HashSet<V>(woodpile);
        woodpile.clear();
        return v;
    }
}
