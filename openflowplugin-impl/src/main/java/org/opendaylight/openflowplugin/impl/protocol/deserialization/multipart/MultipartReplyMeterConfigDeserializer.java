/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStatsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MultipartReplyMeterConfigDeserializer implements OFDeserializer<MultipartReplyBody>,
        DeserializerRegistryInjector {
    private static final byte METER_CONFIG_LENGTH = 8;
    private static final int OFPMBTDROP = 1;
    private static final int OFPMBTDSCP = 2;
    private static final int OFPMBTEXPERIMENTER = 0xFFFF;
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;

    private DeserializerRegistry registry = null;

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        final MultipartReplyMeterConfigBuilder builder = new MultipartReplyMeterConfigBuilder();
        final var items = BindingMap.<MeterConfigStatsKey, MeterConfigStats>orderedBuilder();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();

            final MeterConfigStatsBuilder itemBuilder = new MeterConfigStatsBuilder()
                    .setFlags(readMeterFlags(message))
                    .setMeterId(new MeterId(readUint32(message)));

            final var subItems = BindingMap.<MeterBandHeaderKey, MeterBandHeader>orderedBuilder();
            int actualLength = METER_CONFIG_LENGTH;
            long bandKey = 0;

            while (actualLength < itemLength) {
                final int itemStartIndex = message.readerIndex();
                final int itemBandType = message.readUnsignedShort();
                final MeterBandHeaderBuilder subItemBuilder = new MeterBandHeaderBuilder();
                actualLength += message.readUnsignedShort();

                switch (itemBandType) {
                    case OFPMBTDROP:
                        subItemBuilder
                                .setMeterBandTypes(new MeterBandTypesBuilder()
                                        .setFlags(new MeterBandType(true, false, false))
                                        .build())
                                .setBandType(new DropBuilder()
                                        .setDropRate(readUint32(message))
                                        .setDropBurstSize(readUint32(message))
                                        .build());
                        message.skipBytes(PADDING_IN_METER_BAND_DROP_HEADER);
                        break;

                    case OFPMBTDSCP:
                        subItemBuilder
                                .setMeterBandTypes(new MeterBandTypesBuilder()
                                        .setFlags(new MeterBandType(false, true, false))
                                        .build())
                                .setBandType(new DscpRemarkBuilder()
                                        .setDscpRemarkRate(readUint32(message))
                                        .setDscpRemarkBurstSize(readUint32(message))
                                        .setPrecLevel(readUint8(message))
                                        .build());
                        message.skipBytes(PADDING_IN_METER_BAND_DSCP_HEADER);
                        break;

                    case OFPMBTEXPERIMENTER:
                        // TODO: Finish meter band experimenter deserialization
                        final Uint32 expId =
                            Uint32.fromIntBits(message.getInt(message.readerIndex() + 2 * Integer.BYTES));
                        message.readerIndex(itemStartIndex);

                        final OFDeserializer<Experimenter> deserializer = registry.getDeserializer(
                                new ExperimenterIdDeserializerKey(EncodeConstants.OF_VERSION_1_3, expId,
                                        Experimenter.class));

                        subItemBuilder
                                .setMeterBandTypes(new MeterBandTypesBuilder()
                                        .setFlags(new MeterBandType(false, false, true))
                                        .build())
                                .setBandType(deserializer.deserialize(message));
                        break;
                    default:
                        // no operation

                }
                subItems.add(subItemBuilder.withKey(new MeterBandHeaderKey(new BandId(Uint32.valueOf(bandKey++))))
                    .build());
            }

            items.add(itemBuilder
                    .withKey(new MeterConfigStatsKey(itemBuilder.getMeterId()))
                    .setMeterBandHeaders(new MeterBandHeadersBuilder()
                            .setMeterBandHeader(subItems.build())
                            .build())
                    .build());
        }

        return builder
                .setMeterConfigStats(items.build())
                .build();
    }

    private static MeterFlags readMeterFlags(final ByteBuf message) {
        int input = message.readUnsignedShort();
        final Boolean mfKbps = (input & 1) != 0;
        final Boolean mfPktps = (input & 1 << 1) != 0;
        final Boolean mfBurst = (input & 1 << 2) != 0;
        final Boolean mfStats = (input & 1 << 3) != 0;
        return new MeterFlags(mfBurst, mfKbps, mfPktps, mfStats);
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = requireNonNull(deserializerRegistry);
    }
}
