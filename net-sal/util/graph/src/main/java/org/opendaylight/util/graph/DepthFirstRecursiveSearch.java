/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of a general DFS algorithm using recursion method.
 *
 * @author Thomas Vachuska
 */
public class DepthFirstRecursiveSearch extends DepthFirstSearch {
    
    @Override
    public Result search(Graph g, Vertex src, Vertex dst, EdgeWeight ew) {
        validate(g, src, dst);
        
        // Accrue parent edges and cumulative costs to reach each vertex
        SpanningTreeResult gsr = new SpanningTreeResult();
        Set<Vertex> finished = new HashSet<>();

        // Mark the source as visited with cost 0.0
        gsr.costs.put(src, 0.0);
        
        explore(g, src, dst, 0.0, gsr, finished, ew);
        
        // Now construct a set of paths from the results
        buildPaths(src, dst, gsr);
        return gsr;
    }
    
    /**
     * Recursively explores the given graph, starting with the specified vertex.
     * 
     * @param g graph to explore
     * @param v vertex to start from
     * @param dst optional target vertex; search will stop when found
     * @param cost current cost to reach the given vertex
     * @param gsr  graph search results
     * @param finished set of completed vertices
     * @param ew edge weight function
     * @return true of the exploration was exhaustive; false if halted early
     */
    private boolean explore(Graph g, Vertex v, Vertex dst, double cost, 
                            SpanningTreeResult gsr, Set<Vertex> finished, 
                            EdgeWeight ew) {
        // Visit all edges leading out from the current vertex
        for (Edge e : g.getEdgesFrom(v)) {
            Vertex nv = e.dst();
            
            if (!gsr.hasCost(nv)) {
                // If we have not visited this vertex yet, do so
                gsr.setEdge(e, EdgeType.TREE_EDGE);
                double newCost = cost + (ew == null ? 1.0 : ew.weight(e));
                gsr.update(nv, e, newCost, true);
                if (nv.equals(dst) || !explore(g, nv, dst, newCost, gsr, finished, ew)) 
                    return false;
                
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
        finished.add(v);
        return true;
    }
    
}
