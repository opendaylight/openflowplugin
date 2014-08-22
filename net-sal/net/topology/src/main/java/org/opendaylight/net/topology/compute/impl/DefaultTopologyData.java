/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.opendaylight.util.graph.*;
import org.opendaylight.net.model.*;
import org.opendaylight.net.topology.TopoEdge;
import org.opendaylight.net.topology.TopoVertex;
import org.opendaylight.net.topology.TopologyData;

import java.util.*;

import static org.opendaylight.util.graph.GraphPathSearch.Result;
import static org.opendaylight.util.graph.TarjanGraphSearch.ConnectivityClusterResult;
import static org.opendaylight.net.model.Link.Type.MULTIHOP_LINK;

/**
 * Implementation of topology data carrier.
 *
 * @author Thomas Vachuska
 */
public class DefaultTopologyData implements TopologyData {

    private static final String E_ALREADY_BUILT = "Data already built";

    private static final GraphPathSearch DIJKSTRA = new DijkstraGraphSearch();
    private static final GraphSearch TARJAN = new TarjanGraphSearch();

    private static final EdgeWeight CLUSTER_WEIGHT = new EdgeWeight() {
        @Override public double weight(Edge edge) {
            return ((TopoEdge) edge).link().type() == MULTIHOP_LINK ? -1 : 1;
        }
    };

    private final MutableGraph graph = new AdjacencyListsGraph();
    private final Map<DeviceId, TopoVertex> vertexMap = new HashMap<>();
    private final Map<DeviceId, Result> results = new HashMap<>();
    private final Map<DeviceId, TopologyCluster> deviceClusters = new HashMap<>();
    private final Map<TopologyCluster, Set<DeviceId>> clusterDevices = new HashMap<>();
    private final Map<TopologyCluster, Set<Link>> clusterLinks = new HashMap<>();

    private long computeTimeNano;
    private long timestamp;

    /**
     * Builds and processes the graph from the supplied link iterator.
     *
     * @param links link iterator
     */
    public void build(Iterator<Device> devices, Iterator<Link> links) {
        if (timestamp != 0)
            throw new IllegalStateException(E_ALREADY_BUILT);
        timestamp = System.nanoTime();
        addVertexes(devices);
        addEdges(links);
        searchGraphForPaths();
        computeClusters();
        computeTimeNano = System.nanoTime() - timestamp;
    }

    // Adds graph vertexes using the supplied device iterator.
    private void addVertexes(Iterator<Device> devices) {
        while (devices.hasNext()) {
            TopoVertex vertex = new TopoVertex(devices.next().id());
            graph.add(vertex);
            vertexMap.put(vertex.deviceId(), vertex);
        }
    }

    // Adds graph edges using the supplied link iterator.
    private void addEdges(Iterator<Link> links) {
        while (links.hasNext()) {
            Link l = links.next();
            graph.add(new TopoEdge(vertex(l.src()), vertex(l.dst()), l));
        }
    }

    // Searches graph using multi-path Dijkstra for all sources/targets using
    // hop-count as path cost.
    private void searchGraphForPaths() {
        // Use number of vertices as overhead cost for traversing multi-hop links
        // This will make the search use direct links first.
        final int mhCost = graph.getVertices().size();
        EdgeWeight weight = new EdgeWeight() {
            @Override public double weight(Edge edge) {
                return ((TopoEdge) edge).link().type() == MULTIHOP_LINK ? mhCost : 1;
            }
        };

        try {
            // Search graph for all sources to all targets
            for (TopoVertex src : vertexMap.values())
                results.put(src.deviceId(), DIJKSTRA.search(graph, src, null, weight));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Computes connectivity clusters using Tarjan DFS variant.
    private void computeClusters() {
        ConnectivityClusterResult result = (ConnectivityClusterResult)
                TARJAN.search(graph, CLUSTER_WEIGHT);

        List<Set<Vertex>> clusterVertices = result.clusterVertices();
        List<Set<Edge>> clusterEdges = result.clusterEdges();

        for (int i = 0, n = result.clusterCount(); i < n; i++) {
            Set<Vertex> vertexSet = clusterVertices.get(i);
            Set<Edge> edgeSet = clusterEdges.get(i);

            DefaultTopologyCluster cluster =
                    new DefaultTopologyCluster(i, vertexSet.size(), edgeSet.size());
            Set<DeviceId> ids = findClusterDeviceIds(vertexSet, cluster);
            Set<Link> links = findClusterLinks(edgeSet);

            // Set the 'least' device id as the cluster root.
            cluster.setRoot(findRootDeviceId(ids));

            // Register the device ids and links with the cluster descriptor
            clusterDevices.put(cluster, Collections.unmodifiableSet(ids));
            clusterLinks.put(cluster, Collections.unmodifiableSet(links));
        }
    }

    // Finds the 'least' device id from the given list of device ids.
    private DeviceId findRootDeviceId(Set<DeviceId> ids) {
        DeviceId least = null;
        for (DeviceId id : ids)
            if (least == null || id.toString().compareTo(least.toString()) < 0)
                least = id;
        return least;
    }

    // Scan through the set of cluster vertices and convert it to a set of
    // device ids; register the cluster by device id as well.
    private Set<DeviceId> findClusterDeviceIds(Set<Vertex> vertexSet,
                                               DefaultTopologyCluster cluster) {
        Set<DeviceId> ids = new HashSet<>(vertexSet.size());
        for (Vertex v : vertexSet) {
            DeviceId deviceId = ((TopoVertex) v).deviceId();
            ids.add(deviceId);
            deviceClusters.put(deviceId, cluster);
        }
        return ids;
    }

    private Set<Link> findClusterLinks(Set<Edge> edgeSet) {
        Set<Link> links = new HashSet<>(edgeSet.size());
        for (Edge e : edgeSet)
            links.add(((TopoEdge) e).link());
        return links;
    }

    // Retrieves or creates a vertex from the supplied connection point
    private TopoVertex vertex(ConnectionPoint cp) {
        DeviceId id = (DeviceId) cp.elementId();
        TopoVertex vertex = vertexMap.get(id);
        if (vertex == null) {
            vertex = new TopoVertex(id);
            vertexMap.put(id, vertex);
        }
        return vertex;
    }


    @Override
    public long ts() {
        return timestamp;
    }

    @Override
    public Graph graph() {
        return graph;
    }

    @Override
    public Result searchResults(DeviceId deviceId) {
        return results.get(deviceId);
    }

    @Override
    public Set<TopologyCluster> clusters() {
        return Collections.unmodifiableSet(clusterDevices.keySet());
    }

    @Override
    public TopologyCluster clusterFor(DeviceId deviceId) {
        return deviceClusters.get(deviceId);
    }

    @Override
    public Set<DeviceId> clusterDevices(TopologyCluster cluster) {
        // Cluster devices set is already unmodifiable; no need to wrap.
        return clusterDevices.get(cluster);
    }

    @Override
    public Set<Link> clusterLinks(TopologyCluster cluster) {
        return clusterLinks.get(cluster);
    }

    @Override
    public String toString() {
        return "DefaultTopologyData{ts=" + timestamp +
                ", computeTimeNano=" + computeTimeNano + '}';
    }

}
