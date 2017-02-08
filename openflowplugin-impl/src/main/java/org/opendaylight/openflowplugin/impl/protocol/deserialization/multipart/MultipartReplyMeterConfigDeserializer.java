/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyMeterConfigDeserializer implements OFDeserializer<MultipartReplyBody>, DeserializerRegistryInjector {

    private static final byte METER_CONFIG_LENGTH = 8;

    private static final int OFPMBTDROP = 1;
    private static final int OFPMBTDSCP = 2;
    private static final int OFPMBTEXPERIMENTER = 0xFFFF;
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;

    private DeserializerRegistry registry;

    @Override
    public MultipartReplyBody deserialize(ByteBuf message) {
        final MultipartReplyMeterConfigBuilder builder = new MultipartReplyMeterConfigBuilder();
        final List<MeterConfigStats> items = new ArrayList<>();

        while (message.readableBytes() > 0) {
            final int itemLength = message.readUnsignedShort();

            final MeterConfigStatsBuilder itemBuilder = new MeterConfigStatsBuilder()
                .setFlags(readMeterFlags(message))
                .setMeterId(new MeterId(message.readUnsignedInt()));

            final List<MeterBandHeader> subItems = new ArrayList<>();
            int actualLength = METER_CONFIG_LENGTH;

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
                                    .setDropRate(message.readUnsignedInt())
                                    .setDropBurstSize(message.readUnsignedInt())
                                    .build());
                        message.skipBytes(PADDING_IN_METER_BAND_DROP_HEADER);
                        break;

                    case OFPMBTDSCP:
                        subItemBuilder
                            .setMeterBandTypes(new MeterBandTypesBuilder()
                                    .setFlags(new MeterBandType(false, true, false))
                                    .build())
                            .setBandType(new DscpRemarkBuilder()
                                    .setDscpRemarkRate(message.readUnsignedInt())
                                    .setDscpRemarkBurstSize(message.readUnsignedInt())
                                    .setPrecLevel(message.readUnsignedByte())
                                    .build());
                        message.skipBytes(PADDING_IN_METER_BAND_DSCP_HEADER);
                        break;

                    case OFPMBTEXPERIMENTER:
                        // TODO: Finish meter band experimenter deserialization
                        final long expId = message.getUnsignedInt(message.readerIndex() + 2 * EncodeConstants.SIZE_OF_INT_IN_BYTES);
                        message.readerIndex(itemStartIndex);

                        final OFDeserializer<Experimenter> deserializer = registry.getDeserializer(
                                new ExperimenterIdDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId, Experimenter.class));

                        subItemBuilder
                            .setMeterBandTypes(new MeterBandTypesBuilder()
                                    .setFlags(new MeterBandType(false, false, true))
                                    .build())
                            .setBandType(deserializer.deserialize(message));
                        break;

                }
                subItems.add(subItemBuilder.build());
            }

            items.add(itemBuilder
                .setKey(new MeterConfigStatsKey(itemBuilder.getMeterId()))
                .setMeterBandHeaders(new MeterBandHeadersBuilder()
                    .setMeterBandHeader(subItems)
                    .build())
                .build());
        }

        return builder
            .setMeterConfigStats(items)
            .build();
    }

    private static MeterFlags readMeterFlags(ByteBuf message) {
        int input = message.readUnsignedShort();
        final Boolean mfKBPS = (input & (1)) != 0;
        final Boolean mfPKTPS = (input & (1 << 1)) != 0;
        final Boolean mfBURST = (input & (1 << 2)) != 0;
        final Boolean mfSTATS = (input & (1 << 3)) != 0;
        return new MeterFlags(mfBURST, mfKBPS, mfPKTPS, mfSTATS);
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
