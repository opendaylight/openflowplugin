/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.multipart;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;

public class MultipartReplyMeterConfigDeserializerTest extends AbstractMultipartDeserializerTest {
    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;
    private static final int OFPMBTDROP = 1;
    private static final int OFPMBTDSCP = 2;
    private static final short ITEM_LENGTH = 16;
    private static final boolean MF_KBPS = true;
    private static final boolean MF_PKTPS = false;
    private static final boolean MF_BURST = true;
    private static final boolean MF_STATS = true;
    private static final MeterFlags FLAGS = new MeterFlags(MF_KBPS, MF_PKTPS, MF_BURST, MF_STATS);
    private static final int METER_ID = 1;
    private static final short SUB_ITEM = 8;
    private static final int DROP_RATE = 2;
    private static final int DROP_BURST_SIZE = 3;
    private static final int DSCP_REMARK_RATE = 3;
    private static final int DSCP_REMARK_BURST_SIZE = 3;
    private static final byte PREC_LEVEL = 3;

    @Test
    public void deserializeDrop() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        writeCommonAtributes(buffer);
        buffer.writeShort(OFPMBTDROP);
        buffer.writeShort(SUB_ITEM);
        buffer.writeInt(DROP_RATE);
        buffer.writeInt(DROP_BURST_SIZE);
        buffer.writeZero(PADDING_IN_METER_BAND_DROP_HEADER);

        final MultipartReplyMeterConfig reply = (MultipartReplyMeterConfig) deserializeMultipart(buffer);

        assertEquals(METER_ID, reply.getMeterConfigStats().get(0).getMeterId().getValue().intValue());
        assertEquals(FLAGS, reply.getMeterConfigStats().get(0).getFlags());
        final Drop drop = (Drop) reply.getMeterConfigStats().get(0)
                .getMeterBandHeaders().getMeterBandHeader().get(0).getBandType();
        assertEquals(DROP_RATE, drop.getDropRate().intValue());
        assertEquals(DROP_BURST_SIZE, drop.getDropBurstSize().intValue());
    }

    @Test
    public void deserializeDscp() throws Exception {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        writeCommonAtributes(buffer);
        buffer.writeShort(OFPMBTDSCP);
        buffer.writeShort(SUB_ITEM);
        buffer.writeInt(DSCP_REMARK_RATE);
        buffer.writeInt(DSCP_REMARK_BURST_SIZE);
        buffer.writeByte(PREC_LEVEL);
        buffer.writeZero(PADDING_IN_METER_BAND_DSCP_HEADER);

        final MultipartReplyMeterConfig reply = (MultipartReplyMeterConfig) deserializeMultipart(buffer);

        final DscpRemark dscpRemark = (DscpRemark) reply.getMeterConfigStats().get(0)
                .getMeterBandHeaders().getMeterBandHeader().get(0).getBandType();
        assertEquals(DSCP_REMARK_RATE, dscpRemark.getDscpRemarkRate().intValue());
        assertEquals(DSCP_REMARK_BURST_SIZE, dscpRemark.getDscpRemarkBurstSize().intValue());
        assertEquals(PREC_LEVEL, dscpRemark.getPrecLevel().byteValue());
    }

    @Override
    protected int getType() {
        return MultipartType.OFPMPMETERCONFIG.getIntValue();
    }

    private void writeCommonAtributes(ByteBuf buffer) {
        buffer.writeShort(ITEM_LENGTH);
        buffer.writeShort(ByteBufUtils.fillBitMask(0,
                FLAGS.isMeterKbps(),
                FLAGS.isMeterPktps(),
                FLAGS.isMeterBurst(),
                FLAGS.isMeterStats()));
        buffer.writeInt(METER_ID);
    }
}