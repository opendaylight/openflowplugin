/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializer;
import org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerRegistry;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.ArpOpEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.ArpSourceHardwareAddressEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.ArpSourceTransportAddressEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.ArpTargetHardwareAddressEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.ArpTargetTransportAddressEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.EthernetDestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.EthernetSourceEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.EthernetTypeEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Icmpv4CodeEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Icmpv4TypeEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Icmpv6CodeEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Icmpv6TypeEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.InPhyPortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.InPortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.IpDscpEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.IpEcnEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.IpProtoEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv4ArbitraryBitMaskDestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv4ArbitraryBitMaskSourceEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv4DestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv4SourceEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6ArbitraryBitMaskDestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6ArbitraryBitMaskSourceEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6DestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6ExtHeaderEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6LabelEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6NdSllEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6NdTargetEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6NdTllEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv6SourceEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MatchEntrySerializerKeyImpl;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MatchSerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MetadataEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MplsBosEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MplsLabelEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.MplsTcEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.PbbEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.SctpDestinationPortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.SctpSourcePortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.TcpDestinationPortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.TcpFlagsEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.TcpSourcePortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.TunnelIdEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.TunnelIpv4DestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.TunnelIpv4SourceEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.UdpDestinationPortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.UdpSourcePortEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.VlanPcpEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.VlanVidEntrySerializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 * Util class for injecting new match serializers into OpenflowJava
 */
public class MatchSerializerInjector {

    /**
     * Injects match serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    public static void injectSerializers(final SerializerExtensionProvider provider) {
        final MatchSerializer serializer = new MatchSerializer();
        provider.registerSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class),
                serializer);

        // Inject all match entry serializers to match serializers using injector created by createInjector method
        final Function<Integer, Consumer<MatchEntrySerializer>> injector =
                createInjector(serializer, EncodeConstants.OF13_VERSION_ID);

        // If we have 2 serializers with same key code, use this unique offset
        final int uniqOffset = 40;

        injector.apply(OxmMatchConstants.IN_PORT).accept(new InPortEntrySerializer());
        injector.apply(OxmMatchConstants.IN_PHY_PORT).accept(new InPhyPortEntrySerializer());
        injector.apply(OxmMatchConstants.METADATA).accept(new MetadataEntrySerializer());
        injector.apply(OxmMatchConstants.ETH_DST).accept(new EthernetDestinationEntrySerializer());
        injector.apply(OxmMatchConstants.ETH_SRC).accept(new EthernetSourceEntrySerializer());
        injector.apply(OxmMatchConstants.ETH_TYPE).accept(new EthernetTypeEntrySerializer());
        injector.apply(OxmMatchConstants.VLAN_VID).accept(new VlanVidEntrySerializer());
        injector.apply(OxmMatchConstants.VLAN_PCP).accept(new VlanPcpEntrySerializer());
        injector.apply(OxmMatchConstants.IP_DSCP).accept(new IpDscpEntrySerializer());
        injector.apply(OxmMatchConstants.IP_ECN).accept(new IpEcnEntrySerializer());
        injector.apply(OxmMatchConstants.IP_PROTO).accept(new IpProtoEntrySerializer());
        injector.apply(OxmMatchConstants.TCP_SRC).accept(new TcpSourcePortEntrySerializer());
        injector.apply(OxmMatchConstants.TCP_DST).accept(new TcpDestinationPortEntrySerializer());
        injector.apply(OxmMatchConstants.UDP_SRC).accept(new UdpSourcePortEntrySerializer());
        injector.apply(OxmMatchConstants.UDP_DST).accept(new UdpDestinationPortEntrySerializer());
        injector.apply(OxmMatchConstants.SCTP_SRC).accept(new SctpSourcePortEntrySerializer());
        injector.apply(OxmMatchConstants.SCTP_DST).accept(new SctpDestinationPortEntrySerializer());
        injector.apply(OxmMatchConstants.ICMPV4_TYPE).accept(new Icmpv4TypeEntrySerializer());
        injector.apply(OxmMatchConstants.ICMPV4_CODE).accept(new Icmpv4CodeEntrySerializer());
        injector.apply(OxmMatchConstants.ICMPV6_TYPE).accept(new Icmpv6TypeEntrySerializer());
        injector.apply(OxmMatchConstants.ICMPV6_CODE).accept(new Icmpv6CodeEntrySerializer());
        injector.apply(OxmMatchConstants.IPV4_SRC).accept(new Ipv4ArbitraryBitMaskSourceEntrySerializer());
        injector.apply(OxmMatchConstants.IPV4_DST).accept(new Ipv4ArbitraryBitMaskDestinationEntrySerializer());
        injector.apply(OxmMatchConstants.IPV4_SRC + uniqOffset).accept(new Ipv4SourceEntrySerializer());
        injector.apply(OxmMatchConstants.IPV4_DST + uniqOffset).accept(new Ipv4DestinationEntrySerializer());
        injector.apply(OxmMatchConstants.NXM_NX_TUN_IPV4_SRC + uniqOffset).accept(new TunnelIpv4SourceEntrySerializer());
        injector.apply(OxmMatchConstants.NXM_NX_TUN_IPV4_DST + uniqOffset).accept(new TunnelIpv4DestinationEntrySerializer());
        injector.apply(OxmMatchConstants.ARP_OP).accept(new ArpOpEntrySerializer());
        injector.apply(OxmMatchConstants.ARP_SPA).accept(new ArpSourceTransportAddressEntrySerializer());
        injector.apply(OxmMatchConstants.ARP_TPA).accept(new ArpTargetTransportAddressEntrySerializer());
        injector.apply(OxmMatchConstants.ARP_SHA).accept(new ArpSourceHardwareAddressEntrySerializer());
        injector.apply(OxmMatchConstants.ARP_THA).accept(new ArpTargetHardwareAddressEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_SRC + uniqOffset).accept(new Ipv6ArbitraryBitMaskSourceEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_DST + uniqOffset).accept(new Ipv6ArbitraryBitMaskDestinationEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_SRC).accept(new Ipv6SourceEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_DST).accept(new Ipv6DestinationEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_FLABEL).accept(new Ipv6LabelEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_ND_TARGET).accept(new Ipv6NdTargetEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_ND_SLL).accept(new Ipv6NdSllEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_ND_TLL).accept(new Ipv6NdTllEntrySerializer());
        injector.apply(OxmMatchConstants.IPV6_EXTHDR).accept(new Ipv6ExtHeaderEntrySerializer());
        injector.apply(OxmMatchConstants.MPLS_LABEL).accept(new MplsLabelEntrySerializer());
        injector.apply(OxmMatchConstants.MPLS_BOS).accept(new MplsBosEntrySerializer());
        injector.apply(OxmMatchConstants.MPLS_TC).accept(new MplsTcEntrySerializer());
        injector.apply(OxmMatchConstants.PBB_ISID).accept(new PbbEntrySerializer());
        injector.apply(OxmMatchConstants.TUNNEL_ID).accept(new TunnelIdEntrySerializer());
        injector.apply(EncodeConstants.ONFOXM_ET_TCP_FLAGS).accept(new TcpFlagsEntrySerializer());
    }

    /**
     * Create injector that will inject new match entry serializers into #{@link org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerRegistry}
     * @param registry Match entry serializer registry
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Consumer<MatchEntrySerializer>> createInjector(
            final MatchEntrySerializerRegistry registry,
            final byte version) {
        return code -> serializer ->
                registry.registerEntrySerializer(
                        new MatchEntrySerializerKeyImpl(version, code),
                        serializer);
    }
}
