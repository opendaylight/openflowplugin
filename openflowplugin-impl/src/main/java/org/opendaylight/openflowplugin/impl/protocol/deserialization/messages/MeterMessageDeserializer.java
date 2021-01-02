/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.binding.util.BindingMap.Builder;

public class MeterMessageDeserializer implements OFDeserializer<MeterMessage>, DeserializerRegistryInjector {
    private static final int OFPMBTDROP = 1;
    private static final int OFPMBTDSCP = 2;
    private static final int OFPMBTEXPERIMENTER = 0xFFFF;
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;

    private DeserializerRegistry registry;

    @Override
    public MeterMessage deserialize(final ByteBuf message) {
        final MeterMessageBuilder builder = new MeterMessageBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(message))
                .setCommand(MeterModCommand.forValue(message.readUnsignedShort()))
                .setFlags(readMeterFlags(message))
                .setMeterId(new MeterId(readUint32(message)));

        final Builder<MeterBandHeaderKey, MeterBandHeader> bands = BindingMap.builder();
        long key = 0;

        while (message.readableBytes() > 0) {
            final MeterBandHeaderBuilder bandBuilder = new MeterBandHeaderBuilder();
            final int bandStartIndex = message.readerIndex();
            final int bandType = message.readUnsignedShort();

            switch (bandType) {
                case OFPMBTDROP: {
                    bandBuilder
                            .setMeterBandTypes(new MeterBandTypesBuilder()
                                    .setFlags(new MeterBandType(true, false, false))
                                    .build())
                            .setBandType(new DropBuilder()
                                    .setDropRate(readUint32(message))
                                    .setDropBurstSize(readUint32(message))
                                    .build());
                    message.skipBytes(PADDING_IN_METER_BAND_DROP_HEADER);
                    break;
                }
                case OFPMBTDSCP: {
                    bandBuilder
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
                }
                case OFPMBTEXPERIMENTER: {
                    // TODO: Finish meter band experimenter deserialization
                    long expId = message.getUnsignedInt(message.readerIndex() + 2 * Integer.BYTES);
                    message.readerIndex(bandStartIndex);

                    OFDeserializer<Experimenter> deserializer = registry.getDeserializer(
                            new ExperimenterIdDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId,
                                    Experimenter.class));

                    bandBuilder
                            .setMeterBandTypes(new MeterBandTypesBuilder()
                                    .setFlags(new MeterBandType(false, false, true))
                                    .build())
                            .setBandType(deserializer.deserialize(message));
                    break;
                }
                default:
                    // no operation
            }

            bands.add(bandBuilder.setBandId(new BandId(key++)).build());
        }

        return builder
                .setMeterBandHeaders(new MeterBandHeadersBuilder()
                        .setMeterBandHeader(bands.build())
                        .build())
                .build();
    }


    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

    private static MeterFlags readMeterFlags(final ByteBuf message) {
        int input = message.readUnsignedShort();
        final Boolean mfKbps = (input & 1) != 0;
        final Boolean mfPktps = (input & 1 << 1) != 0;
        final Boolean mfBurst = (input & 1 << 2) != 0;
        final Boolean mfStats = (input & 1 << 3) != 0;
        return new MeterFlags(mfBurst, mfKbps, mfPktps, mfStats);
    }
}
