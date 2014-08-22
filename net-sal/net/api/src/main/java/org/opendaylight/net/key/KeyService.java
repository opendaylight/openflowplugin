/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.key;

import java.util.Set;

/**
 * Service for managing device keys.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
public interface KeyService {

    /**
     * Returns the set of all keys of the specified type.
     *
     * @param type key type
     * @return set of keys
     */
    Set<Key> getKeys(KeyType type);

    /**
     * Returns the set of all keys of the specified type and matching the
     * specified label substring.
     *
     * @param type of key
     * @param label substring to match in the key labels
     * @return set of matching keys
     */
    Set<Key> getKeys(KeyType type, String label);

    /**
     * Returns the key matching the specified id.
     *
     * @param id key id
     * @return the matching key
     */
    Key getKey(String id);

    /**
     * Adds the supplied key to the key inventory.
     *
     * @param key key to be added
     */
    void addKey(Key key);

    /**
     * Remove the specified key from the key inventory.
     *
     * @param id id of the key to be removed
     */
    void removeKey(String id);

}
