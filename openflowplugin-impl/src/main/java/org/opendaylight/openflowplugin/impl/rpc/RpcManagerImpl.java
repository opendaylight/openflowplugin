/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistratorUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcManagerImpl implements RpcManager {

    private static final Logger LOG = LoggerFactory.getLogger(RpcManagerImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private final int maxRequestsQuota;
    private final ConcurrentHashMap<DeviceContext, RpcContext> contexts = new ConcurrentHashMap<>();
    private boolean isStatisticsRpcEnabled;
    private NotificationPublishService notificationPublishService;

    public RpcManagerImpl(final RpcProviderRegistry rpcProviderRegistry,
                          final int quotaValue) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        maxRequestsQuota = quotaValue;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) throws Exception {
        final NodeId nodeId = deviceContext.getDeviceState().getNodeId();

        LOG.debug("Node:{}", nodeId);

        RpcContext rpcContext = contexts.get(deviceContext);
        if (rpcContext == null) {
            rpcContext = new RpcContextImpl(deviceContext.getMessageSpy(), rpcProviderRegistry, deviceContext, maxRequestsQuota);
            contexts.put(deviceContext, rpcContext);
        }

        deviceContext.addDeviceContextClosedHandler(this);

        LOG.info("Registering Openflow RPCs for node:{}", nodeId);
        MdSalRegistratorUtils.unregisterServices(rpcContext, deviceContext, OfpRole.BECOMESLAVE);

        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {

    }


    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        final RpcContext removedContext = contexts.remove(deviceContext);
        if (removedContext != null) {
            try {
                LOG.info("Un-registering rpcs for device context closure");
                removedContext.close();
            } catch (final Exception e) {
                LOG.error("Exception while un-registering rpcs onDeviceContextClosed handler for node:{}. But continuing.",
                        deviceContext.getDeviceState().getNodeId(), e);
            }
        }
    }
    @Override
    public void setStatisticsRpcEnabled(final boolean isStatisticsRpcEnabled) {
        this.isStatisticsRpcEnabled = isStatisticsRpcEnabled;
    }

    @Override
    public void setNotificationPublishService(final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
    }
}
