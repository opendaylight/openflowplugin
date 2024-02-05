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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetGroupStatisticsImpl implements GetGroupStatistics {
    private final GroupStatsService groupStats;
    private final NotificationPublishService notificationPublishService;

    public GetGroupStatisticsImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationPublishService,
            final ConvertorExecutor convertorExecutor) {
        this.notificationPublishService = notificationPublishService;
        groupStats = new GroupStatsService(requestContextStack, deviceContext, compatibilityXidSeed, convertorExecutor);
    }

    @Override
    public ListenableFuture<RpcResult<GetGroupStatisticsOutput>> invoke(final GetGroupStatisticsInput input) {
        return groupStats.handleAndNotify(input, notificationPublishService);
    }
}
