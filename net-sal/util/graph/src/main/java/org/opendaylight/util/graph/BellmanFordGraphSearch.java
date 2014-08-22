/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;


/**
 * Implementation of the Bellman-Ford shortest-path graph search algorithm for
 * directed graphs with negative cycles.
 * 
 * @author Thomas Vachuska
 */
public class BellmanFordGraphSearch extends AbstractGraphPathSearch {
    
    @Override
    public Result search(Graph g, Vertex src, Vertex dst, EdgeWeight ew) {
        validate(g, src, dst);
        
        // Accrue parent edges and cumulative costs to reach each vertex
        DefaultResult gsr = new DefaultResult();

        // Set the cost of the source node to 0.
        gsr.update(src, null, 0.0, true);

        int max = g.getVertices().size() - 1;
        for (int i = 0; i < max; i++) {
            // Visit all edges and relax them as possible
            for (Edge e : g.getEdges())
                if (gsr.hasCost(e.src()))
                    gsr.relaxEdge(e, gsr.cost(e.src()), ew);
        }

        // Sweep through and mark any paths that contain negative cycles. 
        for (Edge e : g.getEdges())
            if (gsr.hasCost(e.src()))
                if (gsr.relaxEdge(e, gsr.cost(e.src()), ew))
                    gsr.remove(e.dst());

        // Now construct a set of paths from the results
        buildPaths(src, dst, gsr);
        return gsr;
    }

}
