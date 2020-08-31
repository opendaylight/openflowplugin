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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxHashFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjNxMpAlgorithm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionMultipathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.multipath.grouping.NxActionMultipathBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MultipathCodecTest {
    private static final int LENGTH = 32;
    private static final byte NXAST_MULTIPATH_SUBTYPE = 10;

    MultipathCodec multipathCodec;
    ByteBuf buffer;
    Action action;

    @Before
    public void setUp() {
        multipathCodec = new MultipathCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }


    @Test
    public void serializeTest() {
        action = createAction();
        multipathCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());
        //SerializeHeaders part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_MULTIPATH_SUBTYPE, buffer.readUnsignedShort());

        //Serialize part
        assertEquals(OfjNxHashFields.NXHASHFIELDSETHSRC.ordinal(), buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedShort());
        buffer.skipBytes(2);
        assertEquals(OfjNxMpAlgorithm.NXMPALGMODULON.ordinal(), buffer.readUnsignedShort());
        assertEquals(4, buffer.readUnsignedShort());
        assertEquals(5, buffer.readUnsignedInt());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = multipathCodec.deserialize(buffer);

        ActionMultipath result = (ActionMultipath) action.getActionChoice();

        assertEquals(OfjNxHashFields.NXHASHFIELDSETHSRC, result.getNxActionMultipath().getFields());
        assertEquals(1, result.getNxActionMultipath().getBasis().intValue());
        assertEquals(OfjNxMpAlgorithm.NXMPALGMODULON, result.getNxActionMultipath().getAlgorithm());
        assertEquals(2, result.getNxActionMultipath().getMaxLink().shortValue());
        assertEquals(3, result.getNxActionMultipath().getArg().intValue());
        assertEquals(4, result.getNxActionMultipath().getOfsNbits().shortValue());
        assertEquals(5, result.getNxActionMultipath().getDst().intValue());
        assertEquals(0, buffer.readableBytes());
    }


    private static Action createAction() {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        final ActionMultipathBuilder actionMultipathBuilder = new ActionMultipathBuilder();
        NxActionMultipathBuilder nxActionMultipathBuilder = new NxActionMultipathBuilder();

        nxActionMultipathBuilder.setFields(OfjNxHashFields.NXHASHFIELDSETHSRC);
        nxActionMultipathBuilder.setBasis(Uint16.TWO);

        nxActionMultipathBuilder.setAlgorithm(OfjNxMpAlgorithm.NXMPALGMODULON);
        nxActionMultipathBuilder.setMaxLink(Uint16.valueOf(4));
        nxActionMultipathBuilder.setArg(Uint32.valueOf(5));


        nxActionMultipathBuilder.setOfsNbits(Uint16.valueOf(6));
        nxActionMultipathBuilder.setDst(Uint32.valueOf(7));


        actionMultipathBuilder.setNxActionMultipath(nxActionMultipathBuilder.build());
        actionBuilder.setActionChoice(actionMultipathBuilder.build());

        return actionBuilder.build();
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_MULTIPATH_SUBTYPE);

        //FIELDS = OfjNxHashFields.NXHASHFIELDSETHSRC
        message.writeShort(OfjNxHashFields.NXHASHFIELDSETHSRC.getIntValue());
        //BASIS = 1
        message.writeShort(1);
        //place 2 empty bytes
        message.writeZero(2);
        //Algorithm = OfjNxMpAlgorithm.NXMPALGMODULON
        message.writeShort(OfjNxMpAlgorithm.NXMPALGMODULON.getIntValue());
        //MaxLink = 2
        message.writeShort(2);
        //Arg = 3
        message.writeInt(3);
        //place 2 empty bytes
        message.writeZero(2);
        //OfsNbits = 4
        message.writeShort(4);
        //Dst = 5
        message.writeInt(5);
    }
}
