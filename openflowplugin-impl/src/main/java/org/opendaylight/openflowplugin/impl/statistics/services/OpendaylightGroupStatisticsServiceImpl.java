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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightGroupStatisticsServiceImpl implements OpendaylightGroupStatisticsService {
    private final AllGroupsStatsService allGroups;
    private final GroupDescriptionService groupDesc;
    private final GroupFeaturesService groupFeat;
    private final GroupStatsService groupStats;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightGroupStatisticsServiceImpl(final RequestContextStack requestContextStack,
                                                  final DeviceContext deviceContext,
                                                  final AtomicLong compatibilityXidSeed,
                                                  final NotificationPublishService notificationPublishService,
                                                  final ConvertorExecutor convertorExecutor) {
        this.notificationPublishService = notificationPublishService;
        allGroups =
                new AllGroupsStatsService(requestContextStack, deviceContext, compatibilityXidSeed, convertorExecutor);
        groupDesc = new GroupDescriptionService(requestContextStack,
                                                deviceContext,
                                                compatibilityXidSeed,
                                                convertorExecutor);
        groupFeat = new GroupFeaturesService(requestContextStack, deviceContext, compatibilityXidSeed);
        groupStats = new GroupStatsService(requestContextStack, deviceContext, compatibilityXidSeed, convertorExecutor);
    }

    @Override
    public ListenableFuture<RpcResult<GetAllGroupStatisticsOutput>> getAllGroupStatistics(
            final GetAllGroupStatisticsInput input) {
        return allGroups.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetGroupDescriptionOutput>> getGroupDescription(
            final GetGroupDescriptionInput input) {
        return groupDesc.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetGroupFeaturesOutput>> getGroupFeatures(final GetGroupFeaturesInput input) {
        return groupFeat.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(
            final GetGroupStatisticsInput input) {
        return groupStats.handleAndNotify(input, notificationPublishService);
    }
}
