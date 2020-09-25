/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.desc._case.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table._case.MultipartRequestTableBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10StatsRequestInputFactory.
 *
 * @author michal.polkorab
 */
public class OF10StatsRequestInputFactoryTest {

    private SerializerRegistry registry;
    private OFSerializer<MultipartRequestInput> multipartFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        multipartFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, MultipartRequestInput.class));
    }

    /**
     * Testing OF10StatsRequestInputFactory (Desc) for correct serialization.
     */
    @Test
    public void testDesc() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPDESC);
        builder.setFlags(new MultipartRequestFlags(false));
        MultipartRequestDescCaseBuilder caseBuilder = new MultipartRequestDescCaseBuilder();
        MultipartRequestDescBuilder descBuilder = new MultipartRequestDescBuilder();
        caseBuilder.setMultipartRequestDesc(descBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 12);
        Assert.assertEquals("Wrong type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsRequestInputFactory (Flow) for correct serialization.
     */
    @Test
    public void testFlow() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPFLOW);
        builder.setFlags(new MultipartRequestFlags(false));
        final MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        final MultipartRequestFlowBuilder flowBuilder = new MultipartRequestFlowBuilder();
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true,
                true, true, true, true));
        matchBuilder.setNwSrcMask(Uint8.valueOf(8));
        matchBuilder.setNwDstMask(Uint8.valueOf(16));
        matchBuilder.setInPort(Uint16.valueOf(51));
        matchBuilder.setDlSrc(new MacAddress("00:01:02:03:04:05"));
        matchBuilder.setDlDst(new MacAddress("05:04:03:02:01:00"));
        matchBuilder.setDlVlan(Uint16.valueOf(52));
        matchBuilder.setDlVlanPcp(Uint8.valueOf(53));
        matchBuilder.setDlType(Uint16.valueOf(54));
        matchBuilder.setNwTos(Uint8.valueOf(55));
        matchBuilder.setNwProto(Uint8.valueOf(56));
        matchBuilder.setNwSrc(new Ipv4Address("10.0.0.1"));
        matchBuilder.setNwDst(new Ipv4Address("10.0.0.2"));
        matchBuilder.setTpSrc(Uint16.valueOf(57));
        matchBuilder.setTpDst(Uint16.valueOf(58));
        flowBuilder.setMatchV10(matchBuilder.build());
        flowBuilder.setTableId(Uint8.ONE);
        flowBuilder.setOutPort(Uint32.valueOf(42));
        caseBuilder.setMultipartRequestFlow(flowBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 56);
        Assert.assertEquals("Wrong type", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong wildcards", 3414271, out.readUnsignedInt());
        Assert.assertEquals("Wrong in-port", 51, out.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        out.readBytes(dlSrc);
        Assert.assertArrayEquals("Wrong dl-src", new byte[] { 00, 01, 02, 03, 04, 05 }, dlSrc);
        byte[] dlDst = new byte[6];
        out.readBytes(dlDst);
        Assert.assertArrayEquals("Wrong dl-dst", new byte[] { 05, 04, 03, 02, 01, 00 }, dlDst);
        Assert.assertEquals("Wrong dl-vlan", 52, out.readUnsignedShort());
        Assert.assertEquals("Wrong dl-vlan-pcp", 53, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong dl-type", 54, out.readUnsignedShort());
        Assert.assertEquals("Wrong nw-tos", 55, out.readUnsignedByte());
        Assert.assertEquals("Wrong nw-proto", 56, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong nw-src", 167772161, out.readUnsignedInt());
        Assert.assertEquals("Wrong nw-dst", 167772162, out.readUnsignedInt());
        Assert.assertEquals("Wrong tp-src", 57, out.readUnsignedShort());
        Assert.assertEquals("Wrong tp-dst", 58, out.readUnsignedShort());
        Assert.assertEquals("Wrong table-id", 1, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong out-port", 42, out.readUnsignedShort());
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsRequestInputFactory (Aggregate) for correct serialization.
     */
    @Test
    public void testAggregate() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPAGGREGATE);
        builder.setFlags(new MultipartRequestFlags(false));
        final MultipartRequestFlowCaseBuilder caseBuilder = new MultipartRequestFlowCaseBuilder();
        final MultipartRequestFlowBuilder flowBuilder = new MultipartRequestFlowBuilder();
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(false, false, false, false,
                false, false, false, false, false, false));
        matchBuilder.setNwSrcMask(Uint8.valueOf(32));
        matchBuilder.setNwDstMask(Uint8.valueOf(32));
        matchBuilder.setInPort(Uint16.valueOf(51));
        matchBuilder.setDlSrc(new MacAddress("00:01:02:03:04:05"));
        matchBuilder.setDlDst(new MacAddress("05:04:03:02:01:00"));
        matchBuilder.setDlVlan(Uint16.valueOf(52));
        matchBuilder.setDlVlanPcp(Uint8.valueOf(53));
        matchBuilder.setDlType(Uint16.valueOf(54));
        matchBuilder.setNwTos(Uint8.valueOf(55));
        matchBuilder.setNwProto(Uint8.valueOf(56));
        matchBuilder.setNwSrc(new Ipv4Address("10.0.0.1"));
        matchBuilder.setNwDst(new Ipv4Address("10.0.0.2"));
        matchBuilder.setTpSrc(Uint16.valueOf(57));
        matchBuilder.setTpDst(Uint16.valueOf(58));
        flowBuilder.setMatchV10(matchBuilder.build());
        flowBuilder.setTableId(Uint8.valueOf(42));
        flowBuilder.setOutPort(Uint32.valueOf(6653));
        caseBuilder.setMultipartRequestFlow(flowBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 56);
        Assert.assertEquals("Wrong type", 2, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong wildcards", 0, out.readUnsignedInt());
        Assert.assertEquals("Wrong in-port", 51, out.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        out.readBytes(dlSrc);
        Assert.assertArrayEquals("Wrong dl-src", new byte[] { 00, 01, 02, 03, 04, 05 }, dlSrc);
        byte[] dlDst = new byte[6];
        out.readBytes(dlDst);
        Assert.assertArrayEquals("Wrong dl-dst", new byte[] { 05, 04, 03, 02, 01, 00 }, dlDst);
        Assert.assertEquals("Wrong dl-vlan", 52, out.readUnsignedShort());
        Assert.assertEquals("Wrong dl-vlan-pcp", 53, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong dl-type", 54, out.readUnsignedShort());
        Assert.assertEquals("Wrong nw-tos", 55, out.readUnsignedByte());
        Assert.assertEquals("Wrong nw-proto", 56, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong nw-src", 167772161, out.readUnsignedInt());
        Assert.assertEquals("Wrong nw-dst", 167772162, out.readUnsignedInt());
        Assert.assertEquals("Wrong tp-src", 57, out.readUnsignedShort());
        Assert.assertEquals("Wrong tp-dst", 58, out.readUnsignedShort());
        Assert.assertEquals("Wrong table-id", 42, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong out-port", 6653, out.readUnsignedShort());
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsRequestInputFactory (Table) for correct serialization.
     */
    @Test
    public void testTable() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPTABLE);
        builder.setFlags(new MultipartRequestFlags(false));
        MultipartRequestTableCaseBuilder caseBuilder = new MultipartRequestTableCaseBuilder();
        MultipartRequestTableBuilder tableBuilder = new MultipartRequestTableBuilder();
        caseBuilder.setMultipartRequestTable(tableBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 12);
        Assert.assertEquals("Wrong type", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsRequestInputFactory (Port) for correct serialization.
     */
    @Test
    public void testPort() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPPORTSTATS);
        builder.setFlags(new MultipartRequestFlags(false));
        MultipartRequestPortStatsCaseBuilder caseBuilder = new MultipartRequestPortStatsCaseBuilder();
        MultipartRequestPortStatsBuilder portBuilder = new MultipartRequestPortStatsBuilder();
        portBuilder.setPortNo(Uint32.valueOf(15));
        caseBuilder.setMultipartRequestPortStats(portBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 20);
        Assert.assertEquals("Wrong type", 4, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong port-no", 15, out.readUnsignedShort());
        out.skipBytes(6);
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing OF10StatsRequestInputFactory (Queue) for correct serialization.
     */
    @Test
    public void testQueue() throws Exception {
        MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setType(MultipartType.OFPMPQUEUE);
        builder.setFlags(new MultipartRequestFlags(false));
        MultipartRequestQueueCaseBuilder caseBuilder = new MultipartRequestQueueCaseBuilder();
        MultipartRequestQueueBuilder queueBuilder = new MultipartRequestQueueBuilder();
        queueBuilder.setPortNo(Uint32.valueOf(15));
        queueBuilder.setQueueId(Uint32.valueOf(16));
        caseBuilder.setMultipartRequestQueue(queueBuilder.build());
        builder.setMultipartRequestBody(caseBuilder.build());
        MultipartRequestInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        multipartFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 16, 20);
        Assert.assertEquals("Wrong type", 5, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong port-no", 15, out.readUnsignedShort());
        out.skipBytes(2);
        Assert.assertEquals("Wrong queue-id", 16, out.readUnsignedInt());
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }
}
