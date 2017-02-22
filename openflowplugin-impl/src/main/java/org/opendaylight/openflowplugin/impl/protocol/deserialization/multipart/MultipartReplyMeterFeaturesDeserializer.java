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
    public MultipartReplyBody deserialize(ByteBuf message) {
        return new MultipartReplyMeterFeaturesBuilder()
            .setMaxMeter(new Counter32(message.readUnsignedInt()))
            .setMeterBandSupported(readMeterBands(message))
            .setMeterCapabilitiesSupported(readMeterCapabilities(message))
            .setMaxBands(message.readUnsignedByte())
            .setMaxColor(message.readUnsignedByte())
            .build();
    }

    private static List<Class<? extends MeterBand>> readMeterBands(ByteBuf message) {
        final List<Class<? extends MeterBand>> bandTypes = new ArrayList<>();
        final long typesMask = message.readUnsignedInt();
        final boolean mbtDROP = (typesMask & (1)) != 0;
        final boolean mbtDSCPREMARK = (typesMask & (1 << 1)) != 0;

        if (mbtDROP) bandTypes.add(MeterBandDrop.class);
        if (mbtDSCPREMARK) bandTypes.add(MeterBandDscpRemark.class);

        return bandTypes;
    }

    private static List<Class<? extends MeterCapability>> readMeterCapabilities(ByteBuf message) {
        final List<Class<? extends MeterCapability>> meterCapabilities = new ArrayList<>();
        final long capabilitiesMask = message.readUnsignedInt();

        final boolean mfKBPS = (capabilitiesMask & (1)) != 0;
        final boolean mfPKTPS = (capabilitiesMask & (1 << 1)) != 0;
        final boolean mfBURST = (capabilitiesMask & (1 << 2)) != 0;
        final boolean mfSTATS = (capabilitiesMask & (1 << 3)) != 0;

        if (mfKBPS) meterCapabilities.add(MeterKbps.class);
        if (mfPKTPS) meterCapabilities.add(MeterPktps.class);
        if (mfBURST) meterCapabilities.add(MeterBurst.class);
        if (mfSTATS) meterCapabilities.add(MeterStats.class);

        return meterCapabilities;
    }

}
