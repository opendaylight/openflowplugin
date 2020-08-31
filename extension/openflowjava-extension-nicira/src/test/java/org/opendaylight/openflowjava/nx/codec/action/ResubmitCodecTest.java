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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionResubmitBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.resubmit.grouping.NxActionResubmitBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

public class ResubmitCodecTest {
    private static final int LENGTH = 16;
    private static final byte NXAST_RESUBMIT_SUBTYPE = 1;
    private static final byte NXAST_RESUBMIT_TABLE_SUBTYPE = 14;

    private static final short OFP_TABLE_ALL = 255;
    private static final int OFP_IN_PORT =  0xfff8;
    private static  final int PADDING = 3;

    ResubmitCodec resubmitCodec;
    ByteBuf buffer;
    Action action;

    @Before
    public void setUp() {
        resubmitCodec = new ResubmitCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }


    /**
     * If table == null or table == OFP_TABLE_ALL
     * SUBTYPE should be set to NXAST_RESUBMIT_SUBTYPE.
     */
    @Test
    public void getSubTypeTest1() {
        action = createAction(null, null);
        ActionResubmit actionResubmit = (ActionResubmit) action.getActionChoice();

        byte result = resubmitCodec.getSubType(actionResubmit);
        assertEquals(NXAST_RESUBMIT_SUBTYPE, result);
    }

    /**
     * If table != null or table != OFP_TABLE_ALL
     * SUBTYPE should be set to NXAST_RESUBMIT_TABLE_SUBTYPE.
     */
    @Test
    public void getSubTypeTest2() {
        action = createAction(null, Uint8.ONE);
        ActionResubmit actionResubmit = (ActionResubmit) action.getActionChoice();

        byte result = resubmitCodec.getSubType(actionResubmit);
        assertEquals(NXAST_RESUBMIT_TABLE_SUBTYPE, result);
    }


    /**
     * If table and inPort are NOT NULL they should be used instead of
     * hardcoded values OFP_TABLE_ALL and OFP_IN_PORT.
     */
    @Test
    public void serializeTest1() {
        action = createAction(Uint16.ONE, Uint8.TWO);
        resubmitCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_RESUBMIT_TABLE_SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedByte());
    }

    /**
     * If table and inPort are NULL they hardcoded values OFP_TABLE_ALL and OFP_IN_PORT should be used.
     */
    @Test
    public void serializeTest2() {
        action = createAction(null, null);
        resubmitCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        //SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_RESUBMIT_SUBTYPE, buffer.readUnsignedShort());
        //Serialize part
        assertEquals(OFP_IN_PORT, buffer.readUnsignedShort());
        assertEquals(OFP_TABLE_ALL, buffer.readUnsignedByte());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = resubmitCodec.deserialize(buffer);

        ActionResubmit result = (ActionResubmit) action.getActionChoice();

        assertEquals(1, result.getNxActionResubmit().getInPort().intValue());
        assertEquals(2, result.getNxActionResubmit().getTable().byteValue());
        assertEquals(0, buffer.readableBytes());
    }

    private static Action createAction(Uint16 inPort, Uint8 table) {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionResubmitBuilder actionResubmitBuilder = new ActionResubmitBuilder();
        NxActionResubmitBuilder nxActionResubmitBuilder = new NxActionResubmitBuilder();

        if (inPort != null) {
            nxActionResubmitBuilder.setInPort(inPort);
        }
        if (table != null) {
            nxActionResubmitBuilder.setTable(table);
        }

        actionResubmitBuilder.setNxActionResubmit(nxActionResubmitBuilder.build());
        actionBuilder.setActionChoice(actionResubmitBuilder.build());

        return actionBuilder.build();
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_RESUBMIT_TABLE_SUBTYPE);

        message.writeShort(1);
        message.writeByte(2);
        message.writeZero(PADDING);
    }
}
