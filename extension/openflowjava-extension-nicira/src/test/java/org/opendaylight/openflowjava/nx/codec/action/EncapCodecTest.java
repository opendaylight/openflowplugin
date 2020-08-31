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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionEncapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.encap.grouping.NxActionEncapBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

public class EncapCodecTest {

    private static final int LENGTH = 16;
    private static final int SUBTYPE = 46;
    private static final int HEADER_SIZE_NOT_SPECIFIED = 0;
    private static final Uint32 PACKET_TYPE = Uint32.valueOf(0x1894F);

    EncapCodec encapCodec;
    ByteBuf buffer;
    Action action;

    @Before
    public void setUp() {
        encapCodec = new EncapCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void deserialize() {
        createBuffer(buffer);

        action = encapCodec.deserialize(buffer);

        ActionEncap result = (ActionEncap) action.getActionChoice();

        assertEquals(PACKET_TYPE, result.getNxActionEncap().getPacketType());
        assertFalse(buffer.isReadable());
    }

    @Test
    public void serialize() {
        action = createAction();
        encapCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());
        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(HEADER_SIZE_NOT_SPECIFIED, buffer.readUnsignedShort());
        assertEquals(PACKET_TYPE.longValue(), buffer.readUnsignedInt());
        assertFalse(buffer.isReadable());
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(SUBTYPE);
        message.writeShort(HEADER_SIZE_NOT_SPECIFIED);
        message.writeInt(PACKET_TYPE.intValue());
    }

    private static Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        final ActionEncapBuilder actionEncapBuilder = new ActionEncapBuilder();
        NxActionEncapBuilder nxActionEncapBuilder = new NxActionEncapBuilder();

        nxActionEncapBuilder.setPacketType(PACKET_TYPE);

        actionEncapBuilder.setNxActionEncap(nxActionEncapBuilder.build());
        actionBuilder.setActionChoice(actionEncapBuilder.build());

        return actionBuilder.build();
    }
}