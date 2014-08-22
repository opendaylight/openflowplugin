/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

/**
 * Representation of a directed graph edge.
 *
 * @author Thomas Vachuska
 */
public interface Edge {

    /**
     * Returns the source vertex.
     * 
     * @return source vertex
     */
    Vertex src();

    /**
     * Returns the destination vertex.
     * 
     * @return destination/target vertex
     */
    Vertex dst();
    
}
