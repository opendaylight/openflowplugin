/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.*;

/**
 * Implementation of the Tarjan algorithm using recursion. It searches the
 * supplied graph and produces results that contains information on clusters
 * of connectivity.
 *
 * @author Thomas Vachuska
 */
public class TarjanGraphSearch implements GraphSearch {

    /**
     * {@inheritDoc}
     * <p>
     * This implementation searches the supplied graph and produces results
     * that contains information on clusters of connectivity.
     * <p>
     * The {@link EdgeWeight#weight} may return a negative number to indicate
     * that the edge should not be considered as viable for traversal.
     *
     */
    @Override
    public ConnectivityClusterResult search(Graph g, EdgeWeight ew) {
        ConnectivityClusterResult gsr = new ConnectivityClusterResult(g);

        for (Vertex v : g.getVertices()) {
            VertexData d = gsr.data(v);
            if (d == null)
                connect(g, v, ew, gsr);
        }
        gsr.freeze();

        return gsr;
    }

    /**
     * Recursively scans the graph from the given vertex, producing strongly
     * connected components in the result.
     *
     * @param g graph to be searched
     * @param v current vertex
     * @param ew optional edge weight
     * @param gsr graph search result
     * @return vertex augmentation data produced for the given vertex
     */
    private VertexData connect(Graph g, Vertex v, EdgeWeight ew,
                               ConnectivityClusterResult gsr) {
        VertexData vd = gsr.addData(v);

        // Scan through the successors of v
        for (Edge e : g.getEdgesFrom(v)) {
            Vertex w = e.dst();

            // If there is an edge weight given and if it yields negative
            // weight for this link, ignore the link
            if (ew != null && ew.weight(e) < 0)
                continue;

            VertexData wd = gsr.data(w);
            if (wd == null) {
                // Successor has not been visited yet so process it.
                wd = connect(g, w, ew, gsr);
                vd.lowLink = Math.min(vd.lowLink, wd.lowLink);
            } else if (gsr.visited(wd)) {
                // Successor has been visited and hence is in the current cluster
                vd.lowLink = Math.min(vd.lowLink, wd.index);
            }
        }

        if (vd.lowLink == vd.index)
            gsr.addCluster(vd);

        return vd;
    }

    /**
     * Graph search result which includes connectivity clusters.
     *
     * @author Thomas Vachuska
     */
    public static class ConnectivityClusterResult implements Result {

        private int index = 0;
        private final Graph graph;
        private final Map<Vertex, VertexData> data = new HashMap<>();
        private final List<VertexData> visited = new ArrayList<>();
        private List<Set<Vertex>> clusterVertices = new ArrayList<>();
        private List<Set<Edge>> clusterEdges = new ArrayList<>();

        private ConnectivityClusterResult(Graph graph) {
            this.graph = graph;
        }

        public int clusterCount() {
            return clusterEdges.size();
        }

        /**
         * Returns the list of strongly connected vertex clusters.
         *
         * @return list of strongly connected vertex sets
         */
        public List<Set<Vertex>> clusterVertices() {
            return clusterVertices;
        }

        /**
         * Returns the list of edges linking strongly connected vertex clusters.
         *
         * @return list of strongly connected edge sets
         */
        public List<Set<Edge>> clusterEdges() {
            return clusterEdges;
        }

        // Gets the augmentation data for the specified vertex
        private VertexData data(Vertex v) {
            return data.get(v);
        }

        // Adds augmentation data for the specified vertex
        private VertexData addData(Vertex v) {
            VertexData d = new VertexData(v, index);
            data.put(v, d);
            visited.add(0, d);
            index++;
            return d;
        }

        // Indicates whether the given vertex has been visited
        private boolean visited(VertexData vd) {
            return visited.contains(vd);
        }

        // Adds a new cluster for the specified vertex
        private void addCluster(VertexData vd) {
            Set<Vertex> vertices = findClusterVertices(vd);
            Set<Edge> edges = findClusterEdges(vertices);

            clusterVertices.add(vertices);
            clusterEdges.add(edges);
        }

        private Set<Vertex> findClusterVertices(VertexData vd) {
            VertexData wd;
            Set<Vertex> vertices = new HashSet<>();
            do {
                wd = visited.remove(0);
                vertices.add(wd.v);
            } while (vd != wd);
            return Collections.unmodifiableSet(vertices);
        }

        private Set<Edge> findClusterEdges(Set<Vertex> vertices) {
            Set<Edge> edges = new HashSet<>();
            for (Vertex v : vertices) {
                for (Edge e : graph.getEdgesFrom(v)) {
                    if (vertices.contains((e.dst())))
                        edges.add(e);
                }
            }
            return Collections.unmodifiableSet(edges);
        }

        public void freeze() {
            clusterVertices = Collections.unmodifiableList(clusterVertices);
            clusterEdges = Collections.unmodifiableList(clusterEdges);
        }
    }

    // Vertex augmentation data
    private static class VertexData {
        private final Vertex v;
        private int index;
        private int lowLink;

        private VertexData(Vertex v, int index) {
            this.v = v;
            this.index = index;
            this.lowLink = index;
        }
    }
    
}
