/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
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
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MultipartReplyMeterStatsDeserializer implements OFDeserializer<MultipartReplyBody> {
    private static final byte PADDING_IN_METER_STATS_HEADER = 6;
    private static final byte METER_BODY_LENGTH = 40;
    private static final byte METER_BAND_STATS_LENGTH = 16;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final var items = BindingMap.<MeterStatsKey, MeterStats>orderedBuilder();

        while (message.readableBytes() > 0) {
            final MeterStatsBuilder itemBuilder = new MeterStatsBuilder()
                .setMeterId(new MeterId(readUint32(message)));

            final int itemLength = message.readUnsignedShort();
            message.skipBytes(PADDING_IN_METER_STATS_HEADER);

            itemBuilder
                .setFlowCount(new Counter32(readUint32(message)));

            itemBuilder
                .setPacketInCount(new Counter64(readUint64(message)))
                .setByteInCount(new Counter64(readUint64(message)))
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(readUint32(message)))
                        .setNanosecond(new Counter32(readUint32(message)))
                        .build());

            final var subItems = BindingMap.<BandStatKey, BandStat>orderedBuilder();
            int actualLength = METER_BODY_LENGTH;
            int bandKey = 0;

            while (actualLength < itemLength) {
                subItems.add(new BandStatBuilder()
                    .setBandId(new BandId(Uint32.valueOf(bandKey++)))
                    .setPacketBandCount(new Counter64(readUint64(message)))
                    .setByteBandCount(new Counter64(readUint64(message)))
                    .build());

                actualLength += METER_BAND_STATS_LENGTH;
            }

            items.add(itemBuilder
                    .setMeterBandStats(new MeterBandStatsBuilder()
                        .setBandStat(subItems.build())
                        .build())
                    .build());
        }

        return new MultipartReplyMeterStatsBuilder().setMeterStats(items.build()).build();
    }
}
