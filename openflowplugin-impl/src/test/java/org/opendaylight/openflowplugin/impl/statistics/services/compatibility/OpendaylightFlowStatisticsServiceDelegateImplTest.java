/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.impl.statistics.services.AbstractSingleStatsServiceTest;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link GetFlowStatisticsFromFlowTableImpl}.
 * Skipping notification verification. This will be tested in tests of underlying single task oriented services.
 */
public class OpendaylightFlowStatisticsServiceDelegateImplTest extends AbstractSingleStatsServiceTest {
    public static final int NOTIFICATION_WAIT_TIMEOUT_MS = 500;

    @Mock
    private MessageTranslator<Object, Object> translator;
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private GetFlowStatisticsFromFlowTableImpl getFlowStatisticsFromFlowTable;
    private GetAllFlowStatisticsFromFlowTableImpl getAllFlowStatisticsFromFlowTable;
    private GetAllFlowsStatisticsFromAllFlowTablesImpl getAllFlowsStatisticsFromAllFlowTables;
    private GetAggregateFlowStatisticsFromFlowTableForAllFlowsImpl getAggregateFlowStatisticsFromFlowTableForAllFlows;

    @Override
    public void setUp() {
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();
        final var xid = new AtomicLong(21);
        getFlowStatisticsFromFlowTable = new GetFlowStatisticsFromFlowTableImpl(
                rqContextStack, deviceContext, convertorManager, xid, notificationPublishService);
        getAllFlowStatisticsFromFlowTable = new GetAllFlowStatisticsFromFlowTableImpl(
            rqContextStack, deviceContext, convertorManager, xid, notificationPublishService);
        getAllFlowsStatisticsFromAllFlowTables = new GetAllFlowsStatisticsFromAllFlowTablesImpl(
            rqContextStack, deviceContext, convertorManager, xid, notificationPublishService);
        getAggregateFlowStatisticsFromFlowTableForAllFlows = new GetAggregateFlowStatisticsFromFlowTableForAllFlowsImpl(
            rqContextStack, deviceContext, convertorManager, xid, notificationPublishService);

        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));
        when(translatorLibrary.lookupTranslator(any())).thenReturn(translator);
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() throws Exception {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder input =
                new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE));

        when(translator.translate(any(MultipartReply.class), eq(deviceInfo),any()))
                .thenReturn(new AggregatedFlowStatisticsBuilder()
                        .setByteCount(new Counter64(Uint64.valueOf(50)))
                        .setPacketCount(new Counter64(Uint64.valueOf(51)))
                        .setFlowCount(new Counter32(Uint32.valueOf(52)))
                        .build());

        rpcResult = RpcResultBuilder.<Object>success(List.of(new MultipartReplyMessageBuilder()
                .setType(MultipartType.OFPMPAGGREGATE)
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setFlags(new MultipartRequestFlags(false))
                .setMultipartReplyBody(new MultipartReplyAggregateCaseBuilder()
                        .setMultipartReplyAggregate(new MultipartReplyAggregateBuilder()
                                .setByteCount(Uint64.valueOf(50))
                                .setPacketCount(Uint64.valueOf(51))
                                .setFlowCount(Uint32.valueOf(52))
                                .build())
                        .build())
                .build()))
                .build();

        final var resultFuture = getAggregateFlowStatisticsFromFlowTableForAllFlows.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResultCompatible = resultFuture.get();
        assertTrue(rpcResultCompatible.isSuccessful());
        assertEquals(MultipartType.OFPMPAGGREGATE, requestInput.getValue().getType());

        verify(notificationPublishService, timeout(NOTIFICATION_WAIT_TIMEOUT_MS))
                .offerNotification(any(Notification.class));
    }

    @Test
    public void testGetAllFlowStatisticsFromFlowTable() throws Exception {
        GetAllFlowStatisticsFromFlowTableInputBuilder input = new GetAllFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId(Uint8.ONE));

        rpcResult = buildFlowStatsReply();

        final var resultFuture = getAllFlowStatisticsFromFlowTable.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResultCompatible = resultFuture.get();
        assertTrue(rpcResultCompatible.isSuccessful());
        assertEquals(MultipartType.OFPMPFLOW, requestInput.getValue().getType());

        verify(notificationPublishService, timeout(NOTIFICATION_WAIT_TIMEOUT_MS))
                .offerNotification(any(Notification.class));
    }

    private static RpcResult<Object> buildFlowStatsReply() {
        return RpcResultBuilder.<Object>success(List.of(new MultipartReplyMessageBuilder()
                .setType(MultipartType.OFPMPFLOW)
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setFlags(new MultipartRequestFlags(false))
                .setMultipartReplyBody(new MultipartReplyFlowCaseBuilder()
                    .setMultipartReplyFlow(new MultipartReplyFlowBuilder()
                        .setFlowStats(List.of(new FlowStatsBuilder()
                            .setTableId(Uint8.valueOf(123))
                            .setDurationSec(Uint32.TEN)
                            .setDurationNsec(Uint32.valueOf(11))
                            .setByteCount(Uint64.valueOf(12))
                            .setPacketCount(Uint64.valueOf(13))
                            .setCookie(Uint64.ZERO)
                            .setPriority(Uint16.valueOf(14))
                            .setMatch(new MatchBuilder().setMatchEntry(List.of()).build())
                            .setHardTimeout(Uint16.valueOf(15))
                            .setIdleTimeout(Uint16.valueOf(16))
                            .setFlags(new FlowModFlags(true, false, false, false, false))
                            .setInstruction(List.of(new InstructionBuilder()
                                .setInstructionChoice(new ApplyActionsCaseBuilder()
                                    .setApplyActions(new ApplyActionsBuilder()
                                        .setAction(List.of(new ActionBuilder()
                                            .setActionChoice(new OutputActionCaseBuilder()
                                                .setOutputAction(new OutputActionBuilder()
                                                    .setMaxLength(Uint16.valueOf(17))
                                                    .setPort(new PortNumber(Uint32.valueOf(18)))
                                                    .build())
                                                .build())
                                            .build()))
                                        .build())
                                    .build())
                                .build()))
                            .build()))
                        .build())
                    .build())
                .build()))
            .build();
    }

    @Test
    public void testGetAllFlowsStatisticsFromAllFlowTables() throws Exception {
        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder input =
                new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildFlowStatsReply();

        final var resultFuture = getAllFlowsStatisticsFromAllFlowTables.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResultCompatible = resultFuture.get();
        assertTrue(rpcResultCompatible.isSuccessful());
        assertEquals(MultipartType.OFPMPFLOW, requestInput.getValue().getType());

        verify(notificationPublishService, timeout(NOTIFICATION_WAIT_TIMEOUT_MS))
                .offerNotification(any(Notification.class));
    }

    @Test
    public void testGetFlowStatisticsFromFlowTable() throws Exception {
        GetFlowStatisticsFromFlowTableInputBuilder input = new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(Uint8.ONE)
                .setPriority(Uint16.valueOf(123))
                .setOutPort(Uint64.ONE);

        rpcResult = buildFlowStatsReply();

        final var resultFuture = getFlowStatisticsFromFlowTable.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResultCompatible = resultFuture.get();
        assertTrue(rpcResultCompatible.isSuccessful());
        assertEquals(MultipartType.OFPMPFLOW, requestInput.getValue().getType());

        verify(notificationPublishService, timeout(NOTIFICATION_WAIT_TIMEOUT_MS))
                .offerNotification(any(Notification.class));
    }
}
