/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ClusteringServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(ClusteringServiceManager.class);
    // TODO provider
    private final ConcurrentHashMap<NodeId, ClusterRegistration> clusterRegistrations = new ConcurrentHashMap();
    private final ReconciliationRegistry reconciliationRegistry;

    public ClusteringServiceManager(final ReconciliationRegistry reconciliationRegistry) {
        // TODO set provider
        this.reconciliationRegistry = reconciliationRegistry;
    }

    public void onDeviceConnected(final NodeId nodeId) {
        final ClusterRegistration clusterRegistration = new ClusterRegistration(nodeId, reconciliationRegistry);
        clusterRegistrations.put(nodeId, clusterRegistration);
        // TODO provider.register(clusterRegistration);
        LOG.debug("FRS service registered for: {}", nodeId.getValue());
    }

    public void onDeviceDisconnected(final NodeId nodeId) {
        reconciliationRegistry.unregisterIfRegistered(nodeId);
        ClusterRegistration context = clusterRegistrations.remove(nodeId);
//        context.close();
        LOG.debug("FRS service unregister for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return clusterRegistrations.get(nodeId) != null && clusterRegistrations.get(nodeId).isDeviceMastered();
    }

    @VisibleForTesting
    ConcurrentMap<NodeId, ClusterRegistration> getClusterRegistrations() {
        return clusterRegistrations;
    }
}
