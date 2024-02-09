/*
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.ActionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.InstructionRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.NextTableRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.OxmRelatedTableFeaturePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.next.table.related.table.feature.property.NextTableIdsBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.table.update.UpdatedTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchKey;
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
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a MD-SAL table features into the OF library table features.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<List<TableFeatures>> ofFeatures = convertorManager..convert(salTableFeatures, data);
 * }
 * </pre>
 */
public class TableFeaturesConvertor extends Convertor<
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures,
        List<TableFeatures>,
        VersionConvertorData> {

    private static final Logger LOG = LoggerFactory.getLogger(TableFeaturesConvertor.class);
    private static final List<Class<?>> TYPES = List.of(
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures.class,
        UpdatedTable.class);

    private static final ImmutableMap<MatchField,
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField> SAL_TO_OF_TABLE_FEATURES =
            ImmutableMap.<MatchField,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField>builder()
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpOp.VALUE, ArpOp.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSha.VALUE, ArpSha.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpSpa.VALUE, ArpSpa.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTha.VALUE, ArpTha.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.ArpTpa.VALUE, ArpTpa.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthDst.VALUE, EthDst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthSrc.VALUE, EthSrc.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.EthType.VALUE, EthType.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Code.VALUE,
                    Icmpv4Code.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv4Type.VALUE,
                    Icmpv4Type.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Code.VALUE,
                    Icmpv6Code.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Icmpv6Type.VALUE,
                    Icmpv6Type.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPhyPort.VALUE,
                    InPhyPort.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.InPort.VALUE, InPort.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpDscp.VALUE, IpDscp.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpEcn.VALUE, IpEcn.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.IpProto.VALUE, IpProto.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Dst.VALUE, Ipv4Dst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv4Src.VALUE, Ipv4Src.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Dst.VALUE, Ipv6Dst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Exthdr.VALUE,
                    Ipv6Exthdr.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Flabel.VALUE,
                    Ipv6Flabel.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdSll.VALUE,
                    Ipv6NdSll.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTarget.VALUE,
                    Ipv6NdTarget.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6NdTll.VALUE,
                    Ipv6NdTll.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Ipv6Src.VALUE, Ipv6Src.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.Metadata.VALUE, Metadata.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsBos.VALUE, MplsBos.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsLabel.VALUE,
                    MplsLabel.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MplsTc.VALUE, MplsTc.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.PbbIsid.VALUE, PbbIsid.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpDst.VALUE, SctpDst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.SctpSrc.VALUE, SctpSrc.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpDst.VALUE, TcpDst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpSrc.VALUE, TcpSrc.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelId.VALUE, TunnelId.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpDst.VALUE, UdpDst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.UdpSrc.VALUE, UdpSrc.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanPcp.VALUE, VlanPcp.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.VlanVid.VALUE, VlanVid.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst.VALUE,
                    Ipv4Dst.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src.VALUE,
                    Ipv4Src.VALUE)
                .put(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TcpFlags.VALUE, TcpFlags.VALUE)
                .build();

    private static List<TableFeatureProperties> toTableProperties(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features
                .TableProperties tableProperties) {
        return tableProperties == null ? List.of() : tableProperties.nonnullTableFeatureProperties().stream()
            .map(property -> {
                final var propBuilder = new TableFeaturePropertiesBuilder();
                final var propType = property.getTableFeaturePropType();
                setTableFeatureProperty(propType);

                if (propType instanceof Instructions instructions) {
                    setTableFeatureProperty(instructions, propBuilder);
                } else if (propType instanceof InstructionsMiss instructionsMiss) {
                    setTableFeatureProperty(instructionsMiss, propBuilder);
                } else if (propType instanceof NextTable nextTable) {
                    setTableFeatureProperty(nextTable, propBuilder);
                } else if (propType instanceof NextTableMiss nextTableMis) {
                    setTableFeatureProperty(nextTableMis, propBuilder);
                } else if (propType instanceof WriteActions writeActions) {
                    setTableFeatureProperty(writeActions, propBuilder);
                } else if (propType instanceof WriteActionsMiss writeActionsMiss) {
                    setTableFeatureProperty(writeActionsMiss, propBuilder);
                } else if (propType instanceof ApplyActions applyActions) {
                    setTableFeatureProperty(applyActions, propBuilder);
                } else if (propType instanceof ApplyActionsMiss applyActionsMiss) {
                    setTableFeatureProperty(applyActionsMiss, propBuilder);
                } else if (propType instanceof Match match) {
                    setTableFeatureProperty(match, propBuilder);
                } else if (propType instanceof Wildcards wildcards) {
                    setTableFeatureProperty(wildcards, propBuilder);
                } else if (propType instanceof WriteSetfield writeSetfield) {
                    setTableFeatureProperty(writeSetfield, propBuilder);
                } else if (propType instanceof WriteSetfieldMiss writeSetfieldMiss) {
                    setTableFeatureProperty(writeSetfieldMiss, propBuilder);
                } else if (propType instanceof ApplySetfield applySetfield) {
                    setTableFeatureProperty(applySetfield, propBuilder);
                } else if (propType instanceof ApplySetfieldMiss applySetfieldMiss) {
                    setTableFeatureProperty(applySetfieldMiss, propBuilder);
                }

                // Experimenter and Experimenter miss Table features are unhandled

                return propBuilder.build();
            })
            .collect(Collectors.toList());
    }

    private static void setTableFeatureProperty(final TableFeaturePropType propType) {
        LOG.debug("Unknown TableFeaturePropType [{}]", propType.getClass());
    }

    private static void setTableFeatureProperty(final ApplySetfieldMiss propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        Map<SetFieldMatchKey, SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.apply.setfield.miss.ApplySetfieldMiss applySetfieldMiss = propType.getApplySetfieldMiss();

        if (null != applySetfieldMiss) {
            setFieldMatch = applySetfieldMiss.nonnullSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYSETFIELDMISS,
                setFieldMatch == null ? Map.of() : setFieldMatch);
    }

    private static void setTableFeatureProperty(final ApplySetfield propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        Map<SetFieldMatchKey, SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.apply.setfield.ApplySetfield applySetfield = propType.getApplySetfield();

        if (null != applySetfield) {
            setFieldMatch = applySetfield.nonnullSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYSETFIELD,
                setFieldMatch == null ? Map.of() : setFieldMatch);
    }

    private static void setTableFeatureProperty(final WriteSetfieldMiss propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        Map<SetFieldMatchKey, SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.write.setfield.miss.WriteSetfieldMiss writeSetfieldMiss = propType.getWriteSetfieldMiss();

        if (null != writeSetfieldMiss) {
            setFieldMatch = writeSetfieldMiss.nonnullSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITESETFIELDMISS,
                setFieldMatch == null ? Map.of() : setFieldMatch);
    }

    private static void setTableFeatureProperty(final WriteSetfield propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        Map<SetFieldMatchKey, SetFieldMatch> setFieldMatch = null;
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.write.setfield.WriteSetfield writeSetField = propType.getWriteSetfield();

        if (null != writeSetField) {
            setFieldMatch = writeSetField.nonnullSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITESETFIELD,
                setFieldMatch == null ? Map.of() : setFieldMatch);
    }

    private static void setTableFeatureProperty(final Wildcards propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        Map<SetFieldMatchKey, SetFieldMatch> setFieldMatch = null;
        WildcardSetfield wildcardSetField = propType.getWildcardSetfield();

        if (null != wildcardSetField) {
            setFieldMatch = wildcardSetField.nonnullSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWILDCARDS,
                setFieldMatch == null ? Map.of() : setFieldMatch);
    }

    private static void setTableFeatureProperty(final Match propType, final TableFeaturePropertiesBuilder propBuilder) {
        MatchSetfield matchSetField = propType.getMatchSetfield();
        Map<SetFieldMatchKey, SetFieldMatch> setFieldMatch = null;

        if (null != matchSetField) {
            setFieldMatch = matchSetField.nonnullSetFieldMatch();
        }

        setSetFieldTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTMATCH,
                setFieldMatch == null ? Map.of() : setFieldMatch);
    }

    private static void setTableFeatureProperty(final ApplyActionsMiss propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.apply.actions.miss.ApplyActionsMiss applyActionsMiss = propType.getApplyActionsMiss();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYACTIONSMISS,
                applyActionsMiss == null ? List.of() : applyActionsMiss.nonnullAction());
    }

    private static void setTableFeatureProperty(final ApplyActions propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.apply.actions.ApplyActions applyActions = propType.getApplyActions();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTAPPLYACTIONS,
                applyActions == null ? List.of() : applyActions.nonnullAction());
    }

    private static void setTableFeatureProperty(final WriteActionsMiss propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.write.actions.miss.WriteActionsMiss writeActionsMiss = propType.getWriteActionsMiss();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITEACTIONSMISS,
                writeActionsMiss == null ? List.of() : writeActionsMiss.nonnullAction());
    }

    private static void setTableFeatureProperty(final WriteActions propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.write.actions.WriteActions writeActions = propType.getWriteActions();
        setActionTableFeatureProperty(
                propBuilder,
                TableFeaturesPropType.OFPTFPTWRITEACTIONS,
                writeActions == null ? List.of() : writeActions.nonnullAction());
    }

    private static void setTableFeatureProperty(final NextTableMiss propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        TablesMiss tables = propType.getTablesMiss();
        setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLESMISS,
                tables == null ? new ArrayList<>() : tables.getTableIds());
    }

    private static void setTableFeatureProperty(final NextTable propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.next.table.Tables tables = propType.getTables();
        setNextTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTNEXTTABLES,
                tables == null ? new ArrayList<>() : tables.getTableIds());
    }

    private static void setTableFeatureProperty(final InstructionsMiss propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.instructions.miss.InstructionsMiss instructions = propType.getInstructionsMiss();
        setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONSMISS,
                instructions == null ? List.of() : instructions.nonnullInstruction());
    }

    private static void setTableFeatureProperty(final Instructions propType,
            final TableFeaturePropertiesBuilder propBuilder) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop
            .type.instructions.Instructions instructions = propType.getInstructions();
        setInstructionTableFeatureProperty(propBuilder, TableFeaturesPropType.OFPTFPTINSTRUCTIONS,
                instructions == null ? List.of() : instructions.nonnullInstruction());
    }

    private static void setInstructionTableFeatureProperty(final TableFeaturePropertiesBuilder builder,
            final TableFeaturesPropType type, final List<Instruction> instructionList) {
        final var instructionTypeList = new ArrayList<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
            .instruction.rev130731.instructions.grouping.Instruction>(instructionList.size());

        for (var currInstruction : instructionList) {
            final var instructionType = new InstructionBuilder();
            final var instruction = currInstruction.getInstruction();

            if (instruction instanceof GoToTableCase) {
                instructionType.setInstructionChoice(new GotoTableCaseBuilder().build());
            } else if (instruction instanceof WriteMetadataCase) {
                instructionType.setInstructionChoice(new WriteMetadataCaseBuilder().build());
            } else if (instruction instanceof WriteActionsCase) {
                instructionType.setInstructionChoice(new WriteActionsCaseBuilder().build());
            } else if (instruction instanceof ApplyActionsCase) {
                instructionType.setInstructionChoice(new ApplyActionsCaseBuilder().build());
            } else if (instruction instanceof ClearActionsCase) {
                instructionType.setInstructionChoice(new ClearActionsCaseBuilder().build());
            } else if (instruction instanceof MeterCase) {
                instructionType.setInstructionChoice(new MeterCaseBuilder().build());
            }

            // TODO: Experimenter instructions are unhandled
            instructionTypeList.add(instructionType.build());
        }

        builder.setType(type)
            .addAugmentation(new InstructionRelatedTableFeaturePropertyBuilder()
            .setInstruction(instructionTypeList)
            .build());
    }

    private static void setNextTableFeatureProperty(final TableFeaturePropertiesBuilder builder,
            final TableFeaturesPropType type, final List<Uint8> tableIds) {
        builder.setType(type).addAugmentation(new NextTableRelatedTableFeaturePropertyBuilder()
            .setNextTableIds(tableIds.stream()
                .map(tableId -> new NextTableIdsBuilder().setTableId(tableId).build())
                .collect(Collectors.toList()))
            .build());
    }

    private static void setActionTableFeatureProperty(final TableFeaturePropertiesBuilder builder,
            final TableFeaturesPropType type,
            final List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>
                  salActions) {
        final var actionList = new ArrayList<Action>(salActions.size());
        for (var currAction : salActions) {
            final var actionType = currAction.getAction();
            final var actionBuilder = new ActionBuilder();

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

        builder.setType(type)
            .addAugmentation(new ActionRelatedTableFeaturePropertyBuilder().setAction(actionList).build());
    }

    private static void setSetFieldTableFeatureProperty(
            final TableFeaturePropertiesBuilder builder,
            final TableFeaturesPropType type,
            final Map<SetFieldMatchKey, SetFieldMatch> setFields) {
        List<MatchEntry> matchEntriesList = new ArrayList<>();

        for (SetFieldMatch currMatch : setFields.values()) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField currMatchType =
                currMatch.getMatchType();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField ofTableFeatureClass =
                SAL_TO_OF_TABLE_FEATURES.get(currMatchType);
            setMatchEntry(matchEntryBuilder, ofTableFeatureClass, currMatch.getHasMask());
            matchEntriesList.add(matchEntryBuilder.build());
        }

        builder.setType(type).addAugmentation(new OxmRelatedTableFeaturePropertyBuilder()
            .setMatchEntry(matchEntriesList)
            .build());
    }

    private static void setMatchEntry(final MatchEntryBuilder builder,
            final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField field,
            final Boolean hasMask) {
        if (TcpFlags.VALUE.equals(field)) {
            builder.setOxmClass(ExperimenterClass.VALUE);
        } else {
            builder.setOxmClass(OpenflowBasicClass.VALUE);
        }
        builder.setOxmMatchField(field);
        builder.setHasMask(hasMask);
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return  TYPES;
    }

    @Override
    public List<TableFeatures> convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableFeatures source,
            final VersionConvertorData data) {
        List<TableFeatures> ofTableFeaturesList = new ArrayList<>();
        TableFeaturesBuilder ofTableFeatures = new TableFeaturesBuilder();

        for (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures
                salTableFeatures : source.nonnullTableFeatures().values()) {
            ofTableFeatures.setTableId(salTableFeatures.getTableId());
            ofTableFeatures.setName(salTableFeatures.getName());
            ofTableFeatures.setMetadataMatch(salTableFeatures.getMetadataMatch());
            ofTableFeatures.setMetadataWrite(salTableFeatures.getMetadataWrite());
            ofTableFeatures.setMaxEntries(salTableFeatures.getMaxEntries());

            if (salTableFeatures.getConfig() != null) {
                ofTableFeatures.setConfig(new TableConfig(salTableFeatures.getConfig().getDEPRECATEDMASK()));
            }

            ofTableFeatures.setTableFeatureProperties(toTableProperties(salTableFeatures.getTableProperties()));
            ofTableFeaturesList.add(ofTableFeatures.build());
        }

        return ofTableFeaturesList;
    }
}
