/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.base.Verify;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
    private final ConcurrentMap<DeviceContext, RpcContext> contexts = new ConcurrentHashMap<>();
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
        final OfpRole ofpRole = deviceContext.getDeviceState().getRole();

        LOG.debug("Node:{}, deviceContext.getDeviceState().getRole():{}", nodeId, ofpRole);
        final RpcContext rpcContext = new RpcContextImpl(deviceContext.getMessageSpy(), rpcProviderRegistry,
                deviceContext, maxRequestsQuota, isStatisticsRpcEnabled, notificationPublishService);

        Verify.verify(contexts.putIfAbsent(deviceContext, rpcContext) == null, "RpcCtx still not closed for node {}", nodeId);
        deviceContext.addDeviceContextClosedHandler(this);

        //FIXME : propagate isStatisticsRpcEnabled to DeviceContext

        if (OfpRole.BECOMEMASTER.equals(ofpRole)) {
            LOG.info("Registering Openflow RPCs for node:{}, role:{}", nodeId, ofpRole);
            MdSalRegistratorUtils.registerMasterServices(rpcContext, deviceContext, ofpRole);

        } else if(OfpRole.BECOMESLAVE.equals(ofpRole)) {
            // if slave, we need to de-register rpcs if any have been registered, in case of master to slave
            LOG.info("Unregistering RPC registration (if any) for slave role for node:{}", deviceContext.getDeviceState().getNodeId());
            MdSalRegistratorUtils.registerSlaveServices(rpcContext, deviceContext, ofpRole);
        } else {
            // if we don't know role, we need to unregister rpcs if any have been registered
            LOG.info("Unregistering RPC registration (if any) for slave role for node:{}", deviceContext.getDeviceState().getNodeId());
            MdSalRegistratorUtils.unregisterServices(rpcContext);
        }

        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {
        for(final RpcContext ctx : contexts.values()) {
            ctx.close();
        }
        contexts.clear();
    }


    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        final RpcContext removedContext = contexts.remove(deviceContext);
        if (removedContext != null) {
            LOG.info("Unregistering rpcs for device context closure");
            removedContext.close();
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
