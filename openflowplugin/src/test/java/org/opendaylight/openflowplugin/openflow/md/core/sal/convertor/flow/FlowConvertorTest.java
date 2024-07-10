/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable;
import org.opendaylight.yangtools.binding.util.BindingMap;
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
        RemoveFlowInput flow = new RemoveFlowInputBuilder()
            .setBarrier(false)
            .setCookie(new FlowCookie(Uint64.valueOf(4)))
            .setCookieMask(new FlowCookie(Uint64.valueOf(5)))
            .setTableId(Uint8.valueOf(6))
            .setStrict(true)
            .setIdleTimeout(Uint16.valueOf(50))
            .setHardTimeout(Uint16.valueOf(500))
            .setPriority(Uint16.valueOf(40))
            .setBufferId(Uint32.valueOf(18))
            .setOutPort(Uint64.valueOf(65535))
            .setOutGroup(Uint32.valueOf(5000))
            .setFlags(null)
            .setMatch(null)
            .build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setDatapathId(Uint64.valueOf(42));

        List<FlowModInputBuilder> flowMod = convert(flow, data);

        assertEquals("Wrong version", 4, flowMod.get(0).getVersion().intValue());
        assertEquals("Wrong cookie", 4, flowMod.get(0).getCookie().intValue());
        assertEquals("Wrong cookie mask", 5, flowMod.get(0).getCookieMask().intValue());
        assertEquals("Wrong table id", 6, flowMod.get(0).getTableId().getValue().intValue());
        assertEquals("Wrong command", FlowModCommand.OFPFCDELETESTRICT, flowMod.get(0).getCommand());
        assertEquals("Wrong idle timeout", 50, flowMod.get(0).getIdleTimeout().intValue());
        assertEquals("Wrong hard timeout", 500, flowMod.get(0).getHardTimeout().intValue());
        assertEquals("Wrong priority", 40, flowMod.get(0).getPriority().intValue());
        assertEquals("Wrong buffer id", 18, flowMod.get(0).getBufferId().intValue());
        assertEquals("Wrong out port", 65535, flowMod.get(0).getOutPort().getValue().intValue());
        assertEquals("Wrong out group", 5000, flowMod.get(0).getOutGroup().intValue());
        assertEquals("Wrong flags", new FlowModFlags(false, false, false, false, false), flowMod.get(0).getFlags());
        assertEquals("Wrong match",
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType.VALUE,
                flowMod.get(0).getMatch().getType());
        assertEquals("Wrong match entries size", 0, flowMod.get(0).getMatch().nonnullMatchEntry().size());
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

        assertEquals("Wrong version", 1, flowMod.get(0).getVersion().intValue());
        assertEquals("Wrong command", FlowModCommand.OFPFCADD, flowMod.get(0).getCommand());
    }

    /**
     * Tests {@link FlowConvertor#convert(Flow, VersionDatapathIdConvertorData)} }.
     */
    @Test
    public void testInstructionsTranslation() {
        AddFlowInput flow = new AddFlowInputBuilder()
            .setInstructions(new InstructionsBuilder()
                .setInstruction(BindingMap.ordered(
                    new InstructionBuilder().setOrder(0).setInstruction(new GoToTableCaseBuilder()
                        .setGoToTable(new GoToTableBuilder().setTableId(Uint8.ONE).build())
                        .build())
                    .build(),
                    new InstructionBuilder().setOrder(1).setInstruction(new WriteMetadataCaseBuilder()
                        .setWriteMetadata(new WriteMetadataBuilder()
                            .setMetadata(Uint64.valueOf(2))
                            .setMetadataMask(Uint64.valueOf(3))
                            .build())
                        .build())
                    .build(),
                    new InstructionBuilder().setOrder(2).setInstruction(new WriteActionsCaseBuilder()
                        .setWriteActions(new WriteActionsBuilder().setAction(Map.of()).build())
                        .build())
                    .build(),
                    new InstructionBuilder().setOrder(3).setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(new ApplyActionsBuilder().setAction(Map.of()).build())
                        .build())
                    .build(),
                    new InstructionBuilder().setOrder(4).setInstruction(new ClearActionsCaseBuilder()
                        .setClearActions(new ClearActionsBuilder().setAction(Map.of()).build())
                        .build())
                    .build(),
                    new InstructionBuilder().setOrder(5).setInstruction(new MeterCaseBuilder()
                        .setMeter(new MeterBuilder().setMeterId(new MeterId(Uint32.valueOf(5))).build())
                        .build())
                    .build()))
                .build())
            .build();

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        data.setDatapathId(Uint64.valueOf(42));
        List<FlowModInputBuilder> flowMod = convert(flow, data);

        assertEquals("Wrong version", 1, flowMod.get(0).getVersion().intValue());
        assertEquals("Wrong command", FlowModCommand.OFPFCADD, flowMod.get(0).getCommand());
        assertEquals("Wrong instructions size", 6, flowMod.get(0).getInstruction().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions
            .grouping.Instruction instruction = flowMod.get(0).getInstruction().get(0);
        assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        GotoTableCase gotoTableCase = (GotoTableCase) instruction.getInstructionChoice();
        assertEquals("Wrong table id", 1, gotoTableCase.getGotoTable().getTableId().intValue());
        instruction = flowMod.get(0).getInstruction().get(1);
        assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        WriteMetadataCase writeMetadataCase = (WriteMetadataCase) instruction.getInstructionChoice();
        assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 2},
                writeMetadataCase.getWriteMetadata().getMetadata());
        assertArrayEquals("Wrong metadata mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                writeMetadataCase.getWriteMetadata().getMetadataMask());

        instruction = flowMod.get(0).getInstruction().get(2);
        assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        WriteActionsCase writeActionsCase = (WriteActionsCase) instruction.getInstructionChoice();
        assertEquals("Wrong actions size", 0, writeActionsCase.getWriteActions().nonnullAction().size());
        instruction = flowMod.get(0).getInstruction().get(3);
        assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        ApplyActionsCase applyActionsCase =  (ApplyActionsCase) instruction.getInstructionChoice();
        assertEquals("Wrong actions size", 0, applyActionsCase.getApplyActions().nonnullAction().size());
        instruction = flowMod.get(0).getInstruction().get(4);
        assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        instruction = flowMod.get(0).getInstruction().get(5);
        assertEquals("Wrong type", "org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common"
                + ".instruction.rev130731.instruction.grouping.instruction.choice.MeterCase",
                instruction.getInstructionChoice().implementedInterface().getName());
        MeterCase meterCase = (MeterCase) instruction.getInstructionChoice();
        assertEquals("Wrong meter id", 5, meterCase.getMeter().getMeterId().intValue());
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
        mockFlow.setInstructions(new InstructionsBuilder()
            .setInstruction(BindingMap.of(new InstructionBuilder()
                .setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder()
                    .setApplyActions(new ApplyActionsBuilder().setAction(BindingMap.of(action1)).build())
                    .build())
                .build()))
            .build());

        VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_3);
        data.setDatapathId(Uint64.ONE);

        List<FlowModInputBuilder> flowModInputBuilders = convert(mockFlow, data);

        assertEquals(2, flowModInputBuilders.size());
    }

    private List<FlowModInputBuilder> convert(final Flow flow, final VersionDatapathIdConvertorData data) {
        Optional<List<FlowModInputBuilder>> flowModOptional = convertorManager.convert(flow, data);
        assertTrue("Flow convertor not found", flowModOptional.isPresent());
        return flowModOptional.orElseThrow();
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

    private static final class MockFlow extends AbstractAugmentable<AddFlowInput> implements AddFlowInput {
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
        public Boolean getInstallHw() {
            return null;
        }

        @Override
        public Boolean getBarrier() {
            return null;
        }

        @Override
        public Boolean getStrict() {
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
        public Match nonnullMatch() {
            return Objects.requireNonNullElse(getMatch(), MatchBuilder.empty());
        }

        @Override
        public Instructions nonnullInstructions() {
            return Objects.requireNonNullElse(getInstructions(), InstructionsBuilder.empty());
        }

        @Override
        public int hashCode() {
            return Objects.hash(instructions, match);
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("instructions", instructions).add("match", match).toString();
        }
    }
}
