/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.OpendaylightDirectStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.concurrent.Future;

/**
 * The Opendaylight direct statistics service.
 * This service handles RPC requests, sends them to registered handlers and returns their replies.
 *
 * @author Tomas Slusny
 */
public class OpendaylightDirectStatisticsServiceImpl implements OpendaylightDirectStatisticsService {
    private final FlowDirectStatisticsService flowDirectStatisticsService;
    private final GroupDirectStatisticsService groupStatisticsService;
    private final MeterDirectStatisticsService meterDirectStatisticsService;
    private final NodeConnectorDirectStatisticsService nodeConnectorDirectStatisticsService;
    private final QueueDirectStatisticsService queueDirectStatisticsService;

    /**
     * Instantiates a new Opendaylight direct statistics service.
     *
     * @param provider the openflow direct statistics service provider
     */
    public OpendaylightDirectStatisticsServiceImpl(final OpendaylightDirectStatisticsServiceProvider provider) {
        flowDirectStatisticsService = provider.lookup(FlowDirectStatisticsService.class);
        groupStatisticsService = provider.lookup(GroupDirectStatisticsService.class);
        meterDirectStatisticsService = provider.lookup(MeterDirectStatisticsService.class);
        nodeConnectorDirectStatisticsService = provider.lookup(NodeConnectorDirectStatisticsService.class);
        queueDirectStatisticsService = provider.lookup(QueueDirectStatisticsService.class);
    }

    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(GetGroupStatisticsInput input) {
        return groupStatisticsService.handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetQueueStatisticsOutput>> getQueueStatistics(GetQueueStatisticsInput input) {
        return queueDirectStatisticsService.handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsOutput>> getFlowStatistics(GetFlowStatisticsInput input) {
        return flowDirectStatisticsService.handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(GetMeterStatisticsInput input) {
        return meterDirectStatisticsService.handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(GetNodeConnectorStatisticsInput input) {
        return nodeConnectorDirectStatisticsService.handleAndReply(input);
    }
}
