/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services.multilayer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.impl.services.AbstractAggregateFlowMultipartService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

public class MultiLayerAggregateFlowMultipartService extends AbstractAggregateFlowMultipartService<MultipartReply> {

    private final TranslatorLibrary translatorLibrary;

    public MultiLayerAggregateFlowMultipartService(final RequestContextStack requestContextStack,
                                                   final DeviceContext deviceContext,
                                                   final ConvertorExecutor convertorExecutor,
                                                   final TranslatorLibrary translatorLibrary) {
        super(requestContextStack, deviceContext, convertorExecutor);
        this.translatorLibrary = translatorLibrary;
    }

    @Override
    public Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> handleAndReply(final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input) {
        return Futures.transform(handleServiceCall(input),
            (Function<RpcResult<List<MultipartReply>>, RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>>) result -> {
                if (Preconditions.checkNotNull(result).isSuccessful()) {
                    final MessageTranslator<MultipartReply, AggregatedFlowStatistics> messageTranslator = translatorLibrary
                        .lookupTranslator(new TranslatorKey(getVersion(), MultipartReplyAggregateCase.class.getName()));

                    return RpcResultBuilder
                        .success(new GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutputBuilder()
                            .setAggregatedFlowStatistics(result
                                .getResult()
                                .stream()
                                .map(multipartReply -> messageTranslator
                                    .translate(multipartReply, getDeviceInfo(), null))
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
