/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for PacketOutInputMessageFactory.
 *
 * @author timotej.kubas
 */
public class PacketOutInputMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 13;
    private static final byte PADDING_IN_PACKET_OUT_MESSAGE = 6;
    private static final int PADDING_IN_ACTION_HEADER = 4;
    private SerializerRegistry registry;
    private OFSerializer<PacketOutInput> packetOutFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        packetOutFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, PacketOutInput.class));
    }

    /**
     * Testing of {@link PacketOutInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testPacketOutInputMessage() throws Exception {
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setBufferId(Uint32.valueOf(256));
        builder.setInPort(new PortNumber(Uint32.valueOf(256)));
        final List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanBuilder = new PushVlanActionBuilder();
        pushVlanBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(25))));
        pushVlanCaseBuilder.setPushVlanAction(pushVlanBuilder.build());
        actionBuilder.setActionChoice(pushVlanCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        builder.setAction(actions);
        builder.setData(ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14"));
        PacketOutInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        packetOutFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, 56);
        Assert.assertEquals("Wrong BufferId", message.getBufferId().longValue(), out.readUnsignedInt());
        Assert.assertEquals("Wrong PortNumber", message.getInPort().getValue().longValue(), out.readUnsignedInt());
        Assert.assertEquals("Wrong ActionsLength", 16, out.readUnsignedShort());
        out.skipBytes(PADDING_IN_PACKET_OUT_MESSAGE);
        Assert.assertEquals("Wrong action type", 17, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong ethertype", 25, out.readUnsignedShort());
        out.skipBytes(Short.BYTES);
        Assert.assertEquals("Wrong action type", 18, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(PADDING_IN_ACTION_HEADER);
        byte[] readData = new byte[out.readableBytes()];
        out.readBytes(readData);
        Assert.assertArrayEquals("Wrong data", message.getData(), readData);
    }

    /**
     * Testing of {@link PacketOutInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testPacketOutInputWithNoData() throws Exception {
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setBufferId(Uint32.valueOf(256));
        builder.setInPort(new PortNumber(Uint32.valueOf(256)));
        List<Action> actions = new ArrayList<>();
        builder.setAction(actions);
        builder.setData(null);
        PacketOutInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        packetOutFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, 24);
        out.skipBytes(16); // skip packet out message to data index
        Assert.assertTrue("Unexpected data", out.readableBytes() == 0);
    }
}
