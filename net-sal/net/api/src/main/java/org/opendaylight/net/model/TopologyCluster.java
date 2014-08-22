/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Topology cluster represents a strongly connected component of network
 * infrastructure devices in a network. This means that path comprising solely
 * of direct or tunnel links exists between any two devices in the cluster.
 *
 * @author Thomas Vachuska
 * @author Uyen Chau
 */
public interface TopologyCluster {

    /**
     * Returns cluster identifier.
     *
     * @return cluster id
     */
    long id();

    /**
     * Returns the id of the root device in the cluster.
     *
     * @return device id of the root device
     */
    DeviceId root();

    /**
     * Returns the number of devices in the current topology.
     *
     * @return number of devices
     */
    int deviceCount();

    /**
     * Returns the number of links in the current topology.
     *
     * @return number of links
     */
    int linkCount();

}
