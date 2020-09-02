/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import static java.util.Objects.requireNonNull;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerLookup;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;

public abstract class AbstractActionInstructionDeserializer extends AbstractInstructionDeserializer {
    private final DeserializerLookup registry;
    private final ActionPath actionPath;

    /**
     * Create new instacte of action instruction deserializer.
     *
     * @param registry deserializer registry
     * @param actionPath action extension path
     */
    public AbstractActionInstructionDeserializer(final DeserializerLookup registry, final ActionPath actionPath) {
        this.registry = requireNonNull(registry);
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
    protected List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> readActions(
            final ByteBuf message, final int length) {
        final int instrLength = length - InstructionConstants.STANDARD_INSTRUCTION_LENGTH;

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions =
                new ArrayList<>();

        if (message.readableBytes() > 0) {
            final int startIndex = message.readerIndex();
            int offset = 0;

            while (message.readerIndex() - startIndex < instrLength) {
                actions.add(new ActionBuilder()
                    .withKey(new ActionKey(offset))
                    .setOrder(offset)
                    .setAction(ActionUtil.readAction(EncodeConstants.OF13_VERSION_ID, message, registry, actionPath))
                    .build());
                offset++;
            }
        }

        return actions;
    }
}
