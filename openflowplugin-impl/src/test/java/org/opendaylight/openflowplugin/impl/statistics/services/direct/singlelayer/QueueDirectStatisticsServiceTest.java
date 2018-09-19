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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
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

public class QueueDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Long QUEUE_NO = 1L;
    private QueueDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new QueueDirectStatisticsService(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetQueueStatisticsInput input = mock(GetQueueStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getQueueId()).thenReturn(new QueueId(QUEUE_NO));
        when(input.getNodeConnectorId()).thenReturn(nodeConnectorId);

        final MultipartRequestQueueStats body = (MultipartRequestQueueStats) ((MultipartRequest) service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        assertEquals(nodeConnectorId, body.getNodeConnectorId());
        assertEquals(QUEUE_NO, body.getQueueId().getValue());
    }

    @Override
    public void testBuildReply() throws Exception {
        final QueueIdAndStatisticsMap queueStats = new QueueIdAndStatisticsMapBuilder()
                .setQueueId(new QueueId(QUEUE_NO))
                .setNodeConnectorId(new NodeConnectorId(PORT_NO.toString()))
                .setTransmittedBytes(new Counter64(BigInteger.ONE))
                .setTransmissionErrors(new Counter64(BigInteger.ONE))
                .setTransmittedBytes(new Counter64(BigInteger.ONE))
                .build();

        final MultipartReply reply = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyQueueStatsBuilder()
                        .setQueueIdAndStatisticsMap(Collections.singletonList(queueStats))
                        .build())
                .build();

        final List<MultipartReply> input = Collections.singletonList(reply);
        final GetQueueStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getQueueIdAndStatisticsMap().size() > 0);

        final QueueIdAndStatisticsMap map = output.getQueueIdAndStatisticsMap().get(0);
        assertEquals(map.getQueueId().getValue(), QUEUE_NO);
        assertEquals(map.getNodeConnectorId().getValue(), PORT_NO.toString());
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

        final List<QueueIdAndStatisticsMap> maps = Collections.singletonList(map);
        final GetQueueStatisticsOutput output = mock(GetQueueStatisticsOutput.class);
        when(output.getQueueIdAndStatisticsMap()).thenReturn(maps);

        multipartWriterProvider.lookup(MultipartType.OFPMPQUEUE).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
