/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Representation of a host location as a time-stamped connection point where a
 * host attaches to the nearest infrastructure device.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface HostLocation extends ConnectionPoint {
    
    /**
     * Returns the unique identifier of the connection point device.
     *
     * @return infrastructure device
     */
    @Override
    DeviceId elementId();

    /**
     * Returns the timestamp representing the time at which the node was seen
     * at this connection point.
     *
     * @return the timestamp (epoch ms)
     */
    long ts();

}
