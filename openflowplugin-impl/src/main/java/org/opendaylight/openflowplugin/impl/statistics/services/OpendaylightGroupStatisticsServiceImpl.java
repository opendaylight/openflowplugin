/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
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

/**
 * @author joe
 */
public class OpendaylightGroupStatisticsServiceImpl implements OpendaylightGroupStatisticsService {
    private final AllGroupsStatsService allGroups;
    private final GroupDescriptionService groupDesc;
    private final GroupFeaturesService groupFeat;
    private final GroupStatsService groupStats;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightGroupStatisticsServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
                                                  final AtomicLong compatibilityXidSeed,
                                                  final NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
        allGroups = new AllGroupsStatsService(requestContextStack, deviceContext, compatibilityXidSeed);
        groupDesc = new GroupDescriptionService(requestContextStack, deviceContext, compatibilityXidSeed);
        groupFeat = new GroupFeaturesService(requestContextStack, deviceContext, compatibilityXidSeed);
        groupStats = new GroupStatsService(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    public Future<RpcResult<GetAllGroupStatisticsOutput>> getAllGroupStatistics(final GetAllGroupStatisticsInput input) {
        return allGroups.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public Future<RpcResult<GetGroupDescriptionOutput>> getGroupDescription(final GetGroupDescriptionInput input) {
        return groupDesc.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public Future<RpcResult<GetGroupFeaturesOutput>> getGroupFeatures(final GetGroupFeaturesInput input) {
        return groupFeat.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(final GetGroupStatisticsInput input) {
        return groupStats.handleAndNotify(input, notificationPublishService);
    }
}
