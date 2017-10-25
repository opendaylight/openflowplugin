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
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

/**
 * @author michal.polkorab
 *
 */
public class MeterInstructionDeserializer implements OFDeserializer<Instruction>,
        HeaderDeserializer<Instruction> {

    @Override
    public Instruction deserialize(ByteBuf input) {
        InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        MeterCaseBuilder caseBuilder = new MeterCaseBuilder();
        MeterBuilder instructionBuilder = new MeterBuilder();
        instructionBuilder.setMeterId(input.readUnsignedInt());
        caseBuilder.setMeter(instructionBuilder.build());
        builder.setInstructionChoice(caseBuilder.build());
        return builder.build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf input) {
        InstructionBuilder builder = new InstructionBuilder();
        input.skipBytes(2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        builder.setInstructionChoice(new MeterCaseBuilder().build());
        return builder.build();
    }

}
