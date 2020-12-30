/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for MultipartReplyMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class MultipartReplyMessageFactoryTest {
    static final int DESC_STR_LEN = 256;
    static final int SERIAL_NUM_LEN = 32;

    private OFDeserializer<MultipartReplyMessage> multipartFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        multipartFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 19, MultipartReplyMessage.class));
    }

    private static final Logger LOG = LoggerFactory.getLogger(MultipartReplyMessageFactoryTest.class);

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyDescBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 00 01 00 00 00 00");

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

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x00, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyDescCase messageCase = (MultipartReplyDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyDesc message = messageCase.getMultipartReplyDesc();
        Assert.assertEquals("Wrong mfrDesc", "Manufacturer description", message.getMfrDesc());
        Assert.assertEquals("Wrong hwDesc", "Hardware description", message.getHwDesc());
        Assert.assertEquals("Wrong swDesc", "Software description", message.getSwDesc());
        Assert.assertEquals("Wrong serialNum", "SN0123456789", message.getSerialNum());
        Assert.assertEquals("Wrong dpDesc", "switch3 in room 3120", message.getDpDesc());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyAggregateBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 02 00 01 00 00 00 00 "
                                            + "FF 01 01 01 01 01 01 01 " //packetCount
                                            + "0F 01 01 01 01 01 01 01 " //byteCount
                                            + "00 00 00 08 " //flowCount
                                            + "00 00 00 00" //pad
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x02, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyAggregateCase messageCase = (MultipartReplyAggregateCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyAggregate message = messageCase.getMultipartReplyAggregate();
        Assert.assertEquals("Wrong packetCount", Uint64.valueOf("FF01010101010101", 16), message.getPacketCount());
        Assert.assertEquals("Wrong byteCount", Uint64.valueOf("0F01010101010101", 16), message.getByteCount());
        Assert.assertEquals("Wrong flowCount", 8, message.getFlowCount().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyTableBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 03 00 01 00 00 00 00 "
                                            + "08 " //tableId
                                            + "00 00 00 " //pad
                                            + "00 00 00 10 " //activeCount
                                            + "FF 01 01 01 01 01 01 01 " //lookupCount
                                            + "AF 01 01 01 01 01 01 01"//matchedCount
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x03, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());

        MultipartReplyTableCase messageCase = (MultipartReplyTableCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyTable message = messageCase.getMultipartReplyTable();
        Assert.assertEquals("Wrong tableId", 8, message.getTableStats().get(0).getTableId().intValue());
        Assert.assertEquals("Wrong activeCount", 16, message.getTableStats().get(0).getActiveCount().longValue());
        Assert.assertEquals("Wrong lookupCount", Uint64.valueOf("FF01010101010101", 16),
                message.getTableStats().get(0).getLookupCount());
        Assert.assertEquals("Wrong matchedCount", Uint64.valueOf("AF01010101010101", 16),
                message.getTableStats().get(0).getMatchedCount());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyPortStatsBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 04 00 01 00 00 00 00 "
                                            + "00 00 00 FF " //portNo
                                            + "00 00 00 00 " //pad
                                            + "FF 01 01 01 01 01 01 01 " //rxPackets
                                            + "FF 02 02 02 02 02 02 02 " //txPackets
                                            + "FF 02 03 02 03 02 03 02 " //rxBytes
                                            + "FF 02 03 02 03 02 03 02 " //txBytes
                                            + "FF 02 03 02 03 02 03 02 " //rxDropped
                                            + "FF 02 03 02 03 02 03 02 " //txDropped
                                            + "FF 02 03 02 03 02 03 02 " //rxErrors
                                            + "FF 02 03 02 03 02 03 02 " //txErrors
                                            + "FF 02 03 02 03 02 03 02 " //rxFrameErr
                                            + "FF 02 03 02 03 02 03 02 " //rxOverErr
                                            + "FF 02 03 02 03 02 03 02 " //rxCrcErr
                                            + "FF 02 03 02 03 02 03 02 " //collisions
                                            + "00 00 00 02 " //durationSec
                                            + "00 00 00 04"//durationNsec
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
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
        Assert.assertEquals("Wrong durationSec", 2, message.getPortStats().get(0).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 4, message.getPortStats().get(0).getDurationNsec().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyQueueBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 05 00 01 00 00 00 00 "
                                            + "00 00 00 FF " //portNo
                                            + "00 00 00 10 " //queueId
                                            + "FF 02 03 02 03 02 03 02 " //txBytes
                                            + "FF 02 02 02 02 02 02 02 " //txPackets
                                            + "FF 02 03 02 03 02 03 02 " //txErrors
                                            + "00 00 00 02 " //durationSec
                                            + "00 00 00 04"//durationNsec
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x05, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
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
        Assert.assertEquals("Wrong durationSec", 2, message.getQueueStats().get(0).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 4, message.getQueueStats().get(0).getDurationNsec().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyGroupBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 06 00 01 00 00 00 00 "
                                            + "00 48 " //length
                                            + "00 00 " //pad1
                                            + "00 00 00 10 " //groupId
                                            + "00 00 00 12 " //refCount
                                            + "00 00 00 00 " //pad2
                                            + "FF 01 01 01 01 01 01 01 " //packetCount
                                            + "FF 01 01 01 01 01 01 01 " //byteCount
                                            + "00 00 00 08 " //durationSec
                                            + "00 00 00 09 " //durationNsec
                                            + "FF 01 01 01 01 01 01 01 " //packetCountBucket
                                            + "FF 01 01 01 01 01 01 01 " //byteCountBucket
                                            + "FF 02 02 02 02 02 02 02 " //packetCountBucket_2
                                            + "FF 02 02 02 02 02 02 02 " //byteCountBucket_2
                                            + "00 48 " //length_2
                                            + "00 00 " //pad1.2
                                            + "00 00 00 10 " //groupId_2
                                            + "00 00 00 12 " //refCount_2
                                            + "00 00 00 00 " //pad2.2
                                            + "FF 01 01 01 01 01 01 01 " //packetCount_2
                                            + "FF 01 01 01 01 01 01 01 " //byteCount_2
                                            + "00 00 00 08 " //durationSec_2
                                            + "00 00 00 09 " //durationNsec_2
                                            + "FF 01 01 01 01 01 01 01 " //packetCountBucket_1.2
                                            + "FF 01 01 01 01 01 01 01 " //byteCountBucket_1.2
                                            + "FF 02 02 02 02 02 02 02 " //packetCountBucket_2.2
                                            + "FF 02 02 02 02 02 02 02"//byteCountBucket_2.2
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 0x06, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyGroupCase messageCase = (MultipartReplyGroupCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroup message = messageCase.getMultipartReplyGroup();
        Assert.assertEquals("Wrong groupId", 16, message.getGroupStats().get(0).getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong refCount", 18, message.getGroupStats().get(0).getRefCount().intValue());
        Assert.assertEquals("Wrong packetCount", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(0).getPacketCount());
        Assert.assertEquals("Wrong byteCount", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(0).getByteCount());
        Assert.assertEquals("Wrong durationSec", 8, message.getGroupStats().get(0).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 9, message.getGroupStats().get(0).getDurationNsec().intValue());
        Assert.assertEquals("Wrong packetCountBucket", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(0).getBucketStats().get(0).getPacketCount());
        Assert.assertEquals("Wrong byteCountBucket", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(0).getBucketStats().get(0).getByteCount());
        Assert.assertEquals("Wrong packetCountBucket_2", Uint64.valueOf("FF02020202020202", 16),
                message.getGroupStats().get(0).getBucketStats().get(1).getPacketCount());
        Assert.assertEquals("Wrong byteCountBucket_2", Uint64.valueOf("FF02020202020202", 16),
                message.getGroupStats().get(0).getBucketStats().get(1).getByteCount());

        Assert.assertEquals("Wrong groupId_2", 16, message.getGroupStats().get(1).getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong refCount_2", 18, message.getGroupStats().get(1).getRefCount().intValue());
        Assert.assertEquals("Wrong packetCount_2", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(1).getPacketCount());
        Assert.assertEquals("Wrong byteCount_2", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(1).getByteCount());
        Assert.assertEquals("Wrong durationSec_2", 8, message.getGroupStats().get(1).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec_2", 9, message.getGroupStats().get(1).getDurationNsec().intValue());
        Assert.assertEquals("Wrong packetCountBucket_1.2", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(1).getBucketStats().get(0).getPacketCount());
        Assert.assertEquals("Wrong byteCountBucket_1.2", Uint64.valueOf("FF01010101010101", 16),
                message.getGroupStats().get(1).getBucketStats().get(0).getByteCount());
        Assert.assertEquals("Wrong packetCountBucket_2.2", Uint64.valueOf("FF02020202020202", 16),
                message.getGroupStats().get(1).getBucketStats().get(1).getPacketCount());
        Assert.assertEquals("Wrong byteCountBucket_2.2", Uint64.valueOf("FF02020202020202", 16),
                message.getGroupStats().get(1).getBucketStats().get(1).getByteCount());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyMeterBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 09 00 01 00 00 00 00 "
                                            + "00 00 00 09 " //meterId
                                            + "00 58 " //len
                                            + "00 00 00 00 00 00 " //pad
                                            + "00 00 00 07 " //flowCount
                                            + "FF 01 01 01 01 01 01 01 " //packetInCount
                                            + "FF 01 01 01 01 01 01 01 " //byteInCount
                                            + "00 00 00 05 " //durationSec
                                            + "00 00 00 05 " //durationNsec
                                            + "FF 01 01 01 01 01 01 01 " //packetBandCount_01
                                            + "FF 01 01 01 01 01 01 01 " //byteBandCount_01
                                            + "FF 02 02 02 02 02 02 02 " //packetBandCount_02
                                            + "FF 02 02 02 02 02 02 02 " //byteBandCount_02
                                            + "FF 03 03 03 03 03 03 03 " //packetBandCount_03
                                            + "FF 03 03 03 03 03 03 03" //byteBandCount_03
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 9, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyMeterCase messageCase = (MultipartReplyMeterCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyMeter message = messageCase.getMultipartReplyMeter();
        Assert.assertEquals("Wrong meterId", 9,
                             message.getMeterStats().get(0).getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong flowCount", 7,
                            message.getMeterStats().get(0).getFlowCount().intValue());
        Assert.assertEquals("Wrong packetInCount", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getPacketInCount());
        Assert.assertEquals("Wrong byteInCount", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getByteInCount());
        Assert.assertEquals("Wrong durationSec", 5,
                message.getMeterStats().get(0).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 5,
                message.getMeterStats().get(0).getDurationNsec().intValue());
        Assert.assertEquals("Wrong packetBandCount_01", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(0).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_01", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(0).getByteBandCount());
        Assert.assertEquals("Wrong packetBandCount_02", Uint64.valueOf("FF02020202020202", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(1).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_02", Uint64.valueOf("FF02020202020202", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(1).getByteBandCount());
        Assert.assertEquals("Wrong packetBandCount_03", Uint64.valueOf("FF03030303030303", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(2).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_03", Uint64.valueOf("FF03030303030303", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(2).getByteBandCount());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyMeterBodyMulti() {
        ByteBuf bb = BufferHelper.buildBuffer("00 09 00 01 00 00 00 00 "
                                            + "00 00 00 09 " //meterId_0
                                            + "00 58 " //len_0
                                            + "00 00 00 00 00 00 " //pad_0
                                            + "00 00 00 07 " //flowCount_0
                                            + "FF 01 01 01 01 01 01 01 " //packetInCount_0
                                            + "FF 01 01 01 01 01 01 01 " //byteInCount_0
                                            + "00 00 00 05 " //durationSec_0
                                            + "00 00 00 05 " //durationNsec_0
                                            + "FF 01 01 01 01 01 01 01 " //packetBandCount_01
                                            + "FF 01 01 01 01 01 01 01 " //byteBandCount_01
                                            + "FF 02 02 02 02 02 02 02 " //packetBandCount_02
                                            + "FF 02 02 02 02 02 02 02 " //byteBandCount_02
                                            + "FF 03 03 03 03 03 03 03 " //packetBandCount_03
                                            + "FF 03 03 03 03 03 03 03 " //byteBandCount_03
                                            + "00 00 00 08 " //meterId_1
                                            + "00 58 " //len_1
                                            + "00 00 00 00 00 00 " //pad_1
                                            + "00 00 00 07 " //flowCount_1
                                            + "FF 01 01 01 01 01 01 01 " //packetInCount_1
                                            + "FF 01 01 01 01 01 01 01 " //byteInCount_1
                                            + "00 00 00 05 " //durationSec_1
                                            + "00 00 00 05 " //durationNsec_1
                                            + "FF 01 01 01 01 01 01 01 " //packetBandCount_11
                                            + "FF 01 01 01 01 01 01 01 " //byteBandCount_11
                                            + "FF 02 02 02 02 02 02 02 " //packetBandCount_12
                                            + "FF 02 02 02 02 02 02 02 " //byteBandCount_12
                                            + "FF 03 03 03 03 03 03 03 " //packetBandCount_13
                                            + "FF 03 03 03 03 03 03 03"//byteBandCount_13
                                           );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 9, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyMeterCase messageCase = (MultipartReplyMeterCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyMeter message = messageCase.getMultipartReplyMeter();
        Assert.assertEquals("Wrong meterId", 9,
                             message.getMeterStats().get(0).getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong flowCount", 7,
                            message.getMeterStats().get(0).getFlowCount().intValue());
        Assert.assertEquals("Wrong packetInCount", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getPacketInCount());
        Assert.assertEquals("Wrong byteInCount", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getByteInCount());
        Assert.assertEquals("Wrong durationSec", 5,
                message.getMeterStats().get(0).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 5,
                message.getMeterStats().get(0).getDurationNsec().intValue());
        Assert.assertEquals("Wrong packetBandCount_01", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(0).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_01", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(0).getByteBandCount());
        Assert.assertEquals("Wrong packetBandCount_02", Uint64.valueOf("FF02020202020202", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(1).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_02", Uint64.valueOf("FF02020202020202", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(1).getByteBandCount());
        Assert.assertEquals("Wrong packetBandCount_03", Uint64.valueOf("FF03030303030303", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(2).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_03", Uint64.valueOf("FF03030303030303", 16),
                message.getMeterStats().get(0).getMeterBandStats().get(2).getByteBandCount());

        Assert.assertEquals("Wrong meterId", 8,
                message.getMeterStats().get(1).getMeterId().getValue().intValue());
        Assert.assertEquals("Wrong flowCount", 7,
                message.getMeterStats().get(1).getFlowCount().intValue());
        Assert.assertEquals("Wrong packetInCount", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(1).getPacketInCount());
        Assert.assertEquals("Wrong byteInCount", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(1).getByteInCount());
        Assert.assertEquals("Wrong durationSec", 5,
                message.getMeterStats().get(1).getDurationSec().intValue());
        Assert.assertEquals("Wrong durationNsec", 5,
                message.getMeterStats().get(1).getDurationNsec().intValue());
        Assert.assertEquals("Wrong packetBandCount_01", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(1).getMeterBandStats().get(0).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_01", Uint64.valueOf("FF01010101010101", 16),
                message.getMeterStats().get(1).getMeterBandStats().get(0).getByteBandCount());
        Assert.assertEquals("Wrong packetBandCount_02", Uint64.valueOf("FF02020202020202", 16),
                message.getMeterStats().get(1).getMeterBandStats().get(1).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_02", Uint64.valueOf("FF02020202020202", 16),
                message.getMeterStats().get(1).getMeterBandStats().get(1).getByteBandCount());
        Assert.assertEquals("Wrong packetBandCount_03", Uint64.valueOf("FF03030303030303", 16),
                message.getMeterStats().get(1).getMeterBandStats().get(2).getPacketBandCount());
        Assert.assertEquals("Wrong byteBandCount_03", Uint64.valueOf("FF03030303030303", 16),
                message.getMeterStats().get(1).getMeterBandStats().get(2).getByteBandCount());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyMeterConfigBody() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0A 00 01 00 00 00 00 "
                                            + "00 28 " //len
                                            + "00 0A " //flags
                                            + "00 00 00 09 " //meterId
                                            + "00 01 " //meterBandDrop.type
                                            + "00 10 " //meterBandDrop.len
                                            + "00 00 00 11 " //meterBandDrop.rate
                                            + "00 00 00 20 " //meterBandDrop.burstSize
                                            + "00 00 00 00 " //meterBandDrop.pad
                                            + "00 02 " //meterBandDscp.type
                                            + "00 10 " //meterBandDscp.len
                                            + "00 00 00 11 " //meterBandDscp.rate
                                            + "00 00 00 20 " //meterBandDscp.burstSize
                                            + "04 " //meterBandDscp.precLevel
                                            + "00 00 00");//meterBandDscp.pad

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 10, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyMeterConfigCase messageCase =
                (MultipartReplyMeterConfigCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyMeterConfig message = messageCase.getMultipartReplyMeterConfig();
        Assert.assertEquals("Wrong flags", new MeterFlags(false, false, true, true),
                             message.getMeterConfig().get(0).getFlags());
        Assert.assertEquals("Wrong meterId", 9,
                             message.getMeterConfig().get(0).getMeterId().getValue().intValue());

        MeterBandDropCase dropCase =
                (MeterBandDropCase) message.getMeterConfig().get(0).getBands().get(0).getMeterBand();
        MeterBandDrop meterBandDrop = dropCase.getMeterBandDrop();
        Assert.assertEquals("Wrong meterBandDrop.type", 1, meterBandDrop.getType().getIntValue());
        Assert.assertEquals("Wrong meterBandDrop.rate", 17, meterBandDrop.getRate().intValue());
        Assert.assertEquals("Wrong meterBandDrop.burstSize", 32, meterBandDrop.getBurstSize().intValue());

        MeterBandDscpRemarkCase dscpCase =
                (MeterBandDscpRemarkCase) message.getMeterConfig().get(0).getBands().get(1).getMeterBand();
        MeterBandDscpRemark meterBandDscp = dscpCase.getMeterBandDscpRemark();
        Assert.assertEquals("Wrong meterBandDscp.type", 2, meterBandDscp.getType().getIntValue());
        Assert.assertEquals("Wrong meterBandDscp.rate", 17, meterBandDscp.getRate().intValue());
        Assert.assertEquals("Wrong meterBandDscp.burstSize", 32, meterBandDscp.getBurstSize().intValue());
        Assert.assertEquals("Wrong meterBandDscp.precLevel", 4, meterBandDscp.getPrecLevel().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testMultipartReplyMeterConfigBodyMulti() {
        ByteBuf bb = BufferHelper.buildBuffer("00 0A 00 01 00 00 00 00 "
                                            + "00 28 " //len
                                            + "00 06 " //flags
                                            + "00 00 00 09 " //meterId
                                            + "00 01 " //meterBandDrop.type
                                            + "00 10 " //meterBandDrop.len
                                            + "00 00 00 11 " //meterBandDrop.rate
                                            + "00 00 00 20 " //meterBandDrop.burstSize
                                            + "00 00 00 00 " //meterBandDrop.pad
                                            + "00 02 " //meterBandDscp.type
                                            + "00 10 " //meterBandDscp.len
                                            + "00 00 00 11 " //meterBandDscp.rate
                                            + "00 00 00 20 " //meterBandDscp.burstSize
                                            + "04 " //meterBandDscp.precLevel
                                            + "00 00 00 " //meterBandDscp.pad
                                            + "00 18 " //len01
                                            + "00 03 " //flags01
                                            + "00 00 00 07 " //meterId01
                                            + "00 02 " //meterBandDscp01.type
                                            + "00 10 " //meterBandDscp01.len
                                            + "00 00 00 11 " //meterBandDscp01.rate
                                            + "00 00 00 20 " //meterBandDscp01.burstSize
                                            + "04 " //meterBandDscp01.precLevel
                                            + "00 00 00"//meterBandDscp01.pad
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 10, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyMeterConfigCase messageCase =
                (MultipartReplyMeterConfigCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyMeterConfig message = messageCase.getMultipartReplyMeterConfig();
        Assert.assertEquals("Wrong flags", new MeterFlags(true, false, true, false),
                             message.getMeterConfig().get(0).getFlags());
        Assert.assertEquals("Wrong meterId", 9,
                             message.getMeterConfig().get(0).getMeterId().getValue().intValue());

        MeterBandDropCase dropCase =
                (MeterBandDropCase) message.getMeterConfig().get(0).getBands().get(0).getMeterBand();
        MeterBandDrop meterBandDrop = dropCase.getMeterBandDrop();
        Assert.assertEquals("Wrong meterBandDrop.type", 1, meterBandDrop.getType().getIntValue());
        Assert.assertEquals("Wrong meterBandDrop.rate", 17, meterBandDrop.getRate().intValue());
        Assert.assertEquals("Wrong meterBandDrop.burstSize", 32, meterBandDrop.getBurstSize().intValue());

        MeterBandDscpRemarkCase dscpCase =
                (MeterBandDscpRemarkCase) message.getMeterConfig().get(0).getBands().get(1).getMeterBand();
        MeterBandDscpRemark meterBandDscp = dscpCase.getMeterBandDscpRemark();
        Assert.assertEquals("Wrong meterBandDscp.type", 2, meterBandDscp.getType().getIntValue());
        Assert.assertEquals("Wrong meterBandDscp.rate", 17, meterBandDscp.getRate().intValue());
        Assert.assertEquals("Wrong meterBandDscp.burstSize", 32, meterBandDscp.getBurstSize().intValue());
        Assert.assertEquals("Wrong meterBandDscp.precLevel", 4, meterBandDscp.getPrecLevel().intValue());

        LOG.info(message.getMeterConfig().get(0).getFlags().toString());
        Assert.assertEquals("Wrong flags01", new MeterFlags(false, true, true, false),
                             message.getMeterConfig().get(1).getFlags());
        Assert.assertEquals("Wrong meterId01", 7,
                             message.getMeterConfig().get(1).getMeterId().getValue().intValue());

        MeterBandDscpRemarkCase dscpCase01 =
                (MeterBandDscpRemarkCase) message.getMeterConfig().get(1).getBands().get(0).getMeterBand();
        MeterBandDscpRemark meterBandDscp01 = dscpCase01.getMeterBandDscpRemark();
        Assert.assertEquals("Wrong meterBandDscp01.type", 2, meterBandDscp01.getType().getIntValue());
        Assert.assertEquals("Wrong meterBandDscp01.rate", 17, meterBandDscp01.getRate().intValue());
        Assert.assertEquals("Wrong meterBandDscp01.burstSize", 32, meterBandDscp01.getBurstSize().intValue());
        Assert.assertEquals("Wrong meterBandDscp01.precLevel", 4, meterBandDscp01.getPrecLevel().intValue());

    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     * Test covers bodies of actions Output, Copy TTL Out, Copy TTL In.
     */
    @Test
    public void testMultipartReplyGroupDescBody01() {
        ByteBuf bb = BufferHelper.buildBuffer("00 07 00 01 00 00 00 00 "
                                            + "00 38 " //len
                                            + "01 " //type
                                            + "00 " //pad
                                            + "00 00 00 08 " //groupId
                                            + "00 30 " //bucketLen
                                            + "00 06 " //bucketWeight
                                            + "00 00 00 05 " //bucketWatchPort
                                            + "00 00 00 04 " //bucketWatchGroup
                                            + "00 00 00 00 " //bucketPad
                                            + "00 00 " //outputType
                                            + "00 10 " //outputLen
                                            + "00 00 10 FF " //outputPort
                                            + "FF FF " //outputMaxLen
                                            + "00 00 00 00 00 00 " //outputPad
                                            + "00 0B " //copyTTLOutType
                                            + "00 08 " //copyTTLOutLen
                                            + "00 00 00 00 " //copyTTLOutPad
                                            + "00 0C " //copyTTLIntType
                                            + "00 08 " //copyTTLIntLen
                                            + "00 00 00 00"//copyTTLInPad
                                            );
        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 7, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyGroupDescCase messageCase = (MultipartReplyGroupDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroupDesc message = messageCase.getMultipartReplyGroupDesc();
        Assert.assertEquals("Wrong type", 1,
                             message.getGroupDesc().get(0).getType().getIntValue());
        Assert.assertEquals("Wrong groupId", 8,
                             message.getGroupDesc().get(0).getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong bucketWeight", 6,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWeight().intValue());
        Assert.assertEquals("Wrong bucketWatchPort", 5,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchPort().getValue().intValue());
        Assert.assertEquals("Wrong bucketWatchGroup", 4,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchGroup().intValue());

        Assert.assertTrue("Wrong outputType",message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(0).getActionChoice() instanceof OutputActionCase);

        Assert.assertEquals("Wrong outputPort", 4351, ((OutputActionCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(0).getActionChoice())
                .getOutputAction().getPort().getValue().intValue());

        Assert.assertEquals("Wrong outputMaxLen", 65535, ((OutputActionCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(0).getActionChoice())
                .getOutputAction().getMaxLength().intValue());

        Assert.assertTrue("Wrong copyTtlOutType", message.getGroupDesc().get(0).getBucketsList()
                .get(0).getAction().get(1).getActionChoice() instanceof CopyTtlOutCase);

        Assert.assertTrue("Wrong copyTtlInType", message.getGroupDesc().get(0).getBucketsList()
                .get(0).getAction().get(2).getActionChoice() instanceof CopyTtlInCase);
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     * Test covers bodies of actions Set MPLS TTL , Dec MPLS TTL, Push VLAN. Push MPLS, Push PBB.
     */
    @Test
    public void testMultipartReplyGroupDescBody02() {
        ByteBuf bb = BufferHelper.buildBuffer("00 07 00 01 00 00 00 00 "
                                            + "00 40 " //len
                                            + "01 " //type
                                            + "00 " //pad
                                            + "00 00 00 08 " //groupId
                                            + "00 38 " //bucketLen
                                            + "00 06 " //bucketWeight
                                            + "00 00 00 05 " //bucketWatchPort
                                            + "00 00 00 04 " //bucketWatchGroup
                                            + "00 00 00 00 " //bucketPad
                                            + "00 0F " //setMplsTtlType
                                            + "00 08 " //setMplsTtlLen
                                            + "09 " //setMplsTtlMPLS_TTL
                                            + "00 00 00 " //setMplsTtlPad
                                            + "00 10 " //decMplsTtlType
                                            + "00 08 " //decMplsTtlLen
                                            + "00 00 00 00 " //decMplsTtlPad
                                            + "00 11 " //pushVlanType
                                            + "00 08 " //pushVlanLen
                                            + "00 20 " //pushVlanEthertype
                                            + "00 00 " //pushVlanPad
                                            + "00 13 " //pushMplsType
                                            + "00 08 " //pushMplsLen
                                            + "00 FF " //pushMplsEthertype
                                            + "00 00 " //pushMplsPad
                                            + "00 1A " //pushPbbType
                                            + "00 08 " //pushPbbLen
                                            + "0F FF " //pushPbbEthertype
                                            + "00 00"//pushPbbPad
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 7, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().getOFPMPFREQMORE());
        MultipartReplyGroupDescCase messageCase = (MultipartReplyGroupDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroupDesc message = messageCase.getMultipartReplyGroupDesc();
        Assert.assertEquals("Wrong type", 1,
                             message.getGroupDesc().get(0).getType().getIntValue());
        Assert.assertEquals("Wrong groupId", 8,
                             message.getGroupDesc().get(0).getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong bucketWeight", 6,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWeight().intValue());
        Assert.assertEquals("Wrong bucketWatchPort", 5,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchPort().getValue().intValue());
        Assert.assertEquals("Wrong bucketWatchGroup", 4,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchGroup().intValue());
        Assert.assertTrue("Wrong setMplsTtlType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(0).getActionChoice() instanceof SetMplsTtlCase);
        Assert.assertEquals("Wrong setMplsTtlMPLS_TTL", 9, ((SetMplsTtlCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(0).getActionChoice()).getSetMplsTtlAction()
                .getMplsTtl().intValue());
        Assert.assertTrue("Wrong decMplsTtlType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(1).getActionChoice() instanceof DecMplsTtlCase);
        Assert.assertTrue("Wrong pushVlanType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(2).getActionChoice() instanceof PushVlanCase);
        Assert.assertEquals("Wrong pushVlanEthertype", 32,((PushVlanCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(2).getActionChoice())
                .getPushVlanAction().getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong pushMplsType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(3).getActionChoice() instanceof PushMplsCase);
        Assert.assertEquals("Wrong pushMplsEthertype", 255, ((PushMplsCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(3).getActionChoice())
                .getPushMplsAction().getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong pushPbbType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(4).getActionChoice() instanceof PushPbbCase);
        Assert.assertEquals("Wrong pushPbbEthertype", 4095, ((PushPbbCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(4).getActionChoice())
                .getPushPbbAction().getEthertype().getValue().intValue());
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     * Test covers bodies of actions Pop VLAN, Pop PBB, Pop MPLS, Group, Dec NW TTL.
     */
    @Test
    public void testMultipartReplyGroupDescBody03() {
        ByteBuf bb = BufferHelper.buildBuffer("00 07 00 01 00 00 00 00 "
                                            + "00 48 " //len
                                            + "01 " //type
                                            + "00 " //pad
                                            + "00 00 00 08 " //groupId
                                            + "00 40 " //bucketLen
                                            + "00 06 " //bucketWeight
                                            + "00 00 00 05 " //bucketWatchPort
                                            + "00 00 00 04 " //bucketWatchGroup
                                            + "00 00 00 00 " //bucketPad
                                            + "00 12 " //popVlanType
                                            + "00 08 " //popVlanLen
                                            + "00 00 00 00 " //popVlanPad
                                            + "00 1B " //popPbbType
                                            + "00 08 " //popPbbLen
                                            + "00 00 00 00 " //popPbbPad
                                            + "00 14 " //popMplsType
                                            + "00 08 " //popMplsLen
                                            + "00 CF " //popMplsEthertype
                                            + "00 00 " //popMplsPad
                                            + "00 15 " //setQueueType
                                            + "00 08 " //setQueueLen
                                            + "00 CF 00 00 " //setQueueQueueId
                                            + "00 16 " //groupType
                                            + "00 08 " //groupLen
                                            + "00 CF 00 00 " //groupGroupId
                                            + "00 18 " //decNwTtlType
                                            + "00 08 " //decNwTtlLen
                                            + "00 00 00 00"//decNwTtlPad
                                            );

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 7, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyGroupDescCase messageCase = (MultipartReplyGroupDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroupDesc message = messageCase.getMultipartReplyGroupDesc();
        Assert.assertEquals("Wrong type", 1, message.getGroupDesc().get(0).getType().getIntValue());
        Assert.assertEquals("Wrong groupId", 8, message.getGroupDesc().get(0).getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong bucketWeight", 6,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWeight().intValue());
        Assert.assertEquals("Wrong bucketWatchPort", 5,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchPort().getValue().intValue());
        Assert.assertEquals("Wrong bucketWatchGroup", 4,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchGroup().intValue());
        Assert.assertTrue("Wrong popVlanType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(0).getActionChoice() instanceof PopVlanCase);
        Assert.assertTrue("Wrong popPbbType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(1).getActionChoice() instanceof PopPbbCase);
        Assert.assertTrue("Wrong popMplsType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(2).getActionChoice() instanceof PopMplsCase);
        Assert.assertEquals("Wrong popMplsEthertype", 207, ((PopMplsCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(2).getActionChoice())
                .getPopMplsAction().getEthertype().getValue().intValue());
        Assert.assertTrue("Wrong setQueueType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(3).getActionChoice() instanceof SetQueueCase);
        Assert.assertEquals("Wrong setQueueQueueId", 13565952, ((SetQueueCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(3).getActionChoice())
                .getSetQueueAction().getQueueId().intValue());
        Assert.assertTrue("Wrong groupType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(4).getActionChoice() instanceof GroupCase);
        Assert.assertEquals("Wrong groupGroupId", 13565952, ((GroupCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(4).getActionChoice())
                .getGroupAction().getGroupId().intValue());
        Assert.assertTrue("Wrong decNwTtlType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(5).getActionChoice() instanceof DecNwTtlCase);
    }

    /**
     * Testing {@link MultipartReplyMessageFactory} for correct translation into POJO.
     * Test covers bodies of actions NW TTL, Experimenter.
     */
    @Test
    public void testMultipartReplyGroupDescBody04() {
        ByteBuf bb = BufferHelper.buildBuffer("00 07 00 01 00 00 00 00 "
                                            + "00 30 " //len
                                            + "01 " //type
                                            + "00 " //pad
                                            + "00 00 00 08 " //groupId
                                            + "00 28 " //bucketLen
                                            + "00 06 " //bucketWeight
                                            + "00 00 00 05 " //bucketWatchPort
                                            + "00 00 00 04 " //bucketWatchGroup
                                            + "00 00 00 00 " //bucketPad
                                            + "00 17 " //nwTTlType
                                            + "00 08 " //nwTTlLen
                                            + "0E " //nwTTlnwTTL
                                            + "00 00 00 " //nwTTlPad
                                            + "00 19 " //setFieldType
                                            + "00 10 " //setFieldLen
                                            + "80 00 " //setFieldOXMClass
                                            + "00 " //setFieldOXMField
                                            + "04 " //setFieldOXMLength
                                            + "00 00 00 FF " //setFieldPort
                                            + "00 00 00 00");

        MultipartReplyMessage builtByFactory = BufferHelper.deserialize(multipartFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong type", 7, builtByFactory.getType().getIntValue());
        Assert.assertEquals("Wrong flag", true, builtByFactory.getFlags().isOFPMPFREQMORE());
        MultipartReplyGroupDescCase messageCase = (MultipartReplyGroupDescCase) builtByFactory.getMultipartReplyBody();
        MultipartReplyGroupDesc message = messageCase.getMultipartReplyGroupDesc();
        Assert.assertEquals("Wrong type", 1,
                             message.getGroupDesc().get(0).getType().getIntValue());
        Assert.assertEquals("Wrong groupId", 8,
                             message.getGroupDesc().get(0).getGroupId().getValue().intValue());
        Assert.assertEquals("Wrong bucketWeight", 6,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWeight().intValue());
        Assert.assertEquals("Wrong bucketWatchPort", 5,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchPort().getValue().intValue());
        Assert.assertEquals("Wrong bucketWatchGroup", 4,
                message.getGroupDesc().get(0).getBucketsList().get(0).getWatchGroup().intValue());

        Assert.assertTrue("Wrong nwTTlType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(0).getActionChoice() instanceof SetNwTtlCase);

        Assert.assertEquals("Wrong nwTTlnwTTL", 14, ((SetNwTtlCase) message.getGroupDesc().get(0)
                .getBucketsList().get(0).getAction().get(0).getActionChoice())
                .getSetNwTtlAction().getNwTtl().intValue());

        Assert.assertTrue("Wrong setFieldType", message.getGroupDesc().get(0).getBucketsList().get(0)
                .getAction().get(1).getActionChoice() instanceof SetFieldCase);

        Assert.assertEquals("Wrong setFieldOXMClass", OpenflowBasicClass.class,
                ((SetFieldCase) message.getGroupDesc().get(0).getBucketsList().get(0).getAction().get(1)
                .getActionChoice()).getSetFieldAction().getMatchEntry().get(0).getOxmClass());

        Assert.assertEquals("Wrong setFieldOXMField", InPort.class,
                ((SetFieldCase) message.getGroupDesc().get(0).getBucketsList().get(0).getAction().get(1)
                        .getActionChoice()).getSetFieldAction().getMatchEntry().get(0).getOxmMatchField());

        MatchEntry entry = ((SetFieldCase) message.getGroupDesc().get(0).getBucketsList().get(0).getAction().get(1)
                .getActionChoice()).getSetFieldAction().getMatchEntry().get(0);
        Assert.assertEquals("Wrong setFieldOXMValue", 255, ((InPortCase) entry.getMatchEntryValue())
                .getInPort().getPortNumber().getValue().intValue());
    }
}
