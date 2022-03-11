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
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.util.BufferHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.async.body.grouping.PortStatusMaskBuilder;

/**
 * Unit tests for SetAsyncInputMessageFactory.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class SetAsyncInputMessageFactoryTest {

    private SerializerRegistry registry;
    private OFSerializer<SetAsyncInput> setAsyncFactory;

    /**
     * Initializes serializer registry and stores correct factory in field.
     */
    @Before
    public void startUp() {
        registry = new SerializerRegistryImpl();
        registry.init();
        setAsyncFactory = registry.getSerializer(
                new MessageTypeKey<>(EncodeConstants.OF_VERSION_1_3, SetAsyncInput.class));
    }

    /**
     * Testing of {@link SetAsyncInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testSetAsyncInputMessage() throws Exception {
        SetAsyncInputBuilder builder = new SetAsyncInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPacketInMask(createPacketInMask());
        builder.setPortStatusMask(createPortStatusMask());
        builder.setFlowRemovedMask(createFlowRemowedMask());
        SetAsyncInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        setAsyncFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out,(byte) 28, 32);
        Assert.assertEquals("Wrong packetInMask", 7, out.readUnsignedInt());
        Assert.assertEquals("Wrong packetInMask", 0, out.readUnsignedInt());
        Assert.assertEquals("Wrong portStatusMask", 7, out.readUnsignedInt());
        Assert.assertEquals("Wrong portStatusMask", 0, out.readUnsignedInt());
        Assert.assertEquals("Wrong flowRemovedMask", 15, out.readUnsignedInt());
        Assert.assertEquals("Wrong flowRemovedMask", 0, out.readUnsignedInt());

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

    /**
     * Testing of {@link SetAsyncInputMessageFactory} for correct translation from POJO.
     */
    @Test
    public void testSetAsyncInputWithNullMasks() throws Exception {
        SetAsyncInputBuilder builder = new SetAsyncInputBuilder();
        BufferHelper.setupHeader(builder, EncodeConstants.OF13_VERSION_ID);
        builder.setPacketInMask(null);
        builder.setPortStatusMask(null);
        builder.setFlowRemovedMask(null);
        SetAsyncInput message = builder.build();

        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        setAsyncFactory.serialize(message, out);

        BufferHelper.checkHeaderV13(out,(byte) 28, 8);
        Assert.assertTrue("Unexpected data", out.readableBytes() == 0);
    }
}
