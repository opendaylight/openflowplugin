/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.*;

import java.util.Set;

/**
 * Service for retrieving network topology &amp; connectivity information
 * between infrastructure devices.
 *
 * @author Thomas Vachuska
 */
public interface TopologyService {

    // TODO: Consider how to treat multiple topologies concurrently

    /**
     * Returns the current topology information.
     *
     * @return topology descriptor
     */
    Topology getTopology();

    /**
     * Indicates, whether or not a path between two infrastructure devices is
     * viable.
     *
     * @param src id of the source device
     * @param dst if of the destination device
     * @return true if path exists / is viable
     */
    boolean isPathViable(DeviceId src, DeviceId dst);

    /**
     * Returns all shortest paths between the specified source and destination
     * infrastructure devices, measuring link edge weight using hop count.
     *
     * @param src id of the source device
     * @param dst if of the destination device
     * @return set of paths or an empty set if no path exists
     */
    Set<Path> getPaths(DeviceId src, DeviceId dst);

    /**
     * Returns all shortest paths between the specified source and destination
     * infrastructure devices using the supplied link edge weight function.
     * <p>
     * The {@link LinkWeight#weight(org.opendaylight.net.model.Link)} may return a
     * negative number to indicate that any path traversing that link should
     * be considered as not viable.
     *
     * @param src    id of the source device
     * @param dst    if of the destination device
     * @param weight link edge weight function
     * @return set of paths or an empty set if no path exists
     */
    Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight);

    /**
     * Indicates whether or not the specified connection point is part of
     * infrastructure. This means it has been detected as an end-point of at
     * least one direct or tunnel infrastructure link.
     * <p>
     * Note that multi-hop link end-points are not considered as infrastructure
     * because hosts can be located on those connection points and broadcast
     * should be allowed for traffic ingressing from those ports.
     *
     * @param point connection point to test
     * @return true if the connection point belongs to the infrastructure
     */
    boolean isInfrastructure(ConnectionPoint point);

    /**
     * Indicates whether or not the specified connection point is allowed to
     * be used for traffic broadcast.
     *
     * @param point connection point to test
     * @return true if the connection point can be used to broadcast traffic
     */
    boolean isBroadcastAllowed(ConnectionPoint point);

    /**
     * Returns the set of clusters in the current topology.
     *
     * @return set of topology clusters
     *
     * @see org.opendaylight.net.model.TopologyCluster
     */
    Set<TopologyCluster> getClusters();

    /**
     * Retrieves the cluster in which the specified infrastructure devices is
     * located.
     *
     * @param deviceId id of the infrastructure device
     * @return topology cluster containing the device
     */
    TopologyCluster getCluster(DeviceId deviceId);

    /**
     * Returns the set of devices contained within the specified cluster.
     *
     * @param cluster topology cluster
     * @return set of device ids
     */
    Set<DeviceId> getClusterDevices(TopologyCluster cluster);

    /**
     * Registers a listener for only topology-related events. This registration
     * will override any prior listener registrations for the same object.
     *
     * @param listener the event listener
     */
    void addListener(TopologyListener listener);

    /**
     * Unregisters a listener from topology-related events, regardless of which
     * topology-related events the listener was registered to receive.
     *
     * @param listener the event listener
     * @throws IllegalArgumentException if the listener was not registered
     */
    void removeListener(TopologyListener listener);

    /**
     * Returns the set of listeners for topology-related events.
     *
     * @return set of listeners
     */
    Set<TopologyListener> getListeners();

}
