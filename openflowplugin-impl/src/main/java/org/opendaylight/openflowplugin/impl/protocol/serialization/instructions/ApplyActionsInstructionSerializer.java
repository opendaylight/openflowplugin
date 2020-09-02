/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerLookup;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;

public class ApplyActionsInstructionSerializer extends AbstractActionInstructionSerializer<ApplyActionsCase> {
    public ApplyActionsInstructionSerializer(final SerializerLookup registry) {
        super(registry);
    }

    @Override
    public void serialize(final ApplyActionsCase input, final ByteBuf outBuffer) {
        int index = outBuffer.writerIndex();
        super.serialize(input, outBuffer);
        writeActions(input.getApplyActions(), EncodeConstants.OF13_VERSION_ID, outBuffer,index);
    }

    @Override
    protected int getType() {
        return InstructionConstants.APPLY_ACTIONS_TYPE;
    }

    @Override
    protected int getLength() {
        return InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
    }
}
