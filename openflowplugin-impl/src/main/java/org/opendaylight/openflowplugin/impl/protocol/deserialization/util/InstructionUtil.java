/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization.util;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeActionExperimenterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

/**
 * Utility class for action deserialization
 */
public class InstructionUtil {

    /**
     * Deserialize OpenFlow instruction
     *
     * @param version OpenFlow version
     * @param message OpenFlow buffered message
     * @param registry deserializer registry
     */
    public static Instruction readInstruction(final short version,
                                              final ByteBuf message,
                                              final DeserializerRegistry registry) {
        final int type = message.getUnsignedShort(message.readerIndex());
        final OFDeserializer<Instruction> deserializer;

        if (InstructionConstants.APPLY_ACTIONS_TYPE == type) {
            deserializer = registry.getDeserializer(
                new MessageCodeActionExperimenterKey(
                    version, type, Instruction.class,
                    ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION,
                    null));
        } else if (InstructionConstants.WRITE_ACTIONS_TYPE == type) {
            deserializer = registry.getDeserializer(
                new MessageCodeActionExperimenterKey(
                    version, type, Instruction.class,
                    ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION,
                    null));
        } else {
            Long expId = null;

            if (EncodeConstants.EXPERIMENTER_VALUE == type) {
                expId = message.getUnsignedInt(message.readerIndex() + 2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
            }

            deserializer = registry.getDeserializer(
                new MessageCodeExperimenterKey(
                    version, type, Instruction.class, expId));
        }

        return deserializer.deserialize(message);
    }

    /**
     * Deserialize OpenFlow instruction header
     *
     * @param version OpenFlow version
     * @param message OpenFlow buffered message
     * @param registry deserializer registry
     */
    public static Instruction readInstructionHeader(final short version,
                                                    final ByteBuf message,
                                                    final DeserializerRegistry registry) {
        final int type = message.getUnsignedShort(message.readerIndex());
        final HeaderDeserializer<Instruction> deserializer;

        if (InstructionConstants.APPLY_ACTIONS_TYPE == type) {
            deserializer = registry.getDeserializer(
                new MessageCodeActionExperimenterKey(
                    version, type, Instruction.class,
                    ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION,
                    null));
        } else if (InstructionConstants.WRITE_ACTIONS_TYPE == type) {
            deserializer = registry.getDeserializer(
                new MessageCodeActionExperimenterKey(
                    version, type, Instruction.class,
                    ActionPath.NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION,
                    null));
        } else {
            Long expId = null;

            if (EncodeConstants.EXPERIMENTER_VALUE == type) {
                expId = message.getUnsignedInt(message.readerIndex() + 2 * EncodeConstants.SIZE_OF_SHORT_IN_BYTES);
            }

            deserializer = registry.getDeserializer(
                new MessageCodeExperimenterKey(
                    version, type, Instruction.class, expId));
        }

        return deserializer.deserializeHeader(message);
    }

}
