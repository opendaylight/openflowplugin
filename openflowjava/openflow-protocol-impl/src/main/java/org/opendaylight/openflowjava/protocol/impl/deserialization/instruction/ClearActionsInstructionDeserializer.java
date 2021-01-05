/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.instruction;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * Deserializer for clear actions instructions.
 *
 * @author michal.polkorab
 */
public class ClearActionsInstructionDeserializer implements OFDeserializer<Instruction>,
        HeaderDeserializer<Instruction> {
    private static final Instruction CLEAR_INSTRUCTION = new InstructionBuilder()
        .setInstructionChoice(new ClearActionsCaseBuilder().build())
        .build();

    @Override
    public Instruction deserialize(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        input.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
        return CLEAR_INSTRUCTION;
    }

    @Override
    public Instruction deserializeHeader(final ByteBuf input) {
        input.skipBytes(2 * Short.BYTES);
        return CLEAR_INSTRUCTION;
    }
}
