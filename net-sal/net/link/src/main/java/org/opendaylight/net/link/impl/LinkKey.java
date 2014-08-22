/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link.impl;

import org.opendaylight.net.model.ConnectionPoint;

/**
 * Auxiliary key for LinkManager based on connection points
 *
 * @author Marjorie Krueger
 */
class LinkKey {
    final ConnectionPoint src;
    final ConnectionPoint dst;

    public LinkKey(ConnectionPoint src, ConnectionPoint dst) {
        this.src = src;
        this.dst = dst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkKey linkKey = (LinkKey) o;
        if (!dst.equals(linkKey.dst)) return false;
        return src.equals(linkKey.src);

    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dst.hashCode();
        return result;
    }
}
