/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.topology.compute.impl;

import org.opendaylight.net.model.DeviceId;
import org.opendaylight.net.model.TopologyCluster;

/**
 * Default implementation of a topology cluster descriptor.
 *
 * @author Thomas Vachuska
 */
class DefaultTopologyCluster implements TopologyCluster {

    private final long id;
    private final int deviceCount;
    private final int linkCount;
    private DeviceId root;

    /**
     * Creates default topology cluster descriptor.
     *
     * @param id          cluster id
     * @param deviceCount number of devices in the cluster
     * @param linkCount   number of links in the cluster
     */
    public DefaultTopologyCluster(long id, int deviceCount, int linkCount) {
        this.id = id;
        this.deviceCount = deviceCount;
        this.linkCount = linkCount;
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public DeviceId root() {
        return root;
    }

    @Override
    public int deviceCount() {
        return deviceCount;
    }

    @Override
    public int linkCount() {
        return linkCount;
    }

    @Override
    public String toString() {
        return "DefaultTopologyCluster{id=" + id + ", root=" + root +
                ", deviceCount=" + deviceCount + ", linkCount=" + linkCount + '}';
    }

    /**
     * Sets the device id of the root vertex of the cluster.
     *
     * @param root new root device id
     */
    void setRoot(DeviceId root) {
        this.root = root;
    }

}
