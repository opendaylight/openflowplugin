/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ListSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMaker;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.experimenter._case.MultipartReplyExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class OF10StatsReplyMessageFactory implements OFSerializer<MultipartReplyMessage>, SerializerRegistryInjector {

    private SerializerRegistry registry;
    private static final byte MESSAGE_TYPE = 17;
    private static final byte FLOW_STATS_PADDING_1 = 1;
    private static final byte FLOW_STATS_PADDING_2 = 6;
    private static final TypeKeyMaker<Action> ACTION_KEY_MAKER = TypeKeyMakerFactory
            .createActionKeyMaker(EncodeConstants.OF10_VERSION_ID);
    private static final int FLOW_STATS_LENGTH_INDEX = 0;
    private static final int QUEUE_STATS_LENGTH_INDEX = 0;
    private static final byte AGGREGATE_PADDING = 4;
    private static final byte TABLE_PADDING = 3;
    private static final byte QUEUE_PADDING = 2;
    private static final byte PORT_STATS_PADDING = 6;

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

    @Override
    public void serialize(MultipartReplyMessage message, ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeShort(message.getType().getIntValue());
        writeFlags(message.getFlags(), outBuffer);
        switch (message.getType()) {
        case OFPMPDESC:
            serializeDescBody(message.getMultipartReplyBody(), outBuffer);
            break;
        case OFPMPFLOW:
            serializeFlowBody(message.getMultipartReplyBody(), outBuffer, message);
            break;
        case OFPMPAGGREGATE:
            serializeAggregateBody(message.getMultipartReplyBody(), outBuffer);
            break;
        case OFPMPTABLE:
            serializeTableBody(message.getMultipartReplyBody(), outBuffer);
            break;
        case OFPMPPORTSTATS:
            serializePortStatsBody(message.getMultipartReplyBody(), outBuffer);
            break;
        case OFPMPQUEUE:
            serializeQueueBody(message.getMultipartReplyBody(), outBuffer);
            break;
        case OFPMPEXPERIMENTER:
            serializeExperimenterBody(message.getMultipartReplyBody(), outBuffer);
            break;
        default:
            break;
        }
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private void serializeExperimenterBody(MultipartReplyBody body, ByteBuf outBuffer) {
        MultipartReplyExperimenterCase experimenterCase = (MultipartReplyExperimenterCase) body;
        MultipartReplyExperimenter experimenterBody = experimenterCase.getMultipartReplyExperimenter();
        // TODO: experimenterBody does not have get methods
    }

    private void serializeQueueBody(MultipartReplyBody body, ByteBuf outBuffer) {
        MultipartReplyQueueCase queueCase = (MultipartReplyQueueCase) body;
        MultipartReplyQueue queue = queueCase.getMultipartReplyQueue();
        for (QueueStats queueStats : queue.getQueueStats()) {
            ByteBuf queueStatsBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            queueStatsBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            queueStatsBuff.writeZero(QUEUE_PADDING);
            queueStatsBuff.writeInt(queueStats.getQueueId().intValue());
            queueStatsBuff.writeLong(queueStats.getTxBytes().longValue());
            queueStatsBuff.writeLong(queueStats.getTxPackets().longValue());
            queueStatsBuff.writeLong(queueStats.getTxErrors().longValue());
            queueStatsBuff.setShort(QUEUE_STATS_LENGTH_INDEX, queueStatsBuff.readableBytes());
            outBuffer.writeBytes(queueStatsBuff);
        }
    }

    private void serializePortStatsBody(MultipartReplyBody body, ByteBuf outBuffer) {
        MultipartReplyPortStatsCase portStatsCase = (MultipartReplyPortStatsCase) body;
        MultipartReplyPortStats portStats = portStatsCase.getMultipartReplyPortStats();
        for (PortStats portStat : portStats.getPortStats()) {
            outBuffer.writeInt(portStat.getPortNo().intValue());
            outBuffer.writeZero(PORT_STATS_PADDING);
            outBuffer.writeLong(portStat.getRxPackets().longValue());
            outBuffer.writeLong(portStat.getTxPackets().longValue());
            outBuffer.writeLong(portStat.getRxBytes().longValue());
            outBuffer.writeLong(portStat.getTxBytes().longValue());
            outBuffer.writeLong(portStat.getRxDropped().longValue());
            outBuffer.writeLong(portStat.getTxDropped().longValue());
            outBuffer.writeLong(portStat.getRxErrors().longValue());
            outBuffer.writeLong(portStat.getTxErrors().longValue());
            outBuffer.writeLong(portStat.getRxFrameErr().longValue());
            outBuffer.writeLong(portStat.getRxOverErr().longValue());
            outBuffer.writeLong(portStat.getRxCrcErr().longValue());
            outBuffer.writeLong(portStat.getCollisions().longValue());
        }
    }

    private void serializeTableBody(MultipartReplyBody body, ByteBuf outBuffer) {
        MultipartReplyTableCase tableCase = (MultipartReplyTableCase) body;
        MultipartReplyTable table = tableCase.getMultipartReplyTable();
        for (TableStats tableStats : table.getTableStats()) {
            outBuffer.writeByte(tableStats.getTableId());
            outBuffer.writeZero(TABLE_PADDING);
            write16String(tableStats.getName(), outBuffer);
            writeFlowWildcardsV10(tableStats.getWildcards(), outBuffer);
            outBuffer.writeInt(tableStats.getMaxEntries().intValue());
            outBuffer.writeInt(tableStats.getActiveCount().intValue());
            outBuffer.writeLong(tableStats.getLookupCount().longValue());
            outBuffer.writeLong(tableStats.getMatchedCount().longValue());
        }
    }

    private void writeFlowWildcardsV10(FlowWildcardsV10 feature, ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, feature.isINPORT());
        map.put(1, feature.isDLVLAN());
        map.put(2, feature.isDLSRC());
        map.put(3, feature.isDLDST());
        map.put(4, feature.isDLTYPE());
        map.put(5, feature.isNWPROTO());
        map.put(6, feature.isTPSRC());
        map.put(7, feature.isTPDST());
        map.put(20, feature.isDLVLANPCP());
        map.put(21, feature.isNWTOS());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeInt(bitmap);
    }

    private void serializeAggregateBody(MultipartReplyBody body, ByteBuf outBuffer) {
        MultipartReplyAggregateCase aggregateCase = (MultipartReplyAggregateCase) body;
        MultipartReplyAggregate aggregate = aggregateCase.getMultipartReplyAggregate();
        outBuffer.writeLong(aggregate.getPacketCount().longValue());
        outBuffer.writeLong(aggregate.getByteCount().longValue());
        outBuffer.writeInt(aggregate.getFlowCount().intValue());
        outBuffer.writeZero(AGGREGATE_PADDING);
    }

    private void serializeFlowBody(MultipartReplyBody body, ByteBuf outBuffer, MultipartReplyMessage message) {
        MultipartReplyFlowCase flowCase = (MultipartReplyFlowCase) body;
        MultipartReplyFlow flow = flowCase.getMultipartReplyFlow();
        for (FlowStats flowStats : flow.getFlowStats()) {
            ByteBuf flowStatsBuff = UnpooledByteBufAllocator.DEFAULT.buffer();
            flowStatsBuff.writeShort(EncodeConstants.EMPTY_LENGTH);
            flowStatsBuff.writeByte(new Long(flowStats.getTableId()).byteValue());
            flowStatsBuff.writeZero(FLOW_STATS_PADDING_1);
            OFSerializer<MatchV10> matchSerializer = registry
                    .getSerializer(new MessageTypeKey<>(message.getVersion(), MatchV10.class));
            matchSerializer.serialize(flowStats.getMatchV10(), flowStatsBuff);
            flowStatsBuff.writeInt(flowStats.getDurationSec().intValue());
            flowStatsBuff.writeInt(flowStats.getDurationNsec().intValue());
            flowStatsBuff.writeShort(flowStats.getPriority());
            flowStatsBuff.writeShort(flowStats.getIdleTimeout());
            flowStatsBuff.writeShort(flowStats.getHardTimeout());
            flowStatsBuff.writeZero(FLOW_STATS_PADDING_2);
            flowStatsBuff.writeLong(flowStats.getCookie().longValue());
            flowStatsBuff.writeLong(flowStats.getPacketCount().longValue());
            flowStatsBuff.writeLong(flowStats.getByteCount().longValue());
            ListSerializer.serializeList(flowStats.getAction(), ACTION_KEY_MAKER, registry, flowStatsBuff);
            flowStatsBuff.setShort(FLOW_STATS_LENGTH_INDEX, flowStatsBuff.readableBytes());
            outBuffer.writeBytes(flowStatsBuff);
        }
    }

    private void writeFlags(MultipartRequestFlags flags, ByteBuf outBuffer) {
        Map<Integer, Boolean> map = new HashMap<>();
        map.put(0, flags.isOFPMPFREQMORE());
        int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
        outBuffer.writeShort(bitmap);
    }

    private void serializeDescBody(MultipartReplyBody body, ByteBuf outBuffer) {
        MultipartReplyDescCase descCase = (MultipartReplyDescCase) body;
        MultipartReplyDesc desc = descCase.getMultipartReplyDesc();
        write256String(desc.getMfrDesc(), outBuffer);
        write256String(desc.getHwDesc(), outBuffer);
        write256String(desc.getSwDesc(), outBuffer);
        write32String(desc.getSerialNum(), outBuffer);
        write256String(desc.getDpDesc(), outBuffer);
    }

    private void write256String(String toWrite, ByteBuf outBuffer) {
        byte[] nameBytes = toWrite.getBytes();
        if (nameBytes.length < 256) {
            byte[] nameBytesPadding = new byte[256];
            int i = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[i] = b;
                i++;
            }
            for (; i < 256; i++) {
                nameBytesPadding[i] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }

    private void write16String(String toWrite, ByteBuf outBuffer) {
        byte[] nameBytes = toWrite.getBytes();
        if (nameBytes.length < 16) {
            byte[] nameBytesPadding = new byte[16];
            int i = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[i] = b;
                i++;
            }
            for (; i < 16; i++) {
                nameBytesPadding[i] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }

    private void write32String(String toWrite, ByteBuf outBuffer) {
        byte[] nameBytes = toWrite.getBytes();
        if (nameBytes.length < 32) {
            byte[] nameBytesPadding = new byte[32];
            int i = 0;
            for (byte b : nameBytes) {
                nameBytesPadding[i] = b;
                i++;
            }
            for (; i < 32; i++) {
                nameBytesPadding[i] = 0x0;
            }
            outBuffer.writeBytes(nameBytesPadding);
        } else {
            outBuffer.writeBytes(nameBytes);
        }
    }
}
