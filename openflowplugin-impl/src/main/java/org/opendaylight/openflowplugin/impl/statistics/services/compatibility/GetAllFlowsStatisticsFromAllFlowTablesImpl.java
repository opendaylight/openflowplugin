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
import org.opendaylight.openflowplugin.impl.statistics.services.AllFlowsInAllTablesService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTables;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;

@Deprecated
public final class GetAllFlowsStatisticsFromAllFlowTablesImpl implements GetAllFlowsStatisticsFromAllFlowTables {
    private final NotificationPublishService notificationService;
    private final AllFlowsInAllTablesService allFlowsInAllTables;

    public GetAllFlowsStatisticsFromAllFlowTablesImpl(final RequestContextStack requestContextStack,
            final DeviceContext deviceContext, final ConvertorExecutor convertorExecutor,
            final AtomicLong compatibilityXidSeed, final NotificationPublishService notificationService) {
        allFlowsInAllTables = new AllFlowsInAllTablesService(requestContextStack, deviceContext, compatibilityXidSeed,
            convertorExecutor);
        this.notificationService = requireNonNull(notificationService);
    }

    @Override
    public ListenableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> invoke(
            final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        return allFlowsInAllTables.handleAndNotify(input, notificationService);
    }
}
