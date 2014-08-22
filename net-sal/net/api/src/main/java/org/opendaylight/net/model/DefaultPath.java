/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.supplier.SupplierId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of a network path.
 *
 * @author Thomas Vachuska
 */
public class DefaultPath extends DefaultLink implements Path {

    private final List<Link> links;

    /**
     * Create a path from the specified list of links.
     *
     * @param supplierId supplier id
     * @param links list of links
     */
    public DefaultPath(SupplierId supplierId, List<Link> links) {
        super(supplierId, links.get(0).src(), links.get(links.size() - 1).dst(),
              Type.MULTIHOP_LINK);
        this.links = Collections.unmodifiableList(new ArrayList<>(links));
    }

    /**
     * Create a path from the specified list of links.
     *
     * @param links list of links
     */
    public DefaultPath(List<Link> links) {
        this(null, links);
    }

    @Override
    public List<Link> links() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultPath that = (DefaultPath) o;
        return !(links != null ? !links.equals(that.links) : that.links != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + links.hashCode();
        return result;
    }
}
