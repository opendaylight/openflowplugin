/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.match;

import java.util.Arrays;
import java.util.Collection;
import org.opendaylight.openflowplugin.api.openflow.protocol.converter.Converter;
import org.opendaylight.openflowplugin.protocol.converter.common.ConvertorProcessor;
import org.opendaylight.openflowplugin.protocol.converter.data.VersionDatapathIdConverterData;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalArpOpCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalArpShaCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalArpSpaCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalArpThaCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalArpTpaCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalEthDstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalEthSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalEthTypeCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalExperimenterIdCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIcmpv4CodeCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIcmpv4TypeCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIcmpv6CodeCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIcmpv6TypeCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalInPhyPortCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalInPortCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpDscpCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpEcnCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpProtoCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv4DstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv4SrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6DstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6ExthdrCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6FlabelCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6NdSllCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6NdTargetCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6NdTllCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalIpv6SrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalMetadataCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalMplsBosCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalMplsLabelCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalMplsTcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalPbbIsidCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalSctpDstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalSctpSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalTcpDstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalTcpSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalTunnelIdCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalTunnelIpv4DstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalTunnelIpv4SrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalUdpDstCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalUdpSrcCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalVlanPcpCase;
import org.opendaylight.openflowplugin.protocol.converter.match.cases.OfToSalVlanVidCase;
import org.opendaylight.openflowplugin.protocol.converter.match.data.MatchResponseConverterData;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchEntriesGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.MatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src;

/**
 * Converts Openflow 1.3+ specific flow match to MD-SAL format flow
 * match
 *
 * Example usage:
 * <pre>
 * {@code
 * VersionDatapathIdConverterData data = new VersionDatapathIdConverterData(version);
 * data.setDatapathId(datapathId);
 * Optional<MatchBuilder> salMatch = converterManager.convert(ofMatch, data);
 * }
 * </pre>
 */
public class MatchResponseConverter extends Converter<MatchEntriesGrouping, MatchBuilder, VersionDatapathIdConverterData> {
    private static final ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConverterData> OF_TO_SAL_PROCESSOR = new ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConverterData>()
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

    private static final ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConverterData> OF_TO_SAL_TUNNEL_PROCESSOR = new ConvertorProcessor<MatchEntryValue, MatchBuilder, MatchResponseConverterData>()
            .addCase(new OfToSalTunnelIpv4SrcCase())
            .addCase(new OfToSalTunnelIpv4DstCase());
    private static final java.util.List<Class<?>> TYPES = Arrays.asList(Match.class, SetFieldAction.class);

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public MatchBuilder convert(MatchEntriesGrouping source, VersionDatapathIdConverterData datapathIdConverterData) {
        final MatchBuilder matchBuilder = new MatchBuilder();

        final MatchResponseConverterData data = new MatchResponseConverterData(datapathIdConverterData.getVersion());
        data.setDatapathId(datapathIdConverterData.getDatapathId());
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
                OF_TO_SAL_TUNNEL_PROCESSOR.process(ofMatch.getMatchEntryValue(), data, getConverterExecutor());
            } else {
                data.setOxmMatchField(ofMatch.getOxmMatchField());
                OF_TO_SAL_PROCESSOR.process(ofMatch.getMatchEntryValue(), data, getConverterExecutor());
            }
        }

        return matchBuilder;
    }
}
