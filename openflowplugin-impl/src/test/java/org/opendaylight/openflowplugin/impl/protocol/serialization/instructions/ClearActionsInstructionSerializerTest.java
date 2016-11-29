/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.clear.actions._case.ClearActionsBuilder;

public class ClearActionsInstructionSerializerTest extends AbstractInstructionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final Instruction instruction = new ClearActionsCaseBuilder()
                .setClearActions(new ClearActionsBuilder()
                        .build())
                .build();

        assertInstruction(instruction, out -> out.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION));
    }

    @Override
    protected Class<? extends Instruction> getClazz() {
        return ClearActionsCase.class;
    }

    @Override
    protected int getType() {
        return InstructionConstants.CLEAR_ACTIONS_TYPE;
    }

    @Override
    protected int getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }

}
