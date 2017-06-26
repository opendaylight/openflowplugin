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
 * @author michal.polkorab
 */
public abstract class OxmMatchConstants {

    /** Backward compatibility with NXM */
    public static final int NXM_0_CLASS = 0x0000;
    /** Backward compatibility with NXM */
    public static final int NXM_1_CLASS = 0x0001;
    /** Basic class for OpenFlow */
    public static final int OPENFLOW_BASIC_CLASS = 0x8000;
    /** Experimenter class */
    public static final int EXPERIMENTER_CLASS = 0xFFFF;

    /** Switch input port */
    public static final int IN_PORT = 0;
    /** Switch physical input port */
    public static final int IN_PHY_PORT = 1;
    /** Metadata passed between tables */
    public static final int METADATA = 2;
    /** Ethernet destination address */
    public static final int ETH_DST = 3;
    /** Ethernet source address */
    public static final int ETH_SRC = 4;
    /** Ethernet frame type */
    public static final int ETH_TYPE = 5;
    /** VLAN id. */
    public static final int VLAN_VID = 6;
    /** VLAN priority. */
    public static final int VLAN_PCP = 7;
    /** IP DSCP (6 bits in ToS field). */
    public static final int IP_DSCP = 8;
    /** IP ECN (2 bits in ToS field). */
    public static final int IP_ECN = 9;
    /** IP protocol. */
    public static final int IP_PROTO = 10;
    /** IPv4 source address. */
    public static final int IPV4_SRC = 11;
    /** IPv4 destination address. */
    public static final int IPV4_DST = 12;
    /** TCP source port. */
    public static final int TCP_SRC = 13;
    /** TCP destination port. */
    public static final int TCP_DST = 14;
    /** UDP source port. */
    public static final int UDP_SRC = 15;
    /** UDP destination port. */
    public static final int UDP_DST = 16;
    /** SCTP source port. */
    public static final int SCTP_SRC = 17;
    /** SCTP destination port. */
    public static final int SCTP_DST = 18;
    /** ICMP type. */
    public static final int ICMPV4_TYPE = 19;
    /** ICMP code. */
    public static final int ICMPV4_CODE = 20;
    /** ARP opcode. */
    public static final int ARP_OP = 21;
    /** ARP source IPv4 address. */
    public static final int ARP_SPA = 22;
    /** ARP target IPv4 address. */
    public static final int ARP_TPA = 23;
    /** ARP source hardware address. */
    public static final int ARP_SHA = 24;
    /** ARP target hardware address. */
    public static final int ARP_THA = 25;
    /** IPv6 source address. */
    public static final int IPV6_SRC = 26;
    /** IPv6 destination address. */
    public static final int IPV6_DST = 27;
    /** IPv6 Flow Label */
    public static final int IPV6_FLABEL = 28;
    /** ICMPv6 type. */
    public static final int ICMPV6_TYPE = 29;
    /** ICMPv6 code. */
    public static final int ICMPV6_CODE = 30;
    /** Target address for ND. */
    public static final int IPV6_ND_TARGET = 31;
    /** Source link-layer for ND. */
    public static final int IPV6_ND_SLL = 32;
    /** Target link-layer for ND. */
    public static final int IPV6_ND_TLL = 33;
    /** MPLS label. */
    public static final int MPLS_LABEL = 34;
    /** MPLS TC. */
    public static final int MPLS_TC = 35;
    /** MPLS BoS bit. */
    public static final int MPLS_BOS = 36;
    /** PBB I-SID. */
    public static final int PBB_ISID = 37;
    /** Logical Port Metadata. */
    public static final int TUNNEL_ID = 38;
    /** IPv6 Extension Header pseudo-field */
    public static final int IPV6_EXTHDR = 39;

    /**
     * OFPXMC_NXM_1 class Constants
     */

    /** NXM IPv4 Tunnel Endpoint Source */
    public static final int NXM_NX_TUN_IPV4_SRC = 31;
    /** NXM IPv4 Tunnel Endpoint Destination */
    public static final int NXM_NX_TUN_IPV4_DST = 32;
    /** NXM TCP_Flag value */
    public static final int NXM_NX_TCP_FLAG = 34;

    private OxmMatchConstants() {
        //not called
    }
}