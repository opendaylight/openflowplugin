/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.supplier.SupplierId;

import static org.opendaylight.net.model.Link.Type.MULTIHOP_LINK;
import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Provides a default implementation of {@link Link}.
 *
 * @author Marjorie Krueger
 */
public class DefaultLink extends AbstractModel implements Link {

    private final ConnectionPoint src;
    private final ConnectionPoint dst;
    private Type type;

    /**
     * Creates a new link between the specified src &amp; dest.
     *
     * @param supplierId supplier id
     * @param src        source connection point
     * @param dst        dest connection point
     * @param type       link type
     */
    public DefaultLink(SupplierId supplierId, ConnectionPoint src, ConnectionPoint dst,
                       Type type) {
        super(supplierId);
        notNull(src, dst, type);
        this.src = src;
        this.dst = dst;
        this.type = type;
    }

    /**
     * Creates a new link between the specified src &amp; dest.
     *
     * @param src  source connection point
     * @param dst  dest connection point
     * @param type link type
     */
    public DefaultLink(ConnectionPoint src, ConnectionPoint dst, Type type) {
        this(null, src, dst, type);
    }

    /**
     * Creates a new link between the specified src &amp; dest.
     *
     * @param src  source connection point
     * @param dst  dest connection point
     */
    public DefaultLink(ConnectionPoint src, ConnectionPoint dst) {
        this(null, src, dst, MULTIHOP_LINK);
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public ConnectionPoint src() {
        return src;
    }

    @Override
    public ConnectionPoint dst() {
        return dst;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultLink)) return false;

        DefaultLink that = (DefaultLink) o;

        if (!dst.equals(that.dst)) return false;
        return src.equals(that.src);
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dst.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DefaultLink{src=" + src + ", dst=" + dst + ", type=" + type + '}';
    }
}
