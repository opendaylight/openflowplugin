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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.function.Function;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Utility class for match normalization.
 */
public final class MatchNormalizationUtil {
    // Cache normalizers for common OpenFlow versions
    private static final ImmutableMap<Uint8, ImmutableSet<Function<MatchBuilder, MatchBuilder>>> COMMON_NORMALIZERS =
        ImmutableMap.<Uint8, ImmutableSet<Function<MatchBuilder, MatchBuilder>>>builder()
            .put(OFConstants.OFP_VERSION_1_0,
                createNormalizers(OFConstants.OFP_VERSION_1_0).collect(ImmutableSet.toImmutableSet()))
            .put(OFConstants.OFP_VERSION_1_3,
                createNormalizers(OFConstants.OFP_VERSION_1_3).collect(ImmutableSet.toImmutableSet()))
            .build();
    private static final LoadingCache<Uint8, ImmutableSet<Function<MatchBuilder, MatchBuilder>>> UNCOMMON_NORMALIZERS =
        CacheBuilder.newBuilder().weakValues().build(new CacheLoader<>() {
            @Override
            public ImmutableSet<Function<MatchBuilder, MatchBuilder>> load(final Uint8 key) {
                return createNormalizers(key).collect(ImmutableSet.toImmutableSet());
            }
        });

    private MatchNormalizationUtil() {
        // Hidden on purpose
    }

    /**
     * Normalize match.
     *
     * @param match   the OpenFlow match
     * @param version the OpenFlow version
     * @return normalized OpenFlow match
     */
    public static @NonNull Match normalizeMatch(@NonNull final Match match, final Uint8 version) {
        final var matchBuilder = new MatchBuilder(match);

        var normalizers = COMMON_NORMALIZERS.get(version);
        if (normalizers == null) {
            normalizers = UNCOMMON_NORMALIZERS.getUnchecked(version);
        }
        normalizers.forEach(normalizer -> normalizer.apply(matchBuilder));

        return matchBuilder.build();
    }

    private static @NonNull Stream<Function<MatchBuilder, MatchBuilder>> createNormalizers(final Uint8 version) {
        return Stream.of(
            MatchNormalizationUtil::normalizeExtensionMatch,
            MatchNormalizationUtil::normalizeEthernetMatch,
            MatchNormalizationUtil::normalizeArpMatch,
            MatchNormalizationUtil::normalizeTunnelIpv4Match,
            MatchNormalizationUtil::normalizeIpv4Match,
            MatchNormalizationUtil::normalizeIpv4MatchArbitraryBitMask,
            MatchNormalizationUtil::normalizeIpv6Match,
            MatchNormalizationUtil::normalizeIpv6MatchArbitraryBitMask,
            match -> normalizeInPortMatch(match, version),
            match -> normalizeInPhyPortMatch(match, version));
    }

    private static @NonNull MatchBuilder normalizeExtensionMatch(@NonNull final MatchBuilder match) {
        return new MatchBuilder(MatchUtil.transformMatch(match.build(), Match.class));
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeInPortMatch(final @NonNull MatchBuilder match, final Uint8 version) {
        final var inPort = match.getInPort();
        if (inPort != null) {
            final var inPortUri = normalizeProtocolAgnosticPort(inPort, version);
            if (inPortUri != null) {
                match.setInPort(new NodeConnectorId(inPortUri));
            }
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeInPhyPortMatch(final @NonNull MatchBuilder match, final Uint8 version) {
        final var inPhyPort = match.getInPhyPort();
        if (inPhyPort != null) {
            final var inPhyPortUri = normalizeProtocolAgnosticPort(inPhyPort, version);
            if (inPhyPortUri != null) {
                match.setInPhyPort(new NodeConnectorId(inPhyPortUri));
            }
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeArpMatch(final @NonNull MatchBuilder match) {
        if (match.getLayer3Match() instanceof ArpMatch arp) {
            final var builder = new ArpMatchBuilder(arp)
                .setArpSourceTransportAddress(normalizeIpv4Prefix(arp.getArpSourceTransportAddress()))
                .setArpTargetTransportAddress(normalizeIpv4Prefix(arp.getArpTargetTransportAddress()));

            final var arpSource = arp.getArpSourceHardwareAddress();
            if (arpSource != null) {
                builder.setArpSourceHardwareAddress(new ArpSourceHardwareAddressBuilder(arpSource)
                    .setAddress(normalizeMacAddress(arpSource.getAddress()))
                    .setMask(normalizeMacAddress(arpSource.getMask()))
                    .build());
            }
            final var arpTarget = arp.getArpTargetHardwareAddress();
            if (arpTarget != null) {
                builder.setArpTargetHardwareAddress(new ArpTargetHardwareAddressBuilder(arpTarget)
                    .setAddress(normalizeMacAddress(arpTarget.getAddress()))
                    .setMask(normalizeMacAddress(arpTarget.getMask()))
                    .build());
            }
            match.setLayer3Match(builder.build());
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeTunnelIpv4Match(final @NonNull MatchBuilder match) {
        if (match.getLayer3Match() instanceof TunnelIpv4Match tunnelIpv4) {
            match.setLayer3Match(new TunnelIpv4MatchBuilder(tunnelIpv4)
                .setTunnelIpv4Source(normalizeIpv4Prefix(tunnelIpv4.getTunnelIpv4Source()))
                .setTunnelIpv4Destination(normalizeIpv4Prefix(tunnelIpv4.getTunnelIpv4Destination()))
                .build());
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeIpv4Match(final @NonNull MatchBuilder match) {
        if (match.getLayer3Match() instanceof Ipv4Match ipv4) {
            match.setLayer3Match(new Ipv4MatchBuilder(ipv4)
                .setIpv4Source(normalizeIpv4Prefix(ipv4.getIpv4Source()))
                .setIpv4Destination(normalizeIpv4Prefix(ipv4.getIpv4Destination()))
                .build());
        }
        return match;
    }

    @NonNull
    @VisibleForTesting
    static MatchBuilder normalizeIpv4MatchArbitraryBitMask(final @NonNull MatchBuilder match) {
        if (match.getLayer3Match() instanceof Ipv4MatchArbitraryBitMask ipv4arbitrary) {
            match.setLayer3Match(new Ipv4MatchBuilder()
                .setIpv4Source(normalizeIpv4Arbitrary(
                    ipv4arbitrary.getIpv4SourceAddressNoMask(),
                    ipv4arbitrary.getIpv4SourceArbitraryBitmask()))
                .setIpv4Destination(normalizeIpv4Arbitrary(
                    ipv4arbitrary.getIpv4DestinationAddressNoMask(),
                    ipv4arbitrary.getIpv4DestinationArbitraryBitmask()))
                .build());
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeIpv6Match(final @NonNull MatchBuilder match) {
        if (match.getLayer3Match() instanceof Ipv6Match ipv6) {
            match.setLayer3Match(new Ipv6MatchBuilder(ipv6)
                .setIpv6NdSll(normalizeMacAddress(ipv6.getIpv6NdSll()))
                .setIpv6NdTll(normalizeMacAddress(ipv6.getIpv6NdTll()))
                .setIpv6NdTarget(normalizeIpv6AddressWithoutMask(ipv6.getIpv6NdTarget()))
                .setIpv6Source(normalizeIpv6Prefix(ipv6.getIpv6Source()))
                .setIpv6Destination(normalizeIpv6Prefix(ipv6.getIpv6Destination()))
                .build());
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeIpv6MatchArbitraryBitMask(final @NonNull MatchBuilder match) {
        if (match.getLayer3Match() instanceof Ipv6MatchArbitraryBitMask ipv6Arbitrary) {
            match.setLayer3Match(new Ipv6MatchBuilder()
                .setIpv6Source(normalizeIpv6Arbitrary(
                    ipv6Arbitrary.getIpv6SourceAddressNoMask(),
                    ipv6Arbitrary.getIpv6SourceArbitraryBitmask()))
                .setIpv6Destination(normalizeIpv6Arbitrary(
                    ipv6Arbitrary.getIpv6DestinationAddressNoMask(),
                    ipv6Arbitrary.getIpv6DestinationArbitraryBitmask()))
                .build());
        }
        return match;
    }

    @VisibleForTesting
    static @NonNull MatchBuilder normalizeEthernetMatch(final @NonNull MatchBuilder match) {
        final var eth = match.getEthernetMatch();
        if (eth != null) {
            final var builder = new EthernetMatchBuilder(eth);
            final var source = eth.getEthernetSource();
            if (source != null) {
                builder.setEthernetSource(new EthernetSourceBuilder(source)
                    .setAddress(normalizeMacAddress(source.getAddress()))
                    .setMask(normalizeMacAddressMask(source.getMask()))
                    .build());
            }
            final var dest = eth.getEthernetDestination();
            if (dest != null) {
                builder.setEthernetDestination(new EthernetDestinationBuilder(dest)
                    .setAddress(normalizeMacAddress(dest.getAddress()))
                    .setMask(normalizeMacAddressMask(dest.getMask()))
                    .build());
            }
            match.setEthernetMatch(builder.build());
        }
        return match;
    }
}
