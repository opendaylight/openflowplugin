/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;

@RunWith(MockitoJUnitRunner.class)
public class MeterMessageSerializerTest extends AbstractSerializerTest {
    // Meter message constants
    private static final short LENGTH_OF_METER_BANDS = 16;
    private static final short PADDING_IN_METER_BAND_DROP = 4;
    private static final short PADDING_IN_METER_BAND_DSCP_REMARK = 3;
    private static final Long XID = 42L;
    private static final String CONTAINER_NAME = "container";
    private static final String METER_NAME = "meter";
    private static final Long METER_ID = 1L;
    private static final Boolean BARRIER = false;
    private static final Short VERSION = EncodeConstants.OF13_VERSION_ID;
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
    private static final Short DSCP_PREC_LEVEL = (short) 4;

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
                    .setMeterBandHeader(Stream
                            .of(
                                    new DropBuilder()
                                            .setDropRate(DROP_RATE)
                                            .setDropBurstSize(DROP_BURST_SIZE)
                                            .build(),
                                    new DscpRemarkBuilder()
                                            .setDscpRemarkBurstSize(DSCP_BURST_SIZE)
                                            .setDscpRemarkRate(DSCP_RATE)
                                            .setPrecLevel(DSCP_PREC_LEVEL)
                                            .build())
                            .map(band -> new MeterBandHeaderBuilder()
                                    .setBandBurstSize(BAND_BURST_SIZE)
                                    .setBandId(new BandId(BAND_ID))
                                    .setBandRate(BAND_RATE)
                                    .setBandType(band)
                                    .build())
                            .collect(Collectors.toList())).build())
            .build();

    private MeterMessageSerializer serializer;


    @Override
    protected void init() {
        serializer = new MeterMessageSerializer();
        serializer.injectSerializerRegistry(getRegistry());
        when(getBuffer().readableBytes()).thenReturn(0);
    }

    @Test
    public void testSerialize() throws Exception {
        serializer.serialize(MESSAGE, getBuffer());

        // Header
        bufferVerify().writeByte(VERSION);
        bufferVerify().writeByte(serializer.getMessageType());
        bufferVerify().writeShort(EncodeConstants.EMPTY_LENGTH);
        bufferVerify().writeInt(XID.intValue());

        // Body
        bufferVerify(atLeastOnce()).writeShort(COMMAND.getIntValue());
        bufferVerify().writeShort(ByteBufUtils.fillBitMask(0,
                IS_METER_KBPS,
                IS_METER_PKTPS,
                IS_METER_BURST,
                IS_METER_STATS));
        bufferVerify().writeInt(METER_ID.intValue());

        // Drop band
        bufferVerify(atLeastOnce()).writeShort(MeterBandType.OFPMBTDROP.getIntValue());
        bufferVerify().writeShort(LENGTH_OF_METER_BANDS);
        bufferVerify().writeInt(BAND_RATE.intValue());
        bufferVerify().writeInt(BAND_BURST_SIZE.intValue());
        bufferVerify().writeZero(PADDING_IN_METER_BAND_DROP);

        // Dscp band
        bufferVerify().writeShort(MeterBandType.OFPMBTDSCPREMARK.getIntValue());
        bufferVerify().writeShort(LENGTH_OF_METER_BANDS);
        bufferVerify().writeInt(BAND_RATE.intValue());
        bufferVerify().writeInt(BAND_BURST_SIZE.intValue());
        bufferVerify().writeByte(DSCP_PREC_LEVEL);
        bufferVerify().writeZero(PADDING_IN_METER_BAND_DSCP_REMARK);

        // Header length
        bufferVerify().setShort(eq(2), anyInt());
    }

}