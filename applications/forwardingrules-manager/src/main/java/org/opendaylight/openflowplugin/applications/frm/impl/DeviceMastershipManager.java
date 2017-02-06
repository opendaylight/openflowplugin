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

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for clustering service registrations of {@link DeviceMastership}.
 */
public class DeviceMastershipManager implements OpendaylightInventoryListener, AutoCloseable{
    private static final Logger LOG = LoggerFactory.getLogger(DeviceMastershipManager.class);
    private final ClusterSingletonServiceProvider clusterSingletonService;
    private final ListenerRegistration<?> notifListenerRegistration;
    private final ConcurrentHashMap<NodeId, DeviceMastership> deviceMasterships = new ConcurrentHashMap();

    public DeviceMastershipManager(final ClusterSingletonServiceProvider clusterSingletonService,
                                   final NotificationProviderService notificationService) {
        this.clusterSingletonService = clusterSingletonService;
        this.notifListenerRegistration = notificationService.registerNotificationListener(this);
    }

    public void onDeviceConnected(final NodeId nodeId) {
        //No-op
    }

    public void onDeviceDisconnected(final NodeId nodeId) {
        //No-op
    }

    public boolean isDeviceMastered(final NodeId nodeId) {
        return deviceMasterships.get(nodeId) != null && deviceMasterships.get(nodeId).isDeviceMastered();
    }

    @VisibleForTesting
    ConcurrentHashMap<NodeId, DeviceMastership> getDeviceMasterships() {
        return deviceMasterships;
    }

    @Override
    public void onNodeUpdated(NodeUpdated notification) {
        LOG.debug("NodeUpdate notification received : {}", notification);
        DeviceMastership membership = deviceMasterships.computeIfAbsent(notification.getId(), device ->
                new DeviceMastership(notification.getId(), clusterSingletonService));
        membership.registerClusterSingletonService();
    }

    @Override
    public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
        //Not published by plugin
    }

    @Override
    public void onNodeRemoved(NodeRemoved notification) {
        LOG.debug("NodeRemoved notification received : {}", notification);
        NodeId nodeId = notification.getNodeRef().getValue().firstKeyOf(Node.class).getId();
        final DeviceMastership mastership = deviceMasterships.remove(nodeId);
        if (mastership != null) {
            mastership.close();
            LOG.info("Unregistered FRM cluster singleton service for service id : {}", nodeId.getValue());
        }
    }

    @Override
    public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
        //Not published by plugin
    }

    @Override
    public void close() throws Exception {
        if (notifListenerRegistration != null) {
            notifListenerRegistration.close();
        }
    }
}
