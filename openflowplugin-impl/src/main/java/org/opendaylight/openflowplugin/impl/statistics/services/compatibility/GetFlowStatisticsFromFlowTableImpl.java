/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import static java.util.Objects.requireNonNull;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.statistics.services.FlowsInTableService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetFlowStatisticsFromFlowTableImpl implements GetFlowStatisticsFromFlowTable {
    private final FlowsInTableService flowsInTable;
    private final NotificationPublishService notificationService;

    public GetFlowStatisticsFromFlowTableImpl(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationService) {
        flowsInTable = new FlowsInTableService(requestContextStack, deviceContext, compatibilityXidSeed,
            convertorExecutor);
        this.notificationService = requireNonNull(notificationService);
    }

    @Override
    public ListenableFuture<RpcResult<GetFlowStatisticsFromFlowTableOutput>> invoke(
            final GetFlowStatisticsFromFlowTableInput input) {
        return flowsInTable.handleAndNotify(input, notificationService);
    }
}
