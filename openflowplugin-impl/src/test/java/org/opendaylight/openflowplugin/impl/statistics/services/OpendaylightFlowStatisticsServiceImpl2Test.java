/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
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

    private GetAggregateFlowStatisticsFromFlowTableForGivenMatchImpl
        getAggregateFlowStatisticsFromFlowTableForGivenMatch;

    @Override
    public void setUp() {
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();
        getAggregateFlowStatisticsFromFlowTableForGivenMatch =
            new GetAggregateFlowStatisticsFromFlowTableForGivenMatchImpl(rqContextStack, deviceContext,
                convertorManager);

        rqContextMp = new AbstractRequestContext<>(Uint32.valueOf(42L)) {
            @Override
            public void close() {
                //NOOP
            }
        };
        when(rqContextStack.<List<MultipartReply>>createRequestContext()).thenReturn(rqContextMp);
        when(translatorLibrary.<MultipartReply, AggregatedFlowStatistics>lookupTranslator(any()))
                .thenReturn(translator);
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForGivenMatch() throws Exception {
        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any());
        doAnswer((Answer<Void>) invocation -> {
            final var messageBuilder = new MultipartReplyMessageBuilder().setVersion(EncodeConstants.OF_VERSION_1_3);

            rqContextMp.setResult(RpcResultBuilder
                    .success(Collections.<MultipartReply>singletonList(messageBuilder.build()))
                    .build());
            return null;
        }).when(multiMsgCollector).endCollecting(any());
        when(translator.translate(any(), same(deviceInfo), isNull()))
            .thenReturn(new AggregatedFlowStatisticsBuilder().build());

        final var input =
                new GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder()
                        .setNode(createNodeRef("unitProt:123"))
                        .setPriority(Uint16.valueOf(5))
                        .setTableId(Uint8.ONE);

        final var resultFuture = getAggregateFlowStatisticsFromFlowTableForGivenMatch.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(1, rpcResult.getResult().getAggregatedFlowStatistics().size());
        assertEquals(MultipartType.OFPMPAGGREGATE, requestInput.getValue().getType());
    }
}