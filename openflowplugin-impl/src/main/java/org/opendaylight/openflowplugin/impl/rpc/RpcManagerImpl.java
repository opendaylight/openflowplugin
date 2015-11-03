/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.util.MdSalRegistratorUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class RpcManagerImpl implements RpcManager {

    private static final Logger LOG = LoggerFactory.getLogger(RpcManagerImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private final int maxRequestsQuota;
    private final ConcurrentHashMap<DeviceContext, RpcContext> contexts = new ConcurrentHashMap<>();

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
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        NodeId nodeId = deviceContext.getDeviceState().getNodeId();
        OfpRole ofpRole = deviceContext.getDeviceState().getRole();

        LOG.debug("Node:{}, deviceContext.getDeviceState().getRole():{}", nodeId, ofpRole);

        RpcContext rpcContext = contexts.get(deviceContext);
        if (rpcContext == null) {
            rpcContext = new RpcContextImpl(deviceContext.getMessageSpy(), rpcProviderRegistry, deviceContext, maxRequestsQuota);
            contexts.put(deviceContext, rpcContext);
        }


        if (ofpRole == OfpRole.BECOMESLAVE) {
            // if slave, we need to de-register rpcs if any have been registered, in case of master to slave
            LOG.info("Unregistering RPC registration (if any) for slave role for node:{}", deviceContext.getDeviceState().getNodeId());
            try {
                MdSalRegistratorUtils.unregisterServices(rpcContext);
            } catch (Exception e) {
                LOG.error("Exception while unregistering rpcs for slave role for node:{}. But continuing.", nodeId, e);
            }

        } else {
            LOG.info("Registering Openflow RPCs for node:{}, role:{}", nodeId, ofpRole);
            MdSalRegistratorUtils.registerServices(rpcContext, deviceContext);
        }

        deviceContext.addDeviceContextClosedHandler(this);

        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
    }

    @Override
    public void close() throws Exception {

    }


    @Override
    public void onDeviceContextClosed(DeviceContext deviceContext) {
        RpcContext removedContext = contexts.remove(deviceContext);
        if (removedContext != null) {
            try {
                LOG.info("Unregistering rpcs for device context closure");
                removedContext.close();
            } catch (Exception e) {
                LOG.error("Exception while unregistering rpcs onDeviceContextClosed handler for node:{}. But continuing.",
                        deviceContext.getDeviceState().getNodeId(), e);
            }
        }

    }
}
