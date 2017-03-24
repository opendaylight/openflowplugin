/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.connection.OutboundQueueProviderImpl;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.services.sal.SalRoleServiceImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager, ExtensionConverterProviderKeeper {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private final long globalNotificationQuota;
    private final boolean switchFeaturesMandatory;
    private boolean isFlowRemovedNotificationOn;
    private boolean skipTableFeatures;
    private static final int SPY_RATE = 10;

    private final DataBroker dataBroker;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorExecutor convertorExecutor;
    private TranslatorLibrary translatorLibrary;

    private final ConcurrentMap<DeviceInfo, DeviceContext> deviceContexts = new ConcurrentHashMap<>();
    private final List<DeviceInfo> notificationCreateNodeSend = new ArrayList<>();

    private long barrierIntervalNanos;
    private int barrierCountLimit;

    private ExtensionConverterProvider extensionConverterProvider;
    private ScheduledThreadPoolExecutor spyPool;
    private final NotificationPublishService notificationPublishService;
    private final MessageSpy messageSpy;
    private final HashedWheelTimer hashedWheelTimer;
    private boolean useSingleLayerSerialization;

    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker,
                             @Nonnull final MessageSpy messageSpy,
                             final NotificationPublishService notificationPublishService,
                             @Nonnull final HashedWheelTimer hashedWheelTimer,
                             @Nonnull final ConvertorExecutor convertorExecutor,
                             @Nonnull final DeviceInitializerProvider deviceInitializerProvider,
                             final long globalNotificationQuota,
                             final boolean switchFeaturesMandatory,
                             final long barrierInterval,
                             final int barrierCountLimit,
                             final boolean isFlowRemovedNotificationOn,
                             final boolean skipTableFeatures,
                             final boolean useSingleLayerSerialization) {

        this.dataBroker = dataBroker;
        this.deviceInitializerProvider = deviceInitializerProvider;

        /* merge empty nodes to oper DS to predict any problems with missing parent for Node */
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        final NodesBuilder nodesBuilder = new NodesBuilder();
        nodesBuilder.setNode(Collections.<Node>emptyList());
        tx.merge(LogicalDatastoreType.OPERATIONAL, InstanceIdentifier.create(Nodes.class), nodesBuilder.build());
        try {
            tx.submit().get();
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Creation of node failed.", e);
            throw new IllegalStateException(e);
        }

        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.globalNotificationQuota = globalNotificationQuota;
        this.isFlowRemovedNotificationOn = isFlowRemovedNotificationOn;
        this.skipTableFeatures = skipTableFeatures;
        this.convertorExecutor = convertorExecutor;
        this.hashedWheelTimer = hashedWheelTimer;
        this.barrierIntervalNanos = TimeUnit.MILLISECONDS.toNanos(barrierInterval);
        this.barrierCountLimit = barrierCountLimit;
        this.spyPool = new ScheduledThreadPoolExecutor(1);
        this.notificationPublishService = notificationPublishService;
        this.messageSpy = messageSpy;
        this.useSingleLayerSerialization = useSingleLayerSerialization;
    }

    @Override
    public TranslatorLibrary oook() {
        return translatorLibrary;
    }

    @Override
    public void setTranslatorLibrary(final TranslatorLibrary translatorLibrary) {
        this.translatorLibrary = translatorLibrary;
    }

    @Override
    public void close() {
        for (final Iterator<DeviceContext> iterator = Iterators.consumingIterator(deviceContexts.values().iterator());
                iterator.hasNext();) {
            final DeviceContext deviceCtx = iterator.next();
            deviceCtx.shutdownConnection();
            deviceCtx.shuttingDownDataStoreTransactions();
        }

        Optional.ofNullable(spyPool).ifPresent(ScheduledThreadPoolExecutor::shutdownNow);
        spyPool = null;

    }

    @Override
    public void initialize() {
        spyPool.scheduleAtFixedRate(messageSpy, SPY_RATE, SPY_RATE, TimeUnit.SECONDS);
    }

    @Override
    public void setExtensionConverterProvider(final ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }

    @Override
    public void setFlowRemovedNotificationOn(boolean isNotificationFlowRemovedOff) {
        this.isFlowRemovedNotificationOn = isNotificationFlowRemovedOff;
    }

    @Override
    public boolean isFlowRemovedNotificationOn() {
        return this.isFlowRemovedNotificationOn;
    }

    @Override
    public void setSkipTableFeatures(boolean skipTableFeaturesValue) {
        skipTableFeatures = skipTableFeaturesValue;
    }

    @Override
    public void setBarrierCountLimit(final int barrierCountLimit) {
        this.barrierCountLimit = barrierCountLimit;
    }

    @Override
    public void setBarrierInterval(final long barrierTimeoutLimit) {
        this.barrierIntervalNanos = TimeUnit.MILLISECONDS.toNanos(barrierTimeoutLimit);
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final KeyedInstanceIdentifier<Node, NodeKey> ii) {    
        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, ii);
        final CheckedFuture<Void, TransactionCommitFailedException> delFuture = delWtx.submit();

        Futures.addCallback(delFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Delete Node {} was successful", ii);
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                LOG.warn("Delete node {} failed with exception {}", ii, t);
            }
        });

        return delFuture;
    }
    
    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final DeviceInfo deviceInfo) {
        return this.removeDeviceFromOperationalDS(deviceInfo.getNodeInstanceIdentifier());
    }

    @Override
    public void setUseSingleLayerSerialization(final Boolean useSingleLayerSerialization) {
        this.useSingleLayerSerialization = useSingleLayerSerialization;
    }

    public DeviceContext createContext(@CheckForNull final ConnectionContext connectionContext) {

        LOG.info("ConnectionEvent: Device connected to controller, Device:{}, NodeId:{}",
                connectionContext.getConnectionAdapter().getRemoteAddress(),
                connectionContext.getDeviceInfo().getNodeId());

        connectionContext.getConnectionAdapter().setPacketInFiltering(true);

        final OutboundQueueProvider outboundQueueProvider
                = new OutboundQueueProviderImpl(connectionContext.getDeviceInfo().getVersion());

        connectionContext.setOutboundQueueProvider(outboundQueueProvider);
        final OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration =
                connectionContext.getConnectionAdapter().registerOutboundQueueHandler(
                        outboundQueueProvider,
                        barrierCountLimit,
                        barrierIntervalNanos);
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);


        final DeviceContext deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
                this,
                convertorExecutor,
                skipTableFeatures,
                hashedWheelTimer,
                useSingleLayerSerialization,
                deviceInitializerProvider);

        deviceContext.setSalRoleService(new SalRoleServiceImpl(deviceContext, deviceContext));
        deviceContext.setSwitchFeaturesMandatory(switchFeaturesMandatory);
        ((ExtensionConverterProviderKeeper) deviceContext).setExtensionConverterProvider(extensionConverterProvider);
        deviceContext.setNotificationPublishService(notificationPublishService);

        deviceContexts.put(connectionContext.getDeviceInfo(), deviceContext);
        updatePacketInRateLimiters();

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionContext.getConnectionAdapter(), deviceContext);

        connectionContext.getConnectionAdapter().setMessageListener(messageListener);

        return deviceContext;
    }

    private void updatePacketInRateLimiters() {
        synchronized (deviceContexts) {
            final int deviceContextsSize = deviceContexts.size();
            if (deviceContextsSize > 0) {
                long freshNotificationLimit = globalNotificationQuota / deviceContextsSize;
                if (freshNotificationLimit < 100) {
                    freshNotificationLimit = 100;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("fresh notification limit = {}", freshNotificationLimit);
                }
                for (final DeviceContext deviceContext : deviceContexts.values()) {
                    deviceContext.updatePacketInRateLimit(freshNotificationLimit);
                }
            }
        }
    }

    private void sendNodeRemovedNotification(final DeviceInfo deviceInfo) {
        NodeRemovedBuilder builder = new NodeRemovedBuilder();
        builder.setNodeRef(new NodeRef(deviceInfo.getNodeInstanceIdentifier()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Publishing node removed notification for {}", deviceInfo.getLOGValue());
        }
        notificationPublishService.offerNotification(builder.build());
    }


    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        deviceContexts.remove(deviceInfo);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device context removed for node {}", deviceInfo.getLOGValue());
        }
        this.updatePacketInRateLimiters();
        this.sendNodeRemovedNotification(deviceInfo);
    }

    @Override
    public long getBarrierIntervalNanos() {
        return barrierIntervalNanos;
    }

    @Override
    public int getBarrierCountLimit() {
        return barrierCountLimit;
    }

    @Override
    public void sendNodeAddedNotification(@CheckForNull final DeviceInfo deviceInfo) {
        if (!notificationCreateNodeSend.contains(deviceInfo)) {
            notificationCreateNodeSend.add(deviceInfo);
            NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
            builder.setId(deviceInfo.getNodeId());
            builder.setNodeRef(new NodeRef(deviceInfo.getNodeInstanceIdentifier()));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Publishing node added notification for {}", deviceInfo.getLOGValue());
            }
            notificationPublishService.offerNotification(builder.build());
        }
    }
}
