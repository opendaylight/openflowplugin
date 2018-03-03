/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionCtClear;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionCtClearBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.ct.clear.grouping.NxActionCtClear;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.ct.clear.grouping.NxActionCtClearBuilder;

public class CtClearCodecTest {
    public static final int LENGTH = 16;
    public static final byte NX_CT_CLEAR_SUBTYPE = 43;
    private static final int PADDING = 6;

    CtClearCodec ctClearCodec;
    ByteBuf buffer;
    Action action;

    @Before
    public void setUp() {
        ctClearCodec = new CtClearCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        ctClearCodec.serialize(action, buffer);
        assertEquals(LENGTH, buffer.readableBytes());
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NX_CT_CLEAR_SUBTYPE, buffer.readUnsignedShort());
        buffer.skipBytes(PADDING);
    }


    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        action = ctClearCodec.deserialize(buffer);
        assertEquals(action.getExperimenterId().getValue(), NiciraConstants.NX_VENDOR_ID);
        ActionCtClear result = (ActionCtClear) action.getActionChoice();
        NxActionCtClear nxActionCtClear = result.getNxActionCtClear();
        assertNotNull(nxActionCtClear);

    }

    private Action createAction() {

        NxActionCtClearBuilder nxActionCtClearBuilder = new NxActionCtClearBuilder();
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionCtClearBuilder actionCtClearBuilder = new ActionCtClearBuilder();
        actionCtClearBuilder.setNxActionCtClear(nxActionCtClearBuilder.build());
        actionBuilder.setActionChoice(actionCtClearBuilder.build());

        return actionBuilder.build();
    }

    private void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NX_CT_CLEAR_SUBTYPE);
        message.writeZero(6);
    }
}


