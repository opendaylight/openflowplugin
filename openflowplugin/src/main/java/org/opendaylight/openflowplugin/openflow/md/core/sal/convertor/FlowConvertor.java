/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;
import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flowflag.FlowFlagReactor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchReactor;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.clear.actions._case.ClearActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MatchTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting a MD-SAL Flow into the OF flow mod
 */
public class FlowConvertor {
    private static final Logger logger = LoggerFactory.getLogger(FlowConvertor.class);

    // Default values for when things are null
    private static final BigInteger DEFAULT_COOKIE = BigInteger.ZERO;
    private static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO;
    private static final TableId DEFAULT_TABLE_ID = new TableId(0L);
    private static final Integer DEFAULT_IDLE_TIMEOUT = 5 * 60;
    private static final Integer DEFAULT_HARD_TIMEOUT = 10 * 60;
    private static final Integer DEFAULT_PRIORITY = Integer.parseInt("8000", 16);
    private static final Long DEFAULT_BUFFER_ID = Long.parseLong("ffffffff", 16);
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_PORT = OFPP_ANY;
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_GROUP = OFPG_ANY;
    /** flow flag: remove */
    public static final boolean DEFAULT_OFPFF_FLOW_REM = true;
    /** flow flag: check overlap */
    public static final boolean DEFAULT_OFPFF_CHECK_OVERLAP = false;
    /** flow flag: reset counts */
    public static final boolean DEFAULT_OFPFF_RESET_COUNTS = false;
    /** flow flag: don't keep track of packet counts */
    public static final boolean DEFAULT_OFPFF_NO_PKT_COUNTS = false;
    /** flow flag: don't keep track of byte counts */
    public static final boolean DEFAULT_OFPFF_NO_BYT_COUNTS = false;
    /** flow flag: emergency [OFP-1.0] */
    public static final boolean DEFAULT_OFPFF_EMERGENCY = false;
    /** OxmMatch type */
    public static final Class<? extends MatchTypeBase> DEFAULT_MATCH_TYPE = OxmMatchType.class;
    /** default match entries - empty */
    public static final List<MatchEntries> DEFAULT_MATCH_ENTRIES = new ArrayList<MatchEntries>();


    /**
     * This method converts the SAL Flow to OF Flow.
     * It checks if there is a set-vlan-id (1.0) action made on OF1.3.
     * If yes its handled separately
     */
    public static List<FlowModInputBuilder> toFlowModInputs(Flow srcFlow, short version, BigInteger datapathId) {
        List<FlowModInputBuilder> list = new ArrayList<>();

        if (version >= OFConstants.OFP_VERSION_1_3 && isSetVlanIdActionCasePresent(srcFlow)) {
            list.addAll(handleSetVlanIdForOF13(srcFlow, version, datapathId));
        } else {
            list.add(toFlowModInput(srcFlow, version, datapathId));
        }
        return list;
    }

    public static FlowModInputBuilder toFlowModInput(Flow flow, short version, BigInteger datapathid) {

        FlowModInputBuilder flowMod = new FlowModInputBuilder();
        if (flow.getCookie() != null) {
            flowMod.setCookie(flow.getCookie().getValue());
        } else {
            flowMod.setCookie(DEFAULT_COOKIE);
        }

        if (flow.getCookieMask() != null) {
            flowMod.setCookieMask(flow.getCookieMask().getValue());
        } else {
            flowMod.setCookieMask(DEFAULT_COOKIE_MASK);
        }

        if (flow.getTableId() != null) {
            flowMod.setTableId(new TableId(flow.getTableId().longValue()));
        } else {
            flowMod.setTableId(DEFAULT_TABLE_ID);
        }

        if (flow instanceof AddFlowInput) {
            flowMod.setCommand(FlowModCommand.OFPFCADD);
        } else if (flow instanceof RemoveFlowInput) {
            if (Objects.firstNonNull(flow.isStrict(), Boolean.FALSE)) {
                flowMod.setCommand(FlowModCommand.OFPFCDELETESTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCDELETE);
            }
        } else if (flow instanceof UpdatedFlow) {
            if (Objects.firstNonNull(flow.isStrict(), Boolean.FALSE)) {
                flowMod.setCommand(FlowModCommand.OFPFCMODIFYSTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCMODIFY);
            }
        }

        if (flow.getIdleTimeout() != null) {
            flowMod.setIdleTimeout(flow.getIdleTimeout());
        } else {
            flowMod.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        }
        if (flow.getHardTimeout() != null) {
            flowMod.setHardTimeout(flow.getHardTimeout());
        } else {
            flowMod.setHardTimeout(DEFAULT_HARD_TIMEOUT);
        }
        if (flow.getPriority() != null) {
            flowMod.setPriority(flow.getPriority());
        } else {
            flowMod.setPriority(DEFAULT_PRIORITY);
        }
        if (flow.getBufferId() != null) {
            flowMod.setBufferId(flow.getBufferId());
        } else {
            flowMod.setBufferId(DEFAULT_BUFFER_ID);
        }

        if (flow.getOutPort() != null) {
            flowMod.setOutPort(new PortNumber(flow.getOutPort().longValue()));
        } else {
            flowMod.setOutPort(new PortNumber(DEFAULT_OUT_PORT));
        }
        if (flow.getOutGroup() != null) {
            flowMod.setOutGroup(flow.getOutGroup());
        } else {
            flowMod.setOutGroup(DEFAULT_OUT_GROUP);
        }

        
        // convert and inject flowFlags
        FlowFlagReactor.getInstance().convert(flow.getFlags(), version, flowMod,datapathid);

        // convert and inject match
        MatchReactor.getInstance().convert(flow.getMatch(), version, flowMod,datapathid);

        if (flow.getInstructions() != null) {
            flowMod.setInstruction(toInstructions(flow.getInstructions(), version,datapathid));
            flowMod.setAction(getActions(flow.getInstructions(), version,datapathid));
        }
        flowMod.setVersion(version);
        
        return flowMod;
    }

    private static List<Instruction> toInstructions(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions instructions,
            short version,BigInteger datapathid) {
        List<Instruction> instructionsList = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : instructions
                .getInstruction()) {
            InstructionBuilder instructionBuilder = new InstructionBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();
            if (curInstruction instanceof GoToTableCase) {
                GoToTableCase goToTablecase = (GoToTableCase) curInstruction;
                GoToTable goToTable = goToTablecase.getGoToTable();
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class);
                TableIdInstructionBuilder tableBuilder = new TableIdInstructionBuilder();
                tableBuilder.setTableId(goToTable.getTableId());
                instructionBuilder.addAugmentation(TableIdInstruction.class, tableBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof WriteMetadataCase) {
                WriteMetadataCase writeMetadatacase = (WriteMetadataCase) curInstruction;
                WriteMetadata writeMetadata = writeMetadatacase.getWriteMetadata();
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class);
                MetadataInstructionBuilder metadataBuilder = new MetadataInstructionBuilder();
                metadataBuilder.setMetadata(ByteUtil.convertBigIntegerToNBytes(writeMetadata.getMetadata(),
                                                                               OFConstants.SIZE_OF_LONG_IN_BYTES));
                metadataBuilder
                        .setMetadataMask(ByteUtil.convertBigIntegerToNBytes(writeMetadata.getMetadataMask(),
                                                                            OFConstants.SIZE_OF_LONG_IN_BYTES));
                instructionBuilder.addAugmentation(MetadataInstruction.class, metadataBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof WriteActionsCase) {
                WriteActionsCase writeActionscase = (WriteActionsCase) curInstruction;
                WriteActions writeActions = writeActionscase.getWriteActions();
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setAction(ActionConvertor.getActions(writeActions.getAction(),
                        version,datapathid));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof ApplyActionsCase) {
                ApplyActionsCase applyActionscase = (ApplyActionsCase) curInstruction;
                ApplyActions applyActions = applyActionscase.getApplyActions();
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setAction(ActionConvertor.getActions(applyActions.getAction(),
                        version,datapathid));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof ClearActionsCase) {
                ClearActionsCase clearActionscase = (ClearActionsCase) curInstruction;
                ClearActions clearActions = clearActionscase.getClearActions();
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class);
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof MeterCase) {
                MeterCase metercase = (MeterCase) curInstruction;
                Meter meter = metercase.getMeter();
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class);
                MeterIdInstructionBuilder meterBuilder = new MeterIdInstructionBuilder();
                Long meterId = meter.getMeterId().getValue();
                meterBuilder.setMeterId(meterId);
                instructionBuilder.addAugmentation(MeterIdInstruction.class, meterBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }
        }
        return instructionsList;
    }
    
    private static List<Action> getActions(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions instructions,
            short version,BigInteger datapathid) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> sortedInstructions =
            Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>toInstance())
                .sortedCopy(instructions.getInstruction());

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : sortedInstructions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();

            if (curInstruction instanceof ApplyActionsCase) {
                ApplyActionsCase applyActionscase = (ApplyActionsCase) curInstruction;
                ApplyActions applyActions = applyActionscase.getApplyActions();
                return ActionConvertor.getActions(applyActions.getAction(), version,datapathid);
            }
        }
        return null;
    }

    // check if set vlanid action is present in the flow
    private static boolean isSetVlanIdActionCasePresent(Flow flow) {
        boolean isPresent = false;
        // we are trying to find if there is a set-vlan-id action (OF1.0) action present in the flow.
        // If yes,then we would need to two flows
        if (flow.getInstructions() != null) {
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction :
                flow.getInstructions().getInstruction()) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction =
                    instruction.getInstruction();

                if (curInstruction instanceof ApplyActionsCase) {
                    ApplyActionsCase applyActionscase = (ApplyActionsCase) curInstruction;
                    ApplyActions applyActions = applyActionscase.getApplyActions();
                    for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action action :
                            applyActions.getAction()) {
                        if (action.getAction() instanceof SetVlanIdActionCase) {
                            isPresent = true;
                            break;
                        }
                    }
                }
            }
        }
        return isPresent;
    }


    /**
     * A) If user provided flow's match includes vlan match  and action has set_vlan_field
     * Install following rules
     *   1) match on (OFPVID_PRESENT |value) without mask + action [set_field]
     *
     * B) if user provided flow's match doesn't include vlan match but action has set_vlan field
     *    1) Match on (OFPVID_NONE ) without mask + action [push vlan tag + set_field]
     *    2) Match on (OFPVID_PRESENT) with mask (OFPVID_PRESENT ) + action [ set_field]
     */
    private static List<FlowModInputBuilder> handleSetVlanIdForOF13(Flow srcFlow, short version, BigInteger datapathId) {
        List<FlowModInputBuilder> list = new ArrayList<>();

        boolean hasVlanMatch = (srcFlow.getMatch() != null && srcFlow.getMatch().getVlanMatch() != null);
        if (hasVlanMatch) {
            //create flow with setfield and match
            // match on vlan tag or vlanid with no mask
            VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
            VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
            vlanIdBuilder.setVlanIdPresent(true);
            vlanIdBuilder.setVlanId(srcFlow.getMatch().getVlanMatch().getVlanId().getVlanId());
            vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
            Match match = new MatchBuilder().setVlanMatch(vlanMatchBuilder.build()).build();

            Optional<? extends Flow> optional = injectMatchToFlow(srcFlow, match);
            if (optional.isPresent()) {
                list.add(toFlowModInput(optional.get(), version, datapathId));
            }
        } else {
            // create 2 flows
            //flow 1
            // match on no vlan tag with no mask
            VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
            VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
            vlanIdBuilder.setVlanIdPresent(false);
            vlanIdBuilder.setVlanId(new VlanId(0));
            vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
            Match match1 = new MatchBuilder().setVlanMatch(vlanMatchBuilder.build()).build();

            Optional<? extends Flow> optional1 = injectMatchAndAction(srcFlow, match1);
            if (optional1.isPresent()) {
                list.add(toFlowModInput(optional1.get(), version, datapathId));
            }

            //flow2
            // match on vlan tag with mask
            VlanMatchBuilder vlanMatchBuilder2 = new VlanMatchBuilder();
            VlanIdBuilder vlanIdBuilder2 = new VlanIdBuilder();
            vlanIdBuilder2.setVlanIdPresent(true);
            vlanIdBuilder2.setVlanId(new VlanId(0));
            vlanMatchBuilder2.setVlanId(vlanIdBuilder2.build());
            Match match2 = new MatchBuilder().setVlanMatch(vlanMatchBuilder2.build()).build();
            Optional<? extends Flow> optional2 = injectMatchToFlow(srcFlow, match2);
            if (optional2.isPresent()) {
                list.add(toFlowModInput(optional2.get(), version, datapathId));
            }
        }
        return list;
    }


    private static Optional<? extends Flow> injectMatchToFlow(Flow sourceFlow, Match match) {
        if (sourceFlow instanceof AddFlowInput) {
            return Optional.<AddFlowInput>of(new AddFlowInputBuilder(sourceFlow).setMatch(match).build());
        } else if (sourceFlow instanceof RemoveFlowInput) {
            return Optional.<RemoveFlowInput>of(new RemoveFlowInputBuilder(sourceFlow).setMatch(match).build());
        } else if (sourceFlow instanceof UpdatedFlow) {
            return Optional.<UpdatedFlow>of(new UpdatedFlowBuilder(sourceFlow).setMatch(match).build());
        } else {
            return Optional.<Flow>absent();
        }
    }

    private static Optional<? extends Flow> injectMatchAndAction(Flow sourceFlow, Match match) {

        Instructions instructions = (new InstructionsBuilder())
            .setInstruction(injectPushActionToInstruction(sourceFlow))
            .build();

        if (sourceFlow instanceof AddFlowInput) {
            return Optional.<AddFlowInput>of(new AddFlowInputBuilder(sourceFlow)
                .setMatch(match).setInstructions(instructions).build());
        } else if (sourceFlow instanceof RemoveFlowInput) {
            return Optional.<RemoveFlowInput>of(new RemoveFlowInputBuilder(sourceFlow)
                .setMatch(match).setInstructions(instructions).build());
        } else if (sourceFlow instanceof UpdatedFlow) {
            return Optional.<UpdatedFlow>of(new UpdatedFlowBuilder(sourceFlow)
                .setMatch(match).setInstructions(instructions).build());
        } else {
            return Optional.<Flow>absent();
        }
    }

    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction>
        injectPushActionToInstruction(final Flow sourceFlow) {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> srcInstructionList =
            sourceFlow.getInstructions().getInstruction();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction> targetInstructionList = new ArrayList<>(srcInstructionList.size());
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> targetActionList = new ArrayList<>();

        for (int i=0; i < srcInstructionList.size(); i++) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction srcInstruction = srcInstructionList.get(i);
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curSrcInstruction = srcInstruction.getInstruction();

            if (curSrcInstruction instanceof ApplyActionsCase) {
                ApplyActionsCase applyActionscase = (ApplyActionsCase) curSrcInstruction;
                ApplyActions applyActions = applyActionscase.getApplyActions();
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> srcActionList = applyActions.getAction();

                boolean orderIncrement = false;
                for (int j=0; j < srcActionList.size(); j++) {
                    // check if its a set-vlan-action. If yes, then add the injected-action

                    if(srcActionList.get(j).getAction() instanceof SetVlanIdActionCase) {
                        SetVlanIdActionCase setVlanIdActionCase = (SetVlanIdActionCase) srcActionList.get(j).getAction();

                        PushVlanActionCaseBuilder pushVlanActionCaseBuilder = new PushVlanActionCaseBuilder();
                        PushVlanActionBuilder pushVlanActionBuilder = new PushVlanActionBuilder();

                        pushVlanActionBuilder.setCfi(new VlanCfi(1))
                            .setVlanId(setVlanIdActionCase.getSetVlanIdAction().getVlanId())
                            .setEthernetType(sourceFlow.getMatch().getEthernetMatch()
                                .getEthernetType().getType().getValue().intValue())
                            .setTag(sourceFlow.getMatch().getEthernetMatch()
                                .getEthernetType().getType().getValue().intValue());
                        pushVlanActionCaseBuilder.setPushVlanAction(pushVlanActionBuilder.build());
                        PushVlanActionCase injectedAction = pushVlanActionCaseBuilder.build();

                        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder actionBuilder = new ActionBuilder();
                        actionBuilder.setAction(injectedAction)
                            .setKey(srcActionList.get(j).getKey())
                            .setOrder(srcActionList.get(j).getOrder());

                        targetActionList.add(actionBuilder.build());

                        orderIncrement = true;
                    }

                    if (orderIncrement) {
                        // we need to increment the order for all the actions added after injection
                        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder actionBuilder =
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder(srcActionList.get(j));
                        actionBuilder.setOrder(srcActionList.get(j).getOrder() + 1);
                        targetActionList.add(actionBuilder.build());
                    } else {
                        targetActionList.add(srcActionList.get(j));
                    }
                }

                ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
                ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
                applyActionsBuilder.setAction(targetActionList);
                applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());

                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder builder =
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder();

                targetInstructionList.add(
                    builder.setKey(srcInstruction.getKey())
                        .setOrder(srcInstruction.getOrder())
                        .setInstruction(applyActionsCaseBuilder.build())
                        .build());


            } else {
                org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder builder =
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder();
                targetInstructionList.add(
                    builder.setKey(srcInstruction.getKey())
                        .setOrder(srcInstruction.getOrder())
                        .setInstruction(curSrcInstruction)
                        .build());
            }
        }

        return targetInstructionList;
    }

}
