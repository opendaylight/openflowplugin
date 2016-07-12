/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice._goto.table._case.GotoTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.actions._case.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.write.metadata._case.WriteMetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase;

/**
 * Created by Martin Bobak mbobak@cisco.com on 9/18/14.
 */
public class FlowStatsResponseConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    private static final int PRESET_COUNT = 7;

    /**
     * Test method for {@link FlowInstructionResponseConvertor#convert(java.util.List, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData)} }
     */
    @Test
    public void testToSALInstruction() {
        List<Instruction> instructionsList = new ArrayList<>();
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        for (int i = 0; i < PRESET_COUNT; i++) {
            ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
            ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
            ActionBuilder actionBuilder = new ActionBuilder();
            List<Action> actions = new ArrayList<>();
            for (int j = 0; j < PRESET_COUNT; j++) {
                actions.add(actionBuilder.build());
            }
            applyActionsBuilder.setAction(actions);
            applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());
            instructionBuilder.setInstructionChoice(applyActionsCaseBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        Instructions instructions;
        VersionConvertorData data = new VersionConvertorData(OFConstants.OFP_VERSION_1_3);

        instructions = convert(instructionsList, data);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());

        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            GotoTableCaseBuilder gotoTableCaseBuilder = new GotoTableCaseBuilder();
            GotoTableBuilder gotoTableBuilder = new GotoTableBuilder();
            gotoTableBuilder.setTableId((short) i);
            gotoTableCaseBuilder.setGotoTable(gotoTableBuilder.build());
            instructionBuilder.setInstructionChoice(gotoTableCaseBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = convert(instructionsList, data);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());

        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
            MeterBuilder meterBuilder = new MeterBuilder();
            meterBuilder.setMeterId((long) i);
            meterCaseBuilder.setMeter(meterBuilder.build());
            instructionBuilder.setInstructionChoice(meterCaseBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = convert(instructionsList, data);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());


        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
            WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
            ActionBuilder actionBuilder = new ActionBuilder();
            List<Action> actions = new ArrayList<>();
            for (int j = 0; j < PRESET_COUNT; j++) {
                actions.add(actionBuilder.build());
            }
            writeActionsBuilder.setAction(actions);
            writeActionsCaseBuilder.setWriteActions(writeActionsBuilder.build());
            instructionBuilder.setInstructionChoice(writeActionsCaseBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = convert(instructionsList, data);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());

        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            ClearActionsCaseBuilder clearActionsCaseBuilder = new ClearActionsCaseBuilder();
            instructionBuilder.setInstructionChoice(clearActionsCaseBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = convert(instructionsList, data);
        assertNotNull(instructions);
        int instructionSize = instructions.getInstruction().size();
        assertEquals(PRESET_COUNT, instructionSize);

        instructionsList = new ArrayList<>();
        for (int i = 0; i < PRESET_COUNT; i++) {
            WriteMetadataCaseBuilder metadataCaseBuilder = new WriteMetadataCaseBuilder();
            WriteMetadataBuilder metadataBuilder = new WriteMetadataBuilder();
            
            metadataBuilder.setMetadata(BigInteger.TEN.setBit(i).toByteArray());
            metadataBuilder.setMetadataMask(BigInteger.ONE.setBit(i).toByteArray());
            metadataCaseBuilder.setWriteMetadata(metadataBuilder.build());
            instructionBuilder.setInstructionChoice(metadataCaseBuilder.build());
            instructionsList.add(instructionBuilder.build());
        }

        instructions = convert(instructionsList, data);
        assertNotNull(instructions);
        assertEquals(PRESET_COUNT, instructions.getInstruction().size());
    }

    private Instructions convert(List<Instruction> instructionsList, VersionConvertorData data) {
        Optional<Instructions> instructionsOptional = convertorManager.convert(instructionsList, data);
        assertTrue("Flow instruction response convertor not found", instructionsOptional.isPresent());
        return instructionsOptional.get();
    }

    private static final class MockActionBase extends ActionBase {
        // for testing purposes
    }
}