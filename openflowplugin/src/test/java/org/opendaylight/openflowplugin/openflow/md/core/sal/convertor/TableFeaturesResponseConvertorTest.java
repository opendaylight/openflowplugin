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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.group._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.pbb._case.PushPbbActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.push.vlan._case.PushVlanActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.mpls.ttl._case.SetMplsTtlActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.nw.src._case.SetNwSrcActionBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;

/**
 * @author michal.polkorab
 */
public class TableFeaturesResponseConvertorTest {

    /**
     * Incorrect / empty input test
     */
    @Test
    public void test() {
        List<TableFeatures> list = convert(null);
        Assert.assertEquals("Returned list is not empty", 0, list.size());
    }

    /**
     * Incorrect / empty input test
     */
    @Test
    public void test2() {
        MultipartReplyTableFeaturesBuilder builder = new MultipartReplyTableFeaturesBuilder();
        List<TableFeatures> list = convert(builder.build());
        Assert.assertEquals("Returned list is not empty", 0, list.size());
    }

    /**
     * Test correct input - without table properties
     */
    @Test
    public void testWithMPTableFeature() {
        MultipartReplyTableFeaturesBuilder builder = new MultipartReplyTableFeaturesBuilder();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart
                .reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features
                .TableFeatures> features = new ArrayList<>();
        TableFeaturesBuilder featuresBuilder = new TableFeaturesBuilder();
        featuresBuilder.setTableId((short) 5);
        featuresBuilder.setName("Aloha");
        byte[] metaMatch = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        featuresBuilder.setMetadataMatch(metaMatch);
        byte[] metaWrite = new byte[]{8, 9, 10, 11, 12, 13, 14, 15};
        featuresBuilder.setMetadataWrite(metaWrite);
        featuresBuilder.setConfig(new TableConfig(false));
        featuresBuilder.setMaxEntries(42L);
        features.add(featuresBuilder.build());
        builder.setTableFeatures(features);
        List<TableFeatures> list = convert(builder.build());
        Assert.assertEquals("Returned empty list", 1, list.size());
        TableFeatures feature = list.get(0);
        Assert.assertEquals("Wrong table-id", 5, feature.getTableId().intValue());
        Assert.assertEquals("Wrong name", "Aloha", feature.getName());
        Assert.assertEquals("Wrong metadata match", new BigInteger(1, metaMatch), feature.getMetadataMatch());
        Assert.assertEquals("Wrong metadata write", new BigInteger(1, metaWrite), feature.getMetadataWrite());
        Assert.assertEquals("Wrong config", false, feature.getConfig().isDEPRECATEDMASK());
        Assert.assertEquals("Wrong max-entries", 42, feature.getMaxEntries().intValue());
        Assert.assertEquals("Wrong properties", 0, feature.getTableProperties().getTableFeatureProperties().size());
    }

    /**
     * Test correct input
     */
    @Test
    public void testWithMPTableFeatureWithProperties() {
        MultipartReplyTableFeaturesBuilder builder = new MultipartReplyTableFeaturesBuilder();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart
                .reply.multipart.reply.body.multipart.reply.table.features._case.multipart.reply.table.features
                .TableFeatures> features = new ArrayList<>();

        TableFeaturesBuilder featuresBuilder = new TableFeaturesBuilder();
        featuresBuilder.setTableId((short) 5);
        featuresBuilder.setName("Aloha");
        byte[] metaMatch = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        featuresBuilder.setMetadataMatch(metaMatch);
        byte[] metaWrite = new byte[]{8, 9, 10, 11, 12, 13, 14, 15};
        featuresBuilder.setMetadataWrite(metaWrite);
        featuresBuilder.setConfig(new TableConfig(false));
        featuresBuilder.setMaxEntries(42L);

        List<TableFeatureProperties> properties = new ArrayList<>();
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();

        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLES);
        NextTableRelatedTableFeaturePropertyBuilder nextPropBuilder =
                new NextTableRelatedTableFeaturePropertyBuilder();
        List<NextTableIds> nextIds = new ArrayList<>();
        nextIds.add(new NextTableIdsBuilder().setTableId((short) 1).build());
        nextIds.add(new NextTableIdsBuilder().setTableId((short) 2).build());
        nextPropBuilder.setNextTableIds(nextIds);
        propBuilder.addAugmentation(NextTableRelatedTableFeatureProperty.class, nextPropBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();

        propBuilder.setType(TableFeaturesPropType.OFPTFPTNEXTTABLESMISS);
        nextPropBuilder = new NextTableRelatedTableFeaturePropertyBuilder();
        nextIds = new ArrayList<>();
        nextIds.add(new NextTableIdsBuilder().setTableId((short) 3).build());
        nextPropBuilder.setNextTableIds(nextIds);
        propBuilder.addAugmentation(NextTableRelatedTableFeatureProperty.class, nextPropBuilder.build());
        properties.add(propBuilder.build());
        propBuilder = new TableFeaturePropertiesBuilder();

        propBuilder.setType(TableFeaturesPropType.OFPTFPTINSTRUCTIONS);
        InstructionRelatedTableFeaturePropertyBuilder insPropBuilder =
                new InstructionRelatedTableFeaturePropertyBuilder();

         /* -------------------------------------------------- */

        List<Action> actions = new ArrayList<>();
        List<Instruction> insIds = new ArrayList<>();
        InstructionBuilder insBuilder = new InstructionBuilder();
        WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
        WriteActionsBuilder writeActionsBuilder = new WriteActionsBuilder();
        ActionBuilder actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createSetNwSrcAction());
        actions.add(actionBuilder.build());
        writeActionsBuilder.setAction(actions);
        writeActionsCaseBuilder.setWriteActions(writeActionsBuilder.build());
        insBuilder.setInstructionChoice(writeActionsCaseBuilder.build());
        insIds.add(insBuilder.build());

         /* -------------------------------------------------- */

        insBuilder = new InstructionBuilder();
        GotoTableCaseBuilder gotoCaseBuilder = new GotoTableCaseBuilder();
        GotoTableBuilder gotoTableBuilder = new GotoTableBuilder();
        gotoCaseBuilder.setGotoTable(gotoTableBuilder.build());
        insBuilder.setInstructionChoice(gotoCaseBuilder.build());
        insIds.add(insBuilder.build());
        insPropBuilder.setInstruction(insIds);
        propBuilder.addAugmentation(InstructionRelatedTableFeatureProperty.class, insPropBuilder.build());
        properties.add(propBuilder.build());

         /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS);
        insPropBuilder = new InstructionRelatedTableFeaturePropertyBuilder();
        insIds = new ArrayList<>();
        insBuilder = new InstructionBuilder();
        WriteMetadataCaseBuilder writeMetadataCaseBuilder = new WriteMetadataCaseBuilder();
        WriteMetadataBuilder writeMetadataBuilder = new WriteMetadataBuilder();
        writeMetadataCaseBuilder.setWriteMetadata(writeMetadataBuilder.build());
        insBuilder.setInstructionChoice(writeMetadataCaseBuilder.build());
        insIds.add(insBuilder.build());

        insBuilder = new InstructionBuilder();
        ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        ApplyActionsBuilder applyActionsBuilder = new ApplyActionsBuilder();
        applyActionsCaseBuilder.setApplyActions(applyActionsBuilder.build());
        insBuilder.setInstructionChoice(applyActionsCaseBuilder.build());
        insIds.add(insBuilder.build());

        insBuilder = new InstructionBuilder();
        MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
        MeterBuilder meterBuilder = new MeterBuilder();
        meterCaseBuilder.setMeter(meterBuilder.build());
        insBuilder.setInstructionChoice(meterCaseBuilder.build());
        insIds.add(insBuilder.build());


        insBuilder = new InstructionBuilder();
        ClearActionsCaseBuilder clearActionsCaseBuilder = new ClearActionsCaseBuilder();
        insBuilder.setInstructionChoice(clearActionsCaseBuilder.build());
        insIds.add(insBuilder.build());


        insBuilder = new InstructionBuilder();
        GotoTableCaseBuilder gotoCaseBuilder2 = new GotoTableCaseBuilder();
        GotoTableBuilder gotoTableBuilder2 = new GotoTableBuilder();
        gotoCaseBuilder2.setGotoTable(gotoTableBuilder2.build());
        insBuilder.setInstructionChoice(gotoCaseBuilder2.build());
        insIds.add(insBuilder.build());

        insPropBuilder.setInstruction(insIds);
        propBuilder.addAugmentation(InstructionRelatedTableFeatureProperty.class, insPropBuilder.build());
        properties.add(propBuilder.build());
        featuresBuilder.setTableFeatureProperties(properties);
        features.add(featuresBuilder.build());
        featuresBuilder = new TableFeaturesBuilder();
        featuresBuilder.setTableId((short) 6);
        featuresBuilder.setName("Mahalo");
        byte[] metaMatch2 = new byte[]{8, 9, 10, 11, 12, 13, 14, 15};
        featuresBuilder.setMetadataMatch(metaMatch2);
        byte[] metaWrite2 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
        featuresBuilder.setMetadataWrite(metaWrite2);
        featuresBuilder.setConfig(new TableConfig(false));
        featuresBuilder.setMaxEntries(24L);

        /* -------------------------------------------------- */

        properties = new ArrayList<>();
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTMATCH);
        OxmRelatedTableFeaturePropertyBuilder oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPort.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build()); //[0]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYSETFIELD);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpProto.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());//[1]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITESETFIELD);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanVid.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());//[2]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanPcp.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TcpSrc.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());
        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());//[3]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(UdpSrc.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(UdpDst.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());

        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());//[4]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWILDCARDS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        entries = new ArrayList<>();

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthSrc.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthDst.class);
        entriesBuilder.setHasMask(false);
        entries.add(entriesBuilder.build());

        oxmBuilder.setMatchEntry(entries);
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());//[5]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYACTIONS);
        ActionRelatedTableFeaturePropertyBuilder actBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        actions = new ArrayList<>();

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createSetNwSrcAction());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createSetNwSrcAction());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createSetNwSrcAction());
        actions.add(actionBuilder.build());
        actBuilder.setAction(actions);
        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());//[6]

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS);
        actBuilder = new ActionRelatedTableFeaturePropertyBuilder();

        actions = new ArrayList<>();
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createGroupAction());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createCopyTtlInCase());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createCopyTtlOutCase());
        actions.add(actionBuilder.build());
        actBuilder.setAction(actions);
        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());//[7]

        /* -------------------------------------------------- */
        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITEACTIONS);
        actBuilder = new ActionRelatedTableFeaturePropertyBuilder();

        actions = new ArrayList<>();
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createSetMplsTtlCase());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createDecMplsTtlCase());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(pushVlanCase());
        actions.add(actionBuilder.build());
        actBuilder.setAction(actions);

        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS);
        actBuilder = new ActionRelatedTableFeaturePropertyBuilder();

        actions = new ArrayList<>();
        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createPopVlanCase());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createPushPbbCase());
        actions.add(actionBuilder.build());

        actionBuilder = new ActionBuilder();
        actionBuilder.setActionChoice(createEmptySetFieldCase());
        actions.add(actionBuilder.build());
        actBuilder.setAction(actions);

        propBuilder.addAugmentation(ActionRelatedTableFeatureProperty.class, actBuilder.build());
        properties.add(propBuilder.build());

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTEXPERIMENTER);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());

        /* -------------------------------------------------- */

        propBuilder = new TableFeaturePropertiesBuilder();
        propBuilder.setType(TableFeaturesPropType.OFPTFPTEXPERIMENTERMISS);
        oxmBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        propBuilder.addAugmentation(OxmRelatedTableFeatureProperty.class, oxmBuilder.build());
        properties.add(propBuilder.build());

        /* -------------------------------------------------- */

        featuresBuilder.setTableFeatureProperties(properties);
        features.add(featuresBuilder.build());
        builder.setTableFeatures(features);

        List<TableFeatures> list = convert(builder.build());

        Assert.assertEquals("Returned empty list", 2, list.size());
        TableFeatures feature = list.get(0);
        Assert.assertEquals("Wrong table-id", 5, feature.getTableId().intValue());
        Assert.assertEquals("Wrong name", "Aloha", feature.getName());
        Assert.assertEquals("Wrong metadata match", new BigInteger(1, metaMatch), feature.getMetadataMatch());
        Assert.assertEquals("Wrong metadata write", new BigInteger(1, metaWrite), feature.getMetadataWrite());
        Assert.assertEquals("Wrong config", false, feature.getConfig().isDEPRECATEDMASK());
        Assert.assertEquals("Wrong max-entries", 42, feature.getMaxEntries().intValue());
        Assert.assertEquals("Wrong properties", 4, feature.getTableProperties().getTableFeatureProperties().size());
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties
                .TableFeatureProperties property = feature.getTableProperties().getTableFeatureProperties().get(0);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        NextTable propType = (NextTable) property.getTableFeaturePropType();
        List<Short> ids = propType.getTables().getTableIds();
        Assert.assertEquals("Wrong next table-id size", 2, ids.size());
        Assert.assertEquals("Wrong next-registry-id", 1, ids.get(0).intValue());
        Assert.assertEquals("Wrong next-registry-id", 2, ids.get(1).intValue());
        property = feature.getTableProperties().getTableFeatureProperties().get(1);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        NextTableMiss propType2 = (NextTableMiss) property.getTableFeaturePropType();
        ids = propType2.getTablesMiss().getTableIds();
        Assert.assertEquals("Wrong next table-id size", 1, ids.size());
        Assert.assertEquals("Wrong next-registry-id", 3, ids.get(0).intValue());

        property = feature.getTableProperties().getTableFeatureProperties().get(2);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        Instructions propType3 = (Instructions) property.getTableFeaturePropType();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list
                .Instruction> instructionIds = propType3.getInstructions().getInstruction();
        Assert.assertEquals("Wrong instruction-ids size", 2, instructionIds.size());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase",
                instructionIds.get(0).getInstruction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase",
                instructionIds.get(1).getInstruction().getImplementedInterface().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(3);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        InstructionsMiss propType4 = (InstructionsMiss) property.getTableFeaturePropType();
        instructionIds = propType4.getInstructionsMiss().getInstruction();
        Assert.assertEquals("Wrong instruction-ids size", 5, instructionIds.size());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase",
                instructionIds.get(0).getInstruction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase",
                instructionIds.get(1).getInstruction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase",
                instructionIds.get(2).getInstruction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase",
                instructionIds.get(3).getInstruction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong instruction-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase",
                instructionIds.get(4).getInstruction().getImplementedInterface().getName());

        feature = list.get(1);
        Assert.assertEquals("Wrong table-id", 6, feature.getTableId().intValue());
        Assert.assertEquals("Wrong name", "Mahalo", feature.getName());
        Assert.assertEquals("Wrong metadata match", new BigInteger(1, metaMatch2), feature.getMetadataMatch());
        Assert.assertEquals("Wrong metadata write", new BigInteger(1, metaWrite2), feature.getMetadataWrite());
        Assert.assertEquals("Wrong config", false, feature.getConfig().isDEPRECATEDMASK());
        Assert.assertEquals("Wrong max-entries", 24, feature.getMaxEntries().intValue());
        Assert.assertEquals("Wrong properties", 12, feature.getTableProperties().getTableFeatureProperties().size());
        property = feature.getTableProperties().getTableFeatureProperties().get(0);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        Match propType5 = (Match) property.getTableFeaturePropType();
        List<SetFieldMatch> fieldMatch = propType5.getMatchSetfield().getSetFieldMatch();
        Assert.assertEquals("Wrong match-entry-ids size", 2, fieldMatch.size());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort",
                fieldMatch.get(0).getMatchType().getName());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort",
                fieldMatch.get(1).getMatchType().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(1);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        ApplySetfield propType6 = (ApplySetfield) property.getTableFeaturePropType();
        fieldMatch = propType6.getApplySetfield().getSetFieldMatch();
        Assert.assertEquals("Wrong match-entry-ids size", 2, fieldMatch.size());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto",
                fieldMatch.get(0).getMatchType().getName());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn",
                fieldMatch.get(1).getMatchType().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(2);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        WriteSetfield propType7 = (WriteSetfield) property.getTableFeaturePropType();
        fieldMatch = propType7.getWriteSetfield().getSetFieldMatch();
        Assert.assertEquals("Wrong match-entry-ids size", 2, fieldMatch.size());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr",
                fieldMatch.get(0).getMatchType().getName());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid",
                fieldMatch.get(1).getMatchType().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(3);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        WriteSetfieldMiss propType8 = (WriteSetfieldMiss) property.getTableFeaturePropType();
        fieldMatch = propType8.getWriteSetfieldMiss().getSetFieldMatch();
        Assert.assertEquals("Wrong match-entry-ids size", 2, fieldMatch.size());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp",
                fieldMatch.get(0).getMatchType().getName());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc",
                fieldMatch.get(1).getMatchType().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(4);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        ApplySetfieldMiss propType9 = (ApplySetfieldMiss) property.getTableFeaturePropType();
        fieldMatch = propType9.getApplySetfieldMiss().getSetFieldMatch();
        Assert.assertEquals("Wrong match-entry-ids size", 2, fieldMatch.size());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc",
                fieldMatch.get(0).getMatchType().getName());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst",
                fieldMatch.get(1).getMatchType().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(5);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        Wildcards propType10 = (Wildcards) property.getTableFeaturePropType();
        fieldMatch = propType10.getWildcardSetfield().getSetFieldMatch();
        Assert.assertEquals("Wrong match-entry-ids size", 2, fieldMatch.size());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc",
                fieldMatch.get(0).getMatchType().getName());
        Assert.assertEquals("Wrong match-entry-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst",
                fieldMatch.get(1).getMatchType().getName());

        property = feature.getTableProperties().getTableFeatureProperties().get(6);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .ApplyActions propType11 = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions) property.getTableFeaturePropType();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionsList = propType11.getApplyActions().getAction();
        Assert.assertEquals("Wrong actions-ids size", 3, actionsList.size());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase",
                actionsList.get(0).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase",
                actionsList.get(1).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCase",
                actionsList.get(2).getAction().getImplementedInterface().getName());

        property = feature.getTableProperties().getTableFeatureProperties().get(7);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss",
                property.getTableFeaturePropType().getImplementedInterface().getName());
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .ApplyActionsMiss propType12 = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .ApplyActionsMiss) property.getTableFeaturePropType();
        actionsList = propType12.getApplyActionsMiss().getAction();
        Assert.assertEquals("Wrong actions-ids size", 3, actionsList.size());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase",
                actionsList.get(0).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase",
                actionsList.get(1).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase",
                actionsList.get(2).getAction().getImplementedInterface().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(8);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions",
                property.getTableFeaturePropType().getImplementedInterface().getName());

        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .WriteActions propType13 = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .WriteActions) property.getTableFeaturePropType();
        actionsList = propType13.getWriteActions().getAction();
        Assert.assertEquals("Wrong actions-ids size", 3, actionsList.size());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase",
                actionsList.get(0).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCase",
                actionsList.get(1).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase",
                actionsList.get(2).getAction().getImplementedInterface().getName());
        property = feature.getTableProperties().getTableFeatureProperties().get(9);
        Assert.assertEquals("Wrong property type", "org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMiss",
                property.getTableFeaturePropType().getImplementedInterface().getName());

        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .WriteActionsMiss propType14 = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type
                .WriteActionsMiss) property.getTableFeaturePropType();
        actionsList = propType14.getWriteActionsMiss().getAction();
        Assert.assertEquals("Wrong actions-ids size", 3, actionsList.size());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase",
                actionsList.get(0).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase",
                actionsList.get(1).getAction().getImplementedInterface().getName());
        Assert.assertEquals("Wrong actions-id", "org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase",
                actionsList.get(2).getAction().getImplementedInterface().getName());
    }

    private static SetNwSrcCase createSetNwSrcAction() {
        final SetNwSrcCaseBuilder setNwSrcCaseBuilder;
        final SetNwSrcActionBuilder setNwSrcActionBuilder;
        setNwSrcCaseBuilder = new SetNwSrcCaseBuilder();
        setNwSrcActionBuilder = new SetNwSrcActionBuilder();
        setNwSrcActionBuilder.setIpAddress(new Ipv4Address("1.2.3.4"));
        setNwSrcCaseBuilder.setSetNwSrcAction(setNwSrcActionBuilder.build());
        return setNwSrcCaseBuilder.build();
    }

    private static GroupCase createGroupAction() {
        final GroupCaseBuilder groupCaseBuilder = new GroupCaseBuilder();
        final GroupActionBuilder groupActionBuilder = new GroupActionBuilder();
        groupActionBuilder.setGroupId(42L);
        groupCaseBuilder.setGroupAction(groupActionBuilder.build());
        return groupCaseBuilder.build();
    }

    private static CopyTtlInCase createCopyTtlInCase() {
        CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
        return copyTtlInCaseBuilder.build();
    }

    private static CopyTtlOutCase createCopyTtlOutCase() {
        CopyTtlOutCaseBuilder copyTtlInCaseBuilder = new CopyTtlOutCaseBuilder();
        return copyTtlInCaseBuilder.build();
    }

    private static SetMplsTtlCase createSetMplsTtlCase() {
        SetMplsTtlCaseBuilder setMplsTtlCaseBuilder = new SetMplsTtlCaseBuilder();
        SetMplsTtlActionBuilder setMplsTtlActionBuilder = new SetMplsTtlActionBuilder();
        setMplsTtlActionBuilder.setMplsTtl((short) 42);
        setMplsTtlCaseBuilder.setSetMplsTtlAction(setMplsTtlActionBuilder.build());
        return setMplsTtlCaseBuilder.build();
    }

    private static DecMplsTtlCase createDecMplsTtlCase() {
        DecMplsTtlCaseBuilder decMplsTtlCaseBuilder = new DecMplsTtlCaseBuilder();
        return decMplsTtlCaseBuilder.build();
    }

    private static PushVlanCase pushVlanCase() {
        PushVlanCaseBuilder pushVlanCaseBuilder = new PushVlanCaseBuilder();
        PushVlanActionBuilder pushVlanActionBuilder = new PushVlanActionBuilder();
        pushVlanActionBuilder.setEthertype(new EtherType(1));
        pushVlanCaseBuilder.setPushVlanAction(pushVlanActionBuilder.build());
        return pushVlanCaseBuilder.build();
    }

    private static PopVlanCase createPopVlanCase() {
        PopVlanCaseBuilder popVlanCaseBuilder = new PopVlanCaseBuilder();
        return popVlanCaseBuilder.build();
    }

    private static PushPbbCase createPushPbbCase() {
        PushPbbCaseBuilder pushPbbCaseBuilder = new PushPbbCaseBuilder();
        PushPbbActionBuilder pushPbbActionBuilder = new PushPbbActionBuilder();
        pushPbbActionBuilder.setEthertype(new EtherType(1));
        pushPbbCaseBuilder.setPushPbbAction(pushPbbActionBuilder.build());
        return pushPbbCaseBuilder.build();
    }

    private static SetFieldCase createEmptySetFieldCase() {
        SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
        SetFieldActionBuilder setFieldActionBuilder = new SetFieldActionBuilder();
        List<MatchEntry> matchEntries = new ArrayList<>();
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();

        matchEntries.add(matchEntryBuilder.build());
        setFieldActionBuilder.setMatchEntry(matchEntries);
        setFieldCaseBuilder.setSetFieldAction(setFieldActionBuilder.build());
        return setFieldCaseBuilder.build();
    }


    private List<TableFeatures> convert(MultipartReplyTableFeatures features) {
        Optional<List<TableFeatures>> listOptional = ConvertorManager.getInstance().convert(features);
        return listOptional.orElse(Collections.emptyList());
    }
}