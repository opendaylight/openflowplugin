/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.of.lib.dt.VId;

/**
 * Provides facilities for encoding {@link MatchField} instances.
 * <p>
 * Used by the {@link FieldFactory}.
 *
 * @author Simon Hunt
 */
class FieldEncoder {

    /** Encodes a single OXM TLV match field, writing it into the supplied
     * buffer. Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the field.
     *
     * @param mf the match field to encode
     * @param pkt the buffer into which the match field is to be written
     */
    static void encodeField(MatchField mf, OfPacketWriter pkt) {
        // First, write out the header..
        int fldAndHasMask = mf.header.rawFieldType << 1;
        if (mf.header.hasMask)
            fldAndHasMask++;
        pkt.writeU16(mf.header.rawClazz);
        pkt.writeU8(fldAndHasMask);
        pkt.writeU8(mf.header.length);

        // if the field is header-only, we are done.
        if (MFieldBasicHeader.class.isInstance(mf))
            return;

        // now deal with the payload, based on the class
        switch (mf.header.clazz) {
            case OPENFLOW_BASIC:
                encodeBasicField(mf, pkt);
                break;
            case EXPERIMENTER:
                encodeExperimenter((MFieldExperimenter) mf, pkt);
                break;
            // everything else is given the minimal treatment
            default:
                encodeMinimal((MFieldMinimal) mf, pkt);
                break;
        }
    }

    /** Encodes the payload portion of the given field.
     *
     * @param mf the match field
     * @param pkt the buffer to write into
     */
    private static void encodeBasicField(MatchField mf, OfPacketWriter pkt) {
        switch ((OxmBasicFieldType) mf.getFieldType()) {

            case IN_PORT:
            case IN_PHY_PORT:
                encodeBigPort((MFieldBasicBigPort) mf, pkt);
                break;

            case METADATA:
            case TUNNEL_ID:
                encodeLong((MFieldBasicLong) mf, pkt);
                break;

            case ETH_DST:
            case ETH_SRC:
            case ARP_SHA:
            case ARP_THA:
            case IPV6_ND_SLL:
            case IPV6_ND_TLL:
                encodeMac((MFieldBasicMac) mf, pkt);
                break;

            case ETH_TYPE:
                encodeEthType((MfbEthType) mf, pkt);
                break;

            case VLAN_VID:
                encodeVlanVid((MfbVlanVid) mf, pkt);
                break;

            case VLAN_PCP:
            case IP_DSCP:
            case IP_ECN:
            case ICMPV4_CODE:
            case ICMPV6_CODE:
            case MPLS_TC:
            case MPLS_BOS:
                encodeIntU8((MFieldBasicInt) mf, pkt);
                break;

            case IP_PROTO:
                encodeIpProto((MfbIpProto) mf, pkt);
                break;

            case IPV4_SRC:
            case IPV4_DST:
            case ARP_SPA:
            case ARP_TPA:
            case IPV6_SRC:
            case IPV6_DST:
            case IPV6_ND_TARGET:
                encodeIp((MFieldBasicIp) mf, pkt);
                break;

            case TCP_SRC:
            case TCP_DST:
            case UDP_SRC:
            case UDP_DST:
            case SCTP_SRC:
            case SCTP_DST:
                encodePort((MFieldBasicPort) mf, pkt);
                break;

            case ICMPV4_TYPE:
                encodeIcmpv4Type((MfbIcmpv4Type) mf, pkt);
                break;

            case ARP_OP:
                encodeIntU16((MFieldBasicInt) mf, pkt);
                break;

            case MPLS_LABEL:
                if (FieldFactory._MPLS_LABEL_ENCODE_IN_4_BYTES)
                    encodeIntU32((MFieldBasicInt) mf, pkt);
                else
                    encodeIntU24((MFieldBasicInt) mf, pkt);
                break;

            case IPV6_FLABEL:
            case PBB_ISID:
                encodeIntU24((MFieldBasicInt) mf, pkt);
                break;

            case ICMPV6_TYPE:
                encodeIcmpv6Type((MfbIcmpv6Type) mf, pkt);
                break;

            case IPV6_EXTHDR:
                encodeIpv6Exthdr((MfbIpv6Exthdr) mf, pkt);
                break;
        }
    }


    // =======================================================================
    // Common encoders


    // encodes a big-port-number-based match field
    private static void encodeBigPort(MFieldBasicBigPort mf, OfPacketWriter pkt) {
        pkt.write(mf.port);
    }

    // encodes a port-number-based match field
    private static void encodePort(MFieldBasicPort mf, OfPacketWriter pkt) {
        pkt.write(mf.getPort());
    }

    // encodes a mac-address-based match field
    private static void encodeMac(MFieldBasicMac mf, OfPacketWriter pkt) {
        pkt.write(mf.getMacAddress());
        if (mf.hasMask())
            pkt.write(mf.getMask());
    }

    // encodes an ip-address-based match field
    private static void encodeIp(MFieldBasicIp mf, OfPacketWriter pkt) {
        pkt.write(mf.ip);
        if (mf.hasMask())
            pkt.write(mf.mask);
    }

    // encodes a U8-based match field
    private static void encodeIntU8(MFieldBasicInt mf, OfPacketWriter pkt) {
        // ASSUMPTION: value is already within bounds.
        pkt.writeU8(mf.getValue());
    }

    // encodes a U16-based match field
    private static void encodeIntU16(MFieldBasicInt mf, OfPacketWriter pkt) {
        // ASSUMPTION: value is already within bounds.
        pkt.writeU16(mf.getValue());
    }

    // encodes a U24-based match field
    private static void encodeIntU24(MFieldBasicInt mf, OfPacketWriter pkt) {
        // ASSUMPTION: value is already within bounds.
        pkt.writeU24(mf.getValue());
        if (mf.hasMask())
            pkt.writeU24(mf.getMask());
    }

    // encodes a U32-based match field
    private static void encodeIntU32(MFieldBasicInt mf, OfPacketWriter pkt) {
        // ASSUMPTION: value is already within bounds.
        pkt.writeU32(mf.getValue());
        if (mf.hasMask())
            pkt.writeU32(mf.getMask());
    }

    // encodes a long-based match field
    private static void encodeLong(MFieldBasicLong mf, OfPacketWriter pkt) {
        pkt.writeLong(mf.getValue());
        if (mf.hasMask())
            pkt.writeLong(mf.getMask());
    }


    // =======================================================================
    // Unique encoders

    // encodes an ETH_TYPE match field
    private static void encodeEthType(MfbEthType mf, OfPacketWriter pkt) {
        pkt.write(mf.ethType);
    }

    // encodes a VLAN_VID match field
    private static void encodeVlanVid(MfbVlanVid mf, OfPacketWriter pkt) {

        // Constraints of VLAN_VID match
        // (see Table 12 (p.45) of 1.3 spec)
        /*
         * +-----------+---------+---------+-----------------------------------
         * | OXM Field | value*  |  mask*  |  Matching Packets
         * +-----------+---------+---------+-----------------------------------
         * |  absent   |    -    |    -    | Pkts WITH and WITHOUT a VLAN tag
         * |  present  |  NONE   | absent  | Only packets WITHOUT a VLAN tag
         * |  present  | PRESENT | PRESENT | Only packets WITH tag, any value
         * |  present  | val|PRS | absent  | Only packets WITH tag, EQ to val
         * +-----------+---------+---------+-----------------------------------
         *   * PRESENT = 0x1000 (bit indicating VLAN id is set)
         *   * NONE = 0x0000 (no VLAN id was set)
         * ---
         * Determine the proper encoding based on whether vid is VId.NONE,
         * VId.PRESENT, or a regular VId with a 12-bit value.
         */
        VId vid = mf.vid;
        int value;
        boolean hasMask = false;
        if (vid.equals(VId.NONE)) {
            value = OxmVlanId.NONE.getValue();
        } else if (vid.equals(VId.PRESENT)) {
            value = OxmVlanId.PRESENT.getValue();
            hasMask = true;
        } else {
            value = OxmVlanId.PRESENT.getValue() | vid.toInt();
        }
        pkt.writeU16(value);
        if (hasMask)
            pkt.writeU16(value);
    }

    // encodes an IP_PROTO match field.
    private static void encodeIpProto(MfbIpProto mf, OfPacketWriter pkt) {
        pkt.write(mf.ipp);
    }

    // encodes an ICMPV4_TYPE match field.
    private static void encodeIcmpv4Type(MfbIcmpv4Type mf, OfPacketWriter pkt) {
        pkt.write(mf.type);
    }

    // encodes an ICMPV6_TYPE match field.
    private static void encodeIcmpv6Type(MfbIcmpv6Type mf, OfPacketWriter pkt) {
        pkt.write(mf.type);
    }

    // encodes an IPV6_EXTHDR match field.
    private static void encodeIpv6Exthdr(MfbIpv6Exthdr mf, OfPacketWriter pkt) {
        pkt.writeU16(mf.rawBits);
        if (mf.hasMask())
            pkt.writeU16(mf.mask);
    }



    // =======================================================================
    // Non-Basic match fields

    // encodes an OXM experimenter match field
    private static void encodeExperimenter(MFieldExperimenter mf,
                                           OfPacketWriter pkt) {
        pkt.writeInt(mf.getId());
        byte[] data = mf.getPayload();
        if (data != null)
            pkt.writeBytes(data);
    }

    // encodes a minimal match field
    private static void encodeMinimal(MFieldMinimal mf, OfPacketWriter pkt) {
        pkt.writeBytes(mf.getPayload());
    }
}
