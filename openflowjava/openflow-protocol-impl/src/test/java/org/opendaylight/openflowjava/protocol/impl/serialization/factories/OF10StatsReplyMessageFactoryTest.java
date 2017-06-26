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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.multipart.reply.flow.FlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStatsBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class OF10StatsReplyMessageFactoryTest {
    private OFSerializer<MultipartReplyMessage> factory;
    private static final byte MESSAGE_TYPE = 17;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, MultipartReplyMessage.class));
    }

    @Test
    public void testDescBodySerialize() throws Exception {
        MultipartReplyMessageBuilder builder;
        builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(0));
        MultipartReplyDescCaseBuilder descCase = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder desc = new MultipartReplyDescBuilder();
        desc.setMfrDesc("Test");
        desc.setHwDesc("Test");
        desc.setSwDesc("Test");
        desc.setSerialNum("12345");
        desc.setDpDesc("Test");
        descCase.setMultipartReplyDesc(desc.build());
        builder.setMultipartReplyBody(descCase.build());
        MultipartReplyMessage message = builder.build();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 1068);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPDESC.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        Assert.assertEquals("Wrong desc body", message.getMultipartReplyBody(), decodeDescBody(serializedBuffer));
    }

    @Test
    public void testFlowBodySerialize() throws Exception {
        MultipartReplyMessageBuilder builder;
        builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(1));
        MultipartReplyFlowCaseBuilder flowCase = new MultipartReplyFlowCaseBuilder();
        MultipartReplyFlowBuilder flow = new MultipartReplyFlowBuilder();
        flow.setFlowStats(createFlowStats());
        flowCase.setMultipartReplyFlow(flow.build());
        builder.setMultipartReplyBody(flowCase.build());
        MultipartReplyMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 108);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPFLOW.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        FlowStats flowStats = flow.getFlowStats().get(0);
        Assert.assertEquals("Wrong length", 96, serializedBuffer.readShort());
        Assert.assertEquals("Wrong Table ID", flowStats.getTableId().intValue(), serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(1);
        Assert.assertEquals("Wrong wildcards", 3678463, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong inPort", 58, serializedBuffer.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        serializedBuffer.readBytes(dlSrc);
        Assert.assertEquals("Wrong dlSrc", "01:01:01:01:01:01", ByteBufUtils.macAddressToString(dlSrc));
        byte[] dlDst = new byte[6];
        serializedBuffer.readBytes(dlDst);
        Assert.assertEquals("Wrong dlDst", "FF:FF:FF:FF:FF:FF", ByteBufUtils.macAddressToString(dlDst));
        Assert.assertEquals("Wrong dlVlan", 18, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong dlVlanPcp", 5, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(1);
        Assert.assertEquals("Wrong dlType", 42, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong nwTos", 4, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong nwProto", 7, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(2);
        Assert.assertEquals("Wrong nwSrc", 134744072, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong nwDst", 269488144, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong tpSrc", 6653, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong tpDst", 6633, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong duration sec", flowStats.getDurationSec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong duration nsec", flowStats.getDurationNsec().intValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong priority", flowStats.getPriority().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong idle timeout", flowStats.getIdleTimeout().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong hard timeout", flowStats.getHardTimeout().intValue(), serializedBuffer.readShort());
        serializedBuffer.skipBytes(6);
        Assert.assertEquals("Wrong cookie", flowStats.getCookie().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong Packet count", flowStats.getPacketCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong Byte count", flowStats.getByteCount().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong action type", 0, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong port", 42, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong maxlength", 50, serializedBuffer.readUnsignedShort());
    }

    @Test
    public void testAggregateBodySerialize() throws Exception {
        MultipartReplyMessageBuilder builder;
        builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(2));
        MultipartReplyAggregateCaseBuilder aggregateCase = new MultipartReplyAggregateCaseBuilder();
        MultipartReplyAggregateBuilder aggregate = new MultipartReplyAggregateBuilder();
        aggregate.setPacketCount(BigInteger.valueOf(1234L));
        aggregate.setByteCount(BigInteger.valueOf(1234L));
        aggregate.setFlowCount(1L);
        aggregateCase.setMultipartReplyAggregate(aggregate.build());
        builder.setMultipartReplyBody(aggregateCase.build());
        MultipartReplyMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 36);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPAGGREGATE.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        Assert.assertEquals("Wrong Packet count", 1234L, serializedBuffer.readLong());
        Assert.assertEquals("Wrong Byte count", 1234L, serializedBuffer.readLong());
        Assert.assertEquals("Wrong flow count", 1L, serializedBuffer.readInt());
        serializedBuffer.skipBytes(4);
    }

    @Test
    public void testTableBodySerialize() throws Exception {
        MultipartReplyMessageBuilder builder;
        builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(3));
        MultipartReplyTableCaseBuilder tableCase = new MultipartReplyTableCaseBuilder();
        MultipartReplyTableBuilder table = new MultipartReplyTableBuilder();
        table.setTableStats(createTableStats());
        tableCase.setMultipartReplyTable(table.build());
        builder.setMultipartReplyBody(tableCase.build());
        MultipartReplyMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 60);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPTABLE.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        Assert.assertEquals("Wrong table id", 1, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(3);
        Assert.assertEquals("Wrong name", "Table name", ByteBufUtils.decodeNullTerminatedString(serializedBuffer, 16));
        Assert.assertEquals("Wrong wildcards", 3145983, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong max entries", 1L, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong active count", 1L, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong lookup count", 1234L, serializedBuffer.readLong());
        Assert.assertEquals("Wrong matched count", 1234L, serializedBuffer.readLong());
    }

    @Test
    public void testPortStatsBodySerialize() throws Exception {
        MultipartReplyMessageBuilder builder;
        builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(4));
        MultipartReplyPortStatsCaseBuilder portStatsCase = new MultipartReplyPortStatsCaseBuilder();
        MultipartReplyPortStatsBuilder portStats = new MultipartReplyPortStatsBuilder();
        portStats.setPortStats(createPortStats());
        portStatsCase.setMultipartReplyPortStats(portStats.build());
        builder.setMultipartReplyBody(portStatsCase.build());
        MultipartReplyMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 118);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPPORTSTATS.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        MultipartReplyPortStatsCase body = (MultipartReplyPortStatsCase) message.getMultipartReplyBody();
        MultipartReplyPortStats messageOutput = body.getMultipartReplyPortStats();
        PortStats portStatsOutput = messageOutput.getPortStats().get(0);
        Assert.assertEquals("Wrong port no", portStatsOutput.getPortNo().intValue(), serializedBuffer.readInt());
        serializedBuffer.skipBytes(6);
        Assert.assertEquals("Wrong rx packets", portStatsOutput.getRxPackets().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx packets", portStatsOutput.getTxPackets().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx bytes", portStatsOutput.getRxBytes().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx bytes", portStatsOutput.getTxBytes().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx dropped", portStatsOutput.getRxDropped().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx dropped", portStatsOutput.getTxDropped().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx errors", portStatsOutput.getRxErrors().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx errors", portStatsOutput.getTxErrors().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx frame err", portStatsOutput.getRxFrameErr().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx over err", portStatsOutput.getRxOverErr().longValue(),
                serializedBuffer.readLong());
        Assert.assertEquals("Wrong rx crc err", portStatsOutput.getRxCrcErr().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong collisions", portStatsOutput.getCollisions().longValue(),
                serializedBuffer.readLong());
    }

    @Test
    public void testQueueBodySerialize() throws Exception {
        MultipartReplyMessageBuilder builder;
        builder = new MultipartReplyMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setFlags(new MultipartRequestFlags(true));
        builder.setType(MultipartType.forValue(5));
        MultipartReplyQueueCaseBuilder queueCase = new MultipartReplyQueueCaseBuilder();
        MultipartReplyQueueBuilder queue = new MultipartReplyQueueBuilder();
        queue.setQueueStats(createQueueStats());
        queueCase.setMultipartReplyQueue(queue.build());
        builder.setMultipartReplyBody(queueCase.build());
        MultipartReplyMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 44);
        Assert.assertEquals("Wrong type", MultipartType.OFPMPQUEUE.getIntValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong flags", message.getFlags(),
                createMultipartRequestFlags(serializedBuffer.readShort()));
        MultipartReplyQueueCase body = (MultipartReplyQueueCase) message.getMultipartReplyBody();
        MultipartReplyQueue messageOutput = body.getMultipartReplyQueue();
        QueueStats queueStats = messageOutput.getQueueStats().get(0);
        Assert.assertEquals("Wrong length", 32, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(2);
        Assert.assertEquals("Wrong queue id", queueStats.getQueueId().intValue(), serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong tx bytes", queueStats.getTxBytes().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx packets", queueStats.getTxPackets().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong tx errors", queueStats.getTxErrors().longValue(), serializedBuffer.readLong());
    }

    private static List<QueueStats> createQueueStats() {
        QueueStatsBuilder builder = new QueueStatsBuilder();
        builder.setQueueId(1L);
        builder.setTxBytes(BigInteger.valueOf(1L));
        builder.setTxPackets(BigInteger.valueOf(1L));
        builder.setTxErrors(BigInteger.valueOf(1L));
        List<QueueStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<PortStats> createPortStats() {
        PortStatsBuilder builder = new PortStatsBuilder();
        builder.setPortNo(1L);
        builder.setRxPackets(BigInteger.valueOf(1L));
        builder.setTxPackets(BigInteger.valueOf(1L));
        builder.setRxBytes(BigInteger.valueOf(1L));
        builder.setTxBytes(BigInteger.valueOf(1L));
        builder.setRxDropped(BigInteger.valueOf(1L));
        builder.setTxDropped(BigInteger.valueOf(1L));
        builder.setRxErrors(BigInteger.valueOf(1L));
        builder.setTxErrors(BigInteger.valueOf(1L));
        builder.setRxFrameErr(BigInteger.valueOf(1L));
        builder.setRxOverErr(BigInteger.valueOf(1L));
        builder.setRxCrcErr(BigInteger.valueOf(1L));
        builder.setCollisions(BigInteger.valueOf(1L));
        List<PortStats> list = new ArrayList<PortStats>();
        list.add(builder.build());
        return list;
    }

    private static List<TableStats> createTableStats() {
        TableStatsBuilder builder = new TableStatsBuilder();
        builder.setTableId((short) 1);
        builder.setName("Table name");
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true));
        builder.setMaxEntries(1L);
        builder.setActiveCount(1L);
        builder.setLookupCount(BigInteger.valueOf(1234L));
        builder.setMatchedCount(BigInteger.valueOf(1234L));
        List<TableStats> list = new ArrayList<>();
        list.add(builder.build());
        return list;
    }

    private static List<FlowStats> createFlowStats() {
        FlowStatsBuilder builder = new FlowStatsBuilder();
        builder.setTableId((short) 1);
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true));
        matchBuilder.setNwSrcMask((short) 0);
        matchBuilder.setNwDstMask((short) 0);
        matchBuilder.setInPort(58);
        matchBuilder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        matchBuilder.setDlDst(new MacAddress("ff:ff:ff:ff:ff:ff"));
        matchBuilder.setDlVlan(18);
        matchBuilder.setDlVlanPcp((short) 5);
        matchBuilder.setDlType(42);
        matchBuilder.setNwTos((short) 4);
        matchBuilder.setNwProto((short) 7);
        matchBuilder.setNwSrc(new Ipv4Address("8.8.8.8"));
        matchBuilder.setNwDst(new Ipv4Address("16.16.16.16"));
        matchBuilder.setTpSrc(6653);
        matchBuilder.setTpDst(6633);
        builder.setMatchV10(matchBuilder.build());
        builder.setDurationSec(1L);
        builder.setDurationNsec(2L);
        builder.setPriority(1);
        builder.setIdleTimeout(1);
        builder.setHardTimeout(1);
        builder.setCookie(BigInteger.valueOf(1234L));
        builder.setPacketCount(BigInteger.valueOf(1234L));
        builder.setByteCount(BigInteger.valueOf(1234L));
        List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(42L));
        outputBuilder.setMaxLength(50);
        caseBuilder.setOutputAction(outputBuilder.build());
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        builder.setAction(actions);
        List<FlowStats> list = new ArrayList<FlowStats>();
        list.add(builder.build());
        return list;
    }

    private static MultipartRequestFlags createMultipartRequestFlags(int input) {
        final Boolean one = ((input) & (1 << 0)) > 0;
        return new MultipartRequestFlags(one);
    }

    private static MultipartReplyDescCase decodeDescBody(ByteBuf output) {
        MultipartReplyDescCaseBuilder descCase = new MultipartReplyDescCaseBuilder();
        MultipartReplyDescBuilder desc = new MultipartReplyDescBuilder();
        byte[] mfrDesc = new byte[256];
        output.readBytes(mfrDesc);
        desc.setMfrDesc(new String(mfrDesc).trim());
        byte[] hwDesc = new byte[256];
        output.readBytes(hwDesc);
        desc.setHwDesc(new String(hwDesc).trim());
        byte[] swDesc = new byte[256];
        output.readBytes(swDesc);
        desc.setSwDesc(new String(swDesc).trim());
        byte[] serialNumber = new byte[32];
        output.readBytes(serialNumber);
        desc.setSerialNum(new String(serialNumber).trim());
        byte[] dpDesc = new byte[256];
        output.readBytes(dpDesc);
        desc.setDpDesc(new String(dpDesc).trim());
        descCase.setMultipartReplyDesc(desc.build());
        return descCase.build();
    }
}
