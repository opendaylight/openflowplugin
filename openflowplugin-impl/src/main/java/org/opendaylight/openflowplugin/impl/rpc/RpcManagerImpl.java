/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcManagerImpl implements RpcManager {

    private static final Logger LOG = LoggerFactory.getLogger(RpcManagerImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminPhaseHandler;
    private final int maxRequestsQuota;
    private final ConcurrentMap<NodeId, RpcContext> contexts = new ConcurrentHashMap<>();

    private final LifecycleConductor conductor;

    public RpcManagerImpl(final RpcProviderRegistry rpcProviderRegistry,
                          final int quotaValue,
                          final LifecycleConductor lifecycleConductor) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        maxRequestsQuota = quotaValue;
        this.conductor = lifecycleConductor;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceInfo deviceInfo) throws Exception {

        final DeviceContext deviceContext = Preconditions.checkNotNull(conductor.getDeviceContext(deviceInfo.getNodeId()));

        final RpcContext rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                deviceContext,
                deviceContext.getMessageSpy(),
                maxRequestsQuota,
                deviceContext.getDeviceState().getNodeInstanceIdentifier());

        deviceContext.setRpcContext(rpcContext);

        Verify.verify(contexts.putIfAbsent(deviceInfo.getNodeId(), rpcContext) == null, "RpcCtx still not closed for node {}", deviceInfo.getNodeId());

        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceInfo);
    }

    @Override
    public void close() {
        for (final Iterator<RpcContext> iterator = Iterators.consumingIterator(contexts.values().iterator());
                iterator.hasNext();) {
            iterator.next().close();
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceContext deviceContext) {
        final RpcContext removedContext = contexts.remove(deviceContext.getDeviceState().getNodeId());
        if (removedContext != null) {
            LOG.info("Unregister RPCs services for device context closure");
            removedContext.close();
        }
        deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceContext);
    }

    @Override
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        this.deviceTerminPhaseHandler = handler;
    }

    /**
     * This method is only for testing
     */
    @VisibleForTesting
    void addRecordToContexts(NodeId nodeId, RpcContext rpcContexts) {
        if(!contexts.containsKey(nodeId)) {
            this.contexts.put(nodeId,rpcContexts);
        }
    }
}
