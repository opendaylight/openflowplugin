/*
 * Copyright (c) 2013, 2015 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.OFConstants;
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
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.flowflag.FlowFlagReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.VlanCfi;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.push.vlan.action._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
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
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts the SAL Flow to OF Flow. It checks if there is a set-vlan-id (1.0) action made on OF1.3.
 * If yes its handled separately.
 *
 * Example usage:
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
     * Default idle timeout
     */
    public static final Integer DEFAULT_IDLE_TIMEOUT = 0;
    /**
     * Default hard timeout
     */
    public static final Integer DEFAULT_HARD_TIMEOUT = 0;
    /**
     * Default priority
     */
    public static final Integer DEFAULT_PRIORITY = Integer.parseInt("8000", 16);
    /**
     * flow flag: remove
     */
    public static final boolean DEFAULT_OFPFF_FLOW_REM = false;
    /**
     * flow flag: check overlap
     */
    public static final boolean DEFAULT_OFPFF_CHECK_OVERLAP = false;
    /**
     * flow flag: reset counts
     */
    public static final boolean DEFAULT_OFPFF_RESET_COUNTS = false;
    /**
     * flow flag: don't keep track of packet counts
     */
    public static final boolean DEFAULT_OFPFF_NO_PKT_COUNTS = false;
    /**
     * flow flag: don't keep track of byte counts
     */
    public static final boolean DEFAULT_OFPFF_NO_BYT_COUNTS = false;
    /**
     * flow flag: emergency [OFP-1.0]
     */
    public static final boolean DEFAULT_OFPFF_EMERGENCY = false;
    /**
     * OxmMatch type
     */
    public static final Class<? extends MatchTypeBase> DEFAULT_MATCH_TYPE = OxmMatchType.class;
    /**
     * default match entries - empty
     */
    public static final List<MatchEntry> DEFAULT_MATCH_ENTRIES = new ArrayList<>();

    // Default values for when things are null
    private static final TableId DEFAULT_TABLE_ID = new TableId(0L);
    private static final Long DEFAULT_BUFFER_ID = OFConstants.OFP_NO_BUFFER;
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_PORT = OFPP_ANY;
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_GROUP = OFPG_ANY;
    private static final Integer PUSH_VLAN = 0x8100;
    private static final Ordering<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> INSTRUCTION_ORDERING = Ordering.from(OrderComparator.build());
    private static final VlanMatch VLAN_MATCH_FALSE;
    private static final VlanMatch VLAN_MATCH_TRUE;
    private static final ConvertorProcessor<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction, Instruction, ActionConvertorData> PROCESSOR = new ConvertorProcessor<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction, Instruction, ActionConvertorData>()
            .addCase(new ApplyActionsCase())
            .addCase(new ClearActionsCase())
            .addCase(new GoToTableCase())
            .addCase(new MeterCase())
            .addCase(new WriteActionsCase())
            .addCase(new WriteMetadataCase());
    private static final List<Class<? extends DataContainer>> TYPES = Arrays.asList(Flow.class, AddFlowInput.class, RemoveFlowInput.class, UpdatedFlow.class);

    static {
        final VlanId zeroVlan = new VlanId(0);
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanIdPresent(false);
        vlanIdBuilder.setVlanId(zeroVlan);
        vlanMatchBuilder.setVlanId(vlanIdBuilder.build());

        VLAN_MATCH_FALSE = vlanMatchBuilder.build();

        VlanMatchBuilder vlanMatchBuilder2 = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder2 = new VlanIdBuilder();
        vlanIdBuilder2.setVlanIdPresent(true);
        vlanIdBuilder2.setVlanId(zeroVlan);
        vlanMatchBuilder2.setVlanId(vlanIdBuilder2.build());

        VLAN_MATCH_TRUE = vlanMatchBuilder2.build();
    }

    private FlowModInputBuilder toFlowModInput(Flow flow, short version, BigInteger datapathid) {

        FlowModInputBuilder flowMod = new FlowModInputBuilder();
        salToOFFlowCookie(flow, flowMod);
        salToOFFlowCookieMask(flow, flowMod);
        salToOFFlowTableId(flow, flowMod);
        salToOFFlowCommand(flow, flowMod);
        salToOFFlowIdleTimeout(flow, flowMod);
        salToOFFlowHardTimeout(flow, flowMod);
        salToOFFlowPriority(flow, flowMod);
        salToOFFlowBufferId(flow, flowMod);
        salToOFFlowOutPort(flow, flowMod);
        salToOFFlowOutGroup(flow, flowMod);

        // convert and inject flowFlags
        FlowFlagReactor.getInstance().convert(flow.getFlags(), version, flowMod, getConvertorExecutor());

        // convert and inject match
        MatchReactor.getInstance().convert(flow.getMatch(), version, flowMod, getConvertorExecutor());

        if (flow.getInstructions() != null) {
            flowMod.setInstruction(toInstructions(flow, version, datapathid));
            flowMod.setAction(getActions(version, datapathid, flow));
        }

        flowMod.setVersion(version);

        return flowMod;
    }

    private static void salToOFFlowOutGroup(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getOutGroup() != null) {
            flowMod.setOutGroup(flow.getOutGroup());
        } else {
            flowMod.setOutGroup(DEFAULT_OUT_GROUP);
        }
    }

    private static void salToOFFlowOutPort(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getOutPort() != null) {
            flowMod.setOutPort(new PortNumber(flow.getOutPort().longValue()));
        } else {
            flowMod.setOutPort(new PortNumber(DEFAULT_OUT_PORT));
        }
    }

    private static void salToOFFlowBufferId(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getBufferId() != null) {
            flowMod.setBufferId(flow.getBufferId());
        } else {
            flowMod.setBufferId(DEFAULT_BUFFER_ID);
        }
    }

    private static void salToOFFlowPriority(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getPriority() != null) {
            flowMod.setPriority(flow.getPriority());
        } else {
            flowMod.setPriority(DEFAULT_PRIORITY);
        }
    }

    private static void salToOFFlowHardTimeout(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getHardTimeout() != null) {
            flowMod.setHardTimeout(flow.getHardTimeout());
        } else {
            flowMod.setHardTimeout(DEFAULT_HARD_TIMEOUT);
        }
    }

    private static void salToOFFlowIdleTimeout(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getIdleTimeout() != null) {
            flowMod.setIdleTimeout(flow.getIdleTimeout());
        } else {
            flowMod.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        }
    }

    private static void salToOFFlowCommand(Flow flow, FlowModInputBuilder flowMod) {
        if (flow instanceof AddFlowInput || flow instanceof UpdatedFlow) {
            flowMod.setCommand(FlowModCommand.OFPFCADD);
        } else if (flow instanceof RemoveFlowInput) {
            if (MoreObjects.firstNonNull(flow.isStrict(), Boolean.FALSE)) {
                flowMod.setCommand(FlowModCommand.OFPFCDELETESTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCDELETE);
            }
        }
    }

    private static void salToOFFlowTableId(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getTableId() != null) {
            flowMod.setTableId(new TableId(flow.getTableId().longValue()));
        } else {
            flowMod.setTableId(DEFAULT_TABLE_ID);
        }
    }

    private static void salToOFFlowCookieMask(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getCookieMask() != null) {
            flowMod.setCookieMask(flow.getCookieMask().getValue());
        } else {
            flowMod.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        }
    }

    private static void salToOFFlowCookie(Flow flow, FlowModInputBuilder flowMod) {
        if (flow.getCookie() != null) {
            flowMod.setCookie(flow.getCookie().getValue());
        } else {
            flowMod.setCookie(OFConstants.DEFAULT_COOKIE);
        }
    }

    private List<Instruction> toInstructions(Flow flow, short version, BigInteger datapathid) {
        final List<Instruction> instructionsList = new ArrayList<>();
        final ActionConvertorData data = new ActionConvertorData(version);
        data.setDatapathId(datapathid);
        data.setIpProtocol(FlowConvertorUtil.getIpProtocolFromFlow(flow));

        Instructions instructions = flow.getInstructions();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : instructions
                .getInstruction()) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();

            Optional<Instruction> result = PROCESSOR.process(curInstruction, data, getConvertorExecutor());

            if (result.isPresent()) {
                instructionsList.add(result.get());
            }
        }

        return instructionsList;
    }

    private List<Action> getActions(short version, BigInteger datapathid, Flow flow) {
        Instructions instructions = flow.getInstructions();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> sortedInstructions =
                INSTRUCTION_ORDERING.sortedCopy(instructions.getInstruction());

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : sortedInstructions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();

            if (curInstruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase applyActionscase = (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase) curInstruction;
                ApplyActions applyActions = applyActionscase.getApplyActions();

                final ActionConvertorData data = new ActionConvertorData(version);
                data.setDatapathId(datapathid);
                data.setIpProtocol(FlowConvertorUtil.getIpProtocolFromFlow(flow));
                Optional<List<Action>> result = getConvertorExecutor().convert(applyActions.getAction(), data);
                return result.orElse(Collections.emptyList());
            }
        }

        return null;
    }

    // check if set vlanid action is present in the flow
    private static boolean isSetVlanIdActionCasePresent(Flow flow) {
        // we are trying to find if there is a set-vlan-id action (OF1.0) action present in the flow.
        // If yes,then we would need to two flows
        if (flow.getInstructions() != null && flow.getInstructions().getInstruction() != null) {
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction :
                    flow.getInstructions().getInstruction()) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction =
                        instruction.getInstruction();

                if (curInstruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase) {
                    org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase applyActionscase = (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase) curInstruction;
                    ApplyActions applyActions = applyActionscase.getApplyActions();
                    for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action action :
                            applyActions.getAction()) {
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
     * Install following rules
     *    1) match on (OFPVID_PRESENT |value) without mask + action [set_field]
     * <p/>
     * B) if user provided flow's match doesn't include vlan match but action has set_vlan field
     *     1) Match on (OFPVID_NONE ) without mask + action [push vlan tag + set_field]
     *     2) Match on (OFPVID_PRESENT) with mask (OFPVID_PRESENT ) + action [ set_field]
     */
    private List<FlowModInputBuilder> handleSetVlanIdForOF13(Flow srcFlow, short version, BigInteger datapathId) {
        List<FlowModInputBuilder> list = new ArrayList<>(2);

        final Match srcMatch = Preconditions.checkNotNull(srcFlow.getMatch());
        final VlanMatch srcVlanMatch = srcMatch.getVlanMatch();
        if (srcVlanMatch != null) {
            //create flow with setfield and match
            // match on vlan tag or vlanid with no mask
            VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder(srcVlanMatch);
            VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
            vlanIdBuilder.setVlanIdPresent(srcVlanMatch.getVlanId().isVlanIdPresent());
            vlanIdBuilder.setVlanId(srcVlanMatch.getVlanId().getVlanId());
            vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
            Match match = new MatchBuilder(srcMatch).setVlanMatch(vlanMatchBuilder.build()).build();

            Optional<? extends Flow> optional = injectMatchToFlow(srcFlow, match);
            if (optional.isPresent()) {
                list.add(toFlowModInput(optional.get(), version, datapathId));
            }
        } else {
            // create 2 flows
            //flow 1
            // match on no vlan tag with no mask
            Match match1 = new MatchBuilder(srcMatch).setVlanMatch(VLAN_MATCH_FALSE).build();

            Optional<? extends Flow> optional1 = injectMatchAndAction(srcFlow, match1);
            if (optional1.isPresent()) {
                list.add(toFlowModInput(optional1.get(), version, datapathId));
            }

            //flow2
            // match on vlan tag with mask
            Match match2 = new MatchBuilder(srcMatch).setVlanMatch(VLAN_MATCH_TRUE).build();
            Optional<? extends Flow> optional2 = injectMatchToFlow(srcFlow, match2);
            if (optional2.isPresent()) {
                list.add(toFlowModInput(optional2.get(), version, datapathId));
            }
        }
        return list;
    }


    private static Optional<? extends Flow> injectMatchToFlow(Flow sourceFlow, Match match) {
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

    private static Optional<? extends Flow> injectMatchAndAction(Flow sourceFlow, Match match) {

        Instructions instructions = (new InstructionsBuilder())
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

    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
    injectPushActionToInstruction(final Flow sourceFlow) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> srcInstructionList =
                sourceFlow.getInstructions().getInstruction();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> targetInstructionList = new ArrayList<>(srcInstructionList.size());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> targetActionList = new ArrayList<>();

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder instructionBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction srcInstruction : srcInstructionList) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curSrcInstruction =
                    srcInstruction.getInstruction();

            if (curSrcInstruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase applyActionscase = (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase) curSrcInstruction;
                ApplyActions applyActions = applyActionscase.getApplyActions();
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> srcActionList = applyActions.getAction();

                int offset = 0;
                for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action actionItem : srcActionList) {
                    // check if its a set-vlan-action. If yes, then add the injected-action

                    if (actionItem.getAction() instanceof SetVlanIdActionCase) {
                        SetVlanIdActionCase setVlanIdActionCase = (SetVlanIdActionCase) actionItem.getAction();

                        PushVlanActionCaseBuilder pushVlanActionCaseBuilder = new PushVlanActionCaseBuilder();
                        PushVlanActionBuilder pushVlanActionBuilder = new PushVlanActionBuilder();

                        pushVlanActionBuilder.setCfi(new VlanCfi(1))
                                .setVlanId(setVlanIdActionCase.getSetVlanIdAction().getVlanId())
                                .setEthernetType(PUSH_VLAN)
                                .setTag(PUSH_VLAN);
                        pushVlanActionCaseBuilder.setPushVlanAction(pushVlanActionBuilder.build());
                        PushVlanActionCase injectedAction = pushVlanActionCaseBuilder.build();

                        ActionBuilder actionBuilder = new ActionBuilder();
                        actionBuilder.setAction(injectedAction)
                                .setKey(actionItem.getKey())
                                .setOrder(actionItem.getOrder() + offset);

                        targetActionList.add(actionBuilder.build());
                        offset++;
                    }

                    if (offset > 0) {
                        // we need to increment the order for all the actions added after injection
                        ActionBuilder actionBuilder =
                                new ActionBuilder(actionItem);
                        actionBuilder.setOrder(actionItem.getOrder() + offset);
                        actionItem = actionBuilder.build();
                    }

                    targetActionList.add(actionItem);
                }

                ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
                ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
                applyActionsBuilder.setAction(targetActionList);
                applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());

                instructionBuilder.setInstruction(applyActionsCaseBuilder.build());
            } else {
                instructionBuilder.setInstruction(curSrcInstruction);
            }

            instructionBuilder
                    .setKey(srcInstruction.getKey())
                    .setOrder(srcInstruction.getOrder());
            targetInstructionList.add(instructionBuilder.build());

        }

        return targetInstructionList;
    }

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return  TYPES;
    }

    @Override
    public List<FlowModInputBuilder> convert(Flow source, VersionDatapathIdConvertorData data) {
        if (data.getVersion() >= OFConstants.OFP_VERSION_1_3 && isSetVlanIdActionCasePresent(source)) {
            return handleSetVlanIdForOF13(source, data.getVersion(), data.getDatapathId());
        } else {
            return Collections.singletonList(toFlowModInput(source, data.getVersion(), data.getDatapathId()));
        }
    }
}
