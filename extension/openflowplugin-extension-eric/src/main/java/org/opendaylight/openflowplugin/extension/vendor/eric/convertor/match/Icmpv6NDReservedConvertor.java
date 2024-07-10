/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.eric.convertor.match;

import java.util.Optional;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.ExtensionAugment;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.vendor.eric.convertor.CodecPreconditionException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EricExpClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdReserved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.reserved.grouping.Icmpv6NdReservedValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.reserved.Icmpv6NdReservedCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.reserved.Icmpv6NdReservedCaseValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchPacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcGetFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcGetFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricOfIcmpv6NdReservedGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.Icmpv6NdReservedKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.eric.of.icmpv6.nd.reserved.grouping.EricOfIcmpv6NdReserved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.eric.of.icmpv6.nd.reserved.grouping.EricOfIcmpv6NdReservedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Convert to/from SAL flow model to openflowjava model for Icmpv6NDReservedCase.
 */
public class Icmpv6NDReservedConvertor implements ConvertorToOFJava<MatchEntry>,
        ConvertorFromOFJava<MatchEntry, MatchPath> {

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        Icmpv6NdReservedCaseValue icmpv6NdReservedCaseValue = (Icmpv6NdReservedCaseValue) input.getMatchEntryValue();
        return resolveAugmentation(new EricOfIcmpv6NdReservedBuilder().setIcmpv6NdReserved(
                icmpv6NdReservedCaseValue.getIcmpv6NdReservedValues().getIcmpv6NdReserved()).build(), path,
                Icmpv6NdReservedKey.VALUE);
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        Optional<EricOfIcmpv6NdReservedGrouping> matchGrouping
                = MatchUtil.ICMPV6_ND_RESERVED_RESOLVER.getExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        Uint32 value = matchGrouping.orElseThrow().getEricOfIcmpv6NdReserved().getIcmpv6NdReserved();
        Icmpv6NdReservedCaseValueBuilder icmpv6NdReservedCaseValueBuilder = new Icmpv6NdReservedCaseValueBuilder();
        icmpv6NdReservedCaseValueBuilder.setIcmpv6NdReservedValues(new Icmpv6NdReservedValuesBuilder()
                .setIcmpv6NdReserved(value).build());
        return MatchUtil.createDefaultMatchEntryBuilder(Icmpv6NdReserved.VALUE, EricExpClass.VALUE,
                icmpv6NdReservedCaseValueBuilder.build()).build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(
            final EricOfIcmpv6NdReserved value, final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(EricAugMatchNodesNodeTableFlow.class,
                new EricAugMatchNodesNodeTableFlowBuilder().setEricOfIcmpv6NdReserved(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(EricAugMatchRpcGetFlowStats.class,
                new EricAugMatchRpcGetFlowStatsBuilder().setEricOfIcmpv6NdReserved(value).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(EricAugMatchNotifPacketIn.class,
                new EricAugMatchNotifPacketInBuilder().setEricOfIcmpv6NdReserved(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(EricAugMatchNotifSwitchFlowRemoved.class,
                new EricAugMatchNotifSwitchFlowRemovedBuilder().setEricOfIcmpv6NdReserved(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(EricAugMatchPacketInMessage.class,
                new EricAugMatchPacketInMessageBuilder().setEricOfIcmpv6NdReserved(value).build(), key);
        };
    }
}