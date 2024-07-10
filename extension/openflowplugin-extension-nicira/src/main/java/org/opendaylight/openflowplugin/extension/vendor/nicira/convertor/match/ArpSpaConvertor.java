/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.IpConverter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpSpaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpSpaCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.arp.spa.grouping.ArpSpaValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchPacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpSpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.spa.grouping.NxmOfArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.spa.grouping.NxmOfArpSpaBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Convert to/from SAL flow model to openflowjava model for ArpSpaCase.
 *
 * @author msunal
 */
public class ArpSpaConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        ArpSpaCaseValue arpSpaCaseValue = (ArpSpaCaseValue) input.getMatchEntryValue();
        Ipv4Address ipv4Address = IpConverter.longToIpv4Address(arpSpaCaseValue.getArpSpaValues().getValue().toJava());
        return resolveAugmentation(new NxmOfArpSpaBuilder().setIpv4Address(ipv4Address).build(), path,
                NxmOfArpSpaKey.VALUE);
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        final var matchGrouping = MatchUtil.ARP_SPA_RESOLVER.findExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        return MatchUtil.createDefaultMatchEntryBuilder(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfArpSpa.VALUE,
            Nxm0Class.VALUE, new ArpSpaCaseValueBuilder()
                .setArpSpaValues(new ArpSpaValuesBuilder()
                    .setValue(Uint32.valueOf(
                        IpConverter.ipv4AddressToLong(matchGrouping.orElseThrow().getNxmOfArpSpa().getIpv4Address())))
                    .build())
                .build())
            .build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(final NxmOfArpSpa value,
            final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                new NxAugMatchNodesNodeTableFlowBuilder().setNxmOfArpSpa(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                new NxAugMatchRpcGetFlowStatsBuilder().setNxmOfArpSpa(value).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                new NxAugMatchNotifPacketInBuilder().setNxmOfArpSpa(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmOfArpSpa(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                new NxAugMatchPacketInMessageBuilder().setNxmOfArpSpa(value).build(), key);
        };
    }
}
