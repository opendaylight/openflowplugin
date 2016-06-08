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
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleConductor;
import org.opendaylight.openflowplugin.api.openflow.rpc.ItemLifeCycleSource;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class StatisticsManagerImpl implements StatisticsManager, StatisticsManagerControlService {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    private static final long DEFAULT_STATS_TIMEOUT_SEC = 50L;

    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    private DeviceTerminationPhaseHandler deviceTerminPhaseHandler;

    private final ConcurrentMap<NodeId, StatisticsContext> contexts = new ConcurrentHashMap<>();

    private static final long basicTimerDelay = 3000;
    private static long currentTimerDelay = basicTimerDelay;
    private static final long maximumTimerDelay = 900000; //wait max 15 minutes for next statistics

    private StatisticsWorkMode workMode = StatisticsWorkMode.COLLECTALL;
    private final Semaphore workModeGuard = new Semaphore(1, true);
    private boolean shuttingDownStatisticsPolling;
    private BindingAwareBroker.RpcRegistration<StatisticsManagerControlService> controlServiceRegistration;

    private final LifecycleConductor conductor;

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    public StatisticsManagerImpl(@CheckForNull final RpcProviderRegistry rpcProviderRegistry,
                                 final boolean shuttingDownStatisticsPolling,
                                 final LifecycleConductor lifecycleConductor) {
        Preconditions.checkArgument(rpcProviderRegistry != null);
        this.controlServiceRegistration = Preconditions.checkNotNull(rpcProviderRegistry.addRpcImplementation(
                StatisticsManagerControlService.class, this));
        this.shuttingDownStatisticsPolling = shuttingDownStatisticsPolling;
        this.conductor = lifecycleConductor;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceInfo deviceInfo) throws Exception {

        final DeviceContext deviceContext = Preconditions.checkNotNull(conductor.getDeviceContext(deviceInfo.getNodeId()));

        final StatisticsContext statisticsContext = new StatisticsContextImpl(deviceInfo.getNodeId(), shuttingDownStatisticsPolling, conductor);
        Verify.verify(contexts.putIfAbsent(deviceInfo.getNodeId(), statisticsContext) == null, "StatisticsCtx still not closed for Node {}", deviceInfo.getNodeId());

        deviceContext.getDeviceState().setDeviceSynchronized(true);
        deviceInitPhaseHandler.onDeviceContextLevelUp(deviceInfo);
    }

    @VisibleForTesting
    void pollStatistics(final DeviceContext deviceContext,
                                final StatisticsContext statisticsContext,
                                final TimeCounter timeCounter) {

        final NodeId nodeId = deviceContext.getDeviceState().getNodeId();

        if (!statisticsContext.isSchedulingEnabled()) {
            LOG.debug("Disabling statistics scheduling for device: {}", nodeId);
            return;
        }
        
        if (!deviceContext.getDeviceState().isValid()) {
            LOG.debug("Session is not valid for device: {}", nodeId);
            return;
        }

        if (!deviceContext.getDeviceState().isStatisticsPollingEnabled()) {
            LOG.debug("Statistics polling is currently disabled for device: {}", nodeId);
            scheduleNextPolling(deviceContext, statisticsContext, timeCounter);
            return;
        }

        LOG.debug("POLLING ALL STATISTICS for device: {}", nodeId);
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
            public void onFailure(@Nonnull final Throwable throwable) {
                timeCounter.addTimeMark();
                LOG.warn("Statistics gathering for single node was not successful: {}", throwable.getMessage());
                LOG.trace("Statistics gathering for single node was not successful.. ", throwable);
                calculateTimerDelay(timeCounter);
                if (throwable instanceof CancellationException) {
                    /** This often happens when something wrong with akka or DS, so closing connection will help to restart device **/
                    conductor.closeConnection(deviceContext.getDeviceState().getNodeId());
                } else {
                    scheduleNextPolling(deviceContext, statisticsContext, timeCounter);
                }
            }
        });

        final long averageTime = TimeUnit.MILLISECONDS.toSeconds(timeCounter.getAverageTimeBetweenMarks());
        final long STATS_TIMEOUT_SEC = averageTime > 0 ? 3 * averageTime : DEFAULT_STATS_TIMEOUT_SEC;
        final TimerTask timerTask = new TimerTask() {

            @Override
            public void run(final Timeout timeout) throws Exception {
                if (!deviceStatisticsCollectionFuture.isDone()) {
                    LOG.info("Statistics collection for node {} still in progress even after {} secs", nodeId, STATS_TIMEOUT_SEC);
                    deviceStatisticsCollectionFuture.cancel(true);
                }
            }
        };

        conductor.newTimeout(timerTask, STATS_TIMEOUT_SEC, TimeUnit.SECONDS);
    }

    private void scheduleNextPolling(final DeviceContext deviceContext,
                                     final StatisticsContext statisticsContext,
                                     final TimeCounter timeCounter) {
        LOG.debug("SCHEDULING NEXT STATISTICS POLLING for device: {}", deviceContext.getDeviceState().getNodeId());
        if (!shuttingDownStatisticsPolling) {
            final Timeout pollTimeout = conductor.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {
                    pollStatistics(deviceContext, statisticsContext, timeCounter);
                }
            }, currentTimerDelay, TimeUnit.MILLISECONDS);
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
    static long getCurrentTimerDelay() {
        return currentTimerDelay;
    }

    @Override
    public void onDeviceContextLevelDown(final DeviceInfo deviceInfo) {
        final StatisticsContext statisticsContext = contexts.remove(deviceInfo.getNodeId());
        if (null != statisticsContext) {
            LOG.trace("Removing device context from stack. No more statistics gathering for device: {}", deviceInfo.getNodeId());
            statisticsContext.close();
        }
        deviceTerminPhaseHandler.onDeviceContextLevelDown(deviceInfo);
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
                shuttingDownStatisticsPolling = StatisticsWorkMode.FULLYDISABLED.equals(targetWorkMode);
                // iterate through stats-ctx: propagate mode
                for (final StatisticsContext statisticsContext : contexts.values()) {
                    final DeviceContext deviceContext = statisticsContext.getDeviceContext();
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
    public void startScheduling(final NodeId nodeId) {
        if (shuttingDownStatisticsPolling) {
            LOG.info("Statistics are shut down for device: {}", nodeId);
            return;
        }

        final StatisticsContext statisticsContext = contexts.get(nodeId);

        if (statisticsContext == null) {
            LOG.warn("Statistics context not found for device: {}", nodeId);
            return;
        }

        if (statisticsContext.isSchedulingEnabled()) {
            LOG.debug("Statistics scheduling is already enabled for device: {}", nodeId);
            return;
        }

        LOG.info("Scheduling statistics poll for device: {}", nodeId);
        final DeviceContext deviceContext = conductor.getDeviceContext(nodeId);

        if (deviceContext == null) {
            LOG.warn("Device context not found for device: {}", nodeId);
            return;
        }

        statisticsContext.setSchedulingEnabled(true);
        scheduleNextPolling(deviceContext, statisticsContext, new TimeCounter());
    }

    @Override
    public void stopScheduling(final NodeId nodeId) {
        LOG.debug("Stopping statistics scheduling for device: {}", nodeId);
        final StatisticsContext statisticsContext = contexts.get(nodeId);

        if (statisticsContext == null) {
            LOG.warn("Statistics context not found for device: {}", nodeId);
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
    public void setDeviceTerminationPhaseHandler(final DeviceTerminationPhaseHandler handler) {
        this.deviceTerminPhaseHandler = handler;
    }
}
