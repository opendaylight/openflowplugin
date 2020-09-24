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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetFlowStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class FlowDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint8 TABLE_NO = Uint8.ONE;
    private FlowDirectStatisticsService service;

    @Override
    public void setUp() {
        final DeviceFlowRegistry registry = mock(DeviceFlowRegistry.class);
        service = new FlowDirectStatisticsService(requestContextStack,
                                                  deviceContext,
                                                  convertorManager,
                                                  multipartWriterProvider);
        when(registry.retrieveDescriptor(any())).thenReturn(FlowDescriptorFactory.create(TABLE_NO, new FlowId("1")));
        when(deviceContext.getDeviceFlowRegistry()).thenReturn(registry);
    }

    @Override
    public void testBuildRequestBody() {
        final GetFlowStatisticsInput input = mock(GetFlowStatisticsInput.class);

        when(input.getTableId()).thenReturn(TABLE_NO);

        final MultipartRequestFlowStats body = (MultipartRequestFlowStats) ((MultipartRequest) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), input))
            .getMultipartRequestBody();

        assertEquals(TABLE_NO, body.getFlowStats().getTableId());
    }

    @Override
    public void testBuildReply() {
        final FlowAndStatisticsMapList flowStat = new FlowAndStatisticsMapListBuilder()
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(Uint32.ONE))
                        .setNanosecond(new Counter32(Uint32.TWO))
                        .build())
                .setTableId(Uint8.valueOf(50))
                .setByteCount(new Counter64(Uint64.valueOf(7094)))
                .setPacketCount(new Counter64(Uint64.valueOf(63)))
                .setCookie(new FlowCookie(Uint64.valueOf(134419365)))
                .setFlags(new FlowModFlags(true, false, false, false, false))
                .setMatch(new MatchBuilder().setEthernetMatch(
                        new EthernetMatchBuilder()
                                .setEthernetDestination(
                                        new EthernetDestinationBuilder()
                                                .setAddress(new MacAddress("fa:16:3e:92:81:45"))
                                                .build()).build()).build())
                .setInstructions(new InstructionsBuilder().build())
                .setPriority(Uint16.valueOf(20))
                .build();

        final MultipartReply reply = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyFlowStatsBuilder()
                        .setFlowAndStatisticsMapList(Collections.singletonList(flowStat))
                        .build())
                .build();


        final FlowAndStatisticsMapList flowStat1 = new FlowAndStatisticsMapListBuilder()
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(Uint32.ONE))
                        .setNanosecond(new Counter32(Uint32.TWO))
                        .build())
                .setTableId(Uint8.valueOf(51))
                .setByteCount(new Counter64(Uint64.valueOf(9853)))
                .setPacketCount(new Counter64(Uint64.valueOf(99)))
                .setCookie(new FlowCookie(Uint64.valueOf(134550437)))
                .setFlags(new FlowModFlags(true, false, false, false, false))
                .setMatch(new MatchBuilder().setEthernetMatch(
                        new EthernetMatchBuilder()
                                .setEthernetDestination(
                                        new EthernetDestinationBuilder()
                                                .setAddress(new MacAddress("fa:16:3e:92:81:45"))
                                                .build()).build()).build())
                .setInstructions(new InstructionsBuilder().build())
                .setPriority(Uint16.valueOf(20))
                .build();

        final MultipartReply reply1 = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyFlowStatsBuilder()
                        .setFlowAndStatisticsMapList(Collections.singletonList(flowStat1))
                        .build())
                .build();
        final List<MultipartReply> input = new ArrayList<>();
        input.add(reply);
        input.add(reply1);
        final GetFlowStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getFlowAndStatisticsMapList().size() > 0);
        FlowAndStatisticsMap stats = output.nonnullFlowAndStatisticsMapList().iterator().next();
        assertEquals(Uint8.valueOf(50),stats.getTableId());
        stats = output.nonnullFlowAndStatisticsMapList().get(1);
        assertEquals(Uint8.valueOf(51),stats.getTableId());
    }

    @Override
    public void testStoreStatistics() {
        final FlowAndStatisticsMapList stat = mock(FlowAndStatisticsMapList.class);
        when(stat.getTableId()).thenReturn(TABLE_NO);
        when(stat.getMatch()).thenReturn(new MatchBuilder().build());

        final List<FlowAndStatisticsMapList> stats
                = Collections.singletonList(stat);
        final GetFlowStatisticsOutput output = mock(GetFlowStatisticsOutput.class);
        when(output.nonnullFlowAndStatisticsMapList()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPFLOW).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
