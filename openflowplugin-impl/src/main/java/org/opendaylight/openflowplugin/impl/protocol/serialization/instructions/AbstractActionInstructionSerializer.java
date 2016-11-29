/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.ActionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

public abstract class AbstractActionInstructionSerializer extends AbstractInstructionSerializer implements SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    public void serialize(Instruction input, ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
    }

    /**
     * Try to write list of OpenFlowPlugin actions to output buffer
     * @param actions List of OpenFlowPlugin actions
     * @param outBuffer output buffer
     * @param startIndex start index of byte buffer
     */
    protected void writeActions(ActionList actions, short version, ByteBuf outBuffer, int startIndex) {
        Optional.ofNullable(actions).flatMap(as -> Optional.ofNullable(as.getAction())).map(as -> {
            int lengthIndex = outBuffer.writerIndex();
            outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
            outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
            ActionUtil.sortActions(as).forEach(a -> ActionUtil.writeAction(a.getAction(), version, registry, outBuffer));
            outBuffer.setShort(lengthIndex, outBuffer.writerIndex() - startIndex);
            return actions;
        }).orElseGet(() -> {
            outBuffer.writeShort(getLength());
            outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
            return actions;
        });
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

}
