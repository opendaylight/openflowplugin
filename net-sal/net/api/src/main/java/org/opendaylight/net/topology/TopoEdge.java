/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.util.graph.Edge;
import org.opendaylight.util.graph.Vertex;
import org.opendaylight.net.model.Link;

/**
 * Topology edge representing an infrastructure link.
 *
 * @author Thomas Vachuska
 */
public class TopoEdge implements Edge {

    private static final String E_INCONGRUENT_LINK =
            "Specified link is not congruent with src/dst";

    private final TopoVertex src;
    private final TopoVertex dst;
    private final Link link;

    /**
     * Creates an edge representing the given link.
     *
     * @param src source vertex
     * @param dst destination vertex
     * @param link backing infrastructure link
     */
    public TopoEdge(TopoVertex src, TopoVertex dst, Link link) {
        // Make sure given vertex src/dst are congruent with the given link.
        if (!src.deviceId().equals(link.src().elementId()) ||
                !dst.deviceId().equals(link.dst().elementId()))
            throw new IllegalArgumentException(E_INCONGRUENT_LINK);
        this.src = src;
        this.dst = dst;
        this.link = link;
    }

    /**
     * Returns the backing link.
     *
     * @return infrastructure link
     */
    public Link link() {
        return link;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopoEdge topoEdge = (TopoEdge) o;
        return src.equals(topoEdge.src) && dst.equals(topoEdge.dst) &&
                link.equals(topoEdge.link);
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dst.hashCode();
        result = 31 * result + link.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TopoEdge{link=" + link + '}';
    }
}
