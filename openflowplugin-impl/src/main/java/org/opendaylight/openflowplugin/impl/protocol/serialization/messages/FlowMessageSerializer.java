/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.messages;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.InstructionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Translates FlowMod messages.
 * OF protocol versions: 1.3
 */
public class FlowMessageSerializer extends AbstractMessageSerializer<FlowMessage> implements
        SerializerRegistryInjector {
    private static final FlowCookie DEFAULT_COOKIE = new FlowCookie(OFConstants.DEFAULT_COOKIE);
    private static final FlowCookie DEFAULT_COOKIE_MASK = new FlowCookie(OFConstants.DEFAULT_COOKIE_MASK);
    private static final Uint8 DEFAULT_TABLE_ID = Uint8.ZERO;
    private static final Uint16 DEFAULT_IDLE_TIMEOUT = Uint16.ZERO;
    private static final Uint16 DEFAULT_HARD_TIMEOUT = Uint16.ZERO;
    private static final Uint16 DEFAULT_PRIORITY = OFConstants.DEFAULT_FLOW_PRIORITY;
    private static final Uint32 DEFAULT_BUFFER_ID = OFConstants.OFP_NO_BUFFER;
    private static final Uint64 DEFAULT_OUT_PORT = Uint64.valueOf(OFConstants.OFPP_ANY);
    private static final Uint32 DEFAULT_OUT_GROUP = OFConstants.OFPG_ANY;
    private static final FlowModFlags DEFAULT_FLAGS = new FlowModFlags(false, false, false, false, false);
    private static final Uint16 PUSH_VLAN = Uint16.valueOf(0x8100);
    private static final Integer PUSH_TAG = PUSH_VLAN.toJava();
    private static final VlanCfi PUSH_CFI = new VlanCfi(1);

    private static final VlanMatch VLAN_MATCH_FALSE = new VlanMatchBuilder()
            .setVlanId(new VlanIdBuilder()
                    .setVlanIdPresent(false)
                    .setVlanId(new VlanId(Uint16.ZERO))
                    .build())
            .build();

    private static final VlanMatch VLAN_MATCH_TRUE = new VlanMatchBuilder()
            .setVlanId(new VlanIdBuilder()
                    .setVlanIdPresent(true)
                    .setVlanId(new VlanId(Uint16.ZERO))
                    .build())
            .build();

    private SerializerRegistry registry;

    @Override
    public void serialize(final FlowMessage message, final ByteBuf outBuffer) {
        if (!isVlanMatchPresent(message) && isSetVlanIdActionCasePresent(message)) {
            writeVlanFlow(message, outBuffer);
        } else {
            writeFlow(message, outBuffer);
        }
    }

    /**
     * Serialize flow message. Needs to be separated from main serialize method to prevent recursion
     * when serializing SetVlanId flows.
     *
     * @param message   flow message
     * @param outBuffer output buffer
     */
    private void writeFlow(final FlowMessage message, final ByteBuf outBuffer) {
        final int index = outBuffer.writerIndex();
        super.serialize(message, outBuffer);
        // FIXME: use Uint/ByteBuf utilities to skip toJava()/longValue()/intValue()
        outBuffer.writeLong(requireNonNullElse(message.getCookie(), DEFAULT_COOKIE).getValue().longValue());
        outBuffer.writeLong(requireNonNullElse(message.getCookieMask(), DEFAULT_COOKIE_MASK).getValue().longValue());
        outBuffer.writeByte(requireNonNullElse(message.getTableId(), DEFAULT_TABLE_ID).toJava());
        outBuffer.writeByte(message.getCommand().getIntValue());
        outBuffer.writeShort(requireNonNullElse(message.getIdleTimeout(), DEFAULT_IDLE_TIMEOUT).toJava());
        outBuffer.writeShort(requireNonNullElse(message.getHardTimeout(), DEFAULT_HARD_TIMEOUT).toJava());
        outBuffer.writeShort(requireNonNullElse(message.getPriority(), DEFAULT_PRIORITY).toJava());
        outBuffer.writeInt(requireNonNullElse(message.getBufferId(), DEFAULT_BUFFER_ID).intValue());
        outBuffer.writeInt(requireNonNullElse(message.getOutPort(), DEFAULT_OUT_PORT).intValue());
        outBuffer.writeInt(requireNonNullElse(message.getOutGroup(), DEFAULT_OUT_GROUP).intValue());
        outBuffer.writeShort(createFlowModFlagsBitmask(requireNonNullElse(message.getFlags(), DEFAULT_FLAGS)));
        outBuffer.writeShort(0);
        writeMatch(message, outBuffer);
        writeInstructions(message, outBuffer);
        outBuffer.setShort(index + 2, outBuffer.writerIndex() - index);
    }

    /**
     * Instead of serializing this flow normally, we need to split it to two parts if flow contains
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase}.
     *
     * @param message   flow mod message
     * @param outBuffer output buffer
     */
    private void writeVlanFlow(final FlowMessage message, final ByteBuf outBuffer) {
        writeFlow(new FlowMessageBuilder(message)
            .setMatch(new MatchBuilder(message.getMatch()).setVlanMatch(VLAN_MATCH_FALSE).build())
            .setInstructions(new InstructionsBuilder().setInstruction(updateSetVlanIdAction(message)).build())
            .build(), outBuffer);
        writeFlow(new FlowMessageBuilder(message)
            .setMatch(new MatchBuilder(message.getMatch()).setVlanMatch(VLAN_MATCH_TRUE).build())
            .build(), outBuffer);
    }

    /**
     * Serialize OpenFlowPlugin match to raw bytes.
     *
     * @param message   OpenFlow flow mod message
     * @param outBuffer output buffer
     */
    private void writeMatch(final FlowMessage message, final ByteBuf outBuffer) {
        requireNonNull(registry).<Match, OFSerializer<Match>>getSerializer(
                new MessageTypeKey<>(message.getVersion().toJava(), Match.class)).serialize(message.getMatch(),
                    outBuffer);
    }

    /**
     * Serialize OpenFlowPlugin instructions and set ip protocol of set-tp-src and set-tp-dst actions of need.
     *
     * @param message   OpenFlow flow mod message
     * @param outBuffer output buffer
     */
    private void writeInstructions(final FlowMessage message, final ByteBuf outBuffer) {
        final var instructions = message.getInstructions();
        if (instructions == null) {
            // Nothing to do
            return;
        }

        // Extract all instructions ...
        Stream<Instruction> flowInstructions = instructions.nonnullInstruction().values().stream()
            .filter(Objects::nonNull)
            .sorted(OrderComparator.build())
            .map(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction::getInstruction)
            .filter(Objects::nonNull);

        // ... updated them if needed ...
        final Uint8 protocol = extractProtocol(message);
        if (protocol != null) {
            flowInstructions = flowInstructions.map(insn -> updateInstruction(insn, protocol));
        }

        // ... and serialize them
        flowInstructions.forEach(i -> InstructionUtil.writeInstruction(i, EncodeConstants.OF13_VERSION_ID, registry,
            outBuffer));
    }

    // Try to get IP protocol from IP match
    private static @Nullable Uint8 extractProtocol(final FlowMessage message) {
        final var match = message.getMatch();
        if (match != null) {
            final var ipMatch = match.getIpMatch();
            if (ipMatch != null) {
                return ipMatch.getIpProtocol();
            }
        }
        return null;
    }

    /**
     * Determine type of instruction and update it's actions if it is apply-actions instruction.
     *
     * @param instruction instruction
     * @param protocol    protocol
     * @return updated or original instruction
     */
    private static @Nullable Instruction updateInstruction(final Instruction instruction, final Uint8 protocol) {
        if (instruction instanceof ApplyActionsCase) {
            final var actions = ((ApplyActionsCase) instruction).getApplyActions();
            if (actions != null) {
                return new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(actions.nonnullAction().values().stream()
                            .filter(Objects::nonNull)
                            .map(a -> updateSetTpActions(a, protocol))
                            .collect(BindingMap.<ActionKey, Action>toOrderedMap()))
                        .build())
                    .build();
            }
        }
        return instruction;
    }

    /**
     * If action is set-tp-src or set-tp-dst, inject IP protocol into it, otherwise return original action.
     *
     * @param action   OpenFlow action
     * @param protocol IP protocol
     * @return updated OpenFlow action
     */
    private static Action updateSetTpActions(final Action action, final Uint8 protocol) {
        if (action.getAction() instanceof SetTpSrcActionCase) {
            final SetTpSrcActionCase actionCase = (SetTpSrcActionCase) action.getAction();

            return new ActionBuilder(action)
                    .setAction(new SetTpSrcActionCaseBuilder(actionCase)
                            .setSetTpSrcAction(new SetTpSrcActionBuilder(
                                    actionCase.getSetTpSrcAction())
                                    .setIpProtocol(protocol)
                                    .build())
                            .build())
                    .build();
        } else if (action.getAction() instanceof SetTpDstActionCase) {
            final SetTpDstActionCase actionCase = (SetTpDstActionCase) action.getAction();

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
     * Create copy of instructions of original flow but insert
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase}
     * before each
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase}.
     *
     * @param message OpenFlowPlugin flow mod message
     * @return list of instructions
     */
    private static Map<
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey,
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
                updateSetVlanIdAction(final FlowMessage message) {
        return message.getInstructions().nonnullInstruction().values()
                .stream()
                .map(FlowMessageSerializer::updateSetVlanIdAction)
                .collect(BindingMap.<
                    org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey,
                    org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
                        toOrderedMap());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction
            updateSetVlanIdAction(final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction
                .list.Instruction insn) {
        final Instruction instruction = insn.getInstruction();
        if (instruction instanceof ApplyActionsCase) {
            final ApplyActions actions = ((ApplyActionsCase) instruction).getApplyActions();
            if (actions != null) {
                final int[] offset = {0};

                return Optional.of(actions)
                    .flatMap(as -> Optional.ofNullable(as.nonnullAction()))
                    .map(a -> a.values().stream()
                        .sorted(OrderComparator.build())
                        .flatMap(action -> {
                            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112
                            .action.list.Action> actionList = new ArrayList<>();

                            // If current action is SetVlanId, insert PushVlan action before it and
                            // update order
                            if (action.getAction() instanceof SetVlanIdActionCase) {
                                actionList.add(new ActionBuilder()
                                    .setAction(new PushVlanActionCaseBuilder()
                                        .setPushVlanAction(new PushVlanActionBuilder()
                                            .setCfi(PUSH_CFI)
                                            .setVlanId(((SetVlanIdActionCase) action
                                                .getAction()).getSetVlanIdAction()
                                                .getVlanId())
                                            .setEthernetType(PUSH_VLAN)
                                            .setTag(PUSH_TAG)
                                            .build())
                                        .build())
                                    .withKey(action.key())
                                    .setOrder(action.getOrder() + offset[0])
                                    .build());

                                offset[0]++;
                            }

                            // Update offset of action if there is any inserted PushVlan actions
                            actionList.add(offset[0] > 0
                                ? new ActionBuilder(action).setOrder(action.getOrder() + offset[0])
                                    .withKey(new ActionKey(action.getOrder() + offset[0]))
                                    .build()
                                    : action);

                            return actionList.stream();
                        }))
                    .map(as -> new InstructionBuilder(insn)
                        .setInstruction(new ApplyActionsCaseBuilder()
                            .setApplyActions(new ApplyActionsBuilder()
                                .setAction(as.collect(BindingMap.<ActionKey, Action>toOrderedMap()))
                                .build())
                            .build())
                        .build())
                    .orElse(insn);

            }
        }
        return insn;
    }

    /**
     * Create integer bit mask from
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags}.
     *
     * @param flags flow mod flags
     * @return bit mask
     */
    private static int createFlowModFlagsBitmask(final FlowModFlags flags) {
        return ByteBufUtils.fillBitMask(0,
                flags.getSENDFLOWREM(),
                flags.getCHECKOVERLAP(),
                flags.getRESETCOUNTS(),
                flags.getNOPKTCOUNTS(),
                flags.getNOBYTCOUNTS());
    }

    /**
     * Determine if flow contains vlan match.
     *
     * @param flow flow
     * @return true if flow contains vlan match
     */
    private static boolean isVlanMatchPresent(final Flow flow) {
        final var match = flow.getMatch();
        return match != null && match.getVlanMatch() != null;
    }

    /**
     * Determine if flow contains
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight
     * .flow.types.rev131026.instruction.instruction.ApplyActionsCase} instruction with
     * #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase}
     * action.
     *
     * @param flow OpenFlowPlugin flow
     * @return true if flow contains SetVlanIdAction
     */
    private static boolean isSetVlanIdActionCasePresent(final Flow flow) {
        final var instructions = flow.getInstructions();
        return instructions != null && instructions.nonnullInstruction().values().stream()
            .map(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction::getInstruction)
            .filter(ApplyActionsCase.class::isInstance)
            .map(i -> ((ApplyActionsCase) i).getApplyActions())
            .filter(Objects::nonNull)
            .flatMap(actionList -> actionList.nonnullAction().values().stream())
            .map(Action::getAction)
            .anyMatch(SetVlanIdActionCase.class::isInstance);
    }

    @Override
    protected byte getMessageType() {
        return 14;
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
