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
 * Abstract base for various graph traversal or search implementations.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractGraphPathSearch implements GraphPathSearch {

    private static final double SAME_COST_THRESHOLD = 0.0000001;

    protected static final String E_SRC_NULL = "Source cannot be null";
    protected static final String E_GRAPH_NULL = "Graph cannot be null";
    protected static final String E_NOT_IN_GRAPH = "Source or destination not in graph";

    /**
     * Default graph search result.
     *
     * @author Thomas Vachuska
     */
    protected static class DefaultResult implements Result {

        protected final Set<Path> paths = new HashSet<>();
        protected final Map<Vertex, Set<Edge>> parents = new HashMap<>();
        protected final Map<Vertex, Double> costs = new HashMap<>();

        @Override
        public Set<Path> paths() {
            return paths;
        }

        @Override
        public Map<Vertex, Set<Edge>> parents() {
            return parents;
        }

        @Override
        public Map<Vertex, Double> costs() {
            return costs;
        }

        double cost(Vertex v) {
            Double c = costs.get(v);
            return c == null ? Double.MAX_VALUE : c;
        }

        boolean hasCost(Vertex v) {
            return costs.get(v) != null;
        }

        /**
         * Updates the cost of the vertex, reached via the given edge.
         *
         * @param v reached vertex
         * @param parent edge through which vertex is reached
         * @param cost cost to reach the vertex from the source
         * @param replace true to indicate previous edges are to be replaced;
         *                false to add to the existing edges as they yield
         *                the same cost
         */
        void update(Vertex v, Edge parent, double cost, boolean replace) {
            costs.put(v, cost);
            if (parent != null) {
                Set<Edge> edges = parents.get(v);
                if (edges == null) {
                    edges = new HashSet<>();
                    parents.put(v, edges);
                }
                if (replace)
                    edges.clear();
                edges.add(parent);
            }
        }

        void remove(Vertex v) {
            parents.remove(v);
        }

        /**
         * Relax the edge given if possible using the supplied base cost and
         * edge-weight function.
         *
         * @param e edge to be relaxed
         * @param cost base cost to reach the edge destination vertex
         * @param ew optional edge weight function
         * @return true if the edge was relaxed
         */
        boolean relaxEdge(Edge e, double cost, EdgeWeight ew) {
            Vertex v = e.dst();
            double oldCost = cost(v);
            double newCost = cost + (ew == null ? 1.0 : ew.weight(e));
            boolean relaxed = newCost < oldCost;
            boolean same = Math.abs(newCost - oldCost) < SAME_COST_THRESHOLD;
            if (relaxed || same)
                update(v, e, newCost, !same);
            return relaxed;
        }

    }

    /**
     * Builds a set of paths for the specified src/dst pair using the provided
     * maps of parents and costs.
     *
     * @param src source vertex
     * @param dst optional destination vertex; leave null for all target paths
     * @param gsr grapg search result
     */
    protected void buildPaths(Vertex src, Vertex dst, DefaultResult gsr) {
        if (src == null)
            throw new NullPointerException(E_SRC_NULL);

        Set<Vertex> targets = new HashSet<>();
        if (dst == null)
            targets.addAll(gsr.costs.keySet());
        else
            targets.add(dst);

        for (Vertex v : targets) {
            if (!v.equals(src))
                buildAllPaths(src, v, gsr);
        }
    }

    /**
     * Performs breadth-first search through the graph results represented by
     * the parents & cost structures and generates all paths between the source
     * and destination on the graph search result.
     *
     * @param src source vertex
     * @param dst destination vertex
     * @param gsr graph search result
     */
    private void buildAllPaths(Vertex src, Vertex dst, DefaultResult gsr) {
        DefaultPath basePath = new DefaultPath(src, dst);
        basePath.setCost(gsr.cost(dst));

        Set<DefaultPath> paths = new HashSet<>();
        paths.add(basePath);

        while (!paths.isEmpty()) {
            Set<DefaultPath> frontier = new HashSet<>();

            for (DefaultPath path : paths) {
                // For each pending path, find the first vertex
                Vertex v = firstVertex(path);

                // If the first vertex is the source, add the pending path
                // to the list of paths
                if (v.equals(path.src())) {
                    path.setCost(gsr.cost(path.dst()));
                    gsr.paths.add(path);

                } else {
                    // Otherwise, get the set of edges leading to the first
                    // vertex of this path; if there are none, bail
                    Set<Edge> pes = gsr.parents.get(v);
                    if (pes == null || pes.isEmpty())
                        break;

                    // Now iterate over all the edges cloning the current path
                    // for all, but the last (or only) edge and then insert
                    // that edge to the path.
                    Iterator<Edge> edges = pes.iterator();
                    while (edges.hasNext()) {
                        Edge edge = edges.next();
                        boolean isLast = !edges.hasNext();
                        DefaultPath pendingPath = isLast ? path : new DefaultPath(path);
                        pendingPath.insertEdge(edge);
                        frontier.add(pendingPath);
                    }
                }
            }

            // Having processed all pending paths, promote the next frontier
            paths = frontier;
        }
    }

    // Returns the first vertex of the given path, which is either the source
    // of the first segment or, if there are no segments, the path destination.
    private Vertex firstVertex(Path path) {
        return path.edges().isEmpty() ? path.dst() : path.edges().get(0).src();
    }

    /**
     * Validates that the given graph and source are not null and that the
     * source and destination vertices (if given) are part of the graph.
     *
     * @param g graph
     * @param src source vertex
     * @param dst optional target vertex
     */
    protected void validate(Graph g, Vertex src, Vertex dst) {
        if (g == null)
            throw new NullPointerException(E_GRAPH_NULL);
        if (src == null)
            throw new NullPointerException(E_SRC_NULL);
        Set<Vertex> vertices = g.getVertices();
        if (!vertices.contains(src) || (dst != null && !vertices.contains(dst)))
            throw new IllegalArgumentException(E_NOT_IN_GRAPH);
    }

}
