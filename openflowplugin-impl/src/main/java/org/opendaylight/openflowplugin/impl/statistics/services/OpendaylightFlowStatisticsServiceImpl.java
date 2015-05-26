/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author joe
 */
public class OpendaylightFlowStatisticsServiceImpl implements OpendaylightFlowStatisticsService {
    private static final Logger LOG = LoggerFactory.getLogger(OpendaylightFlowStatisticsServiceImpl.class);

    private final Function<RpcResult<List<MultipartReply>>, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> matchingConvertor =
            new Function<RpcResult<List<MultipartReply>>, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>>() {
                @Override
                public RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> apply(final RpcResult<List<MultipartReply>> input) {
                    final DeviceContext deviceContext = matchingFlowsInTable.getDeviceContext();
                    TranslatorLibrary translatorLibrary = deviceContext.oook();
                    RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> rpcResult;
                    if (input.isSuccessful()) {
                        MultipartReply reply = input.getResult().get(0);
                        final TranslatorKey translatorKey = new TranslatorKey(reply.getVersion(), MultipartReplyAggregateCase.class.getName());
                        final MessageTranslator<MultipartReply, AggregatedFlowStatistics> messageTranslator = translatorLibrary.lookupTranslator(translatorKey);
                        List<AggregatedFlowStatistics> aggregStats = new ArrayList<AggregatedFlowStatistics>();

                        for (MultipartReply multipartReply : input.getResult()) {
                            aggregStats.add(messageTranslator.translate(multipartReply, deviceContext, null));
                        }

                        GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder getAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder =
                                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder();
                        getAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder.setAggregatedFlowStatistics(aggregStats);

                        rpcResult = RpcResultBuilder
                                .<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>success()
                                .withResult(getAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder.build())
                                .build();

                    } else {
                        rpcResult = RpcResultBuilder
                                .<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>failed()
                                .withRpcErrors(input.getErrors())
                                .build();
                    }
                    return rpcResult;
                }
    };

    private final AggregateFlowsInTableService aggregateFlowsInTable;
    private final MatchingFlowsInTableService matchingFlowsInTable;
    private final AllFlowsInAllTablesService allFlowsInAllTables;
    private final AllFlowsInTableService allFlowsInTable;
    private final FlowsInTableService flowsInTable;

    public OpendaylightFlowStatisticsServiceImpl(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        aggregateFlowsInTable = new AggregateFlowsInTableService(requestContextStack, deviceContext);
        allFlowsInAllTables = new AllFlowsInAllTablesService(requestContextStack, deviceContext);
        allFlowsInTable = new AllFlowsInTableService(requestContextStack, deviceContext);
        flowsInTable = new FlowsInTableService(requestContextStack, deviceContext);
        matchingFlowsInTable = new MatchingFlowsInTableService(requestContextStack, deviceContext);
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> getAggregateFlowStatisticsFromFlowTableForAllFlows(
            final GetAggregateFlowStatisticsFromFlowTableForAllFlowsInput input) {
        return aggregateFlowsInTable.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> getAggregateFlowStatisticsFromFlowTableForGivenMatch(
            final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        return Futures.transform(matchingFlowsInTable.handleServiceCall(input), matchingConvertor);
    }

    @Override
    public Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> getAllFlowStatisticsFromFlowTable(
            final GetAllFlowStatisticsFromFlowTableInput input) {
        return allFlowsInTable.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> getAllFlowsStatisticsFromAllFlowTables(
            final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        return allFlowsInAllTables.handleServiceCall(input);
    }

    @Override
    public Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> getFlowStatisticsFromFlowTable(
            final GetFlowStatisticsFromFlowTableInput input) {
        return flowsInTable.handleServiceCall(input);
    }
}
