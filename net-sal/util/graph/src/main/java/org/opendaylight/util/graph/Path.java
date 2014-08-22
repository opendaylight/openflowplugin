/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.List;

/**
 * Abstraction of a path in a directed graph.
 *
 * @author Thomas Vachuska
 */
public interface Path {
    
    /**
     * Get the source vertex.
     * 
     * @return source vertex
     */
    Vertex src();

    /**
     * Get the target vertex.
     * 
     * @return destination/target vertex
     */
    Vertex dst();
    
    /**
     * Get the total cost of the path.
     * 
     * @return path cost
     */
    double cost();
    
    /**
     * Gets the list of all edges that form the path.
     *  
     * @return list of path edges
     */
    List<Edge> edges();

}
