/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.meter.band.stats.BandStatKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyMeterStatsDeserializer implements OFDeserializer<MultipartReplyBody> {

    private static final byte PADDING_IN_METER_STATS_HEADER = 6;
    private static final byte METER_BODY_LENGTH = 40;
    private static final byte METER_BAND_STATS_LENGTH = 16;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyMeterStatsBuilder builder = new MultipartReplyMeterStatsBuilder();
        final List<MeterStats> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final MeterStatsBuilder itemBuilder = new MeterStatsBuilder()
                .setMeterId(new MeterId(message.readUnsignedInt()));

            final int itemLength = message.readUnsignedShort();
            message.skipBytes(PADDING_IN_METER_STATS_HEADER);

            itemBuilder
                .setKey(new MeterStatsKey(itemBuilder.getMeterId()))
                .setFlowCount(new Counter32(message.readUnsignedInt()));

            final byte[] packetCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(packetCount);
            final byte[] byteCount = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
            message.readBytes(byteCount);

            itemBuilder
                .setPacketInCount(new Counter64(new BigInteger(1, packetCount)))
                .setByteInCount(new Counter64(new BigInteger(1, byteCount)))
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(message.readUnsignedInt()))
                        .setNanosecond(new Counter32(message.readUnsignedInt()))
                        .build());

            final List<BandStat> subItems = new ArrayList<>();
            int actualLength = METER_BODY_LENGTH;
            long bandKey = 0;

            while (actualLength < itemLength) {
                final byte[] packetCountB = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
                message.readBytes(packetCountB);
                final byte[] byteCountB = new byte[EncodeConstants.SIZE_OF_LONG_IN_BYTES];
                message.readBytes(byteCountB);

                subItems.add(new BandStatBuilder()
                    .setBandId(new BandId(bandKey))
                    .setKey(new BandStatKey(new BandId(bandKey)))
                    .setPacketBandCount(new Counter64(new BigInteger(1, packetCountB)))
                    .setByteBandCount(new Counter64(new BigInteger(1, byteCountB)))
                    .build());

                bandKey++;
                actualLength += METER_BAND_STATS_LENGTH;
            }

            items.add(itemBuilder
                    .setMeterBandStats(new MeterBandStatsBuilder()
                        .setBandStat(subItems)
                        .build())
                    .build());
        }

        return builder
            .setMeterStats(items)
            .build();
    }

}
