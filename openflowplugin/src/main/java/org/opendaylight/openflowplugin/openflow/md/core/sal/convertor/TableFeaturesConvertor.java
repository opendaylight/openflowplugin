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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.InstructionRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NextTableRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmRelatedTableFeatureProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.table.features.properties.container.table.feature.properties.NextTableIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.table.features.properties.container.table.feature.properties.NextTableIdsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.instructions.grouping.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableConfig;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
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

import com.google.common.collect.Ordering;

/**
 * Utility class for converting a MD-SAL Table features into the OF library table
 * features.
 */
public class TableFeaturesConvertor {
    private static final Logger LOG = LoggerFactory.getLogger(TableFeaturesConvertor.class);

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
            return Collections.<TableFeatureProperties> emptyList();
        }
        List<TableFeatureProperties> ofTablePropertiesList = new ArrayList<>();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties>
            sortedTableProperties =
                Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties>build())
                    .sortedCopy(tableProperties.getTableFeatureProperties());

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties
            property : sortedTableProperties) {
        	TableFeaturePropertiesBuilder propBuilder = new TableFeaturePropertiesBuilder();
        	
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType propType = property
                    .getTableFeaturePropType();
            
            setTableFeatureProperty(propType, propBuilder);
            if (propType instanceof Instructions) {
                setTableFeatureProperty((Instructions)propType, propBuilder);
            } else if (propType instanceof InstructionsMiss) {
                setTableFeatureProperty((InstructionsMiss)propType, propBuilder);
            } else if (propType instanceof NextTable) {
                setTableFeatureProperty((NextTable)propType, propBuilder);
            } else if (propType instanceof NextTableMiss) {
                setTableFeatureProperty((NextTableMiss)propType, propBuilder);
            } else if (propType instanceof WriteActions) {
                setTableFeatureProperty((WriteActions)propType, propBuilder);
            } else if (propType instanceof WriteActionsMiss) {
                setTableFeatureProperty((WriteActionsMiss)propType, propBuilder);
            } else if (propType instanceof ApplyActions) {
                setTableFeatureProperty((ApplyActions)propType, propBuilder);
            } else if (propType instanceof ApplyActionsMiss) {
                setTableFeatureProperty((ApplyActionsMiss)propType, propBuilder);
            } else if (propType instanceof Match) {
                setTableFeatureProperty((Match)propType, propBuilder);
            } else if (propType instanceof Wildcards) {
                setTableFeatureProperty((Wildcards)propType, propBuilder);
            } else if (propType instanceof WriteSetfield) {
                setTableFeatureProperty((WriteSetfield)propType, propBuilder);
            } else if (propType instanceof WriteSetfieldMiss) {
                setTableFeatureProperty((WriteSetfieldMiss)propType, propBuilder);
            } else if (propType instanceof ApplySetfield) {
                setTableFeatureProperty((ApplySetfield)propType, propBuilder);
            } else if (propType instanceof ApplySetfieldMiss) {
                setTableFeatureProperty((ApplySetfieldMiss)propType, propBuilder);
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
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.miss.ApplySetfieldMiss applySetfieldMiss = ((ApplySetfieldMiss) propType).getApplySetfieldMiss();

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
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.setfield.ApplySetfield applySetfield = ((ApplySetfield) propType).getApplySetfield();

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
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.miss.WriteSetfieldMiss writeSetfieldMiss = ((WriteSetfieldMiss) propType).getWriteSetfieldMiss();

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
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.setfield.WriteSetfield writeSetField = ((WriteSetfield) propType).getWriteSetfield();

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
        WildcardSetfield wildcardSetField = ((Wildcards) propType).getWildcardSetfield();

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
        MatchSetfield matchSetField = ((Match) propType).getMatchSetfield();
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFieldMatch = null;

        if( null != matchSetField) {
            setFieldMatch = matchSetField.getSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTMATCH,
                ((setFieldMatch == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch>()
                        : setFieldMatch));
    }

    private static void setTableFeatureProperty(ApplyActionsMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.miss.ApplyActionsMiss applyActionsMiss = ((ApplyActionsMiss) propType)
                .getApplyActionsMiss();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS,
                ((applyActionsMiss == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : applyActionsMiss.getAction()));
    }

    private static void setTableFeatureProperty(ApplyActions propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.apply.actions.ApplyActions applyActions = ((ApplyActions) propType)
                .getApplyActions();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYACTIONS,
                ((applyActions == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : applyActions.getAction()));
    }

    private static void setTableFeatureProperty(WriteActionsMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.miss.WriteActionsMiss writeActionsMiss = ((WriteActionsMiss) propType)
                .getWriteActionsMiss();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS,
                ((writeActionsMiss == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : writeActionsMiss.getAction()));
    }

    private static void setTableFeatureProperty(WriteActions propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.write.actions.WriteActions writeActions = ((WriteActions) propType)
                .getWriteActions();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITEACTIONS,
                ((writeActions == null) ? new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>()
                        : writeActions.getAction()));
    }

    private static void setTableFeatureProperty(NextTableMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        TablesMiss tables = ((NextTableMiss) propType)
                .getTablesMiss();
        setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLESMISS,
                (tables == null) ? new ArrayList<Short>() : tables.getTableIds());
    }

    private static void setTableFeatureProperty(NextTable propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.next.table.Tables tables = ((NextTable) propType)
                .getTables();
        setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLES,
                (tables == null) ? new ArrayList<Short>() : tables.getTableIds());
    }

    private static void setTableFeatureProperty(InstructionsMiss propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.miss.InstructionsMiss instructions = ((InstructionsMiss) propType)
                .getInstructionsMiss();
        setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS,
                (instructions == null) ? new ArrayList<Instruction>() : instructions.getInstruction());
    }

    private static void setTableFeatureProperty(Instructions propType, TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.instructions.Instructions instructions = ((Instructions) propType)
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
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.GotoTable.class);
            } else if (instruction instanceof WriteMetadataCase) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteMetadata.class);
            } else if (instruction instanceof WriteActionsCase) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.WriteActions.class);
            } else if (instruction instanceof ApplyActionsCase) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ApplyActions.class);
            } else if (instruction instanceof ClearActionsCase) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.ClearActions.class);
            } else if (instruction instanceof MeterCase) {
                instructionType
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.instruction.rev130731.Meter.class);
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
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Output.class);
            } else if (actionType instanceof GroupActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Group.class);
            } else if (actionType instanceof CopyTtlOutCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlOut.class);
            } else if (actionType instanceof CopyTtlInCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.CopyTtlIn.class);
            } else if (actionType instanceof SetMplsTtlActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetMplsTtl.class);
            } else if (actionType instanceof DecMplsTtlCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecMplsTtl.class);
            } else if (actionType instanceof PushVlanActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushVlan.class);
            } else if (actionType instanceof PopVlanActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopVlan.class);
            } else if (actionType instanceof PushMplsActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushMpls.class);
            } else if (actionType instanceof PopMplsActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopMpls.class);
            } else if (actionType instanceof SetQueueActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetQueue.class);
            } else if (actionType instanceof SetNwTtlActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetNwTtl.class);
            } else if (actionType instanceof DecNwTtlCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.DecNwTtl.class);
            } else if (actionType instanceof SetFieldCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.SetField.class);
            } else if (actionType instanceof PushPbbActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PushPbb.class);
            } else if (actionType instanceof PopPbbActionCase) {
                actionBuilder
                        .setType(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.PopPbb.class);
            }
            // Experimenter action is unhandled
            actionList.add(actionBuilder.build());
        }
        ActionRelatedTableFeaturePropertyBuilder propBuilder = new ActionRelatedTableFeaturePropertyBuilder();
        propBuilder.setAction(actionList);
        builder.setType(type);
        builder.addAugmentation(ActionRelatedTableFeatureProperty.class, propBuilder.build());
    }

    private static Map<Class<?>, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField>> SAL_TO_OF_TABLE_FEATURES = new HashMap<>();
    static {
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.class, ArpOp.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.class, ArpSha.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.class, ArpSpa.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.class, ArpTha.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.class, ArpTpa.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.class, EthDst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.class, EthSrc.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.class, EthType.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.class, Icmpv4Code.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.class, Icmpv4Type.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.class, Icmpv6Code.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.class, Icmpv6Type.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.class, InPhyPort.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.class, InPort.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.class, IpDscp.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.class, IpEcn.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.class, IpProto.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.class, Ipv4Dst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.class, Ipv4Src.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.class, Ipv6Dst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.class, Ipv6Exthdr.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.class, Ipv6Flabel.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.class, Ipv6NdSll.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.class, Ipv6NdTarget.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.class, Ipv6NdTll.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.class, Ipv6Src.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.class, Metadata.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.class, MplsBos.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.class, MplsLabel.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.class, MplsTc.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.class, PbbIsid.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.class, SctpDst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.class, SctpSrc.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.class, TcpDst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.class, TcpSrc.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.class, TunnelId.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class, UdpDst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.class, UdpSrc.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.class, VlanPcp.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.class, VlanVid.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.class, TunnelIpv4Dst.class);
        SAL_TO_OF_TABLE_FEATURES.put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.class, TunnelIpv4Src.class);
    }

    private static void setSetFieldTableFeatureProperty(
            TableFeaturePropertiesBuilder builder,
            TableFeaturesPropType type,
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch> setFields) {
        List<MatchEntries> matchEntriesList = new ArrayList<>();
        
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch currMatch : setFields) {
            Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> currMatchType = currMatch
                    .getMatchType();
            MatchEntriesBuilder matchEntryBuilder = new MatchEntriesBuilder();

            if (currMatchType
                    .equals(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlag.class)) {

                //FIXME: move to extensible support
                // TODO: Move to seperate bundle as soon as extension are supported
                setMatchEntry(matchEntryBuilder, TcpFlag.class, currMatch.isHasMask());
            } else {
                Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField> ofTableFeatureClass
                = SAL_TO_OF_TABLE_FEATURES.get(currMatchType);
                setMatchEntry(matchEntryBuilder, ofTableFeatureClass, currMatch.isHasMask());
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
