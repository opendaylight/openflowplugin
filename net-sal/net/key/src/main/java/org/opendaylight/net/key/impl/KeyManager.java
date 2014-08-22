/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.key.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.opendaylight.net.key.Key;
import org.opendaylight.net.key.KeyService;
import org.opendaylight.net.key.KeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Provides implementation of {@link KeyService}.
 *
 * @author Thomas Vachuska
 */
@Service
@Component(immediate = true, metatype = false)
public class KeyManager implements KeyService {

    private final Logger log = LoggerFactory.getLogger(KeyManager.class);

    // FIXME: implement in-memory (with flush to on-disk)

    @Override
    public Set<Key> getKeys(KeyType type) {
        return null;
    }

    @Override
    public Set<Key> getKeys(KeyType type, String label) {
        return null;
    }

    @Override
    public Key getKey(String id) {
        return null;
    }

    @Override
    public void addKey(Key key) {
    }

    @Override
    public void removeKey(String id) {
    }

}
