/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Helper utlities for implementing model caches.
 *
 * @author Thomas Vachuska
 */
public class CacheUtils {

    /**
     * Returns an unmodifiable set of hosts from the supplied set.
     *
     * @param set item set; may be null
     * @return unmodifiable variant of the original
     */
    public static <T> Set<T> safeSet(Set<T> set) {
        return Collections.unmodifiableSet(set == null ? new HashSet<T>() : set);
    }


    /**
     * Adds the specified entity to the given map of sets, registering new set
     * as necessary.
     *
     * @param key    set key
     * @param map    of key-to-set
     * @param entity item to be added to set index
     */
    public static <T, E> void addToIndex(T key, Map<T, Set<E>> map, E entity) {
        Set<E> set = map.get(key);
        if (set == null) {
            set = new CopyOnWriteArraySet<>();
            map.put(key, set);
        }
        set.add(entity);
    }

    /**
     * Removes the specified entity from the given map of sets, pruning the set
     * as necessary.
     *
     * @param key    set key
     * @param map    of key-to-set
     * @param entity item to be added to set index
     */
    public static <T, E> void removeFromIndex(T key, Map<T, Set<E>> map, E entity) {
        Set<E> set = map.get(key);
        if (set != null) {
            set.remove(entity);
            if (set.isEmpty())
                map.remove(key);
        }
    }

}
