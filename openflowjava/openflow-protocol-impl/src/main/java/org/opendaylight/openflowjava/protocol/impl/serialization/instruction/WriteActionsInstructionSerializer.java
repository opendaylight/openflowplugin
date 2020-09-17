/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.serialization.instruction;

import io.netty.buffer.ByteBuf;
import java.util.List;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * WriteActions instruction serializer.
 *
 * @author michal.polkorab
 */
public class WriteActionsInstructionSerializer extends AbstractActionInstructionSerializer {
    public WriteActionsInstructionSerializer() {
        super(InstructionConstants.WRITE_ACTIONS_TYPE);
    }

    @Override
    public void serialize(final Instruction instruction, final ByteBuf outBuffer) {
        final int startIndex = outBuffer.writerIndex();
        outBuffer.writeShort(InstructionConstants.WRITE_ACTIONS_TYPE);
        WriteActionsCase actionsCase = (WriteActionsCase) instruction.getInstructionChoice();
        if (actionsCase != null) {
            List<Action> actions = actionsCase.getWriteActions().getAction();
            writeActions(actions, outBuffer, startIndex);
        } else {
            outBuffer.writeShort(InstructionConstants.STANDARD_INSTRUCTION_LENGTH);
            outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
        }
    }
}
