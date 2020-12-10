/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.AsyncConfigMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.FlowRemovedMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PacketInMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.PortStatusMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.FlowRemovedMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PacketInMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.async.config.service.rev170619.async.config.PortStatusMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PacketInReason;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortReason;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class AsyncConfigMessageSerializerTest extends AbstractSerializerTest {

    private static final short LENGTH = 32;
    private static final Uint32 XID = Uint32.valueOf(42);
    private static final Uint8 VERSION = EncodeConstants.OF_VERSION_1_3;

    // Packet in mask
    private static final Boolean MASTER_IS_NOMATCH = true;
    private static final Boolean MASTER_IS_ACTION = false;
    private static final Boolean MASTER_IS_INVALID_TTL = false;
    private static final Boolean SLAVE_IS_NOMATCH = false;
    private static final Boolean SLAVE_IS_ACTION = false;
    private static final Boolean SLAVE_IS_INVALID_TTL = false;

    // Port status mask
    private static final Boolean MASTER_IS_ADD = false;
    private static final Boolean MASTER_IS_DELETE = false;
    private static final Boolean MASTER_IS_MODIFY = false;
    private static final Boolean SLAVE_IS_ADD = false;
    private static final Boolean SLAVE_IS_DELETE = false;
    private static final Boolean SLAVE_IS_MODIFY = false;

    // Flow removed mask
    private static final Boolean MASTER_IS_IDLETIMEOUT = false;
    private static final Boolean MASTER_IS_HARDTIMEOUT = false;
    private static final Boolean MASTER_IS_FLOWDELETE = false;
    private static final Boolean MASTER_IS_GROUPDELETE = false;
    private static final Boolean SLAVE_IS_IDLETIMEOUT = false;
    private static final Boolean SLAVE_IS_HARDTIMEOUT = false;
    private static final Boolean SLAVE_IS_FLOWDELETE = false;
    private static final Boolean SLAVE_IS_GROUPDELETE = false;

    private static final AsyncConfigMessage MESSAGE = new AsyncConfigMessageBuilder()
        .setXid(XID)
        .setVersion(VERSION)
        .setPacketInMask(new PacketInMaskBuilder()
            .setMasterMask(new PacketInMask(MASTER_IS_ACTION, MASTER_IS_INVALID_TTL, MASTER_IS_NOMATCH))
            .setSlaveMask(new PacketInMask(SLAVE_IS_ACTION, SLAVE_IS_INVALID_TTL, SLAVE_IS_NOMATCH))
            .build())
        .setPortStatusMask(new PortStatusMaskBuilder()
            .setMasterMask(new PortStatusMask(MASTER_IS_ADD, MASTER_IS_DELETE, MASTER_IS_MODIFY))
            .setSlaveMask(new PortStatusMask(SLAVE_IS_ADD, SLAVE_IS_DELETE, SLAVE_IS_MODIFY))
            .build())
        .setFlowRemovedMask(new FlowRemovedMaskBuilder()
            .setMasterMask(new FlowRemovedMask(MASTER_IS_FLOWDELETE, MASTER_IS_GROUPDELETE, MASTER_IS_HARDTIMEOUT,
                    MASTER_IS_IDLETIMEOUT))
            .setSlaveMask(new FlowRemovedMask(SLAVE_IS_FLOWDELETE, SLAVE_IS_GROUPDELETE, SLAVE_IS_HARDTIMEOUT,
                    SLAVE_IS_IDLETIMEOUT))
            .build())
        .build();

    private AsyncConfigMessageSerializer serializer;

    @Override
    protected void init() {
        serializer = getRegistry()
                .getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, AsyncConfigMessage.class));
    }

    @Test
    public void testSerialize() {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Header
        assertEquals(out.readByte(), VERSION.byteValue());
        assertEquals(out.readByte(), serializer.getMessageType());
        assertEquals(out.readShort(), LENGTH);
        assertEquals(out.readInt(), XID.intValue());

        // Packet in master
        assertEquals(out.readInt(), ByteBufUtils.fillBitMaskFromMap(ImmutableMap.<Integer, Boolean>builder()
                                                                            .put(PacketInReason.OFPRNOMATCH
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_NOMATCH)
                                                                            .put(PacketInReason.OFPRACTION
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_ACTION)
                                                                            .put(PacketInReason.OFPRINVALIDTTL
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_INVALID_TTL).build()));

        // Packet in slave
        assertEquals(out.readInt(), ByteBufUtils.fillBitMaskFromMap(
                ImmutableMap.<Integer, Boolean>builder().put(PacketInReason.OFPRNOMATCH.getIntValue(), SLAVE_IS_NOMATCH)
                        .put(PacketInReason.OFPRACTION.getIntValue(), SLAVE_IS_ACTION)
                        .put(PacketInReason.OFPRINVALIDTTL.getIntValue(), SLAVE_IS_INVALID_TTL).build()));

        // Port status master
        assertEquals(out.readInt(), ByteBufUtils.fillBitMaskFromMap(
                ImmutableMap.<Integer, Boolean>builder().put(PortReason.OFPPRADD.getIntValue(), MASTER_IS_ADD)
                        .put(PortReason.OFPPRDELETE.getIntValue(), MASTER_IS_DELETE)
                        .put(PortReason.OFPPRMODIFY.getIntValue(), MASTER_IS_MODIFY).build()));

        // Port status slave
        assertEquals(out.readInt(), ByteBufUtils.fillBitMaskFromMap(
                ImmutableMap.<Integer, Boolean>builder().put(PortReason.OFPPRADD.getIntValue(), SLAVE_IS_ADD)
                        .put(PortReason.OFPPRDELETE.getIntValue(), SLAVE_IS_DELETE)
                        .put(PortReason.OFPPRMODIFY.getIntValue(), SLAVE_IS_MODIFY).build()));

        // Flow removed master
        assertEquals(out.readInt(), ByteBufUtils.fillBitMaskFromMap(ImmutableMap.<Integer, Boolean>builder()
                                                                            .put(FlowRemovedReason.OFPRRIDLETIMEOUT
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_IDLETIMEOUT)
                                                                            .put(FlowRemovedReason.OFPRRHARDTIMEOUT
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_HARDTIMEOUT)
                                                                            .put(FlowRemovedReason.OFPRRDELETE
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_FLOWDELETE)
                                                                            .put(FlowRemovedReason.OFPRRGROUPDELETE
                                                                                         .getIntValue(),
                                                                                 MASTER_IS_GROUPDELETE).build()));

        // Flow removed slave
        assertEquals(out.readInt(), ByteBufUtils.fillBitMaskFromMap(ImmutableMap.<Integer, Boolean>builder()
                                                                            .put(FlowRemovedReason.OFPRRIDLETIMEOUT
                                                                                         .getIntValue(),
                                                                                 SLAVE_IS_IDLETIMEOUT)
                                                                            .put(FlowRemovedReason.OFPRRHARDTIMEOUT
                                                                                         .getIntValue(),
                                                                                 SLAVE_IS_HARDTIMEOUT)
                                                                            .put(FlowRemovedReason.OFPRRDELETE
                                                                                         .getIntValue(),
                                                                                 SLAVE_IS_FLOWDELETE)
                                                                            .put(FlowRemovedReason.OFPRRGROUPDELETE
                                                                                         .getIntValue(),
                                                                                 SLAVE_IS_GROUPDELETE).build()));

        assertEquals(out.readableBytes(), 0);
    }

}
