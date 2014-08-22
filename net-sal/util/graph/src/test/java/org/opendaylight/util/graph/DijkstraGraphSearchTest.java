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

import static org.opendaylight.util.graph.GraphPathSearch.Result;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of the BFS algorithm.
 *
 * @author Thomas Vachuska
 */
public class DijkstraGraphSearchTest extends BreadthFirstSearchTest {

    @Override
    protected GraphPathSearch graphSearch() {
        return new DijkstraGraphSearch();
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
    public void noPath() {
        g = new AdjacencyListsGraph();
        assertTrue("failed vertex add", g.add(A));
        assertTrue("failed vertex add", g.add(B));
        assertTrue("failed vertex add", g.add(C));
        assertTrue("failed vertex add", g.add(D));
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, A, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, C, 1)));

        GraphPathSearch gs = graphSearch();
        Set<Path> paths = gs.search(g, A, B, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 1, paths.size());
        assertEquals("incorrect path cost", 1.0, paths.iterator().next().cost(), 0.1);

        paths = gs.search(g, A, D, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 0, paths.size());

        paths = gs.search(g, A, null, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 1, paths.size());
        assertEquals("incorrect path cost", 1.0, paths.iterator().next().cost(), 0.1);
    }

    @Test
    public void multiPath1() {
        g = new AdjacencyListsGraph();
        assertTrue("failed vertex add", g.add(A));
        assertTrue("failed vertex add", g.add(B));
        assertTrue("failed vertex add", g.add(C));
        assertTrue("failed vertex add", g.add(D));
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(A, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));

        GraphPathSearch gs = graphSearch();
        Set<Path> paths = gs.search(g, A, D, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 2, paths.size());
        assertEquals("incorrect path cost", 2.0, paths.iterator().next().cost(), 0.1);
    }

    @Test
    public void multiPath2() {
        g = new AdjacencyListsGraph();
        assertTrue("failed vertex add", g.add(A));
        assertTrue("failed vertex add", g.add(B));
        assertTrue("failed vertex add", g.add(C));
        assertTrue("failed vertex add", g.add(D));
        assertTrue("failed vertex add", g.add(E));
        assertTrue("failed vertex add", g.add(F));
        assertTrue("failed vertex add", g.add(G));
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(A, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, E, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, F, 1)));
        assertTrue("failed edge add", g.add(new Edg(E, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(A, G, 4)));

        GraphPathSearch gs = graphSearch();
        Result gsr = gs.search(g, A, G, weight);
        Set<Path> paths = gsr.paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 5, paths.size());
        assertEquals("incorrect path cost", 4.0, paths.iterator().next().cost(), 0.1);
    }

    @Test
    public void multiPath3() {
        g = new AdjacencyListsGraph();
        addVertices();
        addEdges();

        assertTrue("failed edge add", g.add(new Edg(A, E, 3)));
        assertTrue("failed edge add", g.add(new Edg(B, D, 1)));

        GraphPathSearch gs = graphSearch();
        Set<Path> paths = gs.search(g, A, E, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", 3, paths.size());
        assertEquals("incorrect path cost", 3.0, paths.iterator().next().cost(), 0.1);
    }

}