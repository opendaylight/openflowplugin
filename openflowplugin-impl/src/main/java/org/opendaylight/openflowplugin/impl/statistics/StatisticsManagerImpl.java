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

    @Override
    public void setDeviceInitializationPhaseHandler(final DeviceInitializationPhaseHandler handler) {
        deviceInitPhaseHandler = handler;
    }

    @Override
    public void onDeviceContextLevelUp(final DeviceContext deviceContext) {
        final StatisticsContext statisticsContext = new StatisticsContextImpl(deviceContext);
        final ListenableFuture<Void> weHaveDynamicData = statisticsContext.gatherDynamicData();
        Futures.addCallback(weHaveDynamicData, new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void aVoid) {
                // wake up RPC registration
                deviceInitPhaseHandler.onDeviceContextLevelUp(deviceContext);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("Statistics manager was not able to collect dynamic info for device {}", deviceContext.getDeviceState().getNodeId());
            }
        });
    }
}
