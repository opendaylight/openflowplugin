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
 * Implementation of a general BFS algorithm.
 *
 * @author Thomas Vachuska
 */
public class BreadthFirstSearch extends AbstractGraphPathSearch {

    @Override
    public Result search(Graph g, Vertex src, Vertex dst, EdgeWeight ew) {
        validate(g, src, dst);
        
        // Accrue parent edges and cumulative costs to reach each vertex
        DefaultResult gsr = new DefaultResult();

        // Prepare the initial frontier to comprise of only the source node
        Set<Vertex> frontier = new HashSet<>();
        gsr.costs.put(src, 0.0);
        frontier.add(src);
        
        search: while (!frontier.isEmpty()) {
            // Start collecting what will be the next frontier
            Set<Vertex> next = new HashSet<>();

            // Visit all frontier vertices
            for (Vertex v : frontier) {
                double cost = gsr.cost(v);
                
                // Visit all edges leading out from the current frontier vertex
                for (Edge e : g.getEdgesFrom(v)) {
                    Vertex nv = e.dst();
                    if (!gsr.hasCost(nv)) {
                        // If we have not visited this vertex yet, do so
                        gsr.update(nv, e, cost + (ew == null ? 1.0 : ew.weight(e)), true);
                        if (nv.equals(dst))
                            break search;
                        next.add(nv);
                    }
                }
            }
            
            // Move up the frontier to the next one
            frontier = next;
        }
        
        // Now construct a set of paths from the results
        buildPaths(src, dst, gsr);
        return gsr;
    }
    
}
