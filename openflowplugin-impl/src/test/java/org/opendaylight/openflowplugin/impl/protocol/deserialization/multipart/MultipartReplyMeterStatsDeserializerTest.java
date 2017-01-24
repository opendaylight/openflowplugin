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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyMeterStatsDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final byte PADDING_IN_METER_STATS_HEADER = 6;
    private static final int METER_ID = 1;
    private static final short ITEM_LENGTH = 50;
    private static final int FLOW_COUNT = 2;
    private static final long PACKET_IN_COUNT = 3l;
    private static final long BYTE_IN_COUNT = 4l;
    private static final int SECOND = 5;
    private static final int NANOSECOND = 6;
    private static final long PACKET_BAND_COUNT = 7l;
    private static final long BYTE_BAND_COUNT = 8l;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeInt(METER_ID);
        buffer.writeShort(ITEM_LENGTH);
        buffer.writeZero(PADDING_IN_METER_STATS_HEADER);
        buffer.writeInt(FLOW_COUNT);
        buffer.writeLong(PACKET_IN_COUNT);
        buffer.writeLong(BYTE_IN_COUNT);
        buffer.writeInt(SECOND);
        buffer.writeInt(NANOSECOND);
        buffer.writeLong(PACKET_BAND_COUNT);
        buffer.writeLong(BYTE_BAND_COUNT);

        final MultipartReplyMeterStats reply = (MultipartReplyMeterStats) deserializeMultipart(buffer);

        final MeterStats meterStats = reply.getMeterStats().get(0);

        assertEquals(METER_ID, meterStats.getMeterId().getValue().intValue());
        assertEquals(FLOW_COUNT, meterStats.getFlowCount().getValue().intValue());
        assertEquals(PACKET_IN_COUNT, meterStats.getPacketInCount().getValue().longValue());
        assertEquals(BYTE_IN_COUNT, meterStats.getByteInCount().getValue().intValue());
        assertEquals(SECOND, meterStats.getDuration().getSecond().getValue().intValue());
        assertEquals(NANOSECOND, meterStats.getDuration().getNanosecond().getValue().intValue());
        assertEquals(PACKET_BAND_COUNT, meterStats.getMeterBandStats().getBandStat().get(0)
                .getPacketBandCount().getValue().longValue());
        assertEquals(BYTE_BAND_COUNT, meterStats.getMeterBandStats().getBandStat().get(0)
                .getByteBandCount().getValue().longValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPMETER.getIntValue();
    }
}