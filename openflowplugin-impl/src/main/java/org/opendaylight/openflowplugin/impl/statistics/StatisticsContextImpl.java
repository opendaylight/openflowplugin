/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.common.api.TransactionChainClosedException;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.util.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringOnTheFlyService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsContextImpl<T extends OfHeader> extends AbstractScheduledService implements StatisticsContext {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContextImpl.class);
    private static final String CONNECTION_CLOSED = "Connection closed.";

    private final Collection<RequestContext<?>> requestContexts = new HashSet<>();
    private final DeviceContext deviceContext;
    private final DeviceState devState;
    private final boolean isStatisticsPollingOn;
    private final Object collectionStatTypeLock = new Object();
    private final ConvertorExecutor convertorExecutor;
    private final MultipartWriterProvider statisticsWriterProvider;
    private final DeviceInfo deviceInfo;
    private final boolean isUsingReconciliationFramework;
    private final long statisticsInterval;
    @GuardedBy("collectionStatTypeLock")
    private List<MultipartType> collectingStatType;
    private StatisticsGatheringService<T> statisticsGatheringService;
    private StatisticsGatheringOnTheFlyService<T> statisticsGatheringOnTheFlyService;
    private ContextChainMastershipWatcher contextChainMastershipWatcher;

    private volatile boolean schedulingEnabled;
    private volatile ListenableFuture<Boolean> lastDataGathering;

    StatisticsContextImpl(final boolean isStatisticsPollingOn,
                          @Nonnull final DeviceContext deviceContext,
                          @Nonnull final ConvertorExecutor convertorExecutor,
                          @Nonnull final StatisticsManager myManager,
                          @Nonnull final MultipartWriterProvider statisticsWriterProvider,
                          boolean isUsingReconciliationFramework,
                          long statisticsInterval) {
        this.deviceContext = deviceContext;
        this.devState = Preconditions.checkNotNull(deviceContext.getDeviceState());
        this.isStatisticsPollingOn = isStatisticsPollingOn;
        this.convertorExecutor = convertorExecutor;
        this.deviceInfo = deviceContext.getDeviceInfo();
        this.statisticsWriterProvider = statisticsWriterProvider;
        this.isUsingReconciliationFramework = isUsingReconciliationFramework;
        this.statisticsInterval = statisticsInterval;

        statisticsGatheringService = new StatisticsGatheringService<>(this, deviceContext);
        statisticsGatheringOnTheFlyService = new StatisticsGatheringOnTheFlyService<>(this,
                deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @VisibleForTesting
    ListenableFuture<Boolean> gatherDynamicData() {
        if (!isStatisticsPollingOn) {
            LOG.debug("Statistics for device {} is not enabled.", getDeviceInfo().getNodeId().getValue());
            return Futures.immediateFuture(Boolean.TRUE);
        }

        if (Objects.isNull(lastDataGathering)
                || lastDataGathering.isCancelled()
                || lastDataGathering.isDone()) {
            lastDataGathering = Futures.immediateFuture(Boolean.TRUE);
        }

        synchronized (collectionStatTypeLock) {
            // write start timestamp to state snapshot container
            StatisticsGatheringUtils.markDeviceStateSnapshotStart(deviceContext);

            lastDataGathering = collectingStatType.stream().reduce(
                    lastDataGathering,
                    this::statChainFuture,
                    (a, b) -> Futures.transformAsync(a, result -> b));

            // write end timestamp to state snapshot container
            Futures.addCallback(lastDataGathering, new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(final Boolean result) {
                    StatisticsGatheringUtils.markDeviceStateSnapshotEnd(deviceContext, result);
                }

                @Override
                public void onFailure(final Throwable t) {
                    if (!(t instanceof TransactionChainClosedException)) {
                        StatisticsGatheringUtils.markDeviceStateSnapshotEnd(deviceContext, false);
                    }
                }
            });
        }

        return lastDataGathering;
    }

    private ListenableFuture<Boolean> chooseStat(final MultipartType multipartType){
        switch (multipartType) {
            case OFPMPFLOW:
                return collectStatistics(multipartType, devState.isFlowStatisticsAvailable(), true);
            case OFPMPTABLE:
                return collectStatistics(multipartType, devState.isTableStatisticsAvailable(), false);
            case OFPMPPORTSTATS:
                return collectStatistics(multipartType, devState.isPortStatisticsAvailable(), false);
            case OFPMPQUEUE:
                return collectStatistics(multipartType, devState.isQueueStatisticsAvailable(), false);
            case OFPMPGROUPDESC:
                return collectStatistics(multipartType, devState.isGroupAvailable(), false);
            case OFPMPGROUP:
                return collectStatistics(multipartType, devState.isGroupAvailable(), false);
            case OFPMPMETERCONFIG:
                return collectStatistics(multipartType, devState.isMetersAvailable(), false);
            case OFPMPMETER:
                return collectStatistics(multipartType, devState.isMetersAvailable(), false);
            default:
                LOG.warn("Unsupported Statistics type {}", multipartType);
                return Futures.immediateFuture(Boolean.FALSE);
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(deviceInfo.reserveXidForDeviceMessage()) {
            @Override
            public void close() {
                requestContexts.remove(this);
            }
        };
        requestContexts.add(ret);
        return ret;
    }

    @Override
    public void setSchedulingEnabled(final boolean schedulingEnabled) {
        this.schedulingEnabled = schedulingEnabled;
    }

    private ListenableFuture<Boolean> statChainFuture(final ListenableFuture<Boolean> prevFuture,
                                                      final MultipartType multipartType) {
        if (ConnectionContext.CONNECTION_STATE.RIP.equals(deviceContext.getPrimaryConnectionContext()
                .getConnectionState())) {
            final String errMsg = String
                    .format("Device connection for node %s doesn't exist anymore. Primary connection status : %s",
                            getDeviceInfo().getNodeId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionState());

            return Futures.immediateFailedFuture(new ConnectionException(errMsg));
        }

        return Futures.transformAsync(prevFuture, result -> {
            LOG.debug("Status of previous stat iteration for node {}: {}", deviceInfo, result);
            LOG.debug("Stats iterating to next type for node {} of type {}", deviceInfo, multipartType);

            return chooseStat(multipartType);
        });
    }

    private ListenableFuture<Boolean> collectStatistics(final MultipartType multipartType,
                                                        final boolean supported,
                                                        final boolean onTheFly) {
        // TODO: Refactor twice sending deviceContext into gatheringStatistics
        return supported ? StatisticsGatheringUtils.gatherStatistics(
                onTheFly ? statisticsGatheringOnTheFlyService : statisticsGatheringService,
                getDeviceInfo(),
                multipartType,
                deviceContext,
                deviceContext,
                convertorExecutor,
                statisticsWriterProvider) : Futures.immediateFuture(Boolean.FALSE);
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public void registerMastershipWatcher(@Nonnull final ContextChainMastershipWatcher contextChainMastershipWatcher) {
        this.contextChainMastershipWatcher = contextChainMastershipWatcher;
    }

    @Override
    public boolean initialSubmitAfterReconciliation() {
        return schedulingEnabled = deviceContext.initialSubmitTransaction();
    }

    @Override
    protected void startUp() throws Exception {
        synchronized (collectionStatTypeLock) {
            final List<MultipartType> statListForCollecting = new ArrayList<>();
            if (devState.isTableStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPTABLE);
            }
            if (devState.isFlowStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPFLOW);
            }
            if (devState.isGroupAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPGROUPDESC);
                statListForCollecting.add(MultipartType.OFPMPGROUP);
            }
            if (devState.isMetersAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPMETERCONFIG);
                statListForCollecting.add(MultipartType.OFPMPMETER);
            }
            if (devState.isPortStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPPORTSTATS);
            }
            if (devState.isQueueStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPQUEUE);
            }
            collectingStatType = ImmutableList.copyOf(statListForCollecting);
        }

        gatherDynamicData().get();
        contextChainMastershipWatcher.onMasterRoleAcquired(deviceInfo, ContextChainMastershipState.INITIAL_GATHERING);

        if (!isUsingReconciliationFramework) {
            if (deviceContext.initialSubmitTransaction()) {
                contextChainMastershipWatcher.onMasterRoleAcquired(deviceInfo,
                        ContextChainMastershipState.INITIAL_SUBMIT);

                if (isStatisticsPollingOn) {
                    schedulingEnabled = true;
                }
            } else {
                contextChainMastershipWatcher.onNotAbleToStartMastershipMandatory(deviceInfo,
                        "Initial transaction cannot be submitted.");
            }
        }
    }

    @Override
    protected void shutDown() throws Exception {
        if (Objects.nonNull(lastDataGathering) && !lastDataGathering.isDone() && !lastDataGathering.isCancelled()) {
            lastDataGathering.cancel(true);
        }

        requestContexts.forEach(requestContext -> RequestContextUtil
                .closeRequestContextWithRpcError(requestContext, CONNECTION_CLOSED));
        requestContexts.clear();
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (!schedulingEnabled) {
            LOG.debug("Statistics scheduling for device {} is disabled", deviceInfo);
            return;
        }

        LOG.debug("POLLING ALL STATISTICS for device: {}", deviceInfo);
        gatherDynamicData().get();
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    @VisibleForTesting
    void setStatisticsGatheringService(final StatisticsGatheringService<T> statisticsGatheringService) {
        this.statisticsGatheringService = statisticsGatheringService;
    }

    @VisibleForTesting
    void setStatisticsGatheringOnTheFlyService(final StatisticsGatheringOnTheFlyService<T> statisticsGatheringOnTheFlyService) {
        this.statisticsGatheringOnTheFlyService = statisticsGatheringOnTheFlyService;
    }

}