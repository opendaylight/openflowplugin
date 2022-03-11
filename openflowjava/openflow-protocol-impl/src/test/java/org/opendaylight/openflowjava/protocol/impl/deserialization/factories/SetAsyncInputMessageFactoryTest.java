/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * Unit tests for SetAsyncInputMessageFactory.
 *
 * @author giuseppex.petralia@intel.com
 */
public class SetAsyncInputMessageFactoryTest {
    private OFDeserializer<SetAsyncInput> factory;

    @Before
    public void startUp() {
        DeserializerRegistry desRegistry = new DeserializerRegistryImpl();
        desRegistry.init();
        factory = desRegistry
                .getDeserializer(new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, 28, SetAsyncInput.class));

    }

    @Test
    public void test() {
        ByteBuf bb = BufferHelper
                .buildBuffer("00 00 00 07 00 00 00 00 00 00 00 " + "07 00 00 00 00 00 00 00 0f 00 00 00 00");
        SetAsyncInput deserializedMessage = BufferHelper.deserialize(factory, bb);
        BufferHelper.checkHeaderV13(deserializedMessage);

        Assert.assertEquals("Wrong packet in mask ", createPacketInMask().get(0),
                deserializedMessage.getPacketInMask().get(0));
        Assert.assertEquals("Wrong packet in mask ", createPacketInMask().get(1),
                deserializedMessage.getPacketInMask().get(1));
        Assert.assertEquals("Wrong port status mask ", createPortStatusMask().get(0),
                deserializedMessage.getPortStatusMask().get(0));
        Assert.assertEquals("Wrong port status mask ", createPortStatusMask().get(1),
                deserializedMessage.getPortStatusMask().get(1));
        Assert.assertEquals("Wrong flow removed mask ", createFlowRemowedMask().get(0),
                deserializedMessage.getFlowRemovedMask().get(0));
        Assert.assertEquals("Wrong flow removed mask ", createFlowRemowedMask().get(1),
                deserializedMessage.getFlowRemovedMask().get(1));

    }

    private static List<PacketInMask> createPacketInMask() {
        return List.of(
            // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
            new PacketInMaskBuilder()
                .setMask(Set.of(PacketInReason.OFPRNOMATCH, PacketInReason.OFPRACTION, PacketInReason.OFPRINVALIDTTL))
                .build(),
            // OFPCR_ROLE_SLAVE
            new PacketInMaskBuilder().setMask(Set.of()).build());
    }

    private static List<PortStatusMask> createPortStatusMask() {
        return List.of(
            // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
            new PortStatusMaskBuilder()
                .setMask(Set.of(PortReason.OFPPRADD, PortReason.OFPPRDELETE, PortReason.OFPPRMODIFY))
                .build(),
            // OFPCR_ROLE_SLAVE
            new PortStatusMaskBuilder().setMask(Set.of()).build());
    }

    private static List<FlowRemovedMask> createFlowRemowedMask() {
        return List.of(
            // OFPCR_ROLE_EQUAL or OFPCR_ROLE_MASTER
            new FlowRemovedMaskBuilder()
                .setMask(Set.of(
                    FlowRemovedReason.OFPRRIDLETIMEOUT,
                    FlowRemovedReason.OFPRRHARDTIMEOUT,
                    FlowRemovedReason.OFPRRDELETE,
                    FlowRemovedReason.OFPRRGROUPDELETE))
                .build(),
            // OFPCR_ROLE_SLAVE
            new FlowRemovedMaskBuilder().setMask(Set.of()).build());
    }
}
