/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeaders;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates MeterMod messages
 * OF protocol versions: 1.3.
 */
public class MeterMessageSerializer extends AbstractMessageSerializer<MeterMessage> implements
        SerializerRegistryInjector {
    private static final Logger LOG = LoggerFactory.getLogger(MeterMessageSerializer.class);
    private static final short LENGTH_OF_METER_BANDS = 16;
    private static final short PADDING_IN_METER_BAND_DROP = 4;
    private static final short PADDING_IN_METER_BAND_DSCP_REMARK = 3;

    private SerializerRegistry registry;

    @Override
    public void serialize(final MeterMessage message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        super.serialize(message, outBuffer);
        outBuffer.writeShort(message.getCommand().getIntValue());
        outBuffer.writeShort(createMeterFlagsBitMask(
                MoreObjects.firstNonNull(message.getFlags(), new MeterFlags(false, false, true, false))));
        outBuffer.writeInt(message.getMeterId().getValue().intValue());
        serializeBands(message.getMeterBandHeaders(), outBuffer);
        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    @Override
    protected byte getMessageType() {
        return 29;
    }

    private void serializeBands(final MeterBandHeaders meterBandHeaders, final ByteBuf outBuffer) {
        if (meterBandHeaders != null && meterBandHeaders.getMeterBandHeader() != null) {
            meterBandHeaders.nonnullMeterBandHeader().values().forEach(meterBandHeader ->
                    // FIXME: get rid of this atrocity and just use a null check
                    Optional.ofNullable(meterBandHeader.getMeterBandTypes())
                            .flatMap(m -> Optional.ofNullable(m.getFlags()))
                            .ifPresent(flags -> Optional.ofNullable(meterBandHeader.getBandType()).ifPresent(type -> {
                                if (flags.getOfpmbtDrop()) {
                                    final Drop band = (Drop) type;
                                    outBuffer.writeShort(MeterBandType.OFPMBTDROP.getIntValue());

                                    outBuffer.writeShort(LENGTH_OF_METER_BANDS);
                                    outBuffer.writeInt(band.getDropRate().intValue());
                                    outBuffer.writeInt(band.getDropBurstSize().intValue());
                                    outBuffer.writeZero(PADDING_IN_METER_BAND_DROP);
                                } else if (flags.getOfpmbtDscpRemark()) {
                                    final DscpRemark band = (DscpRemark) type;
                                    outBuffer.writeShort(MeterBandType.OFPMBTDSCPREMARK.getIntValue());

                                    outBuffer.writeShort(LENGTH_OF_METER_BANDS);
                                    outBuffer.writeInt(band.getDscpRemarkRate().intValue());
                                    outBuffer.writeInt(band.getDscpRemarkBurstSize().intValue());
                                    outBuffer.writeByte(band.getPrecLevel().toJava());
                                    outBuffer.writeZero(PADDING_IN_METER_BAND_DSCP_REMARK);
                                } else if (flags.getOfpmbtExperimenter()) {
                                    final Experimenter band = (Experimenter) type;

                                    // TODO: finish experimenter serialization
                                    final ExperimenterIdSerializerKey<Experimenter> key =
                                            new ExperimenterIdSerializerKey<>(
                                                    EncodeConstants.OF13_VERSION_ID,
                                                    band.getExperimenter().toJava(),
                                                    (Class<Experimenter>) type.implementedInterface());

                                    try {
                                        final OFSerializer<Experimenter> serializer = registry.getSerializer(key);
                                        serializer.serialize(band, outBuffer);
                                    } catch (final IllegalStateException e) {
                                        LOG.warn("Serializer for key: {} wasn't found", key, e);
                                    }
                                }
                            })));
        }
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

    private static int createMeterFlagsBitMask(final MeterFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.getMeterKbps(),
                flags.getMeterPktps(),
                flags.getMeterBurst(),
                flags.getMeterStats());
    }
}
