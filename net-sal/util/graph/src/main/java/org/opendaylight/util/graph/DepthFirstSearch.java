/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Implementation of a general DFS algorithm using iteration method.
 *
 * @author Thomas Vachuska
 */
public class DepthFirstSearch extends AbstractGraphPathSearch {
    
    /** Classification of edges. */
    public static enum EdgeType {
        TREE_EDGE, FORWARD_EDGE, BACK_EDGE, CROSS_EDGE
    }

    @Override
    public Result search(Graph g, Vertex src, Vertex dst, EdgeWeight ew) {
        validate(g, src, dst);
        
        // Accrue parent edges and cumulative costs to reach each vertex
        SpanningTreeResult gsr = new SpanningTreeResult();
        Set<Vertex> finished = new HashSet<>();

        // Mark the source as visited with cost 0.0
        gsr.costs.put(src, 0.0);
        
        Stack<Vertex> stack = new Stack<>();
        stack.push(src);
        
        while (!stack.isEmpty()) {
            Vertex v = stack.peek();
            if (v.equals(dst))
                break;
            
            double cost = gsr.cost(v);  // what did it cost to get to v?
            boolean tangent = false;
            
            // Visit all edges leading out from the current vertex
            for (Edge e : g.getEdgesFrom(v)) {
                // Skip over any edges that we have already seen
                if (gsr.edgeSeen(e))
                    continue;
                
                // Consider the target of the current edge
                Vertex nv = e.dst();
                if (!gsr.hasCost(nv)) {
                    // If we have not started and finished this vertex yet, do so
                    gsr.setEdge(e, EdgeType.TREE_EDGE);
                    gsr.update(nv, e, cost + (ew == null ? 1.0 : ew.weight(e)), true);
                    stack.push(nv);
                    tangent = true;
                    break;
                    
                } else if (!finished.contains(nv)) {
                    // If we have not finished the target, we have a back-edge
                    gsr.setEdge(e, EdgeType.BACK_EDGE);
                } else {
                    // If we have finished the target, the edge is either
                    // a cross-edge or forward-edge
                    gsr.setEdge(e, isForwardEdge(gsr, e) ? 
                            EdgeType.FORWARD_EDGE : EdgeType.CROSS_EDGE);
                }
            }
            
            // If we've exited the edge scan without being sent on a tangent
            // mark the node as finished and pop it off the stack
            if (!tangent) {
                finished.add(v);
                stack.pop();
            }
        }
        
        // Now construct a set of paths from the results
        buildPaths(src, dst, gsr);
        return gsr;
    }

    /**
     * Determines whether the specified edge is a forward edge using the
     * currently accrued set of vertex parent edges.
     * 
     * @param gsr search result
     * @param e edge to be classified
     * @return true if the edge is a forward edge
     */
    protected boolean isForwardEdge(DefaultResult gsr, Edge e) {
        // Follow the parent edges until we hit the edge source vertex
        Vertex t = e.src();
        Vertex v = e.dst();
        Set<Edge> pes;
        while ((pes = gsr.parents.get(v)) != null) {
            for (Edge pe : pes) {
                v = pe.src();
                if (v.equals(t))
                    return true;
            }
        }
        return false;
    }

    /**
     * Graph search result which includes edge classification for building 
     * a spanning tree.
     *
     * @author Thomas Vachuska
     */
    public class SpanningTreeResult extends DefaultResult {

        protected final Map<Edge, EdgeType> edges = new HashMap<>();

        /**
         * Returns the map of edge classifications.
         * 
         * @return map of edge to edge type bindings
         */
        public Map<Edge, EdgeType> edges() {
            return edges;
        }
        
        void setEdge(Edge e, EdgeType type) {
            edges.put(e, type);
        }
        
        boolean edgeSeen(Edge e) {
            return edges.containsKey(e);
        }
        
    }
    
}
