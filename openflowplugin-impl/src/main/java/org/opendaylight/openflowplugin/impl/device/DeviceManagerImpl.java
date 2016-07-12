/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.TimerTask;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceSynchronizeListener;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceValidListener;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterProviderKeeper;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.connection.OutboundQueueProviderImpl;
import org.opendaylight.openflowplugin.impl.device.listener.OpenflowProtocolListenerFullImpl;
import org.opendaylight.openflowplugin.impl.util.DeviceInitializationUtils;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
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
public class DeviceManagerImpl implements DeviceManager, ExtensionConverterProviderKeeper {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);

    private final long globalNotificationQuota;
    private final boolean switchFeaturesMandatory;
    private boolean isNotificationFlowRemovedOff;

    private static final int SPY_RATE = 10;

    private final DataBroker dataBroker;
    private final ConvertorExecutor convertorExecutor;
    private TranslatorLibrary translatorLibrary;
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminPhaseHandler;

    private final ConcurrentMap<DeviceInfo, DeviceContext> deviceContexts = new ConcurrentHashMap<>();

    private final long barrierIntervalNanos;
    private final int barrierCountLimit;
    private ExtensionConverterProvider extensionConverterProvider;
    private ScheduledThreadPoolExecutor spyPool;
    private Set<DeviceSynchronizeListener> deviceSynchronizedListeners;
    private Set<DeviceValidListener> deviceValidListeners;

    private final LifecycleConductor conductor;

    public DeviceManagerImpl(@Nonnull final DataBroker dataBroker,
                             final long globalNotificationQuota, final boolean switchFeaturesMandatory,
                             final long barrierInterval, final int barrierCountLimit,
                             final LifecycleConductor lifecycleConductor, boolean isNotificationFlowRemovedOff,
                             final ConvertorExecutor convertorExecutor) {
        this.switchFeaturesMandatory = switchFeaturesMandatory;
        this.globalNotificationQuota = globalNotificationQuota;
        this.isNotificationFlowRemovedOff = isNotificationFlowRemovedOff;
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.convertorExecutor = convertorExecutor;
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

        this.barrierIntervalNanos = TimeUnit.MILLISECONDS.toNanos(barrierInterval);
        this.barrierCountLimit = barrierCountLimit;

        this.conductor = lifecycleConductor;
        spyPool = new ScheduledThreadPoolExecutor(1);
        this.deviceSynchronizedListeners = new HashSet<>();
        this.deviceValidListeners = new HashSet<>();
    }


    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        this.deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(@CheckForNull DeviceInfo deviceInfo) throws Exception {
        // final phase - we have to add new Device to MD-SAL DataStore
        LOG.debug("Final phase of DeviceContextLevelUp for Node: {} ", deviceInfo.getNodeId());
        DeviceContext deviceContext = Preconditions.checkNotNull(deviceContexts.get(deviceInfo));
        ((DeviceContextImpl) deviceContext).initialSubmitTransaction();
        deviceContext.onPublished();
    }

    @Override
    public boolean deviceConnected(@CheckForNull final ConnectionContext connectionContext) throws Exception {
        Preconditions.checkArgument(connectionContext != null);

        DeviceInfo deviceInfo = connectionContext.getDeviceInfo();
        /**
         * This part prevent destroy another device context. Throwing here an exception result to propagate close connection
         * in {@link org.opendaylight.openflowplugin.impl.connection.org.opendaylight.openflowplugin.impl.connection.HandshakeContextImpl}
         * If context already exist we are in state closing process (connection flapping) and we should not propagate connection close
         */
         if (deviceContexts.containsKey(deviceInfo)) {
            LOG.warn("Rejecting connection from node which is already connected and there exist deviceContext for it: {}", connectionContext.getNodeId());
             return false;
         }

        LOG.info("ConnectionEvent: Device connected to controller, Device:{}, NodeId:{}",
                connectionContext.getConnectionAdapter().getRemoteAddress(), deviceInfo.getNodeId());

        // Add Disconnect handler
        connectionContext.setDeviceDisconnectedHandler(DeviceManagerImpl.this);
        // Cache this for clarity
        final ConnectionAdapter connectionAdapter = connectionContext.getConnectionAdapter();

        //FIXME: as soon as auxiliary connection are fully supported then this is needed only before device context published
        connectionAdapter.setPacketInFiltering(true);

        final OutboundQueueProvider outboundQueueProvider = new OutboundQueueProviderImpl(deviceInfo.getVersion());

        connectionContext.setOutboundQueueProvider(outboundQueueProvider);
        final OutboundQueueHandlerRegistration<OutboundQueueProvider> outboundQueueHandlerRegistration =
                connectionAdapter.registerOutboundQueueHandler(outboundQueueProvider, barrierCountLimit, barrierIntervalNanos);
        connectionContext.setOutboundQueueHandleRegistration(outboundQueueHandlerRegistration);

        final DeviceState deviceState = new DeviceStateImpl(deviceInfo);
        this.addDeviceSynchronizeListener(deviceState);
        this.addDeviceValidListener(deviceState);

        final DeviceContext deviceContext = new DeviceContextImpl(connectionContext,
                deviceState,
                dataBroker,
                conductor,
                outboundQueueProvider,
                translatorLibrary,
                this);

        Verify.verify(deviceContexts.putIfAbsent(deviceInfo, deviceContext) == null, "DeviceCtx still not closed.");

        ((ExtensionConverterProviderKeeper) deviceContext).setExtensionConverterProvider(extensionConverterProvider);
        deviceContext.setNotificationPublishService(conductor.getNotificationPublishService());

        updatePacketInRateLimiters();

        final OpenflowProtocolListenerFullImpl messageListener = new OpenflowProtocolListenerFullImpl(
                connectionAdapter, deviceContext);
        connectionAdapter.setMessageListener(messageListener);
        notifyDeviceValidListeners(deviceInfo, true);

        deviceInitPhaseHandler.onDeviceContextLevelUp(connectionContext.getDeviceInfo());

        notifyDeviceSynchronizeListeners(deviceInfo, true);

        return true;
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
                for (final DeviceContext deviceContext : deviceContexts.values()) {
                    deviceContext.updatePacketInRateLimit(freshNotificationLimit);
                }
            }
        }
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
            notifyDeviceValidListeners(deviceCtx.getDeviceInfo(), false);
            deviceCtx.shutdownConnection();
            deviceCtx.shuttingDownDataStoreTransactions();
        }

        if (spyPool != null) {
            spyPool.shutdownNow();
            spyPool = null;
        }
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceInfo deviceInfo) {
        LOG.debug("onDeviceContextClosed for Node {}", deviceInfo.getNodeId());
        deviceContexts.remove(deviceInfo);
        updatePacketInRateLimiters();
    }

    @Override
    public void initialize() {
        spyPool.scheduleAtFixedRate(conductor.getMessageIntelligenceAgency(), SPY_RATE, SPY_RATE, TimeUnit.SECONDS);
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

        if (null == deviceCtx) {
            LOG.info("DeviceContext for Node {} was not found. Connection is terminated without OFP context suite.", deviceInfo.getNodeId());
            return;
        }

        if (!connectionContext.equals(deviceCtx.getPrimaryConnectionContext())) {
            /* Connection is not PrimaryConnection so try to remove from Auxiliary Connections */
            deviceCtx.removeAuxiliaryConnectionContext(connectionContext);
        } else {
            notifyDeviceValidListeners(deviceInfo, false);
            /* Device is disconnected and so we need to close TxManager */
            final ListenableFuture<Void> future = deviceCtx.shuttingDownDataStoreTransactions();
            Futures.addCallback(future, new FutureCallback<Void>() {

                @Override
                public void onSuccess(final Void result) {
                    LOG.debug("TxChainManager for device {} is closed successful.", deviceInfo.getNodeId());
                    deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceInfo);
                }

                @Override
                public void onFailure(final Throwable t) {
                    LOG.warn("TxChainManager for device {} failed by closing.", deviceInfo.getNodeId(), t);
                    deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceInfo);
                }
            });
            /* Add timer for Close TxManager because it could fain ind cluster without notification */
            final TimerTask timerTask = timeout -> {
                if (!future.isDone()) {
                    LOG.info("Shutting down TxChain for node {} not completed during 10 sec. Continue anyway.", deviceInfo.getNodeId());
                    future.cancel(false);
                }
            };
            conductor.newTimeout(timerTask, 10, TimeUnit.SECONDS);
        }
    }

    @VisibleForTesting
    void addDeviceContextToMap(final DeviceInfo deviceInfo, final DeviceContext deviceContext){
        deviceContexts.put(deviceInfo, deviceContext);
    }

    @Override
    public <T extends OFPContext> T gainContext(final DeviceInfo deviceInfo) {
        return (T) deviceContexts.get(deviceInfo);
    }

    @Override
    public ListenableFuture<Void> onClusterRoleChange(final DeviceInfo deviceInfo, final OfpRole role) {
        DeviceContext deviceContext = conductor.getDeviceContext(deviceInfo);
        LOG.trace("onClusterRoleChange {} for node:", role, deviceInfo.getNodeId());
        if (OfpRole.BECOMEMASTER.equals(role)) {
            return onDeviceTakeClusterLeadership(deviceInfo);
        }
        return ((DeviceContextImpl)deviceContext).getTransactionChainManager().deactivateTransactionManager();
    }

    @Override
    public void addDeviceSynchronizeListener(final DeviceSynchronizeListener deviceSynchronizeListener) {
        this.deviceSynchronizedListeners.add(deviceSynchronizeListener);
    }

    @Override
    public void notifyDeviceSynchronizeListeners(final DeviceInfo deviceInfo, final boolean deviceSynchronized) {
        for (DeviceSynchronizeListener listener : deviceSynchronizedListeners) {
            listener.deviceIsSynchronized(deviceInfo, deviceSynchronized);
        }
    }

    @Override
    public void addDeviceValidListener(final DeviceValidListener deviceValidListener) {
        this.deviceValidListeners.add(deviceValidListener);
    }

    @Override
    public void notifyDeviceValidListeners(final DeviceInfo deviceInfo, final boolean deviceValid) {
        for (DeviceValidListener listener : deviceValidListeners) {
            listener.deviceIsValid(deviceInfo, deviceValid);
        }
    }

    @Override
    public void setIsNotificationFlowRemovedOff(boolean isNotificationFlowRemovedOff) {
        this.isNotificationFlowRemovedOff = isNotificationFlowRemovedOff;
    }

    @Override
    public boolean getIsNotificationFlowRemovedOff() {
        return this.isNotificationFlowRemovedOff;
    }

    private ListenableFuture<Void> onDeviceTakeClusterLeadership(final DeviceInfo deviceInfo) {
        LOG.trace("onDeviceTakeClusterLeadership for node: {}", deviceInfo.getNodeId());
        /* validation */
        StatisticsContext statisticsContext = conductor.getStatisticsContext(deviceInfo);
        if (statisticsContext == null) {
            final String errMsg = String.format("DeviceCtx %s is up but we are missing StatisticsContext", deviceInfo.getDatapathId());
            LOG.warn(errMsg);
            return Futures.immediateFailedFuture(new IllegalStateException(errMsg));
        }
        DeviceContext deviceContext = conductor.getDeviceContext(deviceInfo);
        /* Prepare init info collecting */
        notifyDeviceSynchronizeListeners(deviceInfo, false);
        ((DeviceContextImpl)deviceContext).getTransactionChainManager().activateTransactionManager();
        /* Init Collecting NodeInfo */
        final ListenableFuture<Void> initCollectingDeviceInfo = DeviceInitializationUtils.initializeNodeInformation(
                deviceContext, switchFeaturesMandatory, convertorExecutor);
        /* Init Collecting StatInfo */
        final ListenableFuture<Boolean> statPollFuture = Futures.transform(initCollectingDeviceInfo,
                new AsyncFunction<Void, Boolean>() {

                    @Override
                    public ListenableFuture<Boolean> apply(@Nonnull final Void input) throws Exception {
                        statisticsContext.statListForCollectingInitialization();
                        return statisticsContext.initialGatherDynamicData();
                    }
                });

        return Futures.transform(statPollFuture, getInitialDeviceInformation(deviceContext));
    }

    private Function<Boolean, Void> getInitialDeviceInformation(final DeviceContext deviceContext) {
        return input -> {
            if (ConnectionContext.CONNECTION_STATE.RIP.equals(
                    conductor.gainConnectionStateSafely(deviceContext.getDeviceInfo())
            )) {
                final String errMsg =
                        String.format("We lost connection for Device %s, context has to be closed.",
                        deviceContext.getDeviceInfo().getNodeId());
                LOG.warn(errMsg);
                throw new IllegalStateException(errMsg);
            }

            if (input == null || !input) {
                final String errMsg =
                        String.format("Get Initial Device %s information fails",
                        deviceContext.getDeviceInfo().getNodeId());
                LOG.warn(errMsg);
                throw new IllegalStateException(errMsg);
            }
            LOG.debug("Get Initial Device {} information is successful",
                    deviceContext.getDeviceInfo().getNodeId());
            notifyDeviceSynchronizeListeners(deviceContext.getDeviceInfo(), true);
            ((DeviceContextImpl)deviceContext).getTransactionChainManager().initialSubmitWriteTransaction();
            deviceContext.getDeviceState().setStatisticsPollingEnabledProp(true);
            return null;
        };
    }

}
