/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyGroupStatsDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final byte PADDING_IN_GROUP_HEADER_01 = 2;
    private static final byte PADDING_IN_GROUP_HEADER_02 = 4;

    private static final short ITEM_LENGTH = 56;
    private static final int GROUP_ID = 3;
    private static final int REF_COUNT = 4;
    private static final int SECOND = 5;
    private static final int NANOSECOND = 6;
    private static final long PACKET_COUNT = 1L;
    private static final long BYTE_COUNT = 2L;

    @Test
    public void testDeserialize() {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeShort(ITEM_LENGTH);
        buffer.writeZero(PADDING_IN_GROUP_HEADER_01);
        buffer.writeInt(GROUP_ID);
        buffer.writeInt(REF_COUNT);
        buffer.writeZero(PADDING_IN_GROUP_HEADER_02);
        buffer.writeLong(PACKET_COUNT);
        buffer.writeLong(BYTE_COUNT);
        buffer.writeInt(SECOND);
        buffer.writeInt(NANOSECOND);
        buffer.writeLong(PACKET_COUNT);
        buffer.writeLong(BYTE_COUNT);

        final MultipartReplyGroupStats reply = (MultipartReplyGroupStats) deserializeMultipart(buffer);
        final GroupStats firstGroupStats = reply.getGroupStats().values().iterator().next();
        assertEquals(GROUP_ID, firstGroupStats.getGroupId().getValue().intValue());
        assertEquals(REF_COUNT, firstGroupStats.getRefCount().getValue().intValue());
        assertEquals(PACKET_COUNT, firstGroupStats.getPacketCount().getValue().longValue());
        assertEquals(BYTE_COUNT, firstGroupStats.getByteCount().getValue().longValue());
        assertEquals(SECOND, firstGroupStats.getDuration().getSecond().getValue().intValue());
        assertEquals(NANOSECOND, firstGroupStats.getDuration().getNanosecond().getValue().intValue());
        assertEquals(PACKET_COUNT, firstGroupStats.getPacketCount().getValue().longValue());
        assertEquals(BYTE_COUNT, firstGroupStats.getByteCount().getValue().longValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPGROUP.getIntValue();
    }
}