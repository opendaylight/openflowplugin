/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.util.Arrays;
import java.util.Collection;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalArpOpCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalArpShaCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalArpSpaCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalArpThaCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalArpTpaCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalEthDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalEthSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalEthTypeCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIcmpv4CodeCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIcmpv4TypeCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIcmpv6CodeCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIcmpv6TypeCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalInPhyPortCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalInPortCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpDscpCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpEcnCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpProtoCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv4DstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv4SrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6DstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6ExthdrCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6FlabelCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6NdSllCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6NdTargetCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6NdTllCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalIpv6SrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalMetadataCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalMplsBosCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalMplsLabelCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalMplsTcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalPbbIsidCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalSctpDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalSctpSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalTcpDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalExperimenterIdCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalTcpSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalTunnelIdCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalTunnelIpv4DstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalTunnelIpv4SrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalUdpDstCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalUdpSrcCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalVlanPcpCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.cases.OfToSalVlanVidCase;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.data.MatchResponseConvertorData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagsMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchEntriesGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.MatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Converts Openflow 1.3+ specific flow match to MD-SAL format flow
 * match
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(version);
 * data.setDatapathId(datapathId);
 * Optional<MatchBuilder> salMatch = convertorManager.convert(ofMatch, data);
 * }
 * </pre>
 */
public class MatchResponseConvertor extends Convertor<MatchEntriesGrouping, MatchBuilder, VersionDatapathIdConvertorData> {
    private static final ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConvertorData> OF_TO_SAL_PROCESSOR = new ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConvertorData>()
            .addCase(new OfToSalInPortCase())
            .addCase(new OfToSalInPhyPortCase())
            .addCase(new OfToSalMetadataCase())
            .addCase(new OfToSalEthSrcCase())
            .addCase(new OfToSalEthDstCase())
            .addCase(new OfToSalEthTypeCase())
            .addCase(new OfToSalVlanVidCase())
            .addCase(new OfToSalVlanPcpCase())
            .addCase(new OfToSalIpDscpCase())
            .addCase(new OfToSalIpEcnCase())
            .addCase(new OfToSalIpProtoCase())
            .addCase(new OfToSalTcpSrcCase())
            .addCase(new OfToSalTcpDstCase())
            .addCase(new OfToSalUdpSrcCase())
            .addCase(new OfToSalUdpDstCase())
            .addCase(new OfToSalSctpSrcCase())
            .addCase(new OfToSalSctpDstCase())
            .addCase(new OfToSalIcmpv4TypeCase())
            .addCase(new OfToSalIcmpv4CodeCase())
            .addCase(new OfToSalIcmpv6TypeCase())
            .addCase(new OfToSalIcmpv6CodeCase())
            .addCase(new OfToSalIpv4SrcCase())
            .addCase(new OfToSalIpv4DstCase())
            .addCase(new OfToSalArpOpCase())
            .addCase(new OfToSalArpSpaCase())
            .addCase(new OfToSalArpTpaCase())
            .addCase(new OfToSalArpShaCase())
            .addCase(new OfToSalArpThaCase())
            .addCase(new OfToSalIpv6SrcCase())
            .addCase(new OfToSalIpv6DstCase())
            .addCase(new OfToSalIpv6FlabelCase())
            .addCase(new OfToSalIpv6NdTargetCase())
            .addCase(new OfToSalIpv6NdSllCase())
            .addCase(new OfToSalIpv6NdTllCase())
            .addCase(new OfToSalIpv6ExthdrCase())
            .addCase(new OfToSalMplsLabelCase())
            .addCase(new OfToSalMplsBosCase())
            .addCase(new OfToSalMplsTcCase())
            .addCase(new OfToSalPbbIsidCase())
            .addCase(new OfToSalTunnelIdCase())
            .addCase(new OfToSalExperimenterIdCase());

    private static final ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConvertorData> OF_TO_SAL_TUNNEL_PROCESSOR = new ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConvertorData>()
            .addCase(new OfToSalTunnelIpv4SrcCase())
            .addCase(new OfToSalTunnelIpv4DstCase());
    private static final java.util.List<Class<? extends DataContainer>> TYPES = Arrays.asList(Match.class, SetFieldAction.class);

    @Override
    public Collection<Class<? extends DataContainer>> getTypes() {
        return  TYPES;
    }

    @Override
    public MatchBuilder convert(MatchEntriesGrouping source, VersionDatapathIdConvertorData datapathIdConvertorData) {
        final MatchBuilder matchBuilder = new MatchBuilder();

        final MatchResponseConvertorData data = new MatchResponseConvertorData(datapathIdConvertorData.getVersion());
        data.setDatapathId(datapathIdConvertorData.getDatapathId());
        data.setMatchBuilder(matchBuilder);
        data.setEthernetMatchBuilder(new EthernetMatchBuilder());
        data.setVlanMatchBuilder(new VlanMatchBuilder());
        data.setIpMatchBuilder(new IpMatchBuilder());
        data.setTcpMatchBuilder(new TcpMatchBuilder());
        data.setUdpMatchBuilder(new UdpMatchBuilder());
        data.setSctpMatchBuilder(new SctpMatchBuilder());
        data.setIcmpv4MatchBuilder(new Icmpv4MatchBuilder());
        data.setIcmpv6MatchBuilder(new Icmpv6MatchBuilder());
        data.setIpv4MatchBuilder(new Ipv4MatchBuilder());
        data.setIpv4MatchArbitraryBitMaskBuilder(new Ipv4MatchArbitraryBitMaskBuilder());
        data.setIpv6MatchArbitraryBitMaskBuilder(new Ipv6MatchArbitraryBitMaskBuilder());
        data.setArpMatchBuilder(new ArpMatchBuilder());
        data.setIpv6MatchBuilder(new Ipv6MatchBuilder());
        data.setProtocolMatchFieldsBuilder(new ProtocolMatchFieldsBuilder());
        data.setTunnelIpv4MatchBuilder(new TunnelIpv4MatchBuilder());
        data.setTcpFlagsMatchBuilder(new TcpFlagsMatchBuilder());

        for (MatchEntry ofMatch : source.getMatchEntry()) {
            if (TunnelIpv4Dst.class.isAssignableFrom(ofMatch.getOxmMatchField()) ||
                    TunnelIpv4Src.class.isAssignableFrom(ofMatch.getOxmMatchField())) {
                /**
                 * TODO: Fix TunnelIpv4Src and Ipv4Dst, because current implementation do not work
                 * TunnelIpv4Src and TunnelIpv4Dst are not compatible with
                 * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField
                 * and so you cannot even set them to OxmMatchField.
                 * Creation of TunnelIpv4SrcCase and TunnelIpv4DstCase in
                 * org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value
                 * and proper use of it can fix this bug.
                 */
                OF_TO_SAL_TUNNEL_PROCESSOR.process(ofMatch.getMatchEntryValue(), data, getConvertorExecutor());
            } else {
                data.setOxmMatchField(ofMatch.getOxmMatchField());
                OF_TO_SAL_PROCESSOR.process(ofMatch.getMatchEntryValue(), data, getConvertorExecutor());
            }
        }

        return matchBuilder;
    }
}
