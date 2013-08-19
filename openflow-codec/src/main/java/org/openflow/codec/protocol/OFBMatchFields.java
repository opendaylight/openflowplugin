package org.openflow.codec.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * OXM match field defined for OPENFLOW_BASIC type correspond to enum
 * oxm_ofb_match_fields
 *
 * @author AnilGujele
 *
 */
public enum OFBMatchFields {

    IN_PORT((byte) 0, 32, false), /* Switch input port. */
    IN_PHY_PORT((byte) 1, 32, false), /* Switch physical input port. */
    METADATA((byte) 2, 64, true), /* Metadata passed between tables. */
    ETH_DST((byte) 3, 48, true), /* Ethernet destination address. */
    ETH_SRC((byte) 4, 48, true), /* Ethernet source address. */
    ETH_TYPE((byte) 5, 16, false), /* Ethernet frame type. */
    VLAN_VID((byte) 6, 13, true), /* VLAN id. */
    VLAN_PCP((byte) 7, 3, false), /* VLAN priority. */
    IP_DSCP((byte) 8, 6, false), /* IP DSCP (6 bits in ToS field). */
    IP_ECN((byte) 9, 2, false), /* IP ECN (2 bits in ToS field). */
    IP_PROTO((byte) 10, 8, false), /* IP protocol. */
    IPV4_SRC((byte) 11, 32, true), /* IPv4 source address. */
    IPV4_DST((byte) 12, 32, true), /* IPv4 destination address. */
    TCP_SRC((byte) 13, 16, false), /* TCP source port. */
    TCP_DST((byte) 14, 16, false), /* TCP destination port. */
    UDP_SRC((byte) 15, 16, false), /* UDP source port. */
    UDP_DST((byte) 16, 16, false), /* UDP destination port. */
    SCTP_SRC((byte) 17, 16, false), /* SCTP source port. */
    SCTP_DST((byte) 18, 16, false), /* SCTP destination port. */
    ICMPV4_TYPE((byte) 19, 8, false), /* ICMP type. */
    ICMPV4_CODE((byte) 20, 8, false), /* ICMP code. */
    ARP_OP((byte) 21, 16, false), /* ARP opcode. */
    ARP_SPA((byte) 22, 32, true), /* ARP source IPv4 address. */
    ARP_TPA((byte) 23, 32, true), /* ARP target IPv4 address. */
    ARP_SHA((byte) 24, 48, true), /* ARP source hardware address. */
    ARP_THA((byte) 25, 48, true), /* ARP target hardware address. */
    IPV6_SRC((byte) 26, 128, true), /* IPv6 source address. */
    IPV6_DST((byte) 27, 128, true), /* IPv6 destination address. */
    IPV6_FLABEL((byte) 28, 20, true), /* IPv6 Flow Label */
    ICMPV6_TYPE((byte) 29, 8, false), /* ICMPv6 type. */
    ICMPV6_CODE((byte) 30, 8, false), /* ICMPv6 code. */
    IPV6_ND_TARGET((byte) 31, 128, false), /* Target address for ND. */
    IPV6_ND_SLL((byte) 32, 48, false), /* Source link-layer for ND. */
    IPV6_ND_TLL((byte) 33, 48, false), /* Target link-layer for ND. */
    MPLS_LABEL((byte) 34, 20, false), /* MPLS label. */
    MPLS_TC((byte) 35, 3, false), /* MPLS TC. */
    OFP_MPLS_BOS((byte) 36, 1, false), /* MPLS BoS bit. */
    PBB_ISID((byte) 37, 24, true), /* PBB I-SID. */
    TUNNEL_ID((byte) 38, 64, true), /* Logical Port Metadata. */
    IPV6_EXTHDR((byte) 39, 9, true); /* IPv6 Extension Header pseudo-field */

    private static OFBMatchFields[] mapping;
    private byte value;
    private int lengthInBits;
    private boolean hasMask;

    private OFBMatchFields(byte value, int length, boolean hasMask) {
        this.value = value;
        this.lengthInBits = length;
        this.setHasMask(hasMask);
        OFBMatchFields.addMapping(value, this);
    }

    /**
     * 7 left most bit for match field is the value of match field
     *
     * @return Returns the wire protocol value corresponding to this match field
     */
    public byte getValue() {
        return this.value;
    }

    /**
     * get the length of data for match field in bits.
     *
     * @return
     */
    public int getLengthInBits() {
        return lengthInBits;
    }

    /**
     * get the length of data for match field in bytes.
     *
     * @return
     */
    public int getLengthInBytes() {
        int value = lengthInBits / 8;
        value = (lengthInBits % 8 != 0) ? value + 1 : value;

        return value;
    }

    /**
     * Adds a mapping from field value to OXMMatchField enum
     *
     * @param index
     *            OpenFlow wire protocol field index
     * @param field
     *            type
     */
    static void addMapping(byte index, OFBMatchFields field) {
        if (mapping == null) {
            mapping = new OFBMatchFields[40];
        }
        OFBMatchFields.mapping[index] = field;
    }

    /**
     * Remove a mapping from match value to OXMMatchField enum
     *
     * @param index
     *            OpenFlow wire protocol field index
     */
    static void removeMapping(byte i) {
        OFBMatchFields.mapping[i] = null;
    }

    /**
     * Given a wire protocol OpenFlow field number, return the OXMMatchField
     * associated with it
     *
     * @param i
     *            wire protocol field number
     * @return OXMMatchField enum type
     */

    public static OFBMatchFields valueOf(Byte i) {
        return OFBMatchFields.mapping[i];
    }

    /**
     * this field can have has mask
     *
     * @return
     */
    public boolean isHasMask() {
        return hasMask;
    }

    /**
     *
     * @param hasMask
     */
    void setHasMask(boolean hasMask) {
        this.hasMask = hasMask;
    }

}
