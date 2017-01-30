/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.singlelayer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.AbstractAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowAggregateStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class SingleLayerAggregateFlowMultipartService
    extends AbstractAggregateFlowMultipartService<MultipartReply> {

    public SingleLayerAggregateFlowMultipartService(final RequestContextStack requestContextStack,
                                                    final DeviceContext deviceContext,
                                                    final ConvertorExecutor convertorExecutor) {
        super(requestContextStack, deviceContext, convertorExecutor);
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> handleAndReply(
        final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        return Futures.transform(handleServiceCall(input),
            (Function<RpcResult<List<MultipartReply>>, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>>) result -> {
                if (Preconditions.checkNotNull(result).isSuccessful()) {
                    return RpcResultBuilder
                        .success(new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder()
                            .setAggregatedFlowStatistics(result
                                .getResult()
                                .stream()
                                .map(MultipartReply::getMultipartReplyBody)
                                .filter(MultipartReplyFlowAggregateStats.class::isInstance)
                                .map(multipartReplyBody ->
                                    new AggregatedFlowStatisticsBuilder(MultipartReplyFlowAggregateStats.class
                                        .cast(multipartReplyBody))
                                        .build())
                                .collect(Collectors.toList())))
                        .build();
                }

                return RpcResultBuilder
                    .<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>failed()
                    .withRpcErrors(result.getErrors())
                    .build();
            });
    }

}
