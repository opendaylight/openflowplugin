/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightPortStatisticsRpcs {
    private final AllPortStatsService allPortStats;
    private final PortStatsService portStats;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightPortStatisticsRpcs(final RequestContextStack requestContextStack,
                                                 final DeviceContext deviceContext,
                                                 final AtomicLong compatibilityXidSeed,
                                                 final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
        allPortStats = new AllPortStatsService(requestContextStack, deviceContext, compatibilityXidSeed);
        portStats = new PortStatsService(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    private ListenableFuture<RpcResult<GetAllNodeConnectorsStatisticsOutput>> getAllNodeConnectorsStatistics(
            final GetAllNodeConnectorsStatisticsInput input) {
        return allPortStats.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(
            final GetNodeConnectorStatisticsInput input) {
        return portStats.handleAndNotify(input, notificationPublishService);
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(GetAllNodeConnectorsStatistics.class, this::getAllNodeConnectorsStatistics)
            .put(GetNodeConnectorStatistics.class, this::getNodeConnectorStatistics)
            .build();
    }
}
