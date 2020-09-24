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
import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetTpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.dst._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.tp.src._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlagsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10FlowModInputMessageFactory.
 *
 * @author michal.polkorab
 */
public class OF10FlowModInputMessageFactoryTest {

    private SerializerRegistry registry;
    private OFSerializer<FlowModInput> flowModFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        flowModFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF10_VERSION_ID, FlowModInput.class));
    }

    /**
     * Testing of {@link OF10FlowModInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testFlowModInputMessageFactory() throws Exception {
        FlowModInputBuilder builder = new FlowModInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF10_VERSION_ID);
        MatchV10Builder matchBuilder = new MatchV10Builder();
        matchBuilder.setWildcards(new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true));
        matchBuilder.setNwSrcMask(Uint8.ZERO);
        matchBuilder.setNwDstMask(Uint8.ZERO);
        matchBuilder.setInPort(Uint16.valueOf(58));
        matchBuilder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        matchBuilder.setDlDst(new MacAddress("ff:ff:ff:ff:ff:ff"));
        matchBuilder.setDlVlan(Uint16.valueOf(18));
        matchBuilder.setDlVlanPcp(Uint8.valueOf(5));
        matchBuilder.setDlType(Uint16.valueOf(42));
        matchBuilder.setNwTos(Uint8.valueOf(4));
        matchBuilder.setNwProto(Uint8.valueOf(7));
        matchBuilder.setNwSrc(new Ipv4Address("8.8.8.8"));
        matchBuilder.setNwDst(new Ipv4Address("16.16.16.16"));
        matchBuilder.setTpSrc(Uint16.valueOf(6653));
        matchBuilder.setTpDst(Uint16.valueOf(6633));
        builder.setMatchV10(matchBuilder.build());
        final byte[] cookie = new byte[]{(byte) 0xFF, 0x01, 0x04, 0x01, 0x06, 0x00, 0x07, 0x01};
        builder.setCookie(Uint64.valueOf(new BigInteger(1, cookie)));
        builder.setCommand(FlowModCommand.forValue(0));
        builder.setIdleTimeout(Uint16.valueOf(12));
        builder.setHardTimeout(Uint16.valueOf(16));
        builder.setPriority(Uint16.ONE);
        builder.setBufferId(Uint32.TWO);
        builder.setOutPort(new PortNumber(Uint32.valueOf(4422)));
        builder.setFlagsV10(new FlowModFlagsV10(true, false, true));
        final List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        SetNwDstCaseBuilder nwDstCaseBuilder = new SetNwDstCaseBuilder();
        SetNwDstActionBuilder nwDstBuilder = new SetNwDstActionBuilder();
        nwDstBuilder.setIpAddress(new Ipv4Address("2.2.2.2"));
        nwDstCaseBuilder.setSetNwDstAction(nwDstBuilder.build());
        actionBuilder.setActionChoice(nwDstCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        SetTpSrcCaseBuilder tpSrcCaseBuilder = new SetTpSrcCaseBuilder();
        SetTpSrcActionBuilder tpSrcBuilder = new SetTpSrcActionBuilder();
        tpSrcBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        tpSrcCaseBuilder.setSetTpSrcAction(tpSrcBuilder.build());
        actionBuilder.setActionChoice(tpSrcCaseBuilder.build());
        actions.add(actionBuilder.build());
        builder.setAction(actions);
        FlowModInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        flowModFactory.serialize(message, out);

        BufferHelper.checkHeaderV10(out, (byte) 14, 88);
        Assert.assertEquals("Wrong wildcards", 3678463, out.readUnsignedInt());
        Assert.assertEquals("Wrong inPort", 58, out.readUnsignedShort());
        byte[] dlSrc = new byte[6];
        out.readBytes(dlSrc);
        Assert.assertEquals("Wrong dlSrc", "01:01:01:01:01:01", ByteBufUtils.macAddressToString(dlSrc));
        byte[] dlDst = new byte[6];
        out.readBytes(dlDst);
        Assert.assertEquals("Wrong dlDst", "FF:FF:FF:FF:FF:FF", ByteBufUtils.macAddressToString(dlDst));
        Assert.assertEquals("Wrong dlVlan", 18, out.readUnsignedShort());
        Assert.assertEquals("Wrong dlVlanPcp", 5, out.readUnsignedByte());
        out.skipBytes(1);
        Assert.assertEquals("Wrong dlType", 42, out.readUnsignedShort());
        Assert.assertEquals("Wrong nwTos", 4, out.readUnsignedByte());
        Assert.assertEquals("Wrong nwProto", 7, out.readUnsignedByte());
        out.skipBytes(2);
        Assert.assertEquals("Wrong nwSrc", 134744072, out.readUnsignedInt());
        Assert.assertEquals("Wrong nwDst", 269488144, out.readUnsignedInt());
        Assert.assertEquals("Wrong tpSrc", 6653, out.readUnsignedShort());
        Assert.assertEquals("Wrong tpDst", 6633, out.readUnsignedShort());
        byte[] cookieRead = new byte[8];
        out.readBytes(cookieRead);
        Assert.assertArrayEquals("Wrong cookie", cookie, cookieRead);
        Assert.assertEquals("Wrong command", 0, out.readUnsignedShort());
        Assert.assertEquals("Wrong idleTimeOut", 12, out.readUnsignedShort());
        Assert.assertEquals("Wrong hardTimeOut", 16, out.readUnsignedShort());
        Assert.assertEquals("Wrong priority", 1, out.readUnsignedShort());
        Assert.assertEquals("Wrong bufferId", 2, out.readUnsignedInt());
        Assert.assertEquals("Wrong outPort", 4422, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 3, out.readUnsignedShort());
        Assert.assertEquals("Wrong action - type", 7, out.readUnsignedShort());
        Assert.assertEquals("Wrong action - length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 33686018, out.readUnsignedInt());
        Assert.assertEquals("Wrong action - type", 9, out.readUnsignedShort());
        Assert.assertEquals("Wrong action - length", 8, out.readUnsignedShort());
        Assert.assertEquals("Wrong flags", 42, out.readUnsignedShort());
        out.skipBytes(2);
    }

}
