/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.impl;

import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;
import org.opendaylight.net.topology.LinkWeight;
import org.opendaylight.net.topology.TopoEdge;
import org.opendaylight.net.topology.TopoVertex;
import org.opendaylight.net.topology.TopologyData;
import org.opendaylight.util.TimeUtils;
import org.opendaylight.util.graph.DijkstraGraphSearch;
import org.opendaylight.util.graph.Edge;
import org.opendaylight.util.graph.EdgeWeight;
import org.opendaylight.util.graph.Vertex;

import java.util.*;

import static org.opendaylight.util.graph.GraphPathSearch.Result;

/**
 * Default topology implementation
 *
 * @author Thomas Vachuska
 */
class DefaultTopology extends AbstractModel implements Topology {

    private final static DijkstraGraphSearch DIJKSTRA = new DijkstraGraphSearch();

    // FIXME: cure long-term dependency on topology data

    private final TopologyData data;
    private final long activeAt; // FIXME: replace with use of timestamp()
    private final int clusterCount;
    private final int linkCount;

    private final Map<String, Set<Path>> paths = new HashMap<>();
    private final Set<DeviceId> deviceIds = new HashSet<>();
    private final Set<ConnectionPoint> connectionPoints = new HashSet<>();
    private final Map<TopologyCluster, Set<ConnectionPoint>> broadcastPoints = new HashMap<>();

    /**
     * Creates a new topology descriptor using the given topology data.
     *
     * @param supplierId id of the supplier
     * @param data       topology data
     * @param activeAt   timestamp of topology activation
     */
    public DefaultTopology(SupplierId supplierId, TopologyData data, long activeAt) {
        super(supplierId);
        this.data = data;

        this.activeAt = activeAt;
        this.clusterCount = data.clusters().size();
        this.linkCount = data.graph().getEdges().size();

        processDeviceIds();
        processPrecomputedPaths();
        processLinks();
        processBroadcastTrees();
    }

    // Extracts device ids from the topology data graph vertices
    private void processDeviceIds() {
        for (Vertex v : data.graph().getVertices())
            deviceIds.add(((TopoVertex) v).deviceId());
    }

    // Process all path search results and files paths between each pair of
    // reachable src/dst for later retrieval.
    private void processPrecomputedPaths() {
        for (DeviceId src : deviceIds) {
            for (org.opendaylight.util.graph.Path p : data.searchResults(src).paths()) {
                String key = key(src, ((TopoVertex) p.dst()).deviceId());
                Set<Path> sdp = paths.get(key);
                if (sdp == null) {
                    sdp = new HashSet<>();
                    paths.put(key, sdp);
                }
                sdp.add(path(p));
            }
        }
    }

    // Processes all links which comprise this topology.
    private void processLinks() {
        for (Edge e : data.graph().getEdges()) {
            TopoEdge te = (TopoEdge) e;
            Link link = te.link();
            // We're excluding multi-hop links from being considered infrastructure
            if (link.type() != Link.Type.MULTIHOP_LINK) {
                connectionPoints.add(link.src());
                connectionPoints.add(link.dst());
            }
        }
    }

    // Process all clusters and build a set of broadcast points for each
    private void processBroadcastTrees() {
        for (TopologyCluster cluster : data.clusters())
            broadcastPoints.put(cluster, findBroadcastPoints(cluster));
    }

    // Produce a set of broadcast points for the given cluster
    private Set<ConnectionPoint> findBroadcastPoints(TopologyCluster cluster) {
        Set<ConnectionPoint> points = new HashSet<>();

        // Find the graph search results of the topology cluster root node
        Result gsr = data.searchResults(cluster.root());

        // Add all connection points that lie along the paths between
        // the root and all other cluster devices.
        for (Map.Entry<Vertex, Set<Edge>> me : gsr.parents().entrySet()) {
            TopoVertex tv = (TopoVertex) me.getKey();
            // We only want to be concerned with parents that are in the same
            // cluster as we're processing
            if (!data.clusterFor(tv.deviceId()).equals(cluster))
                continue;

            Set<Edge> parents = me.getValue();
            if (parents.isEmpty())
                continue;

            // Add both the source and destination of  the first parent backlink.
            Link link = ((TopoEdge) parents.iterator().next()).link();
            points.add(link.src());
            points.add(link.dst());
        }

        return points;
    }

    // Converts a set of graph paths to a set of network paths
    static Set<Path> paths(Set<org.opendaylight.util.graph.Path> paths) {
        Set<Path> tp = new HashSet<>(paths.size());
        for (org.opendaylight.util.graph.Path p : paths)
            tp.add(path(p));
        return tp;
    }

    // Converts graph path to a network path
    static Path path(org.opendaylight.util.graph.Path path) {
        List<Link> links = new ArrayList<>();
        for (Edge edge : path.edges())
            links.add(((TopoEdge) edge).link());
        return new DefaultPath(links);
    }


    /**
     * Indicates whether or not a path between the given src/dst pair is viable.
     * @param src path source
     * @param dst path destination
     * @return true if path is viable
     */
    boolean isPathViable(DeviceId src, DeviceId dst) {
        Set<Path> ps = paths.get(key(src, dst));
        return ps != null && !ps.isEmpty();
    }

    /**
     * Get the set of shortest paths, hop-wise, for the supplied src/dst pair.
     * @param src path source
     * @param dst path destination
     * @return set of shortest paths between the src/dst pair
     */
    Set<Path> getPaths(DeviceId src, DeviceId dst) {
        return paths.get(key(src, dst));
    }

    /**
     * Get the set of shortest paths computed using the supplied edge weight.
     *
     * @param src path source
     * @param dst path destination
     * @param weight link edge weight
     * @return set of shortest paths between the src/dst pair
     */
    Set<Path> getPaths(DeviceId src, DeviceId dst, LinkWeight weight) {
        return paths(DIJKSTRA.search(data.graph(),
                                     new TopoVertex(src), new TopoVertex(dst),
                                     weight(weight)).paths());
    }

    /**
     * Returns indication whether the given connection point belongs to an
     * infrastructure link. Note that multi-hop links are not considered as such
     * because hosts can be located on the same ports and broadcast should be
     * allowed on traffic ingressing from those ports.
     *
     * @param point connection point
     * @return true if the connection point is an infrastructure link end-point
     */
    boolean isInfrastructure(ConnectionPoint point) {
        return connectionPoints.contains(point);
    }

    /**
     * Returns indication whether the given connection point belongs to a
     * broadcast tree.
     *
     * @param point connection point
     * @return true if the connection point belongs to a broadcast tree
     */
    boolean isBroadcastAllowed(ConnectionPoint point) {
        // Any connection point that is not part of infrastructure is OK
        if (!isInfrastructure(point))
            return true;

        // Find the broadcast points for the cluster to which the device belongs
        TopologyCluster cluster = data.clusterFor((DeviceId) point.elementId());
        Set<ConnectionPoint> points = broadcastPoints.get(cluster);

        // It's OK if the set of points is empty or it contains our point
        return points == null || points.isEmpty() || points.contains(point);
    }

    /**
     * Returns the set of clusters in the topology.
     *
     * @return set of clusters
     */
    Set<TopologyCluster> getClusters() {
        return data.clusters();
    }

    /**
     * Returns the descriptor of the cluster containing the given device.
     *
     * @param deviceId id device to search for
     * @return cluster containing the device; null if device not found
     */
    TopologyCluster getCluster(DeviceId deviceId) {
        return data.clusterFor(deviceId);
    }

    /**
     * Returns the set of devices contained in the specified cluster.
     *
     * @param cluster cluster
     * @return set of identifiers of devices contained by the cluster
     */
    Set<DeviceId> getClusterDevices(TopologyCluster cluster) {
        return data.clusterDevices(cluster);
    }


    // Produces an edge weight from the supplied link weight function.
    private EdgeWeight weight(final LinkWeight weight) {
        return new EdgeWeight() {
            @Override public double weight(Edge edge) {
                return weight.weight(((TopoEdge) edge).link());
            }
        };
    }

    // Generates a key for filing sets of paths between src/dst pairs.
    private String key(DeviceId src, DeviceId dst) {
        return src + "~" + dst;
    }

    /**
     * Returns the backing topology data.
     *
     * @return backing topology data
     */
    public TopologyData data() {
        return data;
    }

    @Override
    public long activeAt() {
        return activeAt;
    }

    @Override
    public int clusterCount() {
        return clusterCount;
    }

    @Override
    public int deviceCount() {
        return deviceIds.size();
    }

    @Override
    public int linkCount() {
        return linkCount;
    }

    @Override
    public String toString() {
        return "DefaultTopology{" +
                "supplierId=" + supplierId() +
                ", activeAt=" + TimeUtils.rfc822Timestamp(activeAt) +
                ", deviceCount=" + deviceIds.size() +
                ", clusterCount=" + clusterCount +
                ", linkCount=" + linkCount +
                ", data=" + data +
                '}';
    }

}
