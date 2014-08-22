/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.path;

import org.opendaylight.net.model.ConnectionPoint;
import org.opendaylight.net.model.Host;
import org.opendaylight.net.model.Path;
import org.opendaylight.net.topology.LinkWeight;

import java.util.Set;

/**
 * Service for selecting end-to-end paths between network end-station nodes.
 *
 * @author Thomas Vachuska
 */
public interface PathSelectionService {

    /**
     * Returns the set of shortest path between the specified source and
     * destination nodes, measuring link edge weight using hop count.
     *
     * @param src source node
     * @param dst destination node
     * @return set of paths
     */
    Set<Path> getPaths(Host src, Host dst);

    /**
     * Returns the set of shortest paths between the specified source and
     * destination nodes using the supplied link edge weight function.
     * <p/>
     * The {@link org.opendaylight.net.topology.LinkWeight#weight(org.opendaylight.net.model.Link)}
     * may return a negative number to indicate that any path traversing that
     * link should be considered as not viable.
     *
     * @param src    source node
     * @param dst    destination node
     * @param weight link edge weight function
     * @return path or null if the path does not exist
     */
    Set<Path> getPaths(Host src, Host dst, LinkWeight weight);

    /**
     * Indicates whether or not the specified connection point is allowed to
     * be used for traffic broadcast.
     *
     * @param point connection point to test
     * @return true if the connection point can be used to broadcast traffic
     */
    boolean isBroadcastAllowed(ConnectionPoint point);
    // TODO: Consider whether to eliminate the above

}
