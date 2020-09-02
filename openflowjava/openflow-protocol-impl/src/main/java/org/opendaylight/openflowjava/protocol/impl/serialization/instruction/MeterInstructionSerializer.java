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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;

/**
 * Meter instruction serializer.
 *
 * @author michal.polkorab
 */
public class MeterInstructionSerializer extends AbstractInstructionSerializer {
    public MeterInstructionSerializer() {
        super(InstructionConstants.METER_TYPE);
    }

    @Override
    public void serialize(final Instruction instruction, final ByteBuf outBuffer) {
        outBuffer.writeShort(InstructionConstants.METER_TYPE);
        outBuffer.writeShort(InstructionConstants.STANDARD_INSTRUCTION_LENGTH);
        outBuffer.writeInt(((MeterCase) instruction.getInstructionChoice()).getMeter().getMeterId().intValue());
    }
}
