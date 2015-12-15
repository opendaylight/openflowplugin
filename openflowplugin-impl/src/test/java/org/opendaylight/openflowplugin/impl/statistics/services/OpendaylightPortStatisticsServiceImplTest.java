/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.OFConstants;
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
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link OpendaylightPortStatisticsServiceImpl}
 */
public class OpendaylightPortStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightPortStatisticsServiceImpl portStatisticsService;

    public void setUp() {
        portStatisticsService = new OpendaylightPortStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService);

        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verify(notificationPublishService).offerNotification(Matchers.<Notification>any());
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
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyPortStatsCaseBuilder()
                                .setMultipartReplyPortStats(new MultipartReplyPortStatsBuilder()
                                        .setPortStats(Collections.singletonList(new PortStatsBuilder()
                                                .setDurationSec(90L)
                                                .setDurationNsec(91L)
                                                .setCollisions(BigInteger.valueOf(92L))
                                                .setPortNo(93L)
                                                .setRxBytes(BigInteger.valueOf(94L))
                                                .setRxCrcErr(BigInteger.valueOf(95L))
                                                .setRxDropped(BigInteger.valueOf(96L))
                                                .setRxFrameErr(BigInteger.valueOf(97L))
                                                .setRxErrors(BigInteger.valueOf(98L))
                                                .setRxOverErr(BigInteger.valueOf(99L))
                                                .setRxPackets(BigInteger.valueOf(100L))
                                                .setTxBytes(BigInteger.valueOf(94L))
                                                .setTxDropped(BigInteger.valueOf(96L))
                                                .setTxErrors(BigInteger.valueOf(98L))
                                                .setTxPackets(BigInteger.valueOf(98L))
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