/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * IPv4 and IPv6 protocol types.
 *
 * @author Frank Wood
 */
public enum IpType implements ProtocolEnum {
    
    /** IPv6 Hop-by-Hop extension header option. */
    IPV6_HOPOPT(0x00),
    
    /** Internet Control Message Protocol. */
    ICMP(0x01),
    
    /** Internet Group Management Protocol. */
    IGMP(0x02),
    
    /** Gateway-to-Gateway Protocol. */
    GGP(0x03),
    
    /** IPv4(encapsulation). */
    IPV4_ENCAP(0x04),
    
    /** Internet Stream Protocol. */
    ST(0x05),
    
    /** Transmission Control Protocol. */
    TCP(0x06),
    
    /** Core-based trees. */
    CBT(0x07),
    
    /** Exterior Gateway Protocol. */
    EGP(0x08),
    
    /** Interior Gateway Protocol (any private interior gateway). */
    IGP(0x09),
    
    /** BBN RCC Monitoring. */
    BBN_RCC_MON(0x0a),
    
    /** Network Voice Protocol. */
    NVP_II(0x0b),
    
    /** Xerox PUP. */
    PUP(0x0c),
    
    /** ARGUS. */
    ARGUS(0x0d),
    
    /** EMCON. */
    EMCON(0x0e),
    
    /** Cross Net Debugger. */
    XNET(0x0f),

    /** Chaos. */
    CHAOS(0x10),

    /** User Datagram Protocol. */
    UDP(0x11),

    /** Multiplexing. */
    MUX(0x12),

    /** DCN Measurement Subsystems. */
    DCN_MEAS(0x13),

    /** Host Monitoring Protocol. */
    HMP(0x14),

    /** Packet Radio Measurement. */
    PRM(0x15),

    /** XEROX NS IDP. */
    XNS_IDP(0x16),

    /** Trunk-1. */
    TRUNK_1(0x17),

    /** Trunk-2. */
    TRUNK_2(0x18),

    /** Leaf-1. */
    LEAF_1(0x19),

    /** Leaf-2. */
    LEAF_2(0x1a),

    /** Reliable Datagram Protocol. */
    RDP(0x1b),

    /** Internet Reliable Transaction Protocol. */
    IRTP(0x1c),

    /** ISO Transport Protocol Class 4. */
    ISO_TP4(0x1d),

    /** Bulk Data Transfer Protocol. */
    NETBLT(0x1e),

    /** MFE Network Services Protocol. */
    MFE_NSP(0x1f),

    /** MERIT InterNodal Protocol. */
    MERIT_INP(0x20),

    /** Datagram Congestion Control Protocol. */
    DCCP(0x21),

    /** Third Party Connect Protocol. */
    CP_3RD(0x22),

    /** Inter-Domain Policy Routing Protocol. */
    IDPR(0x23),

    /** Xpress Transport Protocol. */
    XTP(0x24),

    /** Datagram Delivery Protocol. */
    DDP(0x25),

    /** IDPR Control Message Transport Protocol. */
    IDPR_CMTP(0x26),

    /** TP++ Transport Protocol. */
    TP_PLUS_PLUS(0x27),

    /** IL Transport Protocol. */
    IL(0x28),

    /** IPv6 6in4 (encapsulation). */
    IPV6_ENCAP(0x29),

    /** Source Demand Routing Protocol. */
    SDRP(0x2a),

    /** IPv6 Routing header extension option. */
    IPV6_ROUTING(0x2b),

    /** IPv6 Fragment header extension option. */
    IPV6_FRAG(0x2c),

    /** Inter-Domain Routing Protocol. */
    IDRP(0x2d),

    /** Resource Reservation Protocol. */
    RSVP(0x2e),

    /** Generic Routing Encapsulation. */
    GRE(0x2f),

    /** Mobile Host Routing Protocol. */
    MHRP(0x30),

    /** BNA. */
    BNA(0x31),

    /** Encapsulating crypt payload (used as an Ipv6 extension option). */
    ESP(0x32),

    /** Authentication header (used as an IPv6 extension option). */
    AUTH_HDR(0x33),

    /** Integrated Net Layer Security Protocol. */
    I_NLSP(0x34),

    /** SwIPe. */
    SWIPE(0x35),

    /** NBMA Address Resolution Protocol. */
    NARP(0x36),

    /** IP Mobility. */
    MOBILE(0x37),

    /** Transport Layer Security Protocol. */
    TLSP(0x38),

    /** Simple Key-Management for Internet Protocol. */
    SKIP(0x39),

    /** ICMP for IPv6. */
    IPV6_ICMP(0x3a),

    /** IPv6 No next header option. */
    IPV6_NO_NEXT_HDR(0x3b),

    /** IPv6 Destination extension option. */
    IPV6_DST_OPTS(0x3c),

    /** Any host internal protocol. */
    ANY_HOST_INTERN(0x3d),

    /** CFTP. */
    CFTP(0x3e),

    /** Any local network. */
    ANY_LOCAL_NET(0x3f),

    /** SATNET and Back room EXPAK. */
    SAT_EXPAK(0x40),

    /** Kryptolan. */
    KRYPTOLAN(0x41),

    /** MIT Remote Virtual Disk Protocol. */
    RVD(0x42),

    /** Internet Pluribus Packet Core. */
    IPPC(0x43),

    /** Any distributed file system. */
    ANY_DIST_FILE_SYS(0x44),

    /** SATNET Monitoring. */
    SAT_MON(0x45),

    /** VISA Protocol. */
    VISA(0x46),

    /** Internet Packet Core Utility. */
    IPCV(0x47),

    /** Computer Protocol Network Executive. */
    CPNX(0x48),

    /** Computer Protocol Heart Beat. */
    CPHB(0x49),

    /** Wang Span Network. */
    WSN(0x4a),

    /** Packet Video Protocol. */
    PVP(0x4b),

    /** Back room SATNET Monitoring. */
    BR_SAT_MON(0x4c),

    /** SUN ND PROTOCOL-Temporary. */
    SUN_ND(0x4d),

    /** WIDEBAND Monitoring. */
    WB_MON(0x4e),

    /** WIDEBAND EXPAK. */
    WB_EXPAK(0x4f),

    /** International Organization for Standardization Internet Protocol. */
    ISO_IP(0x50),

    /** Versatile Message Transaction Protocol. */
    VMTP(0x51),

    /** Secure Versatile Message Transaction Protocol. */
    SECURE_VMTP(0x52),

    /** VINES. */
    VINES(0x53),

    /** TTP. */
    TTP(0x54),

    /** Internet Protocol Traffic Manager. */
    IPTM(0x54),

    /** NSFNET-IGP. */
    NSFNET_IGP(0x55),

    /** Dissimilar Gateway Protocol. */
    DGP(0x56),

    /** TCF. */
    TCF(0x57),

    /** EIGRP. */
    EIGRP(0x58),

    /** Open Shortest Path First. */
    OSPF(0x59),

    /** Sprite RPC Protocol. */
    SPRITE_RPC(0x5a),

    /** Locus Address Resolution Protocol. */
    LARP(0x5b),

    /** Multicast Transport Protocol. */
    MTP(0x5c),

    /** AX.25. */
    AX_25(0x5d),

    /** IP-within-IP Encapsulation Protocol. */
    IPIP(0x5e),

    /** Mobile Inter-networking Control Protocol. */
    MICP(0x5f),

    /** Semaphore Communications SP. */
    SCC_SP(0x60),

    /** Ethernet-within-IP Encapsulation. */
    ETHER_IP(0x61),

    /** Encapsulation Header. */
    ENCAP_HDR(0x62),

    /** Any private encryption scheme. */
    ANY_PRIV_ENCRYP(0x63),

    /** GMTP. */
    GMTP(0x64),

    /** Ipsilon Flow Management Protocol. */
    IFMP(0x65),

    /** PNNI over IP. */
    PNNI(0x66),

    /** Protocol Independent Multicast. */
    PIM(0x67),

    /** IBM's ARIS (Aggregate Route IP Switching) Protocol. */
    ARIS(0x68),

    /** SCPS (Space Communications Protocol Standards). */
    SCPS(0x69),

    /** QNX. */
    QNX(0x6a),

    /** Active Networks. */
    ACT_NET(0x6b),

    /** IP Payload Compression Protocol. */
    IP_COMP(0x6c),

    /** Sitara Networks Protocol. */
    SNP(0x6d),

    /** Compaq Peer Protocol. */
    COMPAQ_PEER(0x6e),

    /** IPX in IP. */
    IPX_IN_IP(0x6f),

    /** Virtual Router Redundancy Protocol. */
    VRRP(0x70),

    /** PGM Reliable Transport Protocol. */
    PGM(0x71),

    /** Any 0-hop protocol. */
    ANY_0_HOP(0x72),

    /** Layer Two Tunneling Protocol Version 3. */
    L2TP(0x73),

    /** D-II Data Exchange (DDX). */
    DDX(0x74),

    /** Interactive Agent Transfer Protocol. */
    IATP(0x75),

    /** Schedule Transfer Protocol. */
    STP(0x76),

    /** SpectraLink Radio Protocol. */
    SRP(0x77),

    /** UTI. */
    UTI(0x78),

    /** Simple Message Protocol. */
    SMP(0x79),

    /** SM. */
    SM(0x7a),

    /** Performance Transparency Protocol. */
    PTP(0x7b),

    /** IS over IPv4. */
    IS_OVER_IPV4(0x7c),

    /** FIRE. */
    FIRE(0x7d),

    /** Combat Radio Transport Protocol. */
    CRTP(0x7e),

    /** Combat Radio User Datagram. */
    CRUDP(0x7f),

    /** SSCOPMCE. */
    SSCOPMCE(0x80),

    /** IPLT. */
    IPLT(0x81),

    /** Secure Packet Shield. */
    SPS(0x82),

    /** Private IP Encapsulation within IP. */
    PIPE(0x83),

    /** Stream Control Transmission Protocol. */
    SCTP(0x84),

    /** Fiber Channel. */
    FC(0x85),

    /** RSVP-E2E-IGNORE. */
    RSVP_E2E_IGNORE(0x86),

    /** Mobility Header. */
    MOBILITY_HDR(0x87),

    /** UDP Lite. */
    UDP_LITE(0x88),

    /** MPLS-in-IP. */
    MPLS_IN_IP(0x89),

    /** MANET Protocols. */
    MANET(0x8A),

    /** Host Identity Protocol. */
    HIP(0x8B),

    /** Site Multihoming by IPv6 Intermediation. */
    SHIM_V6(0x8C),

    /** Reserved type. */
    RESERVED(0x0ff),
    ;
    
    private int code;
    
    private IpType(int code) {
        this.code = code;
    }
    
    @Override
    public int code() {
        return code;
    }

    static IpType get(int code) {
        return ProtocolUtils.getEnum(IpType.class, code, RESERVED);
    }
    
    /**
     * Returns true if this type is a valid IPv6 extension header and NOT
     * the next protocol layer.
     * 
     * @param type enumeration type to check
     * @return true if this type is a valid IPv6 extension header
     */
    static boolean isExtHdrV6(IpType type) {
        switch (type) {
            case IPV6_HOPOPT:
            case IPV6_ROUTING:
            case IPV6_FRAG:
            case IPV6_DST_OPTS:
            case ESP:
            case AUTH_HDR:
            case IPV6_NO_NEXT_HDR:
                return true;
        }
        return false;
    }

}
