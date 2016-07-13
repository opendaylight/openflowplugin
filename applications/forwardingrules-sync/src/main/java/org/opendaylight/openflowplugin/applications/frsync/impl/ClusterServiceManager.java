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
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.openflowplugin.applications.frsync.util.ReconciliationRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link ClusterServiceContext}.
 */
public class ClusterServiceManager {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterServiceManager.class);
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final ConcurrentHashMap<NodeId, ClusterServiceContext> contexts;
    private final ConcurrentHashMap<NodeId, ClusterSingletonServiceRegistration> registrations;
    private final ReconciliationRegistry reconciliationRegistry;

    public ClusterServiceManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                 final ReconciliationRegistry reconciliationRegistry) {
        this.clusterSingletonService = clusterSingletonService;
        this.reconciliationRegistry = reconciliationRegistry;
        this.contexts = new ConcurrentHashMap();
        this.registrations = new ConcurrentHashMap();
    }

    public void onDeviceConnected(final NodeId nodeId) {
        final ClusterServiceContext context = new ClusterServiceContext(nodeId, reconciliationRegistry);
        contexts.put(nodeId, context);
        final ClusterSingletonServiceRegistration registration = clusterSingletonService.registerClusterSingletonService(context);
        registrations.put(nodeId, registration);
        LOG.debug("FRS service registered for: {}", nodeId.getValue());
    }


    public void onDeviceDisconnected(final NodeId nodeId) {
        // TODO consider if necessary {@link ClusterServiceContext#closeServiceInstance()} should unregister
        reconciliationRegistry.unregisterIfRegistered(nodeId);
        contexts.remove(nodeId);
        final ClusterSingletonServiceRegistration registration = registrations.remove(nodeId);
        if (registration != null) {
            try {
                registration.close();
            } catch (Exception e) {
                LOG.error("FRS cluster service close fail: {}", nodeId.getValue());
            }
        }
        LOG.debug("FRS service unregistered for: {}", nodeId.getValue());
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        if (contexts.get(nodeId) == null) {
            return false;
        } else {
            return contexts.get(nodeId).isDeviceMastered();
        }
    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, ClusterServiceContext> getContexts() {
        return contexts;
    }
}
