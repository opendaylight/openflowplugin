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
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.ConnectionException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManagerImpl implements StatisticsManager, StatisticsManagerControlService {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    private static final long DEFAULT_STATS_TIMEOUT_SEC = 50L;
    private final ConvertorExecutor converterExecutor;

    private final ConcurrentMap<DeviceInfo, StatisticsContext> contexts = new ConcurrentHashMap<>();

    private long basicTimerDelay;
    private long currentTimerDelay;
    private long maximumTimerDelay; //wait time for next statistics

    private StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;
    private final Semaphore workModeGuard = new Semaphore(1, true);
    private boolean isStatisticsPollingOn;
    private BindingAwareBroker.RpcRegistration<StatisticsManagerControlService> controlServiceRegistration;

    private final HashedWheelTimer hashedWheelTimer;

    public StatisticsManagerImpl(@Nonnull final RpcProviderRegistry rpcProviderRegistry,
                                 final HashedWheelTimer hashedWheelTimer,
                                 final ConvertorExecutor convertorExecutor) {
        this.converterExecutor = convertorExecutor;
        this.controlServiceRegistration = Preconditions.checkNotNull(rpcProviderRegistry
                .addRpcImplementation(StatisticsManagerControlService.class, this));

        this.hashedWheelTimer = hashedWheelTimer;
    }

    @VisibleForTesting
    void pollStatistics(final DeviceState deviceState,
                        final StatisticsContext statisticsContext,
                        final TimeCounter timeCounter,
                        final DeviceInfo deviceInfo) {

        if (!statisticsContext.isSchedulingEnabled()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Disabled statistics scheduling for device: {}", deviceInfo.getNodeId().getValue());
            }
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("POLLING ALL STATISTICS for device: {}", deviceInfo.getNodeId());
        }

        timeCounter.markStart();
        final ListenableFuture<Boolean> deviceStatisticsCollectionFuture = statisticsContext.gatherDynamicData();
        Futures.addCallback(deviceStatisticsCollectionFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean o) {
                timeCounter.addTimeMark();
                calculateTimerDelay(timeCounter);
                scheduleNextPolling(deviceState, deviceInfo, statisticsContext, timeCounter);
            }

            @Override
            public void onFailure(@Nonnull final Throwable throwable) {
                timeCounter.addTimeMark();
                calculateTimerDelay(timeCounter);

                if (throwable instanceof ConnectionException) {
                    // ConnectionException is raised by StatisticsContextImpl class when the connections
                    // move to RIP state. In this particular case, there is no need to reschedule
                    // because this statistics manager should be closed soon
                    LOG.warn("Device {} is no more connected, stopping the statistics collection. Reason: {}",
                            deviceInfo, throwable.getMessage());
                    stopScheduling(deviceInfo);
                } else if (throwable instanceof CancellationException) {
                    LOG.info("Statistics gathering for device {} was cancelled.", deviceInfo);
                } else {
                    LOG.warn("Unexpected error occurred during statistics collection for device {}, rescheduling " +
                            "statistics collections", deviceInfo, throwable);

                    scheduleNextPolling(deviceState, deviceInfo, statisticsContext, timeCounter);
                }
            }
        });

        final long averageTime = TimeUnit.MILLISECONDS.toSeconds(timeCounter.getAverageTimeBetweenMarks());
        final long statsTimeoutSec = averageTime > 0 ? 3 * averageTime : DEFAULT_STATS_TIMEOUT_SEC;
        final TimerTask timerTask = timeout -> {
            if (!deviceStatisticsCollectionFuture.isDone()) {
                LOG.info("Statistics collection for node {} still in progress even after {} secs", deviceInfo.getLOGValue(), statsTimeoutSec);
                deviceStatisticsCollectionFuture.cancel(true);
            }
        };

        hashedWheelTimer.newTimeout(timerTask, statsTimeoutSec, TimeUnit.SECONDS);
    }

    private void scheduleNextPolling(final DeviceState deviceState,
                                     final DeviceInfo deviceInfo,
                                     final StatisticsContext statisticsContext,
                                     final TimeCounter timeCounter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SCHEDULING NEXT STATISTICS POLLING for device: {}", deviceInfo.getNodeId());
        }

        if (isStatisticsPollingOn) {
            final Timeout pollTimeout = hashedWheelTimer.newTimeout(
                    timeout -> pollStatistics(
                            deviceState,
                            statisticsContext,
                            timeCounter,
                            deviceInfo),
                    currentTimerDelay,
                    TimeUnit.MILLISECONDS);

            statisticsContext.setPollTimeout(pollTimeout);
        }
    }

    @VisibleForTesting
    void calculateTimerDelay(final TimeCounter timeCounter) {
        final long averageStatisticsGatheringTime = timeCounter.getAverageTimeBetweenMarks();
        if (averageStatisticsGatheringTime > currentTimerDelay) {
            currentTimerDelay *= 2;
            if (currentTimerDelay > maximumTimerDelay) {
                currentTimerDelay = maximumTimerDelay;
            }
        } else {
            if (currentTimerDelay > basicTimerDelay) {
                currentTimerDelay /= 2;
            } else {
                currentTimerDelay = basicTimerDelay;
            }
        }
    }

    @VisibleForTesting
    long getCurrentTimerDelay() {
        return currentTimerDelay;
    }

    @Override
    public Future<RpcResult<GetStatisticsWorkModeOutput>> getStatisticsWorkMode() {
        final GetStatisticsWorkModeOutputBuilder smModeOutputBld = new GetStatisticsWorkModeOutputBuilder();
        smModeOutputBld.setMode(workMode);
        return RpcResultBuilder.success(smModeOutputBld.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<Void>> changeStatisticsWorkMode(ChangeStatisticsWorkModeInput input) {
        final Future<RpcResult<Void>> result;
        // acquire exclusive access
        if (workModeGuard.tryAcquire()) {
            final StatisticsWorkMode targetWorkMode = input.getMode();
            if (!workMode.equals(targetWorkMode)) {
                isStatisticsPollingOn = !(StatisticsWorkMode.FULLYDISABLED.equals(targetWorkMode));
                // iterate through stats-ctx: propagate mode
                for (Map.Entry<DeviceInfo, StatisticsContext> entry : contexts.entrySet()) {
                    final DeviceInfo deviceInfo = entry.getKey();
                    final StatisticsContext statisticsContext = entry.getValue();
                    final DeviceContext deviceContext = statisticsContext.gainDeviceContext();
                    switch (targetWorkMode) {
                        case COLLECTALL:
                            scheduleNextPolling(statisticsContext.gainDeviceState(), deviceInfo, statisticsContext, new TimeCounter());
                            break;
                        case FULLYDISABLED:
                            statisticsContext.stopGatheringData();
                            break;
                        default:
                            LOG.warn("Statistics work mode not supported: {}", targetWorkMode);
                    }
                }
                workMode = targetWorkMode;
            }
            workModeGuard.release();
            result = RpcResultBuilder.<Void>success().buildFuture();
        } else {
            result = RpcResultBuilder.<Void>failed()
                    .withError(RpcError.ErrorType.APPLICATION, "mode change already in progress")
                    .buildFuture();
        }
        return result;
    }

    @Override
    public void startScheduling(final DeviceInfo deviceInfo) {
        if (!isStatisticsPollingOn) {
            LOG.info("Statistics are shutdown for device: {}", deviceInfo.getNodeId());
            return;
        }

        final StatisticsContext statisticsContext = contexts.get(deviceInfo);

        if (statisticsContext == null) {
            LOG.warn("Statistics context not found for device: {}", deviceInfo.getNodeId());
            return;
        }

        if (statisticsContext.isSchedulingEnabled()) {
            LOG.debug("Statistics scheduling is already enabled for device: {}", deviceInfo.getNodeId());
            return;
        }

        LOG.info("Scheduling statistics poll for device: {}", deviceInfo.getNodeId());

        statisticsContext.setSchedulingEnabled(true);
        scheduleNextPolling(
                statisticsContext.gainDeviceState(),
                deviceInfo,
                statisticsContext,
                new TimeCounter()
        );
    }

    @Override
    public void stopScheduling(final DeviceInfo deviceInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stopping statistics scheduling for device: {}", deviceInfo.getNodeId());
        }

        final StatisticsContext statisticsContext = contexts.get(deviceInfo);

        if (statisticsContext == null) {
            LOG.warn("Statistics context not found for device: {}", deviceInfo.getNodeId());
            return;
        }

        statisticsContext.setSchedulingEnabled(false);
    }

    @Override
    public void close() {
        if (controlServiceRegistration != null) {
            controlServiceRegistration.close();
            controlServiceRegistration = null;
        }

        for (final Iterator<StatisticsContext> iterator = Iterators.consumingIterator(contexts.values().iterator());
                iterator.hasNext();) {
            iterator.next().close();
        }
    }

    @Override
    public void setIsStatisticsPollingOn(boolean isStatisticsPollingOn){
        this.isStatisticsPollingOn = isStatisticsPollingOn;
    }

    @Override
    public StatisticsContext createContext(@Nonnull final DeviceContext deviceContext) {

        final MultipartWriterProvider statisticsWriterProvider = MultipartWriterProviderFactory
            .createDefaultProvider(deviceContext);

        final StatisticsContext statisticsContext =
            deviceContext.canUseSingleLayerSerialization() ?
            new StatisticsContextImpl<MultipartReply>(
                isStatisticsPollingOn,
                deviceContext,
                converterExecutor,
                    this,
                statisticsWriterProvider) :
            new StatisticsContextImpl<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731
                .MultipartReply>(
                isStatisticsPollingOn,
                deviceContext,
                converterExecutor,
                    this,
                statisticsWriterProvider);
        contexts.putIfAbsent(deviceContext.getDeviceInfo(), statisticsContext);

        return statisticsContext;
    }

    @Override
    public void onDeviceRemoved(final DeviceInfo deviceInfo) {
        contexts.remove(deviceInfo);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Statistics context removed for node {}", deviceInfo.getLOGValue());
        }
    }

    @Override
    public void setBasicTimerDelay(final long basicTimerDelay) {
        this.basicTimerDelay = basicTimerDelay;
        this.currentTimerDelay = basicTimerDelay;
    }

    @Override
    public void setMaximumTimerDelay(final long maximumTimerDelay) {
        this.maximumTimerDelay = maximumTimerDelay;
    }
}
