/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import com.google.common.base.Optional;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm1Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.src.grouping.TunIpv4SrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4SrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4SrcCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcGetFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4SrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4SrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.src.grouping.NxmNxTunIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.src.grouping.NxmNxTunIpv4SrcBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class TunIPv4SrcConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath>{
    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmNxTunIpv4Src value,
            MatchPath path, Class<? extends ExtensionKey> key) {
            switch (path) {
                case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                        return new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                                new NxAugMatchNodesNodeTableFlowBuilder().setNxmNxTunIpv4Src(value).build(), key);
                case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH:
                    return new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                            new NxAugMatchRpcGetFlowStatsBuilder().setNxmNxTunIpv4Src(value).build(), key);
                case PACKETRECEIVED_MATCH:
                        return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                                new NxAugMatchNotifPacketInBuilder().setNxmNxTunIpv4Src(value).build(), key);
                case SWITCHFLOWREMOVED_MATCH:
                        return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxTunIpv4Src(value).build(), key);
                default:
                    throw new CodecPreconditionException(path);
            }
    }

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(
            MatchEntry input, MatchPath path) {
        TunIpv4SrcCaseValue tunIpv4SrcCaseValue = ((TunIpv4SrcCaseValue) input.getMatchEntryValue());
        return resolveAugmentation(new NxmNxTunIpv4SrcBuilder().setIpv4Address(MatchUtil.longToIpv4Address(tunIpv4SrcCaseValue.getTunIpv4SrcValues().getValue())).build(), path,
                NxmNxTunIpv4SrcKey.class);
    }

    @Override
    public MatchEntry convert(Extension extension) {
        Optional<NxmNxTunIpv4SrcGrouping> matchGrouping = MatchUtil.tunIpv4SrcResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Ipv4Address value = matchGrouping.get().getNxmNxTunIpv4Src().getIpv4Address();

        TunIpv4SrcCaseValueBuilder tunIpv4SrcCaseValueBuilder = new TunIpv4SrcCaseValueBuilder();
        tunIpv4SrcCaseValueBuilder.setTunIpv4SrcValues(new TunIpv4SrcValuesBuilder()
                .setValue(MatchUtil.ipv4ToLong(value)).build());
        return MatchUtil.createDefaultMatchEntryBuilder(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Src.class,
                Nxm1Class.class,
                tunIpv4SrcCaseValueBuilder.build()).build();
    }
}
