/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base implementation of a graph, using adjacency lists.
 * <p>
 * Not thread-safe, thus must be guarded externally against concurrent updates.
 *
 * @author Thomas Vachuska
 */
public class AdjacencyListsGraph implements MutableGraph {
    
    private static final String E_NULL = "Edge or vertex cannot be null";
    
    private final Map<Vertex, Set<Edge>> sources = new HashMap<>();
    private final Map<Vertex, Set<Edge>> targets = new HashMap<>();
    private final Set<Edge> edges = new HashSet<>();

    @Override
    public Set<Vertex> getVertices() {
        return Collections.unmodifiableSet(sources.keySet());
    }

    @Override
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    @Override
    public Set<Edge> getEdgesFrom(Vertex src) {
        return Collections.unmodifiableSet(sources.get(src));
    }

    @Override
    public Set<Edge> getEdgesTo(Vertex dst) {
        return Collections.unmodifiableSet(targets.get(dst));
    }

    private void notNull(Object o) {
        if (o == null)
            throw new NullPointerException(E_NULL);
    }
    
    @Override
    public boolean add(Vertex vertex) {
        notNull(vertex);
        if (sources.containsKey(vertex) || targets.containsKey(vertex))
            return false;
        sources.put(vertex, new HashSet<Edge>());
        targets.put(vertex, new HashSet<Edge>());
        return true;
    }


    @Override
    public boolean remove(Vertex vertex) {
        Set<Edge> out = sources.remove(vertex);
        if (out != null)
            edges.removeAll(out);
        Set<Edge> in = targets.remove(vertex);
        if (in != null)
            edges.removeAll(in);
        return out != null && in != null;
    }

    @Override
    public boolean add(Edge edge) {
        notNull(edge);
        if (!edges.add(edge))
            return false;
        
        // Attempt to add vertices just to be sure
        add(edge.src());
        add(edge.dst());
        
        return sources.get(edge.src()).add(edge) &&
                targets.get(edge.dst()).add(edge);
    }

    @Override
    public boolean remove(Edge edge) {
        return edges.remove(edge) &&
                sources.get(edge.src()).remove(edge) &&
                targets.get(edge.dst()).remove(edge);
    }

}
