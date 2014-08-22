/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.Map;
import java.util.Set;

/**
 * Abstraction of a general directed graph search for paths.
 *
 * @author Thomas Vachuska
 */
public interface GraphPathSearch {

    /**
     * Representation of a graph path search result.
     */
    public interface Result {

        /**
         * Returns the set of paths accrued during the search.
         * 
         * @return set of paths
         */
        Set<Path> paths();

        /**
         * Return a map of vertex parent edges in the path.
         * 
         * @return map of vertex to parent edge bindings
         */
        public Map<Vertex, Set<Edge>> parents();
        
        /**
         * Return a map of vertex costs in the path.
         * 
         * @return map of vertex to cost bindings
         */
        public Map<Vertex, Double> costs();

    }

    /**
     * Search the graph producing a set of paths.
     * 
     * @param g graph to be searched
     * @param src source node; implementations may leave this as optional
     * @param dst destination node; implementations may leave this as optional
     * @param ew optional edge weight function
     * @return search result
     */
    Result search(Graph g, Vertex src, Vertex dst, EdgeWeight ew);

}
