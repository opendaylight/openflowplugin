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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class GetAsyncReplyMessageFactoryTest {
    private OFSerializer<GetAsyncOutput> factory;
    private static final byte MESSAGE_TYPE = 27;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetAsyncOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        GetAsyncOutputBuilder builder = new GetAsyncOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPacketInMask(createPacketInMask());
        builder.setPortStatusMask(createPortStatusMask());
        builder.setFlowRemovedMask(createFlowRemowedMask());
        GetAsyncOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 32);
        Assert.assertEquals("Wrong packetInMask", 7, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong packetInMask", 0, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong portStatusMask", 7, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong portStatusMask", 0, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong flowRemovedMask", 15, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong flowRemovedMask", 0, serializedBuffer.readUnsignedInt());
    }

    private static List<PacketInMask> createPacketInMask() {
        List<PacketInMask> masks = new ArrayList<>();
        PacketInMaskBuilder builder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        builder = new PacketInMaskBuilder();
        List<PacketInReason> packetInReasonList = new ArrayList<>();
        packetInReasonList.add(PacketInReason.OFPRNOMATCH);
        packetInReasonList.add(PacketInReason.OFPRACTION);
        packetInReasonList.add(PacketInReason.OFPRINVALIDTTL);
        builder.setMask(packetInReasonList);
        masks.add(builder.build());
        // OFPCR_ROLE_SLAVE
        builder = new PacketInMaskBuilder();
        packetInReasonList = new ArrayList<>();
        builder.setMask(packetInReasonList);
        masks.add(builder.build());
        return masks;
    }

    private static List<PortStatusMask> createPortStatusMask() {
        List<PortStatusMask> masks = new ArrayList<>();
        PortStatusMaskBuilder builder;
        builder = new PortStatusMaskBuilder();
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        List<PortReason> portReasonList = new ArrayList<>();
        portReasonList.add(PortReason.OFPPRADD);
        portReasonList.add(PortReason.OFPPRDELETE);
        portReasonList.add(PortReason.OFPPRMODIFY);
        builder.setMask(portReasonList);
        masks.add(builder.build());
        // OFPCR_ROLE_SLAVE
        builder = new PortStatusMaskBuilder();
        portReasonList = new ArrayList<>();
        builder.setMask(portReasonList);
        masks.add(builder.build());
        return masks;
    }

    private static List<FlowRemovedMask> createFlowRemowedMask() {
        List<FlowRemovedMask> masks = new ArrayList<>();
        FlowRemovedMaskBuilder builder;
        // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
        builder = new FlowRemovedMaskBuilder();
        List<FlowRemovedReason> flowRemovedReasonList = new ArrayList<>();
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRIDLETIMEOUT);
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRHARDTIMEOUT);
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRDELETE);
        flowRemovedReasonList.add(FlowRemovedReason.OFPRRGROUPDELETE);
        builder.setMask(flowRemovedReasonList);
        masks.add(builder.build());
        // OFPCR_ROLE_SLAVE
        builder = new FlowRemovedMaskBuilder();
        flowRemovedReasonList = new ArrayList<>();
        builder.setMask(flowRemovedReasonList);
        masks.add(builder.build());
        return masks;
    }

    @Test
    public void testSetAsyncInputWithNullMasks() throws Exception {
        GetAsyncOutputBuilder builder = new GetAsyncOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPacketInMask(null);
        builder.setPortStatusMask(null);
        builder.setFlowRemovedMask(null);
        GetAsyncOutput message = builder.build();
        GetAsyncReplyMessageFactory serializer = new GetAsyncReplyMessageFactory();
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 8);
        Assert.assertTrue("Unexpected data", serializedBuffer.readableBytes() == 0);
    }

}
