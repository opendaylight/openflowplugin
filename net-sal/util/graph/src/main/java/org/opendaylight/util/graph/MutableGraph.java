/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

/**
 * Abstraction of a mutable directed graph.
 *
 * @author Thomas Vachuska
 */
public interface MutableGraph extends Graph {
    
    /**
     * Adds the specified vertex to the graph.
     * 
     * @param vertex vertex to be added
     * @return true if the vertex was added; false it it was already part of
     *         the graph
     */
    boolean add(Vertex vertex);
    
    /**
     * Deletes the specified vertex from the graph.
     * 
     * @param vertex vertex to be removed
     * @return true if the vertex was removed; false if the vertex was not
     *         part of the graph
     */
    boolean remove(Vertex vertex);
    
    /**
     * Adds the specified edge to the graph.
     * 
     * @param edge edge to be added
     * @return true if the edge was added; false it it was already part of
     *         the graph
     */
    boolean add(Edge edge);

    /**
     * Deletes the specified edge from the graph.
     * 
     * @param edge edge to be removed
     * @return true if the edge was removed; false if the edge was not
     *         part of the graph
     */
    boolean remove(Edge edge);
    
}
