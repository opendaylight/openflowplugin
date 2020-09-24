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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

/**
 * Unit tests for ErrorMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class ErrorMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 1;
    private OFSerializer<ErrorMessage> factory;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry.getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, ErrorMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        ErrorMessageBuilder builder = new ErrorMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setType(Uint16.TEN);
        builder.setCode(Uint16.valueOf(20));
        byte[] data = ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14");
        builder.setData(data);
        ErrorMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 28);
        Assert.assertEquals("Wrong Type", message.getType().intValue(), serializedBuffer.readShort());
        Assert.assertEquals("Wrong Code", message.getCode().intValue(), serializedBuffer.readShort());
        byte[] readData = new byte[serializedBuffer.readableBytes()];
        serializedBuffer.readBytes(readData);
        Assert.assertArrayEquals("Wrong data", message.getData(), readData);
    }
}
