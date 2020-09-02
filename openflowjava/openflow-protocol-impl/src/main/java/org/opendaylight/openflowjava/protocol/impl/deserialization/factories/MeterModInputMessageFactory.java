/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ExperimenterDeserializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.BandsBuilder;

/**
 * Translates MeterModInput messages.
 *
 * @author giuseppex.petralia@intel.com
 */
public class MeterModInputMessageFactory implements OFDeserializer<MeterModInput> {
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;

    private final DeserializerRegistry registry;

    public MeterModInputMessageFactory(final DeserializerRegistry registry) {
        this.registry = requireNonNull(registry);
    }

    @Override
    public MeterModInput deserialize(final ByteBuf rawMessage) {
        MeterModInputBuilder builder = new MeterModInputBuilder()
                .setVersion(EncodeConstants.OF_VERSION_1_3)
                .setXid(readUint32(rawMessage))
                .setCommand(MeterModCommand.forValue(rawMessage.readUnsignedShort()))
                .setFlags(createMeterFlags(rawMessage.readUnsignedShort()))
                .setMeterId(new MeterId(readUint32(rawMessage)));
        List<Bands> bandsList = new ArrayList<>();
        while (rawMessage.readableBytes() > 0) {
            BandsBuilder bandsBuilder = new BandsBuilder();
            int bandStartIndex = rawMessage.readerIndex();
            int bandType = rawMessage.readUnsignedShort();
            switch (bandType) {
                case 1:
                    final MeterBandDropCaseBuilder bandDropCaseBuilder = new MeterBandDropCaseBuilder();
                    MeterBandDropBuilder bandDropBuilder = new MeterBandDropBuilder();
                    bandDropBuilder.setType(MeterBandType.forValue(bandType));
                    rawMessage.readUnsignedShort();
                    bandDropBuilder.setRate(readUint32(rawMessage));
                    bandDropBuilder.setBurstSize(readUint32(rawMessage));
                    rawMessage.skipBytes(PADDING_IN_METER_BAND_DROP_HEADER);
                    bandDropCaseBuilder.setMeterBandDrop(bandDropBuilder.build());
                    bandsBuilder.setMeterBand(bandDropCaseBuilder.build());
                    break;
                case 2:
                    final MeterBandDscpRemarkCaseBuilder bandDscpRemarkCaseBuilder =
                        new MeterBandDscpRemarkCaseBuilder();
                    MeterBandDscpRemarkBuilder bandDscpRemarkBuilder = new MeterBandDscpRemarkBuilder();
                    bandDscpRemarkBuilder.setType(MeterBandType.forValue(bandType));
                    rawMessage.readUnsignedShort();
                    bandDscpRemarkBuilder.setRate(readUint32(rawMessage));
                    bandDscpRemarkBuilder.setBurstSize(readUint32(rawMessage));
                    bandDscpRemarkBuilder.setPrecLevel(readUint8(rawMessage));
                    rawMessage.skipBytes(PADDING_IN_METER_BAND_DSCP_HEADER);
                    bandDscpRemarkCaseBuilder.setMeterBandDscpRemark(bandDscpRemarkBuilder.build());
                    bandsBuilder.setMeterBand(bandDscpRemarkCaseBuilder.build());
                    break;
                case 0xFFFF:
                    long expId = rawMessage.getUnsignedInt(rawMessage.readerIndex()
                            + 2 * Integer.BYTES);
                    rawMessage.readerIndex(bandStartIndex);
                    OFDeserializer<MeterBandExperimenterCase> deserializer = registry
                            .getDeserializer(ExperimenterDeserializerKeyFactory
                                    .createMeterBandDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId));
                    bandsBuilder.setMeterBand(deserializer.deserialize(rawMessage));
                    break;
                default:
                    break;
            }
            bandsList.add(bandsBuilder.build());
        }
        builder.setBands(bandsList);
        return builder.build();
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    private static MeterFlags createMeterFlags(final int input) {
        final Boolean mfKBPS = (input & 1 << 0) != 0;
        final Boolean mfPKTPS = (input & 1 << 1) != 0;
        final Boolean mfBURST = (input & 1 << 2) != 0;
        final Boolean mfSTATS = (input & 1 << 3) != 0;
        return new MeterFlags(mfBURST, mfKBPS, mfPKTPS, mfSTATS);
    }
}
