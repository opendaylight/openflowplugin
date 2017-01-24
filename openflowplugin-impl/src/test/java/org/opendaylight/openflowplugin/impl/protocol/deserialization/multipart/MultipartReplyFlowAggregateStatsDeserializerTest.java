/**
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowAggregateStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyFlowAggregateStatsDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final long PACKET_COUNT = 6l;
    private static final long BYTE_COUNT = 256l;
    private static final int FLOW_COUNT = 3;
    private static final byte PADDING_IN_MULTIPART_REPLY_HEADER = 4;

    @Test
    public void testDeserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeLong(PACKET_COUNT);
        buffer.writeLong(BYTE_COUNT);
        buffer.writeInt(FLOW_COUNT);
        buffer.writeZero(PADDING_IN_MULTIPART_REPLY_HEADER);

        final MultipartReplyFlowAggregateStats reply = (MultipartReplyFlowAggregateStats) deserializeMultipart(buffer);
        assertEquals(PACKET_COUNT, reply.getPacketCount().getValue().longValue());
        assertEquals(BYTE_COUNT, reply.getByteCount().getValue().longValue());
        assertEquals(FLOW_COUNT, reply.getFlowCount().getValue().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPAGGREGATE.getIntValue();
    }
}