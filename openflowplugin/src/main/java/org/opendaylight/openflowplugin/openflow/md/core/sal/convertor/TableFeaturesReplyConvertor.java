/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.InstructionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WildcardsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.miss.ApplySetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.match.MatchSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.TablesMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.wildcards.WildcardSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.WriteSetfieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.miss.WriteSetfieldMissBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TableProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TablePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeaturePropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting a OF library table features to MD-SAL table
 * features.
 */
public class TableFeaturesReplyConvertor {
    private static final Logger logger = LoggerFactory.getLogger(TableFeaturesReplyConvertor.class);

    public static List<TableFeatures> toTableFeaturesReply(
            MultipartReplyTableFeatures ofTableFeaturesList) {
        if (ofTableFeaturesList == null || ofTableFeaturesList.getTableFeatures() == null) {
            return Collections.<TableFeatures> emptyList();
        }
        List<TableFeatures> salTableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder salTableFeatures = new TableFeaturesBuilder();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.
                multipart.reply.table.features._case.multipart.reply.table.features.TableFeatures ofTableFeatures : ofTableFeaturesList
                .getTableFeatures()) {
            salTableFeatures.setTableId(ofTableFeatures.getTableId());
            salTableFeatures.setName(ofTableFeatures.getName());
            if (ofTableFeatures.getMetadataMatch() != null) {
                salTableFeatures.setMetadataMatch(new BigInteger(ofTableFeatures.getMetadataMatch()));
            }
            if (ofTableFeatures.getMetadataWrite() != null) {
                salTableFeatures.setMetadataWrite(new BigInteger(ofTableFeatures.getMetadataWrite()));
            }
            if (ofTableFeatures.getConfig() != null) {
                salTableFeatures.setConfig(new TableConfig(ofTableFeatures.getConfig().isOFPTCDEPRECATEDMASK()));
            }
            salTableFeatures.setMaxEntries(ofTableFeatures.getMaxEntries());
            salTableFeatures.setTableProperties(toTableProperties(ofTableFeatures.getTableFeatureProperties()));
            salTableFeaturesList.add(salTableFeatures.build());
        }
        return salTableFeaturesList;
    }

    private static TableProperties toTableProperties(List<TableFeatureProperties> ofTablePropertiesList) {
        if (ofTablePropertiesList == null) {
            return new TablePropertiesBuilder()
                    .setTableFeatureProperties(
                            Collections
                                    .<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties> emptyList())
                    .build();
        }
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties> salTablePropertiesList = new ArrayList<>();
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();
        for (TableFeatureProperties property : ofTablePropertiesList) {
            TableFeaturesPropType propType = property.getType();
            if (propType != null) {
                switch (propType) {
                case OFPTFPTINSTRUCTIONS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder instructionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder();
                    instructionBuilder
                            .setInstructions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.InstructionsBuilder()
                                    .setInstruction(setInstructionTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(instructionBuilder.build());
                    break;
                case OFPTFPTINSTRUCTIONSMISS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder instructionMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder();
                    instructionMissBuilder
                            .setInstructionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder()
                                    .setInstruction(setInstructionTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(instructionMissBuilder.build());
                    break;
                case OFPTFPTNEXTTABLES:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder nextTableBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder();
                    nextTableBuilder
                            .setTables(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.TablesBuilder()
                                    .setTableIds(setNextTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(nextTableBuilder.build());
                    break;
                case OFPTFPTNEXTTABLESMISS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder nextTableMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder();
                    nextTableMissBuilder
                            .setTablesMiss(new TablesMissBuilder()
                                    .setTableIds(setNextTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(nextTableMissBuilder.build());
                    break;
                case OFPTFPTWRITEACTIONS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder writeActionsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder();
                    writeActionsBuilder
                            .setWriteActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.WriteActionsBuilder()
                                    .setAction(setActionTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(writeActionsBuilder.build());
                    break;
                case OFPTFPTWRITEACTIONSMISS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder writeActionsMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder();
                    writeActionsMissBuilder
                            .setWriteActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMissBuilder()
                                    .setAction(setActionTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(writeActionsMissBuilder.build());
                    break;
                case OFPTFPTAPPLYACTIONS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder applyActionsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder();
                    applyActionsBuilder
                            .setApplyActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActionsBuilder()
                                    .setAction(setActionTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(applyActionsBuilder.build());
                    break;
                case OFPTFPTAPPLYACTIONSMISS:
                    org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder applyActionsMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder();
                    applyActionsMissBuilder
                            .setApplyActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder()
                                    .setAction(setActionTableFeatureProperty(property)).build());
                    propBuilder.setTableFeaturePropType(applyActionsMissBuilder.build());
                    break;
                case OFPTFPTMATCH:
                    MatchSetfieldBuilder matchBuilder = new MatchSetfieldBuilder();
                    matchBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, true));
                    propBuilder.setTableFeaturePropType(new MatchBuilder().setMatchSetfield(matchBuilder.build()).build());
                    break;
                case OFPTFPTWILDCARDS:
                    WildcardSetfieldBuilder wildcardsBuilder = new WildcardSetfieldBuilder();
                    wildcardsBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                    propBuilder.setTableFeaturePropType(new WildcardsBuilder().setWildcardSetfield(wildcardsBuilder.build()).build());
                    break;
                case OFPTFPTWRITESETFIELD:
                    WriteSetfieldBuilder writeSetfieldBuilder = new WriteSetfieldBuilder();
                    writeSetfieldBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                    propBuilder.setTableFeaturePropType(new 
                            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldBuilder().setWriteSetfield(writeSetfieldBuilder.build()).build());
                    break;
                case OFPTFPTWRITESETFIELDMISS:
                    WriteSetfieldMissBuilder writeSetfieldMissBuilder = new WriteSetfieldMissBuilder();
                    writeSetfieldMissBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                    propBuilder.setTableFeaturePropType(new 
                            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMissBuilder().setWriteSetfieldMiss(writeSetfieldMissBuilder.build()).build());
                    break;
                case OFPTFPTAPPLYSETFIELD:
                    ApplySetfieldBuilder applySetfieldBuilder = new ApplySetfieldBuilder();
                    applySetfieldBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                    propBuilder.setTableFeaturePropType(new 
                            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldBuilder().setApplySetfield(applySetfieldBuilder.build()).build());
                    break;
                case OFPTFPTAPPLYSETFIELDMISS:
                    ApplySetfieldMissBuilder applySetfieldMissBuilder = new ApplySetfieldMissBuilder();
                    applySetfieldMissBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                    propBuilder.setTableFeaturePropType(new 
                            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMissBuilder().setApplySetfieldMiss(applySetfieldMissBuilder.build()).build());
                    break;
                case OFPTFPTEXPERIMENTER:
                    // Experimenter Table features are unhandled
                    break;
                case OFPTFPTEXPERIMENTERMISS:
                    // Experimenter miss Table features are unhandled
                    break;
                default:
                    logger.error("Unsupported table feature property : " + propType);
                    break;
                }
                salTablePropertiesList.add(propBuilder.build());
            }
        }
        return new TablePropertiesBuilder().setTableFeatureProperties(salTablePropertiesList).build();
    }

    private static List<Instruction> setInstructionTableFeatureProperty(TableFeatureProperties properties) {
        List<Instruction> instructionList = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder builder = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731
                .instructions.grouping.Instruction currInstruction : properties
                .getAugmentation(InstructionRelatedTableFeatureProperty.class).getInstruction()) {
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.InstructionBase> currInstructionType = currInstruction
                    .getType();
            if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class)) {
                builder.setInstruction((new GoToTableCaseBuilder()
                        .build()));
            } else if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class)) {
                builder.setInstruction((new WriteMetadataCaseBuilder()
                        .build()));
            } else if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class)) {
                builder.setInstruction((new WriteActionsCaseBuilder()
                        .build()));
            } else if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class)) {
                builder.setInstruction((new ApplyActionsCaseBuilder()
                        .build()));
            } else if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class)) {
                builder.setInstruction((new ClearActionsCaseBuilder()
                        .build()));
            } else if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class)) {
                builder.setInstruction((new MeterCaseBuilder()
                        .build()));
            } else if (currInstructionType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Experimenter.class)) {
                // TODO: Experimenter instructions are unhandled
            }
            instructionList.add(builder.build());
        }
        return instructionList;
    }

    private static List<Short> setNextTableFeatureProperty(TableFeatureProperties properties) {
        List<Short> nextTableIdsList = new ArrayList<>();
        for (NextTableIds tableId : properties.getAugmentation(NextTableRelatedTableFeatureProperty.class)
                .getNextTableIds()) {
            nextTableIdsList.add(tableId.getTableId());
        }
        return nextTableIdsList;
    }

    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> setActionTableFeatureProperty(
            TableFeatureProperties properties) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList = new ArrayList<>();
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder();
        for (Action action : properties
                .getAugmentation(ActionRelatedTableFeatureProperty.class).getAction()) {
            if (action != null) {
                Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase> actionType = action
                        .getType();
                if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class)) {
                    actionBuilder.setAction(new OutputActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class)) {
                    actionBuilder.setAction(new GroupActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class)) {
                    actionBuilder.setAction(new CopyTtlOutCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class)) {
                    actionBuilder.setAction(new CopyTtlInCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class)) {
                    actionBuilder.setAction(new SetMplsTtlActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class)) {
                    actionBuilder.setAction(new DecMplsTtlCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class)) {
                    actionBuilder.setAction(new PushVlanActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class)) {
                    actionBuilder.setAction(new PopVlanActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class)) {
                    actionBuilder.setAction(new PushMplsActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class)) {
                    actionBuilder.setAction(new PopMplsActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class)) {
                    actionBuilder.setAction(new SetQueueActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class)) {
                    actionBuilder.setAction(new SetNwTtlActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class)) {
                    actionBuilder.setAction(new DecNwTtlCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class)) {
                    actionBuilder.setAction(new SetFieldCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class)) {
                    actionBuilder.setAction(new PushPbbActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class)) {
                    actionBuilder.setAction(new PopPbbActionCaseBuilder().build());
                } else if (actionType
                        .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter.class)) {
                    // TODO: Experimenter Action unhandled.
                }
                actionList.add(actionBuilder.build());
            }
        }
        return actionList;
    }

    private static List<SetFieldMatch> setSetFieldTableFeatureProperty(TableFeatureProperties properties,
            boolean setHasMask) {
        List<SetFieldMatch> setFieldMatchList = new ArrayList<>();
        SetFieldMatchBuilder setFieldMatchBuilder = new SetFieldMatchBuilder();
        Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> salMatchField = null;
        Class<? extends MatchField> ofMatchField = null;

        // This handles only OpenflowBasicClass oxm class.
        for (MatchEntries currMatch : properties.getAugmentation(OxmRelatedTableFeatureProperty.class)
                .getMatchEntries()) {
            ofMatchField = currMatch.getOxmMatchField();
            if (ofMatchField.equals(ArpOp.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class;
            } else if (ofMatchField.equals(ArpSha.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class;
            } else if (ofMatchField.equals(ArpSpa.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class;
            } else if (ofMatchField.equals(ArpTha.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class;
            } else if (ofMatchField.equals(ArpTpa.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class;
            } else if (ofMatchField.equals(EthDst.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class;
            } else if (ofMatchField.equals(EthSrc.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class;
            } else if (ofMatchField.equals(EthType.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class;
            } else if (ofMatchField.equals(Icmpv4Code.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class;
            } else if (ofMatchField.equals(Icmpv4Type.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class;
            } else if (ofMatchField.equals(Icmpv6Code.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class;
            } else if (ofMatchField.equals(Icmpv6Type.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class;
            } else if (ofMatchField.equals(InPhyPort.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class;
            } else if (ofMatchField.equals(InPort.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class;
            } else if (ofMatchField.equals(IpDscp.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class;
            } else if (ofMatchField.equals(IpEcn.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class;
            } else if (ofMatchField.equals(IpProto.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class;
            } else if (ofMatchField.equals(Ipv4Dst.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class;
            } else if (ofMatchField.equals(Ipv4Src.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class;
            } else if (ofMatchField.equals(Ipv6Dst.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class;
            } else if (ofMatchField.equals(Ipv6Exthdr.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class;
            } else if (ofMatchField.equals(Ipv6Flabel.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class;
            } else if (ofMatchField.equals(Ipv6NdSll.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class;
            } else if (ofMatchField.equals(Ipv6NdTarget.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class;
            } else if (ofMatchField.equals(Ipv6NdTll.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class;
            } else if (ofMatchField.equals(Ipv6Src.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class;
            } else if (ofMatchField.equals(Metadata.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class;
            } else if (ofMatchField.equals(MplsBos.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class;
            } else if (ofMatchField.equals(MplsLabel.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class;
            } else if (ofMatchField.equals(MplsTc.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class;
            } else if (ofMatchField.equals(PbbIsid.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class;
            } else if (ofMatchField.equals(SctpDst.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class;
            } else if (ofMatchField.equals(SctpSrc.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class;
            } else if (ofMatchField.equals(TcpDst.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class;
            } else if (ofMatchField.equals(TcpSrc.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class;
            } else if (ofMatchField.equals(TunnelId.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class;
            } else if (ofMatchField.equals(UdpDst.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class;
            } else if (ofMatchField.equals(UdpSrc.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc.class;
            } else if (ofMatchField.equals(VlanPcp.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class;
            } else if (ofMatchField.equals(VlanVid.class)) {
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class;
            }

            setFieldMatchBuilder.setMatchType(salMatchField);
            if (setHasMask) {
                setFieldMatchBuilder.setHasMask(currMatch.isHasMask());
            }
            setFieldMatchList.add(setFieldMatchBuilder.build());
        }
        return setFieldMatchList;
    }

}