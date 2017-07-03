/**
 * Copyright (c) 2015, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import io.netty.util.internal.ConcurrentSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
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
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager, ExtensionConverterProviderKeeper {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);
    private static final int SPY_RATE = 10;

    private final OpenflowProviderConfig config;
    private final DataBroker dataBroker;
    private final DeviceInitializerProvider deviceInitializerProvider;
    private final ConvertorExecutor convertorExecutor;
    private final ConcurrentMap<DeviceInfo, DeviceContext> deviceContexts = new ConcurrentHashMap<>();
    private final Set<KeyedInstanceIdentifier<Node, NodeKey>> notificationCreateNodeSend = new ConcurrentSet<>();
    private final NotificationPublishService notificationPublishService;
    private final MessageSpy messageSpy;
    private final HashedWheelTimer hashedWheelTimer;
    private TranslatorLibrary translatorLibrary;
    private ExtensionConverterProvider extensionConverterProvider;
    private ScheduledThreadPoolExecutor spyPool;

    public DeviceManagerImpl(@Nonnull final OpenflowProviderConfig config,
                             @Nonnull final DataBroker dataBroker,
                             @Nonnull final MessageSpy messageSpy,
                             @Nonnull final NotificationPublishService notificationPublishService,
                             @Nonnull final HashedWheelTimer hashedWheelTimer,
                             @Nonnull final ConvertorExecutor convertorExecutor,
                             @Nonnull final DeviceInitializerProvider deviceInitializerProvider) {
        this.config = config;
        this.dataBroker = dataBroker;
        this.deviceInitializerProvider = deviceInitializerProvider;
        this.convertorExecutor = convertorExecutor;
        this.hashedWheelTimer = hashedWheelTimer;
        this.spyPool = new ScheduledThreadPoolExecutor(1);
        this.notificationPublishService = notificationPublishService;
        this.messageSpy = messageSpy;
        DeviceInitializationUtil.makeEmptyNodes(dataBroker);
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

    public DeviceContext createContext(@Nonnull final ConnectionContext connectionContext) {

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
                        config.getBarrierCountLimit().getValue(),
                        TimeUnit.MILLISECONDS.toNanos(config.getBarrierIntervalTimeoutLimit().getValue()));
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);


        final DeviceContext deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
                convertorExecutor,
                config.isSkipTableFeatures(),
                hashedWheelTimer,
                config.isUseSingleLayerSerialization(),
                deviceInitializerProvider,
                config.isEnableFlowRemovedNotification(),
                config.isSwitchFeaturesMandatory());

        deviceContext.setSalRoleService(new SalRoleServiceImpl(deviceContext, deviceContext));
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
        synchronized (deviceContexts) {
            final int deviceContextsSize = deviceContexts.size();
            if (deviceContextsSize > 0) {
                long freshNotificationLimit = config.getGlobalNotificationQuota() / deviceContextsSize;
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
            LOG.debug("Device context removed for node {}", deviceInfo.getLOGValue());
        }
        if (deviceContexts.size() > 0) {
            this.updatePacketInRateLimiters();
        }
    }

    @Override
    public void sendNodeRemovedNotification(@Nonnull final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
        if (notificationCreateNodeSend.remove(instanceIdentifier)) {
            NodeRemovedBuilder builder = new NodeRemovedBuilder();
            builder.setNodeRef(new NodeRef(instanceIdentifier));
            LOG.info("Publishing node removed notification for {}", instanceIdentifier.firstKeyOf(Node.class).getId());
            notificationPublishService.offerNotification(builder.build());
        }
    }

    @Override
    public void sendNodeAddedNotification(@Nonnull final KeyedInstanceIdentifier<Node, NodeKey> instanceIdentifier) {
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
