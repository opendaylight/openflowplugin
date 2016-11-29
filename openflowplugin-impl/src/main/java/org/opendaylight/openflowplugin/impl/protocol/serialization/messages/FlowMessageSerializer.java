/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.keys.InstructionSerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.ActionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public class FlowMessageSerializer extends AbstractMessageSerializer<FlowMessage> implements SerializerRegistryInjector {
    private static final FlowCookie DEFAULT_COOKIE = new FlowCookie(OFConstants.DEFAULT_COOKIE);
    private static final FlowCookie DEFAULT_COOKIE_MASK = new FlowCookie(OFConstants.DEFAULT_COOKIE_MASK);
    private static final Short DEFAULT_TABLE_ID = (short) 0L;
    private static final Integer DEFAULT_IDLE_TIMEOUT = 0;
    private static final Integer DEFAULT_HARD_TIMEOUT = 0;
    private static final Integer DEFAULT_PRIORITY = OFConstants.DEFAULT_FLOW_PRIORITY;
    private static final Long DEFAULT_BUFFER_ID = OFConstants.OFP_NO_BUFFER;
    private static final BigInteger DEFAULT_OUT_PORT = BigInteger.valueOf(OFConstants.OFPP_ANY);
    private static final Long DEFAULT_OUT_GROUP = OFConstants.OFPG_ANY;
    private static final byte PADDING_IN_FLOW_MOD_MESSAGE = 2;
    private static final FlowModFlags DEFAULT_FLAGS = new FlowModFlags(false, false, false, false, false);

    private SerializerRegistry registry;

    @Override
    public void serialize(FlowMessage message, ByteBuf outBuffer) {
        super.serialize(message, outBuffer);

        outBuffer.writeLong(MoreObjects.firstNonNull(message.getCookie(), DEFAULT_COOKIE).getValue().longValue());
        outBuffer.writeLong(MoreObjects.firstNonNull(message.getCookieMask(), DEFAULT_COOKIE_MASK).getValue().longValue());
        outBuffer.writeByte(MoreObjects.firstNonNull(message.getTableId(), DEFAULT_TABLE_ID));
        outBuffer.writeByte(message.getCommand().getIntValue());
        outBuffer.writeShort(MoreObjects.firstNonNull(message.getIdleTimeout(), DEFAULT_IDLE_TIMEOUT));
        outBuffer.writeShort(MoreObjects.firstNonNull(message.getHardTimeout(), DEFAULT_HARD_TIMEOUT));
        outBuffer.writeShort(MoreObjects.firstNonNull(message.getPriority(), DEFAULT_PRIORITY));
        outBuffer.writeInt(MoreObjects.firstNonNull(message.getBufferId(), DEFAULT_BUFFER_ID).intValue());
        outBuffer.writeInt(MoreObjects.firstNonNull(message.getOutPort(), DEFAULT_OUT_PORT).intValue());
        outBuffer.writeInt(MoreObjects.firstNonNull(message.getOutGroup(), DEFAULT_OUT_GROUP).intValue());
        outBuffer.writeShort(createFlowModFlagsBitmask(MoreObjects.firstNonNull(message.getFlags(), DEFAULT_FLAGS)));
        outBuffer.writeZero(PADDING_IN_FLOW_MOD_MESSAGE);

        registry.<Match, OFSerializer<Match>>getSerializer(new MessageTypeKey<>(message.getVersion(), Match.class))
                .serialize(message.getMatch(), outBuffer);

        Optional.ofNullable(message.getInstructions()).flatMap(is -> Optional.ofNullable(is.getInstruction()))
                .ifPresent(is -> is
                        .stream()
                        .map(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction::getInstruction)
                        .forEach(i -> registry.<Instruction, OFSerializer<Instruction>>getSerializer(
                                new InstructionSerializerKey<>(
                                        EncodeConstants.OF13_VERSION_ID,
                                        (Class<Instruction>) i.getImplementedInterface(),
                                        null))
                                .serialize(i, outBuffer)
                        ));

        ByteBufUtils.updateOFHeaderLength(outBuffer);

    }

    private static int createFlowModFlagsBitmask(final FlowModFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.isSENDFLOWREM(),
                flags.isCHECKOVERLAP(),
                flags.isRESETCOUNTS(),
                flags.isNOPKTCOUNTS(),
                flags.isNOBYTCOUNTS());
    }

    private static boolean isSetVlanIdActionCasePresent(final Flow flow) {
         return Optional
                 .ofNullable(flow.getInstructions())
                 .flatMap(is -> Optional.ofNullable(is.getInstruction()))
                 .map(is -> is
                         .stream()
                         .map(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction::getInstruction)
                         .filter(ApplyActionsCase.class::isInstance)
                         .map(i -> ApplyActionsCase.class.cast(i).getApplyActions())
                         .filter(Objects::nonNull)
                         .map(ActionList::getAction)
                         .filter(Objects::nonNull)
                         .flatMap(Collection::stream)
                         .map(Action::getAction)
                         .filter(SetVlanIdActionCase.class::isInstance)
                         .count())
                 .orElse(0L) > 0;
    }

    @Override
    protected byte getMessageType() {
        return 14;
    }

    @Override
    public void injectSerializerRegistry(SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
