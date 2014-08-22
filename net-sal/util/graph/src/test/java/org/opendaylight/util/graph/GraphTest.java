/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.Set;

import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Base class for various graph-related tests.
 *
 * @author Thomas Vachuska
 */
public class GraphTest {
    
    static final int VERTEX_COUNT = 8; 
    static final int EDGE_COUNT = 12; 

    static final Vertex A = new Vtx("A");
    static final Vertex B = new Vtx("B");
    static final Vertex C = new Vtx("C");
    static final Vertex D = new Vtx("D");
    static final Vertex E = new Vtx("E");
    static final Vertex F = new Vtx("F");
    static final Vertex G = new Vtx("G");
    static final Vertex H = new Vtx("H");
    
    static class Vtx implements Vertex {
        private String name;

        Vtx(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
        
        @Override
        public int hashCode() {
            return 31 + name.hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vtx v = (Vtx) o;
            return name.equals(v.name);
        }
        
    }
    
    static class Edg implements Edge {
        private Vertex src, dst;
        private double weight;

        Edg(Vertex src, Vertex dst, double weight) {
            this.src = src;
            this.dst = dst;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "(" + src + ", " + dst + ")";
        }

        @Override
        public Vertex src() {
            return src;
        }

        @Override
        public Vertex dst() {
            return dst;
        }
        
        public double weight() {
            return weight;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((src == null) ? 0 : src.hashCode());
            result = prime * result + ((dst == null) ? 0 : dst.hashCode());
            result = prime * result + 
                    Long.valueOf(Double.doubleToLongBits(weight)).hashCode();
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edg e = (Edg) o;
            return src.equals(e.src) && dst.equals(e.dst) && weight == e.weight;
        }
        
    }
    
    protected MutableGraph g;
    
    protected EdgeWeight weight = new EdgeWeight() {
        @Override
        public double weight(Edge edge) {
            return Edg.class.isInstance(edge) ? ((Edg) edge).weight() : 1.0;
        }
    };

    protected void printPaths(Set<Path> paths) {
        for (Path p : paths)
            print(p);
    }

    protected void addVertices() {
        assertTrue("failed vertex add", g.add(A));
        assertTrue("failed vertex add", g.add(B));
        assertTrue("failed vertex add", g.add(C));
        assertTrue("failed vertex add", g.add(D));
        assertTrue("failed vertex add", g.add(E));
        assertTrue("failed vertex add", g.add(F));
        assertTrue("failed vertex add", g.add(G));
        assertTrue("failed vertex add", g.add(H));
        assertEquals("incorrect vertex count", VERTEX_COUNT, g.getVertices().size());
    }

    protected void addEdges() {
        assertTrue("failed edge add", g.add(new Edg(A, B, 1)));
        assertTrue("failed edge add", g.add(new Edg(A, C, 3)));
        assertTrue("failed edge add", g.add(new Edg(B, D, 2)));
        assertTrue("failed edge add", g.add(new Edg(B, C, 1)));
        assertTrue("failed edge add", g.add(new Edg(B, E, 4)));
        assertTrue("failed edge add", g.add(new Edg(C, E, 1)));
        assertTrue("failed edge add", g.add(new Edg(D, H, 5)));
        assertTrue("failed edge add", g.add(new Edg(D, E, 1)));
        assertTrue("failed edge add", g.add(new Edg(E, F, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, D, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, G, 1)));
        assertTrue("failed edge add", g.add(new Edg(F, H, 1)));
        assertEquals("incorrect edge count", EDGE_COUNT, g.getEdges().size());
        assertEquals("incorrect outbound edge count", 3, g.getEdgesFrom(B).size());
        assertEquals("incorrect inbound edge count", 1, g.getEdgesTo(B).size());
        assertEquals("incorrect inbound edge count", 2, g.getEdgesTo(C).size());
    }
    
    
    /**
     * Creates a spine graph.
     * 
     * @param spineCount number of spine vertices
     * @param extCount number of external vertices
     * @param spineCost cost of spine mesh edges
     * @param connectCost cost of external vertex connect edges
     * @param connectCount number of connections each external vertex has to
     *        the spine mesh
     * @return mutable graph
     */
    protected MutableGraph createSpineGraph(int spineCount, int extCount,
                                            double spineCost, 
                                            double connectCost,
                                            int connectCount) {
        MutableGraph g = new AdjacencyListsGraph();
        
        Vertex spines[] = new Vertex[spineCount];
        
        // Create all spine vertices...
        for (int i = 0; i < spineCount; i++) {
            spines[i] = new Vtx("spine-" + i);
            g.add(spines[i]);
        }

        // Create a fully bidirectional spine mesh...
        for (int i = 0; i < spineCount; i++) {
            for (int j = i + 1; j < spineCount; j++) {
                g.add(new Edg(spines[i], spines[j], spineCost));
                g.add(new Edg(spines[j], spines[i], spineCost));
            }
        }
        
        // Create all external vertices and make sure each has the proper amount
        // of connections to the spine mesh
        int spine = 0;
        for (int i = 0; i < extCount; i++) {
            Vertex v = new Vtx("ext-" + i);
            g.add(v);
            for (int j = 0; j < connectCount; j++) {
                int si = (spine + j) % spineCount; 
                g.add(new Edg(v, spines[si], connectCost));
                g.add(new Edg(spines[si], v, connectCost));
                spine++;
            }
            spine--;  // next external vertex should use the last used spine
        }
        
        return g;
    }
    
}
