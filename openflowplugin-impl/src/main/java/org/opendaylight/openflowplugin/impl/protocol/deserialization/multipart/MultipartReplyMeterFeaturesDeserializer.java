/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import java.util.Set;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;

public class MultipartReplyMeterFeaturesDeserializer implements OFDeserializer<MultipartReplyBody> {

    @Override
    public MultipartReplyBody deserialize(final ByteBuf message) {
        return new MultipartReplyMeterFeaturesBuilder()
                .setMaxMeter(new Counter32(readUint32(message)))
                .setMeterBandSupported(readMeterBands(message))
                .setMeterCapabilitiesSupported(readMeterCapabilities(message))
                .setMaxBands(readUint8(message))
                .setMaxColor(readUint8(message))
                .build();
    }

    private static Set<MeterBand> readMeterBands(final ByteBuf message) {
        final long typesMask = message.readUnsignedInt();

        final var bandTypes = ImmutableSet.<MeterBand>builder();
        if ((typesMask & 1) != 0) {
            bandTypes.add(MeterBandDrop.VALUE);
        }
        if ((typesMask & 1 << 1) != 0) {
            bandTypes.add(MeterBandDscpRemark.VALUE);
        }
        return bandTypes.build();
    }

    private static Set<MeterCapability> readMeterCapabilities(final ByteBuf message) {
        final long capabilitiesMask = message.readUnsignedInt();

        final var meterCapabilities = ImmutableSet.<MeterCapability>builder();
        if ((capabilitiesMask & 1) != 0) {
            meterCapabilities.add(MeterKbps.VALUE);
        }
        if ((capabilitiesMask & 1 << 1) != 0) {
            meterCapabilities.add(MeterPktps.VALUE);
        }
        if ((capabilitiesMask & 1 << 2) != 0) {
            meterCapabilities.add(MeterBurst.VALUE);
        }
        if ((capabilitiesMask & 1 << 3) != 0) {
            meterCapabilities.add(MeterStats.VALUE);
        }

        return meterCapabilities.build();
    }
}
