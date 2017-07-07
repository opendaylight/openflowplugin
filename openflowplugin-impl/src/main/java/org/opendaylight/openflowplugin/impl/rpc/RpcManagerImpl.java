/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.rpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcManagerImpl implements RpcManager {

    private static final Logger LOG = LoggerFactory.getLogger(RpcManagerImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;
    private int rpcRequestQuota;
    private final ConcurrentMap<DeviceInfo, RpcContext> contexts = new ConcurrentHashMap<>();
    private boolean isStatisticsRpcEnabled;
    private final ExtensionConverterProvider extensionConverterProvider;
    private final ConvertorExecutor convertorExecutor;
    private final NotificationPublishService notificationPublishService;


    public RpcManagerImpl(final RpcProviderRegistry rpcProviderRegistry,
                          final ExtensionConverterProvider extensionConverterProvider,
                          final ConvertorExecutor convertorExecutor,
                          final NotificationPublishService notificationPublishService) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.extensionConverterProvider = extensionConverterProvider;
        this.convertorExecutor = convertorExecutor;
        this.notificationPublishService = notificationPublishService;
    }

    @Override
    public void close() {
        for (final Iterator<RpcContext> iterator = Iterators.consumingIterator(contexts.values().iterator());
                iterator.hasNext();) {
            iterator.next().close();
        }
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
    public void setStatisticsRpcEnabled(boolean statisticsRpcEnabled) {
        isStatisticsRpcEnabled = statisticsRpcEnabled;
    }

    @Override
    public void setRpcRequestQuota(final int rpcRequestQuota) {
        this.rpcRequestQuota = rpcRequestQuota;
    }

    @Override
    public RpcContext createContext(final @Nonnull DeviceContext deviceContext) {
        final RpcContextImpl rpcContext = new RpcContextImpl(
                rpcProviderRegistry,
                rpcRequestQuota,
                deviceContext,
                extensionConverterProvider,
                convertorExecutor,
                notificationPublishService,
                this.isStatisticsRpcEnabled);

        contexts.put(deviceContext.getDeviceInfo(), rpcContext);
        return rpcContext;
    }

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contexts.remove(deviceInfo);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Rpc context removed for node {}", deviceInfo.getLOGValue());
        }
    }
}
