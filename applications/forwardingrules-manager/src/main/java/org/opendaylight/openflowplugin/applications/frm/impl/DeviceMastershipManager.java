/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap();

    public DeviceMastershipManager(final ClusterSingletonServiceProvider clusterSingletonService) {
        this.clusterSingletonService = clusterSingletonService;
    }

    public void onDeviceConnected(final NodeId nodeId) {
        LOG.debug("FRM service registered for: {}", nodeId.getValue());
        final DeviceMastership mastership = new DeviceMastership(nodeId, clusterSingletonService);
        deviceMasterships.put(nodeId, mastership);
    }

    public void onDeviceDisconnected(final NodeId nodeId) {
        final DeviceMastership mastership = deviceMasterships.remove(nodeId);
        if (mastership != null) {
            mastership.close();
        }
        LOG.debug("FRM service unregistered for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, DeviceMastership> getDeviceMasterships() {
        return deviceMasterships;
    }
}
