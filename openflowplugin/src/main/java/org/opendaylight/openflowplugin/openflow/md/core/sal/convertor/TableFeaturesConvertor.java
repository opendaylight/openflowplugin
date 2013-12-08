/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.MatchEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.InstructionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOut;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.ActionsListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.actions.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.InstructionsBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting a MD-SAL Table features into the OF library table
 * features.
 */
public class TableFeaturesConvertor {
    private static final Logger logger = LoggerFactory.getLogger(TableFeaturesConvertor.class);

    public static List<TableFeatures> toTableFeaturesRequest(
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures salTableFeaturesList) {
        if (salTableFeaturesList == null) {
            return Collections.<TableFeatures> emptyList();
        }
        List<TableFeatures> ofTableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder ofTableFeatures = new TableFeaturesBuilder();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures salTableFeatures : salTableFeaturesList
                .getTableFeatures()) {
            ofTableFeatures.setTableId(salTableFeatures.getTableId());
            ofTableFeatures.setName(salTableFeatures.getName());
            ofTableFeatures.setMetadataMatch(salTableFeatures.getMetadataMatch());
            ofTableFeatures.setMetadataWrite(salTableFeatures.getMetadataWrite());
            if (salTableFeatures.getConfig() != null) {
                ofTableFeatures.setConfig(new TableConfig(salTableFeatures.getConfig().isDEPRECATEDMASK()));
            }
            ofTableFeatures.setTableFeatureProperties(toTableProperties(salTableFeatures.getTableProperties()));
            ofTableFeaturesList.add(ofTableFeatures.build());
        }
        return ofTableFeaturesList;
    }

    private static List<TableFeatureProperties> toTableProperties(
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.TableProperties tableProperties) {
        if (tableProperties == null) {
            return Collections.<TableFeatureProperties> emptyList();
        }
        List<TableFeatureProperties> ofTablePropertiesList = new ArrayList<>();
        TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties property : tableProperties
                .getTableFeatureProperties()) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType propType = property
                    .getTableFeaturePropType();
            if (propType instanceof Instructions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.Instructions instructions = ((Instructions) propType)
                        .getInstructions();
                setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONS,
                        (instructions == null) ? new ArrayList<Instruction>() : instructions.getInstruction());
            } else if (propType instanceof InstructionsMiss) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMiss instructions = ((InstructionsMiss) propType)
                        .getInstructionsMiss();
                setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS,
                        (instructions == null) ? new ArrayList<Instruction>() : instructions.getInstruction());
            } else if (propType instanceof NextTable) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.Tables tables = ((NextTable) propType)
                        .getTables();
                setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLES,
                        (tables == null) ? new ArrayList<Short>() : tables.getTableIds());
            } else if (propType instanceof NextTableMiss) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.Tables tables = ((NextTableMiss) propType)
                        .getTables();
                setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLESMISS,
                        (tables == null) ? new ArrayList<Short>() : tables.getTableIds());
            } else if (propType instanceof WriteActions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.WriteActions writeActions = ((WriteActions) propType)
                        .getWriteActions();
                setActionTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTWRITEACTIONS,
                        ((writeActions == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                                : writeActions.getAction()));
            } else if (propType instanceof WriteActionsMiss) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMiss writeActionsMiss = ((WriteActionsMiss) propType)
                        .getWriteActionsMiss();
                setActionTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS,
                        ((writeActionsMiss == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                                : writeActionsMiss.getAction()));
            } else if (propType instanceof ApplyActions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActions applyActions = ((ApplyActions) propType)
                        .getApplyActions();
                setActionTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTAPPLYACTIONS,
                        ((applyActions == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                                : applyActions.getAction()));
            } else if (propType instanceof ApplyActionsMiss) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMiss applyActionsMiss = ((ApplyActionsMiss) propType)
                        .getApplyActionsMiss();
                setActionTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS,
                        ((applyActionsMiss == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                                : applyActionsMiss.getAction()));
            } else if (propType instanceof Match) {
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = ((Match) propType)
                        .getSetFieldMatch();
                setSetFieldTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTMATCH,
                        ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                                : setFieldMatch));
            } else if (propType instanceof Wildcards) {
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = ((Wildcards) propType)
                        .getSetFieldMatch();
                setSetFieldTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTWILDCARDS,
                        ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                                : setFieldMatch));
            } else if (propType instanceof WriteSetfield) {
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = ((WriteSetfield) propType)
                        .getSetFieldMatch();
                setSetFieldTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTWRITEACTIONS,
                        ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                                : setFieldMatch));
            } else if (propType instanceof WriteSetfieldMiss) {
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = ((WriteSetfieldMiss) propType)
                        .getSetFieldMatch();
                setSetFieldTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS,
                        ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                                : setFieldMatch));
            } else if (propType instanceof ApplySetfield) {
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = ((ApplySetfield) propType)
                        .getSetFieldMatch();
                setSetFieldTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTAPPLYACTIONS,
                        ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                                : setFieldMatch));
            } else if (propType instanceof ApplySetfieldMiss) {
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = ((ApplySetfieldMiss) propType)
                        .getSetFieldMatch();
                setSetFieldTableFeatureProperty(
                        propBuilder,
                        TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS,
                        ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                                : setFieldMatch));
            } // Experimenter and Experimeneter miss Table features are
              // unhandled
            ofTablePropertiesList.add(propBuilder.build());
        }
        return ofTablePropertiesList;
    }

    private static void setInstructionTableFeatureProperty(TableFeaturePropertiesBuilder builder,
            TableFeaturesPropType type, List<Instruction> instructionList) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.Instructions> instructionTypeList = new ArrayList<>();
        InstructionsBuilder instructionType = new InstructionsBuilder();
        for (Instruction currInstruction : instructionList) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction instruction = currInstruction
                    .getInstruction();
            if (instruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTable) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class);
            } else if (instruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadata) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class);
            } else if (instruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActions) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class);
            } else if (instruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActions) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            } else if (instruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActions) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class);
            } else if (instruction instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.Meter) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class);
            } // TODO: Experimeneter instructions are unhandled
            instructionTypeList.add(instructionType.build());
        }
        InstructionRelatedTableFeaturePropertyBuilder propBuilder = new InstructionRelatedTableFeaturePropertyBuilder();
        propBuilder.setInstructions(instructionTypeList);
        builder.setType(type);
        builder.addAugmentation(InstructionRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static void setNextTableFeatureProperty(TableFeaturePropertiesBuilder builder, TableFeaturesPropType type,
            List<Short> tableIds) {
        List<NextTableIds> nextTableIdsList = new ArrayList<>();
        NextTableIdsBuilder nextTableId = new NextTableIdsBuilder();
        for (Short tableId : tableIds) {
            nextTableId.setTableId(tableId);
            nextTableIdsList.add(nextTableId.build());
        }
        NextTableRelatedTableFeaturePropertyBuilder propBuilder = new NextTableRelatedTableFeaturePropertyBuilder();
        propBuilder.setNextTableIds(nextTableIdsList);
        builder.setType(type);
        builder.addAugmentation(NextTableRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static void setActionTableFeatureProperty(TableFeaturePropertiesBuilder builder,
            TableFeaturesPropType type,
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> salActions) {
        List<ActionsList> actionList = new ArrayList<>();
        ActionsListBuilder actionListBuilder = new ActionsListBuilder();
        ActionBuilder actionBuilder = new ActionBuilder();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action currAction : salActions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionType = currAction
                    .getAction();
            if (actionType instanceof OutputAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
            } else if (actionType instanceof GroupAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class);
            } else if (actionType instanceof CopyTtlOut) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);
            } else if (actionType instanceof CopyTtlIn) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);
            } else if (actionType instanceof SetMplsTtlAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class);
            } else if (actionType instanceof DecMplsTtl) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);
            } else if (actionType instanceof PushVlanAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class);
            } else if (actionType instanceof PopVlanAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class);
            } else if (actionType instanceof PushMplsAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class);
            } else if (actionType instanceof PopMplsAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class);
            } else if (actionType instanceof SetQueueAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class);
            } else if (actionType instanceof SetNwTtlAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class);
            } else if (actionType instanceof DecNwTtl) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class);
            } else if (actionType instanceof SetField) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            } else if (actionType instanceof PushPbbAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class);
            } else if (actionType instanceof PopPbbAction) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class);
            } // Experimenter action is unhandled
            actionList.add(actionListBuilder.setAction(actionBuilder.build()).build());
        }
        ActionRelatedTableFeaturePropertyBuilder propBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        propBuilder.setActionsList(actionList);
        builder.setType(type);
        builder.addAugmentation(ActionRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static void setSetFieldTableFeatureProperty(
            TableFeaturePropertiesBuilder builder,
            TableFeaturesPropType type,
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFields) {
        List<MatchEntries> matchEntriesList = new ArrayList<>();
        MatchEntriesBuilder matchEntryBuilder = new MatchEntriesBuilder();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch currMatch : setFields) {
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> currMatchType = currMatch
                    .getMatchType();
            if (currMatchType.equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class)) {
                setMatchEntry(matchEntryBuilder, ArpOp.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class)) {
                setMatchEntry(matchEntryBuilder, ArpSha.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class)) {
                setMatchEntry(matchEntryBuilder, ArpSpa.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class)) {
                setMatchEntry(matchEntryBuilder, ArpTha.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class)) {
                setMatchEntry(matchEntryBuilder, ArpTpa.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class)) {
                setMatchEntry(matchEntryBuilder, EthDst.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class)) {
                setMatchEntry(matchEntryBuilder, EthSrc.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class)) {
                setMatchEntry(matchEntryBuilder, EthType.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class)) {
                setMatchEntry(matchEntryBuilder, Icmpv4Code.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class)) {
                setMatchEntry(matchEntryBuilder, Icmpv4Type.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class)) {
                setMatchEntry(matchEntryBuilder, Icmpv6Code.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class)) {
                setMatchEntry(matchEntryBuilder, Icmpv6Type.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class)) {
                setMatchEntry(matchEntryBuilder, InPhyPort.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class)) {
                setMatchEntry(matchEntryBuilder, InPort.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class)) {
                setMatchEntry(matchEntryBuilder, IpDscp.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class)) {
                setMatchEntry(matchEntryBuilder, IpEcn.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class)) {
                setMatchEntry(matchEntryBuilder, IpProto.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class)) {
                setMatchEntry(matchEntryBuilder, Ipv4Dst.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class)) {
                setMatchEntry(matchEntryBuilder, Ipv4Src.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6Dst.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6Exthdr.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6Flabel.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6NdSll.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6NdTarget.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6NdTll.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class)) {
                setMatchEntry(matchEntryBuilder, Ipv6Src.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class)) {
                setMatchEntry(matchEntryBuilder, Metadata.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class)) {
                setMatchEntry(matchEntryBuilder, MplsBos.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class)) {
                setMatchEntry(matchEntryBuilder, MplsLabel.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class)) {
                setMatchEntry(matchEntryBuilder, MplsTc.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class)) {
                setMatchEntry(matchEntryBuilder, PbbIsid.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class)) {
                setMatchEntry(matchEntryBuilder, SctpDst.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class)) {
                setMatchEntry(matchEntryBuilder, SctpSrc.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class)) {
                setMatchEntry(matchEntryBuilder, TcpDst.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class)) {
                setMatchEntry(matchEntryBuilder, TcpSrc.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class)) {
                setMatchEntry(matchEntryBuilder, TunnelId.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class)) {
                setMatchEntry(matchEntryBuilder, UdpDst.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class)) {
                setMatchEntry(matchEntryBuilder, UdpSrc.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class)) {
                setMatchEntry(matchEntryBuilder, VlanPcp.class, currMatch.isHasMask());
            } else if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class)) {
                setMatchEntry(matchEntryBuilder, VlanVid.class, currMatch.isHasMask());
            }
            matchEntriesList.add(matchEntryBuilder.build());
        }
        OxmRelatedTableFeaturePropertyBuilder propBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        propBuilder.setMatchEntries(matchEntriesList);
        builder.setType(type);
        builder.addAugmentation(OxmRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static void setMatchEntry(MatchEntriesBuilder builder,
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField> field,
            Boolean hasMask) {
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(field);
        builder.setHasMask(hasMask);
    }
}