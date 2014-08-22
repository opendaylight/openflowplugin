/*
 * (c) Copyright 2013-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.util.net.*;

import java.util.Set;

/**
 * Represents the data parsed from the deprecated 1.0 and 1.1 match structures.
 * This class is simply a DTO used internally to simplify the translation
 * between 1.0/1.1 encoded form and 1.3 Match/OXM-MatchField rich-data-type.
 *
 * @author Simon Hunt
 */
class OldMatch {

    /** The protocol version. */
    final ProtocolVersion version;

    /** Explicitly defined in 1.1 only, but assumed for 1.0;
     * (OFPMT_STANDARD == 0).
     */
    int type;
    /** Explicitly defined in 1.1 only, but assumed for 1.0;
     * length of structure (either OFPMT_STANDARD_LENGTH == 88,
     * or STANDARD_LENGTH_10 == 40).
     */
    int length;
    /** Input switch port. (Just u16 for 1.0) */
    BigPortNumber inPort;
    /** Wildcard flags. */
    Set<Wildcard> wildcards;
    /** Ethernet source address. */
    MacAddress dlSrc;
    /** 1.1 Only: Ethernet source address mask. */
    MacAddress dlSrcMask;
    /** Ethernet destination address. */
    MacAddress dlDst;
    /** 1.1 Only: Ethernet destination address mask. */
    MacAddress dlDstMask;
    /** Input VLAN id. (Note, stored as a VId not VlanId here). */
    VId dlVlan;
    /** Input VLAN priority. */
    int dlVlanPcp;
    /** Ethernet frame type. */
    EthernetType dlType;
    /** IP ToS (actually DSCP field, 6 bits). */
    int nwTos;
    /** IP Protocol or lower 8 bits of ARP opcode. */
    int nwProto;
    /** IP Source address. */
    IpAddress nwSrc;
    /** IP Source address mask (1.0, inferred wildcards 6-bit field). */
    IpAddress nwSrcMask;
    /** IP Destination address mask. */
    IpAddress nwDst;
    /** IP Destination address mask (1.0, inferred wildcards 6-bit field). */
    IpAddress nwDstMask;
    /** TCP/UDP/SCTP source port (only TCP/UDP documented in 1.0 spec) */
    PortNumber tpSrc;
    /** TCP/UDP/SCTP destination port (only TCP/UDP documented in 1.0 spec) */
    PortNumber tpDst;
    /** 1.1 Only: MPLS Label */
    int mplsLabel;
    /** 1.1 Only: MPLS TC */
    int mplsTc;
    /** 1.1 Only: Metadata passed between tables */
    long metadata;
    /** 1.1 Only: Metadata mask */
    long metadataMask;

    /** Construct the DTO.
     *
     * @param pv the protocol version
     */
    OldMatch(ProtocolVersion pv) {
        version = pv;
    }
}
