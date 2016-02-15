/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.ChangeStatisticsWorkModeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.GetStatisticsWorkModeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsManagerControlService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.sm.control.rev150812.StatisticsWorkMode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsManagerImpl implements StatisticsManager, StatisticsManagerControlService {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);
    private final RpcProviderRegistry rpcProviderRegistry;

    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;

    private HashedWheelTimer hashedWheelTimer;

    private final ConcurrentHashMap<DeviceContext, StatisticsContext> contexts = new ConcurrentHashMap<>();

    private static final long basicTimerDelay = 3000;
    private static long currentTimerDelay = basicTimerDelay;
    private static long maximumTimerDelay = 900000; //wait max 15 minutes for next statistics

    private StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;
    private final Semaphore workModeGuard = new Semaphore(1, true);
    private boolean shuttingDownStatisticsPolling;
    private BindingAwareBroker.RpcRegistration<StatisticsManagerControlService> controlServiceRegistration;

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    public StatisticsManagerImpl(final RpcProviderRegistry rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        controlServiceRegistration = rpcProviderRegistry.addRpcImplementation(StatisticsManagerControlService.class, this);
    }

    public StatisticsManagerImpl(final RpcProviderRegistry rpcProviderRegistry, final boolean shuttingDownStatisticsPolling) {
        this(rpcProviderRegistry);
        this.shuttingDownStatisticsPolling = shuttingDownStatisticsPolling;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) throws Exception {
        LOG.debug("Node:{}, deviceContext.getDeviceState().getRole():{}", deviceContext.getDeviceState().getNodeId(),
                deviceContext.getDeviceState().getRole());
        if (null == hashedWheelTimer) {
            LOG.trace("This is first device that delivered timer. Starting statistics polling immediately.");
            hashedWheelTimer = deviceContext.getTimer();
        }

        final StatisticsContext statisticsContext = new StatisticsContextImpl(deviceContext);
        deviceContext.addDeviceContextClosedHandler(this);

        if (!OfpRole.BECOMEMASTER.equals(deviceContext.getDeviceState().getRole())) {
            LOG.info("Schedule Statistics poll for no Master role for node:{}", deviceContext.getDeviceState()
                    .getNodeId());
            scheduleNextPolling(deviceContext, statisticsContext, new TimeCounter());
            deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
            return;
        }

        LOG.info("Starting Statistics for master role for node:{}", deviceContext.getDeviceState().getNodeId());
        final ListenableFuture<Boolean> weHaveDynamicData = statisticsContext.gatherDynamicData();
        Futures.addCallback(weHaveDynamicData, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean statisticsGathered) {
                if (statisticsGathered) {
                    //there are some statistics on device worth gathering
                    contexts.put(deviceContext, statisticsContext);
                    final TimeCounter timeCounter = new TimeCounter();
                    scheduleNextPolling(deviceContext, statisticsContext, timeCounter);
                    LOG.trace("Device dynamic info collecting done. Going to announce raise to next level.");
                    try {
                        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
                    } catch (final Exception e) {
                        LOG.info("failed to complete levelUp on next handler for device {}", deviceContext.getDeviceState().getNodeId());
                        deviceContext.close();
                        return;
                    }
                    deviceContext.getDeviceState().setDeviceSynchronized(true);
                } else {
                    final String deviceAdress = deviceContext.getPrimaryConnectionContext().getConnectionAdapter().getRemoteAddress().toString();
                    try {
                        deviceContext.close();
                    } catch (final Exception e) {
                        LOG.info("Statistics for device {} could not be gathered. Closing its device context.", deviceAdress);
                    }
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.warn("Statistics manager was not able to collect dynamic info for device.", deviceContext.getDeviceState().getNodeId(), throwable);
                try {
                    deviceContext.close();
                } catch (final Exception e) {
                    LOG.warn("Error closing device context.", e);
                }
            }
        });
    }

    private void pollStatistics(final DeviceContext deviceContext,
                                final StatisticsContext statisticsContext,
                                final TimeCounter timeCounter) {

        if (!deviceContext.getDeviceState().isValid()) {
            LOG.debug("Session for device {} is not valid.", deviceContext.getDeviceState().getNodeId().getValue());
            return;
        }
        if (!deviceContext.getDeviceState().isStatisticsPollingEnabled()) {
            LOG.debug("StatisticsPolling is disabled for device: {} , try later",
                    deviceContext.getDeviceState().getNodeId());
            scheduleNextPolling(deviceContext, statisticsContext, timeCounter);
            return;
        }
        LOG.debug("POLLING ALL STATS for device: {}", deviceContext.getDeviceState().getNodeId().getValue());
        timeCounter.markStart();
        final ListenableFuture<Boolean> deviceStatisticsCollectionFuture = statisticsContext.gatherDynamicData();
        Futures.addCallback(deviceStatisticsCollectionFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean o) {
                timeCounter.addTimeMark();
                calculateTimerDelay(timeCounter);
                scheduleNextPolling(deviceContext, statisticsContext, timeCounter);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                timeCounter.addTimeMark();
                LOG.info("Statistics gathering for single node was not successful: {}", throwable.getMessage());
                LOG.debug("Statistics gathering for single node was not successful.. ", throwable);
                calculateTimerDelay(timeCounter);
                scheduleNextPolling(deviceContext, statisticsContext, timeCounter);
            }
        });

        final long STATS_TIMEOUT_SEC = 20L;
        try {
            deviceStatisticsCollectionFuture.get(STATS_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Statistics collection for node {} failed", deviceContext.getDeviceState().getNodeId(), e);
        } catch (final TimeoutException e) {
            LOG.info("Statistics collection for node {} still in progress even after {} secs", deviceContext.getDeviceState().getNodeId(), STATS_TIMEOUT_SEC);
        }
    }

    private void scheduleNextPolling(final DeviceContext deviceContext,
                                     final StatisticsContext statisticsContext,
                                     final TimeCounter timeCounter) {
        if (null != hashedWheelTimer) {
            LOG.debug("SCHEDULING NEXT STATS POLLING for device: {}", deviceContext.getDeviceState().getNodeId().getValue());
            if (!shuttingDownStatisticsPolling) {
                final Timeout pollTimeout = hashedWheelTimer.newTimeout(new TimerTask() {
                    @Override
                    public void run(final Timeout timeout) throws Exception {
                        pollStatistics(deviceContext, statisticsContext, timeCounter);
                    }
                }, currentTimerDelay, TimeUnit.MILLISECONDS);
                statisticsContext.setPollTimeout(pollTimeout);
            }
        } else {
            LOG.debug("#!NOT SCHEDULING NEXT STATS POLLING for device: {}", deviceContext.getDeviceState().getNodeId().getValue());
        }
    }

    @VisibleForTesting
    protected void calculateTimerDelay(final TimeCounter timeCounter) {
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
    protected static long getCurrentTimerDelay() {
        return currentTimerDelay;
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        final StatisticsContext statisticsContext = contexts.remove(deviceContext);
        if (null != statisticsContext) {
            LOG.trace("Removing device context from stack. No more statistics gathering for node {}", deviceContext.getDeviceState().getNodeId());
            try {
                statisticsContext.close();
            } catch (final Exception e) {
                LOG.debug("Error closing statistic context for node {}.", deviceContext.getDeviceState().getNodeId());
            }
        }
    }

    @Override
    public Future<RpcResult<GetStatisticsWorkModeOutput>> getStatisticsWorkMode() {
        final GetStatisticsWorkModeOutputBuilder smModeOutputBld = new GetStatisticsWorkModeOutputBuilder();
        smModeOutputBld.setMode(workMode);
        return RpcResultBuilder.success(smModeOutputBld.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<Void>> changeStatisticsWorkMode(final ChangeStatisticsWorkModeInput input) {
        final Future<RpcResult<Void>> result;
        // acquire exclusive access
        if (workModeGuard.tryAcquire()) {
            final StatisticsWorkMode targetWorkMode = input.getMode();
            if (!workMode.equals(targetWorkMode)) {
                shuttingDownStatisticsPolling = StatisticsWorkMode.FULLYDISABLED.equals(targetWorkMode);
                // iterate through stats-ctx: propagate mode
                for (final Map.Entry<DeviceContext, StatisticsContext> contextEntry : contexts.entrySet()) {
                    final DeviceContext deviceContext = contextEntry.getKey();
                    final StatisticsContext statisticsContext = contextEntry.getValue();
                    switch (targetWorkMode) {
                        case COLLECTALL:
                            scheduleNextPolling(deviceContext, statisticsContext, new TimeCounter());
                            for (final ItemLifeCycleSource lifeCycleSource : deviceContext.getItemLifeCycleSourceRegistry().getLifeCycleSources()) {
                                lifeCycleSource.setItemLifecycleListener(null);
                            }
                            break;
                        case FULLYDISABLED:
                            final Optional<Timeout> pollTimeout = statisticsContext.getPollTimeout();
                            if (pollTimeout.isPresent()) {
                                pollTimeout.get().cancel();
                            }
                            for (final ItemLifeCycleSource lifeCycleSource : deviceContext.getItemLifeCycleSourceRegistry().getLifeCycleSources()) {
                                lifeCycleSource.setItemLifecycleListener(statisticsContext.getItemLifeCycleListener());
                            }
                            break;
                        default:
                            LOG.warn("statistics work mode not supported: {}", targetWorkMode);
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
    public void close() {
        if (controlServiceRegistration != null) {
            controlServiceRegistration.close();
            controlServiceRegistration = null;
        }
    }
}
