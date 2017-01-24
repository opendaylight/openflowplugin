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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;

public class MultipartReplyQueueStatsDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final int PORT = 1;
    private static final int QUEUE_ID = 1;
    private static final long TRANSMITTED_BYTES = 5l;
    private static final long TRANSMITTED_PACKETS = 3l;
    private static final long TRANSMISSON_ERRORS = 9l;
    private static final int SECOND = 14;
    private static final int NANOSECOND = 15;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeInt(PORT);
        buffer.writeInt(QUEUE_ID);
        buffer.writeLong(TRANSMITTED_BYTES);
        buffer.writeLong(TRANSMITTED_PACKETS);
        buffer.writeLong(TRANSMISSON_ERRORS);
        buffer.writeInt(SECOND);
        buffer.writeInt(NANOSECOND);

        final MultipartReplyQueueStats reply = (MultipartReplyQueueStats) deserializeMultipart(buffer);

        final QueueIdAndStatisticsMap queueStats = reply.getQueueIdAndStatisticsMap().get(0);

        assertEquals(QUEUE_ID, queueStats.getQueueId().getValue().intValue());
        assertEquals(TRANSMITTED_BYTES, queueStats.getTransmittedBytes().getValue().longValue());
        assertEquals(TRANSMITTED_PACKETS, queueStats.getTransmittedPackets().getValue().longValue());
        assertEquals(TRANSMISSON_ERRORS, queueStats.getTransmissionErrors().getValue().longValue());
        assertEquals(SECOND, queueStats.getDuration().getSecond().getValue().intValue());
        assertEquals(NANOSECOND, queueStats.getDuration().getNanosecond().getValue().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPQUEUE.getIntValue();
    }
}