/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.connection.OutboundQueueProviderImpl;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class DeviceManagerImpl implements DeviceManager, ExtensionConverterProviderKeeper, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private static final long TICK_DURATION = 10; // 0.5 sec.
    private final long globalNotificationQuota;
    private ScheduledThreadPoolExecutor spyPool;
    private final int spyRate = 10;

    private final DataBroker dataBroker;
    private final HashedWheelTimer hashedWheelTimer;
    private TranslatorLibrary translatorLibrary;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private NotificationService notificationService;
    private NotificationPublishService notificationPublishService;

    private final Set<DeviceContext> deviceContexts = Sets.newConcurrentHashSet();
    private final MessageIntelligenceAgency messageIntelligenceAgency;

    private final long barrierNanos = TimeUnit.MILLISECONDS.toNanos(500);
    private final int maxQueueDepth = 25600;
    private final boolean switchFeaturesMandatory;
    private final DeviceTransactionChainManagerProvider deviceTransactionChainManagerProvider;
    private ExtensionConverterProvider extensionConverterProvider;

    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker,
                             @Nonnull final MessageIntelligenceAgency messageIntelligenceAgency,
                             final boolean switchFeaturesMandatory,
                             final long globalNotificationQuota) {
        this.globalNotificationQuota = globalNotificationQuota;
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        hashedWheelTimer = new HashedWheelTimer(TICK_DURATION, TimeUnit.MILLISECONDS, 500);
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

        this.messageIntelligenceAgency = messageIntelligenceAgency;
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        deviceTransactionChainManagerProvider = new DeviceTransactionChainManagerProvider(dataBroker);
    }


    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        // final phase - we have to add new Device to MD-SAL DataStore
        Preconditions.checkNotNull(deviceContext);
        try {

            if (deviceContext.getDeviceState().getRole() != OfpRole.BECOMESLAVE) {
                ((DeviceContextImpl) deviceContext).initialSubmitTransaction();
                deviceContext.onPublished();

            } else {
                //if role = slave
                try {
                    ((DeviceContextImpl) deviceContext).cancelTransaction();
                } catch (Exception e) {
                    //TODO: how can we avoid it. pingpong does not have cancel
                    LOG.debug("Expected Exception: Cancel Txn exception thrown for slaves", e);
                }

            }

        } catch (final Exception e) {
            LOG.warn("Node {} can not be add to OPERATIONAL DataStore yet because {} ", deviceContext.getDeviceState().getNodeId(), e.getMessage());
            LOG.trace("Problem with add node {} to OPERATIONAL DataStore", deviceContext.getDeviceState().getNodeId(), e);
            try {
                deviceContext.close();
            } catch (final Exception e1) {
                LOG.warn("Device context close FAIL - " + deviceContext.getDeviceState().getNodeId());
            }
        }
    }

    @Override
    public void deviceConnected(@CheckForNull final ConnectionContext connectionContext) {
        Preconditions.checkArgument(connectionContext != null);
        initializeDeviceContext(connectionContext);
    }

    private void initializeDeviceContext(final ConnectionContext connectionContext) {
        LOG.info("Initializing New Connection DeviceContext for node:{}",  connectionContext.getNodeId());
        // Cache this for clarity
        final ConnectionAdapter connectionAdapter = connectionContext.getConnectionAdapter();

        //FIXME: as soon as auxiliary connection are fully supported then this is needed only before device context published
        connectionAdapter.setPacketInFiltering(true);

        final Short version = connectionContext.getFeatures().getVersion();
        final OutboundQueueProvider outboundQueueProvider = new OutboundQueueProviderImpl(version);

        connectionContext.setOutboundQueueProvider(outboundQueueProvider);
        final OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration =
                connectionAdapter.registerOutboundQueueHandler(outboundQueueProvider, maxQueueDepth, barrierNanos);
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);

        final NodeId nodeId = connectionContext.getNodeId();
        final DeviceState deviceState = new DeviceStateImpl(connectionContext.getFeatures(), nodeId);

        final DeviceContext deviceContext = new DeviceContextImpl(connectionContext, deviceState, dataBroker,
                hashedWheelTimer, messageIntelligenceAgency, outboundQueueProvider, translatorLibrary);
        deviceContext.addDeviceContextClosedHandler(this);
        // We would like to crete/register TxChainManager after
        final DeviceTransactionChainManagerProvider.TransactionChainManagerRegistration txChainManagerReg = deviceTransactionChainManagerProvider
                .provideTransactionChainManager(connectionContext);
        if (txChainManagerReg.ownedByInvokingConnectionContext()) {
            //this actually is new registration for currently processed connection context
            ((DeviceContextImpl) deviceContext).setTransactionChainManager(txChainManagerReg.getTransactionChainManager());
        } else {
            LOG.info("In deviceConnected {}, ownedByInvokingConnectionContext is false", connectionContext.getNodeId());
            deviceContext.close();
            return;
        }
        ((ExtensionConverterProviderKeeper) deviceContext).setExtensionConverterProvider(extensionConverterProvider);
        deviceContext.setNotificationService(notificationService);
        deviceContext.setNotificationPublishService(notificationPublishService);

        deviceContexts.add(deviceContext);

        updatePacketInRateLimiters();

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionAdapter, deviceContext);
        connectionAdapter.setMessageListener(messageListener);

        deviceCtxLevelUp(deviceContext);
    }

    private void updatePacketInRateLimiters() {
        synchronized (deviceContexts) {
            final int deviceContextsSize = deviceContexts.size();
            if (deviceContextsSize > 0) {
                long freshNotificationLimit = globalNotificationQuota / deviceContextsSize;
                if (freshNotificationLimit < 100) {
                    freshNotificationLimit = 100;
                }
                LOG.debug("fresh notification limit = {}", freshNotificationLimit);
                for (DeviceContext deviceContext : deviceContexts) {
                    deviceContext.updatePacketInRateLimit(freshNotificationLimit);
                }
            }
        }
    }

    void deviceCtxLevelUp(final DeviceContext deviceContext) {
        deviceContext.getDeviceState().setValid(true);
        LOG.trace("Device context level up called.");
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
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
    public void setNotificationService(final NotificationService notificationServiceParam) {
        notificationService = notificationServiceParam;
    }

    @Override
    public void setNotificationPublishService(final NotificationPublishService notificationService) {
        notificationPublishService = notificationService;
    }

    @Override
    public void close() throws Exception {
        for (final DeviceContext deviceContext : deviceContexts) {
            deviceContext.close();
        }
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        deviceContexts.remove(deviceContext);
        updatePacketInRateLimiters();
    }

    @Override
    public void initialize() {
        spyPool = new ScheduledThreadPoolExecutor(1);
        spyPool.scheduleAtFixedRate(messageIntelligenceAgency, spyRate, spyRate, TimeUnit.SECONDS);
    }

    @Override
    public void setExtensionConverterProvider(ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }
}
