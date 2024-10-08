/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.messages;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint16;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint32;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint64;
import static org.opendaylight.yangtools.yang.common.netty.ByteBufUtils.readUint8;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.keys.MessageCodeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeActionExperimenterKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yangtools.binding.util.BindingMap;

public class FlowMessageDeserializer implements OFDeserializer<FlowMessage>, DeserializerRegistryInjector {

    private static final byte PADDING = 2;

    private static final MessageCodeKey MATCH_KEY = new MessageCodeMatchKey(EncodeConstants.OF_VERSION_1_3,
            EncodeConstants.EMPTY_VALUE, Match.class,
            MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);

    private DeserializerRegistry registry;

    @Override
    public FlowMessage deserialize(final ByteBuf message) {
        final FlowMessageBuilder builder = new FlowMessageBuilder()
            .setVersion(EncodeConstants.OF_VERSION_1_3)
            .setXid(readUint32(message))
            .setCookie(new FlowCookie(readUint64(message)))
            .setCookieMask(new FlowCookie(readUint64(message)))
            .setTableId(readUint8(message))
            .setCommand(FlowModCommand.forValue(message.readUnsignedByte()))
            .setIdleTimeout(readUint16(message))
            .setHardTimeout(readUint16(message))
            .setPriority(readUint16(message))
            .setBufferId(readUint32(message))
            .setOutPort(readUint32(message).toUint64())
            .setOutGroup(readUint32(message))
            .setFlags(createFlowModFlagsFromBitmap(message.readUnsignedShort()));

        message.skipBytes(PADDING);

        final OFDeserializer<Match> matchDeserializer = requireNonNull(registry).getDeserializer(MATCH_KEY);
        builder.setMatch(new MatchBuilder(matchDeserializer.deserialize(message)).build());

        final int length = message.readableBytes();

        if (length > 0) {
            final var instructions = BindingMap.<InstructionKey,
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
                    orderedBuilder();
            final int startIndex = message.readerIndex();
            int offset = 0;

            while (message.readerIndex() - startIndex < length) {
                final int type = message.getUnsignedShort(message.readerIndex());
                OFDeserializer<Instruction> deserializer = null;

                if (InstructionConstants.APPLY_ACTIONS_TYPE == type) {
                    deserializer = requireNonNull(registry).getDeserializer(
                            new MessageCodeActionExperimenterKey(
                                EncodeConstants.OF_VERSION_1_3, type, Instruction.class,
                                ActionPath.INVENTORY_FLOWNODE_TABLE_APPLY_ACTIONS,
                                null));
                } else if (InstructionConstants.WRITE_ACTIONS_TYPE == type) {
                    deserializer = requireNonNull(registry).getDeserializer(
                            new MessageCodeActionExperimenterKey(
                                EncodeConstants.OF_VERSION_1_3, type, Instruction.class,
                                ActionPath.INVENTORY_FLOWNODE_TABLE_WRITE_ACTIONS,
                                null));
                } else {
                    Long expId = null;

                    if (EncodeConstants.EXPERIMENTER_VALUE == type) {
                        expId = message.getUnsignedInt(message.readerIndex() + 2 * Short.BYTES);
                    }

                    deserializer = requireNonNull(registry).getDeserializer(
                            new MessageCodeExperimenterKey(
                                EncodeConstants.OF_VERSION_1_3, type, Instruction.class, expId));
                }

                instructions.add(new InstructionBuilder()
                        .setOrder(offset)
                        .setInstruction(deserializer.deserialize(message))
                        .build());

                offset++;
            }

            builder.setInstructions(new InstructionsBuilder()
                    .setInstruction(instructions.build())
                    .build());
        }

        return builder.build();
    }

    private static FlowModFlags createFlowModFlagsFromBitmap(final int input) {
        final Boolean ofp_FF_SendFlowRem = (input & 1 << 0) != 0;
        final Boolean ofp_FF_CheckOverlap = (input & 1 << 1) != 0;
        final Boolean ofp_FF_ResetCounts = (input & 1 << 2) != 0;
        final Boolean ofp_FF_NoPktCounts = (input & 1 << 3) != 0;
        final Boolean ofp_FF_NoBytCounts = (input & 1 << 4) != 0;
        return new FlowModFlags(ofp_FF_CheckOverlap, ofp_FF_NoBytCounts, ofp_FF_NoPktCounts, ofp_FF_ResetCounts,
                ofp_FF_SendFlowRem);
    }

    @Override
    public void injectDeserializerRegistry(final DeserializerRegistry deserializerRegistry) {
        registry = deserializerRegistry;
    }
}
