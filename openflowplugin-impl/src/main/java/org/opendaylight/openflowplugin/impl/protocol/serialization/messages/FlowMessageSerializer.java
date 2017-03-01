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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.InstructionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.ActionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.VlanCfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;

/**
 * Translates FlowMod messages.
 * OF protocol versions: 1.3
 */
public class FlowMessageSerializer extends AbstractMessageSerializer<FlowMessage> implements SerializerRegistryInjector {
    private static final FlowCookie DEFAULT_COOKIE = new FlowCookie(OFConstants.DEFAULT_COOKIE);
    private static final FlowCookie DEFAULT_COOKIE_MASK = new FlowCookie(OFConstants.DEFAULT_COOKIE_MASK);
    private static final Short DEFAULT_TABLE_ID = (short) 0;
    private static final Integer DEFAULT_IDLE_TIMEOUT = 0;
    private static final Integer DEFAULT_HARD_TIMEOUT = 0;
    private static final Integer DEFAULT_PRIORITY = OFConstants.DEFAULT_FLOW_PRIORITY;
    private static final Long DEFAULT_BUFFER_ID = OFConstants.OFP_NO_BUFFER;
    private static final BigInteger DEFAULT_OUT_PORT = BigInteger.valueOf(OFConstants.OFPP_ANY);
    private static final Long DEFAULT_OUT_GROUP = OFConstants.OFPG_ANY;
    private static final byte PADDING_IN_FLOW_MOD_MESSAGE = 2;
    private static final FlowModFlags DEFAULT_FLAGS = new FlowModFlags(false, false, false, false, false);
    private static final Integer PUSH_VLAN = 0x8100;

    private static final Comparator<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                .instruction.list.Instruction> COMPARATOR = (inst1, inst2) -> {
        if (inst1.getOrder() == null || inst2.getOrder() == null) return 0;
        return inst1.getOrder().compareTo(inst2.getOrder());
    };

    private static final VlanMatch VLAN_MATCH_FALSE = new VlanMatchBuilder()
            .setVlanId(new VlanIdBuilder()
                    .setVlanIdPresent(false)
                    .setVlanId(new VlanId(0))
                    .build())
            .build();

    private static final VlanMatch VLAN_MATCH_TRUE = new VlanMatchBuilder()
            .setVlanId(new VlanIdBuilder()
                    .setVlanIdPresent(true)
                    .setVlanId(new VlanId(0))
                    .build())
            .build();

    private SerializerRegistry registry;

    @Override
    public void serialize(FlowMessage message, ByteBuf outBuffer) {
        if (!isVlanMatchPresent(message) && isSetVlanIdActionCasePresent(message)) {
            writeVlanFlow(message, outBuffer);
        } else {
            writeFlow(message, outBuffer);
        }
    }

    /**
     * Serialize flow message. Needs to be separated from main serialize method to prevent recursion
     * when serializing SetVlanId flows
     * @param message flow message
     * @param outBuffer output buffer
     */
    private void writeFlow(final FlowMessage message, final ByteBuf outBuffer) {
        int index = outBuffer.writerIndex();
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
        writeMatch(message, outBuffer);
        writeInstructions(message, outBuffer);
        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    /**
     * Instead of serializing this flow normally, we need to split it to two parts if flow contains
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase}
     * @param message flow mod message
     * @param outBuffer output buffer
     */
    private void writeVlanFlow(final FlowMessage message, final ByteBuf outBuffer) {
        writeFlow(
            new FlowMessageBuilder(message)
                .setMatch(new MatchBuilder(message.getMatch())
                    .setVlanMatch(VLAN_MATCH_FALSE)
                    .build())
                .setInstructions(new InstructionsBuilder()
                    .setInstruction(updateSetVlanIdAction(message))
                    .build())
                .build(),
            outBuffer);

        writeFlow(
            new FlowMessageBuilder(message)
                .setMatch(new MatchBuilder(message.getMatch())
                    .setVlanMatch(VLAN_MATCH_TRUE)
                    .build())
                .build(),
            outBuffer);
    }

    /**
     * Serialize OpenFlowPlugin match to raw bytes
     * @param message OpenFlow flow mod message
     * @param outBuffer output buffer
     */
    private void writeMatch(final FlowMessage message, final ByteBuf outBuffer) {
        registry.<Match, OFSerializer<Match>>getSerializer(new MessageTypeKey<>(message.getVersion(), Match.class))
                .serialize(message.getMatch(), outBuffer);

    }

    /**
     * Serialize OpenFlowPlugin instructions and set ip protocol of set-tp-src and set-tp-dst actions of need
     * @param message OpenFlow flow mod message
     * @param outBuffer output buffer
     */
    @SuppressWarnings("unchecked")
    private void writeInstructions(final FlowMessage message, final ByteBuf outBuffer) {
        // Try to get IP protocol from IP match
        final Optional<Short> protocol = Optional
                .ofNullable(message.getMatch())
                .flatMap(m -> Optional.ofNullable(m.getIpMatch()))
                .flatMap(ipm -> Optional.ofNullable(ipm.getIpProtocol()));

        // Update instructions if needed and then serialize all instructions
        Optional.ofNullable(message.getInstructions())
                .flatMap(is -> Optional.ofNullable(is.getInstruction()))
                .ifPresent(is -> is
                        .stream()
                        .filter(Objects::nonNull)
                        .sorted(COMPARATOR)
                        .map(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction::getInstruction)
                        .filter(Objects::nonNull)
                        .map(i -> protocol.flatMap(p -> updateInstruction(i, p)).orElse(i))
                        .forEach(i -> InstructionUtil.writeInstruction(i, EncodeConstants.OF13_VERSION_ID, registry, outBuffer)));
    }

    /**
     * Determine type of instruction and update it's actions if it is apply-actions instruction
     * @param instruction instruction
     * @param protocol protocol
     * @return updated instruction or empty
     */
    private static Optional<Instruction> updateInstruction(final Instruction instruction, final Short protocol) {
        if( ApplyActionsCase.class.isInstance(instruction)) {
            return Optional
                .ofNullable(ApplyActionsCase.class.cast(instruction).getApplyActions())
                .flatMap(aa -> Optional.ofNullable(aa.getAction()))
                .map(as -> new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(as
                            .stream()
                            .filter(Objects::nonNull)
                            .map(a -> updateSetTpActions(a, protocol))
                            .collect(Collectors.toList()))
                        .build())
                    .build());
        }

        return Optional.empty();
    }

    /**
     * If action is set-tp-src or set-tp-dst, inject IP protocol into it, otherwise return original action
     * @param action OpenFlow action
     * @param protocol IP protocol
     * @return updated OpenFlow action
     */
    private static Action updateSetTpActions(Action action, Short protocol) {
        if (SetTpSrcActionCase.class.isInstance(action.getAction())) {
            final SetTpSrcActionCase actionCase = SetTpSrcActionCase.class.cast(action.getAction());

            return new ActionBuilder(action)
                    .setAction(new SetTpSrcActionCaseBuilder(actionCase)
                            .setSetTpSrcAction(new SetTpSrcActionBuilder(
                                    actionCase.getSetTpSrcAction())
                                    .setIpProtocol(protocol)
                                    .build())
                            .build())
                    .build();
        } else if (SetTpDstActionCase.class.isInstance(action.getAction())) {
            final SetTpDstActionCase actionCase = SetTpDstActionCase.class.cast(action.getAction());

            return new ActionBuilder(action)
                    .setAction(new SetTpDstActionCaseBuilder(actionCase)
                            .setSetTpDstAction(new SetTpDstActionBuilder(
                                    actionCase.getSetTpDstAction())
                                    .setIpProtocol(protocol)
                                    .build())
                            .build())
                    .build();
        }

        // Return original action if no modifications are needed
        return action;
    }

    /**
     * Create copy of instructions of original flow but insert #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase}
     * before each #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase}
     * @param message OpenFlowPlugin flow mod message
     * @return list of instructions
     */
    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list
            .Instruction> updateSetVlanIdAction(final FlowMessage message) {
        return message.getInstructions().getInstruction()
                .stream()
                .map(i -> {
                    final int[] offset = { 0 };

                    return ApplyActionsCase.class.isInstance(i.getInstruction())
                            ? Optional
                            .ofNullable(ApplyActionsCase.class.cast(i.getInstruction()).getApplyActions())
                            .flatMap(as -> Optional.ofNullable(as.getAction()))
                            .map(a -> ActionUtil.sortActions(a)
                                    .stream()
                                    .flatMap(action -> {
                                final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                                        .action.list.Action> actions = new ArrayList<>();

                                // If current action is SetVlanId, insert PushVlan action before it and update order
                                if (SetVlanIdActionCase.class.isInstance(action.getAction())) {
                                    actions.add(new ActionBuilder()
                                            .setAction(new PushVlanActionCaseBuilder()
                                                    .setPushVlanAction(new PushVlanActionBuilder()
                                                            .setCfi(new VlanCfi(1))
                                                            .setVlanId(SetVlanIdActionCase.class.cast(action.getAction()).getSetVlanIdAction().getVlanId())
                                                            .setEthernetType(PUSH_VLAN)
                                                            .setTag(PUSH_VLAN)
                                                            .build())
                                                    .build())
                                            .setKey(action.getKey())
                                            .setOrder(action.getOrder() + offset[0])
                                            .build());

                                    offset[0]++;
                                }

                                // Update offset of action if there is any inserted PushVlan actions
                                actions.add(offset[0] > 0
                                        ? new ActionBuilder(action).setOrder(action.getOrder() + offset[0]).build()
                                        : action);

                                return actions.stream();
                            }))
                            .map(as -> new InstructionBuilder(i)
                                    .setInstruction(new ApplyActionsCaseBuilder()
                                            .setApplyActions(new ApplyActionsBuilder()
                                                    .setAction(as.collect(Collectors.toList()))
                                                    .build())
                                            .build())
                                    .build())
                            .orElse(i)
                            : i;
                }).collect(Collectors.toList());
    }

    /**
     * Create integer bit mask from #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags}
     * @param flags flow mod flags
     * @return bit mask
     */
    private static int createFlowModFlagsBitmask(final FlowModFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.isSENDFLOWREM(),
                flags.isCHECKOVERLAP(),
                flags.isRESETCOUNTS(),
                flags.isNOPKTCOUNTS(),
                flags.isNOBYTCOUNTS());
    }

    /**
     * Determine if flow contains vlan match
     * @param flow flow
     * @return true if flow contains vlan match
     */
    private static boolean isVlanMatchPresent(final Flow flow) {
        return Optional
            .ofNullable(flow.getMatch())
            .flatMap(m -> Optional.ofNullable(m.getVlanMatch()))
            .isPresent();
    }

    /**
     * Determine if flow contains #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase}
     * instruction with #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase} action
     * @param flow OpenFlowPlugin flow
     * @return true if flow contains SetVlanIdAction
     */
    private static boolean isSetVlanIdActionCasePresent(final Flow flow) {
        return Optional
                .ofNullable(flow.getInstructions())
                .flatMap(is -> Optional.ofNullable(is.getInstruction()))
                .flatMap(is -> is
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
                        .findFirst())
                .isPresent();
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
