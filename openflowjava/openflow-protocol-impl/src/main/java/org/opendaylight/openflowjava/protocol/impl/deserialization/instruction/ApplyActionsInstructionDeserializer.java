/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.instruction;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * Deserializer for apply actions instruction.
 *
 * @author michal.polkorab
 */
public class ApplyActionsInstructionDeserializer extends AbstractActionInstructionDeserializer
        implements HeaderDeserializer<Instruction> {
    public ApplyActionsInstructionDeserializer(final DeserializerRegistry registry) {
        super(registry);
    }

    @Override
    public Instruction deserialize(final ByteBuf input) {
        final InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(Short.BYTES);
        int instructionLength = input.readUnsignedShort();
        input.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
        ApplyActionsCaseBuilder caseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder actionsBuilder = new ApplyActionsBuilder();
        actionsBuilder.setAction(deserializeActions(input, instructionLength));
        caseBuilder.setApplyActions(actionsBuilder.build());
        builder.setInstructionChoice(caseBuilder.build());
        return builder.build();
    }

    @Override
    public Instruction deserializeHeader(final ByteBuf input) {
        InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(2 * Short.BYTES);
        builder.setInstructionChoice(new ApplyActionsCaseBuilder().build());
        return builder.build();
    }
}
