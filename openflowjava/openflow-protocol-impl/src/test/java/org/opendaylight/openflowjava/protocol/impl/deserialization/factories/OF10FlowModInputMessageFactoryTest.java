/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for OF10FlowModInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10FlowModInputMessageFactoryTest {
    private OFDeserializer<FlowModInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 14, FlowModInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 38 20 ff 00 3a 01 01 01 01 01 01 ff "
                + "ff ff ff ff ff 00 12 05 00 00 2a 04 07 00 00 08 08 08 08 10 10 10 10 "
                + "19 fd 19 e9 ff 01 04 01 06 00 07 01 00 00 00 0c 00 10 00 01 00 00 00 02 "
                + "11 46 00 03 00 07 00 08 02 02 02 02 00 09 00 08 00 2a 00 00 ");
        FlowModInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV10(deserializedMessage);
        Assert.assertEquals("Wrong Match", createMatch(), deserializedMessage.getMatchV10());
        Assert.assertEquals("Wrong cookie", Uint64.valueOf("FF01040106000701", 16), deserializedMessage.getCookie());
        Assert.assertEquals("Wrong command", FlowModCommand.forValue(0), deserializedMessage.getCommand());
        Assert.assertEquals("Idle Timeout", 12, deserializedMessage.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong Hard Timeout", 16, deserializedMessage.getHardTimeout().intValue());
        Assert.assertEquals("Wrong priority", 1, deserializedMessage.getPriority().intValue());
        Assert.assertEquals("Wrong buffer id", 2L, deserializedMessage.getBufferId().longValue());
        Assert.assertEquals("Wrong out port", new PortNumber(Uint32.valueOf(4422)), deserializedMessage.getOutPort());
        Assert.assertEquals("Wrong flags", new FlowModFlagsV10(true, false, true), deserializedMessage.getFlagsV10());
        Assert.assertEquals("Wrong actions", createAction(), deserializedMessage.getAction());
    }

    private static List<Action> createAction() {
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
        return actions;
    }

    private static MatchV10 createMatch() {
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
        return matchBuilder.build();
    }
}
