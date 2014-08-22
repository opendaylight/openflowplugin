/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.net.VlanId;

/**
 * Vlan Port Pair
 *
 * Comware supports the same vlan id on different ports not being the same
 * broadcast domain.
 */
public interface VlanPortPair {

    // FIXME: fix documentation & move these to util-net

    /**
     * Vlan Id of the pair
     * @return vlan id
     */
    VlanId vid();

    /**
     * Port of the pair
     * @return port
     */
    IfIndex port();

    /**
     * Vlan tag type of the pair
     * @return vlan tag type
     */
    VlanTagType tag();
}
