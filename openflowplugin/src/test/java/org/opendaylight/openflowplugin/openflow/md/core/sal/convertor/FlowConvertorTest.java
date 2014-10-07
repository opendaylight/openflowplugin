/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionsInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MeterIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TableIdInstruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;

/**
 * @author michal.polkorab
 *
 */
public class FlowConvertorTest {

    /**
     * Tests {@link FlowConvertor#toFlowModInput(Flow, short, BigInteger)}
     */
    @Test
    public void test() {
        RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder();
        flowBuilder.setBarrier(false);
        flowBuilder.setCookie(new FlowCookie(new BigInteger("4")));
        flowBuilder.setCookieMask(new FlowCookie(new BigInteger("5")));
        flowBuilder.setTableId((short) 6);
        flowBuilder.setStrict(true);
        flowBuilder.setIdleTimeout(50);
        flowBuilder.setHardTimeout(500);
        flowBuilder.setPriority(40);
        flowBuilder.setBufferId(18L);
        flowBuilder.setOutPort(new BigInteger("65535"));
        flowBuilder.setOutGroup(5000L);
        flowBuilder.setFlags(null);
        flowBuilder.setMatch(null);
        RemoveFlowInput flow = flowBuilder.build();

        FlowModInput flowMod = FlowConvertor
                .toFlowModInput(flow, EncodeConstants.OF13_VERSION_ID, new BigInteger("42")).build();

        Assert.assertEquals("Wrong version", 4, flowMod.getVersion().intValue());
        Assert.assertEquals("Wrong cookie", 4, flowMod.getCookie().intValue());
        Assert.assertEquals("Wrong cookie mask", 5, flowMod.getCookieMask().intValue());
        Assert.assertEquals("Wrong table id", 6, flowMod.getTableId().getValue().intValue());
        Assert.assertEquals("Wrong command", FlowModCommand.OFPFCDELETESTRICT, flowMod.getCommand());
        Assert.assertEquals("Wrong idle timeout", 50, flowMod.getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hard timeout", 500, flowMod.getHardTimeout().intValue());
        Assert.assertEquals("Wrong priority", 40, flowMod.getPriority().intValue());
        Assert.assertEquals("Wrong buffer id", 18, flowMod.getBufferId().intValue());
        Assert.assertEquals("Wrong out port", 65535, flowMod.getOutPort().getValue().intValue());
        Assert.assertEquals("Wrong out group", 5000, flowMod.getOutGroup().intValue());
        Assert.assertEquals("Wrong flags", new FlowModFlags(false, false, false, false, true), flowMod.getFlags());
        Assert.assertEquals("Wrong match", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType",
                flowMod.getMatch().getType().getName());
        Assert.assertEquals("Wrong match entries size", 0, flowMod.getMatch().getMatchEntries().size());
    }

    /**
     * Tests {@link FlowConvertor#toFlowModInput(Flow, short, BigInteger)}
     */
    @Test
    public void testOnlyModifyStrictCommand() {
        UpdatedFlowBuilder flowBuilder = new UpdatedFlowBuilder();
        flowBuilder.setStrict(true);
        UpdatedFlow flow = flowBuilder.build();

        FlowModInput flowMod = FlowConvertor
                .toFlowModInput(flow, EncodeConstants.OF10_VERSION_ID, new BigInteger("42")).build();

        Assert.assertEquals("Wrong version", 1, flowMod.getVersion().intValue());
        Assert.assertEquals("Wrong command", FlowModCommand.OFPFCMODIFYSTRICT, flowMod.getCommand());
    }

    /**
     * Tests {@link FlowConvertor#toFlowModInput(Flow, short, BigInteger)}
     */
    @Test
    public void testInstructionsTranslation() {
        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<>();
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        GoToTableCaseBuilder goToCaseBuilder = new GoToTableCaseBuilder();
        GoToTableBuilder goToBuilder = new GoToTableBuilder();
        goToBuilder.setTableId((short) 1);
        goToCaseBuilder.setGoToTable(goToBuilder.build());
        instructionBuilder.setInstruction(goToCaseBuilder.build());
        instructionBuilder.setOrder(0);
        instructions.add(instructionBuilder.build());
        instructionBuilder = new InstructionBuilder();
        WriteMetadataCaseBuilder metaCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metaBuilder = new WriteMetadataBuilder();
        metaBuilder.setMetadata(new BigInteger("2"));
        metaBuilder.setMetadataMask(new BigInteger("3"));
        metaCaseBuilder.setWriteMetadata(metaBuilder.build());
        instructionBuilder.setInstruction(metaCaseBuilder.build());
        instructionBuilder.setOrder(1);
        instructions.add(instructionBuilder.build());
        instructionBuilder = new InstructionBuilder();
        WriteActionsCaseBuilder writeCaseBuilder = new WriteActionsCaseBuilder();
        WriteActionsBuilder writeBuilder = new WriteActionsBuilder();
        List<Action> actions = new ArrayList<>();
        writeBuilder.setAction(actions);
        writeCaseBuilder.setWriteActions(writeBuilder.build());
        instructionBuilder.setInstruction(writeCaseBuilder.build());
        instructionBuilder.setOrder(2);
        instructions.add(instructionBuilder.build());
        instructionBuilder = new InstructionBuilder();
        ApplyActionsCaseBuilder applyCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder applyBuilder = new ApplyActionsBuilder();
        actions = new ArrayList<>();
        applyBuilder.setAction(actions);
        applyCaseBuilder.setApplyActions(applyBuilder.build());
        instructionBuilder.setInstruction(applyCaseBuilder.build());
        instructionBuilder.setOrder(3);
        instructions.add(instructionBuilder.build());
        instructionBuilder = new InstructionBuilder();
        ClearActionsCaseBuilder clearCaseBuilder = new ClearActionsCaseBuilder();
        ClearActionsBuilder clearBuilder = new ClearActionsBuilder();
        actions = new ArrayList<>();
        clearBuilder.setAction(actions);
        clearCaseBuilder.setClearActions(clearBuilder.build());
        instructionBuilder.setInstruction(clearCaseBuilder.build());
        instructionBuilder.setOrder(4);
        instructions.add(instructionBuilder.build());
        instructionBuilder = new InstructionBuilder();
        MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
        MeterBuilder meterBuilder = new MeterBuilder();
        meterBuilder.setMeterId(new MeterId(5L));
        meterCaseBuilder.setMeter(meterBuilder.build());
        instructionBuilder.setInstruction(meterCaseBuilder.build());
        instructionBuilder.setOrder(5);
        instructions.add(instructionBuilder.build());
        instructionsBuilder.setInstruction(instructions);
        flowBuilder.setInstructions(instructionsBuilder.build());
        AddFlowInput flow = flowBuilder.build();

        FlowModInput flowMod = FlowConvertor
                .toFlowModInput(flow, EncodeConstants.OF10_VERSION_ID, new BigInteger("42")).build();

        Assert.assertEquals("Wrong version", 1, flowMod.getVersion().intValue());
        Assert.assertEquals("Wrong command", FlowModCommand.OFPFCADD, flowMod.getCommand());
        Assert.assertEquals("Wrong instructions size", 6, flowMod.getInstruction().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions
        .grouping.Instruction instruction = flowMod.getInstruction().get(0);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.GotoTable", instruction.getType().getName());
        Assert.assertEquals("Wrong table id", 1, instruction.getAugmentation(TableIdInstruction.class)
                .getTableId().intValue());
        instruction = flowMod.getInstruction().get(1);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.WriteMetadata", instruction.getType().getName());
        Assert.assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 2},
                instruction.getAugmentation(MetadataInstruction.class).getMetadata());
        Assert.assertArrayEquals("Wrong metadata mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                instruction.getAugmentation(MetadataInstruction.class).getMetadataMask());
        instruction = flowMod.getInstruction().get(2);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.WriteActions", instruction.getType().getName());
        Assert.assertEquals("Wrong actions size", 0, instruction.getAugmentation(ActionsInstruction.class)
                .getAction().size());
        instruction = flowMod.getInstruction().get(3);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.ApplyActions", instruction.getType().getName());
        Assert.assertEquals("Wrong actions size", 0, instruction.getAugmentation(ActionsInstruction.class)
                .getAction().size());
        instruction = flowMod.getInstruction().get(4);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.ClearActions", instruction.getType().getName());
        instruction = flowMod.getInstruction().get(5);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.Meter", instruction.getType().getName());
        Assert.assertEquals("Wrong meter id", 5, instruction.getAugmentation(MeterIdInstruction.class)
                .getMeterId().intValue());
    }
}