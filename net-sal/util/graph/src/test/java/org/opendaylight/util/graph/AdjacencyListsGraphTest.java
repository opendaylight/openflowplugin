/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the adjacency lists graph implementation.
 *
 * @author Thomas Vachuska
 */
public class AdjacencyListsGraphTest extends GraphTest {
    
    @Before
    public void setUp() {
        g = new AdjacencyListsGraph();
    }

    @Test
    public void basics() {
        super.addVertices();
        super.addEdges();
    }

    @Test
    public void remove() {
        basics();
        int id = g.getEdgesTo(B).size();
        int od = g.getEdgesFrom(B).size();
        g.remove(B);
        assertEquals("incorrect vertex count", VERTEX_COUNT - 1, g.getVertices().size());
        assertEquals("incorrect edge count", EDGE_COUNT - id - od, g.getEdges().size());
        
        Edge e = g.getEdgesFrom(E).iterator().next();
        g.remove(e);
        assertEquals("incorrect vertex count", VERTEX_COUNT - 1, g.getVertices().size());
        assertEquals("incorrect edge count", EDGE_COUNT - id - od - 1, g.getEdges().size());

        assertEquals("incorrect outbound edge count", 0, g.getEdgesFrom(E).size());
        assertEquals("incorrect inbound edge count", 3, g.getEdgesTo(E).size());
    }
    
    @Test
    public void crookedEdge() {
        assertTrue("failed edge add", g.add(new Edg(A, new Vtx("Z"), 1)));
        assertTrue("failed edge add", g.add(new Edg(new Vtx("Y"), B, 1)));
        
        Edge e = new Edg(F, E, 1);
        assertTrue("failed edge add", g.add(e));
        assertFalse("should fail edge add", g.add(e));

        assertTrue("failed edge remove", g.remove(e));
        assertFalse("should fail edge remove", g.remove(e));
    }
    
    @Test(expected=NullPointerException.class)
    public void nullEdge() {
        g.add((Edge) null);
    }

    @Test(expected=NullPointerException.class)
    public void nullVertexEdge() {
        g.add(new Edg(null, null, 1));
    }

}
