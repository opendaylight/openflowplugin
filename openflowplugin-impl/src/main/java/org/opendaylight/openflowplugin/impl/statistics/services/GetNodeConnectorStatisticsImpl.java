/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetNodeConnectorStatisticsImpl implements GetNodeConnectorStatistics {
    private final PortStatsService portStats;
    private final NotificationPublishService notificationPublishService;

    public GetNodeConnectorStatisticsImpl(final RequestContextStack requestContextStack,
                                                 final DeviceContext deviceContext,
                                                 final AtomicLong compatibilityXidSeed,
                                                 final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
        portStats = new PortStatsService(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    public ListenableFuture<RpcResult<GetNodeConnectorStatisticsOutput>> invoke(
            final GetNodeConnectorStatisticsInput input) {
        return portStats.handleAndNotify(input, notificationPublishService);
    }
}
