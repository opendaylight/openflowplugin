/*
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionRegLoadBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.load.grouping.NxActionRegLoadBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class RegLoadCodecTest {
    private static final int LENGTH = 24;
    private static final byte SUBTYPE = 7;

    RegLoadCodec regLoadCodec;
    ByteBuf buffer;
    Action action;

    @Before
    public void setUp() {
        regLoadCodec = new RegLoadCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        regLoadCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());
        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedInt());
        assertEquals(3, buffer.readLong());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = regLoadCodec.deserialize(buffer);

        ActionRegLoad result = (ActionRegLoad) action.getActionChoice();

        assertEquals(1, result.getNxActionRegLoad().getOfsNbits().shortValue());
        assertEquals(2, result.getNxActionRegLoad().getDst().longValue());
        assertEquals(3, result.getNxActionRegLoad().getValue().longValue());
        assertEquals(0, buffer.readableBytes());
    }

    private static Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        final ActionRegLoadBuilder actionRegLoadBuilder = new ActionRegLoadBuilder();
        NxActionRegLoadBuilder nxActionRegLoadBuilder = new NxActionRegLoadBuilder();

        nxActionRegLoadBuilder.setOfsNbits(Uint16.ONE);
        nxActionRegLoadBuilder.setDst(Uint32.TWO);
        nxActionRegLoadBuilder.setValue(Uint64.valueOf(3));

        actionRegLoadBuilder.setNxActionRegLoad(nxActionRegLoadBuilder.build());
        actionBuilder.setActionChoice(actionRegLoadBuilder.build());

        return actionBuilder.build();
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(SUBTYPE);

        message.writeShort(1);
        message.writeInt(2);
        message.writeLong(3);
    }
}
