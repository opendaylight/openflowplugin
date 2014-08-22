/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple concrete implementation of a directed graph path.
 *
 * @author Thomas Vachuska
 */
public class DefaultPath implements MutablePath {
    
    protected final Vertex src;
    protected final Vertex dst;
    protected final List<Edge> edges = new ArrayList<>();
    protected double cost = 0.0;

    /**
     * Creates a new path from source to destination.
     * 
     * @param src source vertex
     * @param dst destination/target vertex
     */
    public DefaultPath(Vertex src, Vertex dst) {
        this.src = src;
        this.dst = dst;
    }


    /**
     * Creates a new path as a copy of another path.
     *
     * @param path path to be copied
     */
    public DefaultPath(Path path) {
        this.src = path.src();
        this.dst = path.dst();
        this.cost = path.cost();
        edges.addAll(path.edges());
    }
    
    @Override
    public Vertex src() {
        return src;
    }

    @Override
    public Vertex dst() {
        return dst;
    }

    @Override
    public double cost() {
        return cost;
    }

    @Override
    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public void appendEdge(Edge edge) {
        edges.add(edge);
    }

    @Override
    public void insertEdge(Edge edge) {
        edges.add(0, edge);
    }
    
    @Override
    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{src=").append(src)
                .append(";dst=").append(dst).append(";cost=").append(cost)
                .append(";edges=[");
        for (Edge e : edges)
            sb.append(e);
       sb.append("]}");
       return sb.toString();
    }
    
}
