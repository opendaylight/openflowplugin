/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.openflowjava.nx.codec.match.CtStateCodec;
import org.opendaylight.openflowjava.nx.codec.match.CtZoneCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthDstCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.EthTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.IcmpTypeCodec;
import org.opendaylight.openflowjava.nx.codec.match.IpSrcCodec;
import org.opendaylight.openflowjava.nx.codec.match.IpDstCodec;
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
    public static final TcpSrcCodec TCP_SRC_CODEC = new TcpSrcCodec();
    public static final TcpDstCodec TCP_DST_CODEC = new TcpDstCodec();
    public static final UdpSrcCodec UDP_SRC_CODEC = new UdpSrcCodec();
    public static final UdpDstCodec UDP_DST_CODEC = new UdpDstCodec();
    public static final IpSrcCodec IP_SRC_CODEC = new IpSrcCodec();
    public static final IpDstCodec IP_DST_CODEC = new IpDstCodec();
    public static final IcmpTypeCodec ICMP_TYPE_CODEC = new IcmpTypeCodec();
    public static final CtStateCodec CT_ST_CODEC = new CtStateCodec();
    public static final CtZoneCodec CT_ZONE_CODEC = new CtZoneCodec();
}
