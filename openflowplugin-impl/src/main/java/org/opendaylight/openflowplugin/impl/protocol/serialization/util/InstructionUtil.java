/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.util;


import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;

/**
 * Utility class for instruction serialization
 */
public class InstructionUtil {

    /**
     * Serialize instruction
     * @param instruction OpenFlow instruction
     * @param version OpenFlow version
     * @param registry OpenFlowJava serializer registry
     * @param outBuffer output buffer
     */
    @SuppressWarnings("unchecked")
    public static void writeInstruction(final Instruction instruction,
                                        final short version,
                                        final SerializerRegistry registry,
                                        final ByteBuf outBuffer) {

        registry.<Instruction, OFSerializer<Instruction>>getSerializer(
            new MessageTypeKey<>(
                EncodeConstants.OF13_VERSION_ID,
                (Class<Instruction>) instruction.getImplementedInterface()))
            .serialize(instruction, outBuffer);
    }


    /**
     * Serialize instruction header
     * @param instruction OpenFlow instruction
     * @param version OpenFlow version
     * @param registry OpenFlowJava serializer registry
     * @param outBuffer output buffer
     */
    @SuppressWarnings("unchecked")
    public static void writeInstructionHeader(final Instruction instruction,
                                              final short version,
                                              final SerializerRegistry registry,
                                              final ByteBuf outBuffer) {

        registry.<Instruction, HeaderSerializer<Instruction>>getSerializer(
            new MessageTypeKey<>(
                EncodeConstants.OF13_VERSION_ID,
                (Class<Instruction>) instruction.getImplementedInterface()))
            .serializeHeader(instruction, outBuffer);
    }

}
