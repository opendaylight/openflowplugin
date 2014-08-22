/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.Set;

/**
 * Abstraction of an immutable directed graph.
 *
 * @author Thomas Vachuska
 */
public interface Graph {

    /**
     * Gets the set of all graph vertices.
     * 
     * @return set of all vertices
     */
    Set<Vertex> getVertices();
    
    /**
     * Gets the set of all graph edges.
     * 
     * @return set of all edges
     */
    Set<Edge> getEdges();

    /**
     * Gets the set of all edges leading from the given source vertex.
     * 
     * @param src source vertex
     * @return set of all outbound edges
     */
    Set<Edge> getEdgesFrom(Vertex src);

    /**
     * Gets the set of all edges leading from the given source vertex.
     * 
     * @param dst target vertex
     * @return set of all inbound edges
     */
    Set<Edge> getEdgesTo(Vertex dst);

}
