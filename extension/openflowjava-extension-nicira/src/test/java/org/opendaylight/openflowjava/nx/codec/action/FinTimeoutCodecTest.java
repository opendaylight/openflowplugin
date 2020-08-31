/*
 * Copyright © 2016 HPE Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionFinTimeout;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.action.container.action.choice.ActionFinTimeoutBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.fin.timeout.grouping.NxActionFinTimeoutBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;

public class FinTimeoutCodecTest {

    FinTimeoutCodec finTimeoutCodec;
    ByteBuf buffer;
    Action action;

    private static final int LENGTH = 16;
    private static final byte NXAST_FIN_TIMEOUT_SUBTYPE = 19;

    private static final int OFP_NO_TIMEOUT = 0;
    private static final int PADDING = 2;

    @Before
    public void setUp() {
        finTimeoutCodec = new FinTimeoutCodec();
        buffer = ByteBufAllocator.DEFAULT.buffer();
    }

    @Test
    public void serializeTestWithValues() {
        action = createAction(Uint16.ONE, Uint16.TWO);
        finTimeoutCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        // SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_FIN_TIMEOUT_SUBTYPE, buffer.readUnsignedShort());
        // Serialize part
        assertEquals(1, buffer.readUnsignedShort());
        assertEquals(2, buffer.readUnsignedShort());
    }

    @Test
    public void serializeTestNullValues() {
        action = createAction(null, null);
        finTimeoutCodec.serialize(action, buffer);

        assertEquals(LENGTH, buffer.readableBytes());

        // SerializeHeader part
        assertEquals(EncodeConstants.EXPERIMENTER_VALUE, buffer.readUnsignedShort());
        assertEquals(LENGTH, buffer.readUnsignedShort());
        assertEquals(NiciraConstants.NX_VENDOR_ID.intValue(), buffer.readUnsignedInt());
        assertEquals(NXAST_FIN_TIMEOUT_SUBTYPE, buffer.readUnsignedShort());
        // Serialize part
        assertEquals(OFP_NO_TIMEOUT, buffer.readUnsignedShort());
        assertEquals(OFP_NO_TIMEOUT, buffer.readUnsignedByte());
    }

    @Test
    public void deserializeTest() {
        createBuffer(buffer);

        action = finTimeoutCodec.deserialize(buffer);

        ActionFinTimeout result = (ActionFinTimeout) action.getActionChoice();

        assertEquals(Uint16.ONE, result.getNxActionFinTimeout().getFinIdleTimeout());
        assertEquals(Uint16.TWO, result.getNxActionFinTimeout().getFinHardTimeout());
        assertEquals(0, buffer.readableBytes());
    }

    private static Action createAction(Uint16 idleTimeout, Uint16 hardTimeout) {
        ExperimenterId experimenterId = new ExperimenterId(NiciraConstants.NX_VENDOR_ID);
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setExperimenterId(experimenterId);
        ActionFinTimeoutBuilder actionFinTimeoutBuilder = new ActionFinTimeoutBuilder();
        NxActionFinTimeoutBuilder nxActionFinTimeoutBuilder = new NxActionFinTimeoutBuilder();

        if (idleTimeout != null) {
            nxActionFinTimeoutBuilder.setFinIdleTimeout(idleTimeout);
        }
        if (hardTimeout != null) {
            nxActionFinTimeoutBuilder.setFinHardTimeout(hardTimeout);
        }

        actionFinTimeoutBuilder.setNxActionFinTimeout(nxActionFinTimeoutBuilder.build());
        actionBuilder.setActionChoice(actionFinTimeoutBuilder.build());

        return actionBuilder.build();
    }

    private static void createBuffer(ByteBuf message) {
        message.writeShort(EncodeConstants.EXPERIMENTER_VALUE);
        message.writeShort(LENGTH);
        message.writeInt(NiciraConstants.NX_VENDOR_ID.intValue());
        message.writeShort(NXAST_FIN_TIMEOUT_SUBTYPE);

        message.writeShort(1);
        message.writeShort(2);
        message.writeZero(PADDING);
    }
}
