/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.openflowplugin.impl.util.PacketInUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketInMessage;

public class PacketInMessageDeserializerTest extends AbstractDeserializerTest {
    private static final byte PADDING_IN_PACKET_IN_HEADER = 2;
    private static final short REASON = 0; // no match
    private static final short TABLE_ID = 13;
    private static final long FLOW_COOKIE = 12;
    private static final int XID = 42;
    private static final int TYPE = 10;
    private static final int OXM_MATCH_TYPE_CODE = 1;
    private static final int MPLS_LABEL = 135;
    private static final byte[] PAYLOAD = new byte[] { 1, 2, 3, 4, 5 };

    private ByteBuf buffer;

    @Override
    protected void init() {
        buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserialize() {
        buffer.writeByte(TYPE);
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeInt(XID);
        buffer.writeInt(EncodeConstants.EMPTY_VALUE); // Buffer id - irrelevant
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH); // Total len - irrelevant
        buffer.writeByte(REASON);
        buffer.writeByte(TABLE_ID);
        buffer.writeLong(FLOW_COOKIE);

        // Match header
        int matchStartIndex = buffer.writerIndex();
        buffer.writeShort(OXM_MATCH_TYPE_CODE);
        int matchLengthIndex = buffer.writerIndex();
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);

        // MplsLabel match
        buffer.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        buffer.writeByte(OxmMatchConstants.MPLS_LABEL << 1);
        buffer.writeByte(Integer.BYTES);
        buffer.writeInt(MPLS_LABEL);

        // Match footer
        int matchLength = buffer.writerIndex() - matchStartIndex;
        buffer.setShort(matchLengthIndex, matchLength);
        int paddingRemainder = matchLength % EncodeConstants.PADDING;
        if (paddingRemainder != 0) {
            buffer.writeZero(EncodeConstants.PADDING - paddingRemainder);
        }

        buffer.writeZero(PADDING_IN_PACKET_IN_HEADER);
        buffer.writeBytes(PAYLOAD);

        final PacketInMessage message =
                (PacketInMessage) getFactory().deserialize(buffer, EncodeConstants.OF13_VERSION_ID);

        assertEquals(XID, message.getXid().intValue());
        assertEquals(PacketInUtil.getMdSalPacketInReason(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                .types.rev130731.PacketInReason
                .forValue(REASON)), message.getPacketInReason());
        assertEquals(TABLE_ID, message.getTableId().getValue().shortValue());
        assertEquals(FLOW_COOKIE, message.getFlowCookie().getValue().longValue());
        assertEquals(MPLS_LABEL, message.getMatch().getProtocolMatchFields().getMplsLabel().intValue());
        assertArrayEquals(PAYLOAD, message.getPayload());
        assertEquals(buffer.readableBytes(), 0);
    }

}
