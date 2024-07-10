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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpTpaCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.ArpTpaCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.arp.tpa.grouping.ArpTpaValuesBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfArpTpaKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.tpa.grouping.NxmOfArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.arp.tpa.grouping.NxmOfArpTpaBuilder;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Convert to/from SAL flow model to openflowjava model for ArpTpaCase.
 *
 * @author msunal
 */
public class ArpTpaConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        ArpTpaCaseValue arpTpaCaseValue = (ArpTpaCaseValue) input.getMatchEntryValue();
        Ipv4Address ipv4Address = IpConverter.longToIpv4Address(arpTpaCaseValue.getArpTpaValues().getValue());
        return resolveAugmentation(new NxmOfArpTpaBuilder().setIpv4Address(ipv4Address).build(), path,
                NxmOfArpTpaKey.VALUE);
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        final var matchGrouping = MatchUtil.ARP_TPA_RESOLVER.findExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        return MatchUtil.createDefaultMatchEntryBuilder(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfArpTpa.VALUE,
            Nxm0Class.VALUE, new ArpTpaCaseValueBuilder()
                .setArpTpaValues(new ArpTpaValuesBuilder()
                    .setValue(Uint32.valueOf(IpConverter.ipv4AddressToLong(
                        matchGrouping.orElseThrow().getNxmOfArpTpa().getIpv4Address())))
                    .build())
                .build())
            .build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(final NxmOfArpTpa value,
            final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                new NxAugMatchNodesNodeTableFlowBuilder().setNxmOfArpTpa(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                new NxAugMatchRpcGetFlowStatsBuilder().setNxmOfArpTpa(value).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                new NxAugMatchNotifPacketInBuilder().setNxmOfArpTpa(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmOfArpTpa(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                new NxAugMatchPacketInMessageBuilder().setNxmOfArpTpa(value).build(), key);
        };
    }
}
