/*
 * Copyright (c) 2016 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.serializers;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.util.stream.Stream;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.openflowjava.protocol.impl.util.InstructionConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRaw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;

public class FlowRawSerializer implements OFSerializer<FlowRaw>, SerializerRegistryInjector {
    private static final byte MESSAGE_TYPE = 14;
    private static final byte PADDING_IN_FLOW_MOD_MESSAGE = 2;

    private SerializerRegistry registry;

    @Override
    public void serialize(final FlowRaw message, final ByteBuf outBuffer) {
        ByteBufUtils.writeOFHeader(MESSAGE_TYPE, message, outBuffer, EncodeConstants.EMPTY_LENGTH);

        outBuffer.writeLong(MoreObjects.firstNonNull(
                message.getCookie(),
                new FlowCookie(OFConstants.DEFAULT_COOKIE))
                .getValue()
                .longValue());

        outBuffer.writeLong(MoreObjects.firstNonNull(
                message.getCookieMask(),
                new FlowCookie(OFConstants.DEFAULT_COOKIE_MASK))
                .getValue()
                .longValue());

        outBuffer.writeByte(MoreObjects.firstNonNull(
                message.getTableId(),
                0)
                .byteValue());

        outBuffer.writeByte(message.getCommand().getIntValue());

        outBuffer.writeShort(MoreObjects.firstNonNull(
                message.getIdleTimeout(),
                0));

        outBuffer.writeShort(MoreObjects.firstNonNull(
                message.getHardTimeout(),
                0));

        outBuffer.writeShort(MoreObjects.firstNonNull(
                message.getPriority(),
                OFConstants.DEFAULT_FLOW_PRIORITY)
                .shortValue());

        outBuffer.writeInt(MoreObjects.firstNonNull(
                message.getBufferId(),
                OFConstants.OFP_NO_BUFFER)
                .intValue());

        outBuffer.writeInt(MoreObjects.firstNonNull(
                message.getOutPort(),
                OFConstants.ANY)
                .intValue());

        outBuffer.writeInt(MoreObjects.firstNonNull(
                message.getOutGroup(),
                OFConstants.ANY)
                .intValue());

        outBuffer.writeShort(createFlowModFlagsBitmask(MoreObjects.firstNonNull(
                message.getFlags(),
                new FlowModFlags(false, false, false, false, false))));

        outBuffer.writeZero(PADDING_IN_FLOW_MOD_MESSAGE);

        registry.<Match, OFSerializer<Match>>getSerializer(new MessageTypeKey<>(message.getVersion(), Match.class))
                .serialize(message.getMatch(), outBuffer);

        message.getInstructions()
                .getInstruction()
                .stream()
                .map(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction::getInstruction)
                .forEach(instruction -> {
                    if (instruction instanceof ApplyActions) {
                        int startIndex = outBuffer.writerIndex();
                        outBuffer.writeShort(InstructionConstants.APPLY_ACTIONS_TYPE);

                        final Stream<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> actionStream =
                                ApplyActions.class.cast(instruction)
                                        .getAction()
                                        .stream()
                                        .sorted((o1, o2) -> MoreObjects.firstNonNull(o2.getOrder(), 0) -
                                                MoreObjects.firstNonNull(o1.getOrder(), 0))
                                        .map(Action::getAction);

                        int lengthIndex = outBuffer.writerIndex();
                        outBuffer.writeShort(EncodeConstants.EMPTY_LENGTH);
                        outBuffer.writeZero(InstructionConstants.PADDING_IN_ACTIONS_INSTRUCTION);

                        actionStream.forEach(action -> {
                            if (action instanceof DecNwTtlCase) {
                                outBuffer.writeShort(ActionConstants.DEC_NW_TTL_CODE);
                                outBuffer.writeShort(ActionConstants.GENERAL_ACTION_LENGTH);
                                outBuffer.writeZero(ActionConstants.PADDING_IN_ACTION_HEADER);
                            }
                        });

                        int instructionLength = outBuffer.writerIndex() - startIndex;
                        outBuffer.setShort(lengthIndex, instructionLength);
                    }
                });

        ByteBufUtils.updateOFHeaderLength(outBuffer);
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        this.registry = serializerRegistry;
    }

    private static int createFlowModFlagsBitmask(final FlowModFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.isSENDFLOWREM(),
                flags.isCHECKOVERLAP(),
                flags.isRESETCOUNTS(),
                flags.isNOPKTCOUNTS(),
                flags.isNOBYTCOUNTS());
    }
}
