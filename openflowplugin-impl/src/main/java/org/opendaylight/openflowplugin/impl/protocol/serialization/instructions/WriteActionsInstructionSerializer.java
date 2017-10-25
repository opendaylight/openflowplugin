/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;

public class WriteActionsInstructionSerializer extends AbstractActionInstructionSerializer {

    @Override
    public void serialize(Instruction input, ByteBuf outBuffer) {
        int index = outBuffer.writerIndex();
        super.serialize(input, outBuffer);
        writeActions(WriteActionsCase.class.cast(input).getWriteActions(),
                EncodeConstants.OF13_VERSION_ID, outBuffer,index);
    }

    @Override
    protected int getType() {
        return InstructionConstants.WRITE_ACTIONS_TYPE;
    }

    @Override
    protected int getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }

}
