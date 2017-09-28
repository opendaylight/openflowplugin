/*
 * Copyright (c) 2014, 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx;

import org.opendaylight.openflowjava.nx.codec.match.ArpOpCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpShaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpSpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpThaCodec;
import org.opendaylight.openflowjava.nx.codec.match.ArpTpaCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtMarkCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtStateCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtZoneCodec;
import org.opendaylight.openflowjava.nx.codec.match.EncapEthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.EncapEthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EncapEthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.IcmpTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.InPortCodec;
import org.opendaylight.openflowjava.nx.codec.match.IpDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.IpSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.Ipv6DstCodec;
import org.opendaylight.openflowjava.nx.codec.match.Ipv6SrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.MetadataCodec;
import org.opendaylight.openflowjava.nx.codec.match.MplsLabelCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshMdtypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.NshNpCodec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Nshc4Codec;
import org.opendaylight.openflowjava.nx.codec.match.NsiCodec;
import org.opendaylight.openflowjava.nx.codec.match.NspCodec;
import org.opendaylight.openflowjava.nx.codec.match.Reg0Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg4Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg5Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg6Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg7Codec;
import org.opendaylight.openflowjava.nx.codec.match.TcpDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.TcpSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunGpeNpCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIdCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4DstCodec;
import org.opendaylight.openflowjava.nx.codec.match.TunIpv4SrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.UdpDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.UdpSrcCodec;

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
    public static final MetadataCodec OXM_OF_METADATA_CODEC = new MetadataCodec();
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
    public static final CtMarkCodec CT_MARK_CODEC = new CtMarkCodec();
}
