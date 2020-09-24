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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10PacketInMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10PacketInMessageFactoryTest {
    private OFSerializer<PacketInMessage> factory;
    private static final byte MESSAGE_TYPE = 10;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, PacketInMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        PacketInMessageBuilder builder = new PacketInMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setBufferId(Uint32.ONE);
        builder.setTotalLen(Uint16.ONE);
        builder.setInPort(Uint16.ONE);
        builder.setReason(PacketInReason.forValue(0));
        byte[] data = ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14");
        builder.setData(data);
        PacketInMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 34);
        Assert.assertEquals("Wrong buffer id", message.getBufferId().longValue(), serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong total len", message.getTotalLen().intValue(), serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong port in", message.getInPort().intValue(), serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong reason", message.getReason().getIntValue(), serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(1);
        byte[] readData = new byte[serializedBuffer.readableBytes()];
        serializedBuffer.readBytes(readData);
        Assert.assertArrayEquals("Wrong data", message.getData(), readData);
    }
}
