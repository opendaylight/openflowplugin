/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import static org.junit.Assert.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
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

public class RegMoveCodecTest {

    RegMoveCodec regMoveCodec;
    ByteBuf buffer;
    Action action;

    private final int LENGTH = 24;
    private final byte SUBTYPE = 6;

    @Before
    public void setUp() {
        regMoveCodec = new RegMoveCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        regMoveCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedShort());
        assertEquals(3, buffer.readUnsignedShort());
        assertEquals(4, buffer.readUnsignedInt());
        assertEquals(5, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = regMoveCodec.deserialize(buffer);

        ActionRegMove result = (ActionRegMove) action.getActionChoice();

        assertEquals(1, result.getNxActionRegMove().getNBits().shortValue());
        assertEquals(2, result.getNxActionRegMove().getSrcOfs().shortValue());
        assertEquals(3, result.getNxActionRegMove().getDstOfs().shortValue());
        assertEquals(4, result.getNxActionRegMove().getSrc().intValue());
        assertEquals(5, result.getNxActionRegMove().getDst().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    private Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        NxActionRegMoveBuilder nxActionRegMoveBuilder = new NxActionRegMoveBuilder();

        nxActionRegMoveBuilder.setNBits(1);
        nxActionRegMoveBuilder.setSrcOfs(2);
        nxActionRegMoveBuilder.setDstOfs(3);
        nxActionRegMoveBuilder.setSrc((long)4);
        nxActionRegMoveBuilder.setDst((long)5);

        actionRegMoveBuilder.setNxActionRegMove(nxActionRegMoveBuilder.build());
        actionBuilder.setActionChoice(actionRegMoveBuilder.build());

        return actionBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(SUBTYPE);

        message.writeShort(1);
        message.writeShort(2);
        message.writeShort(3);
        message.writeInt(4);
        message.writeInt(5);
    }

}