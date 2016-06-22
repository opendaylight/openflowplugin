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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc3;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc3Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._3.grouping.NxActionSetNshc3Builder;

public class SetNshc3CodecTest {

    SetNshc3Codec setNshc3Codec;
    ByteBuf buffer;
    Action action;

    private final int LENGTH = 16;
    private final byte NXAST_SET_NSC_SUBTYPE = 36;
    private final int padding = 2;

    @Before
    public void setUp() {
        setNshc3Codec= new SetNshc3Codec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        setNshc3Codec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_SET_NSC_SUBTYPE, buffer.readUnsignedShort());
        //Serialize
        buffer.skipBytes(padding);
        assertEquals(3, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = setNshc3Codec.deserialize(buffer);

        ActionSetNshc3 result = (ActionSetNshc3) action.getActionChoice();

        assertEquals(3, result.getNxActionSetNshc3().getNshc().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    private Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionSetNshc3Builder actionSetNshc3Builder = new ActionSetNshc3Builder();
        NxActionSetNshc3Builder nxActionSetNshc3Builder = new NxActionSetNshc3Builder();

        nxActionSetNshc3Builder.setNshc((long)3);

        actionSetNshc3Builder.setNxActionSetNshc3(nxActionSetNshc3Builder.build());
        actionBuilder.setActionChoice(actionSetNshc3Builder.build());

        return actionBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_SET_NSC_SUBTYPE);

        message.writeZero(padding);
        message.writeInt(3);
    }

}