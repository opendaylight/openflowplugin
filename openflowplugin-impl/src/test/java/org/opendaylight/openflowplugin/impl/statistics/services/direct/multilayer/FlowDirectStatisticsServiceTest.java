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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class FlowDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint8 TABLE_NO = Uint8.ONE;
    private MultiGetFlowStatistics service;

    @Override
    public void setUp() {
        service = new MultiGetFlowStatistics(requestContextStack,
                                                  deviceContext,
                                                  convertorManager,
                                                  multipartWriterProvider);
        final DeviceFlowRegistry registry = mock(DeviceFlowRegistry.class);
        when(registry.retrieveDescriptor(any())).thenReturn(FlowDescriptorFactory.create(TABLE_NO, new FlowId("1")));
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(registry);
    }

    @Override
    public void testBuildRequestBody() {
        final var body = (MultipartRequestFlowCase) ((MultipartRequestInput) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), new GetFlowStatisticsInputBuilder()
                .setTableId(TABLE_NO)
                .build()))
            .getMultipartRequestBody();

        final var flow = body.getMultipartRequestFlow();

        assertEquals(TABLE_NO, flow.getTableId());
    }

    @Override
    public void testBuildReply() {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyFlowCase flowCase = mock(MultipartReplyFlowCase.class);
        final MultipartReplyFlow flow = mock(MultipartReplyFlow.class);

        when(flow.getFlowStats()).thenReturn(List.of(new FlowStatsBuilder()
            .setDurationSec(Uint32.ONE)
            .setDurationNsec(Uint32.ONE)
            .setTableId(TABLE_NO)
            .setByteCount(Uint64.ONE)
            .setPacketCount(Uint64.ONE)
            .setFlags(mock(FlowModFlags.class))
            .setMatch(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping
                .MatchBuilder().setMatchEntry(List.of()).build())
            .build()));
        when(flowCase.getMultipartReplyFlow()).thenReturn(flow);
        when(reply.getMultipartReplyBody()).thenReturn(flowCase);

        final var output = service.buildReply(List.of(reply), true);
        assertTrue(output.nonnullFlowAndStatisticsMapList().size() > 0);

        final var stats = output.nonnullFlowAndStatisticsMapList().iterator().next();

        assertEquals(stats.getTableId(), TABLE_NO);
    }

    @Override
    public void testStoreStatistics() {
        multipartWriterProvider.lookup(MultipartType.OFPMPFLOW).orElseThrow().write(new GetFlowStatisticsOutputBuilder()
            .setFlowAndStatisticsMapList(List.of(new FlowAndStatisticsMapListBuilder()
                .setTableId(TABLE_NO)
                .setMatch(new MatchBuilder().build())
                .build()))
            .build(), true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
