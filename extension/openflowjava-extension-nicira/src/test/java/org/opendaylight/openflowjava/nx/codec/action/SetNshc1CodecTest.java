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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionSetNshc1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.set.nshc._1.grouping.NxActionSetNshc1Builder;

public class SetNshc1CodecTest {

    SetNshc1Codec setNshc1Codec;
    ByteBuf buffer;
    Action action;

    private final int LENGTH = 16;
    private final byte NXAST_SET_NSC_SUBTYPE = 34;
    private final int padding = 2;

    @Before
    public void setUp() {
        setNshc1Codec = new SetNshc1Codec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        setNshc1Codec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_SET_NSC_SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        buffer.skipBytes(padding);
        assertEquals(1, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = setNshc1Codec.deserialize(buffer);

        ActionSetNshc1 result = (ActionSetNshc1) action.getActionChoice();

        assertEquals(1, result.getNxActionSetNshc1().getNshc().intValue());
        assertEquals(0, buffer.readableBytes());
    }

    private Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionSetNshc1Builder actionSetNshc1Builder = new ActionSetNshc1Builder();
        NxActionSetNshc1Builder nxActionSetNshc1Builder = new NxActionSetNshc1Builder();

        nxActionSetNshc1Builder.setNshc((long)1);

        actionSetNshc1Builder.setNxActionSetNshc1(nxActionSetNshc1Builder.build());
        actionBuilder.setActionChoice(actionSetNshc1Builder.build());

        return actionBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_SET_NSC_SUBTYPE);

        message.writeZero(padding);
        message.writeInt(1);
    }


}