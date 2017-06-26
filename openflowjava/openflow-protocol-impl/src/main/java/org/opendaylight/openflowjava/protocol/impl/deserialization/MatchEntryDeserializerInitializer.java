/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization;

import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.api.util.OxmMatchConstants;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmArpOpDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmArpShaDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmArpSpaDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmArpThaDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmArpTpaDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmEthDstDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmEthSrcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmEthTypeDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIcmpv4CodeDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIcmpv4TypeDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIcmpv6CodeDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIcmpv6TypeDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmInPhyPortDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmInPortDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpDscpDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpEcnDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpProtoDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv4DstDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv4SrcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6DstDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6ExtHdrDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6FlabelDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6NdSllDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6NdTargetDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6NdTllDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmIpv6SrcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmMetadataDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmMplsBosDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmMplsLabelDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmMplsTcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmPbbIsidDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmSctpDstDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmSctpSrcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmTcpDstDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmTcpSrcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmTunnelIdDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmUdpDstDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmUdpSrcDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmVlanPcpDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.OxmVlanVidDeserializer;
import org.opendaylight.openflowjava.protocol.impl.deserialization.match.ext.OnfOxmTcpFlagsDeserializer;
import org.opendaylight.openflowjava.protocol.impl.util.MatchEntryDeserializerRegistryHelper;

/**
 * Util class for init registration of match entry deserializers.
 * @author michal.polkorab
 */
public final class MatchEntryDeserializerInitializer {

    private MatchEntryDeserializerInitializer() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }

    /**
     * Registers match entry deserializers.
     * @param registry registry to be filled with deserializers
     */
    public static void registerMatchEntryDeserializers(DeserializerRegistry registry) {
        // register OpenflowBasicClass match entry deserializers
        MatchEntryDeserializerRegistryHelper helper =
                new MatchEntryDeserializerRegistryHelper(EncodeConstants.OF13_VERSION_ID,
                        OxmMatchConstants.OPENFLOW_BASIC_CLASS, registry);
        helper.register(OxmMatchConstants.IN_PORT, new OxmInPortDeserializer());
        helper.register(OxmMatchConstants.IN_PHY_PORT, new OxmInPhyPortDeserializer());
        helper.register(OxmMatchConstants.METADATA, new OxmMetadataDeserializer());
        helper.register(OxmMatchConstants.ETH_DST, new OxmEthDstDeserializer());
        helper.register(OxmMatchConstants.ETH_SRC, new OxmEthSrcDeserializer());
        helper.register(OxmMatchConstants.ETH_TYPE, new OxmEthTypeDeserializer());
        helper.register(OxmMatchConstants.VLAN_VID, new OxmVlanVidDeserializer());
        helper.register(OxmMatchConstants.VLAN_PCP, new OxmVlanPcpDeserializer());
        helper.register(OxmMatchConstants.IP_DSCP, new OxmIpDscpDeserializer());
        helper.register(OxmMatchConstants.IP_ECN, new OxmIpEcnDeserializer());
        helper.register(OxmMatchConstants.IP_PROTO, new OxmIpProtoDeserializer());
        helper.register(OxmMatchConstants.IPV4_SRC, new OxmIpv4SrcDeserializer());
        helper.register(OxmMatchConstants.IPV4_DST, new OxmIpv4DstDeserializer());
        helper.register(OxmMatchConstants.TCP_SRC, new OxmTcpSrcDeserializer());
        helper.register(OxmMatchConstants.TCP_DST, new OxmTcpDstDeserializer());
        helper.register(OxmMatchConstants.UDP_SRC, new OxmUdpSrcDeserializer());
        helper.register(OxmMatchConstants.UDP_DST, new OxmUdpDstDeserializer());
        helper.register(OxmMatchConstants.SCTP_SRC, new OxmSctpSrcDeserializer());
        helper.register(OxmMatchConstants.SCTP_DST, new OxmSctpDstDeserializer());
        helper.register(OxmMatchConstants.ICMPV4_TYPE, new OxmIcmpv4TypeDeserializer());
        helper.register(OxmMatchConstants.ICMPV4_CODE, new OxmIcmpv4CodeDeserializer());
        helper.register(OxmMatchConstants.ARP_OP, new OxmArpOpDeserializer());
        helper.register(OxmMatchConstants.ARP_SPA, new OxmArpSpaDeserializer());
        helper.register(OxmMatchConstants.ARP_TPA, new OxmArpTpaDeserializer());
        helper.register(OxmMatchConstants.ARP_SHA, new OxmArpShaDeserializer());
        helper.register(OxmMatchConstants.ARP_THA, new OxmArpThaDeserializer());
        helper.register(OxmMatchConstants.IPV6_SRC, new OxmIpv6SrcDeserializer());
        helper.register(OxmMatchConstants.IPV6_DST, new OxmIpv6DstDeserializer());
        helper.register(OxmMatchConstants.IPV6_FLABEL, new OxmIpv6FlabelDeserializer());
        helper.register(OxmMatchConstants.ICMPV6_TYPE, new OxmIcmpv6TypeDeserializer());
        helper.register(OxmMatchConstants.ICMPV6_CODE, new OxmIcmpv6CodeDeserializer());
        helper.register(OxmMatchConstants.IPV6_ND_TARGET, new OxmIpv6NdTargetDeserializer());
        helper.register(OxmMatchConstants.IPV6_ND_SLL, new OxmIpv6NdSllDeserializer());
        helper.register(OxmMatchConstants.IPV6_ND_TLL, new OxmIpv6NdTllDeserializer());
        helper.register(OxmMatchConstants.MPLS_LABEL, new OxmMplsLabelDeserializer());
        helper.register(OxmMatchConstants.MPLS_TC, new OxmMplsTcDeserializer());
        helper.register(OxmMatchConstants.MPLS_BOS, new OxmMplsBosDeserializer());
        helper.register(OxmMatchConstants.PBB_ISID, new OxmPbbIsidDeserializer());
        helper.register(OxmMatchConstants.TUNNEL_ID, new OxmTunnelIdDeserializer());
        helper.register(OxmMatchConstants.IPV6_EXTHDR, new OxmIpv6ExtHdrDeserializer());

        // Register approved openflow match entry deserializers
        helper.registerExperimenter(EncodeConstants.ONFOXM_ET_TCP_FLAGS, EncodeConstants.ONF_EXPERIMENTER_ID,
                new OnfOxmTcpFlagsDeserializer());
    }
}
