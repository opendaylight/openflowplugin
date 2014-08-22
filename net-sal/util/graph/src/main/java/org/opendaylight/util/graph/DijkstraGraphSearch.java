/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.Comparator;
import java.util.Set;

import org.opendaylight.util.heap.Heap;

/**
 * Implementation of the Dijkstra shortest-path graph search algorithm.
 *
 * @author Thomas Vachuska
 */
public class DijkstraGraphSearch extends AbstractGraphPathSearch {
    
    @Override
    public Result search(Graph g, Vertex src, Vertex dst, EdgeWeight ew) {
        validate(g, src, dst);
        
        // Accrue parent edges and cumulative costs to reach each vertex
        DefaultResult gsr = new DefaultResult();

        // Set the cost of the source node to 0.
        gsr.update(src, null, 0.0, false);
        
        Heap<Vertex> minQ = createMinHeap(g, gsr);
        while (!minQ.isEmpty()) {
            Vertex nearest = minQ.extractExtreme();
            if (nearest.equals(dst))
                break;
            
            double cost = gsr.cost(nearest);
            if (cost < Double.MAX_VALUE) {
                // Visit all edges leading out from the nearest vertex
                for (Edge e : g.getEdgesFrom(nearest))
                    gsr.relaxEdge(e, cost, ew);
            }

            minQ.heapify();
        }
        
        // Now construct a set of paths from the results
        buildPaths(src, dst, gsr);
        return gsr;
    }

    // Create a heap from the graph vertices and graph search result where
    // the vertex path costs will be accrued. 
    private Heap<Vertex> createMinHeap(Graph g, DefaultResult gsr) {
        Set<Vertex> vs = g.getVertices();
        Vertex va[] = new Vertex[vs.size()];
        return new Heap<>(vs.toArray(va), va.length,
                          new PathWeightComparator(gsr));
    }
    
    // Comparator for path weights using accrued path costs.
    private static class PathWeightComparator implements Comparator<Vertex> {
        
        private final DefaultResult gsr;
        
        private PathWeightComparator(DefaultResult gsr) {
            this.gsr = gsr;
        }
        
        @Override
        public int compare(Vertex a, Vertex b) {
            double delta = gsr.cost(b) - gsr.cost(a);
            return delta < 0 ? -1 : (delta > 0 ? 1 : 0);
        }
        
    }
    
}
