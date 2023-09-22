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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightMeterStatisticsRpcs {
    private final AllMeterConfigStatsService allMeterConfig;
    private final AllMeterStatsService allMeterStats;
    private final MeterFeaturesService meterFeatures;
    private final MeterStatsService meterStats;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightMeterStatisticsRpcs(final RequestContextStack requestContextStack,
                                                  final DeviceContext deviceContext,
                                                  final AtomicLong compatibilityXidSeed,
                                                  final NotificationPublishService notificationPublishService,
                                                  final ConvertorExecutor convertorExecutor) {
        this.notificationPublishService = notificationPublishService;

        allMeterConfig = new AllMeterConfigStatsService(requestContextStack,
                                                        deviceContext,
                                                        compatibilityXidSeed,
                                                        convertorExecutor);
        allMeterStats = new AllMeterStatsService(requestContextStack,
                                                 deviceContext,
                                                 compatibilityXidSeed,
                                                 convertorExecutor);
        meterFeatures = new MeterFeaturesService(requestContextStack, deviceContext, compatibilityXidSeed);
        meterStats = new MeterStatsService(requestContextStack, deviceContext, compatibilityXidSeed, convertorExecutor);
    }

    private ListenableFuture<RpcResult<GetAllMeterConfigStatisticsOutput>> getAllMeterConfigStatistics(
            final GetAllMeterConfigStatisticsInput input) {
        return allMeterConfig.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetAllMeterStatisticsOutput>> getAllMeterStatistics(
                                                                      final GetAllMeterStatisticsInput input) {
        return allMeterStats.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetMeterFeaturesOutput>> getMeterFeatures(final GetMeterFeaturesInput input) {
        return meterFeatures.handleAndNotify(input, notificationPublishService);
    }

    private ListenableFuture<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(
            final GetMeterStatisticsInput input) {
        return meterStats.handleAndNotify(input, notificationPublishService);
    }

    public ClassToInstanceMap<Rpc<?,?>> getRpcClassToInstanceMap() {
        return ImmutableClassToInstanceMap.<Rpc<?, ?>>builder()
            .put(GetAllMeterConfigStatistics.class, this::getAllMeterConfigStatistics)
            .put(GetAllMeterStatistics.class, this::getAllMeterStatistics)
            .put(GetMeterFeatures.class, this::getMeterFeatures)
            .put(GetMeterStatistics.class, this::getMeterStatistics)
            .build();
    }
}
