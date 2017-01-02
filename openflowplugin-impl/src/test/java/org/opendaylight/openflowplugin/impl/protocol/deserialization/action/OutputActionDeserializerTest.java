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
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputAction;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class OutputActionDeserializerTest extends AbstractActionDeserializerTest {

    @Test
    public void testDeserialize() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        final int portNum = 10;
        final short maxLength = 24;
        writeHeader(in);
        in.writeInt(portNum);
        in.writeShort(maxLength);
        in.writeZero(ActionConstants.OUTPUT_PADDING);

        final Action action = deserializeAction(in);
        assertTrue(OutputActionCase.class.isInstance(action));

        final OutputAction outputAction = OutputActionCase.class.cast(action).getOutputAction();
        assertEquals(portNum, InventoryDataServiceUtil.portNumberfromNodeConnectorId(
                    OpenflowVersion.OF13, outputAction.getOutputNodeConnector().getValue()).intValue());
        assertEquals(maxLength, outputAction.getMaxLength().shortValue());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return ActionConstants.OUTPUT_CODE;
    }

    @Override
    protected short getLength() {
        return ActionConstants.GENERAL_ACTION_LENGTH;
    }

}
