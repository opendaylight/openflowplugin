/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.clear.actions._case.ClearActionsBuilder;

import io.netty.buffer.ByteBuf;

public class ClearActionsInstructionDeserializer extends AbstractInstructionDeserializer {

    @Override
    public Instruction deserialize(ByteBuf message) {
        processHeader(message);
        message.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        return new ClearActionsCaseBuilder()
            .setClearActions(new ClearActionsBuilder().build())
            .build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new ClearActionsCaseBuilder().build();
    }

}
