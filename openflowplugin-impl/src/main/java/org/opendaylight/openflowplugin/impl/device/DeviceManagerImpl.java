/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.TimerTask;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.connection.OutboundQueueProviderImpl;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.lifecycle.LifecycleServiceImpl;
import org.opendaylight.openflowplugin.impl.services.SalRoleServiceImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
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
    private final ConvertorExecutor convertorExecutor;
    private TranslatorLibrary translatorLibrary;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminPhaseHandler;

    private final ConcurrentMap<DeviceInfo, DeviceContext> deviceContexts = new ConcurrentHashMap<>();
    private final ConcurrentMap<DeviceInfo, LifecycleService> lifecycleServices = new ConcurrentHashMap<>();

    private long barrierIntervalNanos;
    private int barrierCountLimit;

    private ExtensionConverterProvider extensionConverterProvider;
    private ScheduledThreadPoolExecutor spyPool;
    private final ClusterSingletonServiceProvider singletonServiceProvider;
    private final NotificationPublishService notificationPublishService;
    private final MessageSpy messageSpy;
    private final HashedWheelTimer hashedWheelTimer;

    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker,
                             final long globalNotificationQuota,
                             final boolean switchFeaturesMandatory,
                             final long barrierInterval,
                             final int barrierCountLimit,
                             final MessageSpy messageSpy,
                             final boolean isFlowRemovedNotificationOn,
                             final ClusterSingletonServiceProvider singletonServiceProvider,
                             final NotificationPublishService notificationPublishService,
                             final HashedWheelTimer hashedWheelTimer,
                             final ConvertorExecutor convertorExecutor,
                             final boolean skipTableFeatures) {

        this.dataBroker = dataBroker;

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
        this.singletonServiceProvider = singletonServiceProvider;
        this.notificationPublishService = notificationPublishService;
        this.messageSpy = messageSpy;
    }


    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        this.deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull DeviceInfo deviceInfo, final LifecycleService lifecycleService) throws Exception {
        // final phase - we have to add new Device to MD-SAL DataStore
        LOG.debug("Final phase of DeviceContextLevelUp for Node: {} ", deviceInfo.getNodeId());
        DeviceContext deviceContext = Preconditions.checkNotNull(deviceContexts.get(deviceInfo));
        deviceContext.onPublished();
        lifecycleService.registerDeviceRemovedHandler(this);
        lifecycleService.registerService(this.singletonServiceProvider);
    }

    @Override
    public ConnectionStatus deviceConnected(@CheckForNull final ConnectionContext connectionContext) throws Exception {
        Preconditions.checkArgument(connectionContext != null);
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();

        /*
         * This part prevent destroy another device context. Throwing here an exception result to propagate close connection
         * in {@link org.opendaylight.openflowplugin.impl.connection.org.opendaylight.openflowplugin.impl.connection.HandshakeContextImpl}
         * If context already exist we are in state closing process (connection flapping) and we should not propagate connection close
         */
         if (deviceContexts.containsKey(deviceInfo)) {
             DeviceContext deviceContext = deviceContexts.get(deviceInfo);
             LOG.warn("Node {} already connected disconnecting device. Rejecting connection", deviceInfo.getLOGValue());
             if (!deviceContext.getState().equals(OFPContext.CONTEXT_STATE.TERMINATION)) {
                 LOG.warn("Node {} context state not in TERMINATION state.",
                         connectionContext.getDeviceInfo().getLOGValue());
                 return ConnectionStatus.ALREADY_CONNECTED;
             } else {
                 return ConnectionStatus.CLOSING;
             }
         }

        LOG.info("ConnectionEvent: Device connected to controller, Device:{}, NodeId:{}",
                connectionContext.getConnectionAdapter().getRemoteAddress(), deviceInfo.getNodeId());

        // Add Disconnect handler
        connectionContext.setDeviceDisconnectedHandler(this);

        // Cache this for clarity
        final ConnectionAdapter connectionAdapter = connectionContext.getConnectionAdapter();

        // FIXME: as soon as auxiliary connection are fully supported then this is needed only before device context published
        connectionAdapter.setPacketInFiltering(true);

        final OutboundQueueProvider outboundQueueProvider = new OutboundQueueProviderImpl(deviceInfo.getVersion());

        connectionContext.setOutboundQueueProvider(outboundQueueProvider);
        final OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration =
                connectionAdapter.registerOutboundQueueHandler(outboundQueueProvider, barrierCountLimit, barrierIntervalNanos);
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);

        final LifecycleService lifecycleService = new LifecycleServiceImpl();

        final DeviceContext deviceContext = new DeviceContextImpl(
                connectionContext,
                dataBroker,
                messageSpy,
                translatorLibrary,
                this,
                convertorExecutor,
                skipTableFeatures,
                hashedWheelTimer,
                this);

        deviceContext.setSalRoleService(new SalRoleServiceImpl(deviceContext, deviceContext));
        deviceContexts.put(deviceInfo, deviceContext);

        lifecycleService.setDeviceContext(deviceContext);
        deviceContext.putLifecycleServiceIntoTxChainManager(lifecycleService);

        lifecycleServices.put(deviceInfo, lifecycleService);

        addCallbackToDeviceInitializeToSlave(deviceInfo, deviceContext, lifecycleService);

        deviceContext.setSwitchFeaturesMandatory(switchFeaturesMandatory);

        ((ExtensionConverterProviderKeeper) deviceContext).setExtensionConverterProvider(extensionConverterProvider);
        deviceContext.setNotificationPublishService(notificationPublishService);

        updatePacketInRateLimiters();

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionAdapter, deviceContext);

        connectionAdapter.setMessageListener(messageListener);
        deviceInitPhaseHandler.onDeviceContextLevelUp(connectionContext.getDeviceInfo(), lifecycleService);
        return ConnectionStatus.MAY_CONTINUE;
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
    public void onDeviceContextLevelDown(final DeviceInfo deviceInfo) {
        updatePacketInRateLimiters();
        Optional.ofNullable(lifecycleServices.get(deviceInfo)).ifPresent(OFPContext::close);
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
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        this.deviceTerminPhaseHandler = handler;
    }

    @Override
    public void onDeviceDisconnected(final ConnectionContext connectionContext) {
        LOG.trace("onDeviceDisconnected method call for Node: {}", connectionContext.getNodeId());
        final DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        final DeviceContext deviceCtx = this.deviceContexts.get(deviceInfo);

        if (Objects.isNull(deviceCtx)) {
            LOG.info("DeviceContext for Node {} was not found. Connection is terminated without OFP context suite.", deviceInfo.getLOGValue());
            return;
        }

        if (deviceCtx.getState().equals(OFPContext.CONTEXT_STATE.TERMINATION)) {
            LOG.info("Device context for node {} is already is termination state, waiting for close all context", deviceInfo.getLOGValue());
            return;
        }

        deviceCtx.close();

        if (!connectionContext.equals(deviceCtx.getPrimaryConnectionContext())) {
            LOG.debug("Node {} disconnected, but not primary connection.", connectionContext.getDeviceInfo().getLOGValue());
            // Connection is not PrimaryConnection so try to remove from Auxiliary Connections
            deviceCtx.removeAuxiliaryConnectionContext(connectionContext);
        }

        // TODO: Auxiliary connections supported ?
        // Device is disconnected and so we need to close TxManager
        final ListenableFuture<Void> future = deviceCtx.shuttingDownDataStoreTransactions();
        Futures.addCallback(future, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                LOG.debug("TxChainManager for device {} is closed successful.", deviceInfo.getLOGValue());
                deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceInfo);
            }

            @Override
            public void onFailure(final Throwable t) {
                LOG.warn("TxChainManager for device {} failed by closing.", deviceInfo.getLOGValue());
                LOG.trace("TxChainManager failed by closing. ", t);
                deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceInfo);
            }
        });

        // Add timer for Close TxManager because it could fail in cluster without notification
        final TimerTask timerTask = timeout -> {
            if (!future.isDone()) {
                LOG.warn("Shutting down TxChain for node {} not completed during 10 sec. Continue anyway.", deviceInfo.getLOGValue());
                future.cancel(false);
            }
        };

        hashedWheelTimer.newTimeout(timerTask, 10, TimeUnit.SECONDS);
    }

    @VisibleForTesting
    void addDeviceContextToMap(final DeviceInfo deviceInfo, final DeviceContext deviceContext){
        deviceContexts.put(deviceInfo, deviceContext);
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
    public CheckedFuture<Void, TransactionCommitFailedException> removeDeviceFromOperationalDS(final DeviceInfo deviceInfo) {
        final WriteTransaction delWtx = dataBroker.newWriteOnlyTransaction();
        delWtx.delete(LogicalDatastoreType.OPERATIONAL, deviceInfo.getNodeInstanceIdentifier());
        final CheckedFuture<Void, TransactionCommitFailedException> delFuture = delWtx.submit();

        Futures.addCallback(delFuture, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Delete Node {} was successful", deviceInfo.getLOGValue());
                }
            }

            @Override
            public void onFailure(@Nonnull final Throwable t) {
                LOG.warn("Delete node {} failed with exception {}", deviceInfo.getLOGValue(), t);
            }
        });

        return delFuture;
    }


    private void addCallbackToDeviceInitializeToSlave(final DeviceInfo deviceInfo, final DeviceContext deviceContext, final LifecycleService lifecycleService) {
        Futures.addCallback(deviceContext.makeDeviceSlave(), new FutureCallback<RpcResult<SetRoleOutput>>() {
            @Override
            public void onSuccess(@Nullable RpcResult<SetRoleOutput> setRoleOutputRpcResult) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Role SLAVE was successfully propagated on device, node {}", deviceInfo.getLOGValue());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.warn("Was not able to set role SLAVE to device on node {} ",deviceInfo.getLOGValue());
                lifecycleService.closeConnection();
            }
        });
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

    public void onDeviceRemoved(DeviceInfo deviceInfo) {
        deviceContexts.remove(deviceInfo);
        LOG.debug("Device context removed for node {}", deviceInfo.getLOGValue());

        lifecycleServices.remove(deviceInfo);
        LOG.debug("Lifecycle service removed for node {}", deviceInfo.getLOGValue());
    }
}
