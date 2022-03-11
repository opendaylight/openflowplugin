/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * Unit tests for GetAsyncReplyMessageFactory.
 *
 * @author timotej.kubas
 */
public class GetAsyncReplyMessageFactoryTest {

    private OFDeserializer<GetAsyncOutput> asyncFactory;

    /**
     * Initializes deserializer registry and lookups correct deserializer.
     */
    @Before
    public void startUp() {
        DeserializerRegistry registry = new DeserializerRegistryImpl();
        registry.init();
        asyncFactory = registry.getDeserializer(
                new MessageCodeKey(EncodeConstants.OF_VERSION_1_3, 27, GetAsyncOutput.class));
    }

    /**
     * Testing {@link GetAsyncReplyMessageFactory} for correct translation into POJO.
     */
    @Test
    public void testGetAsyncReplyMessage() {
        ByteBuf bb = BufferHelper.buildBuffer(
                "00 00 00 07 " + "00 00 00 00 " + "00 00 00 07 " + "00 00 00 00 " + "00 00 00 0F " + "00 00 00 00");
        GetAsyncOutput builtByFactory = BufferHelper.deserialize(asyncFactory, bb);

        BufferHelper.checkHeaderV13(builtByFactory);
        Assert.assertEquals("Wrong packetInMask", createPacketInMask(), builtByFactory.getPacketInMask());
        Assert.assertEquals("Wrong portStatusMask", createPortStatusMask(), builtByFactory.getPortStatusMask());
        Assert.assertEquals("Wrong flowRemovedMask", createFlowRemovedMask(), builtByFactory.getFlowRemovedMask());
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

    private static List<FlowRemovedMask> createFlowRemovedMask() {
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
