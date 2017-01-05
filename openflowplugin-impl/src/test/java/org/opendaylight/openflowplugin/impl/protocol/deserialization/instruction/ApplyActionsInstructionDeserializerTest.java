/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ApplyActionsInstructionDeserializerTest extends AbstractInstructionDeserializerTest {

    @Test
    public void testDeserialize() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();

        // Header
        final int startIndex = in.writerIndex();
        in.writeShort(getType());
        final int index = in.writerIndex();
        in.writeShort(getLength());
        in.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        // POP PBB action
        in.writeShort(ActionConstants.POP_PBB_CODE);
        in.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
        in.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);

        in.setShort(index, in.writerIndex() - startIndex);

        final Instruction instruction = deserializeInstruction(in);
        assertEquals(ApplyActionsCase.class, instruction.getImplementedInterface());
        final ApplyActionsCase actionCase = ApplyActionsCase.class.cast(instruction);
        assertEquals(1, actionCase.getApplyActions().getAction().size());
        assertEquals(PopPbbActionCase.class, actionCase.getApplyActions().getAction().get(0)
                .getAction().getImplementedInterface());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return InstructionConstants.APPLY_ACTIONS_TYPE;
    }

    @Override
    protected short getLength() {
        return EncodeConstants.EMPTY_LENGTH;
    }

}
