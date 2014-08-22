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
 * Adapter implementation of the {@link KeyService}.
 * 
 * @author Sean Humphress
 */
public class KeyServiceAdapter implements KeyService {
    @Override public Set<Key> getKeys(KeyType type) { return null; }
    @Override public Set<Key> getKeys(KeyType type, String label) { return null; }
    @Override public Key getKey(String id) { return null; }
    @Override public void addKey(Key key) { }
    @Override public void removeKey(String id) { }
}
