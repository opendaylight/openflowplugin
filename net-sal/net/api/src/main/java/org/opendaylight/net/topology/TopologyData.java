/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.util.graph.Graph;
import org.opendaylight.util.graph.GraphPathSearch;
import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.Link;
import org.opendaylight.net.model.TopologyCluster;

import java.util.Set;

/**
 * Carrier of processed topology data used for serving out paths, broadcast
 * trees, etc.
 *
 * @author Thomas Vachuska
 */
public interface TopologyData {

    /**
     * Returns the timestamp (in nanos) of when the topology data was generated.
     *
     * @return timestamp of the topology data
     */
    long ts();

    /**
     * Returns the graph model of the network. The vertexes are assumed to be
     * derivatives of {@link TopoVertex} and edges derivatives of
     * {@link TopoEdge} classes.
     *
     * @return network graph
     */
    Graph graph();

    /**
     * Returns the graph path search result containing paths from the given
     * source vertex to all other vertexes.
     *
     * @param deviceId source device id
     * @return graph search result
     */
    GraphPathSearch.Result searchResults(DeviceId deviceId);

    /**
     * Returns the set of connectivity clusters.
     *
     * @return set of topology clusters
     */
    Set<TopologyCluster> clusters();

    /**
     * Returns the topology cluster data containing the specified device.
     *
     * @param deviceId source device id
     * @return topology cluster data
     */
    TopologyCluster clusterFor(DeviceId deviceId);

    /**
     * Returns the set of devices that belong to the cluster.
     *
     * @return set of cluster devices
     */
    Set<DeviceId> clusterDevices(TopologyCluster cluster);

    /**
     * Returns the set of links that belong to the cluster.
     *
     * @return set of cluster links
     */
    Set<Link> clusterLinks(TopologyCluster cluster);

}
