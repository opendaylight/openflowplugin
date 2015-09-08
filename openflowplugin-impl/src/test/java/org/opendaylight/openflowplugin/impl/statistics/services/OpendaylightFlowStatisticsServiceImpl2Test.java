/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
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

/**
 * Test for {@link OpendaylightFlowStatisticsServiceImpl} - only not delegated method
 */
public class OpendaylightFlowStatisticsServiceImpl2Test extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;
    @Mock
    private MessageTranslator<MultipartReply, AggregatedFlowStatistics> translator;

    private AbstractRequestContext<List<MultipartReply>> rqContextMp;

    private OpendaylightFlowStatisticsServiceImpl flowStatisticsService;


    public void setUp() {
        flowStatisticsService = new OpendaylightFlowStatisticsServiceImpl(rqContextStack, deviceContext);

        rqContextMp = new AbstractRequestContext<List<MultipartReply>>(42L) {
            @Override
            public void close() {
                //NOOP
            }
        };
        Mockito.when(rqContextStack.<List<MultipartReply>>createRequestContext()).thenReturn(rqContextMp);
        Mockito.when(translatorLibrary.<MultipartReply, AggregatedFlowStatistics>lookupTranslator(Matchers.any(TranslatorKey.class)))
                .thenReturn(translator);
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForGivenMatch() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));
        Mockito.doAnswer(new Answer<Void>() {
                             @Override
                             public Void answer(InvocationOnMock invocation) throws Throwable {
                                 final MultipartReplyMessageBuilder messageBuilder = new MultipartReplyMessageBuilder()
                                         .setVersion(OFConstants.OFP_VERSION_1_3);
                                 rqContextMp.setResult(RpcResultBuilder.success(
                                         Collections.<MultipartReply>singletonList(messageBuilder.build())).build());
                                 return null;
                             }
                         }
        ).when(multiMsgCollector).endCollecting(Matchers.any(EventIdentifier.class));
        Mockito.when(translator.translate(
                        Matchers.any(MultipartReply.class), Matchers.same(deviceContext), Matchers.isNull())
        ).thenReturn(new AggregatedFlowStatisticsBuilder().build());


        GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder input =
                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder()
                        .setNode(createNodeRef("unitProt:123"))
                        .setPriority(5)
                        .setTableId((short) 1);

        final Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> resultFuture
                = flowStatisticsService.getAggregateFlowStatisticsFromFlowTableForGivenMatch(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(1, rpcResult.getResult().getAggregatedFlowStatistics().size());
        Assert.assertEquals(MultipartType.OFPMPAGGREGATE, requestInput.getValue().getType());
    }


}