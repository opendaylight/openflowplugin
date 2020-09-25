/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.util;

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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10MatchSerializer.
 *
 * @author michal.polkorab
 */
public class OF10MatchSerializerTest {

    private SerializerRegistry registry;
    private OFSerializer<MatchV10> matchSerializer;

    /**
     * Initializes serializer table and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        matchSerializer = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, MatchV10.class));
    }

    /**
     * Testing correct serialization of ofp_match.
     */
    @Test
    public void test() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(false, false, true, false,
                false, true, false, true, true, true));
        builder.setNwSrcMask(Uint8.valueOf(24));
        builder.setNwDstMask(Uint8.valueOf(16));
        builder.setInPort(Uint16.valueOf(6653));
        builder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        builder.setDlDst(new MacAddress("02:02:02:02:02:02"));
        builder.setDlVlan(Uint16.valueOf(128));
        builder.setDlVlanPcp(Uint8.TWO);
        builder.setDlType(Uint16.valueOf(15));
        builder.setNwTos(Uint8.valueOf(14));
        builder.setNwProto(Uint8.valueOf(85));
        builder.setNwSrc(new Ipv4Address("1.1.1.2"));
        builder.setNwDst(new Ipv4Address("32.16.8.1"));
        builder.setTpSrc(Uint16.valueOf(2048));
        builder.setTpDst(Uint16.valueOf(4096));
        MatchV10 match = builder.build();
        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong wildcards", 2361553, out.readUnsignedInt());
        Assert.assertEquals("Wrong in-port", 6653, out.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        out.readBytes(dlSrc);
        Assert.assertArrayEquals("Wrong dl-src", new byte[] { 01, 01, 01, 01, 01, 01 }, dlSrc);
        byte[] dlDst = new byte[6];
        out.readBytes(dlDst);
        Assert.assertArrayEquals("Wrong dl-dst", new byte[] { 02, 02, 02, 02, 02, 02 }, dlDst);
        Assert.assertEquals("Wrong dl-vlan", 128, out.readUnsignedShort());
        Assert.assertEquals("Wrong dl-vlan-pcp", 2, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong dl-type", 15, out.readUnsignedShort());
        Assert.assertEquals("Wrong nw-tos", 14, out.readUnsignedByte());
        Assert.assertEquals("Wrong nw-proto", 85, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong nw-src", 16843010, out.readUnsignedInt());
        Assert.assertEquals("Wrong nw-dst", 537921537, out.readUnsignedInt());
        Assert.assertEquals("Wrong tp-src", 2048, out.readUnsignedShort());
        Assert.assertEquals("Wrong tp-dst", 4096, out.readUnsignedShort());
    }

    /**
     * Testing correct serialization of ofp_match.
     */
    @Test
    public void test2() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        builder.setNwSrcMask(Uint8.ZERO);
        builder.setNwDstMask(Uint8.ZERO);
        builder.setInPort(Uint16.valueOf(6653));
        builder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        builder.setDlDst(new MacAddress("02:02:02:02:02:02"));
        builder.setDlVlan(Uint16.valueOf(128));
        builder.setDlVlanPcp(Uint8.TWO);
        builder.setDlType(Uint16.valueOf(15));
        builder.setNwTos(Uint8.valueOf(14));
        builder.setNwProto(Uint8.valueOf(85));
        builder.setNwSrc(new Ipv4Address("1.1.1.2"));
        builder.setNwDst(new Ipv4Address("32.16.8.1"));
        builder.setTpSrc(Uint16.valueOf(2048));
        builder.setTpDst(Uint16.valueOf(4096));
        MatchV10 match = builder.build();
        matchSerializer.serialize(match, out);

        Assert.assertEquals("Wrong wildcards", 3678463, out.readUnsignedInt());
        Assert.assertEquals("Wrong in-port", 6653, out.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        out.readBytes(dlSrc);
        Assert.assertArrayEquals("Wrong dl-src", new byte[] { 01, 01, 01, 01, 01, 01 }, dlSrc);
        byte[] dlDst = new byte[6];
        out.readBytes(dlDst);
        Assert.assertArrayEquals("Wrong dl-dst", new byte[] { 02, 02, 02, 02, 02, 02 }, dlDst);
        Assert.assertEquals("Wrong dl-vlan", 128, out.readUnsignedShort());
        Assert.assertEquals("Wrong dl-vlan-pcp", 2, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong dl-type", 15, out.readUnsignedShort());
        Assert.assertEquals("Wrong nw-tos", 14, out.readUnsignedByte());
        Assert.assertEquals("Wrong nw-proto", 85, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong nw-src", 16843010, out.readUnsignedInt());
        Assert.assertEquals("Wrong nw-dst", 537921537, out.readUnsignedInt());
        Assert.assertEquals("Wrong tp-src", 2048, out.readUnsignedShort());
        Assert.assertEquals("Wrong tp-dst", 4096, out.readUnsignedShort());
    }
}
