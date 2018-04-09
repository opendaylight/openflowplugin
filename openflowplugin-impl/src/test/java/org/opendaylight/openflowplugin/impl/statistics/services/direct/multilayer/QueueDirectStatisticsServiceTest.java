/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;

public class QueueDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Long QUEUE_NO = 1L;
    private QueueDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new QueueDirectStatisticsService(requestContextStack, deviceContext, convertorManager, multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetQueueStatisticsInput input = mock(GetQueueStatisticsInput.class);

        when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(input.getNodeConnectorId()).thenReturn(new NodeConnectorId(NODE_ID + ":" + PORT_NO));

        final MultipartRequestQueueCase body = (MultipartRequestQueueCase) ((MultipartRequestInput) service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        final MultipartRequestQueue queue = body.getMultipartRequestQueue();

        assertEquals(PORT_NO, queue.getPortNo());
        assertEquals(QUEUE_NO, queue.getQueueId());
    }

    @Override
    public void testBuildReply() throws Exception {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyQueueCase queueCase = mock(MultipartReplyQueueCase.class);
        final MultipartReplyQueue queue = mock(MultipartReplyQueue.class);
        final QueueStats queueStat = mock(QueueStats.class);
        final List<QueueStats> queueStats = Arrays.asList(queueStat);
        final List<MultipartReply> input = Arrays.asList(reply);

        when(queue.getQueueStats()).thenReturn(queueStats);
        when(queueCase.getMultipartReplyQueue()).thenReturn(queue);
        when(reply.getMultipartReplyBody()).thenReturn(queueCase);

        when(queueStat.getPortNo()).thenReturn(PORT_NO);
        when(queueStat.getQueueId()).thenReturn(QUEUE_NO);
        when(queueStat.getTxBytes()).thenReturn(BigInteger.ONE);
        when(queueStat.getTxErrors()).thenReturn(BigInteger.ONE);
        when(queueStat.getTxPackets()).thenReturn(BigInteger.ONE);

        final GetQueueStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getQueueIdAndStatisticsMap().size() > 0);

        final QueueIdAndStatisticsMap map = output.getQueueIdAndStatisticsMap().get(0);
        assertEquals(map.getQueueId().getValue(), QUEUE_NO);
        assertEquals(map.getNodeConnectorId(), nodeConnectorId);
    }

    @Test
    public void testStoreStatisticsBarePortNo() throws Exception {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("1"));

        final List<QueueIdAndStatisticsMap> maps = Collections.singletonList(map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.getQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }

    @Override
    public void testStoreStatistics() throws Exception {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("openflow:1:1"));

        final List<QueueIdAndStatisticsMap> maps = Arrays.asList(map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.getQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
