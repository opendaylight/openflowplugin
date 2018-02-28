/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.util.Timeout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.common.api.TransactionChainClosedException;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
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

class StatisticsContextImpl<T extends OfHeader> implements StatisticsContext {

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
    private final StatisticsManager myManager;
    @GuardedBy("collectionStatTypeLock")
    private List<MultipartType> collectingStatType;
    private StatisticsGatheringService<T> statisticsGatheringService;
    private StatisticsGatheringOnTheFlyService<T> statisticsGatheringOnTheFlyService;
    private Timeout pollTimeout;
    private MastershipChangeListener mastershipChangeListener;

    private volatile boolean schedulingEnabled;
    private volatile CONTEXT_STATE state;
    private volatile ListenableFuture<Boolean> lastDataGathering;

    StatisticsContextImpl(final boolean isStatisticsPollingOn,
                          @Nonnull final DeviceContext deviceContext,
                          @Nonnull final ConvertorExecutor convertorExecutor,
                          @Nonnull final StatisticsManager myManager,
                          @Nonnull final MultipartWriterProvider statisticsWriterProvider) {
        this.deviceContext = deviceContext;
        this.devState = Preconditions.checkNotNull(deviceContext.getDeviceState());
        this.isStatisticsPollingOn = isStatisticsPollingOn;
        this.convertorExecutor = convertorExecutor;
        statisticsGatheringService = new StatisticsGatheringService<>(this, deviceContext);
        statisticsGatheringOnTheFlyService = new StatisticsGatheringOnTheFlyService<>(this,
            deviceContext, convertorExecutor, statisticsWriterProvider);
        statListForCollectingInitialization();
        this.state = CONTEXT_STATE.INITIALIZATION;
        this.deviceInfo = deviceContext.getDeviceInfo();
        this.myManager = myManager;
        this.lastDataGathering = null;
        this.statisticsWriterProvider = statisticsWriterProvider;
    }

    @Override
    public void statListForCollectingInitialization() {
        synchronized (collectionStatTypeLock) {
            final List<MultipartType> statListForCollecting = new ArrayList<>();
            if (devState.isTableStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPTABLE);
            }
            if (devState.isGroupAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPGROUPDESC);
                statListForCollecting.add(MultipartType.OFPMPGROUP);
            }
            if (devState.isMetersAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPMETERCONFIG);
                statListForCollecting.add(MultipartType.OFPMPMETER);
            }
            if (devState.isFlowStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPFLOW);
            }
            if (devState.isPortStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPPORTSTATS);
            }
            if (devState.isQueueStatisticsAvailable()) {
                statListForCollecting.add(MultipartType.OFPMPQUEUE);
            }
            collectingStatType = ImmutableList.copyOf(statListForCollecting);
        }
    }

    @Override
    public ListenableFuture<Boolean> gatherDynamicData() {
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
                    (future, multipartType) -> statChainFuture(future, multipartType),
                    (a, b) -> Futures.transform(a, (AsyncFunction<Boolean, Boolean>) result -> b));

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
        ListenableFuture<Boolean> result = Futures.immediateCheckedFuture(Boolean.TRUE);

        switch (multipartType) {
            case OFPMPFLOW:
                result = collectStatistics(multipartType, devState.isFlowStatisticsAvailable(), true);
                break;
            case OFPMPTABLE:
                result = collectStatistics(multipartType, devState.isTableStatisticsAvailable(), false);
                break;
            case OFPMPPORTSTATS:
                result = collectStatistics(multipartType, devState.isPortStatisticsAvailable(), false);
                break;
            case OFPMPQUEUE:
                result = collectStatistics(multipartType, devState.isQueueStatisticsAvailable(), false);
                break;
            case OFPMPGROUPDESC:
                result = collectStatistics(multipartType, devState.isGroupAvailable(), false);
                break;
            case OFPMPGROUP:
                result = collectStatistics(multipartType, devState.isGroupAvailable(), false);
                break;
            case OFPMPMETERCONFIG:
                result = collectStatistics(multipartType, devState.isMetersAvailable(), false);
                break;
            case OFPMPMETER:
                result = collectStatistics(multipartType, devState.isMetersAvailable(), false);
                break;
            default:
                LOG.warn("Unsupported Statistics type {}", multipartType);
        }

        return result;
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
    public void close() {
        if (CONTEXT_STATE.TERMINATION.equals(state)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("StatisticsContext for node {} is already in TERMINATION state.", getDeviceInfo().getLOGValue());
            }
        } else {
            this.state = CONTEXT_STATE.TERMINATION;
            stopGatheringData();
            requestContexts.forEach(requestContext -> RequestContextUtil
                    .closeRequestContextWithRpcError(requestContext, CONNECTION_CLOSED));
            requestContexts.clear();
        }
    }

    @Override
    public void setSchedulingEnabled(final boolean schedulingEnabled) {
        this.schedulingEnabled = schedulingEnabled;
    }

    @Override
    public boolean isSchedulingEnabled() {
        return schedulingEnabled;
    }

    @Override
    public void setPollTimeout(final Timeout pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    private ListenableFuture<Boolean> statChainFuture(final ListenableFuture<Boolean> prevFuture, final MultipartType multipartType) {
        return Futures.transform(deviceConnectionCheck(), (AsyncFunction<Boolean, Boolean>) connectionResult -> Futures
                .transform(prevFuture, (AsyncFunction<Boolean, Boolean>) result -> {
                    LOG.debug("Status of previous stat iteration for node {}: {}", deviceInfo.getLOGValue(), result);
                    LOG.debug("Stats iterating to next type for node {} of type {}",
                            deviceInfo.getLOGValue(),
                            multipartType);

                    return chooseStat(multipartType);
                }));
    }

    @VisibleForTesting
    ListenableFuture<Boolean> deviceConnectionCheck() {
        if (ConnectionContext.CONNECTION_STATE.RIP.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            final String errMsg = String
                    .format("Device connection for node %s doesn't exist anymore. Primary connection status : %s",
                            getDeviceInfo().getNodeId(),
                            deviceContext.getPrimaryConnectionContext().getConnectionState());

            return Futures.immediateFailedFuture(new ConnectionException(errMsg));
        }

        return Futures.immediateFuture(Boolean.TRUE);
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

    @VisibleForTesting
    void setStatisticsGatheringService(final StatisticsGatheringService<T> statisticsGatheringService) {
        this.statisticsGatheringService = statisticsGatheringService;
    }

    @VisibleForTesting
    void setStatisticsGatheringOnTheFlyService(final StatisticsGatheringOnTheFlyService<T> statisticsGatheringOnTheFlyService) {
        this.statisticsGatheringOnTheFlyService = statisticsGatheringOnTheFlyService;
    }


    @Override
    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public void registerMastershipChangeListener(@Nonnull final MastershipChangeListener mastershipChangeListener) {
        this.mastershipChangeListener = mastershipChangeListener;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("Stopping statistics context cluster services for node {}", deviceInfo.getLOGValue());

        return Futures.transform(Futures.immediateFuture(null), new Function<Void, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable final Void input) {
                schedulingEnabled = false;
                stopGatheringData();
                return null;
            }
        }, MoreExecutors.directExecutor());
    }

    @Override
    public DeviceState gainDeviceState() {
        return gainDeviceContext().getDeviceState();
    }

    @Override
    public DeviceContext gainDeviceContext() {
        return this.deviceContext;
    }

    @Override
    public void stopGatheringData() {
        LOG.info("Stopping running statistics gathering for node {}", deviceInfo.getLOGValue());

        if (Objects.nonNull(lastDataGathering) && !lastDataGathering.isDone() && !lastDataGathering.isCancelled()) {
            lastDataGathering.cancel(true);
        }

        if (Objects.nonNull(pollTimeout) && !pollTimeout.isExpired()) {
            pollTimeout.cancel();
        }
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting statistics context cluster services for node {}", deviceInfo.getLOGValue());
        this.statListForCollectingInitialization();

        Futures.addCallback(this.gatherDynamicData(), new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(@Nullable Boolean aBoolean) {
                mastershipChangeListener.onMasterRoleAcquired(
                        deviceInfo,
                        ContextChainMastershipState.INITIAL_GATHERING
                );

                if (deviceContext.initialSubmitTransaction()) {
                    mastershipChangeListener.onMasterRoleAcquired(
                            deviceInfo,
                            ContextChainMastershipState.INITIAL_SUBMIT
                    );

                    if (isStatisticsPollingOn) {
                        myManager.startScheduling(deviceInfo);
                    }
                } else {
                    mastershipChangeListener.onNotAbleToStartMastershipMandatory(
                            deviceInfo,
                            "Initial transaction cannot be submitted."
                    );
                }
            }

            @Override
            public void onFailure(@Nonnull Throwable throwable) {
                mastershipChangeListener.onNotAbleToStartMastershipMandatory(
                        deviceInfo,
                        "Initial gathering statistics unsuccessful."
                );
            }
        });
    }

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }
}
