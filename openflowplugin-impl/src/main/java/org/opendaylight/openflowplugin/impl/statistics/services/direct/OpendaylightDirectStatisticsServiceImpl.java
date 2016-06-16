/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import java.util.Optional;
import java.util.concurrent.Future;
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
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * The Opendaylight direct statistics service.
 * This service handles RPC requests, sends them to registered handlers and returns their replies.
 */
public class OpendaylightDirectStatisticsServiceImpl implements OpendaylightDirectStatisticsService {
    private final OpendaylightDirectStatisticsServiceProvider provider;

    /**
     * Instantiates a new Opendaylight direct statistics service.
     *
     * @param provider the openflow direct statistics service provider
     */
    public OpendaylightDirectStatisticsServiceImpl(final OpendaylightDirectStatisticsServiceProvider provider) {
        this.provider = provider;
    }

    @Override
    public Future<RpcResult<GetGroupStatisticsOutput>> getGroupStatistics(GetGroupStatisticsInput input) {
        final Optional<GroupDirectStatisticsService> service = provider.lookup(GroupDirectStatisticsService.class);

        if (!service.isPresent()) {
            return missingImplementation(GroupDirectStatisticsService.class);
        }

        return service.get().handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetQueueStatisticsOutput>> getQueueStatistics(GetQueueStatisticsInput input) {
        final Optional<QueueDirectStatisticsService> service = provider.lookup(QueueDirectStatisticsService.class);

        if (!service.isPresent()) {
            return missingImplementation(QueueDirectStatisticsService.class);
        }

        return service.get().handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsOutput>> getFlowStatistics(GetFlowStatisticsInput input) {
        final Optional<FlowDirectStatisticsService> service = provider.lookup(FlowDirectStatisticsService.class);

        if (!service.isPresent()) {
            return missingImplementation(FlowDirectStatisticsService.class);
        }

        return service.get().handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetMeterStatisticsOutput>> getMeterStatistics(GetMeterStatisticsInput input) {
        final Optional<MeterDirectStatisticsService> service = provider.lookup(MeterDirectStatisticsService.class);

        if (!service.isPresent()) {
            return missingImplementation(MeterDirectStatisticsService.class);
        }

        return service.get().handleAndReply(input);
    }

    @Override
    public Future<RpcResult<GetNodeConnectorStatisticsOutput>> getNodeConnectorStatistics(GetNodeConnectorStatisticsInput input) {
        final Optional<NodeConnectorDirectStatisticsService> service = provider.lookup(NodeConnectorDirectStatisticsService.class);

        if (!service.isPresent()) {
            return missingImplementation(NodeConnectorDirectStatisticsService.class);
        }

        return service.get().handleAndReply(input);
    }

    private <T extends DataObject> Future<RpcResult<T>> missingImplementation(Class service) {
        return RpcResultBuilder.<T>failed().withError(
                RpcError.ErrorType.APPLICATION,
                String.format("No implementation found for direct statistics service %s.", service.getCanonicalName()))
                .buildFuture();
    }
}
