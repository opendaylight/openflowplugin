/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.openflowplugin.impl.statistics.services.AllFlowsInTableService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetAllFlowStatisticsFromFlowTableImpl implements GetAllFlowStatisticsFromFlowTable {
    private final AllFlowsInTableService allFlowsInTable;
    private final NotificationPublishService notificationService;

    public GetAllFlowStatisticsFromFlowTableImpl(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationService) {
        allFlowsInTable = new AllFlowsInTableService(requestContextStack, deviceContext, compatibilityXidSeed,
            convertorExecutor);
        this.notificationService = requireNonNull(notificationService);
    }

    @Override
    public ListenableFuture<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> invoke(
            final GetAllFlowStatisticsFromFlowTableInput input) {
        return allFlowsInTable.handleAndNotify(input, notificationService);
    }
}
