/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yangtools.binding.util.BindingMap;

public abstract class AbstractActionInstructionDeserializer extends AbstractInstructionDeserializer
        implements DeserializerRegistryInjector {

    private DeserializerRegistry registry;
    private final ActionPath actionPath;

    /**
     * Create new instacte of action instruction deserializer.
     *
     * @param actionPath action extension path
     */
    public AbstractActionInstructionDeserializer(final ActionPath actionPath) {
        this.actionPath = actionPath;
    }

    /**
     * Skip first few bytes of instruction message because they are irrelevant and then return length.
     *
     * @param message Openflow buffered message
     * @return instruction length
     **/
    protected static int readHeader(final ByteBuf message) {
        message.skipBytes(Short.BYTES);
        return message.readUnsignedShort();
    }

    /**
     * Read list of actions from message.
     *
     * @param message Openflow buffered message
     * @param length  instruction length
     * @return list of actions
     **/
    protected Map<ActionKey, Action> readActions(final ByteBuf message, final int length) {
        if (message.readableBytes() <= 0) {
            return Map.of();
        }

        final int instrLength = length - InstructionConstants.STANDARD_INSTRUCTION_LENGTH;
        final var actions = BindingMap.<ActionKey, Action>orderedBuilder();
        final int startIndex = message.readerIndex();
        int offset = 0;

        while (message.readerIndex() - startIndex < instrLength) {
            actions.add(new ActionBuilder()
                .setOrder(offset++)
                .setAction(ActionUtil.readAction(EncodeConstants.OF_VERSION_1_3, message, registry, actionPath))
                .build());
        }

        return actions.build();
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
