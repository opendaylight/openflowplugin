/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Ordering;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlInCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.CopyTtlOutCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecMplsTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DecNwTtlCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PopVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushMplsActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushPbbActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.PushVlanActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetMplsTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwTtlActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteMetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.GroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushMplsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PushVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.GotoTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instruction.grouping.instruction.choice.WriteMetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.multipart.request.table.features.TableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeatureProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplySetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.InstructionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Wildcards;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActionsMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteSetfieldMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.match.MatchSetfield;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.miss.TablesMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.wildcards.WildcardSetfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting a MD-SAL Table features into the OF library table
 * features.
 */
public class TableFeaturesConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(TableFeaturesConvertor.class);
    private static final Ordering<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties> TABLE_FEATURE_PROPS_ORDERING =
            Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties>build());

    private TableFeaturesConvertor() {
        //hiding implicit construcotr
    }

    public static List<TableFeatures> toTableFeaturesRequest(
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures salTableFeaturesList) {
        List<TableFeatures> ofTableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder ofTableFeatures = new TableFeaturesBuilder();
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures salTableFeatures : salTableFeaturesList
                .getTableFeatures()) {
            ofTableFeatures.setTableId(salTableFeatures.getTableId());
            ofTableFeatures.setName(salTableFeatures.getName());
            ofTableFeatures.setMetadataMatch(salTableFeatures.getMetadataMatch());
            ofTableFeatures.setMetadataWrite(salTableFeatures.getMetadataWrite());
            ofTableFeatures.setMaxEntries(salTableFeatures.getMaxEntries());
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
            return Collections.<TableFeatureProperties>emptyList();
        }
        List<TableFeatureProperties> ofTablePropertiesList = new ArrayList<>();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties>
                sortedTableProperties = TABLE_FEATURE_PROPS_ORDERING.sortedCopy(tableProperties.getTableFeatureProperties());

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties
                property : sortedTableProperties) {
            TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();

            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType propType = property
                    .getTableFeaturePropType();

            setTableFeatureProperty(propType, propBuilder);
            if (propType instanceof Instructions) {
                setTableFeatureProperty((Instructions) propType, propBuilder);
            } else if (propType instanceof InstructionsMiss) {
                setTableFeatureProperty((InstructionsMiss) propType, propBuilder);
            } else if (propType instanceof NextTable) {
                setTableFeatureProperty((NextTable) propType, propBuilder);
            } else if (propType instanceof NextTableMiss) {
                setTableFeatureProperty((NextTableMiss) propType, propBuilder);
            } else if (propType instanceof WriteActions) {
                setTableFeatureProperty((WriteActions) propType, propBuilder);
            } else if (propType instanceof WriteActionsMiss) {
                setTableFeatureProperty((WriteActionsMiss) propType, propBuilder);
            } else if (propType instanceof ApplyActions) {
                setTableFeatureProperty((ApplyActions) propType, propBuilder);
            } else if (propType instanceof ApplyActionsMiss) {
                setTableFeatureProperty((ApplyActionsMiss) propType, propBuilder);
            } else if (propType instanceof Match) {
                setTableFeatureProperty((Match) propType, propBuilder);
            } else if (propType instanceof Wildcards) {
                setTableFeatureProperty((Wildcards) propType, propBuilder);
            } else if (propType instanceof WriteSetfield) {
                setTableFeatureProperty((WriteSetfield) propType, propBuilder);
            } else if (propType instanceof WriteSetfieldMiss) {
                setTableFeatureProperty((WriteSetfieldMiss) propType, propBuilder);
            } else if (propType instanceof ApplySetfield) {
                setTableFeatureProperty((ApplySetfield) propType, propBuilder);
            } else if (propType instanceof ApplySetfieldMiss) {
                setTableFeatureProperty((ApplySetfieldMiss) propType, propBuilder);
            }
            // Experimenter and Experimeneter miss Table features are unhandled
            ofTablePropertiesList.add(propBuilder.build());
        }
        return ofTablePropertiesList;
    }

    private static void setTableFeatureProperty(TableFeaturePropType propType, TableFeaturePropertiesBuilder propBuilder) {
        LOG.debug("Unknown TableFeaturePropType [{}]", propType.getClass());
    }

    private static void setTableFeatureProperty(ApplySetfieldMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.miss.ApplySetfieldMiss applySetfieldMiss = propType.getApplySetfieldMiss();

        if (null != applySetfieldMiss) {
            setFieldMatch = applySetfieldMiss.getSetFieldMatch();
        }
        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(ApplySetfield propType, TableFeaturePropertiesBuilder propBuilder) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfield applySetfield = propType.getApplySetfield();

        if (null != applySetfield) {
            setFieldMatch = applySetfield.getSetFieldMatch();
        }
        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYSETFIELD,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(WriteSetfieldMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.miss.WriteSetfieldMiss writeSetfieldMiss = propType.getWriteSetfieldMiss();

        if (null != writeSetfieldMiss) {
            setFieldMatch = writeSetfieldMiss.getSetFieldMatch();
        }
        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(WriteSetfield propType, TableFeaturePropertiesBuilder propBuilder) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.WriteSetfield writeSetField = propType.getWriteSetfield();

        if (null != writeSetField) {
            setFieldMatch = writeSetField.getSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITESETFIELD,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(Wildcards propType, TableFeaturePropertiesBuilder propBuilder) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;
        WildcardSetfield wildcardSetField = propType.getWildcardSetfield();

        if (null != wildcardSetField) {
            setFieldMatch = wildcardSetField.getSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWILDCARDS,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(Match propType, TableFeaturePropertiesBuilder propBuilder) {
        MatchSetfield matchSetField = propType.getMatchSetfield();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;

        if (null != matchSetField) {
            setFieldMatch = matchSetField.getSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTMATCH,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(ApplyActionsMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMiss applyActionsMiss = propType
                .getApplyActionsMiss();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS,
                ((applyActionsMiss == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : applyActionsMiss.getAction()));
    }

    private static void setTableFeatureProperty(ApplyActions propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActions applyActions = propType
                .getApplyActions();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYACTIONS,
                ((applyActions == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : applyActions.getAction()));
    }

    private static void setTableFeatureProperty(WriteActionsMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMiss writeActionsMiss = propType
                .getWriteActionsMiss();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS,
                ((writeActionsMiss == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : writeActionsMiss.getAction()));
    }

    private static void setTableFeatureProperty(WriteActions propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.WriteActions writeActions = propType
                .getWriteActions();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITEACTIONS,
                ((writeActions == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : writeActions.getAction()));
    }

    private static void setTableFeatureProperty(NextTableMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        TablesMiss tables = propType
                .getTablesMiss();
        setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLESMISS,
                (tables == null) ? new ArrayList<Short>() : tables.getTableIds());
    }

    private static void setTableFeatureProperty(NextTable propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.Tables tables = propType
                .getTables();
        setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLES,
                (tables == null) ? new ArrayList<Short>() : tables.getTableIds());
    }

    private static void setTableFeatureProperty(InstructionsMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMiss instructions = propType
                .getInstructionsMiss();
        setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS,
                (instructions == null) ? new ArrayList<Instruction>() : instructions.getInstruction());
    }

    private static void setTableFeatureProperty(Instructions propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.Instructions instructions = propType
                .getInstructions();
        setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONS,
                (instructions == null) ? new ArrayList<Instruction>() : instructions.getInstruction());
    }

    private static void setInstructionTableFeatureProperty(TableFeaturePropertiesBuilder builder,
                                                           TableFeaturesPropType type, List<Instruction> instructionList) {
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.Instruction> instructionTypeList = new ArrayList<>();

        for (Instruction currInstruction : instructionList) {
            InstructionBuilder instructionType = new InstructionBuilder();

            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction instruction = currInstruction
                    .getInstruction();
            if (instruction instanceof GoToTableCase) {
                GotoTableCaseBuilder goToTableCaseBuilder = new GotoTableCaseBuilder();
                instructionType.setInstructionChoice(goToTableCaseBuilder.build());
            } else if (instruction instanceof WriteMetadataCase) {
                WriteMetadataCaseBuilder writeMetadataCaseBuilder = new WriteMetadataCaseBuilder();
                instructionType.setInstructionChoice(writeMetadataCaseBuilder.build());
            } else if (instruction instanceof WriteActionsCase) {
                WriteActionsCaseBuilder writeActionsCaseBuilder = new WriteActionsCaseBuilder();
                instructionType.setInstructionChoice(writeActionsCaseBuilder.build());
            } else if (instruction instanceof ApplyActionsCase) {
                ApplyActionsCaseBuilder applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
                instructionType.setInstructionChoice(applyActionsCaseBuilder.build());
            } else if (instruction instanceof ClearActionsCase) {
                ClearActionsCaseBuilder clearActionsCaseBuilder = new ClearActionsCaseBuilder();
                instructionType.setInstructionChoice(clearActionsCaseBuilder.build());
            } else if (instruction instanceof MeterCase) {
                MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
                instructionType.setInstructionChoice(meterCaseBuilder.build());
            }
            // TODO: Experimeneter instructions are unhandled
            instructionTypeList.add(instructionType.build());
        }
        InstructionRelatedTableFeaturePropertyBuilder propBuilder = new InstructionRelatedTableFeaturePropertyBuilder();
        propBuilder.setInstruction(instructionTypeList);
        builder.setType(type);
        builder.addAugmentation(InstructionRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static void setNextTableFeatureProperty(TableFeaturePropertiesBuilder builder, TableFeaturesPropType type,
                                                    List<Short> tableIds) {
        List<NextTableIds> nextTableIdsList = new ArrayList<>();

        for (Short tableId : tableIds) {
            NextTableIdsBuilder nextTableId = new NextTableIdsBuilder();
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

        List<Action> actionList = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action currAction : salActions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action actionType = currAction
                    .getAction();
            ActionBuilder actionBuilder = new ActionBuilder();
            if (actionType instanceof OutputActionCase) {
                OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
                actionBuilder.setActionChoice(outputActionCaseBuilder.build());
            } else if (actionType instanceof GroupActionCase) {
                GroupCaseBuilder groupActionBuilder = new GroupCaseBuilder();
                actionBuilder.setActionChoice(groupActionBuilder.build());
            } else if (actionType instanceof CopyTtlOutCase) {
                CopyTtlOutCaseBuilder copyTtlOutCaseBuilder = new CopyTtlOutCaseBuilder();
                actionBuilder.setActionChoice(copyTtlOutCaseBuilder.build());
            } else if (actionType instanceof CopyTtlInCase) {
                CopyTtlInCaseBuilder copyTtlInCaseBuilder = new CopyTtlInCaseBuilder();
                actionBuilder.setActionChoice(copyTtlInCaseBuilder.build());
            } else if (actionType instanceof SetMplsTtlActionCase) {
                SetMplsTtlCaseBuilder setMplsTtlActionBuilder = new SetMplsTtlCaseBuilder();
                actionBuilder.setActionChoice(setMplsTtlActionBuilder.build());
            } else if (actionType instanceof DecMplsTtlCase) {
                DecMplsTtlCaseBuilder decMplsTtlCaseBuilder = new DecMplsTtlCaseBuilder();
                actionBuilder.setActionChoice(decMplsTtlCaseBuilder.build());
            } else if (actionType instanceof PushVlanActionCase) {
                PushVlanCaseBuilder pushVlanActionBuilder = new PushVlanCaseBuilder();
                actionBuilder.setActionChoice(pushVlanActionBuilder.build());
            } else if (actionType instanceof PopVlanActionCase) {
                PopVlanCaseBuilder popVlanCaseBuilder = new PopVlanCaseBuilder();
                actionBuilder.setActionChoice(popVlanCaseBuilder.build());
            } else if (actionType instanceof PushMplsActionCase) {
                PushMplsCaseBuilder pushMplsActionBuilder = new PushMplsCaseBuilder();
                actionBuilder.setActionChoice(pushMplsActionBuilder.build());
            } else if (actionType instanceof PopMplsActionCase) {
                PopMplsCaseBuilder popMplsCaseBuilder = new PopMplsCaseBuilder();
                actionBuilder.setActionChoice(popMplsCaseBuilder.build());
            } else if (actionType instanceof SetQueueActionCase) {
                SetQueueCaseBuilder setQueueActionBuilder = new SetQueueCaseBuilder();
                actionBuilder.setActionChoice(setQueueActionBuilder.build());
            } else if (actionType instanceof SetNwTtlActionCase) {
                SetNwTtlCaseBuilder setNwTtlActionBuilder = new SetNwTtlCaseBuilder();
                actionBuilder.setActionChoice(setNwTtlActionBuilder.build());
            } else if (actionType instanceof DecNwTtlCase) {
                DecNwTtlCaseBuilder decNwTtlCaseBuilder = new DecNwTtlCaseBuilder();
                actionBuilder.setActionChoice(decNwTtlCaseBuilder.build());
            } else if (actionType instanceof SetFieldCase) {
                SetFieldCaseBuilder setFieldCaseBuilder = new SetFieldCaseBuilder();
                actionBuilder.setActionChoice(setFieldCaseBuilder.build());
            } else if (actionType instanceof PushPbbActionCase) {
                PushPbbCaseBuilder pushPbbCaseBuilder = new PushPbbCaseBuilder();
                actionBuilder.setActionChoice(pushPbbCaseBuilder.build());
            } else if (actionType instanceof PopPbbActionCase) {
                PopPbbCaseBuilder popPbbCaseBuilder = new PopPbbCaseBuilder();
                actionBuilder.setActionChoice(popPbbCaseBuilder.build());
            }
            // Experimenter action is unhandled
            actionList.add(actionBuilder.build());
        }

        ActionRelatedTableFeaturePropertyBuilder propBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        propBuilder.setAction(actionList);
        builder.setType(type);
        builder.addAugmentation(ActionRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static final Map<Class<?>, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField>> SAL_TO_OF_TABLE_FEATURES;

    static {
        Builder<Class<?>, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField>> builder = ImmutableMap.builder();
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class, ArpOp.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class, ArpSha.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class, ArpSpa.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class, ArpTha.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class, ArpTpa.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class, EthDst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class, EthSrc.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class, EthType.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class, Icmpv4Code.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class, Icmpv4Type.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class, Icmpv6Code.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class, Icmpv6Type.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class, InPhyPort.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class, InPort.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class, IpDscp.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class, IpEcn.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class, IpProto.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class, Ipv4Dst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class, Ipv4Src.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class, Ipv6Dst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class, Ipv6Exthdr.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class, Ipv6Flabel.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class, Ipv6NdSll.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class, Ipv6NdTarget.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class, Ipv6NdTll.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class, Ipv6Src.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class, Metadata.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class, MplsBos.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class, MplsLabel.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class, MplsTc.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class, PbbIsid.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class, SctpDst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class, SctpSrc.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class, TcpDst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class, TcpSrc.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class, TunnelId.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class, UdpDst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc.class, UdpSrc.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class, VlanPcp.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class, VlanVid.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.class, Ipv4Dst.class);
        builder.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.class, Ipv4Src.class);
        SAL_TO_OF_TABLE_FEATURES = builder.build();
    }

    private static void setSetFieldTableFeatureProperty(
            TableFeaturePropertiesBuilder builder,
            TableFeaturesPropType type,
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFields) {
        List<MatchEntry> matchEntriesList = new ArrayList<>();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch currMatch : setFields) {
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> currMatchType = currMatch
                    .getMatchType();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();

            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField> ofTableFeatureClass
                    = SAL_TO_OF_TABLE_FEATURES.get(currMatchType);
            setMatchEntry(matchEntryBuilder, ofTableFeatureClass, currMatch.isHasMask());

            matchEntriesList.add(matchEntryBuilder.build());
        }
        OxmRelatedTableFeaturePropertyBuilder propBuilder = new OxmRelatedTableFeaturePropertyBuilder();
        propBuilder.setMatchEntry(matchEntriesList);
        builder.setType(type);
        builder.addAugmentation(OxmRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static void setMatchEntry(MatchEntryBuilder builder,
                                      Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField> field,
                                      Boolean hasMask) {
        builder.setOxmClass(OpenflowBasicClass.class);
        builder.setOxmMatchField(field);
        builder.setHasMask(hasMask);
    }
}
