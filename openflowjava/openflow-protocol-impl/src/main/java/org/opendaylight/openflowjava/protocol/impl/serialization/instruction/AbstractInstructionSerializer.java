/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization.instruction;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * Base class for an instruction serializer.
 *
 * @author michal.polkorab
 */
public abstract class AbstractInstructionSerializer implements OFSerializer<Instruction>,
        HeaderSerializer<Instruction> {
    private final short type;

    protected AbstractInstructionSerializer(final short type) {
        this.type = type;
    }

    @Override
    public final void serializeHeader(final Instruction input, final ByteBuf outBuffer) {
        outBuffer.writeShort(type);
        outBuffer.writeShort(InstructionConstants.INSTRUCTION_IDS_LENGTH);
    }
}
