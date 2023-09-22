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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescription;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupDescriptionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupStatisticsOutput;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightGroupStatisticsRpcs {
    private final AllGroupsStatsService allGroups;
    private final GroupDescriptionService groupDesc;
    private final GroupFeaturesService groupFeat;
    private final GroupStatsService groupStats;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightGroupStatisticsRpcs(final RequestContextStack requestContextStack,
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

    private ListenableFuture<RpcResult<GetAllGroupStatisticsOutput>> getAllGroupStatistics(
            final GetAllGroupStatisticsInput input) {
        return allGroups.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetGroupDescriptionOutput>> getGroupDescription(
            final GetGroupDescriptionInput input) {
        return groupDesc.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetGroupFeaturesOutput>> getGroupFeatures(final GetGroupFeaturesInput input) {
        return groupFeat.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(
            final GetGroupStatisticsInput input) {
        return groupStats.handleAndNotify(input, notificationPublishService);
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(GetAllGroupStatistics.class, this::getAllGroupStatistics)
            .put(GetGroupDescription.class, this::getGroupDescription)
            .put(GetGroupFeatures.class, this::getGroupFeatures)
            .put(GetGroupStatistics.class, this::getGroupStatistics)
            .build();
    }
}
