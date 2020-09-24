/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.singlelayer;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.concurrent.Future;
import org.junit.Test;
import org.opendaylight.openflowplugin.impl.services.ServiceMocking;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowAggregateStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowAggregateStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class SingleLayerAggregateFlowMultipartServiceTest extends ServiceMocking {
    private static final Uint8 TABLE_ID = Uint8.valueOf(42);
    private static final Uint64 BYTE_COUNT = Uint64.valueOf(10);
    private SingleLayerAggregateFlowMultipartService service;

    @Override
    protected void setup() {
        service = new SingleLayerAggregateFlowMultipartService(mockedRequestContextStack, mockedDeviceContext);
    }

    @Test
    public void buildRequest() {
        final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input = new
                GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder()
                .setTableId(TABLE_ID)
                .build();

        final OfHeader ofHeader = service.buildRequest(DUMMY_XID, input);
        assertEquals(MultipartRequest.class, ofHeader.implementedInterface());

        final MultipartRequestFlowAggregateStats result =
            (MultipartRequestFlowAggregateStats) ((MultipartRequest) ofHeader)
                .getMultipartRequestBody();

        assertEquals(TABLE_ID, result.getFlowAggregateStats().getTableId());
    }

    @Test
    public void handleAndReply() throws Exception {
        mockSuccessfulFuture(Collections.singletonList(new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyFlowAggregateStatsBuilder()
                        .setByteCount(new Counter64(BYTE_COUNT))
                        .build())
                .build()));

        final GetAggregateFlowStatisticsFromFlowTableForGivenMatchInput input = new
                GetAggregateFlowStatisticsFromFlowTableForGivenMatchInputBuilder()
                .setTableId(TABLE_ID)
                .build();

        final Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>> rpcResultFuture = service
                .handleAndReply(input);

        final RpcResult<GetAggregateFlowStatisticsFromFlowTableForGivenMatchOutput>
                sendAggregateFlowMpRequestOutputRpcResult = rpcResultFuture.get();

        assertEquals(BYTE_COUNT, sendAggregateFlowMpRequestOutputRpcResult
                .getResult()
                .getAggregatedFlowStatistics()
                .get(0)
                .getByteCount().getValue());
    }
}
