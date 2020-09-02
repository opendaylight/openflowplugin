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
import java.util.Collection;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesKey;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class TableFeaturesConvertorTest {
    private static final TablePropertiesBuilder TABLE_PROPERTIES_BUILDER = new TablePropertiesBuilder();
    private static final Map<Class<? extends TableFeaturePropType>, TableFeaturePropType> AUGMENTATIONS_MAP =
            new HashMap<>();
    private static final List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction
        .Instruction> INSTRUCTIONS_LIST = new ArrayList<>();
    private static final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list
        .Action> ACTIONS = new ArrayList<>();
    private static final List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match
        .SetFieldMatch> FIELD_TABLE_FEATURES = new ArrayList<>();

    private static void setupInstructionsList() {
        INSTRUCTIONS_LIST.add(new GoToTableCaseBuilder().build());
        INSTRUCTIONS_LIST.add(new WriteMetadataCaseBuilder().build());
        INSTRUCTIONS_LIST.add(new WriteActionsCaseBuilder().build());
        INSTRUCTIONS_LIST.add(new ApplyActionsCaseBuilder().build());
        INSTRUCTIONS_LIST.add(new ClearActionsCaseBuilder().build());
        INSTRUCTIONS_LIST.add(new MeterCaseBuilder().build());
    }

    private static void setupFieldTableFeatures() {
        int order = 0;
        SetFieldMatchBuilder setFieldMatchBuilder = new SetFieldMatchBuilder();
        setFieldMatchBuilder.setHasMask(true);
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
        setFieldMatchBuilder
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.class);
        FIELD_TABLE_FEATURES.add(setFieldMatchBuilder.build());
    }

    private static void setupActions() {
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder actionBuilder =
                new ActionBuilder();

        int order = 0;
        OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(outputActionCaseBuilder.build()).build());

        GroupActionCaseBuilder groupActionCaseBuilder = new GroupActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(groupActionCaseBuilder.build()).build());

        CopyTtlOutCaseBuilder copyTtlOutCaseBuilder = new CopyTtlOutCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(copyTtlOutCaseBuilder.build()).build());

        CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(copyTtlInCaseBuilder.build()).build());

        SetMplsTtlActionCaseBuilder setMplsTtlActionCaseBuilder = new SetMplsTtlActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(setMplsTtlActionCaseBuilder.build()).build());

        DecMplsTtlCaseBuilder decMplsTtlCaseBuilder = new DecMplsTtlCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(decMplsTtlCaseBuilder.build()).build());

        PushVlanActionCaseBuilder pushVlanActionCaseBuilder = new PushVlanActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(pushVlanActionCaseBuilder.build()).build());

        PopVlanActionCaseBuilder popVlanActionCaseBuilder = new PopVlanActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(popVlanActionCaseBuilder.build()).build());

        PushMplsActionCaseBuilder pushMplsActionCaseBuilder = new PushMplsActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(pushMplsActionCaseBuilder.build()).build());

        PopMplsActionCaseBuilder popMplsActionCaseBuilder = new PopMplsActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(popMplsActionCaseBuilder.build()).build());

        SetQueueActionCaseBuilder setQueueActionCaseBuilder = new SetQueueActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(setQueueActionCaseBuilder.build()).build());

        SetNwTtlActionCaseBuilder setNwTtlActionCaseBuilder = new SetNwTtlActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(setNwTtlActionCaseBuilder.build()).build());

        DecNwTtlCaseBuilder decNwTtlCaseBuilder = new DecNwTtlCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(decNwTtlCaseBuilder.build()).build());

        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(setFieldCaseBuilder.build()).build());

        PushPbbActionCaseBuilder pushPbbActionCaseBuilder = new PushPbbActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(pushPbbActionCaseBuilder.build()).build());

        PopPbbActionCaseBuilder popPbbActionCaseBuilder = new PopPbbActionCaseBuilder();
        ACTIONS.add(actionBuilder.setOrder(order++).setAction(popPbbActionCaseBuilder.build()).build());

    }

    @Before
    public void setupTest() {
        setupInstructionsList();
        setupActions();
        setupFieldTableFeatures();
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        List<Instruction> instructions = new ArrayList<>();
        int order = 0;
        for (var element : INSTRUCTIONS_LIST) {
            instructions.add(new InstructionBuilder()
                .setOrder(order++)
                .setInstruction(element).build());
        }
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature
            .prop.type.instructions.InstructionsBuilder instructionsBuilder1 =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table
                    .feature.prop.type.instructions.InstructionsBuilder();
        instructionsBuilder1.setInstruction(instructions);
        instructionsBuilder.setInstructions(instructionsBuilder1.build());

        AUGMENTATIONS_MAP.put(Instructions.class, instructionsBuilder.build());

        InstructionsMissBuilder instructionsMissBuilder = new InstructionsMissBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.instructions.miss.InstructionsMissBuilder instructionsMissBuilder1 =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table
                    .feature.prop.type.instructions.miss.InstructionsMissBuilder();
        instructionsMissBuilder1.setInstruction(instructions);
        instructionsMissBuilder.setInstructionsMiss(instructionsMissBuilder1.build());
        AUGMENTATIONS_MAP.put(InstructionsMiss.class, instructionsMissBuilder.build());

        NextTableBuilder nextTableBuilder = new NextTableBuilder();
        AUGMENTATIONS_MAP.put(NextTable.class, nextTableBuilder.build());

        NextTableMissBuilder nextTableMissBuilder = new NextTableMissBuilder();
        AUGMENTATIONS_MAP.put(NextTableMiss.class, nextTableMissBuilder.build());

        WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
        AUGMENTATIONS_MAP.put(WriteActions.class, writeActionsBuilder.build());

        WriteActionsMissBuilder writeActionsMissBuilder = new WriteActionsMissBuilder();
        AUGMENTATIONS_MAP.put(WriteActionsMiss.class, writeActionsMissBuilder.build());

        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        AUGMENTATIONS_MAP.put(ApplyActions.class, applyActionsBuilder.build());

        ApplyActionsMissBuilder applyActionsMissBuilder = new ApplyActionsMissBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.apply.actions.miss.ApplyActionsMissBuilder applyActionsMissBuilder1 =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table
                    .feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder();
        applyActionsMissBuilder1.setAction(ACTIONS);
        applyActionsMissBuilder.setApplyActionsMiss(applyActionsMissBuilder1.build());
        AUGMENTATIONS_MAP.put(ApplyActionsMiss.class, applyActionsMissBuilder.build());

        MatchBuilder matchBuilder = new MatchBuilder();
        AUGMENTATIONS_MAP.put(Match.class, matchBuilder.build());

        WildcardsBuilder wildcardsBuilder = new WildcardsBuilder();
        AUGMENTATIONS_MAP.put(Wildcards.class, wildcardsBuilder.build());

        WriteSetfieldBuilder writeSetfieldBuilder = new WriteSetfieldBuilder();
        AUGMENTATIONS_MAP.put(WriteSetfield.class, writeSetfieldBuilder.build());

        WriteSetfieldMissBuilder writeSetfieldMissBuilder = new WriteSetfieldMissBuilder();
        AUGMENTATIONS_MAP.put(WriteSetfieldMiss.class, writeSetfieldMissBuilder.build());

        ApplySetfieldBuilder applySetfieldBuilder = new ApplySetfieldBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.apply.setfield.ApplySetfieldBuilder applySetfieldBuilder1 =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table
                    .feature.prop.type.apply.setfield.ApplySetfieldBuilder();
        applySetfieldBuilder1.setSetFieldMatch(FIELD_TABLE_FEATURES);
        applySetfieldBuilder.setApplySetfield(applySetfieldBuilder1.build());
        AUGMENTATIONS_MAP.put(ApplySetfield.class, applySetfieldBuilder.build());

        ApplySetfieldMissBuilder applySetfieldMissBuilder = new ApplySetfieldMissBuilder();
        AUGMENTATIONS_MAP.put(ApplySetfieldMiss.class, applySetfieldMissBuilder.build());
    }

    @Test
    public void testToTableFeaturesRequest() {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures>
            tableFeaturesList = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder
            tableFeaturesBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table
                .features.TableFeaturesBuilder();
        for (int i = 0; i < 10; i++) {
            tableFeaturesBuilder.setTableId(Uint8.valueOf(i));
            tableFeaturesBuilder.setName(String.format("table:%d", i));
            tableFeaturesBuilder.setMetadataMatch(Uint64.ONE);
            tableFeaturesBuilder.setMetadataWrite(Uint64.ONE);
            tableFeaturesBuilder.setMaxEntries(Uint32.valueOf(1 + 10 * i));
            tableFeaturesBuilder.setConfig(new TableConfig(false));
            tableFeaturesBuilder.setTableProperties(getTableProperties());
            tableFeaturesList.add(tableFeaturesBuilder.build());
        }

        TableFeatures tableFeatures = new UpdatedTableBuilder()
                .setTableFeatures(tableFeaturesList)
                .build();

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();

        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request
            .multipart.request.body.multipart.request.table.features._case.multipart.request.table.features
                .TableFeatures>> tableFeaturesesOptional =
                    convertorManager.convert(tableFeatures, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));

        assertNotNull(tableFeatures);
        assertEquals(10, tableFeatures.getTableFeatures().size());
        Collection<TableFeatureProperties> tableFeaturePropertieses = tableFeatures.nonnullTableFeatures().values()
                .iterator().next().getTableProperties().nonnullTableFeatureProperties().values();
        assertEquals(AUGMENTATIONS_MAP.size() + 1, tableFeaturePropertieses.size());

        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature
            .prop.type.ApplyActionsMiss applyActionsMiss = null;
        for (var featureProp : tableFeaturePropertieses) {
            var prop = featureProp.getTableFeaturePropType();
            if (prop.implementedInterface().isAssignableFrom(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types
                    .rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss.class)) {
                applyActionsMiss = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                    .prop.type.table.feature.prop.type.ApplyActionsMiss) prop;
                break;
            }
        }

        assertNotNull(applyActionsMiss);
        assertEquals(ACTIONS.size(), applyActionsMiss.getApplyActionsMiss().getAction().size());

    }

    private static TableProperties getTableProperties() {
        TableFeaturePropertiesBuilder tableFeaturePropertiesBuilder = new TableFeaturePropertiesBuilder();
        List<TableFeatureProperties> tableFeaturePropertieses = new ArrayList<>();
        int counter = 0;
        int order = 0;
        for (Entry<Class<? extends TableFeaturePropType>, TableFeaturePropType> entry : AUGMENTATIONS_MAP.entrySet()) {
            counter++;
            tableFeaturePropertiesBuilder.setTableFeaturePropType(entry.getValue());
            tableFeaturePropertiesBuilder.setOrder(counter);
            tableFeaturePropertiesBuilder.withKey(new TableFeaturePropertiesKey(order++));
            tableFeaturePropertieses.add(tableFeaturePropertiesBuilder.build());
        }
        tableFeaturePropertieses.add(
                tableFeaturePropertiesBuilder.withKey(new TableFeaturePropertiesKey(order++)).build());
        TABLE_PROPERTIES_BUILDER.setTableFeatureProperties(tableFeaturePropertieses);
        return TABLE_PROPERTIES_BUILDER.build();
    }
}
