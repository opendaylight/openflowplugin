/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.instruction;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.util.ActionUtil;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;

import io.netty.buffer.ByteBuf;

public abstract class AbstractActionInstructionDeserializer extends AbstractInstructionDeserializer
    implements DeserializerRegistryInjector {

    private DeserializerRegistry registry;
    private final ActionPath actionPath;

    /**
     * Create new instacte of action instruction deserializer
     * @param actionPath action extension path
     */
    public AbstractActionInstructionDeserializer(final ActionPath actionPath) {
        this.actionPath = actionPath;
    }

    /**
     * Skip first few bytes of instruction message because they are irrelevant and then return length
     * @param message Openflow buffered message
     * @return instruction length
     **/
    protected static int readHeader(ByteBuf message) {
        message.skipBytes(EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
        return message.readUnsignedShort();
    }

    /**
     * Read list of actions from message
     * @param message Openflow buffered message
     * @param length instruction length
     * @return list of actions
     **/
    protected List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
        .Action> readActions(ByteBuf message, int length) {

        final int instrLength = length - InstructionConstants.STANDARD_INSTRUCTION_LENGTH;

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
            .Action> actions = new ArrayList<>();

        if (message.readableBytes() > 0) {
            final int startIndex = message.readerIndex();
            int offset = 0;

            while ((message.readerIndex() - startIndex) < instrLength) {
                actions.add(new ActionBuilder()
                        .setKey(new ActionKey(offset))
                        .setOrder(offset)
                        .setAction(ActionUtil
                            .readAction(EncodeConstants.OF13_VERSION_ID, message, registry, actionPath))
                        .build());

                offset++;
            }
        }

        return actions;
    }

    @Override
    public void injectDeserializerRegistry(DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }

}
