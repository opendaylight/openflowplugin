/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

/**
 * @author msunal
 * @author Josh Hershberg (jhershbe@redhat.com)
 */
public class NiciraMatchCodecs {
    private NiciraMatchCodecs () { }

    public static final Reg0Codec REG0_CODEC = new Reg0Codec();
    public static final Reg1Codec REG1_CODEC = new Reg1Codec();
    public static final Reg2Codec REG2_CODEC = new Reg2Codec();
    public static final Reg3Codec REG3_CODEC = new Reg3Codec();
    public static final Reg4Codec REG4_CODEC = new Reg4Codec();
    public static final Reg5Codec REG5_CODEC = new Reg5Codec();
    public static final Reg6Codec REG6_CODEC = new Reg6Codec();
    public static final Reg7Codec REG7_CODEC = new Reg7Codec();
    public static final TunIdCodec TUN_ID_CODEC = new TunIdCodec();
    public static final ArpOpCodec ARP_OP_CODEC = new ArpOpCodec();
    public static final ArpShaCodec ARP_SHA_CODEC = new ArpShaCodec();
    public static final ArpSpaCodec ARP_SPA_CODEC = new ArpSpaCodec();
    public static final ArpThaCodec ARP_THA_CODEC = new ArpThaCodec();
    public static final ArpTpaCodec ARP_TPA_CODEC = new ArpTpaCodec();
    public static final InPortCodec NXM_OF_IN_PORT_CODEC = new InPortCodec();
    public static final MplsLabelCodec OXM_OF_MPLS_LABEL = new MplsLabelCodec();
    public static final EthDstCodec ETH_DST_CODEC = new EthDstCodec();
    public static final EthSrcCodec ETH_SRC_CODEC = new EthSrcCodec();
    public static final TunIpv4DstCodec TUN_IPV4_DST_CODEC = new TunIpv4DstCodec();
    public static final TunIpv4SrcCodec TUN_IPV4_SRC_CODEC = new TunIpv4SrcCodec();
    public static final EthTypeCodec ETH_TYPE_CODEC = new EthTypeCodec();
    public static final NspCodec NSP_CODEC = new NspCodec();
    public static final NsiCodec NSI_CODEC = new NsiCodec();
    public static final Nshc1Codec NSC1_CODEC = new Nshc1Codec();
    public static final Nshc2Codec NSC2_CODEC = new Nshc2Codec();
    public static final Nshc3Codec NSC3_CODEC = new Nshc3Codec();
    public static final Nshc4Codec NSC4_CODEC = new Nshc4Codec();
    public static final EncapEthTypeCodec ENCAP_ETH_TYPE_CODEC = new EncapEthTypeCodec();
    public static final EncapEthSrcCodec ENCAP_ETH_SRC_CODEC = new EncapEthSrcCodec();
    public static final EncapEthDstCodec ENCAP_ETH_DST_CODEC = new EncapEthDstCodec();
    public static final NshMdtypeCodec NSH_MDTYPE_CODEC = new NshMdtypeCodec();
    public static final NshNpCodec NSH_NP_CODEC = new NshNpCodec();
    public static final TunGpeNpCodec TUN_GPE_NP_CODEC = new TunGpeNpCodec();
    public static final TcpSrcCodec TCP_SRC_CODEC = new TcpSrcCodec();
    public static final TcpDstCodec TCP_DST_CODEC = new TcpDstCodec();
    public static final UdpSrcCodec UDP_SRC_CODEC = new UdpSrcCodec();
    public static final UdpDstCodec UDP_DST_CODEC = new UdpDstCodec();
    public static final CtStateCodec CT_ST_CODEC = new CtStateCodec();
    public static final CtZoneCodec CT_ZONE_CODEC = new CtZoneCodec();
    public static final IpSrcCodec IP_SRC_CODEC = new IpSrcCodec();
    public static final IpDstCodec IP_DST_CODEC = new IpDstCodec();
    public static final Ipv6SrcCodec IPV6_SRC_CODEC = new Ipv6SrcCodec();
    public static final Ipv6DstCodec IPV6_DST_CODEC = new Ipv6DstCodec();
    public static final IcmpTypeCodec ICMP_TYPE_CODEC = new IcmpTypeCodec();
}
