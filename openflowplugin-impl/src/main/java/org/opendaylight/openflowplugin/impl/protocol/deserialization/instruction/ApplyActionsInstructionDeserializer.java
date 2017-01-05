/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;

import io.netty.buffer.ByteBuf;

public class ApplyActionsInstructionDeserializer extends AbstractActionInstructionDeserializer {

    public ApplyActionsInstructionDeserializer(ActionPath path) {
        super(path);
    }

    @Override
    public Instruction deserialize(ByteBuf message) {
        final int length = readHeader(message);
        message.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        return new ApplyActionsCaseBuilder()
            .setApplyActions(new ApplyActionsBuilder()
                    .setAction(readActions(message, length))
                    .build())
            .build();
    }

    @Override
    public Instruction deserializeHeader(ByteBuf message) {
        processHeader(message);
        return new ApplyActionsCaseBuilder().build();
    }

}
