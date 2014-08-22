/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import org.junit.Test;

/**
 * Test of the default path.
 *
 * @author Thomas Vachuska
 */
public class DefaultPathTest extends GraphTest {

    @Test
    public void test() {
        MutablePath p = new DefaultPath(A, D);
        assertEquals("incorrect path src", A, p.src());
        assertEquals("incorrect path src", D, p.dst());
        assertEquals("incorrect path cost", 0.0, p.cost(), 0.1);
        
        p.setCost(123.0);
        assertEquals("incorrect path cost", 123.0, p.cost(), 0.1);
        
        p.insertEdge(new Edg(B, C, 1));
        p.insertEdge(new Edg(A, B, 1));
        p.appendEdge(new Edg(C, D, 1));
        assertEquals("incorrect path length", 3, p.edges().size());

        Edge edge = new Edg(C, E, 1);
        p.appendEdge(edge);
        assertEquals("incorrect path length", 4, p.edges().size());
        p.removeEdge(edge);
        assertEquals("incorrect path length", 3, p.edges().size());
        
        Vertex s = p.src();
        Iterator<Edge> it = p.edges().iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            assertEquals("incorrect edge src", s, e.src());
            s = e.dst();
        }
        assertEquals("incorrect edge dst", p.dst(), s);
    }

}
