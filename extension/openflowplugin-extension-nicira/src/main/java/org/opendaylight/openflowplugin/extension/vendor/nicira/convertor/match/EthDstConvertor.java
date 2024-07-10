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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.EthDstCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.aug.nx.match.EthDstCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.eth.dst.grouping.EthDstValuesBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfEthDstKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.eth.dst.grouping.NxmOfEthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.eth.dst.grouping.NxmOfEthDstBuilder;
import org.opendaylight.yangtools.binding.Augmentation;

/**
 * Convert to/from SAL flow model to openflowjava model for EthDstCase.
 *
 * @author msunal
 */
public class EthDstConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        EthDstCaseValue ethDstCaseValue = (EthDstCaseValue) input.getMatchEntryValue();
        return resolveAugmentation(
                new NxmOfEthDstBuilder().setMacAddress(ethDstCaseValue.getEthDstValues().getMacAddress()).build(), path,
                NxmOfEthDstKey.VALUE);
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        final var matchGrouping = MatchUtil.ETH_DST_RESOLVER.findExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        return MatchUtil.createDefaultMatchEntryBuilder(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmOfEthDst.VALUE,
            Nxm0Class.VALUE,
            new EthDstCaseValueBuilder()
                .setEthDstValues(new EthDstValuesBuilder()
                    .setMacAddress(matchGrouping.orElseThrow().getNxmOfEthDst().getMacAddress())
                    .build())
                .build())
            .build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(final NxmOfEthDst value,
            final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                new NxAugMatchNodesNodeTableFlowBuilder().setNxmOfEthDst(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                new NxAugMatchRpcGetFlowStatsBuilder().setNxmOfEthDst(value).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifPacketIn.class,
                new NxAugMatchNotifPacketInBuilder().setNxmOfEthDst(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmOfEthDst(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                new NxAugMatchPacketInMessageBuilder().setNxmOfEthDst(value).build(), key);
        };
    }
}
