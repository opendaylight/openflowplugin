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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.Icmpv6NdOptionsType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.icmpv6.nd.options.type.grouping.Icmpv6NdOptionsTypeValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.options.Icmpv6NdOptionsTypeCaseValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.eric.match.rev180730.ofj.aug.eric.match.options.Icmpv6NdOptionsTypeCaseValueBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricOfIcmpv6NdOptionsTypeGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.Icmpv6NdOptionsTypeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.eric.of.icmpv6.nd.options.type.grouping.EricOfIcmpv6NdOptionsType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.eric.of.icmpv6.nd.options.type.grouping.EricOfIcmpv6NdOptionsTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Convert to/from SAL flow model to openflowjava model for Icmpv6NDOptionsTypeCase.
 */
public class Icmpv6NDOptionsTypeConvertor implements ConvertorToOFJava<MatchEntry>,
        ConvertorFromOFJava<MatchEntry, MatchPath> {

    @Override
    public ExtensionAugment<? extends Augmentation<Extension>> convert(final MatchEntry input, final MatchPath path) {
        Icmpv6NdOptionsTypeCaseValue icmpv6NdOptionsTypeCaseValue
                = (Icmpv6NdOptionsTypeCaseValue)input.getMatchEntryValue();
        return resolveAugmentation(new EricOfIcmpv6NdOptionsTypeBuilder()
                .setIcmpv6NdOptionsType(icmpv6NdOptionsTypeCaseValue.getIcmpv6NdOptionsTypeValues()
                .getIcmpv6NdOptionsType()).build(), path, Icmpv6NdOptionsTypeKey.VALUE);
    }

    @Override
    public MatchEntry convert(final Extension extension) {
        Optional<EricOfIcmpv6NdOptionsTypeGrouping> matchGrouping = MatchUtil.ICMPV6_ND_OPTIONS_TYPE_RESOLVER
                .getExtension(extension);
        if (matchGrouping.isEmpty()) {
            throw new CodecPreconditionException(extension);
        }
        Uint8 value = matchGrouping.orElseThrow().getEricOfIcmpv6NdOptionsType().getIcmpv6NdOptionsType();
        Icmpv6NdOptionsTypeCaseValueBuilder icmpv6NdOptionsTypeCaseValueBuilder
                = new Icmpv6NdOptionsTypeCaseValueBuilder();
        icmpv6NdOptionsTypeCaseValueBuilder.setIcmpv6NdOptionsTypeValues(new Icmpv6NdOptionsTypeValuesBuilder()
                .setIcmpv6NdOptionsType(value).build());
        return MatchUtil.createDefaultMatchEntryBuilder(Icmpv6NdOptionsType.VALUE, EricExpClass.VALUE,
                icmpv6NdOptionsTypeCaseValueBuilder.build()).build();
    }

    private static ExtensionAugment<? extends Augmentation<Extension>> resolveAugmentation(
            final EricOfIcmpv6NdOptionsType value, final MatchPath path, final ExtensionKey key) {
        return switch (path) {
            case FLOWS_STATISTICS_UPDATE_MATCH -> new ExtensionAugment<>(EricAugMatchNodesNodeTableFlow.class,
                new EricAugMatchNodesNodeTableFlowBuilder().setEricOfIcmpv6NdOptionsType(value).build(), key);
            case FLOWS_STATISTICS_RPC_MATCH -> new ExtensionAugment<>(EricAugMatchRpcGetFlowStats.class,
                new EricAugMatchRpcGetFlowStatsBuilder().setEricOfIcmpv6NdOptionsType(value).build(), key);
            case PACKET_RECEIVED_MATCH -> new ExtensionAugment<>(EricAugMatchNotifPacketIn.class,
                new EricAugMatchNotifPacketInBuilder().setEricOfIcmpv6NdOptionsType(value).build(), key);
            case SWITCH_FLOW_REMOVED_MATCH -> new ExtensionAugment<>(EricAugMatchNotifSwitchFlowRemoved.class,
                new EricAugMatchNotifSwitchFlowRemovedBuilder().setEricOfIcmpv6NdOptionsType(value).build(), key);
            case PACKET_IN_MESSAGE_MATCH -> new ExtensionAugment<>(EricAugMatchPacketInMessage.class,
                new EricAugMatchPacketInMessageBuilder().setEricOfIcmpv6NdOptionsType(value).build(), key);
        };
    }
}