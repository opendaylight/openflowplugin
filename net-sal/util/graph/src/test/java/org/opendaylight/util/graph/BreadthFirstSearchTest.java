/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import org.opendaylight.util.graph.GraphPathSearch.Result;
import org.junit.Test;

import java.util.Set;

import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;

/**
 * Test of the BFS algorithm.
 *
 * @author Thomas Vachuska
 */
public class BreadthFirstSearchTest extends AbstractGraphSearchTest {

    @Override
    protected GraphPathSearch graphSearch() {
        return new BreadthFirstSearch();
    }
    
    @Test
    public void basics() {
        runBasics(3, 8.0, 7);
    }
    
    @Test
    public void defaultWeight() {
        weight = null;
        runBasics(3, 3.0, 7);
    }

    @Test
    public void spineGraph() {
        g = createSpineGraph(15, 1100, 1.0, 2.0, 3);
        int vc = g.getVertices().size();
        int ec = g.getEdges().size();
        print("There are " + vc + " vertices and " + ec + " edges.");
        
        GraphPathSearch dsp = graphSearch();
        Result gsr = dsp.search(g, new Vtx("ext-1"), new Vtx("ext-64"), weight);
        
        printPaths(gsr.paths());
        
        double start = System.currentTimeMillis();
        gsr = dsp.search(g, new Vtx("ext-1"), null, weight);
        double end = System.currentTimeMillis();
        print("Computed " + gsr.paths().size() +
                      " shortest paths in " + (end - start) + " ms");
    }

    protected void runBasics(int expectedLength, double expectedCost, int expectedPaths) {
        g = new AdjacencyListsGraph();
        addVertices();
        addEdges();
        GraphPathSearch bfs = graphSearch();
        Set<Path> paths = bfs.search(g, A, H, weight).paths();
        assertEquals("incorrect paths count", 1, paths.size());
        
        Path p = paths.iterator().next();
        assertEquals("incorrect src", A, p.src());
        assertEquals("incorrect dst", H, p.dst());
        assertEquals("incorrect path length", expectedLength, p.edges().size());
        assertEquals("incorrect path cost", expectedCost, p.cost(), 0.1);
        
        paths = bfs.search(g, A, null, weight).paths();
        printPaths(paths);
        assertEquals("incorrect paths count", expectedPaths, paths.size());
    }

}
