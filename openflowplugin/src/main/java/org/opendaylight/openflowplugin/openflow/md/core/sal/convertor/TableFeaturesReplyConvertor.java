/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.HashMap;

import java.util.Map;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelIpv4Src;
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
    private static final Logger LOG = LoggerFactory.getLogger(TableFeaturesReplyConvertor.class);

    private TableFeaturesReplyConvertor() {
        //hiding implicit constructor
    }

    private interface ActionExecutor {
        public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder);
    }
    
    public static List<TableFeatures> toTableFeaturesReply(
            final MultipartReplyTableFeatures ofTableFeaturesList) {
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

    private static final Map<TableFeaturesPropType, ActionExecutor> TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION = new HashMap<>();
    static {
        
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTINSTRUCTIONS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder instructionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsBuilder();
                instructionBuilder
                        .setInstructions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.InstructionsBuilder()
                                .setInstruction(setInstructionTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(instructionBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder instructionMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMissBuilder();
                instructionMissBuilder
                        .setInstructionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMissBuilder()
                                .setInstruction(setInstructionTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(instructionMissBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTNEXTTABLES, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder nextTableBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableBuilder();
                nextTableBuilder
                        .setTables(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.TablesBuilder()
                                .setTableIds(setNextTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(nextTableBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTNEXTTABLESMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder nextTableMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMissBuilder();
                nextTableMissBuilder
                        .setTablesMiss(new TablesMissBuilder()
                                .setTableIds(setNextTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(nextTableMissBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTWRITEACTIONS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder writeActionsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsBuilder();
                writeActionsBuilder
                        .setWriteActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.WriteActionsBuilder()
                                .setAction(setActionTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(writeActionsBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder writeActionsMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMissBuilder();
                writeActionsMissBuilder
                        .setWriteActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMissBuilder()
                                .setAction(setActionTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(writeActionsMissBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTAPPLYACTIONS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder applyActionsBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsBuilder();
                applyActionsBuilder
                        .setApplyActions(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActionsBuilder()
                                .setAction(setActionTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(applyActionsBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder applyActionsMissBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMissBuilder();
                applyActionsMissBuilder
                        .setApplyActionsMiss(new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMissBuilder()
                                .setAction(setActionTableFeatureProperty(property)).build());
                propBuilder.setTableFeaturePropType(applyActionsMissBuilder.build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTMATCH, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                MatchSetfieldBuilder matchBuilder = new MatchSetfieldBuilder();
                matchBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, true));
                propBuilder.setTableFeaturePropType(new MatchBuilder().setMatchSetfield(matchBuilder.build()).build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTWILDCARDS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                WildcardSetfieldBuilder wildcardsBuilder = new WildcardSetfieldBuilder();
                wildcardsBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                propBuilder.setTableFeaturePropType(new WildcardsBuilder().setWildcardSetfield(wildcardsBuilder.build()).build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTWRITESETFIELD, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                WriteSetfieldBuilder writeSetfieldBuilder = new WriteSetfieldBuilder();
                writeSetfieldBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                propBuilder.setTableFeaturePropType(new
                        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldBuilder().setWriteSetfield(writeSetfieldBuilder.build()).build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                WriteSetfieldMissBuilder writeSetfieldMissBuilder = new WriteSetfieldMissBuilder();
                writeSetfieldMissBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                propBuilder.setTableFeaturePropType(new
                        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMissBuilder().setWriteSetfieldMiss(writeSetfieldMissBuilder.build()).build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTAPPLYSETFIELD, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                ApplySetfieldBuilder applySetfieldBuilder = new ApplySetfieldBuilder();
                applySetfieldBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                propBuilder.setTableFeaturePropType(new
                        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldBuilder().setApplySetfield(applySetfieldBuilder.build()).build());
            }
        });
        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                ApplySetfieldMissBuilder applySetfieldMissBuilder = new ApplySetfieldMissBuilder();
                applySetfieldMissBuilder.setSetFieldMatch(setSetFieldTableFeatureProperty(property, false));
                propBuilder.setTableFeaturePropType(new
                        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMissBuilder().setApplySetfieldMiss(applySetfieldMissBuilder.build()).build());
            }
        });

        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTEXPERIMENTER, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                LOG.debug("Experimenter Table features is unhandled");
            }
        });

        TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.put(TableFeaturesPropType.OFPTFPTEXPERIMENTERMISS, new ActionExecutor() {
            
            @Override
            public void execute(final TableFeatureProperties property, final TableFeaturePropertiesBuilder propBuilder) {
                LOG.debug("Experimenter miss Table features is unhandled");
            }
        });

    }
    private static TableProperties toTableProperties(final List<TableFeatureProperties> ofTablePropertiesList) {
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
            
            ActionExecutor actionExecutor = TABLE_FEATURE_PROPERTY_TYPE_TO_ACTION.get(propType);
            if (actionExecutor != null) {
                actionExecutor.execute(property, propBuilder);
            } else {
                LOG.error("Unsupported table feature property : " + propType);
            }

            salTablePropertiesList.add(propBuilder.build());
        }

        return new TablePropertiesBuilder().setTableFeatureProperties(salTablePropertiesList).build();
    }

    private static List<Instruction> setInstructionTableFeatureProperty(final TableFeatureProperties properties) {
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

    private static List<Short> setNextTableFeatureProperty(final TableFeatureProperties properties) {
        List<Short> nextTableIdsList = new ArrayList<>();
        for (NextTableIds tableId : properties.getAugmentation(NextTableRelatedTableFeatureProperty.class)
                .getNextTableIds()) {
            nextTableIdsList.add(tableId.getTableId());
        }
        return nextTableIdsList;
    }

    private static final Map<Class<?>,org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> OF_TO_SAL_ACTION = new HashMap<>(); 
    static {
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class, new OutputActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class, new GroupActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class, new CopyTtlOutCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class, new CopyTtlInCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class, new SetMplsTtlActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class, new DecMplsTtlCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class, new PushVlanActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class, new PopVlanActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class, new PushMplsActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class, new PopMplsActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class, new SetQueueActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class, new SetNwTtlActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class, new DecNwTtlCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class, new SetFieldCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class, new PushPbbActionCaseBuilder().build());
        OF_TO_SAL_ACTION.put(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class, new PopPbbActionCaseBuilder().build());
        // TODO: Experimenter Action unhandled. org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter.class
    }

    private static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> setActionTableFeatureProperty(
            final TableFeatureProperties properties) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actionList = new ArrayList<>();
        int order=0;
        for (Action action : properties
                .getAugmentation(ActionRelatedTableFeatureProperty.class).getAction()) {
            if (action != null) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder actionBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder();

                actionBuilder.setOrder(order++);
                Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase> actionType = action
                        .getType();
                org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action salAction = OF_TO_SAL_ACTION.get(actionType);
                actionBuilder.setAction(salAction);
                actionList.add(actionBuilder.build());
            }
        }
        return actionList;
    }

    private static final Map<Class<?>, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField>> OF_TO_SAL_TABLE_FEATURE_PROPERTIES = new HashMap<>();
    static {
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(ArpOp.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(ArpSha.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(ArpSpa.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(ArpTha.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(ArpTpa.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(EthDst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(EthSrc.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(EthType.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Icmpv4Code.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Icmpv4Type.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Icmpv6Code.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Icmpv6Type.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(InPhyPort.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(InPort.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(IpDscp.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(IpEcn.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(IpProto.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv4Dst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv4Src.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6Dst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6Exthdr.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6Flabel.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6NdSll.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6NdTarget.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6NdTll.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Ipv6Src.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(Metadata.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(MplsBos.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(MplsLabel.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(MplsTc.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(PbbIsid.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(SctpDst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(SctpSrc.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(TcpDst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(TcpSrc.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(TunnelId.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(UdpDst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(UdpSrc.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(VlanPcp.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(VlanVid.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(TunnelIpv4Dst.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.class);
        OF_TO_SAL_TABLE_FEATURE_PROPERTIES.put(TunnelIpv4Src.class, org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.class);
    }

    private static List<SetFieldMatch> setSetFieldTableFeatureProperty(final TableFeatureProperties properties,
            final boolean setHasMask) {
        List<SetFieldMatch> setFieldMatchList = new ArrayList<>();
        SetFieldMatchBuilder setFieldMatchBuilder = new SetFieldMatchBuilder();
        
        Class<? extends MatchField> ofMatchField = null;

        // This handles only OpenflowBasicClass oxm class.
        for (MatchEntries currMatch : properties.getAugmentation(OxmRelatedTableFeatureProperty.class)
                .getMatchEntries()) {
            ofMatchField = currMatch.getOxmMatchField();
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> salMatchField = null;
            // TODO: Move to seperate bundle as soon as extension are supported in openflowplugin/java
            if (ofMatchField.equals(TcpFlag.class)) {
                //FIXME: move to extensible support
                salMatchField = org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlag.class;
            } else {
                salMatchField = OF_TO_SAL_TABLE_FEATURE_PROPERTIES.get(ofMatchField);
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