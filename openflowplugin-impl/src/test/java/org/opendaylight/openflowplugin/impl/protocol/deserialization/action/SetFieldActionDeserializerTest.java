/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.BinContent;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class SetFieldActionDeserializerTest extends AbstractActionDeserializerTest {

    @Test
    public void testDeserialize() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final int portNum = 0xfffffffa;
        writeHeader(in);
        in.writeShort(OxmMatchConstants.OPENFLOW_BASIC_CLASS);
        in.writeByte(OxmMatchConstants.IN_PORT << 1);
        in.writeByte(EncodeConstants.SIZE_OF_INT_IN_BYTES);
        in.writeInt(portNum);
        in.writeZero(EncodeConstants.SIZE_OF_INT_IN_BYTES);

        final Action action = deserializeAction(in);
        assertTrue(SetFieldCase.class.isInstance(action));
        assertEquals(
                OpenflowPortsUtil.getPortLogicalName(EncodeConstants.OF13_VERSION_ID, BinContent.intToUnsignedLong(portNum)),
                SetFieldCase.class.cast(action).getSetField().getInPort().getValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return ActionConstants.SET_FIELD_CODE;
    }

    @Override
    protected short getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

}
