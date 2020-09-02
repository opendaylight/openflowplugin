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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.actions._case.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * Deserializer for write actions instructions.
 *
 * @author michal.polkorab
 */
public class WriteActionsInstructionDeserializer extends AbstractActionInstructionDeserializer
        implements HeaderDeserializer<Instruction> {

    @Override
    public Instruction deserialize(ByteBuf input) {
        final InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(Short.BYTES);
        int instructionLength = input.readUnsignedShort();
        input.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
        WriteActionsCaseBuilder caseBuilder = new WriteActionsCaseBuilder();
        WriteActionsBuilder actionsBuilder = new WriteActionsBuilder();
        actionsBuilder.setAction(deserializeActions(input, instructionLength));
        caseBuilder.setWriteActions(actionsBuilder.build());
        builder.setInstructionChoice(caseBuilder.build());
        return builder.build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf input) {
        InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(2 * Short.BYTES);
        builder.setInstructionChoice(new WriteActionsCaseBuilder().build());
        return builder.build();
    }

}
