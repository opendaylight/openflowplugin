/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

/**
 * Test of the DFS algorithm.
 *
 * @author Thomas Vachuska
 */
public class DepthFirstRecursiveSearchTest extends DepthFirstSearchTest {

    @Override
    protected GraphPathSearch graphSearch() {
        return new DepthFirstRecursiveSearch();
    }
    
}
