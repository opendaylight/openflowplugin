/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.compatibility;

import com.google.common.util.concurrent.FutureCallback;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.impl.statistics.services.AbstractSingleStatsServiceTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowStatisticsFromFlowTableOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetFlowStatisticsFromFlowTableOutput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
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
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link OpendaylightFlowStatisticsServiceDelegateImpl}.
 * Skipping notification verification. This will be tested in tests of underlying single task oriented services.
 */
public class OpendaylightFlowStatisticsServiceDelegateImplTest extends AbstractSingleStatsServiceTest {

    public static final int NOTIFICATION_WAIT_TIMEOUT_MS = 500;
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightFlowStatisticsServiceDelegateImpl flowStatisticsServiceDelegate;
    @Mock
    private MessageTranslator<Object, Object> translator;

    @Override
    public void setUp() {
        flowStatisticsServiceDelegate = new OpendaylightFlowStatisticsServiceDelegateImpl(
                rqContextStack, deviceContext, notificationPublishService, new AtomicLong(21));

        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));
        Mockito.when(translatorLibrary.lookupTranslator(Matchers.<TranslatorKey>any())).thenReturn(translator);
    }

    @Test(expected = IllegalAccessError.class)
    public void testGetAggregateFlowStatisticsFromFlowTableForGivenMatch() throws Exception {
        flowStatisticsServiceDelegate.getAggregateFlowStatisticsFromFlowTableForGivenMatch(null);
    }

    @Test
    public void testGetAggregateFlowStatisticsFromFlowTableForAllFlows() throws Exception {
        GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder input = new GetAggregateFlowStatisticsFromFlowTableForAllFlowsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId((short) 1));

        Mockito.when(translator.translate(Matchers.any(MultipartReply.class), Matchers.eq(deviceContext), Matchers.any()))
                .thenReturn(new AggregatedFlowStatisticsBuilder()
                        .setByteCount(new Counter64(BigInteger.valueOf(50L)))
                        .setPacketCount(new Counter64(BigInteger.valueOf(51L)))
                        .setFlowCount(new Counter32(52L))
                        .build());

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(new MultipartReplyMessageBuilder()
                .setType(MultipartType.OFPMPAGGREGATE)
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .setFlags(new MultipartRequestFlags(false))
                .setMultipartReplyBody(new MultipartReplyAggregateCaseBuilder()
                        .setMultipartReplyAggregate(new MultipartReplyAggregateBuilder()
                                .setByteCount(BigInteger.valueOf(50L))
                                .setPacketCount(BigInteger.valueOf(51L))
                                .setFlowCount(52L)
                                .build())
                        .build())
                .build()))
                .build();

        final Future<RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput>> resultFuture
                = flowStatisticsServiceDelegate.getAggregateFlowStatisticsFromFlowTableForAllFlows(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAggregateFlowStatisticsFromFlowTableForAllFlowsOutput> rpcResultCompatible = resultFuture.get();
        Assert.assertTrue(rpcResultCompatible.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPAGGREGATE, requestInput.getValue().getType());

        Mockito.verify(notificationPublishService, Mockito.timeout(NOTIFICATION_WAIT_TIMEOUT_MS)).offerNotification(Matchers.any(Notification.class));
    }

    @Test
    public void testGetAllFlowStatisticsFromFlowTable() throws Exception {
        GetAllFlowStatisticsFromFlowTableInputBuilder input = new GetAllFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId(new TableId((short) 1));

        rpcResult = buildFlowStatsReply();

        final Future<RpcResult<GetAllFlowStatisticsFromFlowTableOutput>> resultFuture
                = flowStatisticsServiceDelegate.getAllFlowStatisticsFromFlowTable(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllFlowStatisticsFromFlowTableOutput> rpcResultCompatible = resultFuture.get();
        Assert.assertTrue(rpcResultCompatible.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPFLOW, requestInput.getValue().getType());

        Mockito.verify(notificationPublishService, Mockito.timeout(NOTIFICATION_WAIT_TIMEOUT_MS)).offerNotification(Matchers.any(Notification.class));
    }

    private static RpcResult<Object> buildFlowStatsReply() {
        return RpcResultBuilder.<Object>success(Collections.singletonList(new MultipartReplyMessageBuilder()
                .setType(MultipartType.OFPMPFLOW)
                .setVersion(OFConstants.OFP_VERSION_1_3)
                .setFlags(new MultipartRequestFlags(false))
                .setMultipartReplyBody(new MultipartReplyFlowCaseBuilder()
                        .setMultipartReplyFlow(new MultipartReplyFlowBuilder()
                                .setFlowStats(Collections.singletonList(new FlowStatsBuilder()
                                        .setTableId((short) 123)
                                        .setDurationSec(10L)
                                        .setDurationNsec(11L)
                                        .setByteCount(BigInteger.valueOf(12L))
                                        .setPacketCount(BigInteger.valueOf(13L))
                                        .setCookie(BigInteger.ZERO)
                                        .setPriority(14)
                                        .setMatch(new MatchBuilder()
                                                .setMatchEntry(Collections.<MatchEntry>emptyList())
                                                .build())
                                        .setHardTimeout(15)
                                        .setIdleTimeout(16)
                                        .setFlags(new FlowModFlags(true, false, false, false, false))
                                        .setInstruction(Collections.singletonList(new InstructionBuilder()
                                                .setInstructionChoice(new ApplyActionsCaseBuilder()
                                                        .setApplyActions(new ApplyActionsBuilder()
                                                                .setAction(Collections.singletonList(new ActionBuilder()
                                                                        .setActionChoice(new OutputActionCaseBuilder()
                                                                                .setOutputAction(new OutputActionBuilder()
                                                                                        .setMaxLength(17)
                                                                                        .setPort(new PortNumber(18L))
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
        GetAllFlowsStatisticsFromAllFlowTablesInputBuilder input = new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildFlowStatsReply();

        final Future<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> resultFuture
                = flowStatisticsServiceDelegate.getAllFlowsStatisticsFromAllFlowTables(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput> rpcResultCompatible = resultFuture.get();
        Assert.assertTrue(rpcResultCompatible.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPFLOW, requestInput.getValue().getType());

        Mockito.verify(notificationPublishService, Mockito.timeout(NOTIFICATION_WAIT_TIMEOUT_MS)).offerNotification(Matchers.any(Notification.class));
    }

    @Test
    public void testGetFlowStatisticsFromFlowTable() throws Exception {
        GetFlowStatisticsFromFlowTableInputBuilder input = new GetFlowStatisticsFromFlowTableInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setTableId((short) 1)
                .setPriority(123)
                .setOutPort(BigInteger.ONE);

        rpcResult = buildFlowStatsReply();

        final Future<RpcResult<GetFlowStatisticsFromFlowTableOutput>> resultFuture
                = flowStatisticsServiceDelegate.getFlowStatisticsFromFlowTable(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetFlowStatisticsFromFlowTableOutput> rpcResultCompatible = resultFuture.get();
        Assert.assertTrue(rpcResultCompatible.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPFLOW, requestInput.getValue().getType());

        Mockito.verify(notificationPublishService, Mockito.timeout(NOTIFICATION_WAIT_TIMEOUT_MS)).offerNotification(Matchers.any(Notification.class));
    }
}