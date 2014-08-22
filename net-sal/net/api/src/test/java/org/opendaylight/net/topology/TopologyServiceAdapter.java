/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.net.model.*;

import java.util.Set;

/**
 * An adapter for the {@link org.opendaylight.net.topology.TopologyService} API, provided
 * specifically for unit tests and implementers to use, to insulate from
 * changes in the API.
 *
 * @author Thomas Vachuska
 */
public class TopologyServiceAdapter implements TopologyService {
    @Override public Topology getTopology() { return null; }
    @Override public boolean isPathViable(DeviceId src, DeviceId dst) { return false; }
    @Override public Set<Path> getPaths(DeviceId src, DeviceId dst) { return null; }
    @Override public Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight) { return null; }
    @Override public boolean isInfrastructure(ConnectionPoint point) { return false; }
    @Override public boolean isBroadcastAllowed(ConnectionPoint point) { return false; }
    @Override public Set<TopologyCluster> getClusters() { return null; }
    @Override public TopologyCluster getCluster(DeviceId deviceId) { return null; }
    @Override public Set<DeviceId> getClusterDevices(TopologyCluster cluster) { return null; }
    @Override public void addListener(TopologyListener listener) { }
    @Override public void removeListener(TopologyListener listener) { }
    @Override public Set<TopologyListener> getListeners() { return null; }
}
