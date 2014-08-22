/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.Vni;

import java.util.Set;
/**
 * Represents VxLAN Attributes
 * @author Anuradha Musunuri
 *
 */
public interface VxlanInfo {
    /**
     * Returns the tunnel interface index of this tunnel.
     * @return tunnel index
     */
    BigPortNumber index();

    /**
     * Returns the local VTEP address.
     * @return ip address of the near side of the tunnel
     */
    IpAddress localAddress();

    /**
     * Returns the remote VTEP address.
     * @return ip address of the far side of the tunnel
     */
    IpAddress remoteAddress();
    /**
     * Returns the set of VNIs with which this tunnel has been associated.
     *
     * @return set of VNIs associated with the numbers
     */
    Set<Vni> vnis();
    
}
