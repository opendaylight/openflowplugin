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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class GetFeaturesOutputFactoryTest {
    private OFSerializer<GetFeaturesOutput> factory;
    private static final byte MESSAGE_TYPE = 6;
    private static final byte PADDING = 2;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, GetFeaturesOutput.class));
    }

    @Test
    public void testSerialize() throws Exception {
        GetFeaturesOutputBuilder builder = new GetFeaturesOutputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setDatapathId(Uint64.valueOf(1234));
        builder.setBuffers(Uint32.valueOf(1234));
        builder.setTables(Uint8.valueOf(12));
        builder.setAuxiliaryId(Uint8.valueOf(12));
        builder.setCapabilities(new Capabilities(true, false, true, false, true, false, true));
        builder.setReserved(Uint32.valueOf(1234));
        GetFeaturesOutput message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);

        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 32);
        Assert.assertEquals("Wrong DatapathId", message.getDatapathId().longValue(), serializedBuffer.readLong());
        Assert.assertEquals("Wrong Buffer ID", message.getBuffers().longValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong tables", message.getTables().shortValue(), serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong auxiliary ID", message.getAuxiliaryId().shortValue(),
                serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(PADDING);
        Assert.assertEquals("Wrong Capabilities", message.getCapabilities(),
                createCapabilities(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong reserved", message.getReserved().longValue(), serializedBuffer.readInt());
    }

    private static Capabilities createCapabilities(int input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        final Boolean four = (input & 1 << 3) > 0;
        final Boolean five = (input & 1 << 5) > 0;
        final Boolean six = (input & 1 << 6) > 0;
        final Boolean seven = (input & 1 << 8) > 0;
        return new Capabilities(one, four, five, seven, three, six, two);
    }
}
