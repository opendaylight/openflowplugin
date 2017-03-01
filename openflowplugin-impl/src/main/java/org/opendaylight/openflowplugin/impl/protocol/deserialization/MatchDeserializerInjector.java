/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.deserialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializer;
import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializerRegistry;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.key.MessageCodeMatchKey;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.ArpOpEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.ArpSourceHardwareAddressEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.ArpSourceTransportAddressEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.ArpTargetHardwareAddressEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.ArpTargetTransportAddressEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.EthernetDestinationEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.EthernetSourceEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.EthernetTypeEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Icmpv4CodeEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Icmpv4TypeEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Icmpv6CodeEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Icmpv6TypeEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.InPhyPortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.InPortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.IpDscpEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.IpEcnEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.IpProtoEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv4DestinationEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv4SourceEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6DestinationEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6ExtHeaderEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6FlabelEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6NdSllEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6NdTargetEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6NdTllEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.Ipv6SourceEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.MatchDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.MetadataEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.MplsBosEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.MplsLabelEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.MplsTcEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.PbbEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.SctpDestinationPortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.SctpSourcePortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.TcpDestinationPortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.TcpFlagsEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.TcpSourcePortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.TunnelIdEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.UdpDestinationPortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.UdpSourcePortEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.VlanPcpEntryDeserializer;
import org.opendaylight.openflowplugin.impl.protocol.deserialization.match.VlanVidEntryDeserializer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

/**
 * Util class for injecting new match entry deserializers into OpenflowJava
 */
public class MatchDeserializerInjector {

    /**
     * Injects deserializers into provided {@link org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerExtensionProvider}
     * @param provider OpenflowJava deserializer extension provider
     */
    static void injectDeserializers(final DeserializerExtensionProvider provider) {
        for (MatchPath path : MatchPath.values()) {
            final MatchDeserializer deserializer = new MatchDeserializer(path);
            provider.registerDeserializer(
                    new MessageCodeMatchKey(
                        EncodeConstants.OF13_VERSION_ID,
                        EncodeConstants.EMPTY_LENGTH,
                        Match.class,
                        path),
                    deserializer);

            // Inject new match entry serializers here using injector created by createInjector method
            final Function<Integer, Function<Long, Function<Integer, Consumer<MatchEntryDeserializer>>>> injector =
                createInjector(deserializer, EncodeConstants.OF13_VERSION_ID);


            // Wrapped injector that uses OPENFLOW_BASIC_CLASS
            final Function<Integer, Consumer<MatchEntryDeserializer>> basicInjector =
                injector.apply(OxmMatchConstants.OPENFLOW_BASIC_CLASS).apply(null);

            // Wrapped injector that uses EXPERIMENTER_CLASS
            final Function<Long, Function<Integer, Consumer<MatchEntryDeserializer>>> experInjector =
                injector.apply(OxmMatchConstants.EXPERIMENTER_CLASS);

            basicInjector.apply(OxmMatchConstants.ARP_OP).accept(new ArpOpEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ARP_SHA).accept(new ArpSourceHardwareAddressEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ARP_THA).accept(new ArpTargetHardwareAddressEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ARP_SPA).accept(new ArpSourceTransportAddressEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ARP_TPA).accept(new ArpTargetTransportAddressEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IN_PORT).accept(new InPortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IN_PHY_PORT).accept(new InPhyPortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.METADATA).accept(new MetadataEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ETH_DST).accept(new EthernetDestinationEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ETH_SRC).accept(new EthernetSourceEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ETH_TYPE).accept(new EthernetTypeEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.VLAN_PCP).accept(new VlanPcpEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.VLAN_VID).accept(new VlanVidEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IP_DSCP).accept(new IpDscpEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IP_ECN).accept(new IpEcnEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IP_PROTO).accept(new IpProtoEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.TCP_SRC).accept(new TcpSourcePortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.TCP_DST).accept(new TcpDestinationPortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.UDP_SRC).accept(new UdpSourcePortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.UDP_DST).accept(new UdpDestinationPortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.SCTP_SRC).accept(new SctpSourcePortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.SCTP_DST).accept(new SctpDestinationPortEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ICMPV4_CODE).accept(new Icmpv4CodeEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ICMPV4_TYPE).accept(new Icmpv4TypeEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ICMPV6_CODE).accept(new Icmpv6CodeEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.ICMPV6_TYPE).accept(new Icmpv6TypeEntryDeserializer());
            // TODO: How to differentiate between Ipv4 and Tunnel when both are serialized to same format?
            basicInjector.apply(OxmMatchConstants.IPV4_SRC).accept(new Ipv4SourceEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV4_DST).accept(new Ipv4DestinationEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_SRC).accept(new Ipv6SourceEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_DST).accept(new Ipv6DestinationEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_EXTHDR).accept(new Ipv6ExtHeaderEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_FLABEL).accept(new Ipv6FlabelEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_ND_SLL).accept(new Ipv6NdSllEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_ND_TLL).accept(new Ipv6NdTllEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.IPV6_ND_TARGET).accept(new Ipv6NdTargetEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.MPLS_LABEL).accept(new MplsLabelEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.MPLS_BOS).accept(new MplsBosEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.MPLS_TC).accept(new MplsTcEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.PBB_ISID).accept(new PbbEntryDeserializer());
            basicInjector.apply(OxmMatchConstants.TUNNEL_ID).accept(new TunnelIdEntryDeserializer());
            experInjector.apply(EncodeConstants.ONF_EXPERIMENTER_ID).apply(EncodeConstants.ONFOXM_ET_TCP_FLAGS).accept(new TcpFlagsEntryDeserializer());
        }
    }

    /**
     * Create injector that will inject new serializers into {@link org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MatchEntryDeserializerRegistry}
     * @param registry Match entry deserializer registry
     * @param version Openflow version
     * @return injector
     */
    @VisibleForTesting
    static Function<Integer, Function<Long, Function<Integer, Consumer<MatchEntryDeserializer>>>> createInjector(
            final MatchEntryDeserializerRegistry registry,
            final short version
            ) {
        return oxmClass -> expId -> oxmField -> deserializer -> {
            final MatchEntryDeserializerKey key = new MatchEntryDeserializerKey(version, oxmClass, oxmField);
            key.setExperimenterId(expId);
            registry.registerEntryDeserializer(key, deserializer);
        };
    }

}
