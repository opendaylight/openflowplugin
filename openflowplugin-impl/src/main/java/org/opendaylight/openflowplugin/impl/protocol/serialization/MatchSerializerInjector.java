/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerExtensionProvider;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.match.AbstractMatchEntrySerializer;
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
        // Add new match entry serializers to this list (order matters!)
        final List<AbstractMatchEntrySerializer> entrySerializers = new ArrayList<>();
        entrySerializers.add(new InPortEntrySerializer());
        entrySerializers.add(new InPhyPortEntrySerializer());
        entrySerializers.add(new MetadataEntrySerializer());
        entrySerializers.add(new EthernetDestinationEntrySerializer());
        entrySerializers.add(new EthernetSourceEntrySerializer());
        entrySerializers.add(new EthernetTypeEntrySerializer());
        entrySerializers.add(new VlanVidEntrySerializer());
        entrySerializers.add(new VlanPcpEntrySerializer());
        entrySerializers.add(new IpDscpEntrySerializer());
        entrySerializers.add(new IpEcnEntrySerializer());
        entrySerializers.add(new IpProtoEntrySerializer());
        entrySerializers.add(new TcpSourcePortEntrySerializer());
        entrySerializers.add(new TcpDestinationPortEntrySerializer());
        entrySerializers.add(new UdpSourcePortEntrySerializer());
        entrySerializers.add(new UdpDestinationPortEntrySerializer());
        entrySerializers.add(new SctpSourcePortEntrySerializer());
        entrySerializers.add(new SctpDestinationPortEntrySerializer());
        entrySerializers.add(new Icmpv4TypeEntrySerializer());
        entrySerializers.add(new Icmpv4CodeEntrySerializer());
        entrySerializers.add(new Icmpv6TypeEntrySerializer());
        entrySerializers.add(new Icmpv6CodeEntrySerializer());
        entrySerializers.add(new Ipv4ArbitraryBitMaskSourceEntrySerializer());
        entrySerializers.add(new Ipv4ArbitraryBitMaskDestinationEntrySerializer());
        entrySerializers.add(new Ipv4SourceEntrySerializer());
        entrySerializers.add(new Ipv4DestinationEntrySerializer());
        entrySerializers.add(new TunnelIpv4SourceEntrySerializer());
        entrySerializers.add(new TunnelIpv4DestinationEntrySerializer());
        entrySerializers.add(new ArpOpEntrySerializer());
        entrySerializers.add(new ArpSourceTransportAddressEntrySerializer());
        entrySerializers.add(new ArpTargetTransportAddressEntrySerializer());
        entrySerializers.add(new ArpSourceHardwareAddressEntrySerializer());
        entrySerializers.add(new ArpTargetHardwareAddressEntrySerializer());
        entrySerializers.add(new Ipv6ArbitraryBitMaskSourceEntrySerializer());
        entrySerializers.add(new Ipv6ArbitraryBitMaskDestinationEntrySerializer());
        entrySerializers.add(new Ipv6SourceEntrySerializer());
        entrySerializers.add(new Ipv6DestinationEntrySerializer());
        entrySerializers.add(new Ipv6LabelEntrySerializer());
        entrySerializers.add(new Ipv6NdTargetEntrySerializer());
        entrySerializers.add(new Ipv6NdSllEntrySerializer());
        entrySerializers.add(new Ipv6NdTllEntrySerializer());
        entrySerializers.add(new Ipv6ExtHeaderEntrySerializer());
        entrySerializers.add(new MplsLabelEntrySerializer());
        entrySerializers.add(new MplsBosEntrySerializer());
        entrySerializers.add(new MplsTcEntrySerializer());
        entrySerializers.add(new PbbEntrySerializer());
        entrySerializers.add(new TunnelIdEntrySerializer());
        entrySerializers.add(new TcpFlagsEntrySerializer());

        // Register all match entries to MatchSerializer and then inject it to provider
        provider.registerSerializer(
                new MessageTypeKey<>(EncodeConstants.OF13_VERSION_ID, Match.class),
                new MatchSerializer(entrySerializers));
    }
}
