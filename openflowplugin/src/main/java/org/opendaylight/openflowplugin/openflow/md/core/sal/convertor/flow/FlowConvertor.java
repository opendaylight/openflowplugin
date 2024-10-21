/*
 * Copyright (c) 2013, 2015 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases.ApplyActionsCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases.ClearActionsCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases.GoToTableCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases.MeterCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases.WriteActionsCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases.WriteMetadataCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchInjector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.VlanCfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MatchTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Converts the SAL Flow to OF Flow. It checks if there is a set-vlan-id (1.0) action made on OF1.3.
 * If yes its handled separately.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(version);
 * data.setDatapathId(datapathId);
 * Optional<List<FlowModInputBuilder>> ofFlow = convertorManager.convert(salFlow, data);
 * }
 * </pre>
 */
public class FlowConvertor extends Convertor<Flow, List<FlowModInputBuilder>, VersionDatapathIdConvertorData> {
    /**
     * Default idle timeout.
     */
    public static final Uint16 DEFAULT_IDLE_TIMEOUT = Uint16.ZERO;

    /**
     * Default hard timeout.
     */
    public static final Uint16 DEFAULT_HARD_TIMEOUT = Uint16.ZERO;

    /**
     * Default priority.
     */
    public static final Uint16 DEFAULT_PRIORITY = Uint16.valueOf(0x8000);

    /**
     * flow flag: remove.
     */
    public static final boolean DEFAULT_OFPFF_FLOW_REM = false;

    /**
     * flow flag: check overlap.
     */
    public static final boolean DEFAULT_OFPFF_CHECK_OVERLAP = false;

    /**
     * flow flag: reset counts.
     */
    public static final boolean DEFAULT_OFPFF_RESET_COUNTS = false;

    /**
     * flow flag: don't keep track of packet counts.
     */
    public static final boolean DEFAULT_OFPFF_NO_PKT_COUNTS = false;

    /**
     * flow flag: don't keep track of byte counts.
     */
    public static final boolean DEFAULT_OFPFF_NO_BYT_COUNTS = false;

    /**
     * flow flag: emergency [OFP-1.0].
     */
    public static final boolean DEFAULT_OFPFF_EMERGENCY = false;

    /**
     * OxmMatch type.
     */
    public static final MatchTypeBase DEFAULT_MATCH_TYPE = OxmMatchType.VALUE;

    /**
     * default match entries - empty.
     */
    public static final List<MatchEntry> DEFAULT_MATCH_ENTRIES = ImmutableList.of();

    // Default values for when things are null
    private static final TableId DEFAULT_TABLE_ID = new TableId(Uint32.ZERO);
    private static final Uint32 DEFAULT_BUFFER_ID = OFConstants.OFP_NO_BUFFER;
    private static final Uint32 OFPP_ANY = Uint32.MAX_VALUE;
    private static final PortNumber DEFAULT_OUT_PORT = new PortNumber(OFPP_ANY);
    private static final Uint32 OFPG_ANY = Uint32.MAX_VALUE;
    private static final Uint32 DEFAULT_OUT_GROUP = OFPG_ANY;
    private static final Uint16 PUSH_VLAN = Uint16.valueOf(0x8100);
    private static final Integer PUSH_TAG = PUSH_VLAN.toJava();
    private static final VlanCfi PUSH_CFI = new VlanCfi(1);
    private static final Ordering<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction
        .list.Instruction> INSTRUCTION_ORDERING = Ordering.from(OrderComparator.build());
    private static final VlanMatch VLAN_MATCH_FALSE;
    private static final VlanMatch VLAN_MATCH_TRUE;
    private static final ConvertorProcessor<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
        .instruction.Instruction, Instruction, ActionConvertorData> PROCESSOR =
            new ConvertorProcessor<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                .instruction.Instruction, Instruction, ActionConvertorData>()
            .addCase(new ApplyActionsCase())
            .addCase(new ClearActionsCase())
            .addCase(new GoToTableCase())
            .addCase(new MeterCase())
            .addCase(new WriteActionsCase())
            .addCase(new WriteMetadataCase());

    private static final List<Class<?>> TYPES = Arrays.asList(Flow.class, AddFlowInput.class,
            RemoveFlowInput.class, UpdatedFlow.class);

    static {
        final VlanId zeroVlan = new VlanId(Uint16.ZERO);
        VLAN_MATCH_FALSE = new VlanMatchBuilder()
            .setVlanId(new VlanIdBuilder().setVlanIdPresent(false).setVlanId(zeroVlan).build())
            .build();
        VLAN_MATCH_TRUE = new VlanMatchBuilder()
            .setVlanId(new VlanIdBuilder().setVlanIdPresent(true).setVlanId(zeroVlan).build())
            .build();
    }

    private FlowModInputBuilder toFlowModInput(final Flow flow,
            final VersionDatapathIdConvertorData versionConverterData) {

        FlowModInputBuilder flowMod = new FlowModInputBuilder()
            .setIdleTimeout(requireNonNullElse(flow.getIdleTimeout(), DEFAULT_IDLE_TIMEOUT))
            .setHardTimeout(requireNonNullElse(flow.getHardTimeout(), DEFAULT_HARD_TIMEOUT))
            .setPriority(requireNonNullElse(flow.getPriority(), DEFAULT_PRIORITY))
            .setBufferId(requireNonNullElse(flow.getBufferId(), DEFAULT_BUFFER_ID))
            .setOutGroup(requireNonNullElse(flow.getOutGroup(), DEFAULT_OUT_GROUP));
        salToOFFlowCookie(flow, flowMod);
        salToOFFlowCookieMask(flow, flowMod);
        salToOFFlowTableId(flow, flowMod);
        salToOFFlowCommand(flow, flowMod);
        salToOFFlowOutPort(flow, flowMod);

        // convert and inject flowFlags
        final Optional<Object> conversion = getConvertorExecutor().convert(flow.getFlags(), versionConverterData);
        FlowFlagsInjector.inject(conversion, flowMod, versionConverterData.getVersion());

        // convert and inject match
        final Optional<Object> conversionMatch = getConvertorExecutor().convert(flow.getMatch(), versionConverterData);
        MatchInjector.inject(conversionMatch, flowMod, versionConverterData.getVersion());

        if (flow.getInstructions() != null) {
            flowMod
                .setInstruction(toInstructions(flow, versionConverterData.getVersion(),
                    versionConverterData.getDatapathId()))
                .setAction(getActions(versionConverterData.getVersion(), versionConverterData.getDatapathId(), flow));
        }

        return flowMod
            .setVersion(versionConverterData.getVersion());
    }

    private static void salToOFFlowOutPort(final Flow flow, final FlowModInputBuilder flowMod) {
        final var outPort = flow.getOutPort();
        flowMod.setOutPort(outPort != null ? new PortNumber(outPort.toUint32()) : DEFAULT_OUT_PORT);
    }

    private static void salToOFFlowCommand(final Flow flow, final FlowModInputBuilder flowMod) {
        if (flow instanceof AddFlowInput || flow instanceof UpdatedFlow) {
            flowMod.setCommand(FlowModCommand.OFPFCADD);
        } else if (flow instanceof RemoveFlowInput) {
            flowMod.setCommand(Boolean.TRUE.equals(flow.getStrict())
                ? FlowModCommand.OFPFCDELETESTRICT : FlowModCommand.OFPFCDELETE);
        }
    }

    private static void salToOFFlowTableId(final Flow flow, final FlowModInputBuilder flowMod) {
        final var tableId = flow.getTableId();
        flowMod.setTableId(tableId != null ? new TableId(flow.getTableId().toUint32()) : DEFAULT_TABLE_ID);
    }

    private static void salToOFFlowCookieMask(final Flow flow, final FlowModInputBuilder flowMod) {
        final var mask = flow.getCookieMask();
        flowMod.setCookieMask(mask != null ? mask.getValue() : OFConstants.DEFAULT_COOKIE_MASK);
    }

    private static void salToOFFlowCookie(final Flow flow, final FlowModInputBuilder flowMod) {
        final var omNomNom = flow.getCookie();
        flowMod.setCookie(omNomNom != null ? omNomNom.getValue() : OFConstants.DEFAULT_COOKIE);
    }

    private List<Instruction> toInstructions(final Flow flow, final Uint8 version, final Uint64 datapathid) {
        final List<Instruction> instructionsList = new ArrayList<>();
        final ActionConvertorData data = new ActionConvertorData(version);
        data.setDatapathId(datapathid);
        data.setIpProtocol(FlowConvertorUtil.getIpProtocolFromFlow(flow));

        final ConvertorExecutor convertor = getConvertorExecutor();
        for (var instruction : flow.getInstructions().nonnullInstruction().values()) {
            Optional<Instruction> result = PROCESSOR.process(instruction.getInstruction(), data, convertor);
            result.ifPresent(instructionsList::add);
        }

        return instructionsList;
    }

    private List<Action> getActions(final Uint8 version, final Uint64 datapathid, final Flow flow) {
        Instructions instructions = flow.getInstructions();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
            sortedInstructions = INSTRUCTION_ORDERING.sortedCopy(instructions.nonnullInstruction().values());

        for (var instruction : sortedInstructions) {
            final var curInstruction = instruction.getInstruction();
            if (curInstruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                    .instruction.instruction.ApplyActionsCase) {
                final var applyActions = ((org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                    .instruction.instruction.ApplyActionsCase) curInstruction).getApplyActions();

                final ActionConvertorData data = new ActionConvertorData(version);
                data.setDatapathId(datapathid);
                data.setIpProtocol(FlowConvertorUtil.getIpProtocolFromFlow(flow));
                Optional<List<Action>> result = getConvertorExecutor().convert(applyActions.getAction(), data);
                return result.orElse(List.of());
            }
        }

        return null;
    }

    // check if set vlanid action is present in the flow
    private static boolean isSetVlanIdActionCasePresent(final Flow flow) {
        // we are trying to find if there is a set-vlan-id action (OF1.0) action present in the flow.
        // If yes,then we would need to two flows
        final var insns = flow.getInstructions();
        if (insns != null) {
            for (var instruction : insns.nonnullInstruction().values()) {
                final var curInstruction = instruction.getInstruction();

                if (curInstruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                        .instruction.instruction.ApplyActionsCase) {
                    ApplyActions applyActions = ((org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types
                        .rev131026.instruction.instruction.ApplyActionsCase) curInstruction).getApplyActions();
                    for (var action : applyActions.nonnullAction().values()) {
                        if (action.getAction() instanceof SetVlanIdActionCase) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * A) If user provided flow's match includes vlan match  and action has set_vlan_field
     * Install following rules.
     *    1) match on (OFPVID_PRESENT |value) without mask + action [set_field]
     * <p/>
     * B) if user provided flow's match doesn't include vlan match but action has set_vlan field
     *     1) Match on (OFPVID_NONE ) without mask + action [push vlan tag + set_field]
     *     2) Match on (OFPVID_PRESENT) with mask (OFPVID_PRESENT ) + action [ set_field]
     */
    private List<FlowModInputBuilder> handleSetVlanIdForOF13(final Flow srcFlow,
            final VersionDatapathIdConvertorData versionDatapathIdConverterData) {
        List<FlowModInputBuilder> list = new ArrayList<>(2);

        final Match srcMatch = requireNonNull(srcFlow.getMatch());
        final VlanMatch srcVlanMatch = srcMatch.getVlanMatch();
        if (srcVlanMatch != null) {
            //create flow with setfield and match
            // match on vlan tag or vlanid with no mask
            Optional<? extends Flow> optional = injectMatchToFlow(srcFlow, new MatchBuilder(srcMatch)
                .setVlanMatch(new VlanMatchBuilder(srcVlanMatch)
                    .setVlanId(new VlanIdBuilder()
                        .setVlanIdPresent(srcVlanMatch.getVlanId().getVlanIdPresent())
                        .setVlanId(srcVlanMatch.getVlanId().getVlanId())
                        .build())
                    .build())
                .build());
            if (optional.isPresent()) {
                list.add(toFlowModInput(optional.orElseThrow(), versionDatapathIdConverterData));
            }
        } else {
            // create 2 flows
            //flow 1
            // match on no vlan tag with no mask
            Optional<? extends Flow> optional1 = injectMatchAndAction(srcFlow,
                new MatchBuilder(srcMatch).setVlanMatch(VLAN_MATCH_FALSE).build());
            if (optional1.isPresent()) {
                list.add(toFlowModInput(optional1.orElseThrow(), versionDatapathIdConverterData));
            }

            //flow2
            // match on vlan tag with mask
            Optional<? extends Flow> optional2 = injectMatchToFlow(srcFlow,
                new MatchBuilder(srcMatch).setVlanMatch(VLAN_MATCH_TRUE).build());
            if (optional2.isPresent()) {
                list.add(toFlowModInput(optional2.orElseThrow(), versionDatapathIdConverterData));
            }
        }
        return list;
    }


    private static Optional<? extends Flow> injectMatchToFlow(final Flow sourceFlow, final Match match) {
        if (sourceFlow instanceof AddFlowInput) {
            return Optional.of(new AddFlowInputBuilder(sourceFlow).setMatch(match).build());
        } else if (sourceFlow instanceof RemoveFlowInput) {
            return Optional.of(new RemoveFlowInputBuilder(sourceFlow).setMatch(match).build());
        } else if (sourceFlow instanceof UpdatedFlow) {
            return Optional.of(new UpdatedFlowBuilder(sourceFlow).setMatch(match).build());
        } else {
            return Optional.empty();
        }
    }

    private static Optional<? extends Flow> injectMatchAndAction(final Flow sourceFlow, final Match match) {

        Instructions instructions = new InstructionsBuilder()
                .setInstruction(injectPushActionToInstruction(sourceFlow))
                .build();

        if (sourceFlow instanceof AddFlowInput) {
            return Optional.of(new AddFlowInputBuilder(sourceFlow)
                    .setMatch(match).setInstructions(instructions).build());
        } else if (sourceFlow instanceof RemoveFlowInput) {
            return Optional.of(new RemoveFlowInputBuilder(sourceFlow)
                    .setMatch(match).setInstructions(instructions).build());
        } else if (sourceFlow instanceof UpdatedFlow) {
            return Optional.of(new UpdatedFlowBuilder(sourceFlow)
                    .setMatch(match).setInstructions(instructions).build());
        } else {
            return Optional.empty();
        }
    }

    private static @NonNull Map<InstructionKey,
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
                injectPushActionToInstruction(final Flow sourceFlow) {
        final var srcInstructionList = sourceFlow.getInstructions().nonnullInstruction();
        final var targetInstructionList = BindingMap.<
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey,
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
                orderedBuilder(srcInstructionList.size());
        final var targetActionList = BindingMap.<
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey,
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>orderedBuilder();
        final var instructionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
            .instruction.list.InstructionBuilder();

        for (var srcInstruction : srcInstructionList.values()) {
            final var curSrcInstruction = srcInstruction.getInstruction();

            if (curSrcInstruction
                    instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026
                        .instruction.instruction.ApplyActionsCase applyActionscase) {
                final var srcActionList = applyActionscase.getApplyActions().nonnullAction();

                int offset = 0;
                for (var actionItem : srcActionList.values()) {
                    // check if its a set-vlan-action. If yes, then add the injected-action

                    if (actionItem.getAction() instanceof SetVlanIdActionCase) {
                        final var setVlanIdActionCase = (SetVlanIdActionCase) actionItem.getAction();

                        targetActionList.add(new ActionBuilder()
                            .setAction(new PushVlanActionCaseBuilder()
                                .setPushVlanAction(new PushVlanActionBuilder()
                                    .setCfi(PUSH_CFI)
                                    .setVlanId(setVlanIdActionCase.getSetVlanIdAction().getVlanId())
                                    .setEthernetType(PUSH_VLAN)
                                    .setTag(PUSH_TAG)
                                    .build())
                                .build())
                            // FIXME: this looks like a mismatch
                            .withKey(actionItem.key())
                            .setOrder(actionItem.getOrder() + offset)
                            .build());
                        offset++;
                    }

                    if (offset > 0) {
                        // we need to increment the order for all the actions added after injection
                        final Integer newOrder = actionItem.getOrder() + offset;
                        actionItem = new ActionBuilder(actionItem)
                            .setOrder(newOrder)
                            .withKey(new ActionKey(newOrder))
                            .build();
                    }

                    targetActionList.add(actionItem);
                }

                instructionBuilder.setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder().setAction(targetActionList.build()).build())
                    .build());
            } else {
                instructionBuilder.setInstruction(curSrcInstruction);
            }

            targetInstructionList.add(instructionBuilder
                .withKey(srcInstruction.key())
                .setOrder(srcInstruction.getOrder())
                .build());
        }

        return targetInstructionList.build();
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return  TYPES;
    }

    @Override
    public List<FlowModInputBuilder> convert(final Flow source, final VersionDatapathIdConvertorData data) {
        if (OFConstants.OFP_VERSION_1_3.compareTo(data.getVersion()) <= 0 && isSetVlanIdActionCasePresent(source)) {
            return handleSetVlanIdForOF13(source, data);
        } else {
            return Collections.singletonList(toFlowModInput(source, data));
        }
    }
}
