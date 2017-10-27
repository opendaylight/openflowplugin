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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.nx.match.tun.ipv4.dst.grouping.TunIpv4DstValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TunIpv4DstCaseValueBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxTunIpv4DstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.dst.grouping.NxmNxTunIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.nx.tun.ipv4.dst.grouping.NxmNxTunIpv4DstBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

public class TunIPv4DstConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath>{
    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmNxTunIpv4Dst value,
            MatchPath path, Class<? extends ExtensionKey> key) {
            switch (path) {
                case FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH:
                        return new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                                new NxAugMatchNodesNodeTableFlowBuilder().setNxmNxTunIpv4Dst(value).build(), key);
                case RPCFLOWSSTATISTICS_FLOWANDSTATISTICSMAPLIST_MATCH:
                    return new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                            new NxAugMatchRpcGetFlowStatsBuilder().setNxmNxTunIpv4Dst(value).build(), key);
                case PACKETRECEIVED_MATCH:
                        return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                                new NxAugMatchNotifPacketInBuilder().setNxmNxTunIpv4Dst(value).build(), key);
                case SWITCHFLOWREMOVED_MATCH:
                        return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmNxTunIpv4Dst(value).build(), key);
                default:
                    throw new CodecPreconditionException(path);
            }
    }

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(
            MatchEntry input, MatchPath path) {
        TunIpv4DstCaseValue tunIpv4DstCaseValue = ((TunIpv4DstCaseValue) input.getMatchEntryValue());
        return resolveAugmentation(new NxmNxTunIpv4DstBuilder().setIpv4Address(MatchUtil.longToIpv4Address(tunIpv4DstCaseValue.getTunIpv4DstValues().getValue())).build(), path,
                NxmNxTunIpv4DstKey.class);
    }

    @Override
    public MatchEntry convert(Extension extension) {
        Optional<NxmNxTunIpv4DstGrouping> matchGrouping = MatchUtil.tunIpv4DstResolver.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        Ipv4Address value = matchGrouping.get().getNxmNxTunIpv4Dst().getIpv4Address();

        TunIpv4DstCaseValueBuilder tunIpv4DstCaseValueBuilder = new TunIpv4DstCaseValueBuilder();
        tunIpv4DstCaseValueBuilder.setTunIpv4DstValues(new TunIpv4DstValuesBuilder()
                .setValue(MatchUtil.ipv4ToLong(value)).build());
        return MatchUtil.createDefaultMatchEntryBuilder(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxTunIpv4Dst.class,
                Nxm1Class.class,
                tunIpv4DstCaseValueBuilder.build()).build();
    }

}
