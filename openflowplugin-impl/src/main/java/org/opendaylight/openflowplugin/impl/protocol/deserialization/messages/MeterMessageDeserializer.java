/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;

public class MeterMessageDeserializer implements OFDeserializer<MeterMessage>, DeserializerRegistryInjector {
    private static final int OFPMBTDROP = 1;
    private static final int OFPMBTDSCP = 2;
    private static final int OFPMBTEXPERIMENTER = 0xFFFF;
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;
    private DeserializerRegistry registry;

    @Override
    public MeterMessage deserialize(ByteBuf message) {
        final MeterMessageBuilder builder = new MeterMessageBuilder()
                .setVersion((short) EncodeConstants.OF13_VERSION_ID)
                .setXid(message.readUnsignedInt())
                .setCommand(MeterModCommand.forValue(message.readUnsignedShort()))
                .setFlags(readMeterFlags(message))
                .setMeterId(new MeterId(message.readUnsignedInt()));

        final List<MeterBandHeader> bands = new ArrayList<>();

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
                                    .setDropRate(message.readUnsignedInt())
                                    .setDropBurstSize(message.readUnsignedInt())
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
                                    .setDscpRemarkRate(message.readUnsignedInt())
                                    .setDscpRemarkBurstSize(message.readUnsignedInt())
                                    .setPrecLevel(message.readUnsignedByte())
                                    .build());
                    message.skipBytes(PADDING_IN_METER_BAND_DSCP_HEADER);
                    break;
                }
                case OFPMBTEXPERIMENTER: {
                    // TODO: Finish meter band experimenter deserialization
                    long expId = message.getUnsignedInt(message.readerIndex() + 2 * EncodeConstants.SIZE_OF_INT_IN_BYTES);
                    message.readerIndex(bandStartIndex);

                    OFDeserializer<Experimenter> deserializer = registry.getDeserializer(
                            new ExperimenterIdDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId, Experimenter.class));

                    bandBuilder
                            .setMeterBandTypes(new MeterBandTypesBuilder()
                                    .setFlags(new MeterBandType(false, false, true))
                                    .build())
                            .setBandType(deserializer.deserialize(message));
                    break;
                }
            }

            bands.add(bandBuilder.build());
        }

        return builder
                .setMeterBandHeaders(new MeterBandHeadersBuilder()
                        .setMeterBandHeader(bands)
                        .build())
                .build();
    }


    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

    private static MeterFlags readMeterFlags(ByteBuf message) {
        int input = message.readUnsignedShort();
        final Boolean mfKBPS = (input & (1)) != 0;
        final Boolean mfPKTPS = (input & (1 << 1)) != 0;
        final Boolean mfBURST = (input & (1 << 2)) != 0;
        final Boolean mfSTATS = (input & (1 << 3)) != 0;
        return new MeterFlags(mfBURST, mfKBPS, mfPKTPS, mfSTATS);
    }

}
