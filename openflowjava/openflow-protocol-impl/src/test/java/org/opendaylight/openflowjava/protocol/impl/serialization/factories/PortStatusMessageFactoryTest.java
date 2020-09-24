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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for PortStatusMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class PortStatusMessageFactoryTest {
    private OFSerializer<PortStatusMessage> factory;
    private static final byte MESSAGE_TYPE = 12;
    private static final byte PADDING = 7;
    private static final byte PORT_PADDING_1 = 4;
    private static final byte PORT_PADDING_2 = 2;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, PortStatusMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        PortStatusMessageBuilder builder = new PortStatusMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setReason(PortReason.forValue(1));
        builder.setPortNo(Uint32.ONE);
        builder.setHwAddr(new MacAddress("94:de:80:a6:61:40"));
        builder.setName("Port name");
        builder.setConfig(new PortConfig(true, false, true, false));
        builder.setState(new PortState(true, false, true));
        builder.setCurrentFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false,
                true, false, true, false, true, false));
        builder.setAdvertisedFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false,
                true, false, true, false, true, false));
        builder.setSupportedFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false,
                true, false, true, false, true, false));
        builder.setPeerFeatures(new PortFeatures(true, false, true, false, true, false, true, false, true, false, true,
                false, true, false, true, false));
        builder.setCurrSpeed(Uint32.valueOf(1234));
        builder.setMaxSpeed(Uint32.valueOf(1234));
        PortStatusMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV13(serializedBuffer, MESSAGE_TYPE, 80);
        Assert.assertEquals("Wrong reason", message.getReason().getIntValue(), serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(PADDING);
        Assert.assertEquals("Wrong PortNo", message.getPortNo().intValue(), serializedBuffer.readUnsignedInt());
        serializedBuffer.skipBytes(PORT_PADDING_1);
        byte[] address = new byte[6];
        serializedBuffer.readBytes(address);
        Assert.assertEquals("Wrong MacAddress", message.getHwAddr().getValue().toLowerCase(),
                new MacAddress(ByteBufUtils.macAddressToString(address)).getValue().toLowerCase());
        serializedBuffer.skipBytes(PORT_PADDING_2);
        byte[] name = new byte[16];
        serializedBuffer.readBytes(name);
        Assert.assertEquals("Wrong name", message.getName(), new String(name).trim());
        Assert.assertEquals("Wrong config", message.getConfig(), createPortConfig(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong state", message.getState(), createPortState(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong current", message.getCurrentFeatures(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong advertised", message.getAdvertisedFeatures(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong supported", message.getSupportedFeatures(),
                createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong peer", message.getPeerFeatures(), createPortFeatures(serializedBuffer.readInt()));
        Assert.assertEquals("Wrong Current speed", message.getCurrSpeed().longValue(), serializedBuffer.readInt());
        Assert.assertEquals("Wrong Max speed", message.getMaxSpeed().longValue(), serializedBuffer.readInt());
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
        return new PortFeatures(_100gbFd, _100mbFd, _100mbHd, _10gbFd, _10mbFd, _10mbHd, _1gbFd, _1gbHd, _1tbFd,
                _40gbFd, _autoneg, _copper, _fiber, _other, _pause, _pauseAsym);
    }

    private static PortState createPortState(long input) {
        final Boolean one = (input & 1 << 0) > 0;
        final Boolean two = (input & 1 << 1) > 0;
        final Boolean three = (input & 1 << 2) > 0;
        return new PortState(two, one, three);
    }

}
