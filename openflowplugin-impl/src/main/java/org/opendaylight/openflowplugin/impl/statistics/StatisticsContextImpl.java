/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.TransactionChainClosedException;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.DeviceInitializationContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringOnTheFlyService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StatisticsContextImpl<T extends OfHeader> implements StatisticsContext, DeviceInitializationContext {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContextImpl.class);
    private static final String CONNECTION_CLOSED = "Connection closed.";

    private final Collection<RequestContext<?>> requestContexts = ConcurrentHashMap.newKeySet();
    private final DeviceContext deviceContext;
    private final DeviceState devState;
    private final Executor executorService;
    private final boolean isStatisticsPollingOn;
    private final ConvertorExecutor convertorExecutor;
    private final MultipartWriterProvider statisticsWriterProvider;
    private final DeviceInfo deviceInfo;
    private final TimeCounter timeCounter = new TimeCounter();
    private final OpenflowProviderConfig config;
    private final long statisticsPollingInterval;
    private final long maximumPollingDelay;
    private final boolean isUsingReconciliationFramework;
    private final AtomicBoolean schedulingEnabled = new AtomicBoolean(true);
    private final AtomicReference<ListenableFuture<Boolean>> lastDataGatheringRef = new AtomicReference<>();
    private final AtomicReference<StatisticsPollingService> statisticsPollingServiceRef = new AtomicReference<>();
    private List<MultipartType> collectingStatType;
    private StatisticsGatheringService<T> statisticsGatheringService;
    private StatisticsGatheringOnTheFlyService<T> statisticsGatheringOnTheFlyService;
    private ContextChainMastershipWatcher contextChainMastershipWatcher;

    StatisticsContextImpl(@NonNull final DeviceContext deviceContext,
                          @NonNull final ConvertorExecutor convertorExecutor,
                          @NonNull final MultipartWriterProvider statisticsWriterProvider,
                          @NonNull final Executor executorService,
                          @NonNull final OpenflowProviderConfig config,
                          final boolean isStatisticsPollingOn,
                          final boolean isUsingReconciliationFramework) {
        this.deviceContext = deviceContext;
        this.devState = requireNonNull(deviceContext.getDeviceState());
        this.executorService = executorService;
        this.isStatisticsPollingOn = isStatisticsPollingOn;
        this.config = config;
        this.convertorExecutor = convertorExecutor;
        this.deviceInfo = deviceContext.getDeviceInfo();
        this.statisticsPollingInterval = config.getBasicTimerDelay().getValue().toJava();
        this.maximumPollingDelay = config.getMaximumTimerDelay().getValue().toJava();
        this.statisticsWriterProvider = statisticsWriterProvider;
        this.isUsingReconciliationFramework = isUsingReconciliationFramework;

        statisticsGatheringService = new StatisticsGatheringService<>(this, deviceContext);
        statisticsGatheringOnTheFlyService = new StatisticsGatheringOnTheFlyService<>(this, deviceContext,
                                                                                      convertorExecutor,
                                                                                      statisticsWriterProvider);
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @NonNull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void registerMastershipWatcher(@NonNull final ContextChainMastershipWatcher newWatcher) {
        this.contextChainMastershipWatcher = newWatcher;
    }

    @Override
    public <O> RequestContext<O> createRequestContext() {
        final AbstractRequestContext<O> ret = new AbstractRequestContext<>(deviceInfo.reserveXidForDeviceMessage()) {
            @Override
            public void close() {
                requestContexts.remove(this);
            }
        };

        requestContexts.add(ret);
        return ret;
    }

    @Override
    public void enableGathering() {
        this.schedulingEnabled.set(true);
    }

    @Override
    public void disableGathering() {
        this.schedulingEnabled.set(false);
    }

    @Override
    public void continueInitializationAfterReconciliation() {
        if (deviceContext.initialSubmitTransaction()) {
            contextChainMastershipWatcher.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_SUBMIT);

            startGatheringData();
        } else {
            contextChainMastershipWatcher
                    .onNotAbleToStartMastershipMandatory(deviceInfo, "Initial transaction cannot be submitted.");
        }
    }

    @Override
    public void instantiateServiceInstance() {

    }

    @Override
    public void initializeDevice() {
        final List<MultipartType> statListForCollecting = new ArrayList<>();

        if (devState.isGroupAvailable() && config.getIsGroupStatisticsPollingOn()) {
            statListForCollecting.add(MultipartType.OFPMPGROUPDESC);
            statListForCollecting.add(MultipartType.OFPMPGROUP);
        }

        if (devState.isMetersAvailable() && config.getIsMeterStatisticsPollingOn()) {
            statListForCollecting.add(MultipartType.OFPMPMETERCONFIG);
            statListForCollecting.add(MultipartType.OFPMPMETER);
        }

        if (devState.isFlowStatisticsAvailable() && config.getIsFlowStatisticsPollingOn()) {
            statListForCollecting.add(MultipartType.OFPMPFLOW);
        }

        if (devState.isTableStatisticsAvailable() && config.getIsTableStatisticsPollingOn()) {
            statListForCollecting.add(MultipartType.OFPMPTABLE);
        }

        if (devState.isPortStatisticsAvailable() && config.getIsPortStatisticsPollingOn()) {
            statListForCollecting.add(MultipartType.OFPMPPORTSTATS);
        }

        if (devState.isQueueStatisticsAvailable() && config.getIsQueueStatisticsPollingOn()) {
            statListForCollecting.add(MultipartType.OFPMPQUEUE);
        }

        collectingStatType = ImmutableList.copyOf(statListForCollecting);
        Futures.addCallback(gatherDynamicData(), new InitialSubmitCallback(), MoreExecutors.directExecutor());
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return stopGatheringData();
    }

    @Override
    public void close() {
        Futures.addCallback(stopGatheringData(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                requestContexts.forEach(requestContext -> RequestContextUtil
                        .closeRequestContextWithRpcError(requestContext, CONNECTION_CLOSED));
            }

            @Override
            public void onFailure(final Throwable throwable) {
                requestContexts.forEach(requestContext -> RequestContextUtil
                        .closeRequestContextWithRpcError(requestContext, CONNECTION_CLOSED));
            }
        }, MoreExecutors.directExecutor());
    }

    private ListenableFuture<Boolean> gatherDynamicData() {
        if (!isStatisticsPollingOn || !schedulingEnabled.get()) {
            LOG.debug("Statistics for device {} are not enabled.", getDeviceInfo().getNodeId().getValue());
            return Futures.immediateFuture(Boolean.TRUE);
        }

        return this.lastDataGatheringRef.updateAndGet(future -> {
            // write start timestamp to state snapshot container
            StatisticsGatheringUtils.markDeviceStateSnapshotStart(deviceInfo, deviceContext);

            // recreate gathering future if it should be recreated
            final ListenableFuture<Boolean> lastDataGathering = future == null || future.isCancelled()
                    || future.isDone() ? Futures.immediateFuture(Boolean.TRUE) : future;

            // build statistics gathering future
            final ListenableFuture<Boolean> newDataGathering = collectingStatType.stream()
                    .reduce(lastDataGathering, this::statChainFuture,
                        (listenableFuture, asyn) -> Futures.transformAsync(listenableFuture, result -> asyn,
                                MoreExecutors.directExecutor()));

            // write end timestamp to state snapshot container
            Futures.addCallback(newDataGathering, new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(final Boolean result) {
                    StatisticsGatheringUtils.markDeviceStateSnapshotEnd(deviceInfo, deviceContext, result);
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    if (!(throwable instanceof TransactionChainClosedException)) {
                        StatisticsGatheringUtils.markDeviceStateSnapshotEnd(deviceInfo, deviceContext, false);
                    }
                }
            }, MoreExecutors.directExecutor());

            return newDataGathering;
        });
    }

    private ListenableFuture<Boolean> statChainFuture(final ListenableFuture<Boolean> prevFuture,
                                                      final MultipartType multipartType) {
        if (ConnectionContext.CONNECTION_STATE.RIP
                .equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            final String errMsg = String
                    .format("Device connection for node %s doesn't exist anymore. Primary connection status : %s",
                            getDeviceInfo().getNodeId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionState());

            return Futures.immediateFailedFuture(new ConnectionException(errMsg));
        }

        return Futures.transformAsync(prevFuture, result -> {
            LOG.debug("Status of previous stat iteration for node {}: {}", deviceInfo, result);
            LOG.debug("Stats iterating to next type for node {} of type {}", deviceInfo, multipartType);
            final boolean onTheFly = MultipartType.OFPMPFLOW.equals(multipartType);
            final boolean supported = collectingStatType.contains(multipartType);

            // TODO: Refactor twice sending deviceContext into gatheringStatistics
            return supported ? StatisticsGatheringUtils
                    .gatherStatistics(onTheFly ? statisticsGatheringOnTheFlyService : statisticsGatheringService,
                                      getDeviceInfo(), multipartType, deviceContext, deviceContext, convertorExecutor,
                                      statisticsWriterProvider, executorService) : Futures
                    .immediateFuture(Boolean.FALSE);
        }, MoreExecutors.directExecutor());
    }

    private void startGatheringData() {
        if (!isStatisticsPollingOn) {
            return;
        }

        LOG.info("Starting statistics gathering for node {}", deviceInfo);
        final StatisticsPollingService statisticsPollingService =
                new StatisticsPollingService(timeCounter,
                                             statisticsPollingInterval,
                                             maximumPollingDelay,
                                             StatisticsContextImpl.this::gatherDynamicData);

        schedulingEnabled.set(true);
        statisticsPollingService.startAsync();
        this.statisticsPollingServiceRef.set(statisticsPollingService);
    }

    private ListenableFuture<Void> stopGatheringData() {
        LOG.info("Stopping running statistics gathering for node {}", deviceInfo);
        cancelLastDataGathering();

        return Optional.ofNullable(statisticsPollingServiceRef.getAndSet(null)).map(StatisticsPollingService::stop)
                .orElseGet(() -> Futures.immediateFuture(null));
    }

    private void cancelLastDataGathering() {
        final ListenableFuture<Boolean> future = lastDataGatheringRef.getAndSet(null);

        if (future != null && !future.isDone() && !future.isCancelled()) {
            future.cancel(true);
        }
    }

    @VisibleForTesting
    void setStatisticsGatheringService(final StatisticsGatheringService<T> statisticsGatheringService) {
        this.statisticsGatheringService = statisticsGatheringService;
    }

    @VisibleForTesting
    void setStatisticsGatheringOnTheFlyService(
            final StatisticsGatheringOnTheFlyService<T> statisticsGatheringOnTheFlyService) {
        this.statisticsGatheringOnTheFlyService = statisticsGatheringOnTheFlyService;
    }

    private final class InitialSubmitCallback implements FutureCallback<Boolean> {
        @Override
        public void onSuccess(final Boolean result) {
            if (!isUsingReconciliationFramework) {
                continueInitializationAfterReconciliation();
            }
        }

        @Override
        public void onFailure(final Throwable throwable) {
            contextChainMastershipWatcher.onNotAbleToStartMastershipMandatory(deviceInfo,
                                                                              "Initial gathering statistics "
                                                                                      + "unsuccessful: "
                                                                                      + throwable.getMessage());
        }
    }
}
