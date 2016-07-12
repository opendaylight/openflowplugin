/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts Openflow 1.3+ specific instructions to MD-SAL format flow instruction
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<Instructions> salFlowInstruction = convertorManager.convert(ofFlowInstructions, data);
 * }
 * </pre>
 */
public final class FlowInstructionResponseConvertor extends Convertor<
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction>,
        Instructions,
        VersionConvertorData> {

    private static final Set<Class<? extends DataContainer>> TYPES = Collections.singleton(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction.class);

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return TYPES;
    }

    @Override
    public Instructions convert(List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction> source, VersionConvertorData data) {
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();

        List<Instruction> salInstructionList = new ArrayList<>();
        int instructionTreeNodekey = 0;
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction salInstruction;

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.
                Instruction switchInst : source) {
            if (switchInst.getInstructionChoice() instanceof ApplyActionsCase) {
                ApplyActionsCase actionsInstruction = ((ApplyActionsCase) switchInst.getInstructionChoice());
                ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
                ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();

                final ActionResponseConvertorData actionResponseConvertorData = new ActionResponseConvertorData(data.getVersion());
                actionResponseConvertorData.setActionPath(ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION);

                final Optional<List<Action>> actions = getConvertorExecutor().convert(
                        actionsInstruction.getApplyActions().getAction(), actionResponseConvertorData);

                applyActionsBuilder.setAction(FlowConvertorUtil.wrapActionList(actions.orElse(Collections.emptyList())));
                applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());
                salInstruction = applyActionsCaseBuilder.build();
            } else if (switchInst.getInstructionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase) {
                ClearActionsCaseBuilder clearActionsCaseBuilder = new ClearActionsCaseBuilder();
                salInstruction = clearActionsCaseBuilder.build();
            } else if (switchInst.getInstructionChoice() instanceof GotoTableCase) {
                GotoTableCase gotoTableCase = ((GotoTableCase) switchInst.getInstructionChoice());

                GoToTableCaseBuilder goToTableCaseBuilder = new GoToTableCaseBuilder();
                GoToTableBuilder goToTableBuilder = new GoToTableBuilder();
                goToTableBuilder.setTableId(gotoTableCase.getGotoTable().getTableId());
                goToTableCaseBuilder.setGoToTable(goToTableBuilder.build());

                salInstruction = goToTableCaseBuilder.build();
            } else if (switchInst.getInstructionChoice() instanceof MeterCase) {
                MeterCase meterIdInstruction = ((MeterCase) switchInst.getInstructionChoice());

                MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
                MeterBuilder meterBuilder = new MeterBuilder();
                meterBuilder.setMeterId(new MeterId(meterIdInstruction.getMeter().getMeterId()));
                meterCaseBuilder.setMeter(meterBuilder.build());

                salInstruction = meterCaseBuilder.build();
            } else if (switchInst.getInstructionChoice() instanceof WriteActionsCase) {
                WriteActionsCase writeActionsCase = ((WriteActionsCase) switchInst.getInstructionChoice());
                WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
                WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();

                final ActionResponseConvertorData actionResponseConvertorData = new ActionResponseConvertorData(data.getVersion());
                actionResponseConvertorData.setActionPath(ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION);

                final Optional<List<Action>> actions = getConvertorExecutor().convert(
                        writeActionsCase.getWriteActions().getAction(), actionResponseConvertorData);

                writeActionsBuilder.setAction(FlowConvertorUtil.wrapActionList(actions.orElse(Collections.emptyList())));
                writeActionsCaseBuilder.setWriteActions(writeActionsBuilder.build());
                salInstruction = writeActionsCaseBuilder.build();
            } else if (switchInst.getInstructionChoice() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase writeMetadataCase =
                        ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase) switchInst.getInstructionChoice());
                WriteMetadataCaseBuilder writeMetadataCaseBuilder = new WriteMetadataCaseBuilder();
                WriteMetadataBuilder writeMetadataBuilder = new WriteMetadataBuilder();
                writeMetadataBuilder.setMetadata(new BigInteger(OFConstants.SIGNUM_UNSIGNED, writeMetadataCase.getWriteMetadata().getMetadata()));
                writeMetadataBuilder.setMetadataMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, writeMetadataCase.getWriteMetadata().getMetadataMask()));
                writeMetadataCaseBuilder.setWriteMetadata(writeMetadataBuilder.build());
                salInstruction = writeMetadataCaseBuilder.build();
            } else {
                continue;
            }

            InstructionBuilder instBuilder = new InstructionBuilder();
            instBuilder.setInstruction(salInstruction);
            instBuilder.setKey(new InstructionKey(instructionTreeNodekey));
            instBuilder.setOrder(instructionTreeNodekey);
            instructionTreeNodekey++;
            salInstructionList.add(instBuilder.build());

        }

        instructionsBuilder.setInstruction(salInstructionList);
        return instructionsBuilder.build();
    }
}
