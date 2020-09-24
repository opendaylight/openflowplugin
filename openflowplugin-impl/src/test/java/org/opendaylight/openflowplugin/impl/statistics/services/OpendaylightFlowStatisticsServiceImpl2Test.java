/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link OpendaylightFlowStatisticsServiceImpl} - only not delegated method.
 */
public class OpendaylightFlowStatisticsServiceImpl2Test extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;
    @Mock
    private MessageTranslator<MultipartReply, AggregatedFlowStatistics> translator;

    private AbstractRequestContext<List<MultipartReply>> rqContextMp;

    private OpendaylightFlowStatisticsServiceImpl flowStatisticsService;


    @Override
    public void setUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        flowStatisticsService = OpendaylightFlowStatisticsServiceImpl.createWithOook(rqContextStack,
                                                                                     deviceContext,
                                                                                     convertorManager);

        rqContextMp = new AbstractRequestContext<>(Uint32.valueOf(42L)) {
            @Override
            public void close() {
                //NOOP
            }
        };
        Mockito.when(rqContextStack.<List<MultipartReply>>createRequestContext()).thenReturn(rqContextMp);
        Mockito.when(translatorLibrary
                .<MultipartReply, AggregatedFlowStatistics>lookupTranslator(Mockito.any()))
                .thenReturn(translator);
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForGivenMatch() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(42L), requestInput.capture(), Mockito.<FutureCallback>any());
        Mockito.doAnswer((Answer<Void>) invocation -> {
            final MultipartReplyMessageBuilder messageBuilder = new MultipartReplyMessageBuilder()
                    .setVersion(EncodeConstants.OF_VERSION_1_3);

            rqContextMp.setResult(RpcResultBuilder
                    .success(Collections.<MultipartReply>singletonList(messageBuilder.build()))
                    .build());
            return null;
        }).when(multiMsgCollector).endCollecting(Mockito.any());
        Mockito.when(translator.translate(
                        Mockito.any(), same(deviceInfo), isNull())
        ).thenReturn(new AggregatedFlowStatisticsBuilder().build());

        GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder input =
                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder()
                        .setNode(createNodeRef("unitProt:123"))
                        .setPriority(Uint16.valueOf(5))
                        .setTableId(Uint8.ONE);

        final Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> resultFuture
                = flowStatisticsService.getAggregateFlowStatisticsFromFlowTableForGivenMatch(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getResult().getAggregatedFlowStatistics().size());
        Assert.assertEquals(MultipartType.OFPMPAGGREGATE, requestInput.getValue().getType());
    }
}