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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * ClearActions instruction serializer.
 *
 * @author michal.polkorab
 */
public class ClearActionsInstructionSerializer extends AbstractInstructionSerializer {
    public ClearActionsInstructionSerializer() {
        super(InstructionConstants.CLEAR_ACTIONS_TYPE);
    }

    @Override
    public void serialize(final Instruction instruction, final ByteBuf outBuffer) {
        outBuffer.writeShort(InstructionConstants.CLEAR_ACTIONS_TYPE);
        outBuffer.writeShort(InstructionConstants.STANDARD_INSTRUCTION_LENGTH);
        outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
    }
}
