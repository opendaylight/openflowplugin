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
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv4DestinationEntrySerializer;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.Ipv4SourceEntrySerializer;
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
class MatchSerializerInjector {

    /**
     * Injects match serializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider}
     * @param provider OpenflowJava serializer extension provider
     */
    static void injectSerializers(final SerializerExtensionProvider provider) {
        final MatchSerializer serializer = new MatchSerializer();
        provider.registerSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class),
                serializer);

        // Inject all match entry serializers to match serializers using injector created by createInjector method
        final Function<Integer, Function<Integer, Consumer<MatchEntrySerializer>>> injector =
                createInjector(serializer, EncodeConstants.OF13_VERSION_ID);

        final Function<Integer, Consumer<MatchEntrySerializer>> basicInjector =
                injector.apply(OxmMatchConstants.OPENFLOW_BASIC_CLASS);

        final Function<Integer, Consumer<MatchEntrySerializer>> experInjector =
                injector.apply(OxmMatchConstants.EXPERIMENTER_CLASS);

        basicInjector.apply(OxmMatchConstants.IN_PORT).accept(new InPortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IN_PHY_PORT).accept(new InPhyPortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.METADATA).accept(new MetadataEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ETH_DST).accept(new EthernetDestinationEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ETH_SRC).accept(new EthernetSourceEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ETH_TYPE).accept(new EthernetTypeEntrySerializer());
        basicInjector.apply(OxmMatchConstants.VLAN_VID).accept(new VlanVidEntrySerializer());
        basicInjector.apply(OxmMatchConstants.VLAN_PCP).accept(new VlanPcpEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IP_DSCP).accept(new IpDscpEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IP_ECN).accept(new IpEcnEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IP_PROTO).accept(new IpProtoEntrySerializer());
        basicInjector.apply(OxmMatchConstants.TCP_SRC).accept(new TcpSourcePortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.TCP_DST).accept(new TcpDestinationPortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.UDP_SRC).accept(new UdpSourcePortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.UDP_DST).accept(new UdpDestinationPortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.SCTP_SRC).accept(new SctpSourcePortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.SCTP_DST).accept(new SctpDestinationPortEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ICMPV4_TYPE).accept(new Icmpv4TypeEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ICMPV4_CODE).accept(new Icmpv4CodeEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ICMPV6_TYPE).accept(new Icmpv6TypeEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ICMPV6_CODE).accept(new Icmpv6CodeEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV4_SRC).accept(new Ipv4SourceEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV4_DST).accept(new Ipv4DestinationEntrySerializer());
        experInjector.apply(OxmMatchConstants.NXM_NX_TUN_IPV4_SRC).accept(new TunnelIpv4SourceEntrySerializer());
        experInjector.apply(OxmMatchConstants.NXM_NX_TUN_IPV4_DST).accept(new TunnelIpv4DestinationEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ARP_OP).accept(new ArpOpEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ARP_SPA).accept(new ArpSourceTransportAddressEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ARP_TPA).accept(new ArpTargetTransportAddressEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ARP_SHA).accept(new ArpSourceHardwareAddressEntrySerializer());
        basicInjector.apply(OxmMatchConstants.ARP_THA).accept(new ArpTargetHardwareAddressEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_SRC).accept(new Ipv6SourceEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_DST).accept(new Ipv6DestinationEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_FLABEL).accept(new Ipv6LabelEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_ND_TARGET).accept(new Ipv6NdTargetEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_ND_SLL).accept(new Ipv6NdSllEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_ND_TLL).accept(new Ipv6NdTllEntrySerializer());
        basicInjector.apply(OxmMatchConstants.IPV6_EXTHDR).accept(new Ipv6ExtHeaderEntrySerializer());
        basicInjector.apply(OxmMatchConstants.MPLS_LABEL).accept(new MplsLabelEntrySerializer());
        basicInjector.apply(OxmMatchConstants.MPLS_BOS).accept(new MplsBosEntrySerializer());
        basicInjector.apply(OxmMatchConstants.MPLS_TC).accept(new MplsTcEntrySerializer());
        basicInjector.apply(OxmMatchConstants.PBB_ISID).accept(new PbbEntrySerializer());
        basicInjector.apply(OxmMatchConstants.TUNNEL_ID).accept(new TunnelIdEntrySerializer());
        experInjector.apply(EncodeConstants.ONFOXM_ET_TCP_FLAGS).accept(new TcpFlagsEntrySerializer());
    }

    /**
     * Create injector that will inject new match entry serializers into #{@link org.opendaylight.openflowplugin.api.openflow.protocol.serialization.MatchEntrySerializerRegistry}
     * @param registry Match entry serializer registry
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Function<Integer, Consumer<MatchEntrySerializer>>> createInjector(
            final MatchEntrySerializerRegistry registry,
            final byte version) {
        return oxmClass -> oxmField -> serializer ->
                registry.registerEntrySerializer(
                        new MatchEntrySerializerKeyImpl(version, oxmClass, oxmField),
                        serializer);
    }
}
