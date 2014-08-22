/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.key;

/**
 * Abstraction of device keys, which can be community names, user/password,
 * certificates or other forms of credentials required for communicating with a device
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
public interface Key {

    // TODO: provide strong type for ID & type; cannot be enum because we want dynamic extensibility

    /**
     * Unique ID of the key.
     *
     * @return key id
     */
    String id();

    /**
     * Return the type of key
     *
     * @return key type
     */
    KeyType type();

    /**
     * Return the user provided label for the key
     *
     * @return user provided key label
     */
    String label();

}
