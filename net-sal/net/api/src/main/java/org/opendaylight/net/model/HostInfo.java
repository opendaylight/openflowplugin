/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.MacAddress;

/**
 * Key information describing a node and it's location as observed in the
 * environment.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
public interface HostInfo {

    /**
     * Returns the node network interface.
     *
     * @return network interface; null if unknown
     */
    Interface netInterface();

    /**
     * Returns the node MAC address.
     *
     * @return node MAC; null if unknown
     */
    MacAddress mac();

    /**
     * Returns the most recent node location.
     *
     * @return node location; null if unknown
     */
    HostLocation location();

}