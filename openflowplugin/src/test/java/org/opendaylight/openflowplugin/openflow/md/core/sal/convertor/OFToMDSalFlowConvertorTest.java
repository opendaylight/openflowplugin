/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/18/14.
 */
public class OFToMDSalFlowConvertorTest {

    private static final int PRESET_COUNT = 7;

    @Test
    /**
     * Test method for {@link OFToMDSalFlowConvertor#wrapOF10ActionsToInstruction(java.util.List, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    public void testWrapOF10ActionsToInstruction() {
        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actions = new ArrayList<>();
        for (int j = 0; j < PRESET_COUNT; j++) {
            actionBuilder.setType(MockActionBase.class);
            actions.add(actionBuilder.build());
        }
        Instructions instructions = OFToMDSalFlowConvertor.wrapOF10ActionsToInstruction(actions, OpenflowVersion.OF13);
        assertNotNull(instructions);
    }
        @Test
    /**
     * Test method for {@link OFToMDSalFlowConvertor#toSALInstruction(java.util.List, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    public void testToSALInstruction() {
        List<Instruction> instructionsList = new ArrayList<>();
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        for (int i = 0; i < PRESET_COUNT; i++) {
            instructionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
            ActionBuilder actionBuilder = new ActionBuilder();
            List<Action> actions = new ArrayList<>();
            for (int j = 0; j < PRESET_COUNT; j++) {
                actionBuilder.setType(MockActionBase.class);
                actions.add(actionBuilder.build());
            }
            actionsInstructionBuilder.setAction(actions);
            instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        Instructions instructions = OFToMDSalFlowConvertor.toSALInstruction(instructionsList, OpenflowVersion.OF13);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());

        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            instructionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            TableIdInstructionBuilder tableIdInstructionBuilder = new TableIdInstructionBuilder();
            tableIdInstructionBuilder.setTableId((short) i);
            instructionBuilder.addAugmentation(TableIdInstruction.class, tableIdInstructionBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = OFToMDSalFlowConvertor.toSALInstruction(instructionsList, OpenflowVersion.OF13);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());

        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            instructionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            MeterIdInstructionBuilder meterIdInstructionBuilder = new MeterIdInstructionBuilder();
            meterIdInstructionBuilder.setMeterId(Long.valueOf(i));
            instructionBuilder.addAugmentation(MeterIdInstruction.class, meterIdInstructionBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = OFToMDSalFlowConvertor.toSALInstruction(instructionsList, OpenflowVersion.OF13);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());


        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            instructionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            ActionsInstructionBuilder actionsInstructionBuilder = new ActionsInstructionBuilder();
            ActionBuilder actionBuilder = new ActionBuilder();
            List<Action> actions = new ArrayList<>();
            for (int j = 0; j < PRESET_COUNT; j++) {
                actionBuilder.setType(MockActionBase.class);
                actions.add(actionBuilder.build());
            }
            actionsInstructionBuilder.setAction(actions);
            instructionBuilder.addAugmentation(ActionsInstruction.class, actionsInstructionBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = OFToMDSalFlowConvertor.toSALInstruction(instructionsList, OpenflowVersion.OF13);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());


        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            instructionBuilder.setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            MetadataInstructionBuilder metadataInstructionBuilder = new MetadataInstructionBuilder();
            metadataInstructionBuilder.setMetadata(new byte[i]);
            metadataInstructionBuilder.setMetadataMask(new byte[i]);
            instructionBuilder.addAugmentation(MetadataInstruction.class, metadataInstructionBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = OFToMDSalFlowConvertor.toSALInstruction(instructionsList, OpenflowVersion.OF13);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());


    }

    private static final class MockActionBase extends ActionBase {

    }

}
