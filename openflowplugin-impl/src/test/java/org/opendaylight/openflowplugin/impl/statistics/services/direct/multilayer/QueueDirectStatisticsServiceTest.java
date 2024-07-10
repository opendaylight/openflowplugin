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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class QueueDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    private MultiGetQueueStatistics service;

    @Override
    public void setUp() {
        service = new MultiGetQueueStatistics(requestContextStack, deviceContext, convertorManager,
            multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final GetQueueStatisticsInput input = mock(GetQueueStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getQueueId()).thenReturn(new QueueId(Uint32.ONE));
        when(input.getNodeConnectorId()).thenReturn(new NodeConnectorId(NODE_ID + ":" + PORT_NO));

        final MultipartRequestQueueCase body = (MultipartRequestQueueCase) ((MultipartRequestInput) service
            .buildRequest(new Xid(Uint32.valueOf(42)), input))
            .getMultipartRequestBody();

        final MultipartRequestQueue queue = body.getMultipartRequestQueue();

        assertEquals(PORT_NO, queue.getPortNo());
        assertEquals(Uint32.ONE, queue.getQueueId());
    }

    @Override
    public void testBuildReply() {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyQueueCase queueCase = mock(MultipartReplyQueueCase.class);
        final MultipartReplyQueue queue = mock(MultipartReplyQueue.class);
        final QueueStats queueStat = mock(QueueStats.class);
        final List<QueueStats> queueStats = List.of(queueStat);
        final List<MultipartReply> input = List.of(reply);

        when(queue.getQueueStats()).thenReturn(queueStats);
        when(queue.nonnullQueueStats()).thenCallRealMethod();
        when(queueCase.getMultipartReplyQueue()).thenReturn(queue);
        when(reply.getMultipartReplyBody()).thenReturn(queueCase);

        when(queueStat.getPortNo()).thenReturn(PORT_NO);
        when(queueStat.getQueueId()).thenReturn(Uint32.ONE);
        when(queueStat.getTxBytes()).thenReturn(Uint64.ONE);
        when(queueStat.getTxErrors()).thenReturn(Uint64.ONE);
        when(queueStat.getTxPackets()).thenReturn(Uint64.ONE);
        when(queueStat.getDurationSec()).thenReturn(Uint32.ZERO);
        when(queueStat.getDurationNsec()).thenReturn(Uint32.ZERO);

        final GetQueueStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.nonnullQueueIdAndStatisticsMap().size() > 0);

        final QueueIdAndStatisticsMap map = output.nonnullQueueIdAndStatisticsMap().values().iterator().next();
        assertEquals(map.getQueueId().getValue(), Uint32.ONE);
        assertEquals(map.getNodeConnectorId(), nodeConnectorId);
    }

    @Test
    public void testStoreStatisticsBarePortNo() {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(Uint32.ONE));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("1"));
        when(map.key()).thenReturn(new QueueIdAndStatisticsMapKey(new NodeConnectorId("1"), new QueueId(Uint32.ONE)));

        final Map<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap> maps = BindingMap.of(map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.nonnullQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).orElseThrow().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }

    @Override
    public void testStoreStatistics() {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(Uint32.ONE));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("openflow:1:1"));
        when(map.key()).thenReturn(new QueueIdAndStatisticsMapKey(new NodeConnectorId("openflow:1:1"),
            new QueueId(Uint32.ONE)));

        final Map<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap> maps = BindingMap.of(map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.nonnullQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).orElseThrow().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
