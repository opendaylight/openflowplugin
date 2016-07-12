/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerInitialization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
public class MultipartReplyTranslatorFirstTest extends ConvertorManagerInitialization {

    @Mock SwitchConnectionDistinguisher cookie;
    @Mock SessionContext sc;
    @Mock ConnectionConductor conductor;
    @Mock GetFeaturesOutput features;

    MultipartReplyTranslator translator;

    /**
     * Initializes mocks
     */
    @Before
    public void startUp() {
        translator = new MultipartReplyTranslator(getConvertorManager());
        when(sc.getPrimaryConductor()).thenReturn(conductor);
        when(conductor.getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with null and incorrect message
     */
    @Test
    public void test() {
        List<DataObject> list = translator.translate(cookie, sc, null);

        Assert.assertEquals("Wrong list size", 0, list.size());
        HelloMessageBuilder helloBuilder = new HelloMessageBuilder();
        list = translator.translate(cookie, sc, helloBuilder.build());
        Assert.assertEquals("Wrong list size", 0, list.size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with experimenter MultipartReply message
     */
    @Test
    public void testExperimenterCase() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPEXPERIMENTER);
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 0, list.size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty flow stats
     */
    @Test
    public void testEmptyFlowCase() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPFLOW);
        
        MultipartReplyFlowCaseBuilder caseBuilder = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flowBuilder = new MultipartReplyFlowBuilder();
        List<FlowStats> flowStats = new ArrayList<>();
        flowBuilder.setFlowStats(flowStats);
        caseBuilder.setMultipartReplyFlow(flowBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        FlowsStatisticsUpdate flowUpdate = (FlowsStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", flowUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, flowUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, flowUpdate.getTransactionId().getValue().intValue());
        List<FlowAndStatisticsMapList> mapList = flowUpdate.getFlowAndStatisticsMapList();
        Assert.assertEquals("Wrong flow stats size", 0, mapList.size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with experimenter MultipartReply message
     */
    @Test
    public void testFlowCase() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPFLOW);
        
        MultipartReplyFlowCaseBuilder caseBuilder = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flowBuilder = new MultipartReplyFlowBuilder();
        List<FlowStats> flowStats = new ArrayList<>();
        FlowStatsBuilder statsBuilder = new FlowStatsBuilder();
        statsBuilder.setTableId((short) 1);
        statsBuilder.setDurationSec(2L);
        statsBuilder.setDurationNsec(3L);
        statsBuilder.setPriority(4);
        statsBuilder.setIdleTimeout(5);
        statsBuilder.setHardTimeout(6);
        FlowModFlags flags = new FlowModFlags(true, false, true, false, true);
        statsBuilder.setFlags(flags);
        statsBuilder.setCookie(new BigInteger("7"));
        statsBuilder.setPacketCount(new BigInteger("8"));
        statsBuilder.setByteCount(new BigInteger("9"));
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setType(OxmMatchType.class);
        matchBuilder.setMatchEntry(new ArrayList<MatchEntry>());
        statsBuilder.setMatch(matchBuilder.build());
        statsBuilder.setInstruction(new ArrayList<Instruction>());
        flowStats.add(statsBuilder.build());
        statsBuilder = new FlowStatsBuilder();
        statsBuilder.setTableId((short) 10);
        statsBuilder.setDurationSec(20L);
        statsBuilder.setDurationNsec(30L);
        statsBuilder.setPriority(40);
        statsBuilder.setIdleTimeout(50);
        statsBuilder.setHardTimeout(60);
        flags = new FlowModFlags(false, true, false, true, false);
        statsBuilder.setFlags(flags);
        statsBuilder.setCookie(new BigInteger("70"));
        statsBuilder.setPacketCount(new BigInteger("80"));
        statsBuilder.setByteCount(new BigInteger("90"));
        matchBuilder = new MatchBuilder();
        matchBuilder.setType(OxmMatchType.class);
        matchBuilder.setMatchEntry(new ArrayList<MatchEntry>());
        statsBuilder.setMatch(matchBuilder.build());
        statsBuilder.setInstruction(new ArrayList<Instruction>());
        flowStats.add(statsBuilder.build());
        flowBuilder.setFlowStats(flowStats);
        caseBuilder.setMultipartReplyFlow(flowBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();

        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        FlowsStatisticsUpdate flowUpdate = (FlowsStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", flowUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, flowUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, flowUpdate.getTransactionId().getValue().intValue());
        List<FlowAndStatisticsMapList> mapList = flowUpdate.getFlowAndStatisticsMapList();
        Assert.assertEquals("Wrong flow stats size", 2, mapList.size());
        FlowAndStatisticsMapList stat = mapList.get(0);
        Assert.assertEquals("Wrong table-id", 1, stat.getTableId().intValue());
        Assert.assertEquals("Wrong duration sec", 2, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 3, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong priority", 4, stat.getPriority().intValue());
        Assert.assertEquals("Wrong idle-timeout", 5, stat.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hard-timeout", 6, stat.getHardTimeout().intValue());
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags expectedFlags = 
                new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                .FlowModFlags(!flags.isOFPFFCHECKOVERLAP(), !flags.isOFPFFNOBYTCOUNTS(), !flags.isOFPFFNOPKTCOUNTS(),
                        !flags.isOFPFFRESETCOUNTS(), !flags.isOFPFFSENDFLOWREM());
        Assert.assertEquals("Wrong flags", expectedFlags, stat.getFlags());
        Assert.assertEquals("Wrong cookie", 7, stat.getCookie().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 8, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 9, stat.getByteCount().getValue().intValue());
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder emptyMatchBuilder = 
                new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder();
        Match emptyMatch = emptyMatchBuilder.build();
        Assert.assertEquals("Wrong match", emptyMatch, stat.getMatch());
        Assert.assertEquals("Wrong instructions", 0, stat.getInstructions().getInstruction().size());
        stat = mapList.get(1);
        Assert.assertEquals("Wrong table-id", 10, stat.getTableId().intValue());
        Assert.assertEquals("Wrong duration sec", 20, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 30, stat.getDuration().getNanosecond().getValue().intValue());
        Assert.assertEquals("Wrong priority", 40, stat.getPriority().intValue());
        Assert.assertEquals("Wrong idle-timeout", 50, stat.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hard-timeout", 60, stat.getHardTimeout().intValue());
        expectedFlags = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                .FlowModFlags(flags.isOFPFFCHECKOVERLAP(), flags.isOFPFFNOBYTCOUNTS(), flags.isOFPFFNOPKTCOUNTS(),
                        flags.isOFPFFRESETCOUNTS(), flags.isOFPFFSENDFLOWREM());
        Assert.assertEquals("Wrong flags", expectedFlags, stat.getFlags());
        Assert.assertEquals("Wrong cookie", 70, stat.getCookie().getValue().intValue());
        Assert.assertEquals("Wrong packet count", 80, stat.getPacketCount().getValue().intValue());
        Assert.assertEquals("Wrong byte count", 90, stat.getByteCount().getValue().intValue());
        Assert.assertEquals("Wrong match", emptyMatch, stat.getMatch());
        Assert.assertEquals("Wrong instructions", 0, stat.getInstructions().getInstruction().size());
    }
}