/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetQueueStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapKey;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class QueueDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint32 QUEUE_NO = Uint32.ONE;
    private QueueDirectStatisticsService service;

    @Override
    public void setUp() {
        service = new QueueDirectStatisticsService(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final GetQueueStatisticsInput input = mock(GetQueueStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(input.getNodeConnectorId()).thenReturn(nodeConnectorId);

        final MultipartRequestQueueStats body = (MultipartRequestQueueStats) ((MultipartRequest) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), input))
            .getMultipartRequestBody();

        assertEquals(nodeConnectorId, body.getNodeConnectorId());
        assertEquals(QUEUE_NO, body.getQueueId().getValue());
    }

    @Override
    public void testBuildReply() {
        final QueueIdAndStatisticsMap queueStats = new QueueIdAndStatisticsMapBuilder()
                .setQueueId(new QueueId(QUEUE_NO))
                .setNodeConnectorId(new NodeConnectorId(PORT_NO.toString()))
                .setTransmittedBytes(new Counter64(Uint64.ONE))
                .setTransmissionErrors(new Counter64(Uint64.ONE))
                .setTransmittedBytes(new Counter64(Uint64.ONE))
                .build();

        final MultipartReply reply = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyQueueStatsBuilder()
                        .setQueueIdAndStatisticsMap(Collections.singletonMap(queueStats.key(), queueStats))
                        .build())
                .build();

        final List<MultipartReply> input = Collections.singletonList(reply);
        final GetQueueStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.nonnullQueueIdAndStatisticsMap().size() > 0);

        final QueueIdAndStatisticsMap map = output.nonnullQueueIdAndStatisticsMap().values().iterator().next();
        assertEquals(map.getQueueId().getValue(), QUEUE_NO);
        assertEquals(map.getNodeConnectorId().getValue(), PORT_NO.toString());
    }

    @Test
    public void testStoreStatisticsBarePortNo() {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("1"));
        when(map.key()).thenReturn(
            new QueueIdAndStatisticsMapKey(new NodeConnectorId("1"), new QueueId(QUEUE_NO)));

        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        Map<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap> stats = Collections.singletonMap(map.key(), map);
        when(output.nonnullQueueIdAndStatisticsMap()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }

    @Override
    public void testStoreStatistics() {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("openflow:1:1"));
        when(map.key()).thenReturn(
            new QueueIdAndStatisticsMapKey(new NodeConnectorId("openflow:1:1"), new QueueId(QUEUE_NO)));

        final Map<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap> maps = Collections.singletonMap(map.key(), map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.nonnullQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
