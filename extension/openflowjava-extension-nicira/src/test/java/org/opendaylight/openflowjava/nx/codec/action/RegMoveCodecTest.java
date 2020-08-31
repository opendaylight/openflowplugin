/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegMoveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.NxActionRegMoveBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;

public class RegMoveCodecTest {
    private static final byte SUBTYPE = 6;

    private static final int SRC = 4;
    private static final int DST = 5;
    private static final long SRC_EXP = 0xFFFF000000000000L | SRC;
    private static final long DST_EXP = 0xFFFF000000000000L | DST;
    private static final Uint64 SRC_EXP_BIGINT = Uint64.valueOf(new BigInteger(1, Longs.toByteArray(SRC_EXP)));

    private static final Uint64 DST_EXP_BIGINT = Uint64.valueOf(new BigInteger(1, Longs.toByteArray(DST_EXP)));
    RegMoveCodec regMoveCodec;
    ByteBuf buffer;
    Action action;

    @Before
    public void setUp() {
        regMoveCodec = new RegMoveCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction(Uint64.valueOf(SRC), Uint64.valueOf(DST));

        regMoveCodec.serialize(action, buffer);

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(24, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedShort());
        assertEquals(3, buffer.readUnsignedShort());
        assertEquals(SRC, buffer.readUnsignedInt());
        assertEquals(DST, buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeWithExperimenterSrcTest() {
        action = createAction(SRC_EXP_BIGINT, Uint64.valueOf(DST));

        regMoveCodec.serialize(action, buffer);

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(32, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedShort());
        assertEquals(3, buffer.readUnsignedShort());
        assertEquals(SRC_EXP, buffer.readLong());
        assertEquals(5, buffer.readInt());
        assertEquals(0, buffer.readInt()); // padding
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serializeWithExperimenterBothTest() {
        action = createAction(SRC_EXP_BIGINT, DST_EXP_BIGINT);

        regMoveCodec.serialize(action, buffer);

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(32, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedShort());
        assertEquals(3, buffer.readUnsignedShort());
        assertEquals(SRC_EXP, buffer.readLong());
        assertEquals(DST_EXP, buffer.readLong());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer, false, false);

        action = regMoveCodec.deserialize(buffer);

        ActionRegMove result = (ActionRegMove) action.getActionChoice();

        assertEquals(1, result.getNxActionRegMove().getNBits().shortValue());
        assertEquals(2, result.getNxActionRegMove().getSrcOfs().shortValue());
        assertEquals(3, result.getNxActionRegMove().getDstOfs().shortValue());
        assertEquals(4, result.getNxActionRegMove().getSrc().intValue());
        assertEquals(5, result.getNxActionRegMove().getDst().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    public void deserializeWithExperimenterDstTest() {
        createBuffer(buffer, false, true);

        action = regMoveCodec.deserialize(buffer);

        ActionRegMove result = (ActionRegMove) action.getActionChoice();

        assertEquals(1, result.getNxActionRegMove().getNBits().shortValue());
        assertEquals(2, result.getNxActionRegMove().getSrcOfs().shortValue());
        assertEquals(3, result.getNxActionRegMove().getDstOfs().shortValue());
        assertEquals(4, result.getNxActionRegMove().getSrc().longValue());
        assertEquals(DST_EXP_BIGINT, result.getNxActionRegMove().getDst());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    public void deserializeWithExperimenterBothTest() {
        createBuffer(buffer, true, true);

        action = regMoveCodec.deserialize(buffer);

        ActionRegMove result = (ActionRegMove) action.getActionChoice();

        assertEquals(1, result.getNxActionRegMove().getNBits().shortValue());
        assertEquals(2, result.getNxActionRegMove().getSrcOfs().shortValue());
        assertEquals(3, result.getNxActionRegMove().getDstOfs().shortValue());
        assertEquals(SRC_EXP_BIGINT, result.getNxActionRegMove().getSrc());
        assertEquals(DST_EXP_BIGINT, result.getNxActionRegMove().getDst());
        assertEquals(0, buffer.readableBytes());
    }

    private static Action createAction(Uint64 src, Uint64 dst) {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        final ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        NxActionRegMoveBuilder nxActionRegMoveBuilder = new NxActionRegMoveBuilder();

        nxActionRegMoveBuilder.setNBits(Uint16.ONE);
        nxActionRegMoveBuilder.setSrcOfs(Uint16.TWO);
        nxActionRegMoveBuilder.setDstOfs(Uint16.valueOf(3));
        nxActionRegMoveBuilder.setSrc(src);
        nxActionRegMoveBuilder.setDst(dst);

        actionRegMoveBuilder.setNxActionRegMove(nxActionRegMoveBuilder.build());
        actionBuilder.setActionChoice(actionRegMoveBuilder.build());

        return actionBuilder.build();
    }

    private static void createBuffer(ByteBuf message, boolean withExpSrc, boolean withExpDst) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        int length = withExpSrc || withExpDst ? 32 : 24;
        message.writeShort(length);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(SUBTYPE);

        message.writeShort(1);
        message.writeShort(2);
        message.writeShort(3);
        if (withExpSrc) {
            message.writeLong(SRC_EXP);
        } else {
            message.writeInt(SRC);
        }
        if (withExpDst) {
            message.writeLong(DST_EXP);
        } else {
            message.writeInt(DST);
        }
        if (message.writerIndex() < length) {
            message.writeInt(0);
        }
    }
}
