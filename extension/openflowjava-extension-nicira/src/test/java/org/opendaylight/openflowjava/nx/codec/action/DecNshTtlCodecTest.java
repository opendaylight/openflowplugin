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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionDecNshTtlBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.dec.nsh.ttl.grouping.NxActionDecNshTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.dec.nsh.ttl.grouping.NxActionDecNshTtlBuilder;

public class DecNshTtlCodecTest {
    private static final int LENGTH = 16;
    private static final byte NXAST_DEC_NSH_TTL_SUBTYPE = 48;
    private static final int PADDING = 6;

    private DecNshTtlCodec decNshTtlCodec;
    private ByteBuf buffer;
    private Action action;

    @Before
    public void setUp() {
        decNshTtlCodec = new DecNshTtlCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);
        action = decNshTtlCodec.deserialize(buffer);
        assertEquals(action.getExperimenterId().getValue(), NiciraConstants.NX_VENDOR_ID);
        ActionDecNshTtl result = (ActionDecNshTtl) action.getActionChoice();
        NxActionDecNshTtl nxActionDecNshTtl = result.getNxActionDecNshTtl();
        assertNotNull(nxActionDecNshTtl);
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serialize() {
        action = createAction();
        decNshTtlCodec.serialize(action, buffer);
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_DEC_NSH_TTL_SUBTYPE, buffer.readUnsignedShort());
        buffer.skipBytes(PADDING);
        assertFalse(buffer.isReadable());
    }


    private static Action createAction() {
        NxActionDecNshTtlBuilder nxActionDecNshTtlBuilder = new NxActionDecNshTtlBuilder();
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionDecNshTtlBuilder actionDecNshTtlBuilder = new ActionDecNshTtlBuilder();
        actionDecNshTtlBuilder.setNxActionDecNshTtl(nxActionDecNshTtlBuilder.build());
        actionBuilder.setActionChoice(actionDecNshTtlBuilder.build());
        return actionBuilder.build();
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_DEC_NSH_TTL_SUBTYPE);
        message.writeZero(PADDING);
    }
}