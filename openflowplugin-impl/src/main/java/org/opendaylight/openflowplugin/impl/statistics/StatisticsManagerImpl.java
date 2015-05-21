/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsManagerImpl implements StatisticsManager {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerImpl.class);

    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;

    private HashedWheelTimer hashedWheelTimer;

    private final ConcurrentHashMap<DeviceContext, StatisticsContext> contexts = new ConcurrentHashMap<>();

    private static final long basicTimerDelay = 3000;

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {

        if (null == hashedWheelTimer) {
            LOG.trace("This is first device that deliverd timer.");
            hashedWheelTimer = deviceContext.getTimer();
        }

        final StatisticsContext statisticsContext = new StatisticsContextImpl(deviceContext);
        deviceContext.addDeviceContextClosedHandler(this);
        final ListenableFuture<Boolean> weHaveDynamicData = statisticsContext.gatherDynamicData();
        Futures.addCallback(weHaveDynamicData, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean statisticsGathered) {
                if (statisticsGathered.booleanValue()) {
                    //there are some statistics on device worth gathering
                    contexts.put(deviceContext, statisticsContext);
                    pollStatistics(statisticsContext);
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

    private void pollStatistics(final StatisticsContext statisticsContext) {
        final ListenableFuture<Boolean> deviceStatisticsCollectionFuture = statisticsContext.gatherDynamicData();
        Futures.addCallback(deviceStatisticsCollectionFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(final Boolean o) {
                setStatisticsTimer(statisticsContext);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                setStatisticsTimer(statisticsContext);
                LOG.info("Statistics gathering for single node was not successful: {}", throwable.getMessage());
                LOG.debug("Statistics gathering for single node was not successful.. ", throwable);
            }
        });
    }

    private void setStatisticsTimer(final StatisticsContext statisticsContext) {
        if ( ! contexts.contains(statisticsContext)) {
            LOG.debug("StatisticsContext {} is not active.", statisticsContext);
            return;
        }
        if (null != hashedWheelTimer) {
            hashedWheelTimer.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {
                    pollStatistics(statisticsContext);
                }
            }, basicTimerDelay, TimeUnit.MILLISECONDS);
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
}
