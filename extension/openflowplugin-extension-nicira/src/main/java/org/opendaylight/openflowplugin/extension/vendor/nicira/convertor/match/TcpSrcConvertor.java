/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Nxm0Class;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.ofj.nxm.of.match.tcp.src.grouping.TcpSrcValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpSrcCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.oxm.container.match.entry.value.TcpSrcCaseValueBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpSrcGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmOfTcpSrcKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.tcp.src.grouping.NxmOfTcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.nxm.of.tcp.src.grouping.NxmOfTcpSrcBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Convert to/from SAL flow model to openflowjava model for TcpSrcCase.
 *
 * @author Aswin Suryanarayanan.
 */
public class TcpSrcConvertor implements ConvertorToOFJava<MatchEntry>, ConvertorFromOFJava<MatchEntry, MatchPath> {
    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(MatchEntry input, MatchPath path) {
        TcpSrcCaseValue tcpSrcCaseValue = (TcpSrcCaseValue) input.getMatchEntryValue();
        NxmOfTcpSrcBuilder tcpSrcBuilder = new NxmOfTcpSrcBuilder();
        tcpSrcBuilder.setPort(tcpSrcCaseValue.getTcpSrcValues().getPort());
        tcpSrcBuilder.setMask(tcpSrcCaseValue.getTcpSrcValues().getMask());
        return resolveAugmentation(tcpSrcBuilder.build(), path,
                NxmOfTcpSrcKey.class);
    }

    @Override
    public MatchEntry convert(Extension extension) {
        Optional<NxmOfTcpSrcGrouping> matchGrouping = MatchUtil.TCP_SRC_RESOLVER.getExtension(extension);
        if (!matchGrouping.isPresent()) {
            throw new CodecPreconditionException(extension);
        }
        TcpSrcCaseValueBuilder tcpSrcCaseValueBuilder = new TcpSrcCaseValueBuilder();
        TcpSrcValuesBuilder tcpSrcValuesBuilder = new TcpSrcValuesBuilder();
        tcpSrcValuesBuilder.setPort(matchGrouping.get().getNxmOfTcpSrc().getPort());
        tcpSrcValuesBuilder.setMask(matchGrouping.get().getNxmOfTcpSrc().getMask());
        tcpSrcCaseValueBuilder.setTcpSrcValues(tcpSrcValuesBuilder.build());
        MatchEntryBuilder ofMatch = MatchUtil
                .createDefaultMatchEntryBuilder(org.opendaylight.yang.gen.v1.urn
                                                .opendaylight.openflowjava.nx.match.rev140421.NxmOfTcpSrc.class,
                                                Nxm0Class.class, tcpSrcCaseValueBuilder.build());
        ofMatch.setHasMask(true);
        return ofMatch.build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(NxmOfTcpSrc value,
                                                                   MatchPath path, Class<? extends ExtensionKey> key) {
        switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH:
                return new ExtensionAugment<>(NxAugMatchNodesNodeTableFlow.class,
                        new NxAugMatchNodesNodeTableFlowBuilder().setNxmOfTcpSrc(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH:
                return new ExtensionAugment<>(NxAugMatchRpcGetFlowStats.class,
                        new NxAugMatchRpcGetFlowStatsBuilder().setNxmOfTcpSrc(value).build(), key);
            case PACKET_RECEIVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifPacketIn.class, new NxAugMatchNotifPacketInBuilder()
                        .setNxmOfTcpSrc(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH:
                return new ExtensionAugment<>(NxAugMatchNotifSwitchFlowRemoved.class,
                        new NxAugMatchNotifSwitchFlowRemovedBuilder().setNxmOfTcpSrc(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH:
                return new ExtensionAugment<>(NxAugMatchPacketInMessage.class,
                        new NxAugMatchPacketInMessageBuilder().setNxmOfTcpSrc(value).build(), key);
            default:
                throw new CodecPreconditionException(path);
        }
    }
}
