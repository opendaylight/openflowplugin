/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.keys.ExperimenterIdMeterBandTypeSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates MeterMod messages
 * OF protocol versions: 1.3.
 */
public class MeterMessageSerializer extends AbstractMessageSerializer<MeterMessage> implements SerializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MeterMessageSerializer.class);
    private static final short LENGTH_OF_METER_BANDS = 16;
    private static final short PADDING_IN_METER_BAND_DROP = 4;
    private static final short PADDING_IN_METER_BAND_DSCP_REMARK = 3;

    private SerializerRegistry registry;

    @Override
    public void serialize(final MeterMessage message, final ByteBuf outBuffer) {
        super.serialize(message, outBuffer);
        outBuffer.writeShort(message.getCommand().getIntValue());
        outBuffer.writeShort(createMeterFlagsBitMask(message.getFlags()));
        outBuffer.writeInt(message.getMeterId().getValue().intValue());
        serializeBands(message.getMeterBandHeaders(), outBuffer);
        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    @Override
    protected byte getMessageType() {
        return 29;
    }

    private static int createMeterFlagsBitMask(final MeterFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.isMeterKbps(),
                flags.isMeterPktps(),
                flags.isMeterBurst(),
                flags.isMeterStats());
    }

    private void serializeBands(final MeterBandHeaders meterBandHeaders, final ByteBuf outBuffer) {
        if (Objects.nonNull(meterBandHeaders) && Objects.nonNull(meterBandHeaders.getMeterBandHeader())) {
            meterBandHeaders.getMeterBandHeader().forEach(meterBandHeader ->
                    Optional.ofNullable(meterBandHeader.getBandType()).ifPresent(bandType -> {
                        if (Drop.class.equals(bandType.getImplementedInterface())) {
                            outBuffer.writeShort(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.
                                    rev130731.MeterBandType.OFPMBTDROP.getIntValue());
                            writeBandCommonFields(meterBandHeader, outBuffer);
                            outBuffer.writeZero(PADDING_IN_METER_BAND_DROP);
                        } else if (DscpRemark.class.equals(bandType.getImplementedInterface())) {
                            final DscpRemark band = DscpRemark.class.cast(bandType);
                            outBuffer.writeShort(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.
                                    rev130731.MeterBandType.OFPMBTDSCPREMARK.getIntValue());
                            writeBandCommonFields(meterBandHeader, outBuffer);
                            outBuffer.writeByte(band.getPrecLevel());
                            outBuffer.writeZero(PADDING_IN_METER_BAND_DSCP_REMARK);
                        } else if (Experimenter.class.equals(bandType.getImplementedInterface())) {
                            final Experimenter band = Experimenter.class.cast(bandType);
                            final ExperimenterIdMeterBandTypeSerializerKey<Experimenter> key =
                                    new ExperimenterIdMeterBandTypeSerializerKey<>(
                                            EncodeConstants.OF13_VERSION_ID,
                                            band.getExperimenter(),
                                            Experimenter.class,
                                            band.getExperimenterType().getClass());

                            try {
                                final OFSerializer<Experimenter> serializer = registry.getSerializer(key);
                                serializer.serialize(band, outBuffer);
                            } catch (final IllegalStateException e) {
                                LOG.warn("Serializer for key: {} wasn't found, exception {}", key, e);
                            }
                        }
                    }));
        }
    }

    private static void writeBandCommonFields(final MeterBandHeader meterBand, final ByteBuf outBuffer) {
        outBuffer.writeShort(LENGTH_OF_METER_BANDS);
        outBuffer.writeInt(meterBand.getBandRate().intValue());
        outBuffer.writeInt(meterBand.getBandBurstSize().intValue());
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
