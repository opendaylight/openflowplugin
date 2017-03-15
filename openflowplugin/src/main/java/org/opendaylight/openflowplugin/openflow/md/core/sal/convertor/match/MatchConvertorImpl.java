/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.OFApprovedExperimenterIds;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfArpMatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfIpv4MatchArbitraryBitMaskCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfIpv4MatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfIpv6MatchArbitraryBitMaskCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfIpv6MatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfSctpMatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfTcpMatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfTunnelIpv4MatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.SalToOfUdpMatchCase;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.Pbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlagsContainerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.oxm.container.match.entry.value.experimenter.id._case.TcpFlagsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.ExperimenterIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.oxm.container.match.entry.value.experimenter.id._case.ExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsLabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsTcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.type._case.EthTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.phy.port._case.InPhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.proto._case.IpProtoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.bos._case.MplsBosBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.label._case.MplsLabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.tc._case.MplsTcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.pbb.isid._case.PbbIsidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tunnel.id._case.TunnelIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;

/**
 * Utility class for converting a MD-SAL Flow into the OF flow mod
 */
public class MatchConvertorImpl implements MatchConvertor<List<MatchEntry>> {
    private static final ConvertorProcessor<Layer3Match, List<MatchEntry>, VersionConvertorData> LAYER3_PROCESSOR = new ConvertorProcessor<Layer3Match, List<MatchEntry>, VersionConvertorData>()
            .addCase(new SalToOfIpv4MatchArbitraryBitMaskCase())
            .addCase(new SalToOfIpv4MatchCase())
            .addCase(new SalToOfTunnelIpv4MatchCase())
            .addCase(new SalToOfArpMatchCase())
            .addCase(new SalToOfIpv6MatchArbitraryBitMaskCase())
            .addCase(new SalToOfIpv6MatchCase());

    private static final ConvertorProcessor<Layer4Match, List<MatchEntry>, VersionConvertorData> LAYER4_PROCESSOR = new ConvertorProcessor<Layer4Match, List<MatchEntry>, VersionConvertorData>()
            .addCase(new SalToOfTcpMatchCase())
            .addCase(new SalToOfUdpMatchCase())
            .addCase(new SalToOfSctpMatchCase());

    private static final byte[] VLAN_VID_MASK = new byte[]{16, 0};

    private static void layer3Match(final List<MatchEntry> matchEntryList, final Layer3Match layer3Match, ConvertorExecutor convertorExecutor) {
        java.util.Optional<List<MatchEntry>> result = LAYER3_PROCESSOR.process(layer3Match, convertorExecutor);

        if (result.isPresent()) {
            matchEntryList.addAll(result.get());
        }
    }

    private static void layer4Match(final List<MatchEntry> matchEntryList, final Layer4Match layer4Match, ConvertorExecutor convertorExecutor) {
        java.util.Optional<List<MatchEntry>> result = LAYER4_PROCESSOR.process(layer4Match, convertorExecutor);

        if (result.isPresent()) {
            matchEntryList.addAll(result.get());
        }
    }

    private static void inPortMatch(final List<MatchEntry> matchEntryList, NodeConnectorId inPort) {
        if (inPort == null) {
            return;
        }

        //TODO: currently this matchconverter is mapped to OF1.3 in MatchReactorMappingFactory. Will need to revisit during 1.4+
        final long portNumber = InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, inPort);
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(InPort.class);
        InPortCaseBuilder caseBuilder = new InPortCaseBuilder();
        InPortBuilder portBuilder = new InPortBuilder();
        portBuilder.setPortNumber(new PortNumber(portNumber));
        caseBuilder.setInPort(portBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        matchEntryList.add(matchEntryBuilder.build());
    }

    private static void inPhyPortMatch(final List<MatchEntry> matchEntryList, NodeConnectorId inPhyPort) {
        if (inPhyPort == null) {
            return;
        }

        //TODO: currently this matchconverter is mapped to OF1.3 in MatchReactorMappingFactory. Will need to revisit during 1.4+
        final long portNumber = InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, inPhyPort);
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(InPhyPort.class);
        InPhyPortCaseBuilder caseBuilder = new InPhyPortCaseBuilder();
        InPhyPortBuilder portBuilder = new InPhyPortBuilder();
        portBuilder.setPortNumber(new PortNumber(portNumber));
        caseBuilder.setInPhyPort(portBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());
        matchEntryList.add(matchEntryBuilder.build());
    }

    private static void metadataMatch(final List<MatchEntry> matchEntryList,
                                      final org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata metadata) {
        if (metadata == null) {
            return;
        }

        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        boolean hasmask = metadata.getMetadataMask() != null;
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(Metadata.class);
        MetadataCaseBuilder metadataCaseBuilder = new MetadataCaseBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder metadataBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder();
        metadataBuilder.setMetadata(ByteUtil.convertBigIntegerToNBytes(metadata.getMetadata(), OFConstants.SIZE_OF_LONG_IN_BYTES));

        if (hasmask) {
            metadataBuilder.setMask(ByteUtil.convertBigIntegerToNBytes(metadata.getMetadataMask(), OFConstants.SIZE_OF_LONG_IN_BYTES));
        }

        metadataCaseBuilder.setMetadata(metadataBuilder.build());
        matchEntryBuilder.setMatchEntryValue(metadataCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasmask);
        matchEntryList.add(matchEntryBuilder.build());
    }

    private static void tunnelMatch(final List<MatchEntry> matchEntryList,
                                    final org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel tunnel) {
        if (tunnel == null) {
            return;
        }

        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        TunnelIdCaseBuilder tunnelIdCaseBuilder = new TunnelIdCaseBuilder();
        TunnelIdBuilder tunnelIdBuilder = new TunnelIdBuilder();
        boolean hasMask = tunnel.getTunnelMask() != null;

        if (hasMask) {
            tunnelIdBuilder.setMask(ByteUtil.convertBigIntegerToNBytes(tunnel.getTunnelMask(), OFConstants.SIZE_OF_LONG_IN_BYTES));
        }

        tunnelIdBuilder.setTunnelId(ByteUtil.convertBigIntegerToNBytes(tunnel.getTunnelId(), OFConstants.SIZE_OF_LONG_IN_BYTES));
        tunnelIdCaseBuilder.setTunnelId(tunnelIdBuilder.build());
        matchEntryBuilder.setMatchEntryValue(tunnelIdCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasMask);
        matchEntryBuilder.setOxmMatchField(TunnelId.class);
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryList.add(matchEntryBuilder.build());
    }

    private static void protocolMatchFields(final List<MatchEntry> matchEntryList,
                                            final ProtocolMatchFields protocolMatchFields) {
        if (protocolMatchFields == null) {
            return;
        }

        if (protocolMatchFields.getMplsLabel() != null) {
            matchEntryList.add(toOfMplsLabel(protocolMatchFields.getMplsLabel()));
        }

        if (protocolMatchFields.getMplsBos() != null) {
            matchEntryList.add(toOfMplsBos(protocolMatchFields.getMplsBos()));
        }

        if (protocolMatchFields.getMplsTc() != null) {
            matchEntryList.add(toOfMplsTc(protocolMatchFields.getMplsTc()));
        }

        if (protocolMatchFields.getPbb() != null) {
            matchEntryList.add(toOfMplsPbb(protocolMatchFields.getPbb()));
        }
    }

    private static void icmpv6Match(final List<MatchEntry> matchEntryList, final Icmpv6Match icmpv6Match) {
        if (icmpv6Match == null) {
            return;
        }

        if (icmpv6Match.getIcmpv6Type() != null) {
            matchEntryList.add(toOfIcmpv6Type(icmpv6Match.getIcmpv6Type()));
        }

        if (icmpv6Match.getIcmpv6Code() != null) {
            matchEntryList.add(toOfIcmpv6Code(icmpv6Match.getIcmpv6Code()));
        }
    }

    private static void icmpv4Match(final List<MatchEntry> matchEntryList, final Icmpv4Match icmpv4Match) {
        if (icmpv4Match == null) {
            return;
        }

        if (icmpv4Match.getIcmpv4Type() != null) {
            matchEntryList.add(toOfIcmpv4Type(icmpv4Match.getIcmpv4Type()));
        }

        if (icmpv4Match.getIcmpv4Code() != null) {
            matchEntryList.add(toOfIcmpv4Code(icmpv4Match.getIcmpv4Code()));
        }
    }

    private static void ipMatch(final List<MatchEntry> matchEntryList, final IpMatch ipMatch) {
        if (ipMatch == null) {
            return;
        }

        if (ipMatch.getIpDscp() != null) {
            matchEntryList.add(MatchConvertorUtil.toOfIpDscp(ipMatch.getIpDscp()));
        }

        if (ipMatch.getIpEcn() != null) {
            matchEntryList.add(toOfIpEcn(ipMatch.getIpEcn()));
        }

        if (ipMatch.getIpProtocol() != null) {
            matchEntryList.add(toOfIpProto(ipMatch.getIpProtocol()));
        }
    }

    private static void vlanMatch(final List<MatchEntry> matchEntryList, final VlanMatch vlanMatch) {
        if (vlanMatch == null) {
            return;
        }

        if (vlanMatch.getVlanId() != null) {
            VlanId vlanId = vlanMatch.getVlanId();
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(VlanVid.class);
            VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
            VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
            boolean setCfiBit = false;
            Integer vidEntryValue = 0;
            boolean hasmask = false;

            if (Boolean.TRUE.equals(vlanId.isVlanIdPresent())) {
                setCfiBit = true;
                if (vlanId.getVlanId() != null) {
                    vidEntryValue = vlanId.getVlanId().getValue();
                }

                hasmask = vidEntryValue == 0;
                if (hasmask) {
                    vlanVidBuilder.setMask(VLAN_VID_MASK);
                }
            }

            vlanVidBuilder.setCfiBit(setCfiBit);
            vlanVidBuilder.setVlanVid(vidEntryValue);
            vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
            matchEntryBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasmask);
            matchEntryList.add(matchEntryBuilder.build());
        }

        if (vlanMatch.getVlanPcp() != null) {
            matchEntryList.add(MatchConvertorUtil.toOfVlanPcp(vlanMatch.getVlanPcp()));
        }
    }

    private static void ethernetMatch(final List<MatchEntry> matchEntryList, final EthernetMatch ethernetMatch) {
        if (ethernetMatch == null) {
            return;
        }

        EthernetDestination ethernetDestination = ethernetMatch.getEthernetDestination();
        if (ethernetDestination != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(EthDst.class);
            EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
            EthDstBuilder ethDstBuilder = new EthDstBuilder();
            ethDstBuilder.setMacAddress(ethernetDestination.getAddress());
            boolean hasMask = ethernetDestination.getMask() != null;

            if (hasMask) {
                ethDstBuilder.setMask(ByteBufUtils.macAddressToBytes(ethernetDestination.getMask().getValue()));
            }

            ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            matchEntryList.add(matchEntryBuilder.build());
        }

        EthernetSource ethernetSource = ethernetMatch.getEthernetSource();
        if (ethernetSource != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryBuilder.setOxmMatchField(EthSrc.class);
            EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
            EthSrcBuilder ethDstBuilder = new EthSrcBuilder();
            ethDstBuilder.setMacAddress(ethernetSource.getAddress());
            boolean hasMask = ethernetSource.getMask() != null;

            if (hasMask) {
                ethDstBuilder.setMask(ByteBufUtils.macAddressToBytes(ethernetSource.getMask().getValue()));
            }

            ethSrcCaseBuilder.setEthSrc(ethDstBuilder.build());
            matchEntryBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            matchEntryList.add(matchEntryBuilder.build());
        }

        if (ethernetMatch.getEthernetType() != null) {
            matchEntryList.add(toOfEthernetType(ethernetMatch.getEthernetType()));
        }
    }

    private static void tcpFlagsMatch(List<MatchEntry> matchEntryList, TcpFlagsMatch tcpFlagsMatch) {
        ExperimenterIdCaseBuilder expIdCaseBuilder = new ExperimenterIdCaseBuilder();
        if (tcpFlagsMatch != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            matchEntryBuilder.setOxmClass(ExperimenterClass.class);
            matchEntryBuilder.setHasMask(false);
            matchEntryBuilder.setOxmMatchField(TcpFlags.class);

            TcpFlagsContainerBuilder tcpFlagsContainerBuilder = new TcpFlagsContainerBuilder();
            TcpFlagsBuilder tcpFlagsBuilder = new TcpFlagsBuilder();
            tcpFlagsBuilder.setFlags(tcpFlagsMatch.getTcpFlags());
            if(tcpFlagsMatch.getTcpFlagsMask() != null) {
                matchEntryBuilder.setHasMask(true);
                tcpFlagsBuilder.setMask(ByteUtil.unsignedShortToBytes(tcpFlagsMatch.getTcpFlagsMask()));
            }
            tcpFlagsContainerBuilder.setTcpFlags(tcpFlagsBuilder.build());

            //Set experimenter ID.
            ExperimenterBuilder experimenterBuilder = new ExperimenterBuilder();
            experimenterBuilder.setExperimenter(new ExperimenterId(OFApprovedExperimenterIds.MATCH_TCP_FLAGS_EXP_ID));
            expIdCaseBuilder.setExperimenter(experimenterBuilder.build());

            expIdCaseBuilder.addAugmentation(TcpFlagsContainer.class, tcpFlagsContainerBuilder.build());
            matchEntryBuilder.setMatchEntryValue(expIdCaseBuilder.build());
            matchEntryList.add(matchEntryBuilder.build());
        }
    }

    private static MatchEntry toOfMplsPbb(final Pbb pbb) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        boolean hasmask = pbb.getPbbMask() != null;
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(PbbIsid.class);
        PbbIsidCaseBuilder pbbIsidCaseBuilder = new PbbIsidCaseBuilder();
        PbbIsidBuilder pbbIsidBuilder = new PbbIsidBuilder();
        pbbIsidBuilder.setIsid(pbb.getPbbIsid());

        if (hasmask) {
            pbbIsidBuilder.setMask(ByteUtil.unsignedMediumToBytes(pbb.getPbbMask()));
        }

        pbbIsidCaseBuilder.setPbbIsid(pbbIsidBuilder.build());
        matchEntryBuilder.setMatchEntryValue(pbbIsidCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasmask);
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMplsTc(final Short mplsTc) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(MplsTc.class);
        MplsTcCaseBuilder mplsTcCaseBuilder = new MplsTcCaseBuilder();
        MplsTcBuilder mplsTcBuilder = new MplsTcBuilder();
        mplsTcBuilder.setTc(mplsTc);
        mplsTcCaseBuilder.setMplsTc(mplsTcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsTcCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMplsBos(final Short mplsBos) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(MplsBos.class);
        MplsBosCaseBuilder mplsBosCaseBuilder = new MplsBosCaseBuilder();
        MplsBosBuilder mplsBosBuilder = new MplsBosBuilder();
        mplsBosBuilder.setBos(mplsBos != 0);
        mplsBosCaseBuilder.setMplsBos(mplsBosBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsBosCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMplsLabel(final Long mplsLabel) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(MplsLabel.class);
        MplsLabelCaseBuilder mplsLabelCaseBuilder = new MplsLabelCaseBuilder();
        MplsLabelBuilder mplsLabelBuilder = new MplsLabelBuilder();
        mplsLabelBuilder.setMplsLabel(mplsLabel);
        mplsLabelCaseBuilder.setMplsLabel(mplsLabelBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsLabelCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfEthernetType(final EthernetType ethernetType) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(EthType.class);
        EthTypeCaseBuilder ethTypeCaseBuilder = new EthTypeCaseBuilder();
        EthTypeBuilder ethTypeBuilder = new EthTypeBuilder();
        EtherType etherType = new EtherType(ethernetType.getType().getValue().intValue());
        ethTypeBuilder.setEthType(etherType);
        ethTypeCaseBuilder.setEthType(ethTypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethTypeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv4Type(final Short icmpv4Type) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv4Type.class);
        Icmpv4TypeCaseBuilder icmpv4TypeCaseBuilder = new Icmpv4TypeCaseBuilder();
        Icmpv4TypeBuilder icmpv4TypeBuilder = new Icmpv4TypeBuilder();
        icmpv4TypeBuilder.setIcmpv4Type(icmpv4Type);
        icmpv4TypeCaseBuilder.setIcmpv4Type(icmpv4TypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv4TypeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv4Code(final Short icmpv4Code) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv4Code.class);
        Icmpv4CodeCaseBuilder icmpv4CodeCaseBuilder = new Icmpv4CodeCaseBuilder();
        Icmpv4CodeBuilder icmpv4CodeBuilder = new Icmpv4CodeBuilder();
        icmpv4CodeBuilder.setIcmpv4Code(icmpv4Code);
        icmpv4CodeCaseBuilder.setIcmpv4Code(icmpv4CodeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv4CodeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv6Type(final Short icmpv6Type) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv6Type.class);
        Icmpv6TypeCaseBuilder icmpv6TypeCaseBuilder = new Icmpv6TypeCaseBuilder();
        Icmpv6TypeBuilder icmpv6TypeBuilder = new Icmpv6TypeBuilder();
        icmpv6TypeBuilder.setIcmpv6Type(icmpv6Type);
        icmpv6TypeCaseBuilder.setIcmpv6Type(icmpv6TypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv6TypeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv6Code(final Short icmpv6Code) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv6Code.class);
        Icmpv6CodeCaseBuilder icmpv6CodeCaseBuilder = new Icmpv6CodeCaseBuilder();
        Icmpv6CodeBuilder icmpv6CodeBuilder = new Icmpv6CodeBuilder();
        icmpv6CodeBuilder.setIcmpv6Code(icmpv6Code);
        icmpv6CodeCaseBuilder.setIcmpv6Code(icmpv6CodeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv6CodeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIpProto(final Short ipProtocol) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(IpProto.class);
        IpProtoCaseBuilder ipProtoCaseBuilder = new IpProtoCaseBuilder();
        IpProtoBuilder ipProtoBuilder = new IpProtoBuilder();
        ipProtoBuilder.setProtocolNumber(ipProtocol);
        ipProtoCaseBuilder.setIpProto(ipProtoBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipProtoCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIpEcn(final Short ipEcn) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(IpEcn.class);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn(ipEcn);
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    @Override
    public List<MatchEntry> convert(final Match match, ConvertorExecutor convertorExecutor) {
        List<MatchEntry> result = new ArrayList<>();

        if (match == null) {
            return result;
        }

        inPortMatch(result, match.getInPort());
        inPhyPortMatch(result, match.getInPhyPort());
        metadataMatch(result, match.getMetadata());
        ethernetMatch(result, match.getEthernetMatch());
        vlanMatch(result, match.getVlanMatch());
        ipMatch(result, match.getIpMatch());
        layer4Match(result, match.getLayer4Match(), convertorExecutor);
        icmpv4Match(result, match.getIcmpv4Match());
        icmpv6Match(result, match.getIcmpv6Match());
        layer3Match(result, match.getLayer3Match(), convertorExecutor);
        protocolMatchFields(result, match.getProtocolMatchFields());
        tunnelMatch(result, match.getTunnel());
        tcpFlagsMatch(result, match.getTcpFlagsMatch());

        /**
         * TODO: EXTENSION PROPOSAL (match, MD-SAL to OFJava)
         * - we might need version for conversion and for key
         */
        Optional<GeneralExtensionListGrouping> extensionListOpt = ExtensionResolvers.getMatchExtensionResolver().getExtension(match);
        if (extensionListOpt.isPresent()) {
            List<ExtensionList> extensionListList = extensionListOpt.get().getExtensionList();
            for (ExtensionList extensionItem : extensionListList) {
                // TODO: get real version
                ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(extensionItem.getExtensionKey(), OFConstants.OFP_VERSION_1_3);
                ConvertorToOFJava<MatchEntry> convertor = OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                if (convertor == null) {
                    throw new IllegalStateException("No converter found for key: " + key.toString());
                }
                MatchEntry ofMatch = convertor.convert(extensionItem.getExtension());
                result.add(ofMatch);
            }
        }

        return result;
    }
}
