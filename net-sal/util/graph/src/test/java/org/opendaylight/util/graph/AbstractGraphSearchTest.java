/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import org.junit.Test;

/**
 * Base for all graph search tests.
 *
 * @author Thomas Vachuska
 */
public abstract class AbstractGraphSearchTest extends GraphTest {

    /**
     * Creates a graph search to be tested.
     * 
     * @return graph search
     */
    protected abstract GraphPathSearch graphSearch();
    
    @Test(expected=IllegalArgumentException.class)
    public void badSource() {
        graphSearch().search(new AdjacencyListsGraph(), A, H, weight);
    }

    @Test(expected=NullPointerException.class)
    public void nullSource() {
        graphSearch().search(new AdjacencyListsGraph(), null, H, weight);
    }

    @Test(expected=NullPointerException.class)
    public void nullGraph() {
        graphSearch().search(null, A, H, weight);
    }

}
