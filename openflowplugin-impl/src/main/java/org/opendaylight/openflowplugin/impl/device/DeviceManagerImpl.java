/*
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.connection.OutboundQueueProviderImpl;
import org.opendaylight.openflowplugin.impl.device.initialization.DeviceInitializerProvider;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.util.concurrent.QueuedNotificationManager;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerImpl implements DeviceManager, ExtensionConverterProviderKeeper {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);
    private static final int SPY_RATE = 10;

    private final OpenflowProviderConfig config;
    private final DataBroker dataBroker;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorExecutor convertorExecutor;
    private final ConcurrentMap<DeviceInfo, DeviceContext> deviceContexts = new ConcurrentHashMap<>();
    private final Set<KeyedInstanceIdentifier<Node, NodeKey>> notificationCreateNodeSend =
            ConcurrentHashMap.newKeySet();
    private final NotificationPublishService notificationPublishService;
    private final MessageSpy messageSpy;
    private final HashedWheelTimer hashedWheelTimer;
    private final Object updatePacketInRateLimitersLock = new Object();
    private TranslatorLibrary translatorLibrary;
    private ExtensionConverterProvider extensionConverterProvider;
    private ScheduledThreadPoolExecutor spyPool;
    private ContextChainHolder contextChainHolder;
    private final QueuedNotificationManager<String, Runnable> queuedNotificationManager;

    public DeviceManagerImpl(@NonNull final OpenflowProviderConfig config,
                             @NonNull final DataBroker dataBroker,
                             @NonNull final MessageSpy messageSpy,
                             @NonNull final NotificationPublishService notificationPublishService,
                             @NonNull final HashedWheelTimer hashedWheelTimer,
                             @NonNull final ConvertorExecutor convertorExecutor,
                             @NonNull final DeviceInitializerProvider deviceInitializerProvider,
                             @NonNull final ExecutorService executorService) {
        this.config = config;
        this.dataBroker = dataBroker;
        this.deviceInitializerProvider = deviceInitializerProvider;
        this.convertorExecutor = convertorExecutor;
        this.hashedWheelTimer = hashedWheelTimer;
        this.spyPool = new ScheduledThreadPoolExecutor(1);
        this.notificationPublishService = notificationPublishService;
        this.messageSpy = messageSpy;
        DeviceInitializationUtil.makeEmptyNodes(dataBroker);
        this.queuedNotificationManager =  QueuedNotificationManager.create(executorService, (key, entries) -> {
            entries.forEach(Runnable::run);
        }, 2048, "port-status-queue");
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
        deviceContexts.values().forEach(OFPContext::close);
        deviceContexts.clear();
        if (spyPool != null) {
            spyPool.shutdownNow();
            spyPool = null;
        }
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
    public ListenableFuture<?> removeDeviceFromOperationalDS(@NonNull final KeyedInstanceIdentifier<Node, NodeKey> ii) {

        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, ii);
        return delWtx.commit();

    }

    @Override
    public DeviceContext createContext(@NonNull final ConnectionContext connectionContext) {

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
                        config.getBarrierCountLimit().getValue().toJava(),
                        TimeUnit.MILLISECONDS.toNanos(config.getBarrierIntervalTimeoutLimit().getValue().toJava()));
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);


        final DeviceContext deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
                convertorExecutor,
                config.getSkipTableFeatures(),
                hashedWheelTimer,
                config.getUseSingleLayerSerialization(),
                deviceInitializerProvider,
                config.getEnableFlowRemovedNotification(),
                config.getSwitchFeaturesMandatory(),
                contextChainHolder,
                queuedNotificationManager,
                config.getIsStatisticsPollingOn());
        ((ExtensionConverterProviderKeeper) deviceContext).setExtensionConverterProvider(extensionConverterProvider);
        deviceContext.setNotificationPublishService(notificationPublishService);

        deviceContexts.put(connectionContext.getDeviceInfo(), deviceContext);
        updatePacketInRateLimiters();

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionContext.getConnectionAdapter(), deviceContext);

        connectionContext.getConnectionAdapter().setMessageListener(messageListener);
        connectionContext.getConnectionAdapter().setAlienMessageListener(messageListener);

        return deviceContext;
    }

    private void updatePacketInRateLimiters() {
        synchronized (updatePacketInRateLimitersLock) {
            final int deviceContextsSize = deviceContexts.size();
            if (deviceContextsSize > 0) {
                long freshNotificationLimit = config.getGlobalNotificationQuota().toJava() / deviceContextsSize;
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

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        deviceContexts.remove(deviceInfo);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device context removed for node {}", deviceInfo);
        }
        this.updatePacketInRateLimiters();
    }

    @Override
    public void sendNodeRemovedNotification(@NonNull final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        if (notificationCreateNodeSend.remove(instanceIdentifier)) {
            NodeRemovedBuilder builder = new NodeRemovedBuilder();
            builder.setNodeRef(new NodeRef(instanceIdentifier));
            LOG.info("Publishing node removed notification for {}", instanceIdentifier.firstKeyOf(Node.class).getId());
            notificationPublishService.offerNotification(builder.build());
        }
    }

    @Override
    public void setContextChainHolder(@NonNull final ContextChainHolder contextChainHolder) {
        this.contextChainHolder = contextChainHolder;
    }

    @Override
    public void sendNodeAddedNotification(@NonNull final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        if (!notificationCreateNodeSend.contains(instanceIdentifier)) {
            notificationCreateNodeSend.add(instanceIdentifier);
            final NodeId id = instanceIdentifier.firstKeyOf(Node.class).getId();
            NodeUpdatedBuilder builder = new NodeUpdatedBuilder();
            builder.setId(id);
            builder.setNodeRef(new NodeRef(instanceIdentifier));
            LOG.info("Publishing node added notification for {}", id);
            notificationPublishService.offerNotification(builder.build());
        }
    }
}
