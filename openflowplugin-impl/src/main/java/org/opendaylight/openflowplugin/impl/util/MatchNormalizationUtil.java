/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeIpv4Arbitrary;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeIpv4Prefix;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeIpv6AddressWithoutMask;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeIpv6Arbitrary;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeIpv6Prefix;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeMacAddress;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeMacAddressMask;
import static org.opendaylight.openflowplugin.impl.util.AddressNormalizationUtil.normalizeProtocolAgnosticPort;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;

/**
 * Utility class for match normalization
 */
public final class MatchNormalizationUtil {
    // Cache normalizers for common OpenFlow versions
    private static final Map<Short, Set<Function<MatchBuilder, MatchBuilder>>> NORMALIZERS = ImmutableMap
            .<Short, Set<Function<MatchBuilder, MatchBuilder>>>builder()
            .put(OFConstants.OFP_VERSION_1_0, createNormalizers(OFConstants.OFP_VERSION_1_0).collect(Collectors.toSet()))
            .put(OFConstants.OFP_VERSION_1_3, createNormalizers(OFConstants.OFP_VERSION_1_3).collect(Collectors.toSet()))
            .build();

    private MatchNormalizationUtil() {
        throw new RuntimeException("Creating instance of util classes is prohibited");
    }

    /**
     * Normalize match.
     *
     * @param match   the OpenFlow match
     * @param version the OpenFlow version
     * @return normalized OpenFlow match
     */
    @Nonnull
    public static Match normalizeMatch(@Nonnull final Match match, final short version) {
        final MatchBuilder matchBuilder = new MatchBuilder(match);

        Optional.ofNullable(NORMALIZERS.get(version))
                .orElseGet(() -> createNormalizers(version).collect(Collectors.toSet()))
                .forEach(normalizer -> normalizer.apply(matchBuilder));

        return matchBuilder.build();
    }

    @Nonnull
    private static Stream<Function<MatchBuilder, MatchBuilder>> createNormalizers(final short version) {
        return Stream.of(
                MatchNormalizationUtil::normalizeExtensionMatch,
                MatchNormalizationUtil::normalizeEthernetMatch,
                MatchNormalizationUtil::normalizeArpMatch,
                MatchNormalizationUtil::normalizeTunnelIpv4Match,
                MatchNormalizationUtil::normalizeIpv4Match,
                MatchNormalizationUtil::normalizeIpv4MatchArbitraryBitMask,
                MatchNormalizationUtil::normalizeIpv6Match,
                MatchNormalizationUtil::normalizeIpv6MatchArbitraryBitMask,
                (match) -> normalizeInPortMatch(match, version),
                (match) -> normalizeInPhyPortMatch(match, version));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static MatchBuilder normalizeExtensionMatch(@Nonnull final MatchBuilder match) {
        return new MatchBuilder(MatchUtil.transformMatch(match.build(), Match.class));
    }

    @Nonnull
    private static MatchBuilder normalizeInPortMatch(@Nonnull final MatchBuilder match, final short version) {
        return Optional
                .ofNullable(match.getInPort())
                .flatMap(inPort -> Optional.ofNullable(normalizeProtocolAgnosticPort(inPort, version)))
                .map(inPortUri -> match.setInPort(new NodeConnectorId(inPortUri)))
                .orElse(match);
    }

    @Nonnull
    private static MatchBuilder normalizeInPhyPortMatch(@Nonnull final MatchBuilder match, final short version) {
        return Optional
                .ofNullable(match.getInPhyPort())
                .flatMap(inPhyPort -> Optional.ofNullable(normalizeProtocolAgnosticPort(inPhyPort, version)))
                .map(inPhyPortUri -> match.setInPhyPort(new NodeConnectorId(inPhyPortUri)))
                .orElse(match);
    }

    @Nonnull
    private static MatchBuilder normalizeArpMatch(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getLayer3Match())
                .filter(ArpMatch.class::isInstance)
                .map(ArpMatch.class::cast)
                .map(arp -> match.setLayer3Match(new ArpMatchBuilder(arp)
                        .setArpSourceHardwareAddress(Optional
                                .ofNullable(arp.getArpSourceHardwareAddress())
                                .map(arpSource -> new ArpSourceHardwareAddressBuilder(arpSource)
                                        .setAddress(normalizeMacAddress(arpSource.getAddress()))
                                        .setMask(normalizeMacAddress(arpSource.getMask()))
                                        .build())
                                .orElse(arp.getArpSourceHardwareAddress()))
                        .setArpTargetHardwareAddress(Optional
                                .ofNullable(arp.getArpTargetHardwareAddress())
                                .map(arpTarget -> new ArpTargetHardwareAddressBuilder(arpTarget)
                                        .setAddress(normalizeMacAddress(arpTarget.getAddress()))
                                        .setMask(normalizeMacAddress(arpTarget.getMask()))
                                        .build())
                                .orElse(arp.getArpTargetHardwareAddress()))
                        .setArpSourceTransportAddress(normalizeIpv4Prefix(arp.getArpSourceTransportAddress()))
                        .setArpTargetTransportAddress(normalizeIpv4Prefix(arp.getArpTargetTransportAddress()))
                        .build())
                )
                .orElse(match);
    }


    @Nonnull
    private static MatchBuilder normalizeTunnelIpv4Match(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getLayer3Match())
                .filter(TunnelIpv4Match.class::isInstance)
                .map(TunnelIpv4Match.class::cast)
                .map(tunnelIpv4 -> match.setLayer3Match(new TunnelIpv4MatchBuilder(tunnelIpv4)
                        .setTunnelIpv4Source(normalizeIpv4Prefix(tunnelIpv4.getTunnelIpv4Source()))
                        .setTunnelIpv4Destination(normalizeIpv4Prefix(tunnelIpv4.getTunnelIpv4Destination()))
                        .build()))
                .orElse(match);
    }

    @Nonnull
    private static MatchBuilder normalizeIpv4Match(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getLayer3Match())
                .filter(Ipv4Match.class::isInstance)
                .map(Ipv4Match.class::cast)
                .map(ipv4 -> match.setLayer3Match(new Ipv4MatchBuilder(ipv4)
                        .setIpv4Source(normalizeIpv4Prefix(ipv4.getIpv4Source()))
                        .setIpv4Destination(normalizeIpv4Prefix(ipv4.getIpv4Destination()))
                        .build()))
                .orElse(match);
    }

    @Nonnull
    private static MatchBuilder normalizeIpv4MatchArbitraryBitMask(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getLayer3Match())
                .filter(Ipv4MatchArbitraryBitMask.class::isInstance)
                .map(Ipv4MatchArbitraryBitMask.class::cast)
                .map(ipv4arbitrary -> match.setLayer3Match(new Ipv4MatchBuilder()
                        .setIpv4Source(normalizeIpv4Arbitrary(
                                ipv4arbitrary.getIpv4SourceAddressNoMask(),
                                ipv4arbitrary.getIpv4SourceArbitraryBitmask()))
                        .setIpv4Destination(normalizeIpv4Arbitrary(
                                ipv4arbitrary.getIpv4DestinationAddressNoMask(),
                                ipv4arbitrary.getIpv4DestinationArbitraryBitmask()))
                        .build()))
                .orElse(match);
    }


    @Nonnull
    private static MatchBuilder normalizeIpv6Match(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getLayer3Match())
                .filter(Ipv6Match.class::isInstance)
                .map(Ipv6Match.class::cast)
                .map(ipv6 -> match.setLayer3Match(new Ipv6MatchBuilder(ipv6)
                        .setIpv6NdSll(normalizeMacAddress(ipv6.getIpv6NdSll()))
                        .setIpv6NdTll(normalizeMacAddress(ipv6.getIpv6NdTll()))
                        .setIpv6NdTarget(normalizeIpv6AddressWithoutMask(ipv6.getIpv6NdTarget()))
                        .setIpv6Source(normalizeIpv6Prefix(ipv6.getIpv6Source()))
                        .setIpv6Destination(normalizeIpv6Prefix(ipv6.getIpv6Destination()))
                        .build()))
                .orElse(match);
    }


    @Nonnull
    private static MatchBuilder normalizeIpv6MatchArbitraryBitMask(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getLayer3Match())
                .filter(Ipv6MatchArbitraryBitMask.class::isInstance)
                .map(Ipv6MatchArbitraryBitMask.class::cast)
                .map(ipv6Arbitrary -> match.setLayer3Match(new Ipv6MatchBuilder()
                        .setIpv6Source(normalizeIpv6Arbitrary(
                                ipv6Arbitrary.getIpv6SourceAddressNoMask(),
                                ipv6Arbitrary.getIpv6SourceArbitraryBitmask()))
                        .setIpv6Destination(normalizeIpv6Arbitrary(
                                ipv6Arbitrary.getIpv6DestinationAddressNoMask(),
                                ipv6Arbitrary.getIpv6DestinationArbitraryBitmask()))
                        .build()))
                .orElse(match);
    }

    @Nonnull
    private static MatchBuilder normalizeEthernetMatch(@Nonnull final MatchBuilder match) {
        return Optional
                .ofNullable(match.getEthernetMatch())
                .map(eth -> match.setEthernetMatch(new EthernetMatchBuilder(eth)
                        .setEthernetSource(Optional
                                .ofNullable(eth.getEthernetSource())
                                .map(filter -> new EthernetSourceBuilder(filter)
                                        .setAddress(normalizeMacAddress(filter.getAddress()))
                                        .setMask(normalizeMacAddressMask(filter.getMask()))
                                        .build())
                                .orElse(eth.getEthernetSource()))
                        .setEthernetDestination(Optional
                                .ofNullable(eth.getEthernetDestination())
                                .map(filter -> new EthernetDestinationBuilder(filter)
                                        .setAddress(normalizeMacAddress(filter.getAddress()))
                                        .setMask(normalizeMacAddressMask(filter.getMask()))
                                        .build())
                                .orElse(eth.getEthernetDestination()))
                        .build()))
                .orElse(match);
    }

}