/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.UpdateFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MatchTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.match.grouping.MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting a MD-SAL Flow into the OF flow mod
 */
public class FlowConvertor {
    private static final Logger logger = LoggerFactory.getLogger(FlowConvertor.class);

    // Default values for when things are null
    private static final  BigInteger DEFAULT_COOKIE = BigInteger.ZERO; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final TableId DEFAULT_TABLE_ID = new TableId(new Long(0)); // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Integer DEFAULT_IDLE_TIMEOUT = new Integer(5*60); // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Integer DEFAULT_HARD_TIMEOUT = new Integer(10*60); // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Integer DEFAULT_PRIORITY = Integer.parseInt("8000", 16);  // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Long DEFAULT_BUFFER_ID = Long.parseLong("ffffffff", 16);  // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Long OFPP_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_PORT = OFPP_ANY; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Long OFPG_ANY = Long.parseLong("ffffffff", 16);
    private static final Long DEFAULT_OUT_GROUP = OFPG_ANY; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_FLOW_REM = true; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_CHECK_OVERLAP = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_RESET_COUNTS = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_NO_PKT_COUNTS = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final boolean DEFAULT_OFPFF_NO_BYT_COUNTS = false; // TODO: Someone check me, I have no idea if this is a good default - eaw@cisco.com
    private static final Class<? extends MatchTypeBase> DEFAULT_MATCH_TYPE = OxmMatchType.class;

    public static FlowModInput toFlowModInput(Flow flow, short version) {
        FlowModInputBuilder flowMod = new FlowModInputBuilder();
        if(flow.getCookie() != null){
            flowMod.setCookie(flow.getCookie());
        } else {
            flowMod.setCookie(DEFAULT_COOKIE);
        }

        if (flow.getCookieMask() != null) {
            flowMod.setCookieMask(new BigInteger(flow.getCookieMask().toString()));
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
            if (flow.isStrict() != null && flow.isStrict()) {
                flowMod.setCommand(FlowModCommand.OFPFCDELETESTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCDELETE);
            }
        } else if (flow instanceof UpdateFlowInput) {
            if (flow.isStrict() != null && flow.isStrict()) {
                flowMod.setCommand(FlowModCommand.OFPFCMODIFYSTRICT);
            } else {
                flowMod.setCommand(FlowModCommand.OFPFCMODIFY);
            }
        }
        if(flow.getIdleTimeout() != null) {
            flowMod.setIdleTimeout(flow.getIdleTimeout());
        } else {
            flowMod.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
        }
        if(flow.getHardTimeout() != null) {
            flowMod.setHardTimeout(flow.getHardTimeout());
        } else {
            flowMod.setHardTimeout(DEFAULT_HARD_TIMEOUT);
        }
        if(flow.getPriority() != null) {
            flowMod.setPriority(flow.getPriority());
        } else {
            flowMod.setPriority(DEFAULT_PRIORITY);
        }
        if(flow.getBufferId() != null ) {
            flowMod.setBufferId(flow.getBufferId());
        } else {
            flowMod.setBufferId(DEFAULT_BUFFER_ID);
        }

        if (flow.getOutPort() != null) {
            flowMod.setOutPort(new PortNumber(flow.getOutPort().longValue()));
        } else {
            flowMod.setOutPort(new PortNumber(DEFAULT_OUT_PORT));
        }
        if(flow.getOutGroup() != null) {
            flowMod.setOutGroup(flow.getOutGroup());
        } else {
            flowMod.setOutGroup(DEFAULT_OUT_GROUP);
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags flowModFlags = flow.getFlags();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags ofFlowModFlags = null;
        if (flowModFlags != null) {
            ofFlowModFlags = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags(
                    flowModFlags.isCHECKOVERLAP(), flowModFlags.isNOBYTCOUNTS(), flowModFlags.isNOPKTCOUNTS(),
                    flowModFlags.isRESETCOUNTS(), flowModFlags.isSENDFLOWREM());
        } else {
            ofFlowModFlags = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags(
                    DEFAULT_OFPFF_CHECK_OVERLAP,DEFAULT_OFPFF_NO_BYT_COUNTS,DEFAULT_OFPFF_NO_PKT_COUNTS,
                    DEFAULT_OFPFF_RESET_COUNTS,DEFAULT_OFPFF_FLOW_REM);
        }
        flowMod.setFlags(ofFlowModFlags);

        if (flow.getMatch() != null) {
            MatchBuilder matchBuilder = new MatchBuilder();
            matchBuilder.setMatchEntries(MatchConvertor.toMatch(flow.getMatch()));
            matchBuilder.setType(DEFAULT_MATCH_TYPE);
            flowMod.setMatch(matchBuilder.build());
        }

        if (flow.getInstructions() != null) {
            flowMod.setInstructions(toInstructions(flow.getInstructions(), version));
        }
        flowMod.setVersion(version);
        return flowMod.build();
    }

    private static List<Instructions> toInstructions(
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions instructions,
            short version) {
        List<Instructions> instructionsList = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction instruction : instructions
                .getInstruction()) {
            InstructionsBuilder instructionBuilder = new InstructionsBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction curInstruction = instruction
                    .getInstruction();
            if (curInstruction instanceof GoToTable) {
                GoToTable goToTable = (GoToTable) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class);
                TableIdInstructionBuilder tableBuilder = new TableIdInstructionBuilder();
                tableBuilder.setTableId(goToTable.getTableId());
                instructionBuilder.addAugmentation(TableIdInstruction.class, tableBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof WriteMetadata) {
                WriteMetadata writeMetadata = (WriteMetadata) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class);
                MetadataInstructionBuilder metadataBuilder = new MetadataInstructionBuilder();
                metadataBuilder.setMetadata(MatchConvertor.convertBigIntegerTo64Bit(writeMetadata.getMetadata()));
                metadataBuilder
                        .setMetadataMask(MatchConvertor.convertBigIntegerTo64Bit(writeMetadata.getMetadataMask()));
                instructionBuilder.addAugmentation(MetadataInstruction.class, metadataBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof WriteActions) {
                WriteActions writeActions = (WriteActions) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(writeActions.getAction(),
                        version));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof ApplyActions) {
                ApplyActions applyActions = (ApplyActions) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(applyActions.getAction(),
                        version));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof ClearActions) {
                ClearActions clearActions = (ClearActions) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class);
                ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
                actionsInstructionBuilder.setActionsList(ActionConvertor.getActionList(clearActions.getAction(),
                        version));
                instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }

            else if (curInstruction instanceof Meter) {
                Meter meter = (Meter) curInstruction;
                instructionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class);
                MeterIdInstructionBuilder meterBuilder = new MeterIdInstructionBuilder();
                Long meterId = Long.parseLong(meter.getMeter());
                meterBuilder.setMeterId(meterId);
                instructionBuilder.addAugmentation(MeterIdInstruction.class, meterBuilder.build());
                instructionsList.add(instructionBuilder.build());
            }
        }
        return instructionsList;
    }
}