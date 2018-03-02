/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;

public class MeterInstructionSerializer extends AbstractInstructionSerializer<MeterCase> {

    @Override
    public void serialize(MeterCase input, ByteBuf outBuffer) {
        super.serialize(input, outBuffer);
        outBuffer.writeInt(input.getMeter().getMeterId().getValue().intValue());
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
