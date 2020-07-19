/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketInBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifSwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchPacketInMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class MatchUtil {
    private static final MacAddress ZERO_MAC_ADDRESS = new MacAddress("00:00:00:00:00:00");
    private static final Ipv4Address ZERO_IPV4_ADDRESS = new Ipv4Address("0.0.0.0");
    private static final FlowWildcardsV10 FULL_WILDCARDS =
            new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true);

    private static final Map<Class<? extends Match>, Function<Match, Match>> TRANSFORMERS = ImmutableMap
            .<Class<? extends Match>, Function<Match, Match>>builder()
            .put(SetField.class, match -> {
                final SetFieldBuilder matchBuilder = new SetFieldBuilder(match);

                resolveExtensions(match).ifPresent(extensionLists -> matchBuilder
                        .addAugmentation(new GeneralAugMatchNodesNodeTableFlowWriteActionsSetFieldBuilder()
                                        .setExtensionList(extensionLists)
                                        .build()));

                return matchBuilder.build();
            })
            .put(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                    .Match.class, (match) -> {
                    final MatchBuilder matchBuilder = new MatchBuilder(match);

                    resolveExtensions(match).ifPresent(extensionLists -> matchBuilder
                            .addAugmentation(new GeneralAugMatchNodesNodeTableFlowBuilder()
                                            .setExtensionList(extensionLists)
                                            .build()));

                    return matchBuilder.build();
                })
            .put(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed
                    .Match.class, (match) -> {
                    final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed
                            .MatchBuilder matchBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types
                            .rev131026.flow.mod.removed.MatchBuilder(match);

                    resolveExtensions(match).ifPresent(extensionLists -> matchBuilder
                            .addAugmentation(new GeneralAugMatchNotifSwitchFlowRemovedBuilder()
                                            .setExtensionList(extensionLists)
                                            .build()));

                    return matchBuilder.build();
                })
            .put(org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received
                    .Match.class, (match) -> {
                    final org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.received
                            .MatchBuilder matchBuilder =
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service
                            .rev130709.packet.received.MatchBuilder(match);

                    resolveExtensions(match).ifPresent(extensionLists -> matchBuilder
                            .addAugmentation(new GeneralAugMatchNotifPacketInBuilder()
                                            .setExtensionList(extensionLists)
                                            .build()));

                    return matchBuilder.build();
                })
            .put(org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.in.message
                    .Match.class, (match) -> {
                    final org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.packet.in.message
                            .MatchBuilder matchBuilder =
                            new org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service
                            .rev130709.packet.in.message.MatchBuilder(match);

                    resolveExtensions(match).ifPresent(extensionLists -> matchBuilder
                            .addAugmentation(new GeneralAugMatchPacketInMessageBuilder()
                                            .setExtensionList(extensionLists)
                                            .build()));

                    return matchBuilder.build();
                })
            .build();

    private MatchUtil() {
        throw new IllegalStateException("This class should not be instantiated.");
    }


    public static MatchV10Builder createEmptyV10Match() {
        return new MatchV10Builder()
                .setDlDst(ZERO_MAC_ADDRESS)
                .setDlSrc(ZERO_MAC_ADDRESS)
                .setDlType(Uint16.ZERO)
                .setDlVlan(Uint16.ZERO)
                .setDlVlanPcp(Uint8.ZERO)
                .setInPort(Uint16.ZERO)
                .setNwDst(ZERO_IPV4_ADDRESS)
                .setNwDstMask(Uint8.ZERO)
                .setNwProto(Uint8.ZERO)
                .setNwSrc(ZERO_IPV4_ADDRESS)
                .setNwSrcMask(Uint8.ZERO)
                .setNwTos(Uint8.ZERO)
                .setTpDst(Uint16.ZERO)
                .setTpSrc(Uint16.ZERO)
                .setWildcards(FULL_WILDCARDS);
    }

    @Nullable
    public static <T extends Match> T transformMatch(@Nullable final Match match,
                                                     @NonNull final Class<T> implementedInterface) {
        if (match == null) {
            return null;
        }

        if (implementedInterface.equals(match.implementedInterface())) {
            return implementedInterface.cast(match);
        }

        final Function<Match, Match> matchMatchFunction = TRANSFORMERS.get(implementedInterface);
        return matchMatchFunction == null ? null : implementedInterface.cast(matchMatchFunction.apply(match));
    }

    private static Optional<List<ExtensionList>> resolveExtensions(final Match match) {
        return ExtensionResolvers
                .getMatchExtensionResolver()
                .getExtension(match)
                .flatMap(matchExtension ->
                        Optional.ofNullable(matchExtension.nonnullExtensionList().values()
                        .stream().collect(Collectors.toList())));
    }
}
