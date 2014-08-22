/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

/**
 * Abstraction of a general directed graph search.
 *
 * @author Thomas Vachuska
 */
public interface GraphSearch {
    
    /**
     * Representation of a graph search result.
     */
    public interface Result {
    }
    
    /**
     * Search the graph and produce a result of the search.
     *
     * @param g graph to be processed/searched
     * @param ew optional edge weight function
     * @return graph search result
     */
    Result search(Graph g, EdgeWeight ew);

}
