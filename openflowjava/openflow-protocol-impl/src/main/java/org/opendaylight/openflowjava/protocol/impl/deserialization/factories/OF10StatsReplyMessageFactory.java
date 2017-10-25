/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.CodeKeyMakerFactory;
import org.opendaylight.openflowjava.protocol.impl.util.ListDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.OF10MatchDeserializer;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.experimenter._case.MultipartReplyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;

/**
 * Translates StatsReply messages (OpenFlow v1.0)
 *
 * @author michal.polkorab
 */
public class OF10StatsReplyMessageFactory implements OFDeserializer<MultipartReplyMessage>,
        DeserializerRegistryInjector {

    private static final int DESC_STR_LEN = 256;
    private static final int SERIAL_NUM_LEN = 32;
    private static final byte PADDING_IN_FLOW_STATS_HEADER = 1;
    private static final byte PADDING_IN_FLOW_STATS_HEADER_02 = 6;
    private static final byte PADDING_IN_AGGREGATE_HEADER = 4;
    private static final byte PADDING_IN_TABLE_HEADER = 3;
    private static final byte MAX_TABLE_NAME_LENGTH = 32;
    private static final byte PADDING_IN_PORT_STATS_HEADER = 6;
    private static final byte PADDING_IN_QUEUE_HEADER = 2;
    private static final byte LENGTH_OF_FLOW_STATS = 88;
    private static final int TABLE_STATS_LENGTH = 64;
    private DeserializerRegistry registry;

    @Override
    public MultipartReplyMessage deserialize(ByteBuf rawMessage) {
        MultipartReplyMessageBuilder builder = new MultipartReplyMessageBuilder();
        builder.setVersion((short) EncodeConstants.OF10_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        int type = rawMessage.readUnsignedShort();
        builder.setType(MultipartType.forValue(type));
        builder.setFlags(new MultipartRequestFlags((rawMessage.readUnsignedShort() & 0x01) != 0));
        switch (MultipartType.forValue(type)) {
            case OFPMPDESC:
                builder.setMultipartReplyBody(setDesc(rawMessage));
                break;
            case OFPMPFLOW:
                builder.setMultipartReplyBody(setFlow(rawMessage));
                break;
            case OFPMPAGGREGATE:
                builder.setMultipartReplyBody(setAggregate(rawMessage));
                break;
            case OFPMPTABLE:
                builder.setMultipartReplyBody(setTable(rawMessage));
                break;
            case OFPMPPORTSTATS:
                builder.setMultipartReplyBody(setPortStats(rawMessage));
                break;
            case OFPMPQUEUE:
                builder.setMultipartReplyBody(setQueue(rawMessage));
                break;
            case OFPMPEXPERIMENTER:
                builder.setMultipartReplyBody(setExperimenter(rawMessage));
                break;
            default:
                break;
        }
        return builder.build();
    }

    private static MultipartReplyDescCase setDesc(ByteBuf input) {
        MultipartReplyDescCaseBuilder caseBuilder = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder descBuilder = new MultipartReplyDescBuilder();
        byte[] mfrDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(mfrDescBytes);
        String mfrDesc = new String(mfrDescBytes);
        descBuilder.setMfrDesc(mfrDesc.trim());
        byte[] hwDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(hwDescBytes);
        String hwDesc = new String(hwDescBytes);
        descBuilder.setHwDesc(hwDesc.trim());
        byte[] swDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(swDescBytes);
        String swDesc = new String(swDescBytes);
        descBuilder.setSwDesc(swDesc.trim());
        byte[] serialNumBytes = new byte[SERIAL_NUM_LEN];
        input.readBytes(serialNumBytes);
        String serialNum = new String(serialNumBytes);
        descBuilder.setSerialNum(serialNum.trim());
        byte[] dpDescBytes = new byte[DESC_STR_LEN];
        input.readBytes(dpDescBytes);
        String dpDesc = new String(dpDescBytes);
        descBuilder.setDpDesc(dpDesc.trim());
        caseBuilder.setMultipartReplyDesc(descBuilder.build());
        return caseBuilder.build();
    }

    private MultipartReplyFlowCase setFlow(ByteBuf input) {
        MultipartReplyFlowCaseBuilder caseBuilder = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flowBuilder = new MultipartReplyFlowBuilder();
        List<FlowStats> flowStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            FlowStatsBuilder flowStatsBuilder = new FlowStatsBuilder();
            int length = input.readUnsignedShort();
            flowStatsBuilder.setTableId(input.readUnsignedByte());
            input.skipBytes(PADDING_IN_FLOW_STATS_HEADER);
            OFDeserializer<MatchV10> matchDeserializer = registry.getDeserializer(
                    new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, EncodeConstants.EMPTY_VALUE, MatchV10.class));
            flowStatsBuilder.setMatchV10(matchDeserializer.deserialize(input));
            flowStatsBuilder.setDurationSec(input.readUnsignedInt());
            flowStatsBuilder.setDurationNsec(input.readUnsignedInt());
            flowStatsBuilder.setPriority(input.readUnsignedShort());
            flowStatsBuilder.setIdleTimeout(input.readUnsignedShort());
            flowStatsBuilder.setHardTimeout(input.readUnsignedShort());
            input.skipBytes(PADDING_IN_FLOW_STATS_HEADER_02);
            byte[] cookie = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(cookie);
            flowStatsBuilder.setCookie(new BigInteger(1, cookie));
            byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(packetCount);
            flowStatsBuilder.setPacketCount(new BigInteger(1, packetCount));
            byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(byteCount);
            flowStatsBuilder.setByteCount(new BigInteger(1, byteCount));
            CodeKeyMaker keyMaker = CodeKeyMakerFactory.createActionsKeyMaker(EncodeConstants.OF10_VERSION_ID);
            List<Action> actions = ListDeserializer.deserializeList(EncodeConstants.OF10_VERSION_ID,
                    length - LENGTH_OF_FLOW_STATS, input, keyMaker, registry);
            flowStatsBuilder.setAction(actions);
            flowStatsList.add(flowStatsBuilder.build());
        }
        flowBuilder.setFlowStats(flowStatsList);
        caseBuilder.setMultipartReplyFlow(flowBuilder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyAggregateCase setAggregate(ByteBuf input) {
        MultipartReplyAggregateCaseBuilder caseBuilder = new MultipartReplyAggregateCaseBuilder();
        MultipartReplyAggregateBuilder builder = new MultipartReplyAggregateBuilder();
        byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(packetCount);
        builder.setPacketCount(new BigInteger(1, packetCount));
        byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
        input.readBytes(byteCount);
        builder.setByteCount(new BigInteger(1, byteCount));
        builder.setFlowCount(input.readUnsignedInt());
        input.skipBytes(PADDING_IN_AGGREGATE_HEADER);
        caseBuilder.setMultipartReplyAggregate(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyTableCase setTable(ByteBuf input) {
        MultipartReplyTableCaseBuilder caseBuilder = new MultipartReplyTableCaseBuilder();
        MultipartReplyTableBuilder builder = new MultipartReplyTableBuilder();
        List<TableStats> tableStatsList = new ArrayList<>();
        // TODO - replace ">= TABLE_STATS_LENGTH" with "> 0" after fix in OVS switch
        while (input.readableBytes() >= TABLE_STATS_LENGTH) {
            TableStatsBuilder tableStatsBuilder = new TableStatsBuilder();
            tableStatsBuilder.setTableId(input.readUnsignedByte());
            input.skipBytes(PADDING_IN_TABLE_HEADER);
            tableStatsBuilder.setName(ByteBufUtils.decodeNullTerminatedString(input, MAX_TABLE_NAME_LENGTH));
            long wildcards = input.readUnsignedInt();
            tableStatsBuilder.setWildcards(OF10MatchDeserializer.createWildcards(wildcards));
            tableStatsBuilder.setNwSrcMask(OF10MatchDeserializer.decodeNwSrcMask(wildcards));
            tableStatsBuilder.setNwDstMask(OF10MatchDeserializer.decodeNwDstMask(wildcards));
            tableStatsBuilder.setMaxEntries(input.readUnsignedInt());
            tableStatsBuilder.setActiveCount(input.readUnsignedInt());
            byte[] lookupCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(lookupCount);
            tableStatsBuilder.setLookupCount(new BigInteger(1, lookupCount));
            byte[] matchedCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(matchedCount);
            tableStatsBuilder.setMatchedCount(new BigInteger(1, matchedCount));
            tableStatsList.add(tableStatsBuilder.build());
        }
        input.skipBytes(input.readableBytes());
        builder.setTableStats(tableStatsList);
        caseBuilder.setMultipartReplyTable(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyPortStatsCase setPortStats(ByteBuf input) {
        MultipartReplyPortStatsCaseBuilder caseBuilder = new MultipartReplyPortStatsCaseBuilder();
        MultipartReplyPortStatsBuilder builder = new MultipartReplyPortStatsBuilder();
        List<PortStats> portStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            PortStatsBuilder portStatsBuilder = new PortStatsBuilder();
            portStatsBuilder.setPortNo((long) input.readUnsignedShort());
            input.skipBytes(PADDING_IN_PORT_STATS_HEADER);
            byte[] rxPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxPackets);
            portStatsBuilder.setRxPackets(new BigInteger(1, rxPackets));
            byte[] txPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txPackets);
            portStatsBuilder.setTxPackets(new BigInteger(1, txPackets));
            byte[] rxBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxBytes);
            portStatsBuilder.setRxBytes(new BigInteger(1, rxBytes));
            byte[] txBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txBytes);
            portStatsBuilder.setTxBytes(new BigInteger(1, txBytes));
            byte[] rxDropped = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxDropped);
            portStatsBuilder.setRxDropped(new BigInteger(1, rxDropped));
            byte[] txDropped = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txDropped);
            portStatsBuilder.setTxDropped(new BigInteger(1, txDropped));
            byte[] rxErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxErrors);
            portStatsBuilder.setRxErrors(new BigInteger(1, rxErrors));
            byte[] txErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txErrors);
            portStatsBuilder.setTxErrors(new BigInteger(1, txErrors));
            byte[] rxFrameErr = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxFrameErr);
            portStatsBuilder.setRxFrameErr(new BigInteger(1, rxFrameErr));
            byte[] rxOverErr = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxOverErr);
            portStatsBuilder.setRxOverErr(new BigInteger(1, rxOverErr));
            byte[] rxCrcErr = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(rxCrcErr);
            portStatsBuilder.setRxCrcErr(new BigInteger(1, rxCrcErr));
            byte[] collisions = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(collisions);
            portStatsBuilder.setCollisions(new BigInteger(1, collisions));
            portStatsList.add(portStatsBuilder.build());
        }
        builder.setPortStats(portStatsList);
        caseBuilder.setMultipartReplyPortStats(builder.build());
        return caseBuilder.build();
    }

    private static MultipartReplyQueueCase setQueue(ByteBuf input) {
        MultipartReplyQueueCaseBuilder caseBuilder = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder builder = new MultipartReplyQueueBuilder();
        List<QueueStats> queueStatsList = new ArrayList<>();
        while (input.readableBytes() > 0) {
            QueueStatsBuilder queueStatsBuilder = new QueueStatsBuilder();
            queueStatsBuilder.setPortNo((long) input.readUnsignedShort());
            input.skipBytes(PADDING_IN_QUEUE_HEADER);
            queueStatsBuilder.setQueueId(input.readUnsignedInt());
            byte[] txBytes = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txBytes);
            queueStatsBuilder.setTxBytes(new BigInteger(1, txBytes));
            byte[] txPackets = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txPackets);
            queueStatsBuilder.setTxPackets(new BigInteger(1, txPackets));
            byte[] txErrors = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            input.readBytes(txErrors);
            queueStatsBuilder.setTxErrors(new BigInteger(1, txErrors));
            queueStatsList.add(queueStatsBuilder.build());
        }
        builder.setQueueStats(queueStatsList);
        caseBuilder.setMultipartReplyQueue(builder.build());
        return caseBuilder.build();
    }

    private MultipartReplyExperimenterCase setExperimenter(ByteBuf input) {
        final long expId = input.readUnsignedInt();
        final OFDeserializer<ExperimenterDataOfChoice> deserializer = registry.getDeserializer(ExperimenterDeserializerKeyFactory.createMultipartReplyVendorMessageDeserializerKey(
                EncodeConstants.OF10_VERSION_ID, expId));

        final MultipartReplyExperimenterBuilder mpExperimenterBld = new MultipartReplyExperimenterBuilder()
                .setExperimenter(new ExperimenterId(expId))
                .setExperimenterDataOfChoice(deserializer.deserialize(input));
        final MultipartReplyExperimenterCaseBuilder mpReplyExperimenterCaseBld = new MultipartReplyExperimenterCaseBuilder()
                .setMultipartReplyExperimenter(mpExperimenterBld.build());
        return mpReplyExperimenterCaseBld.build();
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
