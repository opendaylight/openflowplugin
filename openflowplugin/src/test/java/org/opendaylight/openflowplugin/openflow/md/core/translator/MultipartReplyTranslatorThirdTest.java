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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumberValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author michal.polkorab
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MultipartReplyTranslatorThirdTest {

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
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        translator = new MultipartReplyTranslator(convertorManager);
        when(sc.getPrimaryConductor()).thenReturn(conductor);
        when(conductor.getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        when(sc.getFeatures()).thenReturn(features);
        when(features.getDatapathId()).thenReturn(new BigInteger("42"));
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty port stats
     */
    @Test
    public void testEmptyPortStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPPORTSTATS);
        
        MultipartReplyPortStatsCaseBuilder caseBuilder = new MultipartReplyPortStatsCaseBuilder();
        MultipartReplyPortStatsBuilder statsBuilder = new MultipartReplyPortStatsBuilder();
        List<PortStats> portStats = new ArrayList<>();
        statsBuilder.setPortStats(portStats);
        caseBuilder.setMultipartReplyPortStats(statsBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        NodeConnectorStatisticsUpdate statUpdate = (NodeConnectorStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong port stats size", 0, statUpdate.getNodeConnectorStatisticsAndPortNumberMap().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with port stats
     */
    @Test
    public void testPortStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPPORTSTATS);
        
        MultipartReplyPortStatsCaseBuilder caseBuilder = new MultipartReplyPortStatsCaseBuilder();
        MultipartReplyPortStatsBuilder statsBuilder = new MultipartReplyPortStatsBuilder();
        List<PortStats> portStats = new ArrayList<>();
        PortStatsBuilder builder = new PortStatsBuilder();
        builder.setPortNo(1L);
        builder.setRxPackets(new BigInteger("2"));
        builder.setTxPackets(new BigInteger("3"));
        builder.setRxBytes(new BigInteger("4"));
        builder.setTxBytes(new BigInteger("5"));
        builder.setRxDropped(new BigInteger("6"));
        builder.setTxDropped(new BigInteger("7"));
        builder.setRxErrors(new BigInteger("8"));
        builder.setTxErrors(new BigInteger("9"));
        builder.setRxFrameErr(new BigInteger("10"));
        builder.setRxOverErr(new BigInteger("11"));
        builder.setRxCrcErr(new BigInteger("12"));
        builder.setCollisions(new BigInteger("13"));
        builder.setDurationSec(14L);
        builder.setDurationNsec(15L);
        portStats.add(builder.build());
        builder = new PortStatsBuilder();
        builder.setPortNo(BinContent.intToUnsignedLong(PortNumberValues.CONTROLLER.getIntValue()));
        builder.setRxPackets(new BigInteger("20"));
        builder.setTxPackets(new BigInteger("30"));
        builder.setRxBytes(new BigInteger("40"));
        builder.setTxBytes(new BigInteger("50"));
        builder.setRxDropped(new BigInteger("60"));
        builder.setTxDropped(new BigInteger("70"));
        builder.setRxErrors(new BigInteger("80"));
        builder.setTxErrors(new BigInteger("90"));
        builder.setRxFrameErr(new BigInteger("100"));
        builder.setRxOverErr(new BigInteger("110"));
        builder.setRxCrcErr(new BigInteger("120"));
        builder.setCollisions(new BigInteger("130"));
        portStats.add(builder.build());
        statsBuilder.setPortStats(portStats);
        caseBuilder.setMultipartReplyPortStats(statsBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        NodeConnectorStatisticsUpdate statUpdate = (NodeConnectorStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong port stats size", 2, statUpdate.getNodeConnectorStatisticsAndPortNumberMap().size());
        NodeConnectorStatisticsAndPortNumberMap stat = statUpdate.getNodeConnectorStatisticsAndPortNumberMap().get(0);
        Assert.assertEquals("Wrong port number", "openflow:42:1", stat.getNodeConnectorId().getValue());
        Assert.assertEquals("Wrong rx packets", 2, stat.getPackets().getReceived().intValue());
        Assert.assertEquals("Wrong tx packets", 3, stat.getPackets().getTransmitted().intValue());
        Assert.assertEquals("Wrong rx bytes", 4, stat.getBytes().getReceived().intValue());
        Assert.assertEquals("Wrong tx bytes", 5, stat.getBytes().getTransmitted().intValue());
        Assert.assertEquals("Wrong rx dropped", 6, stat.getReceiveDrops().intValue());
        Assert.assertEquals("Wrong tx dropped", 7, stat.getTransmitDrops().intValue());
        Assert.assertEquals("Wrong rx errors", 8, stat.getReceiveErrors().intValue());
        Assert.assertEquals("Wrong tx errors", 9, stat.getTransmitErrors().intValue());
        Assert.assertEquals("Wrong rx frame error", 10, stat.getReceiveFrameError().intValue());
        Assert.assertEquals("Wrong rx over error", 11, stat.getReceiveOverRunError().intValue());
        Assert.assertEquals("Wrong rx crc error", 12, stat.getReceiveCrcError().intValue());
        Assert.assertEquals("Wrong collision count", 13, stat.getCollisionCount().intValue());
        Assert.assertEquals("Wrong duration sec", 14, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 15, stat.getDuration().getNanosecond().getValue().intValue());
        stat = statUpdate.getNodeConnectorStatisticsAndPortNumberMap().get(1);
        Assert.assertEquals("Wrong port number", "openflow:42:CONTROLLER", stat.getNodeConnectorId().getValue());
        Assert.assertEquals("Wrong rx packets", 20, stat.getPackets().getReceived().intValue());
        Assert.assertEquals("Wrong tx packets", 30, stat.getPackets().getTransmitted().intValue());
        Assert.assertEquals("Wrong rx bytes", 40, stat.getBytes().getReceived().intValue());
        Assert.assertEquals("Wrong tx bytes", 50, stat.getBytes().getTransmitted().intValue());
        Assert.assertEquals("Wrong rx dropped", 60, stat.getReceiveDrops().intValue());
        Assert.assertEquals("Wrong tx dropped", 70, stat.getTransmitDrops().intValue());
        Assert.assertEquals("Wrong rx errors", 80, stat.getReceiveErrors().intValue());
        Assert.assertEquals("Wrong tx errors", 90, stat.getTransmitErrors().intValue());
        Assert.assertEquals("Wrong rx frame error", 100, stat.getReceiveFrameError().intValue());
        Assert.assertEquals("Wrong rx over error", 110, stat.getReceiveOverRunError().intValue());
        Assert.assertEquals("Wrong rx crc error", 120, stat.getReceiveCrcError().intValue());
        Assert.assertEquals("Wrong collision count", 130, stat.getCollisionCount().intValue());
        Assert.assertEquals("Wrong duration sec", null, stat.getDuration().getSecond());
        Assert.assertEquals("Wrong duration n sec", null, stat.getDuration().getNanosecond());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty table stats
     */
    @Test
    public void testEmptyTableStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPTABLE);
        
        MultipartReplyTableCaseBuilder caseBuilder = new MultipartReplyTableCaseBuilder();
        MultipartReplyTableBuilder statsBuilder = new MultipartReplyTableBuilder();
        List<TableStats> tableStats = new ArrayList<>();
        statsBuilder.setTableStats(tableStats);
        caseBuilder.setMultipartReplyTable(statsBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        FlowTableStatisticsUpdate statUpdate = (FlowTableStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong table stats size", 0, statUpdate.getFlowTableAndStatisticsMap().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with table stats
     */
    @Test
    public void testTableStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPTABLE);
        
        MultipartReplyTableCaseBuilder caseBuilder = new MultipartReplyTableCaseBuilder();
        MultipartReplyTableBuilder statsBuilder = new MultipartReplyTableBuilder();
        List<TableStats> tableStats = new ArrayList<>();
        TableStatsBuilder builder = new TableStatsBuilder();
        builder.setTableId((short) 1);
        builder.setActiveCount(2L);
        builder.setLookupCount(new BigInteger("3"));
        builder.setMatchedCount(new BigInteger("4"));
        tableStats.add(builder.build());
        builder = new TableStatsBuilder();
        builder.setTableId((short) 10);
        builder.setActiveCount(20L);
        builder.setLookupCount(new BigInteger("30"));
        builder.setMatchedCount(new BigInteger("40"));
        tableStats.add(builder.build());
        statsBuilder.setTableStats(tableStats);
        caseBuilder.setMultipartReplyTable(statsBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        FlowTableStatisticsUpdate statUpdate = (FlowTableStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong table stats size", 2, statUpdate.getFlowTableAndStatisticsMap().size());
        FlowTableAndStatisticsMap stat = statUpdate.getFlowTableAndStatisticsMap().get(0);
        Assert.assertEquals("Wrong table-id", 1, stat.getTableId().getValue().intValue());
        Assert.assertEquals("Wrong active count", 2, stat.getActiveFlows().getValue().intValue());
        Assert.assertEquals("Wrong lookup count", 3, stat.getPacketsLookedUp().getValue().intValue());
        Assert.assertEquals("Wrong matched count", 4, stat.getPacketsMatched().getValue().intValue());
        stat = statUpdate.getFlowTableAndStatisticsMap().get(1);
        Assert.assertEquals("Wrong table-id", 10, stat.getTableId().getValue().intValue());
        Assert.assertEquals("Wrong active count", 20, stat.getActiveFlows().getValue().intValue());
        Assert.assertEquals("Wrong lookup count", 30, stat.getPacketsLookedUp().getValue().intValue());
        Assert.assertEquals("Wrong matched count", 40, stat.getPacketsMatched().getValue().intValue());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with empty queue stats
     */
    @Test
    public void testEmptyQueueStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPQUEUE);
        
        MultipartReplyQueueCaseBuilder caseBuilder = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder statsBuilder = new MultipartReplyQueueBuilder();
        List<QueueStats> queueStats = new ArrayList<>();
        statsBuilder.setQueueStats(queueStats);
        caseBuilder.setMultipartReplyQueue(statsBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        QueueStatisticsUpdate statUpdate = (QueueStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong queue stats size", 0, statUpdate.getQueueIdAndStatisticsMap().size());
    }

    /**
     * Test {@link MultipartReplyTranslator#translate(SwitchConnectionDistinguisher, SessionContext, OfHeader)}
     * with queue stats
     */
    @Test
    public void testQueueStats() {
        MultipartReplyMessageBuilder mpBuilder = new MultipartReplyMessageBuilder();
        mpBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        mpBuilder.setXid(123L);
        mpBuilder.setFlags(new MultipartRequestFlags(false));
        mpBuilder.setType(MultipartType.OFPMPQUEUE);
        
        MultipartReplyQueueCaseBuilder caseBuilder = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder statsBuilder = new MultipartReplyQueueBuilder();
        List<QueueStats> queueStats = new ArrayList<>();
        QueueStatsBuilder builder = new QueueStatsBuilder();
        builder.setPortNo(BinContent.intToUnsignedLong(PortNumberValues.FLOOD.getIntValue()));
        builder.setQueueId(2L);
        builder.setTxPackets(new BigInteger("3"));
        builder.setTxBytes(new BigInteger("4"));
        builder.setTxErrors(new BigInteger("5"));
        builder.setDurationSec(6L);
        builder.setDurationNsec(7L);
        queueStats.add(builder.build());
        builder = new QueueStatsBuilder();
        builder.setPortNo(BinContent.intToUnsignedLong(PortNumberValues.INPORT.getIntValue()));
        builder.setQueueId(20L);
        builder.setTxPackets(new BigInteger("30"));
        builder.setTxBytes(new BigInteger("40"));
        builder.setTxErrors(new BigInteger("50"));
        builder.setDurationSec(60L);
        builder.setDurationNsec(70L);
        queueStats.add(builder.build());
        statsBuilder.setQueueStats(queueStats);
        caseBuilder.setMultipartReplyQueue(statsBuilder.build());
        mpBuilder.setMultipartReplyBody(caseBuilder.build());
        MultipartReplyMessage message = mpBuilder.build();
        
        List<DataObject> list = translator.translate(cookie, sc, message);

        Assert.assertEquals("Wrong list size", 1, list.size());
        QueueStatisticsUpdate statUpdate = (QueueStatisticsUpdate) list.get(0);
        Assert.assertEquals("Wrong node-id", "openflow:42", statUpdate.getId().getValue());
        Assert.assertEquals("Wrong more-replies", false, statUpdate.isMoreReplies());
        Assert.assertEquals("Wrong transaction-id", 123, statUpdate.getTransactionId().getValue().intValue());
        Assert.assertEquals("Wrong queue stats size", 2, statUpdate.getQueueIdAndStatisticsMap().size());
        QueueIdAndStatisticsMap stat = statUpdate.getQueueIdAndStatisticsMap().get(0);
        Assert.assertEquals("Wrong port number", "openflow:42:FLOOD", stat.getNodeConnectorId().getValue());
        Assert.assertEquals("Wrong queue-id", 2, stat.getQueueId().getValue().intValue());
        Assert.assertEquals("Wrong tx packets", 3, stat.getTransmittedPackets().getValue().intValue());
        Assert.assertEquals("Wrong tx bytes", 4, stat.getTransmittedBytes().getValue().intValue());
        Assert.assertEquals("Wrong tx errors", 5, stat.getTransmissionErrors().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 6, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 7, stat.getDuration().getNanosecond().getValue().intValue());
        stat = statUpdate.getQueueIdAndStatisticsMap().get(1);
        Assert.assertEquals("Wrong port number", "openflow:42:INPORT", stat.getNodeConnectorId().getValue());
        Assert.assertEquals("Wrong queue-id", 20, stat.getQueueId().getValue().intValue());
        Assert.assertEquals("Wrong tx packets", 30, stat.getTransmittedPackets().getValue().intValue());
        Assert.assertEquals("Wrong tx bytes", 40, stat.getTransmittedBytes().getValue().intValue());
        Assert.assertEquals("Wrong tx errors", 50, stat.getTransmissionErrors().getValue().intValue());
        Assert.assertEquals("Wrong duration sec", 60, stat.getDuration().getSecond().getValue().intValue());
        Assert.assertEquals("Wrong duration n sec", 70, stat.getDuration().getNanosecond().getValue().intValue());
    }
}