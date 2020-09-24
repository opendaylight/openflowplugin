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
import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10FlowRemovedMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10FlowRemovedMessageFactoryTest {
    private OFSerializer<FlowRemovedMessage> factory;
    private static final byte MESSAGE_TYPE = 11;

    @Before
    public void startUp() {
        SerializerRegistry registry = new SerializerRegistryImpl();
        registry.init();
        factory = registry
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, FlowRemovedMessage.class));
    }

    @Test
    public void testSerialize() throws Exception {
        FlowRemovedMessageBuilder builder = new FlowRemovedMessageBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true));
        matchBuilder.setNwSrcMask(Uint8.ZERO);
        matchBuilder.setNwDstMask(Uint8.ZERO);
        matchBuilder.setInPort(Uint16.valueOf(58));
        matchBuilder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        matchBuilder.setDlDst(new MacAddress("ff:ff:ff:ff:ff:ff"));
        matchBuilder.setDlVlan(Uint16.valueOf(18));
        matchBuilder.setDlVlanPcp((short) 5);
        matchBuilder.setDlType(Uint16.valueOf(42));
        matchBuilder.setNwTos((short) 4);
        matchBuilder.setNwProto((short) 7);
        matchBuilder.setNwSrc(new Ipv4Address("8.8.8.8"));
        matchBuilder.setNwDst(new Ipv4Address("16.16.16.16"));
        matchBuilder.setTpSrc(Uint16.valueOf(6653));
        matchBuilder.setTpDst(Uint16.valueOf(6633));
        builder.setMatchV10(matchBuilder.build());
        byte[] cookie = new byte[] { (byte) 0xFF, 0x01, 0x04, 0x01, 0x01, 0x01, 0x04, 0x01 };
        builder.setCookie(new BigInteger(1, cookie));
        builder.setPriority(1);
        builder.setReason(FlowRemovedReason.forValue(1));
        builder.setDurationSec(Uint32.ONE);
        builder.setDurationNsec(Uint32.ONE);
        builder.setIdleTimeout(Uint16.valueOf(12));
        builder.setPacketCount(Uint64.ONE);
        builder.setByteCount(Uint64.TWO);
        FlowRemovedMessage message = builder.build();

        ByteBuf serializedBuffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.serialize(message, serializedBuffer);
        BufferHelper.checkHeaderV10(serializedBuffer, MESSAGE_TYPE, 88);
        Assert.assertEquals("Wrong wildcards", 3678463, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong inPort", 58, serializedBuffer.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        serializedBuffer.readBytes(dlSrc);
        Assert.assertEquals("Wrong dlSrc", "01:01:01:01:01:01", ByteBufUtils.macAddressToString(dlSrc));
        byte[] dlDst = new byte[6];
        serializedBuffer.readBytes(dlDst);
        Assert.assertEquals("Wrong dlDst", "FF:FF:FF:FF:FF:FF", ByteBufUtils.macAddressToString(dlDst));
        Assert.assertEquals("Wrong dlVlan", 18, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong dlVlanPcp", 5, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(1);
        Assert.assertEquals("Wrong dlType", 42, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong nwTos", 4, serializedBuffer.readUnsignedByte());
        Assert.assertEquals("Wrong nwProto", 7, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(2);
        Assert.assertEquals("Wrong nwSrc", 134744072, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong nwDst", 269488144, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong tpSrc", 6653, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong tpDst", 6633, serializedBuffer.readUnsignedShort());
        byte[] cookieRead = new byte[8];
        serializedBuffer.readBytes(cookieRead);
        Assert.assertArrayEquals("Wrong cookie", cookie, cookieRead);
        Assert.assertEquals("Wrong priority", 1, serializedBuffer.readUnsignedShort());
        Assert.assertEquals("Wrong reason", 1, serializedBuffer.readUnsignedByte());
        serializedBuffer.skipBytes(1);
        Assert.assertEquals("Wrong duration", 1L, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong duration nsec", 1L, serializedBuffer.readUnsignedInt());
        Assert.assertEquals("Wrong idle timeout", 12, serializedBuffer.readUnsignedShort());
        serializedBuffer.skipBytes(2);
        Assert.assertEquals("Wrong packet count", 1L, serializedBuffer.readLong());
        Assert.assertEquals("Wrong byte count", 2L, serializedBuffer.readLong());
    }
}
