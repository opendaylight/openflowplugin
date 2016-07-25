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
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcManagerImpl implements RpcManager {

    private static final Logger LOG = LoggerFactory.getLogger(RpcManagerImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminPhaseHandler;
    private final int maxRequestsQuota;
    private final ConcurrentMap<DeviceInfo, RpcContext> contexts = new ConcurrentHashMap<>();
    private boolean isStatisticsRpcEnabled;
    private final ExtensionConverterProvider extensionConverterProvider;
    private final NotificationPublishService notificationPublishService;

    private final LifecycleConductor conductor;

    public RpcManagerImpl(
            final RpcProviderRegistry rpcProviderRegistry,
            final int quotaValue,
            final LifecycleConductor lifecycleConductor,
            final ExtensionConverterProvider extensionConverterProvider,
            final NotificationPublishService notificationPublishService) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        maxRequestsQuota = quotaValue;
        this.conductor = lifecycleConductor;
        this.extensionConverterProvider = extensionConverterProvider;
        this.notificationPublishService = notificationPublishService;
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceInfo deviceInfo, final LifecycleService lifecycleService) throws Exception {

        final DeviceContext deviceContext = Preconditions.checkNotNull(conductor.getDeviceContext(deviceInfo));

        final RpcContext rpcContext = new RpcContextImpl(
                deviceInfo,
                rpcProviderRegistry,
                deviceContext,
                deviceContext.getMessageSpy(),
                maxRequestsQuota,
                deviceInfo.getNodeInstanceIdentifier(),
                deviceContext,
                extensionConverterProvider,
                notificationPublishService);

        Verify.verify(contexts.putIfAbsent(deviceInfo, rpcContext) == null, "RpcCtx still not closed for node {}", deviceInfo.getNodeId());
        lifecycleService.setRpcContext(rpcContext);
        rpcContext.setStatisticsRpcEnabled(isStatisticsRpcEnabled);

        // finish device initialization cycle back to DeviceManager
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceInfo, lifecycleService);
    }

    @Override
    public void close() {
        for (final Iterator<RpcContext> iterator = Iterators.consumingIterator(contexts.values().iterator());
                iterator.hasNext();) {
            iterator.next().close();
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceInfo deviceInfo) {
        final RpcContext removedContext = contexts.remove(deviceInfo);
        if (removedContext != null) {
            LOG.info("Unregister RPCs services for device context closure");
            removedContext.close();
        }
        deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceInfo);
    }

    @Override
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        this.deviceTerminPhaseHandler = handler;
    }

    /**
     * This method is only for testing
     */
    @VisibleForTesting
    void addRecordToContexts(DeviceInfo deviceInfo, RpcContext rpcContexts) {
        if(!contexts.containsKey(deviceInfo)) {
            this.contexts.put(deviceInfo,rpcContexts);
        }
    }

    @Override
    public <T extends OFPContext> T gainContext(DeviceInfo deviceInfo) {
        return (T) contexts.get(deviceInfo);
    }


    @Override
    public void setStatisticsRpcEnabled(boolean statisticsRpcEnabled) {
        isStatisticsRpcEnabled = statisticsRpcEnabled;
    }
}
