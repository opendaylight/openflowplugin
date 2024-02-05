/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetMeterStatisticsImpl implements GetMeterStatistics {
    private final NotificationPublishService notificationPublishService;
    private final MeterStatsService meterStats;

    public GetMeterStatisticsImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationPublishService,
            final ConvertorExecutor convertorExecutor) {
        meterStats = new MeterStatsService(requestContextStack, deviceContext, compatibilityXidSeed, convertorExecutor);
        this.notificationPublishService = requireNonNull(notificationPublishService);
    }

    @Override
    public ListenableFuture<RpcResult<GetMeterStatisticsOutput>> invoke(final GetMeterStatisticsInput input) {
        return meterStats.handleAndNotify(input, notificationPublishService);
    }
}
