/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

/**
 * Abstraction of an entity capable of computing edge weights.
 *
 * @author Thomas Vachuska
 */
public interface EdgeWeight {

    /**
     * Return the weight of the specified edge.
     * 
     * @param edge edge to be weighted
     * @return unit-less edge weight
     */
    double weight(Edge edge);
    
}
