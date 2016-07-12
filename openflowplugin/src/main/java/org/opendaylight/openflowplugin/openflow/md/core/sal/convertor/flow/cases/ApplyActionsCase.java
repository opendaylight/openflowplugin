/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.cases;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action.data.ActionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;

public class ApplyActionsCase extends ConvertorCase<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase, Instruction, ActionConvertorData> {
    public ApplyActionsCase() {
        super(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase.class, true, OFConstants.OFP_VERSION_1_0, OFConstants.OFP_VERSION_1_3);
    }

    @Override
    public Optional<Instruction> process(final @Nonnull org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase source, final ActionConvertorData data, ConvertorExecutor convertorExecutor) {
        ApplyActions applyActions = source.getApplyActions();
        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();

        final Optional<List<Action>> actionList = convertorExecutor.convert(applyActions.getAction(), data);

        applyActionsBuilder.setAction(actionList.orElse(Collections.emptyList()));
        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        instructionBuilder.setInstructionChoice(applyActionsCaseBuilder.build());
        return Optional.of(instructionBuilder.build());
    }
}
