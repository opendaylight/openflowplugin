/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import org.opendaylight.util.graph.DepthFirstSearch.EdgeType;
import org.opendaylight.util.graph.DepthFirstSearch.SpanningTreeResult;

/**
 * Test of the DFS algorithm.
 *
 * @author Thomas Vachuska
 */
public class DepthFirstSearchTest extends AbstractGraphSearchTest {
    
    @Override
    protected GraphPathSearch graphSearch() {
        return new DepthFirstSearch();
    }
    
    @Test
    public void basics() {
        runBasics(3, 6, 5.0, 12.0);
    }

    @Test
    public void defaultWeight() {
        weight = null;
        runBasics(3, 6, 3.0, 6.0);
    }

    protected void runBasics(int minLength, int maxLength, 
                             double minCost, double maxCost) {
        g = new AdjacencyListsGraph();
        addVertices();
        addEdges();
        GraphPathSearch dfs = graphSearch();
        SpanningTreeResult gsr = (SpanningTreeResult) dfs.search(g, A, H, weight);
        Set<Path> paths = gsr.paths();
        assertEquals("incorrect paths count", 1, paths.size());
        
        Path p = paths.iterator().next();
        print(p);
        assertEquals("incorrect src", A, p.src());
        assertEquals("incorrect dst", H, p.dst());
        
        int l = p.edges().size();
        assertTrue("incorrect path length " + l, 
                   minLength <= l && l <= maxLength);
        assertTrue("incorrect path cost " + p.cost(), 
                   minCost <= p.cost() && p.cost() <= maxCost);
        
        gsr = (SpanningTreeResult) dfs.search(g, A, null, weight);
        assertEquals("incorrect paths count", 7, gsr.paths().size());

        int[] types = new int[] { 0, 0, 0, 0 };
        for (EdgeType t : gsr.edges().values())
            types[t.ordinal()] += 1;
        assertEquals("incorrect tree-edge count", 7, 
                     types[EdgeType.TREE_EDGE.ordinal()]);
        assertEquals("incorrect cross-edge & forward-edge count", 4, 
                     types[EdgeType.FORWARD_EDGE.ordinal()] +
                     types[EdgeType.CROSS_EDGE.ordinal()]);
        assertEquals("incorrect back-edge count", 1, 
                     types[EdgeType.BACK_EDGE.ordinal()]);
        
        print(gsr.edges());
        printPaths(paths);
    }

}
