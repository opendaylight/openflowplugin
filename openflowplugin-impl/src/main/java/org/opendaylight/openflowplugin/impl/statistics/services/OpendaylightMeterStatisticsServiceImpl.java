/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class OpendaylightMeterStatisticsServiceImpl implements OpendaylightMeterStatisticsService {
    private final AllMeterConfigStatsService allMeterConfig;
    private final AllMeterStatsService allMeterStats;
    private final MeterFeaturesService meterFeatures;
    private final MeterStatsService meterStats;
    private final NotificationPublishService notificationPublishService;

    public OpendaylightMeterStatisticsServiceImpl(final RequestContextStack requestContextStack,
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

    @Override
    public ListenableFuture<RpcResult<GetAllMeterConfigStatisticsOutput>> getAllMeterConfigStatistics(
            final GetAllMeterConfigStatisticsInput input) {
        return allMeterConfig.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetAllMeterStatisticsOutput>> getAllMeterStatistics(
                                                                      final GetAllMeterStatisticsInput input) {
        return allMeterStats.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetMeterFeaturesOutput>> getMeterFeatures(final GetMeterFeaturesInput input) {
        return meterFeatures.handleAndNotify(input, notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(
            final GetMeterStatisticsInput input) {
        return meterStats.handleAndNotify(input, notificationPublishService);
    }
}
