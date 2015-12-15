/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.statistics.services.AggregateFlowsInTableService;
import org.opendaylight.openflowplugin.impl.statistics.services.AllFlowsInAllTablesService;
import org.opendaylight.openflowplugin.impl.statistics.services.AllFlowsInTableService;
import org.opendaylight.openflowplugin.impl.statistics.services.FlowsInTableService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joe
 */
public class OpendaylightFlowStatisticsServiceDelegateImpl implements OpendaylightFlowStatisticsService {

    private static final Logger LOG = LoggerFactory.getLogger(OpendaylightFlowStatisticsServiceDelegateImpl.class);

    private final AggregateFlowsInTableService aggregateFlowsInTable;
    private final AllFlowsInAllTablesService allFlowsInAllTables;
    private final AllFlowsInTableService allFlowsInTable;
    private final FlowsInTableService flowsInTable;
    private final NotificationPublishService notificationService;

    public OpendaylightFlowStatisticsServiceDelegateImpl(final RequestContextStack requestContextStack,
                                                         final DeviceContext deviceContext,
                                                         final NotificationPublishService notificationService,
                                                         final AtomicLong compatibilityXidSeed) {
        this.notificationService = notificationService;
        aggregateFlowsInTable = new AggregateFlowsInTableService(requestContextStack, deviceContext, compatibilityXidSeed);
        allFlowsInAllTables = new AllFlowsInAllTablesService(requestContextStack, deviceContext, compatibilityXidSeed);
        allFlowsInTable = new AllFlowsInTableService(requestContextStack, deviceContext, compatibilityXidSeed);
        flowsInTable = new FlowsInTableService(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    /**
     * @deprecated this is the only method with real implementation provided, in delegate it makes no sense
     */
    @Override
    @Deprecated
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> getAggregateFlowStatisticsFromFlowTableForGivenMatch(
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        throw new IllegalAccessError("unsupported by backward compatibility delegate service " +
                "- this rpc is always provided by default service implementation");
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> getAggregateFlowStatisticsFromFlowTableForAllFlows(
            final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {
        return aggregateFlowsInTable.handleAndNotify(input, notificationService);
    }

    @Override
    public Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            final GetAllFlowStatisticsFromFlowTableInput input) {
        return allFlowsInTable.handleAndNotify(input, notificationService);
    }

    @Override
    public Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        return allFlowsInAllTables.handleAndNotify(input, notificationService);
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            final GetFlowStatisticsFromFlowTableInput input) {
        return flowsInTable.handleAndNotify(input, notificationService);
    }
}
