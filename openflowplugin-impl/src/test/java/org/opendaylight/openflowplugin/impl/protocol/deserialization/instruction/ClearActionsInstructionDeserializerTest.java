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
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class ClearActionsInstructionDeserializerTest extends AbstractInstructionDeserializerTest {

    @Test
    public void testDeserialize() throws Exception {
        final ByteBuf in = UnpooledByteBufAllocator.DEFAULT.buffer();
        writeHeader(in);
        in.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        final Instruction instruction = deserializeInstruction(in);
        assertEquals(ClearActionsCase.class, instruction.getImplementedInterface());
        assertEquals(0, in.readableBytes());
    }

    @Override
    protected short getType() {
        return InstructionConstants.CLEAR_ACTIONS_TYPE;
    }

    @Override
    protected short getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }

}
