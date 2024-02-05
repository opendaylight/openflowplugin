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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetQueueStatisticsFromGivenPortInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

@Deprecated
public class OpendaylightQueueStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private GetAllQueuesStatisticsFromAllPortsImpl getAllQueuesStatisticsFromAllPorts;
    private GetAllQueuesStatisticsFromGivenPortImpl getAllQueuesStatisticsFromGivenPort;
    private GetQueueStatisticsFromGivenPortImpl getQueueStatisticsFromGivenPort;

    @Override
    public void setUp() {
        final var xid = new AtomicLong();
        getAllQueuesStatisticsFromAllPorts = new GetAllQueuesStatisticsFromAllPortsImpl(rqContextStack, deviceContext,
                xid, notificationPublishService);
        getAllQueuesStatisticsFromGivenPort = new GetAllQueuesStatisticsFromGivenPortImpl(rqContextStack, deviceContext,
            xid, notificationPublishService);
        getQueueStatisticsFromGivenPort = new GetQueueStatisticsFromGivenPortImpl(rqContextStack, deviceContext,
            xid, notificationPublishService);
    }

    @After
    public void tearDown() {
        verify(notificationPublishService).offerNotification(any());
    }

    @Test
    public void testGetAllQueuesStatisticsFromAllPorts() throws Exception {
        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        GetAllQueuesStatisticsFromAllPortsInputBuilder input = new GetAllQueuesStatisticsFromAllPortsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildQueueStatsReply();

        final var resultFuture = getAllQueuesStatisticsFromAllPorts.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }

    protected RpcResult<Object> buildQueueStatsReply() {
        return RpcResultBuilder.<Object>success(List.of(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyQueueCaseBuilder()
                                .setMultipartReplyQueue(new MultipartReplyQueueBuilder()
                                        .setQueueStats(List.of(new QueueStatsBuilder()
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
        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        GetAllQueuesStatisticsFromGivenPortInputBuilder input = new GetAllQueuesStatisticsFromGivenPortInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"));

        rpcResult = buildQueueStatsReply();

        final var resultFuture = getAllQueuesStatisticsFromGivenPort.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }

    @Test
    public void testGetQueueStatisticsFromGivenPort() throws Exception {
        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));

        GetQueueStatisticsFromGivenPortInputBuilder input = new GetQueueStatisticsFromGivenPortInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setNodeConnectorId(new NodeConnectorId("unitProt:123:321"))
                .setQueueId(new QueueId(Uint32.valueOf(21)));

        rpcResult = buildQueueStatsReply();

        final var resultFuture = getQueueStatisticsFromGivenPort.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPQUEUE, requestInput.getValue().getType());
    }
}