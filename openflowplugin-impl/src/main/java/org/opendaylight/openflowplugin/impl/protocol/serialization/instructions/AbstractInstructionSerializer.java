/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

public abstract class AbstractInstructionSerializer implements OFSerializer<Instruction>, HeaderSerializer<Instruction> {

    @Override
    public void serialize(Instruction input, ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
        outBuffer.writeShort(getLength());
    }

    @Override
    public void serializeHeader(Instruction input, ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
        outBuffer.writeShort(InstructionConstants.INSTRUCTION_IDS_LENGTH);
    }

    /**
     * @return numeric representation of instruction type
     */
    protected abstract int getType();

    /**
     * @return instruction length
     */
    protected abstract int getLength();

}
