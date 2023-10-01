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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Test for {@link OpendaylightQueueStatisticsRpcs}.
 */
public class OpendaylightQueueStatisticsRpcsTest extends AbstractSingleStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightQueueStatisticsRpcs queueStatisticsService;

    @Override
    public void setUp() {
        queueStatisticsService = new OpendaylightQueueStatisticsRpcs(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService);
    }

    @After
    public void tearDown() {
        Mockito.verify(notificationPublishService).offerNotification(ArgumentMatchers.any());
    }

    @Test
    public void testGetAllQueuesStatisticsFromAllPorts() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        GetAllQueuesStatisticsFromAllPortsInputBuilder input = new GetAllQueuesStatisticsFromAllPortsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildQueueStatsReply();

        final Future<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> resultFuture
                = queueStatisticsService.getAllQueuesStatisticsFromAllPorts(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllQueuesStatisticsFromAllPortsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }

    protected RpcResult<Object> buildQueueStatsReply() {
        return RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyQueueCaseBuilder()
                                .setMultipartReplyQueue(new MultipartReplyQueueBuilder()
                                        .setQueueStats(Collections.singletonList(new QueueStatsBuilder()
                                                .setDurationSec(Uint32.valueOf(41))
                                                .setDurationNsec(Uint32.valueOf(42))
                                                .setTxBytes(Uint64.valueOf(43))
                                                .setTxErrors(Uint64.valueOf(44))
                                                .setTxPackets(Uint64.valueOf(45))
                                                .setPortNo(Uint32.valueOf(46))
                                                .setQueueId(Uint32.valueOf(47))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();
    }

    @Test
    public void testGetAllQueuesStatisticsFromGivenPort() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        GetAllQueuesStatisticsFromGivenPortInputBuilder input = new GetAllQueuesStatisticsFromGivenPortInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"));

        rpcResult = buildQueueStatsReply();

        final Future<RpcResult<GetAllQueuesStatisticsFromGivenPortOutput>> resultFuture
                = queueStatisticsService.getAllQueuesStatisticsFromGivenPort(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllQueuesStatisticsFromGivenPortOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }

    @Test
    public void testGetQueueStatisticsFromGivenPort() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        GetQueueStatisticsFromGivenPortInputBuilder input = new GetQueueStatisticsFromGivenPortInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"))
                .setQueueId(new QueueId(Uint32.valueOf(21)));

        rpcResult = buildQueueStatsReply();

        final Future<RpcResult<GetQueueStatisticsFromGivenPortOutput>> resultFuture
                = queueStatisticsService.getQueueStatisticsFromGivenPort(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetQueueStatisticsFromGivenPortOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }
}