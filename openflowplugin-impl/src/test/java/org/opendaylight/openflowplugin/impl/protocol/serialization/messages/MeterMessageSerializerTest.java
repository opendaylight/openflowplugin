/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.Arrays;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.serialization.AbstractSerializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemarkBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;

public class MeterMessageSerializerTest extends AbstractSerializerTest {
    // Meter message constants
    private static final short LENGTH = 48;
    private static final short LENGTH_OF_METER_BANDS = 16;
    private static final short PADDING_IN_METER_BAND_DROP = 4;
    private static final short PADDING_IN_METER_BAND_DSCP_REMARK = 3;
    private static final Long XID = 42L;
    private static final String CONTAINER_NAME = "container";
    private static final String METER_NAME = "meter";
    private static final Long METER_ID = 1L;
    private static final Boolean BARRIER = false;
    private static final short VERSION = EncodeConstants.OF13_VERSION_ID;
    private static final MeterModCommand COMMAND = MeterModCommand.OFPMCADD;
    // Flags
    private static final Boolean IS_METER_BURST = false;
    private static final Boolean IS_METER_KBPS = true;
    private static final Boolean IS_METER_PKTPS = false;
    private static final Boolean IS_METER_STATS = true;
    // Bands
    private static final Long BAND_BURST_SIZE = 50L;
    private static final Long BAND_ID = 8L;
    private static final Long BAND_RATE = 25L;
    private static final Long DROP_RATE = 12L;
    private static final Long DROP_BURST_SIZE = 24L;
    private static final Long DSCP_RATE = 13L;
    private static final Long DSCP_BURST_SIZE = 26L;
    private static final short DSCP_PREC_LEVEL = (short) 4;

    // Message
    private static final MeterMessage MESSAGE = new MeterMessageBuilder()
            .setBarrier(BARRIER)
            .setCommand(COMMAND)
            .setContainerName(CONTAINER_NAME)
            .setFlags(new MeterFlags(IS_METER_BURST, IS_METER_KBPS, IS_METER_PKTPS, IS_METER_STATS))
            .setMeterId(new MeterId(METER_ID))
            .setXid(XID)
            .setVersion(VERSION)
            .setMeterName(METER_NAME)
            .setMeterBandHeaders(new MeterBandHeadersBuilder()
                    .setMeterBandHeader(Arrays.asList(
                            new MeterBandHeaderBuilder()
                                    .setMeterBandTypes(new MeterBandTypesBuilder()
                                            .setFlags(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918
                                                    .MeterBandType(true, false, false))
                                            .build())
                                    .setBandBurstSize(BAND_BURST_SIZE)
                                    .setBandId(new BandId(BAND_ID))
                                    .setBandRate(BAND_RATE)
                                    .setBandType(new DropBuilder()
                                            .setDropRate(DROP_RATE)
                                            .setDropBurstSize(DROP_BURST_SIZE)
                                            .build())
                                    .build(),
                            new MeterBandHeaderBuilder()
                                    .setMeterBandTypes(new MeterBandTypesBuilder()
                                            .setFlags(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918
                                                    .MeterBandType(false, true, false))
                                            .build())
                                    .setBandBurstSize(BAND_BURST_SIZE)
                                    .setBandId(new BandId(BAND_ID))
                                    .setBandRate(BAND_RATE)
                                    .setBandType(new DscpRemarkBuilder()
                                            .setDscpRemarkBurstSize(DSCP_BURST_SIZE)
                                            .setDscpRemarkRate(DSCP_RATE)
                                            .setPrecLevel(DSCP_PREC_LEVEL)
                                            .build())
                                    .build()))
                    .build())
            .build();

    private MeterMessageSerializer serializer;


    @Override
    protected void init() {
        serializer = getRegistry().getSerializer(new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, MeterMessage.class)) ;
    }

    @Test
    public void testSerialize() throws Exception {
        final ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        serializer.serialize(MESSAGE, out);

        // Header
        assertEquals(out.readByte(), VERSION);
        assertEquals(out.readByte(), serializer.getMessageType());
        assertEquals(out.readShort(), LENGTH);
        assertEquals(out.readInt(), XID.intValue());

        // Body
        assertEquals(out.readShort(), COMMAND.getIntValue());
        assertEquals(out.readShort(), ByteBufUtils.fillBitMask(0,
                IS_METER_KBPS,
                IS_METER_PKTPS,
                IS_METER_BURST,
                IS_METER_STATS));
        assertEquals(out.readInt(), METER_ID.intValue());

        // Drop band
        assertEquals(out.readShort(), MeterBandType.OFPMBTDROP.getIntValue());
        assertEquals(out.readShort(), LENGTH_OF_METER_BANDS);
        assertEquals(out.readInt(), DROP_RATE.intValue());
        assertEquals(out.readInt(), DROP_BURST_SIZE.intValue());
        out.skipBytes(PADDING_IN_METER_BAND_DROP);

        // Dscp band
        assertEquals(out.readShort(), MeterBandType.OFPMBTDSCPREMARK.getIntValue());
        assertEquals(out.readShort(), LENGTH_OF_METER_BANDS);
        assertEquals(out.readInt(), DSCP_RATE.intValue());
        assertEquals(out.readInt(), DSCP_BURST_SIZE.intValue());
        assertEquals(out.readByte(), DSCP_PREC_LEVEL);
        out.skipBytes(PADDING_IN_METER_BAND_DSCP_REMARK);

        assertEquals(out.readableBytes(), 0);
    }

}