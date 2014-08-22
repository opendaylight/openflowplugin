/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import org.junit.Test;

import static org.opendaylight.util.graph.TarjanGraphSearch.ConnectivityClusterResult;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test of the Tarjan graph search for connectivity clusters.
 *
 * @author Thomas Vachuska
 */
public class TarjanGraphSearchTest extends GraphTest {

    private void validate(ConnectivityClusterResult gsr, int cc) {
        print("Cluster count: " + gsr.clusterVertices().size());
        print("Clusters: " + gsr.clusterVertices());
        assertEquals("incorrect cluster count", cc, gsr.clusterCount());
    }

    private void validate(ConnectivityClusterResult gsr, int i, int vc, int ec) {
        assertEquals("incorrect cluster count", vc, gsr.clusterVertices().get(i).size());
        assertEquals("incorrect edge count", ec, gsr.clusterEdges().get(i).size());
    }

    @Test
    public void basic() {
        g = new AdjacencyListsGraph();
        addVertices();
        addEdges();

        TarjanGraphSearch gs = new TarjanGraphSearch();
        ConnectivityClusterResult gsr = gs.search(g ,null);
        validate(gsr, 6);
    }

    @Test
    public void one() {
        g = new AdjacencyListsGraph();
        addVertices();
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, E, 1)));
        assertTrue("failed edge add", g.add(new Edg(E, F, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(G, H, 1)));
        assertTrue("failed edge add", g.add(new Edg(H, A, 1)));

        TarjanGraphSearch gs = new TarjanGraphSearch();
        ConnectivityClusterResult gsr = gs.search(g, null);
        validate(gsr, 1);
        validate(gsr, 0, 8, 8);
    }

    @Test
    public void twoIsolated() {
        g = new AdjacencyListsGraph();
        addVertices();
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, A, 1)));
        assertTrue("failed edge add", g.add(new Edg(E, F, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(G, H, 1)));
        assertTrue("failed edge add", g.add(new Edg(H, E, 1)));

        TarjanGraphSearch gs = new TarjanGraphSearch();
        ConnectivityClusterResult gsr = gs.search(g, null);
        validate(gsr, 2);
        validate(gsr, 0, 4, 4);
        validate(gsr, 1, 4, 4);
    }

    @Test
    public void twoConnectedWeakly() {
        g = new AdjacencyListsGraph();
        addVertices();
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, A, 1)));
        assertTrue("failed edge add", g.add(new Edg(E, F, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(G, H, 1)));
        assertTrue("failed edge add", g.add(new Edg(H, E, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, E, 1)));

        TarjanGraphSearch gs = new TarjanGraphSearch();
        ConnectivityClusterResult gsr = gs.search(g, null);
        validate(gsr, 2);
        validate(gsr, 0, 4, 4);
        validate(gsr, 1, 4, 4);
    }

    @Test
    public void twoConnectedWithIgnoredEdges() {
        g = new AdjacencyListsGraph();
        addVertices();
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(C, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, A, 1)));
        assertTrue("failed edge add", g.add(new Edg(E, F, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(G, H, 1)));
        assertTrue("failed edge add", g.add(new Edg(H, E, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, E, -1)));
        assertTrue("failed edge add", g.add(new Edg(E, B, -1)));

        TarjanGraphSearch gs = new TarjanGraphSearch();
        ConnectivityClusterResult gsr = gs.search(g, weight);
        validate(gsr, 2);
        validate(gsr, 0, 4, 4);
        validate(gsr, 1, 4, 4);
    }

    @Test
    public void spine() {
        g = createSpineGraph(4, 8, 1.0, 2.0, 2);
        int vc = g.getVertices().size();
        int ec = g.getEdges().size();
        print("There are " + vc + " vertices and " + ec + " edges.");

        TarjanGraphSearch gs = new TarjanGraphSearch();
        ConnectivityClusterResult gsr = gs.search(g, null);
        validate(gsr, 1);
    }


}
