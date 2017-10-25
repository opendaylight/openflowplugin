/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.instruction;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * @author michal.polkorab
 *
 */
public class GoToTableInstructionSerializer extends AbstractInstructionSerializer {

    @Override
    public void serialize(Instruction instruction, ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
        outBuffer.writeShort(InstructionConstants.STANDARD_INSTRUCTION_LENGTH);
        outBuffer.writeByte(((GotoTableCase) instruction.getInstructionChoice())
                .getGotoTable().getTableId());
        outBuffer.writeZero(InstructionConstants.PADDING_IN_GOTO_TABLE);
    }

    @Override
    protected int getType() {
        return InstructionConstants.GOTO_TABLE_TYPE;
    }

}
