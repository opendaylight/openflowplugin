/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Default implementation of {@link org.opendaylight.net.model.ConnectionPoint}
 * @author Marjorie Krueger
 */
public class DefaultConnectionPoint implements ConnectionPoint {
    private final ElementId elementId;
    private final InterfaceId interfaceId;

    public DefaultConnectionPoint(ElementId elementId, InterfaceId interfaceId) {
        this.elementId = elementId;
        this.interfaceId = interfaceId;
    }

    @Override
    public ElementId elementId() {
        return this.elementId;
    }

    @Override
    public InterfaceId interfaceId() {
        return this.interfaceId;
    }

    @Override
    public String toString() {
        return "DefaultConnectionPoint{" +
                "elementId=" + elementId +
                ", interfaceId=" + interfaceId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultConnectionPoint)) return false;

        DefaultConnectionPoint that = (DefaultConnectionPoint) o;

        if (!elementId.equals(that.elementId)) return false;
        if (!interfaceId.equals(that.interfaceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = elementId.hashCode();
        result = 31 * result + interfaceId.hashCode();
        return result;
    }
}
