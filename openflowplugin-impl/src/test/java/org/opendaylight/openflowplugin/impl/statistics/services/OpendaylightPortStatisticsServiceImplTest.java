/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.google.common.util.concurrent.FutureCallback;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetNodeConnectorStatisticsOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Test for {@link OpendaylightPortStatisticsServiceImpl}.
 */
public class OpendaylightPortStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightPortStatisticsServiceImpl portStatisticsService;

    @Override
    public void setUp() {
        portStatisticsService = new OpendaylightPortStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService);

        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(42L), requestInput.capture(), any(FutureCallback.class));
    }

    @After
    public void tearDown() {
        Mockito.verify(notificationPublishService).offerNotification(ArgumentMatchers.any());
    }

    @Test
    public void testGetAllNodeConnectorsStatistics() throws Exception {
        GetAllNodeConnectorsStatisticsInputBuilder input = new GetAllNodeConnectorsStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildPortStatisticsReply();

        final Future<RpcResult<GetAllNodeConnectorsStatisticsOutput>> resultFuture
                = portStatisticsService.getAllNodeConnectorsStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllNodeConnectorsStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPPORTSTATS, requestInput.getValue().getType());
    }

    private static RpcResult<Object> buildPortStatisticsReply() {
        return RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyPortStatsCaseBuilder()
                                .setMultipartReplyPortStats(new MultipartReplyPortStatsBuilder()
                                        .setPortStats(Collections.singletonList(new PortStatsBuilder()
                                                .setDurationSec(Uint32.valueOf(90))
                                                .setDurationNsec(Uint32.valueOf(91))
                                                .setCollisions(Uint64.valueOf(92))
                                                .setPortNo(Uint32.valueOf(93))
                                                .setRxBytes(Uint64.valueOf(94))
                                                .setRxCrcErr(Uint64.valueOf(95))
                                                .setRxDropped(Uint64.valueOf(96))
                                                .setRxFrameErr(Uint64.valueOf(97))
                                                .setRxErrors(Uint64.valueOf(98))
                                                .setRxOverErr(Uint64.valueOf(99))
                                                .setRxPackets(Uint64.valueOf(100))
                                                .setTxBytes(Uint64.valueOf(94))
                                                .setTxDropped(Uint64.valueOf(96))
                                                .setTxErrors(Uint64.valueOf(98))
                                                .setTxPackets(Uint64.valueOf(98))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();
    }

    @Test
    public void testGetNodeConnectorStatistics() throws Exception {
        GetNodeConnectorStatisticsInputBuilder input = new GetNodeConnectorStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"));

        rpcResult = buildPortStatisticsReply();

        final Future<RpcResult<GetNodeConnectorStatisticsOutput>> resultFuture
                = portStatisticsService.getNodeConnectorStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetNodeConnectorStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPPORTSTATS, requestInput.getValue().getType());
    }
}