/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrack;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionConntrackBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.conntrack.grouping.NxActionConntrackBuilder;

public class ConntrackCodecTest {

    private ConntrackCodec conntrackCodec;

    private ByteBuf buffer;
    private Action action;

    private final int LENGTH = 24;
    private final byte NXAST_CONNTRACK_SUBTYPE = 35;

    @Before
    public void setUp() {
        conntrackCodec = new ConntrackCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTest() {
        action = createAction();
        conntrackCodec.serialize(action, buffer);

        Assert.assertEquals(24, buffer.readableBytes());
        Assert.assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        Assert.assertEquals(LENGTH, buffer.readUnsignedShort());
        Assert.assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        Assert.assertEquals(NXAST_CONNTRACK_SUBTYPE, buffer.readUnsignedShort());
        Assert.assertEquals(1, buffer.readUnsignedShort());
        Assert.assertEquals(2, buffer.readUnsignedInt());
        Assert.assertEquals(3, buffer.readUnsignedShort());
        Assert.assertEquals(4, buffer.readByte());
        buffer.skipBytes(5);
    }

    @Test
    public void deserializeTest() {
        createBufer(buffer);
        action = conntrackCodec.deserialize(buffer);

        ActionConntrack result = ((ActionConntrack) action.getActionChoice());

        Assert.assertEquals(1, result.getNxActionConntrack().getFlags().shortValue());
        Assert.assertEquals(2, result.getNxActionConntrack().getZoneSrc().intValue());
        Assert.assertEquals(3, result.getNxActionConntrack().getConntrackZone().shortValue());
        Assert.assertEquals(4, result.getNxActionConntrack().getRecircTable().byteValue());
        Assert.assertEquals(0, buffer.readableBytes());

    }

    private Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionConntrackBuilder actionConntrackBuilder = new ActionConntrackBuilder();

        NxActionConntrackBuilder nxActionConntrackBuilder = new NxActionConntrackBuilder();
        nxActionConntrackBuilder.setFlags(1);
        nxActionConntrackBuilder.setZoneSrc((long) 2);
        nxActionConntrackBuilder.setConntrackZone(3);
        nxActionConntrackBuilder.setRecircTable((short) 4);
        actionConntrackBuilder.setNxActionConntrack(nxActionConntrackBuilder.build());
        actionBuilder.setActionChoice(actionConntrackBuilder.build());

        return actionBuilder.build();
    }

    private void createBufer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_CONNTRACK_SUBTYPE);
        //FLAG = 1
        message.writeShort(1);
        //ZONE_SRC = 2
        message.writeInt(2);
        //CONNTRACK_ZONE = 3
        message.writeShort(3);
        //RECIRC_TABLE = 4
        message.writeByte(4);
        //ADDS 5 empty bytes
        message.writeZero(5);
    }
}