/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
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
 * @author giuseppex.petralia@intel.com
 *
 */
public class MeterModInputMessageFactory implements OFDeserializer<MeterModInput>, DeserializerRegistryInjector {

    private DeserializerRegistry registry;
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

    @Override
    public MeterModInput deserialize(ByteBuf rawMessage) {
        MeterModInputBuilder builder = new MeterModInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(rawMessage.readUnsignedInt());
        builder.setCommand(MeterModCommand.forValue(rawMessage.readUnsignedShort()));
        builder.setFlags(createMeterFlags(rawMessage.readUnsignedShort()));
        builder.setMeterId(new MeterId(rawMessage.readUnsignedInt()));
        List<Bands> bandsList = new ArrayList<>();
        while (rawMessage.readableBytes() > 0) {
            BandsBuilder bandsBuilder = new BandsBuilder();
            int bandStartIndex = rawMessage.readerIndex();
            int bandType = rawMessage.readUnsignedShort();
            switch (bandType) {
            case 1:
                MeterBandDropCaseBuilder bandDropCaseBuilder = new MeterBandDropCaseBuilder();
                MeterBandDropBuilder bandDropBuilder = new MeterBandDropBuilder();
                bandDropBuilder.setType(MeterBandType.forValue(bandType));
                rawMessage.readUnsignedShort();
                bandDropBuilder.setRate(rawMessage.readUnsignedInt());
                bandDropBuilder.setBurstSize(rawMessage.readUnsignedInt());
                rawMessage.skipBytes(PADDING_IN_METER_BAND_DROP_HEADER);
                bandDropCaseBuilder.setMeterBandDrop(bandDropBuilder.build());
                bandsBuilder.setMeterBand(bandDropCaseBuilder.build());
                break;
            case 2:
                MeterBandDscpRemarkCaseBuilder bandDscpRemarkCaseBuilder = new MeterBandDscpRemarkCaseBuilder();
                MeterBandDscpRemarkBuilder bandDscpRemarkBuilder = new MeterBandDscpRemarkBuilder();
                bandDscpRemarkBuilder.setType(MeterBandType.forValue(bandType));
                rawMessage.readUnsignedShort();
                bandDscpRemarkBuilder.setRate(rawMessage.readUnsignedInt());
                bandDscpRemarkBuilder.setBurstSize(rawMessage.readUnsignedInt());
                bandDscpRemarkBuilder.setPrecLevel(rawMessage.readUnsignedByte());
                rawMessage.skipBytes(PADDING_IN_METER_BAND_DSCP_HEADER);
                bandDscpRemarkCaseBuilder.setMeterBandDscpRemark(bandDscpRemarkBuilder.build());
                bandsBuilder.setMeterBand(bandDscpRemarkCaseBuilder.build());
                break;
            case 0xFFFF:
                long expId = rawMessage
                        .getUnsignedInt(rawMessage.readerIndex() + 2 * EncodeConstants.SIZE_OF_INT_IN_BYTES);
                rawMessage.readerIndex(bandStartIndex);
                OFDeserializer<MeterBandExperimenterCase> deserializer = registry
                        .getDeserializer(ExperimenterDeserializerKeyFactory
                                .createMeterBandDeserializerKey(EncodeConstants.OF13_VERSION_ID, expId));
                bandsBuilder.setMeterBand(deserializer.deserialize(rawMessage));
                break;
            }
            bandsList.add(bandsBuilder.build());
        }
        builder.setBands(bandsList);
        return builder.build();
    }

    private static MeterFlags createMeterFlags(int input) {
        final Boolean mfKBPS = (input & (1 << 0)) != 0;
        final Boolean mfPKTPS = (input & (1 << 1)) != 0;
        final Boolean mfBURST = (input & (1 << 2)) != 0;
        final Boolean mfSTATS = (input & (1 << 3)) != 0;
        return new MeterFlags(mfBURST, mfKBPS, mfPKTPS, mfSTATS);
    }

}
