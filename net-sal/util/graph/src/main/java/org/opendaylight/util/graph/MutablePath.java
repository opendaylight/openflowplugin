/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

/**
 * Abstraction of a mutable path through a directed graph.
 *
 * @author Thomas Vachuska
 */
public interface MutablePath extends Path {

    /**
     * Sets the new path cost.
     * 
     * @param cost new path cost
     */
    void setCost(double cost);
    
    /**
     * Appends the given edge to the end of the path.
     * 
     * @param edge edge to be appended
     */
    void appendEdge(Edge edge);
    
    /**
     * Inserts the given edge to the start of the path.
     * 
     * @param edge edge to be inserted
     */
    void insertEdge(Edge edge);
    
    /**
     * Removes the given edge from the path.
     * 
     * @param edge edge to be removed
     */
    void removeEdge(Edge edge);

}
