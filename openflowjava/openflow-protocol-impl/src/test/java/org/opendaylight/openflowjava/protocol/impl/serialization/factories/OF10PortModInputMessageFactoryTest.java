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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10PortModInputMessageFactory.
 *
 * @author michal.polkorab
 */
public class OF10PortModInputMessageFactoryTest {

    private SerializerRegistry registry;
    private OFSerializer<PortModInput> portModFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        portModFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, PortModInput.class));
    }

    /**
     * Testing of {@link OF10PortModInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testPortModInput() throws Exception {
        PortModInputBuilder builder = new PortModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        builder.setPortNo(new PortNumber(Uint32.valueOf(6633)));
        builder.setHwAddress(new MacAddress("08:00:27:00:B0:EB"));
        builder.setConfigV10(new PortConfigV10(true, false, false, true, false, false, true));
        builder.setMaskV10(new PortConfigV10(false, true, true, false, false, true, false));
        builder.setAdvertiseV10(new PortFeaturesV10(true, true, false, false, false, false,
                false, true, true, false, false, false));
        PortModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        portModFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 15, 32);
        Assert.assertEquals("Wrong PortNo", message.getPortNo().getValue().longValue(), out.readUnsignedShort());
        byte[] address = new byte[6];
        out.readBytes(address);
        Assert.assertEquals("Wrong MacAddress", message.getHwAddress(),
                new MacAddress(ByteBufUtils.macAddressToString(address)));
        Assert.assertEquals("Wrong config", 21, out.readUnsignedInt());
        Assert.assertEquals("Wrong mask", 98, out.readUnsignedInt());
        Assert.assertEquals("Wrong advertise", 652, out.readUnsignedInt());
        out.skipBytes(4);
    }
}
