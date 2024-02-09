/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import com.google.common.collect.ImmutableList;
import java.math.BigInteger;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Converts Openflow 1.3+ specific instructions to MD-SAL format flow instruction.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<Instructions> salFlowInstruction = convertorManager.convert(ofFlowInstructions, data);
 * }
 * </pre>
 */
public final class FlowInstructionResponseConvertor extends Convertor<
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions
            .grouping.Instruction>,
        Instructions,
        VersionConvertorData> {

    private static final Set<Class<?>> TYPES = Collections.singleton(org.opendaylight.yang.gen.v1.urn.opendaylight
            .openflow.common.instruction.rev130731.instructions.grouping.Instruction.class);

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public Instructions convert(final List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction
            .rev130731.instructions.grouping.Instruction> source, final VersionConvertorData data) {

        final var salInstructionList = ImmutableList.<Instruction>builder();

        for (var switchInst : source) {
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction
                .Instruction salInstruction;

            if (switchInst.getInstructionChoice() instanceof ApplyActionsCase actionsInstruction) {
                final var actionResponseConvertorData = new ActionResponseConvertorData(data.getVersion());
                actionResponseConvertorData.setActionPath(ActionPath.FLOWS_STATISTICS_UPDATE_APPLY_ACTIONS);

                final Optional<List<Action>> actions = getConvertorExecutor().convert(
                        actionsInstruction.getApplyActions().getAction(), actionResponseConvertorData);

                salInstruction = new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder()
                        .setAction(FlowConvertorUtil.wrapActionList(actions.orElse(List.of())))
                        .build())
                    .build();
            } else if (switchInst.getInstructionChoice() instanceof ClearActionsCase) {
                salInstruction = new ClearActionsCaseBuilder().build();
            } else if (switchInst.getInstructionChoice() instanceof GotoTableCase gotoTableCase) {
                salInstruction = new GoToTableCaseBuilder()
                    .setGoToTable(new GoToTableBuilder()
                        .setTableId(gotoTableCase.getGotoTable().getTableId())
                        .build())
                    .build();
            } else if (switchInst.getInstructionChoice() instanceof MeterCase meterIdInstruction) {
                salInstruction = new MeterCaseBuilder()
                    .setMeter(new MeterBuilder()
                        .setMeterId(new MeterId(meterIdInstruction.getMeter().getMeterId()))
                        .build())
                    .build();
            } else if (switchInst.getInstructionChoice() instanceof WriteActionsCase writeActionsCase) {
                final var actionResponseConvertorData = new ActionResponseConvertorData(data.getVersion());
                actionResponseConvertorData.setActionPath(ActionPath.FLOWS_STATISTICS_UPDATE_WRITE_ACTIONS);

                final Optional<List<Action>> actions = getConvertorExecutor().convert(
                        writeActionsCase.getWriteActions().getAction(), actionResponseConvertorData);

                salInstruction = new WriteActionsCaseBuilder()
                    .setWriteActions(new WriteActionsBuilder()
                        .setAction(FlowConvertorUtil.wrapActionList(actions.orElse(List.of())))
                        .build())
                    .build();
            } else if (switchInst.getInstructionChoice() instanceof WriteMetadataCase writeMetadataCase) {
                final var meta = writeMetadataCase.getWriteMetadata();
                salInstruction = new WriteMetadataCaseBuilder()
                    .setWriteMetadata(new WriteMetadataBuilder()
                        .setMetadata(Uint64.valueOf(new BigInteger(OFConstants.SIGNUM_UNSIGNED, meta.getMetadata())))
                        .setMetadataMask(Uint64.valueOf(new BigInteger(OFConstants.SIGNUM_UNSIGNED,
                            meta.getMetadataMask())))
                        .build())
                    .build();
            } else {
                continue;
            }

            salInstructionList.add(new InstructionBuilder().setInstruction(salInstruction).build());
        }

        return new InstructionsBuilder()
            .setInstruction(salInstructionList.build())
            .build();
    }
}
