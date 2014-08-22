/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.driver.DeviceInfo;
import org.opendaylight.of.lib.dt.DataPathId;

import java.net.URI;
import java.util.Set;

/**
 * Device is a network infrastructure entity, e.g. Switch, Router, Access
 * Point, Firewall. It is capable of processing and forwarding network
 * packets. It has a set of {@link Interface Interfaces}.
 * 
 * @author Thomas Vachuska
 * @author Uyen Chau
 * @author Simon Hunt
 */
public interface Device extends NetworkElement {
    
    /**
     * Returns the device ID for this device.
     * 
     * @return device ID
     */
    @Override
    DeviceId id();

    /**
     * Set of information accrued about the device through the use of device
     * drivers.
     * @return DeviceInfo containing information about the device
     */
    DeviceInfo info();

    /**
     * Indicates whether the device is presently online.
     *
     * @return true if online
     */
    boolean isOnline();

    /**
     * Set of URIs that can be used to communicate with the device via
     * device drivers.
     * <p>
     * snmp://12312313/
     * of://dpid/
     *
     * @return set of URIs
     */
    Set<URI> managementURIs();

    /**
     * Returns the data path id for this device or null if the device is not an
     * OpenFlow datapath.
     *
     * @return device dpid
     */
    DataPathId dpid();

    /**
     * Optional reference to a device that is used to realize this one.
     *
     * @return underlying device descriptor; null if none
     */
    Device realizedBy();

}
