/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

/**
 * Provisional representation of a network topology.
 * To be revised!
 *
 * @author Thomas Vachuska
 */
public interface Topology extends Model {

    /**
     * Returns the timestamp of when the topology became active.
     *
     * @return millis since start of epoch
     */
    long activeAt();

    /**
     * Returns the number of clusters in the current topology.
     *
     * @return number of clusters
     */
    int clusterCount();

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
