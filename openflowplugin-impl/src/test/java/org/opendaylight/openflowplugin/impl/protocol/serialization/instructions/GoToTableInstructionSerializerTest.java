/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

public class GoToTableInstructionSerializerTest extends AbstractInstructionSerializerTest {

    @Test
    public void testSerialize() {
        final Instruction instruction = new GoToTableCaseBuilder()
                .setGoToTable(new GoToTableBuilder().setTableId(Uint8.TWO).build())
                .build();

        assertInstruction(instruction, out -> {
            assertEquals(out.readUnsignedByte(), 2);
            out.skipBytes(InstructionConstants.PADDING_IN_GOTO_TABLE);
        });
    }

    @Override
    protected Class<? extends Instruction> getClazz() {
        return GoToTableCase.class;
    }

    @Override
    protected int getType() {
        return InstructionConstants.GOTO_TABLE_TYPE;
    }

    @Override
    protected int getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }

}