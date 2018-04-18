/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.util;

/**
 * Stores oxm_match constants.
 *
 * @author michal.polkorab
 */
public interface OxmMatchConstants {

    /** Backward compatibility with NXM. */
    int NXM_0_CLASS = 0x0000;

    /** Backward compatibility with NXM. */
    int NXM_1_CLASS = 0x0001;

    /** Basic class for OpenFlow. */
    int OPENFLOW_BASIC_CLASS = 0x8000;

    /** Experimenter class. */
    int EXPERIMENTER_CLASS = 0xFFFF;

    /** Switch input port. */
    int IN_PORT = 0;

    /** Switch physical input port. */
    int IN_PHY_PORT = 1;

    /** Metadata passed between tables. */
    int METADATA = 2;

    /** Ethernet destination address. */
    int ETH_DST = 3;

    /** Ethernet source address. */
    int ETH_SRC = 4;

    /** Ethernet frame type. */
    int ETH_TYPE = 5;

    /** VLAN id. */
    int VLAN_VID = 6;

    /** VLAN priority. */
    int VLAN_PCP = 7;

    /** IP DSCP (6 bits in ToS field). */
    int IP_DSCP = 8;

    /** IP ECN (2 bits in ToS field). */
    int IP_ECN = 9;

    /** IP protocol. */
    int IP_PROTO = 10;

    /** IPv4 source address. */
    int IPV4_SRC = 11;

    /** IPv4 destination address. */
    int IPV4_DST = 12;

    /** TCP source port. */
    int TCP_SRC = 13;

    /** TCP destination port. */
    int TCP_DST = 14;

    /** UDP source port. */
    int UDP_SRC = 15;

    /** UDP destination port. */
    int UDP_DST = 16;

    /** SCTP source port. */
    int SCTP_SRC = 17;

    /** SCTP destination port. */
    int SCTP_DST = 18;

    /** ICMP type. */
    int ICMPV4_TYPE = 19;

    /** ICMP code. */
    int ICMPV4_CODE = 20;

    /** ARP opcode. */
    int ARP_OP = 21;

    /** ARP source IPv4 address. */
    int ARP_SPA = 22;

    /** ARP target IPv4 address. */
    int ARP_TPA = 23;

    /** ARP source hardware address. */
    int ARP_SHA = 24;

    /** ARP target hardware address. */
    int ARP_THA = 25;

    /** IPv6 source address. */
    int IPV6_SRC = 26;

    /** IPv6 destination address. */
    int IPV6_DST = 27;

    /** IPv6 Flow Label. */
    int IPV6_FLABEL = 28;

    /** ICMPv6 type. */
    int ICMPV6_TYPE = 29;

    /** ICMPv6 code. */
    int ICMPV6_CODE = 30;

    /** Target address for ND. */
    int IPV6_ND_TARGET = 31;

    /** Source link-layer for ND. */
    int IPV6_ND_SLL = 32;

    /** Target link-layer for ND. */
    int IPV6_ND_TLL = 33;

    /** MPLS label. */
    int MPLS_LABEL = 34;

    /** MPLS TC. */
    int MPLS_TC = 35;

    /** MPLS BoS bit. */
    int MPLS_BOS = 36;

    /** PBB I-SID. */
    int PBB_ISID = 37;

    /** Logical Port Metadata. */
    int TUNNEL_ID = 38;

    /** IPv6 Extension Header pseudo-field. */
    int IPV6_EXTHDR = 39;

    /** Packet Type. */
    int PACKET_TYPE = 44;

    /**
     * OFPXMC_NXM_1 class Constants.
     */

    /** NXM IPv4 Tunnel Endpoint Source. */
    int NXM_NX_TUN_IPV4_SRC = 31;

    /** NXM IPv4 Tunnel Endpoint Destination.
     *  */
    int NXM_NX_TUN_IPV4_DST = 32;

    /** NXM TCP_Flag value. */
    int NXM_NX_TCP_FLAG = 34;
}
