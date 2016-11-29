/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;

public class GoToTableInstructionSerializer extends AbstractInstructionSerializer {

    @Override
    public void serialize(Instruction input, ByteBuf outBuffer) {
        super.serialize(input, outBuffer);
        outBuffer.writeByte(GoToTableCase.class.cast(input).getGoToTable().getTableId());
        outBuffer.writeZero(InstructionConstants.PADDING_IN_GOTO_TABLE);
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
