/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.net.model.Link.Type;

/**
 * Default implementation of {@link org.opendaylight.net.model.LinkInfo}.
 *
 * @author Marjorie Krueger
 */
public class DefaultLinkInfo implements LinkInfo {
    private Link.Type type;

    /**
     * Creates a new link info descriptor
     *
     * @param type link type
     */
    public DefaultLinkInfo(Type type) {
        this.type = type;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String toString() {
        return "DefaultLinkInfo{, type=" + type + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultLinkInfo)) return false;
        DefaultLinkInfo that = (DefaultLinkInfo) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
}
