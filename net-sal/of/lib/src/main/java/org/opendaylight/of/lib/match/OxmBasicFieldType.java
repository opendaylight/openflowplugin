/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.DecodeException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.ResourceUtils;

import java.util.ResourceBundle;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;

/**
 * Denotes the default set of match field types for the
 * {@link OxmClass#OPENFLOW_BASIC OXM Basic class}.
 * <p>
 * Those match fields that all OpenFlow switches (v1.2, v1.3) are required to
 * support are marked as "Required".
 * <p>
 * The description of each field type includes the size of the field in bits,
 * whether or not a bitmask can be applied to the field (for wild-carding),
 * and what pre-requisites exist (if any).
 *
 * @author Simon Hunt
 */
public enum OxmBasicFieldType implements OxmFieldType {

    /** Switch input port; Required; Since 1.2.
     * <p>
     * Ingress port. Numerical representation of incoming port, starting at 1.
     * This may be a physical or switch-defined logical port.
     * <p>
     * 32 bits; No mask.<br>
     * No pre-requisite.
     */
    IN_PORT(0, true, 4, false),

    /** Switch physical input port; Since 1.2.
     * <p>
     * Physical port. In {@code OfmPacketIn} messages, underlying physical
     * port when packet received on a logical port.
     * <p>
     * 32 bits; No mask.<br>
     * Pre-requisite: {@link #IN_PORT} present.
     */
    IN_PHY_PORT(1, false, 4, false),

    /** Metadata passed between tables; Since 1.2.
     * <p>
     * Table metadata. Used to pass information between tables.
     * <p>
     * 64 bits; Mask allowed.<br>
     * No pre-requisite.
     */
    METADATA(2, false, 8, true),

    /** Ethernet destination address; Required; Since 1.2.
     * <p>
     * Ethernet destination MAC address.
     * <p>
     * 48 bits; Mask allowed.<br>
     * No pre-requisite.
     */
    ETH_DST(3, true, 6, true),

    /** Ethernet source address; Required; Since 1.2.
     * <p>
     * Ethernet source MAC address.
     * <p>
     * 48 bits; Mask allowed.<br>
     * No pre-requisite.
     */
    ETH_SRC(4, true, 6, true),

    /** Ethernet frame type; Required; Since 1.2.
     * <p>
     * Ethernet type of the OpenFlow packet payload, after VLAN tags.
     * <p>
     * 16 bits; No mask.<br>
     * No pre-requisite.
     */
    ETH_TYPE(5, true, 2, false),

    /** VLAN id; Since 1.2.
     * <p>
     * VLAN-ID from 802.1Q header. The CFI bit indicates the presence
     * of a valid VLAN-ID.
     * <p>
     * 12+1 bits; No mask.<br>
     * No pre-requisite.
     */
    VLAN_VID(6, false, 2, true),

    /** VLAN priority; Since 1.2.
     * <p>
     * VLAN-PCP from 802.1Q header.
     * <p>
     * 3 bits; No mask.<br>
     * Pre-requisite: {@code VLAN_VID present and != NONE}
     */
    VLAN_PCP(7, false, 1, false),

    /** IP DSCP (6 bits in ToS field); Since 1.2.
     * <p>
     * Diff Serv Code Point (DSCP). Part of the IPv4 ToS field or the
     * IPv6 Traffic Class field.
     * <p>
     * 6 bits; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0800} (IPv4) or
     * {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    IP_DSCP(8, false, 1, false),

    /** IP ECN (2 bits in ToS field); Since 1.2.
     * <p>
     * ECN bits of the IP header. Part of the IPv4 ToS field or the
     * IPv6 Traffic Class field.
     * <p>
     * 2 bits; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0800} (IPv4) or
     * {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    IP_ECN(9, false, 1, false),

    /** IP Protocol; Required; Since 1.2.
     * <p>
     * IPv4 or IPv6 protocol number.
     * <p>
     * 8 bits; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0800} (IPv4) or
     * {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    IP_PROTO(10, true, 1, false),

    /** IPv4 source address; Required; Since 1.2.
     * <p>
     * IPv4 source address. Can use subnet mask or arbitrary bitmask.
     * <p>
     * 32 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0800} (IPv4).
     */
    IPV4_SRC(11, true, 4, true),

    /** IPv4 destination address; Required; Since 1.2.
     * <p>
     * IPv4 destination address. Can use subnet mask or arbitrary bitmask.
     * <p>
     * 32 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0800} (IPv4).
     */
    IPV4_DST(12, true, 4, true),

    /** TCP source port; Required; Since 1.2.
     * <p>
     *  TCP source port.
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 6} (TCP).
     */
    TCP_SRC(13, true, 2, false),

    /** TCP destination port; Required; Since 1.2.
     * <p>
     *  TCP destination port.
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 6} (TCP).
     */
    TCP_DST(14, true, 2, false),

    /** UDP source port; Required; Since 1.2.
     * <p>
     *  UDP source port.
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 17} (UDP).
     */
    UDP_SRC(15, true, 2, false),

    /** UDP destination port; Required; Since 1.2.
     * <p>
     *  UDP destination port.
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 17} (UDP).
     */
    UDP_DST(16, true, 2, false),

    /** SCTP source port; Since 1.2.
     * <p>
     *  SCTP source port.
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 132} (SCTP).
     */
    SCTP_SRC(17, false, 2, false),

    /** SCTP destination port; Since 1.2.
     * <p>
     *  SCTP destination port.
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 132} (SCTP).
     */
    SCTP_DST(18, false, 2, false),

    /** ICMP type; Since 1.2.
     * <p>
     * ICMP type.
     * <p>
     * 8 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 1} (ICMP).
     */
    ICMPV4_TYPE(19, false, 1, false),

    /** ICMP code; Since 1.2.
     * <p>
     * ICMP code.
     * <p>
     * 8 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 1} (ICMP).
     */
    ICMPV4_CODE(20, false, 1, false),

    /** ARP opcode; Since 1.2.
     * <p>
     * ARP opcode
     * <p>
     * 16 bits; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0806} (ARP).
     */
    ARP_OP(21, false, 2, false),

    /** ARP source IPv4 address; Since 1.2.
     * <p>
     * Source IPv4 address in the ARP payload. Can use subnet mask
     * or arbitrary bitmask.
     * <p>
     * 32 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0806} (ARP).
     */
    ARP_SPA(22, false, 4, true),

    /** ARP target IPv4 address; Since 1.2.
     * <p>
     * Target IPv4 address in the ARP payload. Can use subnet mask
     * or arbitrary bitmask.
     * <p>
     * 32 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0806} (ARP).
     */
    ARP_TPA(23, false, 4, true),

    /** ARP source hardware address; Since 1.2.
     * <p>
     * Source Ethernet address in the ARP payload.
     * <p>
     * 48 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0806} (ARP).
     */
    ARP_SHA(24, false, 6, true),

    /** ARP target hardware address; Since 1.2.
     * <p>
     * Target Ethernet address in the ARP payload.
     * <p>
     * 48 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x0806} (ARP).
     */
    ARP_THA(25, false, 6, true),

    /** IPv6 source address; Required; Since 1.2.
     * <p>
     * IPv6 source address. Can use subnet mask or arbitrary bitmask.
     * <p>
     * 128 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    IPV6_SRC(26, true, 16, true),

    /** IPv6 destination address; Required; Since 1.2.
     * <p>
     * IPv6 destination address. Can use subnet mask or arbitrary bitmask.
     * <p>
     * 128 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    IPV6_DST(27, true, 16, true),

    /** IPv6 Flow Label; Since 1.2.
     * <p>
     * IPv6 flow label.
     * <p>
     * 20 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    // FIXME: length is 3 or 4?
    IPV6_FLABEL(28, false, 3, true),

    /** ICMPv6 type; Since 1.2.
     * <p>
     * ICMPv6 type.
     * <p>
     * 8 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 58} (ICMPv6).
     */
    ICMPV6_TYPE(29, false, 1, false),

    /** ICMPv6 code; Since 1.2.
     * <p>
     * ICMPv6 code.
     * <p>
     * 8 bits; No mask.<br>
     * Pre-requisite: {@code IP_PROTO == 58} (ICMPv6).
     */
    ICMPV6_CODE(30, false, 1, false),

    /** Target address for ND; Since 1.2.
     * <p>
     * The target address in an IPv6 Neighbor Discovery message.
     * <p>
     * 128 bits; No mask.<br>
     * Pre-requisite: {@code ICMPV6_TYPE == 135} (NBR_SOL) or
     * {@code ICMPV6_TYPE == 136} (NBR_ADV).
     */
    IPV6_ND_TARGET(31, false, 16, false),

    /** Source link-layer for ND; Since 1.2.
     * <p>
     * The source link-layer address option in an IPv6 Neighbor Discovery
     * message.
     * <p>
     * 48 bits; No mask.<br>
     * Pre-requisite: {@code ICMPV6_TYPE == 135} (NBR_SOL).
     */
    IPV6_ND_SLL(32, false, 6, false),

    /** Target link-layer for ND; Since 1.2.
     * <p>
     * The target link-layer address option in an IPv6 Neighbor Discovery
     * message.
     * <p>
     * 48 bits; No mask.<br>
     * Pre-requisite: {@code ICMPV6_TYPE == 136} (NBR_ADV).
     */
    IPV6_ND_TLL(33, false, 6, false),

    /** MPLS label; Since 1.2.
     * <p>
     * The LABEL in the first MPLS shim header.
     * <p>
     * 20 bits; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x8847} (MPLS_U) or
     * {@code ETH_TYPE == 0x8848} (MPLS_M).
     */
    // FIXME: length is 3 or 4?
    MPLS_LABEL(34, false, 3, false),

    /** MPLS TC; Since 1.2.
     * <p>
     * The TC in the first MPLS shim header.
     * <p>
     * 3 bits; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x8847} (MPLS_U) or
     * {@code ETH_TYPE == 0x8848} (MPLS_M).
     */
    MPLS_TC(35, false, 1, false),

    /** MPLS BoS bit; Since 1.3.
     * <p>
     * The BoS bit in the first MPLS shim header.
     * <p>
     * 1 bit; No mask.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x8847} (MPLS_U) or
     * {@code ETH_TYPE == 0x8848} (MPLS_M).
     */
    MPLS_BOS(36, false, 1, false),

    /** PBB I-SID; Since 1.3.
     * <p>
     * The I-SID in the first PBB service instance tag.
     * <p>
     * 24 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x88e7} (PBB).
     */
    // FIXME: length is 3 or 4?
    PBB_ISID(37, false, 3, true),

    /** Logical Port Metadata; Since 1.3.
     * <p>
     * Metadata associated with a logical port.
     * <p>
     * 64 bits; Mask allowed.<br>
     * No pre-requisite.
     */
    TUNNEL_ID(38, false, 8, true),

    /** IPv6 Extension Header pseudo-field; Since 1.3.
     * <p>
     * IPv6 Extension Header pseudo-field.
     * <p>
     * 9 bits; Mask allowed.<br>
     * Pre-requisite: {@code ETH_TYPE == 0x86dd} (IPv6).
     */
    IPV6_EXTHDR(39, false, 2, true),

    ; // required semi-colon

    static final int V12_MAX = 35; // max code for version 1.2
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OxmBasicFieldType.class, "oxmBasicFieldType");

    static final String E_BAD_CODE = RES.getString("e_bad_code");

    private int code;
    private boolean required;
    private int payloadLength;
    private boolean maskable;

    /** Constructor.
     *
     * @param code the field type code
     * @param required OpenFlow switches required to implement this field
     * @param length expected payload length in bytes
     * @param maskable whether this match field may include a mask
     */
    OxmBasicFieldType(int code, boolean required, int length, boolean maskable) {
        this.code = code;
        this.required = required;
        this.payloadLength = length;
        this.maskable = maskable;
    }

    /** Returns the code value for this basic field type.
     *
     * @return the code value
     */
    public int getCode() {
        return code;
    }

    /** Returns true if this match field type is required to be supported
     * by OpenFlow (v1.2, v1.3) switches.
     *
     * @return true if the field type is required
     */
    public boolean isRequired() {
        return required;
    }

    /** Returns true if this match field type allows masking of the value.
     *
     * @return true if the field type is maskable
     */
    public boolean isMaskable() {
        return maskable;
    }

    /** Returns the expected payload length of this field type.
     *
     * @param hasMask payload include a mask?
     * @return the expected payload length
     */
    int expectedLength(boolean hasMask) {
        return hasMask ? payloadLength * 2 : payloadLength;
    }

    /** Decodes the basic field type value and returns the corresponding
     * constant. If the code is not recognized, an exception is thrown.
     *
     * @param code the encoded field type
     * @param pv the protocol version
     * @return the field type
     * @throws DecodeException if the code is not recognized
     * @throws VersionMismatchException if the code is not supported in the
     *          given version
     */
    static OxmBasicFieldType decode(int code, ProtocolVersion pv)
            throws DecodeException {
        OxmBasicFieldType type = null;
        for (OxmBasicFieldType ft: values())
            if (ft.code == code) {
                type = ft;
                break;
            }
        // exit now if no match
        if (type == null)
            throw new DecodeException("OxmBasicFieldType: unknown code: " + code);

        // validate version constraints
        if (pv.lt(V_1_2) || (pv == V_1_2 && code > V12_MAX))
            throw new VersionMismatchException(pv + E_BAD_CODE + type);
        return type;
    }
}