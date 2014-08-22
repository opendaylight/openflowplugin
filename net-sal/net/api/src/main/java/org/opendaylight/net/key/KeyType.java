/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.key;

/**
 * Token representation of a type of device key
 *
 * @author Thomas Vachuska
 */
public interface KeyType {

    /**
     * Name of the key type
     *
     * @return key type name
     */
    String name();
}
