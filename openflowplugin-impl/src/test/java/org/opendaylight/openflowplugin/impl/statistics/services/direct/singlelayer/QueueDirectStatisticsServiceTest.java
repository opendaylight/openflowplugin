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
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapKey;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class QueueDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    private SingleGetQueueStatistics service;

    @Override
    public void setUp() {
        service = new SingleGetQueueStatistics(requestContextStack, deviceContext, convertorManager,
            multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final var input = mock(GetQueueStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getQueueId()).thenReturn(new QueueId(Uint32.ONE));
        when(input.getNodeConnectorId()).thenReturn(nodeConnectorId);

        final var body = (MultipartRequestQueueStats) ((MultipartRequest) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), input))
            .getMultipartRequestBody();

        assertEquals(nodeConnectorId, body.getNodeConnectorId());
        assertEquals(Uint32.ONE, body.getQueueId().getValue());
    }

    @Override
    public void testBuildReply() {
        final var output = service.buildReply(List.of(new MultipartReplyBuilder()
            .setMultipartReplyBody(new MultipartReplyQueueStatsBuilder()
                .setQueueIdAndStatisticsMap(BindingMap.of(new QueueIdAndStatisticsMapBuilder()
                    .setQueueId(new QueueId(Uint32.ONE))
                    .setNodeConnectorId(new NodeConnectorId(PORT_NO.toString()))
                    .setTransmittedBytes(new Counter64(Uint64.ONE))
                    .setTransmissionErrors(new Counter64(Uint64.ONE))
                    .setTransmittedBytes(new Counter64(Uint64.ONE))
                    .build()))
                .build())
            .build()), true);
        assertTrue(output.nonnullQueueIdAndStatisticsMap().size() > 0);

        final var map = output.nonnullQueueIdAndStatisticsMap().values().iterator().next();
        assertEquals(map.getQueueId().getValue(), Uint32.ONE);
        assertEquals(map.getNodeConnectorId().getValue(), PORT_NO.toString());
    }

    @Test
    public void testStoreStatisticsBarePortNo() {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(Uint32.ONE));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("1"));
        when(map.key()).thenReturn(
            new QueueIdAndStatisticsMapKey(new NodeConnectorId("1"), new QueueId(Uint32.ONE)));

        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        Map<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap> stats = BindingMap.of(map);
        when(output.nonnullQueueIdAndStatisticsMap()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).orElseThrow().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }

    @Override
    public void testStoreStatistics() {
        final QueueIdAndStatisticsMap map = mock(QueueIdAndStatisticsMap.class);
        when(map.getQueueId()).thenReturn(new QueueId(Uint32.ONE));
        when(map.getNodeConnectorId()).thenReturn(new NodeConnectorId("openflow:1:1"));
        when(map.key()).thenReturn(
            new QueueIdAndStatisticsMapKey(new NodeConnectorId("openflow:1:1"), new QueueId(Uint32.ONE)));

        final Map<QueueIdAndStatisticsMapKey, QueueIdAndStatisticsMap> maps = BindingMap.of(map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.nonnullQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).orElseThrow().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
