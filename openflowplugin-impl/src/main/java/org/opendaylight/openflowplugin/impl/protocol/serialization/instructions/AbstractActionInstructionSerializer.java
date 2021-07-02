/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.instructions;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.ActionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yangtools.yang.common.Uint8;

public abstract class AbstractActionInstructionSerializer<T extends Instruction>
        extends AbstractInstructionSerializer<T> implements SerializerRegistryInjector {
    private SerializerRegistry registry = null;

    @Override
    public void serialize(final T input, final ByteBuf outBuffer) {
        outBuffer.writeShort(getType());
    }

    /**
     * Try to write list of OpenFlowPlugin actions to output buffer.
     *
     * @param actions List of OpenFlowPlugin actions
     * @param outBuffer output buffer
     * @param startIndex start index of byte buffer
     */
    protected void writeActions(final ActionList actions, final Uint8 version, final ByteBuf outBuffer,
            final int startIndex) {
        if (actions != null) {
            final int lengthIndex = outBuffer.writerIndex();
            outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
            outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
            actions.nonnullAction().values().stream()
                .sorted(OrderComparator.build())
                .forEach(a -> ActionUtil.writeAction(a.getAction(), version, registry, outBuffer));
            outBuffer.setShort(lengthIndex, outBuffer.writerIndex() - startIndex);
        } else {
            outBuffer.writeShort(getLength());
            outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);
        }
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = requireNonNull(serializerRegistry);
    }
}
