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
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for PacketOutInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class PacketOutInputMessageFactoryTest {
    private OFDeserializer<PacketOutInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        factory = registry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF13_VERSION_ID, 13, PacketOutInput.class));

    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer(
            "00 00 01 00 00 00 01 00 00 28 00 00 00 00 00 00 00 11 00 08 00 19 00 00 00 12 00 08 00 00 00 00 00 12 "
            + "00 08 00 00 00 00 00 12 00 08 00 00 00 00 00 12 00 08 00 00 00 00 00 00 01 02 03 04 05 06 07 08 09 10 "
            + "11 12 13 14");
        PacketOutInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        Assert.assertEquals("Wrong buffer Id", 256L, deserializedMessage.getBufferId().longValue());
        Assert.assertEquals("Wrong In Port", new PortNumber(Uint32.valueOf(256)), deserializedMessage.getInPort());
        Assert.assertEquals("Wrong Numbers of actions", createAction(), deserializedMessage.getAction());
        byte[] data = ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14");
        Assert.assertArrayEquals("Wrong data", data, deserializedMessage.getData());
    }

    private static List<Action> createAction() {
        final List<Action> actions = new ArrayList<>();
        ActionBuilder actionBuilder = new ActionBuilder();
        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanBuilder = new PushVlanActionBuilder();
        pushVlanBuilder.setEthertype(new EtherType(new EtherType(Uint16.valueOf(25))));
        pushVlanCaseBuilder.setPushVlanAction(pushVlanBuilder.build());
        actionBuilder.setActionChoice(pushVlanCaseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new PopVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        return actions;
    }
}
