/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetVlanVidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Unit tests for OF10StatsReplyMessageFactory.
 *
 * @author michal.polkorab
 */
public class OF10StatsReplyMessageFactoryTest {
    static final int DESC_STR_LEN = 256;
    static final int SERIAL_NUM_LEN = 32;

    private OFDeserializer<MultipartReplyMessage> statsFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        statsFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 17, MultipartReplyMessage.class));
    }

    /**
     * Testing OF10StatsReplyMessageFactory (Desc) for correct deserialization.
     */
    @Test
    public void testDesc() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 00");

        String mfrDesc = "Manufacturer description";
        byte[] mfrDescBytes = new byte[256];
        mfrDescBytes = mfrDesc.getBytes();
        bb.writeBytes(mfrDescBytes);
        bb.writeZero(DESC_STR_LEN - mfrDescBytes.length);

        String hwDesc = "Hardware description";
        byte[] hwDescBytes = new byte[256];
        hwDescBytes = hwDesc.getBytes();
        bb.writeBytes(hwDescBytes);
        bb.writeZero(DESC_STR_LEN - hwDescBytes.length);

        String swDesc = "Software description";
        byte[] swDescBytes = new byte[256];
        swDescBytes = swDesc.getBytes();
        bb.writeBytes(swDescBytes);
        bb.writeZero(DESC_STR_LEN - swDescBytes.length);

        String serialNum = "SN0123456789";
        byte[] serialNumBytes = new byte[32];
        serialNumBytes = serialNum.getBytes();
        bb.writeBytes(serialNumBytes);
        bb.writeZero(SERIAL_NUM_LEN - serialNumBytes.length);

        String dpDesc = "switch3 in room 3120";
        byte[] dpDescBytes = new byte[256];
        dpDescBytes = dpDesc.getBytes();
        bb.writeBytes(dpDescBytes);
        bb.writeZero(DESC_STR_LEN - dpDescBytes.length);

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(statsFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", false, builtByFactory.getFlags().getOFPMPFREQMORE().booleanValue());
        MultipartReplyDescCase messageCase = (MultipartReplyDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyDesc message = messageCase.getMultipartReplyDesc();
        Assert.assertEquals("Wrong mfrDesc", "Manufacturer description", message.getMfrDesc());
        Assert.assertEquals("Wrong hwDesc", "Hardware description", message.getHwDesc());
        Assert.assertEquals("Wrong swDesc", "Software description", message.getSwDesc());
        Assert.assertEquals("Wrong serialNum", "SN0123456789", message.getSerialNum());
        Assert.assertEquals("Wrong dpDesc", "switch3 in room 3120", message.getDpDesc());
        Assert.assertTrue("Unread data", bb.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsReplyMessageFactory (Flow) for correct deserialization.
     */
    @Test
    public void testFlow() {
        ByteBuf bb = BufferHelper.buildBuffer("00 01 00 01 00 68 01 00 "
                + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 02 00 00 00 03 00 04 00 05 00 06 00 00 00 00 00 00 "
                + "FF 01 02 03 04 05 06 07 FF 01 02 03 04 05 06 07 FF 00 00 00 00 00 00 20 "
                + "00 00 00 08 00 01 00 02 00 01 00 08 00 03 00 00");

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(statsFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0x01, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE().booleanValue());
        MultipartReplyFlowCase messageCase = (MultipartReplyFlowCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyFlow message = messageCase.getMultipartReplyFlow();
        Assert.assertEquals("Wrong tableId", 1, message.getFlowStats().get(0).getTableId().intValue());
        Assert.assertEquals("Wrong durationSec", 2, message.getFlowStats().get(0).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 3, message.getFlowStats().get(0).getDurationNsec().intValue());
        Assert.assertEquals("Wrong priority", 4, message.getFlowStats().get(0).getPriority().intValue());
        Assert.assertEquals("Wrong idleTimeOut", 5, message.getFlowStats().get(0).getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hardTimeOut", 6, message.getFlowStats().get(0).getHardTimeout().intValue());
        Assert.assertEquals("Wrong cookie", Uint64.valueOf("FF01020304050607", 16),
                message.getFlowStats().get(0).getCookie());
        Assert.assertEquals("Wrong packetCount", Uint64.valueOf("FF01020304050607", 16),
                message.getFlowStats().get(0).getPacketCount());
        Assert.assertEquals("Wrong byteCount", Uint64.valueOf("FF00000000000020", 16),
                message.getFlowStats().get(0).getByteCount());
        Action action1 = message.getFlowStats().get(0).getAction().get(0);
        Assert.assertTrue("Wrong action type", action1.getActionChoice() instanceof OutputActionCase);
        Assert.assertEquals("Wrong action port", 1, ((OutputActionCase) action1.getActionChoice())
                .getOutputAction().getPort().getValue().intValue());
        Assert.assertEquals("Wrong action port", 2, ((OutputActionCase) action1.getActionChoice())
                .getOutputAction().getMaxLength().intValue());
        Action action2 = message.getFlowStats().get(0).getAction().get(1);
        Assert.assertTrue("Wrong action type",action2.getActionChoice() instanceof SetVlanVidCase);
        Assert.assertEquals("Wrong action port", 3, ((SetVlanVidCase) action2.getActionChoice())
                .getSetVlanVidAction().getVlanVid().intValue());
        Assert.assertTrue("Unread data", bb.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsReplyMessageFactory (Aggregate) for correct deserialization.
     */
    @Test
    public void testAggregate() {
        ByteBuf bb = BufferHelper.buildBuffer("00 02 00 01 "
                + "FF 01 02 03 04 05 06 07 FF 00 00 00 00 00 00 20 00 00 00 30 00 00 00 00");

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(statsFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0x02, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE().booleanValue());
        MultipartReplyAggregateCase messageCase = (MultipartReplyAggregateCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyAggregate message = messageCase.getMultipartReplyAggregate();
        Assert.assertEquals("Wrong packet-count", Uint64.valueOf("FF01020304050607", 16), message.getPacketCount());
        Assert.assertEquals("Wrong byte-count", Uint64.valueOf("FF00000000000020", 16), message.getByteCount());
        Assert.assertEquals("Wrong flow-count", 48, message.getFlowCount().intValue());
        Assert.assertTrue("Unread data", bb.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsReplyMessageFactory (Table) for correct deserialization.
     */
    @Test
    public void testTable() {
        ByteBuf bb = BufferHelper.buildBuffer("00 03 00 01 "
                + "08 00 00 00 4A 41 4D 45 53 20 42 4F 4E 44 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
                + "00 00 00 30 00 00 00 10 FF 01 01 01 01 01 01 01 FF 01 01 01 01 01 01 00");

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(statsFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0x03, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());

        MultipartReplyTableCase messageCase = (MultipartReplyTableCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyTable message = messageCase.getMultipartReplyTable();
        Assert.assertEquals("Wrong tableId", 8, message.getTableStats().get(0).getTableId().intValue());
        Assert.assertEquals("Wrong name", "JAMES BOND", message.getTableStats().get(0).getName());
        Assert.assertEquals("Wrong wildcards", new FlowWildcardsV10(false, false, false, false, false, false, false,
                false, false, false), message.getTableStats().get(0).getWildcards());
        Assert.assertEquals("Wrong src-mask", 32, message.getTableStats().get(0).getNwSrcMask().intValue());
        Assert.assertEquals("Wrong dst-mask", 32, message.getTableStats().get(0).getNwDstMask().intValue());
        Assert.assertEquals("Wrong max-entries", 48, message.getTableStats().get(0).getMaxEntries().longValue());
        Assert.assertEquals("Wrong activeCount", 16, message.getTableStats().get(0).getActiveCount().longValue());
        Assert.assertEquals("Wrong lookupCount", Uint64.valueOf("FF01010101010101", 16),
                message.getTableStats().get(0).getLookupCount());
        Assert.assertEquals("Wrong matchedCount", Uint64.valueOf("FF01010101010100", 16),
                message.getTableStats().get(0).getMatchedCount());
        Assert.assertTrue("Unread data", bb.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsReplyMessageFactory (Port) for correct deserialization.
     */
    @Test
    public void testPort() {
        ByteBuf bb = BufferHelper.buildBuffer("00 04 00 01 "
                + "00 FF 00 00 00 00 00 00 "
                + "FF 01 01 01 01 01 01 01 FF 02 02 02 02 02 02 02 "
                + "FF 02 03 02 03 02 03 02 FF 02 03 02 03 02 03 02 "
                + "FF 02 03 02 03 02 03 02 FF 02 03 02 03 02 03 02 "
                + "FF 02 03 02 03 02 03 02 FF 02 03 02 03 02 03 02 "
                + "FF 02 03 02 03 02 03 02 FF 02 03 02 03 02 03 02 FF 02 03 02 03 02 03 02 "
                + "FF 02 03 02 03 02 03 02");

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(statsFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0x04, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyPortStatsCase messageCase = (MultipartReplyPortStatsCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyPortStats message = messageCase.getMultipartReplyPortStats();
        Assert.assertEquals("Wrong portNo", 255, message.getPortStats().get(0).getPortNo().intValue());
        Assert.assertEquals("Wrong rxPackets", Uint64.valueOf("FF01010101010101", 16),
                message.getPortStats().get(0).getRxPackets());
        Assert.assertEquals("Wrong txPackets", Uint64.valueOf("FF02020202020202", 16),
                message.getPortStats().get(0).getTxPackets());
        Assert.assertEquals("Wrong rxBytes", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getRxBytes());
        Assert.assertEquals("Wrong txBytes", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getTxBytes());
        Assert.assertEquals("Wrong rxDropped", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getRxDropped());
        Assert.assertEquals("Wrong txDropped", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getTxDropped());
        Assert.assertEquals("Wrong rxErrors", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getRxErrors());
        Assert.assertEquals("Wrong txErrors", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getTxErrors());
        Assert.assertEquals("Wrong rxFrameErr", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getRxFrameErr());
        Assert.assertEquals("Wrong rxOverErr", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getRxOverErr());
        Assert.assertEquals("Wrong rxCrcErr", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getRxCrcErr());
        Assert.assertEquals("Wrong collisions", Uint64.valueOf("FF02030203020302", 16),
                message.getPortStats().get(0).getCollisions());
        Assert.assertTrue("Unread data", bb.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsReplyMessageFactory (Queue) for correct deserialization.
     */
    @Test
    public void testQueue() {
        ByteBuf bb = BufferHelper.buildBuffer("00 05 00 00 "
                + "00 FF 00 00 00 00 00 10 "
                + "FF 02 03 02 03 02 03 02 "
                + "FF 02 02 02 02 02 02 02 "
                + "FF 02 03 02 03 02 03 02");

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(statsFactory, bb);

        BufferHelper.checkHeaderV10(builtByFactory);
        Assert.assertEquals("Wrong type", 0x05, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", false, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyQueueCase messageCase = (MultipartReplyQueueCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyQueue message = messageCase.getMultipartReplyQueue();
        Assert.assertEquals("Wrong portNo", 255, message.getQueueStats().get(0).getPortNo().intValue());
        Assert.assertEquals("Wrong queueId", 16, message.getQueueStats().get(0).getQueueId().intValue());
        Assert.assertEquals("Wrong txBytes", Uint64.valueOf("FF02030203020302", 16),
                message.getQueueStats().get(0).getTxBytes());
        Assert.assertEquals("Wrong txPackets", Uint64.valueOf("FF02020202020202", 16),
                message.getQueueStats().get(0).getTxPackets());
        Assert.assertEquals("Wrong txErrors", Uint64.valueOf("FF02030203020302", 16),
                message.getQueueStats().get(0).getTxErrors());
        Assert.assertTrue("Unread data", bb.readableBytes() == 0);
    }
}
