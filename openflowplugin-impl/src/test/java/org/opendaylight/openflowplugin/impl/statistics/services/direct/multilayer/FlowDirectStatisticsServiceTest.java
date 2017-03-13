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
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;

public class FlowDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Short TABLE_NO = 1;
    private FlowDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new FlowDirectStatisticsService(requestContextStack, deviceContext, convertorManager, multipartWriterProvider);
        final DeviceFlowRegistry registry = mock(DeviceFlowRegistry.class);
        when(registry.retrieveDescriptor(any())).thenReturn(FlowDescriptorFactory.create(TABLE_NO, new FlowId("1")));
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(registry);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetFlowStatisticsInput input = mock(GetFlowStatisticsInput.class);

        when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getTableId()).thenReturn(TABLE_NO);

        final MultipartRequestFlowCase body = (MultipartRequestFlowCase) ((MultipartRequestInput) service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        final MultipartRequestFlow flow = body.getMultipartRequestFlow();

        assertEquals(TABLE_NO, flow.getTableId());
    }

    @Override
    public void testBuildReply() throws Exception {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyFlowCase flowCase = mock(MultipartReplyFlowCase.class);
        final MultipartReplyFlow flow = mock(MultipartReplyFlow.class);
        final FlowStats flowStat = new FlowStatsBuilder()
                .setDurationSec(1L)
                .setDurationNsec(1L)
                .setTableId(TABLE_NO)
                .setByteCount(BigInteger.ONE)
                .setPacketCount(BigInteger.ONE)
                .setFlags(mock(FlowModFlags.class))
                .setMatch(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder()
                        .setMatchEntry(Collections.emptyList())
                        .build())
                .build();

        final List<FlowStats> flowStats = Collections.singletonList(flowStat);
        final List<MultipartReply> input = Collections.singletonList(reply);

        when(flow.getFlowStats()).thenReturn(flowStats);
        when(flowCase.getMultipartReplyFlow()).thenReturn(flow);
        when(reply.getMultipartReplyBody()).thenReturn(flowCase);

        final GetFlowStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getFlowAndStatisticsMapList().size() > 0);

        final FlowAndStatisticsMap stats = output.getFlowAndStatisticsMapList().get(0);

        assertEquals(stats.getTableId(), TABLE_NO);
    }

    @Override
    public void testStoreStatistics() throws Exception {
        final FlowAndStatisticsMapList stat = mock(FlowAndStatisticsMapList.class);
        when(stat.getTableId()).thenReturn(TABLE_NO);
        when(stat.getMatch()).thenReturn(new MatchBuilder().build());

        final List<FlowAndStatisticsMapList> stats = Arrays.asList(stat);
        final GetFlowStatisticsOutput output = mock(GetFlowStatisticsOutput.class);
        when(output.getFlowAndStatisticsMapList()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPFLOW).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
