/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

/**
 * Defines Nicira match codecs.
 *
 * @author msunal
 * @author Josh Hershberg (jhershbe@redhat.com)
 */
public interface NiciraMatchCodecs {
    Reg0Codec REG0_CODEC = new Reg0Codec();
    Reg1Codec REG1_CODEC = new Reg1Codec();
    Reg2Codec REG2_CODEC = new Reg2Codec();
    Reg3Codec REG3_CODEC = new Reg3Codec();
    Reg4Codec REG4_CODEC = new Reg4Codec();
    Reg5Codec REG5_CODEC = new Reg5Codec();
    Reg6Codec REG6_CODEC = new Reg6Codec();
    Reg7Codec REG7_CODEC = new Reg7Codec();
    TunIdCodec TUN_ID_CODEC = new TunIdCodec();
    ArpOpCodec ARP_OP_CODEC = new ArpOpCodec();
    ArpShaCodec ARP_SHA_CODEC = new ArpShaCodec();
    ArpSpaCodec ARP_SPA_CODEC = new ArpSpaCodec();
    ArpThaCodec ARP_THA_CODEC = new ArpThaCodec();
    ArpTpaCodec ARP_TPA_CODEC = new ArpTpaCodec();
    InPortCodec NXM_OF_IN_PORT_CODEC = new InPortCodec();
    MplsLabelCodec OXM_OF_MPLS_LABEL = new MplsLabelCodec();
    MetadataCodec OXM_OF_METADATA_CODEC = new MetadataCodec();
    EthDstCodec ETH_DST_CODEC = new EthDstCodec();
    EthSrcCodec ETH_SRC_CODEC = new EthSrcCodec();
    TunIpv4DstCodec TUN_IPV4_DST_CODEC = new TunIpv4DstCodec();
    TunIpv4SrcCodec TUN_IPV4_SRC_CODEC = new TunIpv4SrcCodec();
    EthTypeCodec ETH_TYPE_CODEC = new EthTypeCodec();
    NspCodec NSP_CODEC = new NspCodec();
    NsiCodec NSI_CODEC = new NsiCodec();
    Nshc1Codec NSC1_CODEC = new Nshc1Codec();
    Nshc2Codec NSC2_CODEC = new Nshc2Codec();
    Nshc3Codec NSC3_CODEC = new Nshc3Codec();
    Nshc4Codec NSC4_CODEC = new Nshc4Codec();
    NshFlagsCodec NSH_FLAGS_CODEC = new NshFlagsCodec();
    NshMdtypeCodec NSH_MDTYPE_CODEC = new NshMdtypeCodec();
    NshNpCodec NSH_NP_CODEC = new NshNpCodec();
    NshTtlCodec NSH_TTL_CODEC = new NshTtlCodec();
    TcpSrcCodec TCP_SRC_CODEC = new TcpSrcCodec();
    TcpDstCodec TCP_DST_CODEC = new TcpDstCodec();
    UdpSrcCodec UDP_SRC_CODEC = new UdpSrcCodec();
    UdpDstCodec UDP_DST_CODEC = new UdpDstCodec();
    CtStateCodec CT_ST_CODEC = new CtStateCodec();
    CtZoneCodec CT_ZONE_CODEC = new CtZoneCodec();
    IpSrcCodec IP_SRC_CODEC = new IpSrcCodec();
    IpDstCodec IP_DST_CODEC = new IpDstCodec();
    Ipv6SrcCodec IPV6_SRC_CODEC = new Ipv6SrcCodec();
    Ipv6DstCodec IPV6_DST_CODEC = new Ipv6DstCodec();
    IcmpTypeCodec ICMP_TYPE_CODEC = new IcmpTypeCodec();
    CtMarkCodec CT_MARK_CODEC = new CtMarkCodec();
    CtTpSrcCodec CT_TP_SRC_CODEC = new CtTpSrcCodec();
    CtTpDstCodec CT_TP_DST_CODEC = new CtTpDstCodec();
    PktMarkCodec PKT_MARK_CODEC = new PktMarkCodec();
}
