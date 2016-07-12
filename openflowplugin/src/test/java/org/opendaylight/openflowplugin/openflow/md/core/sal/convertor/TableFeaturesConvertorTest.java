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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WildcardsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TableProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;

public class TableFeaturesConvertorTest {
    private static final TablePropertiesBuilder tablePropertiesBuilder = new TablePropertiesBuilder();
    private static final Map<Class<? extends TableFeaturePropType>, TableFeaturePropType> augmentationsMap = new HashMap<>();
    private static final List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction> instructionsList = new ArrayList<>();
    private static final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions = new ArrayList<>();
    private static final List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> fieldTableFeatures = new ArrayList<>();

    private static void setupInstructionsList() {
        instructionsList.add(new GoToTableCaseBuilder().build());
        instructionsList.add(new WriteMetadataCaseBuilder().build());
        instructionsList.add(new WriteActionsCaseBuilder().build());
        instructionsList.add(new ApplyActionsCaseBuilder().build());
        instructionsList.add(new ClearActionsCaseBuilder().build());
        instructionsList.add(new MeterCaseBuilder().build());
    }

    private static void setupFieldTableFeatures() {
        SetFieldMatchBuilder setFieldMatchBuilder = new SetFieldMatchBuilder();
        setFieldMatchBuilder.setHasMask(true);
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder.setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.class);
        fieldTableFeatures.add(setFieldMatchBuilder.build());
    }

    private static void setupActions() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder _actionBuilder = new ActionBuilder();

        OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
        actions.add(_actionBuilder.setAction(outputActionCaseBuilder.build()).build());

        GroupActionCaseBuilder groupActionCaseBuilder = new GroupActionCaseBuilder();
        actions.add(_actionBuilder.setAction(groupActionCaseBuilder.build()).build());

        CopyTtlOutCaseBuilder copyTtlOutCaseBuilder = new CopyTtlOutCaseBuilder();
        actions.add(_actionBuilder.setAction(copyTtlOutCaseBuilder.build()).build());

        CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
        actions.add(_actionBuilder.setAction(copyTtlInCaseBuilder.build()).build());

        SetMplsTtlActionCaseBuilder setMplsTtlActionCaseBuilder = new SetMplsTtlActionCaseBuilder();
        actions.add(_actionBuilder.setAction(setMplsTtlActionCaseBuilder.build()).build());

        DecMplsTtlCaseBuilder decMplsTtlCaseBuilder = new DecMplsTtlCaseBuilder();
        actions.add(_actionBuilder.setAction(decMplsTtlCaseBuilder.build()).build());

        PushVlanActionCaseBuilder pushVlanActionCaseBuilder = new PushVlanActionCaseBuilder();
        actions.add(_actionBuilder.setAction(pushVlanActionCaseBuilder.build()).build());

        PopVlanActionCaseBuilder popVlanActionCaseBuilder = new PopVlanActionCaseBuilder();
        actions.add(_actionBuilder.setAction(popVlanActionCaseBuilder.build()).build());

        PushMplsActionCaseBuilder pushMplsActionCaseBuilder = new PushMplsActionCaseBuilder();
        actions.add(_actionBuilder.setAction(pushMplsActionCaseBuilder.build()).build());

        PopMplsActionCaseBuilder popMplsActionCaseBuilder = new PopMplsActionCaseBuilder();
        actions.add(_actionBuilder.setAction(popMplsActionCaseBuilder.build()).build());

        SetQueueActionCaseBuilder setQueueActionCaseBuilder = new SetQueueActionCaseBuilder();
        actions.add(_actionBuilder.setAction(setQueueActionCaseBuilder.build()).build());

        SetNwTtlActionCaseBuilder setNwTtlActionCaseBuilder = new SetNwTtlActionCaseBuilder();
        actions.add(_actionBuilder.setAction(setNwTtlActionCaseBuilder.build()).build());

        DecNwTtlCaseBuilder decNwTtlCaseBuilder = new DecNwTtlCaseBuilder();
        actions.add(_actionBuilder.setAction(decNwTtlCaseBuilder.build()).build());

        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        actions.add(_actionBuilder.setAction(setFieldCaseBuilder.build()).build());

        PushPbbActionCaseBuilder pushPbbActionCaseBuilder = new PushPbbActionCaseBuilder();
        actions.add(_actionBuilder.setAction(pushPbbActionCaseBuilder.build()).build());

        PopPbbActionCaseBuilder popPbbActionCaseBuilder = new PopPbbActionCaseBuilder();
        actions.add(_actionBuilder.setAction(popPbbActionCaseBuilder.build()).build());

    }

    @Before
    public void setupTest() {
        setupInstructionsList();
        setupActions();
        setupFieldTableFeatures();
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<>();
        for (int i = 0; i < instructionsList.size(); i++) {
            InstructionBuilder instructionBuilder = new InstructionBuilder();
            instructionBuilder.setInstruction(instructionsList.get(i));
            instructions.add(instructionBuilder.build());
        }
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.InstructionsBuilder instructionsBuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.InstructionsBuilder();
        instructionsBuilder1.setInstruction(instructions);
        instructionsBuilder.setInstructions(instructionsBuilder1.build());

        augmentationsMap.put(Instructions.class, instructionsBuilder.build());

        InstructionsMissBuilder instructionsMissBuilder = new InstructionsMissBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder instructionsMissBuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder();
        instructionsMissBuilder1.setInstruction(instructions);
        instructionsMissBuilder.setInstructionsMiss(instructionsMissBuilder1.build());
        augmentationsMap.put(InstructionsMiss.class, instructionsMissBuilder.build());

        NextTableBuilder nextTableBuilder = new NextTableBuilder();
        augmentationsMap.put(NextTable.class, nextTableBuilder.build());

        NextTableMissBuilder nextTableMissBuilder = new NextTableMissBuilder();
        augmentationsMap.put(NextTableMiss.class, nextTableMissBuilder.build());

        WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
        augmentationsMap.put(WriteActions.class, writeActionsBuilder.build());

        WriteActionsMissBuilder writeActionsMissBuilder = new WriteActionsMissBuilder();
        augmentationsMap.put(WriteActionsMiss.class, writeActionsMissBuilder.build());

        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        augmentationsMap.put(ApplyActions.class, applyActionsBuilder.build());

        ApplyActionsMissBuilder applyActionsMissBuilder = new ApplyActionsMissBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder applyActionsMissBuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder();
        applyActionsMissBuilder1.setAction(actions);
        applyActionsMissBuilder.setApplyActionsMiss(applyActionsMissBuilder1.build());
        augmentationsMap.put(ApplyActionsMiss.class, applyActionsMissBuilder.build());

        MatchBuilder matchBuilder = new MatchBuilder();
        augmentationsMap.put(Match.class, matchBuilder.build());

        WildcardsBuilder wildcardsBuilder = new WildcardsBuilder();
        augmentationsMap.put(Wildcards.class, wildcardsBuilder.build());

        WriteSetfieldBuilder writeSetfieldBuilder = new WriteSetfieldBuilder();
        augmentationsMap.put(WriteSetfield.class, writeSetfieldBuilder.build());

        WriteSetfieldMissBuilder writeSetfieldMissBuilder = new WriteSetfieldMissBuilder();
        augmentationsMap.put(WriteSetfieldMiss.class, writeSetfieldMissBuilder.build());

        ApplySetfieldBuilder applySetfieldBuilder = new ApplySetfieldBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder applySetfieldBuilder1 = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder();
        applySetfieldBuilder1.setSetFieldMatch(fieldTableFeatures);
        applySetfieldBuilder.setApplySetfield(applySetfieldBuilder1.build());
        augmentationsMap.put(ApplySetfield.class, applySetfieldBuilder.build());

        ApplySetfieldMissBuilder applySetfieldMissBuilder = new ApplySetfieldMissBuilder();
        augmentationsMap.put(ApplySetfieldMiss.class, applySetfieldMissBuilder.build());
    }

    @Test
    /**
     * Basic functionality test method for {@link TableFeaturesConvertor#toTableFeaturesRequest(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures)}
     */
    public void testToTableFeaturesRequest() throws Exception {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures> tableFeaturesList = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder tableFeaturesBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder();
        for (int i = 0; i < 10; i++) {
            tableFeaturesBuilder.setTableId((short) i);
            tableFeaturesBuilder.setName(String.format("table:%d", i));
            tableFeaturesBuilder.setMetadataMatch(BigInteger.ONE);
            tableFeaturesBuilder.setMetadataWrite(BigInteger.ONE);
            tableFeaturesBuilder.setMaxEntries((long) 1 + (10 * i));
            tableFeaturesBuilder.setConfig(new TableConfig(false));
            tableFeaturesBuilder.setTableProperties(getTableProperties());
            tableFeaturesList.add(tableFeaturesBuilder.build());
        }

        TableFeatures tableFeatures = new UpdatedTableBuilder()
                .setTableFeatures(tableFeaturesList)
                .build();

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures>> tableFeaturesesOptional =
                convertorManager.convert(tableFeatures, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures> tableFeatureses =
                tableFeaturesesOptional.orElse(Collections.emptyList());

        assertNotNull(tableFeatures);
        assertEquals(10, tableFeatures.getTableFeatures().size());
        List<TableFeatureProperties> tableFeaturePropertieses = tableFeatures.getTableFeatures().get(0).getTableProperties().getTableFeatureProperties();
        assertEquals(augmentationsMap.size() + 1, tableFeaturePropertieses.size());

        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss applyActionsMiss = null;
        for (int i = 0; i < tableFeaturePropertieses.size(); i++) {
            if (tableFeaturePropertieses.get(i).getTableFeaturePropType().getImplementedInterface().isAssignableFrom(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss.class)) {
                applyActionsMiss = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss) tableFeaturePropertieses.get(i).getTableFeaturePropType();
                break;
            }
        }

        assertNotNull(applyActionsMiss);
        assertEquals(actions.size(), applyActionsMiss.getApplyActionsMiss().getAction().size());

    }

    private static TableProperties getTableProperties() {
        TableFeaturePropertiesBuilder tableFeaturePropertiesBuilder = new TableFeaturePropertiesBuilder();
        List<TableFeatureProperties> tableFeaturePropertieses = new ArrayList<>();
        int counter = 0;
        for (Entry<Class<? extends TableFeaturePropType>, TableFeaturePropType> entry : augmentationsMap.entrySet()) {
            counter++;
            tableFeaturePropertiesBuilder.setTableFeaturePropType(entry.getValue());
            tableFeaturePropertiesBuilder.setOrder(counter);
            tableFeaturePropertieses.add(tableFeaturePropertiesBuilder.build());
        }
        tableFeaturePropertieses.add(tableFeaturePropertiesBuilder.build());
        tablePropertiesBuilder.setTableFeatureProperties(tableFeaturePropertieses);
        return tablePropertiesBuilder.build();
    }
}
