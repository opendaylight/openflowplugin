/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyMeterFeaturesDeserializerTest extends AbstractMultipartDeserializerTest{
    private static final int MAX_METER = 3;
    private static final List BANDS_SUPPORTED = Arrays.asList(MeterBandDrop.class);
    private static final List CAPABILITIES_SUPPORTED = Arrays.asList(MeterKbps.class, MeterBurst.class);
    private static final byte MAX_BANDS = 56;
    private static final byte MAX_COLOR = 48;

    @Test
    public void deserialize() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeInt(MAX_METER);

        int bitMaskBands = ByteBufUtils.fillBitMask(0,
                BANDS_SUPPORTED.contains(MeterBandDrop.class),
                BANDS_SUPPORTED.contains(MeterBandDscpRemark.class));
        buffer.writeInt(bitMaskBands);

        int bitMaskCapabilities = ByteBufUtils.fillBitMask(0,
                CAPABILITIES_SUPPORTED.contains(MeterKbps.class),
                CAPABILITIES_SUPPORTED.contains(MeterPktps.class),
                CAPABILITIES_SUPPORTED.contains(MeterBurst.class),
                CAPABILITIES_SUPPORTED.contains(MeterStats.class));
        buffer.writeInt(bitMaskCapabilities);

        buffer.writeByte(MAX_BANDS);
        buffer.writeByte(MAX_COLOR);

        final MultipartReplyMeterFeatures reply = (MultipartReplyMeterFeatures) deserializeMultipart(buffer);

        assertEquals(MAX_METER, reply.getMaxMeter().getValue().intValue());
        assertTrue(reply.getMeterBandSupported().containsAll(BANDS_SUPPORTED));
        assertTrue(reply.getMeterCapabilitiesSupported().containsAll(CAPABILITIES_SUPPORTED));
        assertEquals(MAX_BANDS, reply.getMaxBands().byteValue());
        assertEquals(MAX_COLOR, reply.getMaxColor().byteValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPMETERFEATURES.getIntValue();
    }
}