/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.factories;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowjava.util.ExperimenterSerializerKeyFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ExperimenterIdMeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterBandCommons;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDscpRemarkCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.dscp.remark._case.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.experimenter._case.MeterBandExperimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.mod.Bands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates MeterMod messages.
 *
 * @author timotej.kubas
 * @author michal.polkorab
 */
public class MeterModInputMessageFactory implements OFSerializer<MeterModInput>,
        SerializerRegistryInjector {

    private static final Logger LOG = LoggerFactory
            .getLogger(MeterModInputMessageFactory.class);
    private static final byte MESSAGE_TYPE = 29;
    private static final short LENGTH_OF_METER_BANDS = 16;
    private static final short PADDING_IN_METER_BAND_DROP = 4;
    private static final short PADDING_IN_METER_BAND_DSCP_REMARK = 3;
    private SerializerRegistry registry;

    @Override
    public void serialize(final MeterModInput message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);
        outBuffer.writeShort(message.getCommand().getIntValue());
        outBuffer.writeShort(createMeterFlagsBitmask(message.getFlags()));
        outBuffer.writeInt(message.getMeterId().getValue().intValue());
        serializeBands(message.getBands(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    private static int createMeterFlagsBitmask(final MeterFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.getOFPMFKBPS(),
                flags.getOFPMFPKTPS(),
                flags.getOFPMFBURST(),
                flags.getOFPMFSTATS());
    }

    private void serializeBands(final List<Bands> bands, final ByteBuf outBuffer) {
        if (bands != null) {
            for (Bands currentBand : bands) {
                MeterBand meterBand = currentBand.getMeterBand();
                if (meterBand instanceof MeterBandDropCase) {
                    MeterBandDropCase dropBandCase = (MeterBandDropCase) meterBand;
                    MeterBandDrop dropBand = dropBandCase.getMeterBandDrop();
                    writeBandCommonFields(dropBand, outBuffer);
                    outBuffer.writeZero(PADDING_IN_METER_BAND_DROP);
                } else if (meterBand instanceof MeterBandDscpRemarkCase) {
                    MeterBandDscpRemarkCase dscpRemarkBandCase = (MeterBandDscpRemarkCase) meterBand;
                    MeterBandDscpRemark dscpRemarkBand = dscpRemarkBandCase.getMeterBandDscpRemark();
                    writeBandCommonFields(dscpRemarkBand, outBuffer);
                    outBuffer.writeByte(dscpRemarkBand.getPrecLevel().toJava());
                    outBuffer.writeZero(PADDING_IN_METER_BAND_DSCP_REMARK);
                } else if (meterBand instanceof MeterBandExperimenterCase) {
                    MeterBandExperimenterCase experimenterBandCase = (MeterBandExperimenterCase) meterBand;
                    MeterBandExperimenter experimenterBand = experimenterBandCase.getMeterBandExperimenter();
                    ExperimenterIdMeterBand expIdMeterBand =
                            experimenterBand.augmentation(ExperimenterIdMeterBand.class);
                    if (expIdMeterBand != null) {
                        long expId = expIdMeterBand.getExperimenter().getValue().toJava();
                        Class<? extends ExperimenterMeterBandSubType> meterBandSubType = expIdMeterBand.getSubType();
                        try {
                            OFSerializer<MeterBandExperimenterCase> serializer = registry.getSerializer(
                                    ExperimenterSerializerKeyFactory.createMeterBandSerializerKey(
                                            EncodeConstants.OF13_VERSION_ID, expId, meterBandSubType));
                            serializer.serialize(experimenterBandCase, outBuffer);
                        } catch (final IllegalStateException e) {
                            LOG.warn("Serializer for key: {} wasn't found",
                                    ExperimenterSerializerKeyFactory.createMeterBandSerializerKey(
                                    EncodeConstants.OF13_VERSION_ID, expId, meterBandSubType));
                        }
                    }
                }
            }
        }
    }

    private static void writeBandCommonFields(final MeterBandCommons meterBand, final ByteBuf outBuffer) {
        outBuffer.writeShort(meterBand.getType().getIntValue());
        outBuffer.writeShort(LENGTH_OF_METER_BANDS);
        outBuffer.writeInt(meterBand.getRate().intValue());
        outBuffer.writeInt(meterBand.getBurstSize().intValue());
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
