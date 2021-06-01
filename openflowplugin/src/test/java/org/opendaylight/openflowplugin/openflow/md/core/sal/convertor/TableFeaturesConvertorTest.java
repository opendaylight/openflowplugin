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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchKey;
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
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public class TableFeaturesConvertorTest {
    private static final Map<SetFieldMatchKey, SetFieldMatch> FIELD_TABLE_FEATURES =
        BindingMap.<SetFieldMatchKey, SetFieldMatch>ordered(
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.class)
                .build(),
            new SetFieldMatchBuilder().setHasMask(true)
                .setMatchType(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.class)
                .build());

    private static final Map<Class<? extends TableFeaturePropType>, TableFeaturePropType> AUGMENTATIONS_MAP =
        new HashMap<>();

    static {
        var instructions = BindingMap.<InstructionKey, Instruction>ordered(
            new InstructionBuilder().setOrder(0).setInstruction(new GoToTableCaseBuilder().build()).build(),
            new InstructionBuilder().setOrder(1).setInstruction(new WriteMetadataCaseBuilder().build()).build(),
            new InstructionBuilder().setOrder(2).setInstruction(new WriteActionsCaseBuilder().build()).build(),
            new InstructionBuilder().setOrder(3).setInstruction(new ApplyActionsCaseBuilder().build()).build(),
            new InstructionBuilder().setOrder(4).setInstruction(new ClearActionsCaseBuilder().build()).build(),
            new InstructionBuilder().setOrder(5).setInstruction(new MeterCaseBuilder().build()).build());

        AUGMENTATIONS_MAP.put(Instructions.class, new InstructionsBuilder()
            .setInstructions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop
                .type.table.feature.prop.type.instructions.InstructionsBuilder()
                    .setInstruction(instructions)
                    .build())
            .build());
        AUGMENTATIONS_MAP.put(InstructionsMiss.class, new InstructionsMissBuilder()
            .setInstructionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                .prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder()
                    .setInstruction(instructions)
                    .build())
            .build());
        AUGMENTATIONS_MAP.put(NextTable.class, new NextTableBuilder().build());
        AUGMENTATIONS_MAP.put(NextTableMiss.class, new NextTableMissBuilder().build());
        AUGMENTATIONS_MAP.put(WriteActions.class, new WriteActionsBuilder().build());
        AUGMENTATIONS_MAP.put(WriteActionsMiss.class, new WriteActionsMissBuilder().build());
        AUGMENTATIONS_MAP.put(ApplyActions.class, new ApplyActionsBuilder().build());
        AUGMENTATIONS_MAP.put(ApplyActionsMiss.class, new ApplyActionsMissBuilder()
            .setApplyActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature
                .prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder()
                    .setAction(BindingMap.ordered(
                        new ActionBuilder().setOrder(0).setAction(new OutputActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(1).setAction(new GroupActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(2).setAction(new CopyTtlOutCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(3).setAction(new CopyTtlInCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(4).setAction(new SetMplsTtlActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(5).setAction(new DecMplsTtlCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(6).setAction(new PushVlanActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(7).setAction(new PopVlanActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(8).setAction(new PushMplsActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(9).setAction(new PopMplsActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(10).setAction(new SetQueueActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(11).setAction(new SetNwTtlActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(12).setAction(new DecNwTtlCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(13).setAction(new SetFieldCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(14).setAction(new PushPbbActionCaseBuilder().build()).build(),
                        new ActionBuilder().setOrder(15).setAction(new PopPbbActionCaseBuilder().build()).build()))
                    .build())
            .build());
        AUGMENTATIONS_MAP.put(Match.class, new MatchBuilder().build());
        AUGMENTATIONS_MAP.put(Wildcards.class, new WildcardsBuilder().build());
        AUGMENTATIONS_MAP.put(WriteSetfield.class, new WriteSetfieldBuilder().build());
        AUGMENTATIONS_MAP.put(WriteSetfieldMiss.class, new WriteSetfieldMissBuilder().build());
        AUGMENTATIONS_MAP.put(ApplySetfield.class, new ApplySetfieldBuilder()
            .setApplySetfield(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop
                .type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder()
                    .setSetFieldMatch(FIELD_TABLE_FEATURES)
                    .build())
            .build());
        AUGMENTATIONS_MAP.put(ApplySetfieldMiss.class, new ApplySetfieldMissBuilder().build());
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
                .setTableFeatures(BindingMap.ordered(tableFeaturesList))
                .build();

        // FIXME: this seems to be completely unused!
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request
            .multipart.request.body.multipart.request.table.features._case.multipart.request.table.features
                .TableFeatures>> tableFeaturesesOptional =
                    convertorManager.convert(tableFeatures, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));

        assertNotNull(tableFeatures);
        assertEquals(10, tableFeatures.nonnullTableFeatures().size());
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
        assertEquals(16, applyActionsMiss.getApplyActionsMiss().getAction().size());
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
        return new TablePropertiesBuilder()
            .setTableFeatureProperties(BindingMap.ordered(tableFeaturePropertieses))
            .build();
    }
}
