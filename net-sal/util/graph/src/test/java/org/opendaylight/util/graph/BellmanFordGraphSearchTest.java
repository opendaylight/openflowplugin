/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test of the BFS algorithm.
 *
 * @author Thomas Vachuska
 */
public class BellmanFordGraphSearchTest extends BreadthFirstSearchTest {

    @Override
    protected GraphPathSearch graphSearch() {
        return new BellmanFordGraphSearch();
    }
    
    @Test
    @Override 
    public void basics() {
        runBasics(5, 5.0, 7);
    }

    @Test
    public void defaultWeight() {
        weight = null;
        runBasics(3, 3.0, 10);
    }

    @Test
    public void runWithNegatives() {
        g = new AdjacencyListsGraph();
        addVertices();
        Vertex Z = new Vtx("Z");
        g.add(Z);
        
        addEdges();
        g.add(new Edg(G, Z, 1.0));
        g.add(new Edg(Z, G, -2.0));
        
        GraphPathSearch bfs = graphSearch();
        Set<Path> paths = bfs.search(g, A, H, weight).paths();
        assertEquals("incorrect paths count", 1, paths.size());

        Path p = paths.iterator().next();
        assertEquals("incorrect src", A, p.src());
        assertEquals("incorrect dst", H, p.dst());
        assertEquals("incorrect path length", 5, p.edges().size());
        assertEquals("incorrect path cost", 5.0, p.cost(), 0.1);
        
        paths = bfs.search(g, A, G, weight).paths();
        assertEquals("incorrect paths count", 0, paths.size());

        paths = bfs.search(g, A, null, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 6, paths.size());
    }

}
