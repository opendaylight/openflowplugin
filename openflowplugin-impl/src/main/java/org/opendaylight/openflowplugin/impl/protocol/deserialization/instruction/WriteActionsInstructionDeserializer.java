/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.WriteActionsBuilder;

public class WriteActionsInstructionDeserializer extends AbstractActionInstructionDeserializer {
    public WriteActionsInstructionDeserializer(final DeserializerRegistry registry, final ActionPath path) {
        super(registry, path);
    }

    @Override
    public Instruction deserialize(final ByteBuf message) {
        final int length = readHeader(message);
        message.skipBytes(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

        return new WriteActionsCaseBuilder()
                .setWriteActions(new WriteActionsBuilder()
                        .setAction(readActions(message, length))
                        .build())
                .build();
    }

    @Override
    public Instruction deserializeHeader(final ByteBuf message) {
        processHeader(message);
        return new WriteActionsCaseBuilder().build();
    }

}
