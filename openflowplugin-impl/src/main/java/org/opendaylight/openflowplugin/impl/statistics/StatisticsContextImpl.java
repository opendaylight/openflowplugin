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
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.Timeout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.rpc.listener.ItemLifecycleListenerImpl;
import org.opendaylight.openflowplugin.impl.services.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringOnTheFlyService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StatisticsContextImpl implements StatisticsContext {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContextImpl.class);
    private static final String CONNECTION_CLOSED = "Connection closed.";

    private final ItemLifecycleListener itemLifeCycleListener;
    private final Collection<RequestContext<?>> requestContexts = new HashSet<>();
    private final DeviceContext deviceContext;
    private final DeviceState devState;
    private final ListenableFuture<Boolean> emptyFuture;
    private final boolean shuttingDownStatisticsPolling;
    private final Object COLLECTION_STAT_TYPE_LOCK = new Object();
    private final SinglePurposeMultipartReplyTranslator multipartReplyTranslator;
    @GuardedBy("COLLECTION_STAT_TYPE_LOCK")
    private List<MultipartType> collectingStatType;

    private StatisticsGatheringService statisticsGatheringService;
    private StatisticsGatheringOnTheFlyService statisticsGatheringOnTheFlyService;
    private Timeout pollTimeout;

    private volatile boolean schedulingEnabled;
    private volatile CONTEXT_STATE contextState;

    StatisticsContextImpl(@CheckForNull final DeviceInfo deviceInfo, final boolean shuttingDownStatisticsPolling, final LifecycleConductor lifecycleConductor, final ConvertorManager convertorManager) {
        this.deviceContext = Preconditions.checkNotNull(lifecycleConductor.getDeviceContext(deviceInfo));
        this.devState = Preconditions.checkNotNull(deviceContext.getDeviceState());
        this.shuttingDownStatisticsPolling = shuttingDownStatisticsPolling;
        multipartReplyTranslator = new SinglePurposeMultipartReplyTranslator(convertorManager);
        emptyFuture = Futures.immediateFuture(false);
        statisticsGatheringService = new StatisticsGatheringService(this, deviceContext);
        statisticsGatheringOnTheFlyService = new StatisticsGatheringOnTheFlyService(this, deviceContext, convertorManager);
        itemLifeCycleListener = new ItemLifecycleListenerImpl(deviceContext);
        statListForCollectingInitialization();
        contextState = CONTEXT_STATE.WORKING;
    }

    @Override
    public void statListForCollectingInitialization() {
        synchronized (COLLECTION_STAT_TYPE_LOCK) {
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
            collectingStatType = ImmutableList.<MultipartType>copyOf(statListForCollecting);
        }
    }


    @Override
    public ListenableFuture<Boolean> initialGatherDynamicData() {
        return gatherDynamicData(true);
    }

    @Override
    public ListenableFuture<Boolean> gatherDynamicData(){
        return gatherDynamicData(false);
    }

    private ListenableFuture<Boolean> gatherDynamicData(final boolean initial) {
        if (shuttingDownStatisticsPolling) {
            LOG.debug("Statistics for device {} is not enabled.", deviceContext.getDeviceInfo().getNodeId());
            return Futures.immediateFuture(Boolean.TRUE);
        }
        final ListenableFuture<Boolean> errorResultFuture = deviceConnectionCheck();
        if (errorResultFuture != null) {
            return errorResultFuture;
        }
        synchronized (COLLECTION_STAT_TYPE_LOCK) {
            final Iterator<MultipartType> statIterator = collectingStatType.iterator();
            final SettableFuture<Boolean> settableStatResultFuture = SettableFuture.create();

            // write start timestamp to state snapshot container
            StatisticsGatheringUtils.markDeviceStateSnapshotStart(deviceContext);

            statChainFuture(statIterator, settableStatResultFuture, initial);

            // write end timestamp to state snapshot container
            Futures.addCallback(settableStatResultFuture, new FutureCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable final Boolean result) {
                    StatisticsGatheringUtils.markDeviceStateSnapshotEnd(deviceContext, true);
                }
                @Override
                public void onFailure(final Throwable t) {
                    StatisticsGatheringUtils.markDeviceStateSnapshotEnd(deviceContext, false);
                }
            });
            return settableStatResultFuture;
        }
    }

    private ListenableFuture<Boolean> chooseStat(final MultipartType multipartType, final boolean initial){
        switch (multipartType) {
            case OFPMPFLOW:
                return collectFlowStatistics(multipartType, initial);
            case OFPMPTABLE:
                return collectTableStatistics(multipartType);
            case OFPMPPORTSTATS:
                return collectPortStatistics(multipartType);
            case OFPMPQUEUE:
                return collectQueueStatistics(multipartType);
            case OFPMPGROUPDESC:
                return collectGroupDescStatistics(multipartType);
            case OFPMPGROUP:
                return collectGroupStatistics(multipartType);
            case OFPMPMETERCONFIG:
                return collectMeterConfigStatistics(multipartType);
            case OFPMPMETER:
                return collectMeterStatistics(multipartType);
            default:
                LOG.warn("Unsuported Statistics type {}", multipartType);
                return Futures.immediateCheckedFuture(Boolean.TRUE);
        }
    }


    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(deviceContext.reserveXidForDeviceMessage()) {
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
        if (CONTEXT_STATE.TERMINATION.equals(contextState)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Statistics context is already in state TERMINATION.");
            }
        } else {
            contextState = CONTEXT_STATE.TERMINATION;
            schedulingEnabled = false;
            for (final Iterator<RequestContext<?>> iterator = Iterators.consumingIterator(requestContexts.iterator());
                 iterator.hasNext(); ) {
                RequestContextUtil.closeRequestContextWithRpcError(iterator.next(), CONNECTION_CLOSED);
            }
            if (null != pollTimeout && !pollTimeout.isExpired()) {
                pollTimeout.cancel();
            }
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

    @Override
    public Optional<Timeout> getPollTimeout() {
        return Optional.ofNullable(pollTimeout);
    }

    private void statChainFuture(final Iterator<MultipartType> iterator, final SettableFuture<Boolean> resultFuture, final boolean initial) {
        if (ConnectionContext.CONNECTION_STATE.RIP.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            final String errMsg = String.format("Device connection is closed for Node : %s.",
                    deviceContext.getDeviceInfo().getNodeId());
            LOG.debug(errMsg);
            resultFuture.setException(new IllegalStateException(errMsg));
            return;
        }
        if ( ! iterator.hasNext()) {
            resultFuture.set(Boolean.TRUE);
            LOG.debug("Stats collection successfully finished for node {}", deviceContext.getDeviceInfo().getNodeId());
            return;
        }

        final MultipartType nextType = iterator.next();
        LOG.debug("Stats iterating to next type for node {} of type {}", deviceContext.getDeviceInfo().getNodeId(), nextType);

        final ListenableFuture<Boolean> deviceStatisticsCollectionFuture = chooseStat(nextType, initial);
        Futures.addCallback(deviceStatisticsCollectionFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                statChainFuture(iterator, resultFuture, initial);
            }
            @Override
            public void onFailure(@Nonnull final Throwable t) {
                resultFuture.setException(t);
            }
        });
    }

    /**
     * Method checks a device state. It returns null for be able continue. Otherwise it returns immediateFuture
     * which has to be returned from caller too
     *
     * @return
     */
    @VisibleForTesting
    ListenableFuture<Boolean> deviceConnectionCheck() {
        if (!ConnectionContext.CONNECTION_STATE.WORKING.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            ListenableFuture<Boolean> resultingFuture;
            switch (deviceContext.getPrimaryConnectionContext().getConnectionState()) {
                case RIP:
                    final String errMsg = String.format("Device connection doesn't exist anymore. Primary connection status : %s",
                            deviceContext.getPrimaryConnectionContext().getConnectionState());
                    resultingFuture = Futures.immediateFailedFuture(new Throwable(errMsg));
                    break;
                default:
                    resultingFuture = Futures.immediateCheckedFuture(Boolean.TRUE);
                    break;
            }
            return resultingFuture;
        }
        return null;
    }

    //TODO: Refactor twice sending deviceContext into gatheringStatistics
    private ListenableFuture<Boolean> collectFlowStatistics(final MultipartType multipartType, final boolean initial) {
        return devState.isFlowStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringOnTheFlyService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPFLOW*/ multipartType,
                deviceContext,
                deviceContext,
                initial, multipartReplyTranslator) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectTableStatistics(final MultipartType multipartType) {
        return devState.isTableStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPTABLE*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectPortStatistics(final MultipartType multipartType) {
        return devState.isPortStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPPORTSTATS*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectQueueStatistics(final MultipartType multipartType) {
        return !devState.isQueueStatisticsAvailable() ? emptyFuture : StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPQUEUE*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator);
    }

    private ListenableFuture<Boolean> collectGroupDescStatistics(final MultipartType multipartType) {
        return devState.isGroupAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPGROUPDESC*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectGroupStatistics(final MultipartType multipartType) {
        return devState.isGroupAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPGROUP*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectMeterConfigStatistics(final MultipartType multipartType) {
        return devState.isMetersAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPMETERCONFIG*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectMeterStatistics(final MultipartType multipartType) {
        return devState.isMetersAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService,
                deviceContext.getDeviceInfo(),
                /*MultipartType.OFPMPMETER*/ multipartType,
                deviceContext,
                deviceContext,
                false, multipartReplyTranslator) : emptyFuture;
    }

    @VisibleForTesting
    void setStatisticsGatheringService(final StatisticsGatheringService statisticsGatheringService) {
        this.statisticsGatheringService = statisticsGatheringService;
    }

    @VisibleForTesting
    void setStatisticsGatheringOnTheFlyService(final StatisticsGatheringOnTheFlyService
                                                             statisticsGatheringOnTheFlyService) {
        this.statisticsGatheringOnTheFlyService = statisticsGatheringOnTheFlyService;
    }

    @Override
    public ItemLifecycleListener getItemLifeCycleListener () {
        return itemLifeCycleListener;
    }

    @Override
    public CONTEXT_STATE getState() {
        return contextState;
    }
}
