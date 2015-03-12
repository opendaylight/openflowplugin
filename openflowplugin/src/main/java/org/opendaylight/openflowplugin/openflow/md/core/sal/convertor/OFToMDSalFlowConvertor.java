/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.clear.actions._case.ClearActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.actions._case.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionsInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.MetadataInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.MeterIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.TableIdInstruction;

public class OFToMDSalFlowConvertor {

    private OFToMDSalFlowConvertor() {
        // hiding implicite constructor
    }

    /**
     * Method convert Openflow 1.3+ specific instructions to MD-SAL format
     * flow instruction
     *
     * @param instructions
     * @param ofVersion    current ofp version
     * @return
     */
    public static Instructions toSALInstruction(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction> instructions, OpenflowVersion ofVersion) {

        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();

        List<Instruction> salInstructionList = new ArrayList<Instruction>();
        int instructionTreeNodekey = 0;
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction salInstruction;
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction switchInst : instructions) {
            if (switchInst.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class)) {

                ActionsInstruction actionsInstruction = (ActionsInstruction) switchInst.getAugmentation(ActionsInstruction.class);
                ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
                ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();


                applyActionsBuilder.setAction(wrapActionList(ActionConvertor.toMDSalActions(actionsInstruction.getAction(), ofVersion, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_APPLYACTIONSCASE_APPLYACTIONS_ACTION_ACTION)));

                applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());
                salInstruction = applyActionsCaseBuilder.build();
            } else if (switchInst.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class)) {

                ClearActionsCaseBuilder clearActionsCaseBuilder = new ClearActionsCaseBuilder();
                ClearActionsBuilder clearActionsBuilder = new ClearActionsBuilder();
                clearActionsCaseBuilder.setClearActions(clearActionsBuilder.build());

                salInstruction = clearActionsCaseBuilder.build();
            } else if (switchInst.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class)) {

                TableIdInstruction tableIdInstruction = (TableIdInstruction) switchInst.getAugmentation(TableIdInstruction.class);

                GoToTableCaseBuilder goToTableCaseBuilder = new GoToTableCaseBuilder();
                GoToTableBuilder goToTableBuilder = new GoToTableBuilder();
                goToTableBuilder.setTableId(tableIdInstruction.getTableId());
                goToTableCaseBuilder.setGoToTable(goToTableBuilder.build());

                salInstruction = goToTableCaseBuilder.build();
            } else if (switchInst.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class)) {

                MeterIdInstruction meterIdInstruction = (MeterIdInstruction) switchInst.getAugmentation(MeterIdInstruction.class);


                MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
                MeterBuilder meterBuilder = new MeterBuilder();
                meterBuilder.setMeterId(new MeterId(meterIdInstruction.getMeterId()));
                meterCaseBuilder.setMeter(meterBuilder.build());

                salInstruction = meterCaseBuilder.build();
            } else if (switchInst.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class)) {

                ActionsInstruction actionsInstruction = (ActionsInstruction) switchInst.getAugmentation(ActionsInstruction.class);

                WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
                WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
                writeActionsBuilder.setAction(wrapActionList(ActionConvertor.toMDSalActions(actionsInstruction.getAction(), ofVersion, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION)));
                writeActionsCaseBuilder.setWriteActions(writeActionsBuilder.build());

                salInstruction = writeActionsCaseBuilder.build();
            } else if (switchInst.getType().equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class)) {

                MetadataInstruction metadataInstruction = (MetadataInstruction) switchInst.getAugmentation(MetadataInstruction.class);

                WriteMetadataCaseBuilder writeMetadataCaseBuilder = new WriteMetadataCaseBuilder();
                WriteMetadataBuilder writeMetadataBuilder = new WriteMetadataBuilder();
                writeMetadataBuilder.setMetadata(new BigInteger(1, metadataInstruction.getMetadata()));
                writeMetadataBuilder.setMetadataMask(new BigInteger(1, metadataInstruction.getMetadataMask()));
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

    /*
     * Method wrapping all the actions org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action
     * in org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action, to set appropriate keys
     * for actions. 
     */
    private static List<Action> wrapActionList(List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> actionList) {
        List<Action> actions = new ArrayList<>();

        int actionKey = 0;
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action : actionList) {
            ActionBuilder wrappedAction = new ActionBuilder();
            wrappedAction.setAction(action);
            wrappedAction.setKey(new ActionKey(actionKey));
            wrappedAction.setOrder(actionKey);
            actions.add(wrappedAction.build());
            actionKey++;
        }

        return actions;
    }

    /**
     * Method wraps openflow 1.0 actions list to Apply Action Instructions
     *
     * @param ofVersion current ofp version
     */

    public static Instructions wrapOF10ActionsToInstruction(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> actionsList, OpenflowVersion ofVersion) {
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();

        List<Instruction> salInstructionList = new ArrayList<Instruction>();

        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();

        applyActionsBuilder.setAction(wrapActionList(ActionConvertor.toMDSalActions(actionsList, ofVersion, ActionPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_INSTRUCTIONS_INSTRUCTION_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION)));

        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());

        InstructionBuilder instBuilder = new InstructionBuilder();
        instBuilder.setInstruction(applyActionsCaseBuilder.build());
        instBuilder.setKey(new InstructionKey(0));
        instBuilder.setOrder(0);
        salInstructionList.add(instBuilder.build());

        instructionsBuilder.setInstruction(salInstructionList);
        return instructionsBuilder.build();
    }

}
