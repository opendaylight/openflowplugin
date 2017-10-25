/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.serialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmArpOpSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmArpShaSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmArpSpaSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmArpThaSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmArpTpaSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmEthDstSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmEthSrcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmEthTypeSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIcmpv4CodeSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIcmpv4TypeSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIcmpv6CodeSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIcmpv6TypeSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmInPhyPortSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmInPortSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpDscpSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpEcnSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpProtoSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv4DstSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv4SrcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6DstSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6ExtHdrSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6FlabelSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6NdSllSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6NdTargetSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6NdTllSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmIpv6SrcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmMetadataSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmMplsBosSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmMplsLabelSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmMplsTcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmPbbIsidSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmSctpDstSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmSctpSrcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmTcpDstSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmTcpSrcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmTunnelIdSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmUdpDstSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmUdpSrcSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmVlanPcpSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.OxmVlanVidSerializer;
import org.opendaylight.openflowjava.protocol.impl.serialization.match.ext.OnfOxmTcpFlagsSerializer;
import org.opendaylight.openflowjava.protocol.impl.util.MatchEntrySerializerRegistryHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.TcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;

/**
 * Initializes serializer registry with match entry serializers.
 * @author michal.polkorab
 */
public final class MatchEntriesInitializer {

    private MatchEntriesInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers match entry serializers into provided registry.
     * @param serializerRegistry registry to be initialized with match entry serializers
     */
    public static void registerMatchEntrySerializers(SerializerRegistry serializerRegistry) {
        // register OF v1.3 OpenflowBasicClass match entry serializers
        Class<OpenflowBasicClass> oxmClass = OpenflowBasicClass.class;
        MatchEntrySerializerRegistryHelper<OpenflowBasicClass> helper =
                new MatchEntrySerializerRegistryHelper<>(EncodeConstants.OF13_VERSION_ID,
                        oxmClass, serializerRegistry);
        helper.registerSerializer(InPort.class, new OxmInPortSerializer());
        helper.registerSerializer(InPhyPort.class, new OxmInPhyPortSerializer());
        helper.registerSerializer(Metadata.class, new OxmMetadataSerializer());
        helper.registerSerializer(EthDst.class, new OxmEthDstSerializer());
        helper.registerSerializer(EthSrc.class, new OxmEthSrcSerializer());
        helper.registerSerializer(EthType.class, new OxmEthTypeSerializer());
        helper.registerSerializer(VlanVid.class, new OxmVlanVidSerializer());
        helper.registerSerializer(VlanPcp.class, new OxmVlanPcpSerializer());
        helper.registerSerializer(IpDscp.class, new OxmIpDscpSerializer());
        helper.registerSerializer(IpEcn.class, new OxmIpEcnSerializer());
        helper.registerSerializer(IpProto.class, new OxmIpProtoSerializer());
        helper.registerSerializer(Ipv4Src.class, new OxmIpv4SrcSerializer());
        helper.registerSerializer(Ipv4Dst.class, new OxmIpv4DstSerializer());
        helper.registerSerializer(TcpSrc.class, new OxmTcpSrcSerializer());
        helper.registerSerializer(TcpDst.class, new OxmTcpDstSerializer());
        helper.registerSerializer(UdpSrc.class, new OxmUdpSrcSerializer());
        helper.registerSerializer(UdpDst.class, new OxmUdpDstSerializer());
        helper.registerSerializer(SctpSrc.class, new OxmSctpSrcSerializer());
        helper.registerSerializer(SctpDst.class, new OxmSctpDstSerializer());
        helper.registerSerializer(Icmpv4Type.class, new OxmIcmpv4TypeSerializer());
        helper.registerSerializer(Icmpv4Code.class, new OxmIcmpv4CodeSerializer());
        helper.registerSerializer(ArpOp.class, new OxmArpOpSerializer());
        helper.registerSerializer(ArpSpa.class, new OxmArpSpaSerializer());
        helper.registerSerializer(ArpTpa.class, new OxmArpTpaSerializer());
        helper.registerSerializer(ArpSha.class, new OxmArpShaSerializer());
        helper.registerSerializer(ArpTha.class, new OxmArpThaSerializer());
        helper.registerSerializer(Ipv6Src.class, new OxmIpv6SrcSerializer());
        helper.registerSerializer(Ipv6Dst.class, new OxmIpv6DstSerializer());
        helper.registerSerializer(Ipv6Flabel.class, new OxmIpv6FlabelSerializer());
        helper.registerSerializer(Icmpv6Type.class, new OxmIcmpv6TypeSerializer());
        helper.registerSerializer(Icmpv6Code.class, new OxmIcmpv6CodeSerializer());
        helper.registerSerializer(Ipv6NdTarget.class, new OxmIpv6NdTargetSerializer());
        helper.registerSerializer(Ipv6NdSll.class, new OxmIpv6NdSllSerializer());
        helper.registerSerializer(Ipv6NdTll.class, new OxmIpv6NdTllSerializer());
        helper.registerSerializer(MplsLabel.class, new OxmMplsLabelSerializer());
        helper.registerSerializer(MplsTc.class, new OxmMplsTcSerializer());
        helper.registerSerializer(MplsBos.class, new OxmMplsBosSerializer());
        helper.registerSerializer(PbbIsid.class, new OxmPbbIsidSerializer());
        helper.registerSerializer(TunnelId.class, new OxmTunnelIdSerializer());
        helper.registerSerializer(Ipv6Exthdr.class, new OxmIpv6ExtHdrSerializer());

        // Register approved openflow match entry serializers
        helper.registerExperimenterSerializer(TcpFlags.class, EncodeConstants.ONF_EXPERIMENTER_ID,
                new OnfOxmTcpFlagsSerializer());
    }
}
