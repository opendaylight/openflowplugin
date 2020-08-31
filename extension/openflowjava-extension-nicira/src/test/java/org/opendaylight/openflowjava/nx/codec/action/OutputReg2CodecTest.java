/*
 * Copyright (c) 2018 SUSE LINUX GmbH.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg2Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg2.grouping.NxActionOutputReg2Builder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;

public class OutputReg2CodecTest {

    private static final int LENGTH = 24;
    private static final byte SUBTYPE = 32;

    private static final Uint16 OFS_N_BITS = Uint16.ONE;
    private static final int SRC = 2;
    private static final Uint16 MAX_LEN = Uint16.TWO;
    private static final long SRC_EXP = 0xFFFF000000000000L | SRC;
    private static final Uint64 SRC_EXP_BIGINT = Uint64.valueOf(new BigInteger(1, Longs.toByteArray(SRC_EXP)));

    private OutputReg2Codec outReg2Codec;
    private ByteBuf buffer;

    @Before
    public void setUp() {
        outReg2Codec = new OutputReg2Codec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer, false);

        Action action = outReg2Codec.deserialize(buffer);

        ActionOutputReg2 result = (ActionOutputReg2) action.getActionChoice();

        assertEquals(OFS_N_BITS, result.getNxActionOutputReg2().getNBits());
        assertEquals(MAX_LEN, result.getNxActionOutputReg2().getMaxLen());
        assertEquals(Uint64.valueOf(SRC), result.getNxActionOutputReg2().getSrc());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeWithExperimenterTest() {
        createBuffer(buffer, true);

        Action action = outReg2Codec.deserialize(buffer);

        ActionOutputReg2 result = (ActionOutputReg2) action.getActionChoice();

        assertEquals(OFS_N_BITS, result.getNxActionOutputReg2().getNBits());
        assertEquals(MAX_LEN, result.getNxActionOutputReg2().getMaxLen());
        assertEquals(SRC_EXP_BIGINT, result.getNxActionOutputReg2().getSrc());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeTest() {
        Action action = createAction(Uint64.valueOf(SRC));
        outReg2Codec.serialize(action, buffer);

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());

        //Serialize part
        assertEquals(OFS_N_BITS.shortValue(), buffer.readUnsignedShort());
        assertEquals(MAX_LEN.shortValue(), buffer.readUnsignedShort());
        assertEquals(SRC, buffer.readUnsignedInt());

        // padding
        assertEquals(0, buffer.readUnsignedInt());
        assertEquals(0, buffer.readUnsignedShort());

        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeWithExperimenterTest() {
        Action action = createAction(SRC_EXP_BIGINT);
        outReg2Codec.serialize(action, buffer);

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());

        //Serialize part
        assertEquals(OFS_N_BITS.shortValue(), buffer.readUnsignedShort());
        assertEquals(MAX_LEN.shortValue(), buffer.readUnsignedShort());
        assertEquals(SRC_EXP, buffer.readLong());

        // padding
        assertEquals(0, buffer.readUnsignedShort());

        assertFalse(buffer.isReadable());
    }

    private static Action createAction(final Uint64 src) {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        final ActionOutputReg2Builder actionOutputReg2Builder = new ActionOutputReg2Builder();
        NxActionOutputReg2Builder nxActionOutputBuilder = new NxActionOutputReg2Builder();

        nxActionOutputBuilder.setNBits(OFS_N_BITS);
        nxActionOutputBuilder.setSrc(src);
        nxActionOutputBuilder.setMaxLen(MAX_LEN);

        actionOutputReg2Builder.setNxActionOutputReg2(nxActionOutputBuilder.build());
        actionBuilder.setActionChoice(actionOutputReg2Builder.build());

        return actionBuilder.build();
    }

    private static void createBuffer(final ByteBuf message, final boolean withExpSrc) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(SUBTYPE);
        message.writeShort(OFS_N_BITS.shortValue());
        message.writeShort(MAX_LEN.shortValue());
        if (withExpSrc) {
            message.writeLong(SRC_EXP);
            message.writeZero(2);
        } else {
            message.writeInt(SRC);
            message.writeZero(6);
        }
    }
}