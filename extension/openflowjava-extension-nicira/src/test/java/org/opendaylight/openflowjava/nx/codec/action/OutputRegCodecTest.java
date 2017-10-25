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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputReg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionOutputRegBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.output.reg.grouping.NxActionOutputRegBuilder;

public class OutputRegCodecTest {

    OutputRegCodec outRegCodec;
    ByteBuf buffer;
    Action action;

    private final int LENGTH = 24;
    private final byte SUBTYPE = 15;
    private final byte PADDING = 6;


    @Before
    public void setUp() {
        outRegCodec = new OutputRegCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        outRegCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());

        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedInt());
        assertEquals(3, buffer.readUnsignedShort());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = outRegCodec.deserialize(buffer);

        ActionOutputReg result = (ActionOutputReg) action.getActionChoice();

        assertEquals(1, result.getNxActionOutputReg().getNBits().shortValue());
        assertEquals(2, result.getNxActionOutputReg().getSrc().intValue());
        assertEquals(3, result.getNxActionOutputReg().getMaxLen().shortValue());
        assertEquals(0, buffer.readableBytes());
    }


    private Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionOutputRegBuilder actionOutputRegBuilder = new ActionOutputRegBuilder();
        NxActionOutputRegBuilder nxActionOutputBuilder = new NxActionOutputRegBuilder();

        nxActionOutputBuilder.setNBits(1);
        nxActionOutputBuilder.setSrc((long)2);
        nxActionOutputBuilder.setMaxLen(3);

        actionOutputRegBuilder.setNxActionOutputReg(nxActionOutputBuilder.build());
        actionBuilder.setActionChoice(actionOutputRegBuilder.build());

        return actionBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(SUBTYPE);

        message.writeShort(1);
        message.writeInt(2);
        message.writeShort(3);
        message.writeZero(PADDING);

    }

}