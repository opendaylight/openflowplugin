/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl.clustering;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
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
    private final ReconciliationRegistry reconciliationRegistry;

    public DeviceMastershipManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                   final ReconciliationRegistry reconciliationRegistry) {
        this.clusterSingletonService = clusterSingletonService;
        this.reconciliationRegistry = reconciliationRegistry;
    }

    public void onDeviceConnected(final NodeId nodeId) {
        LOG.debug("FRS service registered for: {}", nodeId.getValue());
        final DeviceMastership mastership = new DeviceMastership(nodeId, reconciliationRegistry, clusterSingletonService);
        deviceMasterships.put(nodeId, mastership);
    }

    public void onDeviceDisconnected(final NodeId nodeId) {
        final DeviceMastership mastership = deviceMasterships.remove(nodeId);
        if (mastership != null) {
            mastership.close();
        }
        LOG.debug("FRS service unregistered for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, DeviceMastership> getDeviceMasterships() {
        return deviceMasterships;
    }
}
