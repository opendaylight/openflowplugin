/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Node Link is a logical association of a {@link org.opendaylight.net.model.ConnectionPoint}
 * and a {@link Host}.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface HostLink extends Link {

    /**
     * Returns the identifier of the node.
     *
     * @return node id
     */
    HostId nodeId();

    /**
     * Returns the node attachment point to the network.
     *
     * @return connection point
     */
    ConnectionPoint connectionPoint();

}
