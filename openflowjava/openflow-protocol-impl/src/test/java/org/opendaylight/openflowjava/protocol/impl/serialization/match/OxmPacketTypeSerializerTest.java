/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PacketType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PacketTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PacketTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.packet.type._case.PacketTypeBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class OxmPacketTypeSerializerTest {
    private ByteBuf buffer;
    private OxmPacketTypeSerializer serializer;

    @Before
    public void setup() {
        buffer = PooledByteBufAllocator.DEFAULT.buffer();
        serializer = new OxmPacketTypeSerializer();
    }

    @Test
    public void serializeTest() {
        final long packetType = 0x1894f;
        MatchEntry matchEntry = createMatchEntry(Uint32.valueOf(packetType));

        serializer.serialize(matchEntry, buffer);

        assertEquals(OxmMatchConstants.OPENFLOW_BASIC_CLASS, buffer.readUnsignedShort());
        short fieldMask = buffer.readUnsignedByte();
        assertEquals(OxmMatchConstants.PACKET_TYPE, fieldMask >> 1);
        assertEquals(0, fieldMask & 1);
        assertEquals(Integer.BYTES, buffer.readUnsignedByte());
        assertEquals(packetType, buffer.readUnsignedInt());
    }

    private static MatchEntry createMatchEntry(Uint32 packetType) {
        PacketTypeCase packetTypeCase = new PacketTypeCaseBuilder()
                .setPacketType(new PacketTypeBuilder().setPacketType(packetType).build())
                .build();
        return new MatchEntryBuilder()
                .setOxmClass(OpenflowBasicClass.class)
                .setOxmMatchField(PacketType.class)
                .setHasMask(false)
                .setMatchEntryValue(packetTypeCase)
                .build();
    }
}
