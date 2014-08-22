/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * A network protocol key used to identify protocol classes. This is used
 * to identify which encoder to use for each protocol.
 *
 * @author Frank Wood
 */
public enum ProtocolId {
    
    /** No protocol (indicates that no protocol follows - i.e. no payload) */
    NONE(0x0),
    /** Unknown protocol. */
    UNKNOWN(0x1),
    /** Ethernet protocol (Ethernet2, Ethernet 802.3, etc.). */
    ETHERNET(0x2),
    /** PPP-over-Ethernet protocol. */
    PPP_ETHERNET(0x4),
    /** MPLS protocol. */
    MPLS(0x8),
    /** Address Resolution Protocol (ARP). */
    ARP(0x10),
    /** Internet Protocol version 4 (IPv4). */
    IP(0x20),
    /** Generic Routing Encapsulation (GRE). */
    GRE(0x2f),
    /** Internet Protocol version 6 (IPv6). */
    IPV6(0x40),
    /** Broadcast Domain Discovery Protocol (BDDP). */
    BDDP(0x80),
    /** Link Layer Discovery Protocol (LLDP). */
    LLDP(0x100),
    /** Internet Control Message Protocol version 4 (ICMPv4). */
    ICMP(0x200),
    /** Internet Control Message Protocol version 6 (ICMPv6). */
    ICMPV6(0x400),
    /** Transmission Control Protocol (TCP). */
    TCP(0x800),
    /** User Datagram Protocol (UDP). */
    UDP(0x1000),
    /** Stream Control Transmission Protocol (SCTP). */
    SCTP(0x2000),
    /** Dynamic Host Configuration Protocol version 4 (DHCP). */
    DHCP(0x4000),
    /** Dynamic Host Configuration Protocol version 6 (DHCPv6). */
    DHCPV6(0x8000),
    /** Domain Name System Protocol version 4 (DNS). */
    DNS(0x10000),
    ;
    
    private final long bit;

    /**
     * Creates the ID and assigns it the specified representation bit.
     * 
     * @param bit protocol ID bit
     */
    private ProtocolId(long bit) {
        this.bit = bit;
    }
    
    /**
     * Returns the protocol ID representation bit.
     * 
     * @return protocol ID bit
     */
    long bit() {
        return bit;
    }
    
}
