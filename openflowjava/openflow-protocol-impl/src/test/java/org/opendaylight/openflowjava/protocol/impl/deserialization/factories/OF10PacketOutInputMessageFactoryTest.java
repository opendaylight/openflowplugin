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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for OF10PacketOutInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class OF10PacketOutInputMessageFactoryTest {
    private OFDeserializer<PacketOutInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF10_VERSION_ID, 13, PacketOutInput.class));
    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper.buildBuffer("00 00 01 00 01 01 00 10 00 00 00 08 "
                + "00 2a 00 32 00 03 00 08 00 00 00 00 00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14");

        PacketOutInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV10(deserializedMessage);
        Assert.assertEquals("Wrong bufferId ", 256L, deserializedMessage.getBufferId().longValue());
        Assert.assertEquals("Wrong inPort ", new PortNumber(Uint32.valueOf(257)), deserializedMessage.getInPort());
        Assert.assertEquals("Wrong action ", createActionList().get(0), deserializedMessage.getAction().get(0));
        Assert.assertEquals("Wrong action ", createActionList().get(1), deserializedMessage.getAction().get(1));
        Assert.assertArrayEquals("Wrong data ",
                ByteBufUtils.hexStringToBytes("00 00 01 02 03 04 05 06 07 08 09 10 11 12 13 14"),
                deserializedMessage.getData());
    }

    private static List<Action> createActionList() {
        final List<Action> actions = new ArrayList<>();
        OutputActionCaseBuilder caseBuilder = new OutputActionCaseBuilder();
        OutputActionBuilder outputBuilder = new OutputActionBuilder();
        outputBuilder.setPort(new PortNumber(Uint32.valueOf(42)));
        outputBuilder.setMaxLength(Uint16.valueOf(50));
        caseBuilder.setOutputAction(outputBuilder.build());
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(caseBuilder.build());
        actions.add(actionBuilder.build());
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(new StripVlanCaseBuilder().build());
        actions.add(actionBuilder.build());
        return actions;
    }
}
