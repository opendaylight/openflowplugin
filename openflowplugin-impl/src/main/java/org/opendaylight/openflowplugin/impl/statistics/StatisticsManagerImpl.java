/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsManagerImpl implements StatisticsManager {

    static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;

    private HashedWheelTimer hashedWheelTimer;

    private final ConcurrentHashMap<DeviceContext, StatisticsContext> contexts = new ConcurrentHashMap<>();

    private final List<MultipartType> collectingStatType;

    private final TimeCounter timeCounter = new TimeCounter();

    private static final long basicTimerDelay = 3000;
    private static long currentTimerDelay = basicTimerDelay;
    private static long maximumTimerDelay = 900000; //wait max 15 minutes for next statistics

    public StatisticsManagerImpl () {
        final MultipartType[] allCollectingStatType = {MultipartType.OFPMPFLOW, MultipartType.OFPMPTABLE,
                MultipartType.OFPMPPORTSTATS, MultipartType.OFPMPQUEUE, MultipartType.OFPMPGROUPDESC,
                MultipartType.OFPMPGROUP,MultipartType.OFPMPMETERCONFIG, MultipartType.OFPMPMETER};
        collectingStatType = ImmutableList.<MultipartType>copyOf(allCollectingStatType);
    }

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {

        if (null == hashedWheelTimer) {
            LOG.trace("This is first device that delivered timer. Starting statistics polling immediately.");
            hashedWheelTimer = deviceContext.getTimer();
            pollStatistics();
        }

        final StatisticsContext statisticsContext = new StatisticsContextImpl(deviceContext);
        deviceContext.addDeviceContextClosedHandler(this);
        final ListenableFuture<Boolean> weHaveDynamicData = poolStatPerDevice(statisticsContext);
        Futures.addCallback(weHaveDynamicData, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean statisticsGathered) {
                if (statisticsGathered.booleanValue()) {
                    //there are some statistics on device worth gathering
                    contexts.put(deviceContext, statisticsContext);
                }
                LOG.trace("Device dynamic info collecting done. Going to announce raise to next level.");
                deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
                deviceContext.getDeviceState().setDeviceSynchronized(true);
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

    private ListenableFuture<Boolean> poolStatPerDevice(final StatisticsContext statisticsContext) {
        final SettableFuture<Boolean> settableResultingFuture = SettableFuture.create();
        final Iterator<MultipartType> statCollIterator = collectingStatType.iterator();
        statFutureMaker(statisticsContext, statCollIterator, settableResultingFuture);
        return settableResultingFuture;
    }

    void statFutureMaker(final StatisticsContext statisticsContext, final Iterator<MultipartType> iterator,
            final SettableFuture<Boolean> resultFuture) {
        Preconditions.checkArgument(statisticsContext != null);
        Preconditions.checkArgument(resultFuture != null);
        Preconditions.checkArgument(iterator != null);
        if ( ! iterator.hasNext()) {
            resultFuture.set(Boolean.TRUE);
            return;
        }
        final ListenableFuture<Boolean> deviceStatisticsCollectionFuture = statisticsContext.gatherDynamicData(iterator.next());
        Futures.addCallback(deviceStatisticsCollectionFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                statFutureMaker(statisticsContext, iterator, resultFuture);
            }
            @Override
            public void onFailure(final Throwable t) {
                resultFuture.setException(t);
            }

        });
    }

    void pollStatistics(final Iterator<StatisticsContext> deviceIterator, final SettableFuture<Boolean> resultFuture) {
        Preconditions.checkArgument(resultFuture != null);
        Preconditions.checkArgument(deviceIterator != null);
        if ( ! deviceIterator.hasNext()) {
            resultFuture.set(Boolean.TRUE);
            return;
        }
        final ListenableFuture<Boolean> deviceStatResult = poolStatPerDevice(deviceIterator.next());
        Futures.addCallback(deviceStatResult, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean result) {
                pollStatistics(deviceIterator, resultFuture);
            }
            @Override
            public void onFailure(final Throwable t) {
                resultFuture.setException(t);
            }
        });
    }

    private void pollStatistics() {
        try {
            timeCounter.markStart();
            final SettableFuture<Boolean> settableResultingFuture = SettableFuture.create();
            final Iterator<StatisticsContext> deviceIterator = contexts.values().iterator();
            pollStatistics(deviceIterator, settableResultingFuture);
            Futures.addCallback(settableResultingFuture, new FutureCallback<Boolean>() {

                @Override
                public void onSuccess(final Boolean result) {
                    timeCounter.addTimeMark();
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    timeCounter.addTimeMark();
                    LOG.info("Statistics gathering for single node was not successful: {}", throwable.getMessage());
                    LOG.debug("Statistics gathering for single node was not successful.. ", throwable);
                }

            });
        } finally {
            calculateTimerDelay();
            if (null != hashedWheelTimer) {
                hashedWheelTimer.newTimeout(new TimerTask() {
                    @Override
                    public void run(final Timeout timeout) throws Exception {
                        pollStatistics();
                    }
                }, currentTimerDelay, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void calculateTimerDelay() {
        final long averageStatisticsGatheringTime = timeCounter.getAverageTimeBetweenMarks();
        final int numberOfDevices = contexts.size();
        if ((averageStatisticsGatheringTime * numberOfDevices) > currentTimerDelay) {
            currentTimerDelay *= 2;
            if (currentTimerDelay > maximumTimerDelay) {
                currentTimerDelay = maximumTimerDelay;
            }
        } else {
            if (currentTimerDelay > basicTimerDelay) {
                currentTimerDelay /= 2;
            }
        }
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

    private final class TimeCounter {
        private long beginningOfTime;
        private long delta;
        private int marksCount = 0;

        public void markStart() {
            beginningOfTime = System.nanoTime();
            delta = 0;
            marksCount = 0;
        }

        public void addTimeMark() {
            delta += System.nanoTime() - beginningOfTime;
            marksCount++;
        }

        public long getAverageTimeBetweenMarks() {
            long average = 0;
            if (marksCount > 0) {
                average = delta / marksCount;
            }
            return TimeUnit.NANOSECONDS.toMillis(average);
        }

    }
}
