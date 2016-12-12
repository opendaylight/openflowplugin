/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.AbstractDeserializerTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.Drop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterModCommand;

public class MeterMessageDeserializerTest extends AbstractDeserializerTest {

    private static final byte PADDING_IN_METER_BAND_DROP_HEADER = 4;
    private static final byte PADDING_IN_METER_BAND_DSCP_HEADER = 3;
    private static final int TYPE = 29;
    private static final int XID = 42;
    private static final MeterModCommand COMMAND = MeterModCommand.OFPMCADD;
    private static final boolean IS_KBPS = true;
    private static final boolean IS_PKTPS = true;
    private static final boolean IS_BURST = false;
    private static final boolean IS_STATS = false;
    private static final int ID = 10;
    private static final int DROP_RATE = 15;
    private static final int DROP_BURST = 16;
    private static final int DSCP_RATE = 17;
    private static final int DSCP_BURST = 18;
    private static final int DSCP_PREC = 19;

    private ByteBuf buffer;

    @Override
    protected void init() {
        buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserialize() throws Exception {
        buffer.writeByte(TYPE); // Message type
        buffer.writeShort(EncodeConstants.EMPTY_LENGTH);
        buffer.writeInt(XID);
        buffer.writeShort(COMMAND.getIntValue());
        buffer.writeShort(ByteBufUtils.fillBitMask(0,
                IS_KBPS,
                IS_PKTPS,
                IS_BURST,
                IS_STATS));
        buffer.writeInt(ID);

        // Drop band
        buffer.writeShort(1);
        buffer.writeInt(DROP_RATE);
        buffer.writeInt(DROP_BURST);
        buffer.writeZero(PADDING_IN_METER_BAND_DROP_HEADER);

        // Dscp remark band
        buffer.writeShort(2);
        buffer.writeInt(DSCP_RATE);
        buffer.writeInt(DSCP_BURST);
        buffer.writeByte(DSCP_PREC);
        buffer.writeZero(PADDING_IN_METER_BAND_DSCP_HEADER);

        final MeterMessage message = (MeterMessage)getFactory().deserialize(buffer, EncodeConstants.OF13_VERSION_ID);

        assertEquals(message.getXid().intValue(), XID);
        assertEquals(message.getCommand().getIntValue(), COMMAND.getIntValue());
        assertEquals(message.getFlags().isMeterBurst(), IS_BURST);
        assertEquals(message.getFlags().isMeterKbps(), IS_KBPS);
        assertEquals(message.getFlags().isMeterPktps(), IS_PKTPS);
        assertEquals(message.getFlags().isMeterStats(), IS_STATS);
        assertEquals(message.getMeterId().getValue().intValue(), ID);

        final List<MeterBandHeader> meterBandHeader = message.getMeterBandHeaders().getMeterBandHeader();
        assertEquals(meterBandHeader.size(), 2);

        // Drop band
        final MeterBandHeader dropHeader = meterBandHeader.get(0);
        assertEquals(Drop.class, dropHeader.getBandType().getImplementedInterface());
        assertTrue(dropHeader.getMeterBandTypes().getFlags().isOfpmbtDrop());

        final Drop drop = Drop.class.cast(dropHeader.getBandType());
        assertEquals(DROP_RATE, drop.getDropRate().intValue());
        assertEquals(DROP_BURST, drop.getDropBurstSize().intValue());

        // Dscp band
        final MeterBandHeader dscpHeader = meterBandHeader.get(1);
        assertEquals(DscpRemark.class, dscpHeader.getBandType().getImplementedInterface());
        assertTrue(dscpHeader.getMeterBandTypes().getFlags().isOfpmbtDscpRemark());

        final DscpRemark dscpRemark = DscpRemark.class.cast(dscpHeader.getBandType());
        assertEquals(DSCP_RATE, dscpRemark.getDscpRemarkRate().intValue());
        assertEquals(DSCP_BURST, dscpRemark.getDscpRemarkBurstSize().intValue());

        assertEquals(buffer.readableBytes(), 0);
    }

}
