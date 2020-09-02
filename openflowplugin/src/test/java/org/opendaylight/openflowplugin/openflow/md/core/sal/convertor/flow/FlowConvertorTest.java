/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetVlanIdActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.vlan.id.action._case.SetVlanIdActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowTableRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModCommand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for flow conversion.
 *
 * @author michal.polkorab
 */
public class FlowConvertorTest {
    private ConvertorManager convertorManager;

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
    }

    /**
     * Tests {@link FlowConvertor#convert(Flow, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void test() {
        RemoveFlowInputBuilder flowBuilder = new RemoveFlowInputBuilder();
        flowBuilder.setBarrier(false);
        flowBuilder.setCookie(new FlowCookie(Uint64.valueOf(4)));
        flowBuilder.setCookieMask(new FlowCookie(Uint64.valueOf(5)));
        flowBuilder.setTableId(Uint8.valueOf(6));
        flowBuilder.setStrict(true);
        flowBuilder.setIdleTimeout(Uint16.valueOf(50));
        flowBuilder.setHardTimeout(Uint16.valueOf(500));
        flowBuilder.setPriority(Uint16.valueOf(40));
        flowBuilder.setBufferId(Uint32.valueOf(18));
        flowBuilder.setOutPort(Uint64.valueOf(65535));
        flowBuilder.setOutGroup(Uint32.valueOf(5000));
        flowBuilder.setFlags(null);
        flowBuilder.setMatch(null);
        RemoveFlowInput flow = flowBuilder.build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setDatapathId(Uint64.valueOf(42));

        List<FlowModInputBuilder> flowMod = convert(flow, data);

        Assert.assertEquals("Wrong version", 4, flowMod.get(0).getVersion().intValue());
        Assert.assertEquals("Wrong cookie", 4, flowMod.get(0).getCookie().intValue());
        Assert.assertEquals("Wrong cookie mask", 5, flowMod.get(0).getCookieMask().intValue());
        Assert.assertEquals("Wrong table id", 6, flowMod.get(0).getTableId().getValue().intValue());
        Assert.assertEquals("Wrong command", FlowModCommand.OFPFCDELETESTRICT, flowMod.get(0).getCommand());
        Assert.assertEquals("Wrong idle timeout", 50, flowMod.get(0).getIdleTimeout().intValue());
        Assert.assertEquals("Wrong hard timeout", 500, flowMod.get(0).getHardTimeout().intValue());
        Assert.assertEquals("Wrong priority", 40, flowMod.get(0).getPriority().intValue());
        Assert.assertEquals("Wrong buffer id", 18, flowMod.get(0).getBufferId().intValue());
        Assert.assertEquals("Wrong out port", 65535, flowMod.get(0).getOutPort().getValue().intValue());
        Assert.assertEquals("Wrong out group", 5000, flowMod.get(0).getOutGroup().intValue());
        Assert.assertEquals("Wrong flags", new FlowModFlags(false, false, false, false, false),
                flowMod.get(0).getFlags());
        Assert.assertEquals("Wrong match",
                "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType",
                flowMod.get(0).getMatch().getType().getName());
        Assert.assertEquals("Wrong match entries size", 0, flowMod.get(0).getMatch().nonnullMatchEntry().size());
    }

    /**
     * Tests {@link FlowConvertor#convert(Flow, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testOnlyModifyStrictCommand() {
        UpdatedFlowBuilder flowBuilder = new UpdatedFlowBuilder();
        flowBuilder.setStrict(true);
        UpdatedFlow flow = flowBuilder.build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        data.setDatapathId(Uint64.valueOf(42));

        List<FlowModInputBuilder> flowMod = convert(flow, data);

        Assert.assertEquals("Wrong version", 1, flowMod.get(0).getVersion().intValue());
        Assert.assertEquals("Wrong command", FlowModCommand.OFPFCADD, flowMod.get(0).getCommand());
    }

    /**
     * Tests {@link FlowConvertor#convert(Flow, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testInstructionsTranslation() {
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        GoToTableCaseBuilder goToCaseBuilder = new GoToTableCaseBuilder();
        GoToTableBuilder goToBuilder = new GoToTableBuilder();
        goToBuilder.setTableId(Uint8.ONE);
        goToCaseBuilder.setGoToTable(goToBuilder.build());
        instructionBuilder.setInstruction(goToCaseBuilder.build());
        instructionBuilder.setOrder(0);

        List<Instruction> instructions = new ArrayList<>();
        instructions.add(instructionBuilder.build());
        instructionBuilder = new InstructionBuilder();
        WriteMetadataCaseBuilder metaCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder metaBuilder = new WriteMetadataBuilder();
        metaBuilder.setMetadata(Uint64.valueOf(2));
        metaBuilder.setMetadataMask(Uint64.valueOf(3));
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
        meterBuilder.setMeterId(new MeterId(Uint32.valueOf(5)));
        meterCaseBuilder.setMeter(meterBuilder.build());
        instructionBuilder.setInstruction(meterCaseBuilder.build());
        instructionBuilder.setOrder(5);
        instructions.add(instructionBuilder.build());

        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        instructionsBuilder.setInstruction(instructions);

        AddFlowInputBuilder flowBuilder = new AddFlowInputBuilder();
        flowBuilder.setInstructions(instructionsBuilder.build());
        AddFlowInput flow = flowBuilder.build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        data.setDatapathId(Uint64.valueOf(42));
        List<FlowModInputBuilder> flowMod = convert(flow, data);

        Assert.assertEquals("Wrong version", 1, flowMod.get(0).getVersion().intValue());
        Assert.assertEquals("Wrong command", FlowModCommand.OFPFCADD, flowMod.get(0).getCommand());
        Assert.assertEquals("Wrong instructions size", 6, flowMod.get(0).getInstruction().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions
            .grouping.Instruction instruction = flowMod.get(0).getInstruction().get(0);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        GotoTableCase gotoTableCase = (GotoTableCase) instruction.getInstructionChoice();
        Assert.assertEquals("Wrong table id", 1, gotoTableCase.getGotoTable().getTableId().intValue());
        instruction = flowMod.get(0).getInstruction().get(1);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        WriteMetadataCase writeMetadataCase = (WriteMetadataCase) instruction.getInstructionChoice();
        Assert.assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 2},
                writeMetadataCase.getWriteMetadata().getMetadata());
        Assert.assertArrayEquals("Wrong metadata mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                writeMetadataCase.getWriteMetadata().getMetadataMask());

        instruction = flowMod.get(0).getInstruction().get(2);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        WriteActionsCase writeActionsCase = (WriteActionsCase) instruction.getInstructionChoice();
        Assert.assertEquals("Wrong actions size", 0, writeActionsCase.getWriteActions().nonnullAction().size());
        instruction = flowMod.get(0).getInstruction().get(3);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        ApplyActionsCase applyActionsCase =  (ApplyActionsCase) instruction.getInstructionChoice();
        Assert.assertEquals("Wrong actions size", 0, applyActionsCase.getApplyActions().nonnullAction().size());
        instruction = flowMod.get(0).getInstruction().get(4);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        instruction = flowMod.get(0).getInstruction().get(5);
        Assert.assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.MeterCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        MeterCase meterCase = (MeterCase) instruction.getInstructionChoice();
        Assert.assertEquals("Wrong meter id", 5, meterCase.getMeter().getMeterId().intValue());
    }

    @Test
    public void testCloneAndAugmentFlowWithSetVlanId() {
        MockFlow mockFlow = new MockFlow();
        Action action1 = createAction(
                new SetVlanIdActionCaseBuilder().setSetVlanIdAction(
                        new SetVlanIdActionBuilder().setVlanId(new VlanId(Uint16.TEN)).build())
                        .build(),
                0);

        mockFlow.setMatch(new MatchBuilder().setEthernetMatch(createEthernetMatch()).build());
        mockFlow.setInstructions(toApplyInstruction(Collections.singletonList(action1)));

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setDatapathId(Uint64.ONE);

        List<FlowModInputBuilder> flowModInputBuilders = convert(mockFlow, data);

        Assert.assertEquals(2, flowModInputBuilders.size());

    }

    private List<FlowModInputBuilder> convert(final Flow flow, final VersionDatapathIdConvertorData data) {
        Optional<List<FlowModInputBuilder>> flowModOptional = convertorManager.convert(flow, data);
        Assert.assertTrue("Flow convertor not found", flowModOptional.isPresent());
        return flowModOptional.get();
    }

    private static Action createAction(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionCase,
            final int order) {
        Action action = new ActionBuilder().setOrder(order).setAction(actionCase).build();
        return action;
    }

    private static EthernetMatch createEthernetMatch() {
        return new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(33024))).build())
                .build();
    }

    private static Instructions toApplyInstruction(
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
                .Action> actions) {
        return new InstructionsBuilder().setInstruction(Collections.singletonList(new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(new ApplyActionsBuilder().setAction(actions).build()).build())
                .build())).build();
    }

    private static class MockFlow implements AddFlowInput {
        private Instructions instructions;
        private Match match;

        public void setInstructions(final Instructions instructions) {
            this.instructions = instructions;
        }

        public void setMatch(final Match match) {
            this.match = match;
        }


        @Override
        public FlowRef getFlowRef() {
            return null;
        }

        @Override
        public <E extends Augmentation<AddFlowInput>> E augmentation(final Class<E> augmentationType) {
            return null;
        }

        @Override
        public FlowTableRef getFlowTable() {
            return null;
        }

        @Override
        public Match getMatch() {
            return match;
        }

        @Override
        public Instructions getInstructions() {
            return instructions;
        }

        @Override
        public String getContainerName() {
            return null;
        }

        @Override
        public FlowCookie getCookieMask() {
            return null;
        }

        @Override
        public Uint32 getBufferId() {
            return null;
        }

        @Override
        public Uint64 getOutPort() {
            return null;
        }

        @Override
        public Uint32 getOutGroup() {
            return null;
        }

        @Override
        public org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags getFlags() {
            return null;
        }

        @Override
        public String getFlowName() {
            return null;
        }

        @Override
        public Boolean isInstallHw() {
            return null;
        }

        @Override
        public Boolean isBarrier() {
            return null;
        }

        @Override
        public Boolean isStrict() {
            return null;
        }

        @Override
        public Uint16 getPriority() {
            return null;
        }

        @Override
        public Uint16 getIdleTimeout() {
            return null;
        }

        @Override
        public Uint16 getHardTimeout() {
            return null;
        }

        @Override
        public FlowCookie getCookie() {
            return null;
        }

        @Override
        public Uint8 getTableId() {
            return null;
        }

        @Override
        public NodeRef getNode() {
            return null;
        }

        @Override
        public Uri getTransactionUri() {
            return null;
        }

        @Override
        public Class<AddFlowInput> implementedInterface() {
            return AddFlowInput.class;
        }
    }
}
