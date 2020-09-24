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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10PacketOutInputMessageFactory.
 *
 * @author michal.polkorab
 */
public class OF10PacketOutInputMessageFactoryTest {

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
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, PacketOutInput.class));
    }

    /**
     * Testing of {@link OF10PacketOutInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testPacketOutInputMessage() throws Exception {
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setBufferId(Uint32.valueOf(256));
        builder.setInPort(new PortNumber(Uint32.valueOf(257)));
        final List<Action> actions = new ArrayList<>();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        outputBuilder.setMaxLength(Uint16.valueOf(50));
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new StripVlanCaseBuilder().build());
        builder.setAction(actions);
        actions.add(actionBuilder.build());
        builder.setAction(actions);
        builder.setData(ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14"));
        PacketOutInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        packetOutFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 13, 48);
        Assert.assertEquals("Wrong BufferId", 256, out.readUnsignedInt());
        Assert.assertEquals("Wrong PortNumber", 257, out.readUnsignedShort());
        Assert.assertEquals("Wrong actions length", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong port", 42, out.readUnsignedShort());
        Assert.assertEquals("Wrong maxlength", 50, out.readUnsignedShort());
        Assert.assertEquals("Wrong action type", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong action length", 8, out.readUnsignedShort());
        out.skipBytes(4);
        byte[] readData = new byte[out.readableBytes()];
        out.readBytes(readData);
        Assert.assertArrayEquals("Wrong data", message.getData(), readData);
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }

    /**
     * Testing of {@link OF10PacketOutInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testPacketOutInputWithNoData() throws Exception {
        PacketOutInputBuilder builder = new PacketOutInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setBufferId(Uint32.valueOf(256));
        builder.setInPort(new PortNumber(Uint32.valueOf(257)));
        List<Action> actions = new ArrayList<>();
        builder.setAction(actions);
        builder.setData(null);
        PacketOutInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        packetOutFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 13, 16);
        out.skipBytes(8); // skip packet out message to data index
        Assert.assertTrue("Unread data", out.readableBytes() == 0);
    }
}
