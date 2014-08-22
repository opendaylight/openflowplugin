/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology;

import org.opendaylight.util.graph.Vertex;
import org.opendaylight.net.model.DeviceId;

/**
 * Topology vertex representing a device.
 *
 * @author Thomas Vachuska
 */
public class TopoVertex implements Vertex {

    private final DeviceId deviceId;

    /**
     * Creates a new topology vertex repesenting the specified device.
     *
     * @param deviceId device id
     */
    public TopoVertex(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the unique id of the device which this vertex represents.
     *
     * @return vertex device id
     */
    public DeviceId deviceId() {
        return deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopoVertex that = (TopoVertex) o;
        return deviceId.equals(that.deviceId);
    }

    @Override
    public int hashCode() {
        return deviceId.hashCode();
    }

    @Override
    public String toString() {
        return "TopoVertex{deviceId=" + deviceId + '}';
    }

}
