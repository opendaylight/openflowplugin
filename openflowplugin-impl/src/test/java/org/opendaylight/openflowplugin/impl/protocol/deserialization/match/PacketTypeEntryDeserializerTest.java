/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.match;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yangtools.yang.common.Uint32;

public class PacketTypeEntryDeserializerTest extends AbstractMatchEntryDeserializerTest {
    @Test
    public void deserializeEntryTest() {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final Uint32 packetType = Uint32.valueOf(0x1894fL);

        writeHeader(in, false);
        in.writeInt(packetType.intValue());

        final Match match = deserialize(in);
        assertEquals(packetType, match.getPacketTypeMatch().getPacketType());
    }

    @Override
    protected int getOxmFieldCode() {
        return OxmMatchConstants.PACKET_TYPE;
    }

    @Override
    protected int getOxmClassCode() {
        return OxmMatchConstants.OPENFLOW_BASIC_CLASS;
    }

    @Override
    protected int getValueLength() {
        return Integer.BYTES;
    }
}
