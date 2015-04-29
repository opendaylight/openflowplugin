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

    private ConcurrentHashMap<DeviceContext, StatisticsContext> contexts = new ConcurrentHashMap();

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
        final ListenableFuture<Boolean> weHaveDynamicData = statisticsContext.gatherDynamicData();
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
                } catch (Exception e) {
                    LOG.warn("Error closing device context.", e);
                }
            }
        });
    }

    private void pollStatistics() {
        for (final StatisticsContext statisticsContext : contexts.values()) {
            ListenableFuture deviceStatisticsCollectionFuture = statisticsContext.gatherDynamicData();
            Futures.addCallback(deviceStatisticsCollectionFuture, new FutureCallback() {
                @Override
                public void onSuccess(final Object o) {
                    //nothing to do here
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    LOG.info("Statistics gathering for single node was not successful.");
                }
            });
        }
        if (null != hashedWheelTimer) {
            hashedWheelTimer.newTimeout(new TimerTask() {
                @Override
                public void run(final Timeout timeout) throws Exception {
                    pollStatistics();
                }
            }, 3000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onDeviceContextClosed(final DeviceContext deviceContext) {
        if (contexts.containsKey(deviceContext)) {
            LOG.trace("Removing device context from stack. No more statistics gathering for node {}", deviceContext.getDeviceState().getNodeId());
            contexts.remove(deviceContext);
            StatisticsContext statisticsContext = contexts.remove(deviceContext);
            try {
                statisticsContext.close();
            } catch (Exception e) {
                LOG.debug("Error closing statistic context for node {}.", deviceContext.getDeviceState().getNodeId());
            }
        }
    }
}
