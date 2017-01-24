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
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;

public class MultipartReplyPortStatsDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final byte PADDING_IN_PORT_STATS_HEADER = 4;
    private static final int PORT = 1;
    private static final long PACKETS_RECEIVED = 2l;
    private static final long PACKETS_TRANSMITTED = 3l;
    private static final long BYTES_RECEIVED = 4l;
    private static final long BYTES_TRANSMITTED = 5l;
    private static final long RECEIVE_DROPS = 6l;
    private static final long TRANSMIT_DROPS = 7l;
    private static final long RECEIVE_ERRORS = 8l;
    private static final long TRANSMIT_ERRORS = 9l;
    private static final long RECEIVE_FRAME_ERROR = 10l;
    private static final long RECEIVE_OVER_RUN_ERROR = 11l;
    private static final long RECEIVE_CRC_ERROR = 12l;
    private static final long COLLIESION_COUNT = 13l;
    private static final int SECOND = 14;
    private static final int NANOSECOND = 15;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeInt(PORT);
        buffer.writeZero(PADDING_IN_PORT_STATS_HEADER);
        buffer.writeLong(PACKETS_RECEIVED);
        buffer.writeLong(PACKETS_TRANSMITTED);
        buffer.writeLong(BYTES_RECEIVED);
        buffer.writeLong(BYTES_TRANSMITTED);
        buffer.writeLong(RECEIVE_DROPS);
        buffer.writeLong(TRANSMIT_DROPS);
        buffer.writeLong(RECEIVE_ERRORS);
        buffer.writeLong(TRANSMIT_ERRORS);
        buffer.writeLong(RECEIVE_FRAME_ERROR);
        buffer.writeLong(RECEIVE_OVER_RUN_ERROR);
        buffer.writeLong(RECEIVE_CRC_ERROR);
        buffer.writeLong(COLLIESION_COUNT);
        buffer.writeInt(SECOND);
        buffer.writeInt(NANOSECOND);

        final MultipartReplyPortStats reply = (MultipartReplyPortStats) deserializeMultipart(buffer);

        final NodeConnectorStatisticsAndPortNumberMap portStats = reply.getNodeConnectorStatisticsAndPortNumberMap().get(0);
        assertEquals(PACKETS_RECEIVED, portStats.getPackets().getReceived().longValue());
        assertEquals(PACKETS_TRANSMITTED, portStats.getPackets().getTransmitted().longValue());
        assertEquals(BYTES_RECEIVED, portStats.getBytes().getReceived().longValue());
        assertEquals(BYTES_TRANSMITTED, portStats.getBytes().getTransmitted().longValue());
        assertEquals(RECEIVE_DROPS, portStats.getReceiveDrops().longValue());
        assertEquals(TRANSMIT_DROPS, portStats.getTransmitDrops().longValue());
        assertEquals(RECEIVE_ERRORS, portStats.getReceiveErrors().longValue());
        assertEquals(TRANSMIT_ERRORS, portStats.getTransmitErrors().longValue());
        assertEquals(RECEIVE_FRAME_ERROR, portStats.getReceiveFrameError().longValue());
        assertEquals(RECEIVE_OVER_RUN_ERROR, portStats.getReceiveOverRunError().longValue());
        assertEquals(RECEIVE_CRC_ERROR, portStats.getReceiveCrcError().longValue());
        assertEquals(COLLIESION_COUNT, portStats.getCollisionCount().longValue());
        assertEquals(SECOND, portStats.getDuration().getSecond().getValue().intValue());
        assertEquals(NANOSECOND, portStats.getDuration().getNanosecond().getValue().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPPORTSTATS.getIntValue();
    }
}