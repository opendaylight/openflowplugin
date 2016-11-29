/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

public class MeterInstructionSerializerTest extends AbstractInstructionSerializerTest {

    @Test
    public void testSerialize() throws Exception {
        final long meter = 2;

        final Instruction instruction = new MeterCaseBuilder()
                .setMeter(new MeterBuilder()
                        .setMeterId(new MeterId(meter))
                        .build())
                .build();

        assertInstruction(instruction, out -> assertEquals(out.readUnsignedInt(), meter));
    }

    @Override
    protected Class<? extends Instruction> getClazz() {
        return MeterCase.class;
    }

    @Override
    protected int getType() {
        return InstructionConstants.METER_TYPE;
    }

    @Override
    protected int getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }

}
