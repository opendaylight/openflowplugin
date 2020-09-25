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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.IetfYangUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for PortModInputMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class PortModInputMessageFactoryTest {
    private static final byte MESSAGE_TYPE = 16;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_01 = 4;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_02 = 2;
    private static final byte PADDING_IN_PORT_MOD_MESSAGE_03 = 4;
    private static final int MESSAGE_LENGTH = 40;
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
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, PortModInput.class));
    }

    /**
     * Testing of {@link PortModInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testPortModInput() throws Exception {
        PortModInputBuilder builder = new PortModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPortNo(new PortNumber(Uint32.valueOf(9)));
        builder.setHwAddress(new MacAddress("08:00:27:00:b0:eb"));
        builder.setConfig(new PortConfig(true, false, true, false));
        builder.setMask(new PortConfig(false, true, false, true));
        builder.setAdvertise(new PortFeatures(true, false, false, false,
                                              false, false, false, true,
                                              false, false, false, false,
                                              false, false, false, false));
        final PortModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();

        // simulate parent message
        out.writeInt(1);
        out.writeZero(2);
        out.writeShort(3);

        portModFactory.serialize(message, out);

        // read parent message
        out.readInt();
        out.skipBytes(2);
        out.readShort();

        BufferHelper.checkHeaderV13(out, MESSAGE_TYPE, MESSAGE_LENGTH);
        Assert.assertEquals("Wrong PortNo", message.getPortNo().getValue().longValue(), out.readUnsignedInt());
        out.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_01);
        byte[] address = new byte[6];
        out.readBytes(address);
        Assert.assertEquals("Wrong MacAddress", message.getHwAddress(), IetfYangUtil.INSTANCE.macAddressFor(address));
        out.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_02);
        Assert.assertEquals("Wrong config", message.getConfig(), createPortConfig(out.readInt()));
        Assert.assertEquals("Wrong mask", message.getMask(), createPortConfig(out.readInt()));
        Assert.assertEquals("Wrong advertise", message.getAdvertise(), createPortFeatures(out.readInt()));
        out.skipBytes(PADDING_IN_PORT_MOD_MESSAGE_03);
    }

    private static PortConfig createPortConfig(long input) {
        final Boolean _portDown = (input & 1 << 0) > 0;
        final Boolean _noRecv = (input & 1 << 2) > 0;
        final Boolean _noFwd = (input & 1 << 5) > 0;
        final Boolean _noPacketIn = (input & 1 << 6) > 0;
        return new PortConfig(_noFwd, _noPacketIn, _noRecv, _portDown);
    }

    private static PortFeatures createPortFeatures(long input) {
        final Boolean _10mbHd = (input & 1 << 0) > 0;
        final Boolean _10mbFd = (input & 1 << 1) > 0;
        final Boolean _100mbHd = (input & 1 << 2) > 0;
        final Boolean _100mbFd = (input & 1 << 3) > 0;
        final Boolean _1gbHd = (input & 1 << 4) > 0;
        final Boolean _1gbFd = (input & 1 << 5) > 0;
        final Boolean _10gbFd = (input & 1 << 6) > 0;
        final Boolean _40gbFd = (input & 1 << 7) > 0;
        final Boolean _100gbFd = (input & 1 << 8) > 0;
        final Boolean _1tbFd = (input & 1 << 9) > 0;
        final Boolean _other = (input & 1 << 10) > 0;
        final Boolean _copper = (input & 1 << 11) > 0;
        final Boolean _fiber = (input & 1 << 12) > 0;
        final Boolean _autoneg = (input & 1 << 13) > 0;
        final Boolean _pause = (input & 1 << 14) > 0;
        final Boolean _pauseAsym = (input & 1 << 15) > 0;
        return new PortFeatures(_100gbFd, _100mbFd,  _100mbHd, _10gbFd, _10mbFd, _10mbHd,
                _1gbFd, _1gbHd, _1tbFd, _40gbFd, _autoneg, _copper, _fiber, _other, _pause, _pauseAsym);
    }

}
