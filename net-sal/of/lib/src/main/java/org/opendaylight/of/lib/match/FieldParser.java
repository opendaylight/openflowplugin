/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.MessageParseException;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.util.ResourceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.opendaylight.of.lib.CommonUtils.hex;
import static org.opendaylight.of.lib.CommonUtils.verMin13;
import static org.opendaylight.of.lib.match.FieldFactory.*;

/**
 * Provides facilities for parsing {@link MatchField} instances.
 * <p>
 * Used by the {@link FieldFactory}.
 *
 * @author Simon Hunt
 */
class FieldParser {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            FieldParser.class, "fieldParser");

    private static final String E_UNEX_OXM_CLASS = RES
            .getString("e_unex_oxm_class");
    private static final String E_OFF_BY = RES.getString("e_off_by");

    // No instantiation
    private FieldParser() { }

    private static void verifyTargetRi(int targetRi, OfPacketReader pkt)
            throws MessageParseException {
        if (pkt.ri() != targetRi) {
            int offby = pkt.ri() - targetRi;
            throw FF.mpe(pkt, E_OFF_BY + offby);
        }
    }

    /** Parses a single OXM TLV match field from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the field.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed match field
     * @throws MessageParseException if unable to parse the field
     */
    static MatchField parseField(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        try {
            MatchField.Header header = MatchField.parseHeader(pkt, pv);
            return createFieldInstance(header, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw FF.mpe(pkt, e);
        }
    }

    /** Parses a list of match field header structures from the supplied buffer.
     * This method is provided to support the parsing of an "OXM" table feature
     * property. This list returned contains either {@link MFieldBasicHeader}
     * instances or {@link MFieldExperimenter} instances.
     *
     * @see org.opendaylight.of.lib.msg.TableFeaturePropOxm
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed match fields (header info only)
     * @throws MessageParseException if unable to parse the headers
     */
    public static List<MatchField> parseFieldHeaders(int targetRi,
                                                     OfPacketReader pkt,
                                                     ProtocolVersion pv)
            throws MessageParseException {
        List<MatchField> fieldList = new ArrayList<>();
        while (pkt.ri() < targetRi) {
            MatchField mf = parseFieldHeader(pkt, pv);
            fieldList.add(mf);
        }
        verifyTargetRi(targetRi, pkt);
        return fieldList;
    }

    /** Parses a MatchField structure (header only) from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the structure.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed match field (header info only)
     * @throws MessageParseException if unable to parse the structure
     */
    private static MatchField parseFieldHeader(OfPacketReader pkt,
                                               ProtocolVersion pv)
            throws MessageParseException {
        try {
            MatchField.Header header = MatchField.parseHeader(pkt, pv, true);
            MatchField result;
            switch (header.clazz) {
                case OPENFLOW_BASIC:
                    result = new MFieldBasicHeader(pv, header);
                    break;

                case EXPERIMENTER:
                    result = new MFieldExperimenter(pv, header);
                    // need the experimenter id also
                    ((MFieldExperimenter)result).id = pkt.readInt();
                    break;

                // TODO: Review - should remaining classes be treated as Exper?
//                case NXM_0:
//                case NXM_1:
//                case BIG_SWITCH:
//                case HP:
//                case UNKNOWN:
                default:
                    throw FF.mpe(pkt, E_UNEX_OXM_CLASS + hex(header.rawClazz));
            }
            return result;

        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw FF.mpe(pkt, e);
        }
    }

    /** Uses OXM class and OXM field to instantiate the appropriate concrete
     * match field instance, then continues to parse the remaining payload.
     *
     * @param header the parsed header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a completely parsed match field
     * @throws MessageParseException if there is an issue parsing the field
     */
    private static MatchField createFieldInstance(MatchField.Header header,
                                                  OfPacketReader pkt,
                                                  ProtocolVersion pv)
            throws MessageParseException {
        MatchField result;
        switch (header.clazz) {
            case OPENFLOW_BASIC:
                result = createParsedBasicField(header, pkt, pv);
                break;
            case EXPERIMENTER:
                result = experField(new MFieldExperimenter(pv, header), pkt, pv);
                break;
            default:
                result = minimalField(new MFieldMinimal(pv, header), pkt, pv);
                break;
        }
        return result;
    }

    /** Completes parsing the appropriate OXM basic match field instance.
     *
     * @param header the field header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the fully parsed match field
     * @throws MessageParseException if there is an issue parsing the field
     */
    private static MatchField createParsedBasicField(MatchField.Header header,
                                                     OfPacketReader pkt,
                                                     ProtocolVersion pv)
            throws MessageParseException {
        MFieldBasic field = null;
        switch ((OxmBasicFieldType) header.fieldType) {
            case IN_PORT:
                field = bigPort(new MfbInPort(pv, header), pkt, pv);
                break;
            case IN_PHY_PORT:
                field = bigPort(new MfbInPhyPort(pv, header), pkt, pv);
                break;
            case METADATA:
                field = longf(new MfbMetadata(pv, header), pkt, pv);
                break;
            case ETH_DST:
                field = mac(new MfbEthDst(pv, header), pkt, pv);
                break;
            case ETH_SRC:
                field = mac(new MfbEthSrc(pv, header), pkt, pv);
                break;
            case ETH_TYPE:
                field = ethType(new MfbEthType(pv, header), pkt, pv);
                break;
            case VLAN_VID:
                field = vlanVid(new MfbVlanVid(pv, header), pkt, pv);
                break;
            case VLAN_PCP:
                field = intU8(new MfbVlanPcp(pv, header), pkt, pv,
                        MAX_3_BITS, E_BAD_VAL_VLANP);
                break;
            case IP_DSCP:
                field = intU8(new MfbIpDscp(pv, header), pkt, pv,
                        MAX_6_BITS, E_BAD_VAL_IPDSCP);
                break;
            case IP_ECN:
                field = intU8(new MfbIpEcn(pv, header), pkt, pv,
                        MAX_2_BITS, E_BAD_VAL_IPECN);
                break;
            case IP_PROTO:
                field = ipProto(new MfbIpProto(pv, header), pkt, pv);
                break;
            case IPV4_SRC:
                field = ipv4(new MfbIpv4Src(pv, header), pkt, pv);
                break;
            case IPV4_DST:
                field = ipv4(new MfbIpv4Dst(pv, header), pkt, pv);
                break;
            case TCP_SRC:
                field = port(new MfbTcpSrc(pv, header), pkt, pv);
                break;
            case TCP_DST:
                field = port(new MfbTcpDst(pv, header), pkt, pv);
                break;
            case UDP_SRC:
                field = port(new MfbUdpSrc(pv, header), pkt, pv);
                break;
            case UDP_DST:
                field = port(new MfbUdpDst(pv, header), pkt, pv);
                break;
            case SCTP_SRC:
                field = port(new MfbSctpSrc(pv, header), pkt, pv);
                break;
            case SCTP_DST:
                field = port(new MfbSctpDst(pv, header), pkt, pv);
                break;
            case ICMPV4_TYPE:
                field = icmpv4Type(new MfbIcmpv4Type(pv, header), pkt, pv);
                break;
            case ICMPV4_CODE:
                field = intU8(new MfbIcmpv4Code(pv, header), pkt, pv);
                break;
            case ARP_OP:
                field = intU16(new MfbArpOp(pv, header), pkt, pv);
                break;
            case ARP_SPA:
                field = ipv4(new MfbArpSpa(pv, header), pkt, pv);
                break;
            case ARP_TPA:
                field = ipv4(new MfbArpTpa(pv, header), pkt, pv);
                break;
            case ARP_SHA:
                field = mac(new MfbArpSha(pv, header), pkt, pv);
                break;
            case ARP_THA:
                field = mac(new MfbArpTha(pv, header), pkt, pv);
                break;
            case IPV6_SRC:
                field = ipv6(new MfbIpv6Src(pv, header), pkt, pv);
                break;
            case IPV6_DST:
                field = ipv6(new MfbIpv6Dst(pv, header), pkt, pv);
                break;
            case IPV6_FLABEL:
                // TODO: Review - should this be U32 or U24?
                field = intU24(new MfbIpv6Flabel(pv, header), pkt, pv,
                        MAX_20_BITS, E_BAD_VAL_FLABEL);
                break;
            case ICMPV6_TYPE:
                field = icmpv6Type(new MfbIcmpv6Type(pv, header), pkt, pv);
                break;
            case ICMPV6_CODE:
                field = intU8(new MfbIcmpv6Code(pv, header), pkt, pv);
                break;
            case IPV6_ND_TARGET:
                field = ipv6(new MfbIpv6NdTarget(pv, header), pkt, pv);
                break;
            case IPV6_ND_SLL:
                field = mac(new MfbIpv6NdSll(pv, header), pkt, pv);
                break;
            case IPV6_ND_TLL:
                field = mac(new MfbIpv6NdTll(pv, header), pkt, pv);
                break;
            case MPLS_LABEL:
                // TODO: Review - should this be U32 or U24?
                field = FieldFactory._MPLS_LABEL_ENCODE_IN_4_BYTES
                    ? intU32(new MfbMplsLabel(pv, header), pkt, pv,
                        MAX_20_BITS, E_BAD_VAL_MPLSLABEL)
                    : intU24(new MfbMplsLabel(pv, header), pkt, pv,
                        MAX_20_BITS, E_BAD_VAL_MPLSLABEL);
                break;
            case MPLS_TC:
                field = intU8(new MfbMplsTc(pv, header), pkt, pv,
                        MAX_3_BITS, E_BAD_VAL_MPLSTC);
                break;
            case MPLS_BOS:
                verMin13(pv, "MPLS BoS");
                field = intU8(new MfbMplsBos(pv, header), pkt, pv,
                        MAX_1_BIT, E_BAD_VAL_MPLSBOS);
                break;
            case PBB_ISID:
                verMin13(pv, "PBB I-SID");
                // TODO: Review - should this be U32 or U24?
                field = intU24(new MfbPbbIsid(pv, header), pkt, pv);
                break;
            case TUNNEL_ID:
                verMin13(pv, "Tunnel ID");
                field = longf(new MfbTunnelId(pv, header), pkt, pv);
                break;
            case IPV6_EXTHDR:
                verMin13(pv, "IPv6 ExtHdr");
                field = ipv6Exthdr(new MfbIpv6Exthdr(pv, header), pkt, pv);
                break;
        }
        return field;
    }

    // =======================================================================
    // Common parsers

    /** Completes parsing a big-port-number-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic bigPort(MFieldBasicBigPort field,
                                       OfPacketReader pkt,
                                       ProtocolVersion pv) {
        field.port = pkt.readBigPortNumber();
        return field;
    }

    /** Completes parsing a port-number-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic port(MFieldBasicPort field,
                                    OfPacketReader pkt,
                                    ProtocolVersion pv) {
        field.port = pkt.readPortNumber();
        return field;
    }

    /** Completes parsing a mac-address-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic mac(MFieldBasicMac field,
                                   OfPacketReader pkt,
                                   ProtocolVersion pv) {
        field.mac = pkt.readMacAddress();
        if (field.header.hasMask)
            field.mask = pkt.readMacAddress();
        return field;
    }

    /** Completes parsing an ipv4-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic ipv4(MFieldBasicIp field,
                                    OfPacketReader pkt,
                                    ProtocolVersion pv) {
        field.ip = pkt.readIPv4Address();
        if (field.header.hasMask)
            field.mask = pkt.readIPv4Address();
        return field;
    }

    /** Completes parsing an ipv6-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic ipv6(MFieldBasicIp field,
                                    OfPacketReader pkt,
                                    ProtocolVersion pv) {
        field.ip = pkt.readIPv6Address();
        if (field.header.hasMask)
            field.mask = pkt.readIPv6Address();
        return field;
    }

    /** Completes parsing an int-u8-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic intU8(MFieldBasicInt field,
                                     OfPacketReader pkt,
                                     ProtocolVersion pv) {
        field.value = pkt.readU8();
        return field;
    }

    /** Completes parsing an int-u8-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @param fieldMax maximum value
     * @param errMsg error message to be used
     * @return field element descriptor
     * @throws MessageParseException if match field cannot be parsed
     */
    private static MFieldBasic intU8(MFieldBasicInt field,
                                     OfPacketReader pkt,
                                     ProtocolVersion pv,
                                     int fieldMax,
                                     String errMsg)
            throws MessageParseException {
        field.value = pkt.readU8();

        if (field.value > fieldMax)
            throw FF.mpe(pkt, errMsg + " bad value: " + field.value);

        return field;
    }

    /** Completes parsing an int-u16-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic intU16(MFieldBasicInt field,
                                      OfPacketReader pkt,
                                      ProtocolVersion pv) {
        field.value = pkt.readU16();
        return field;
    }

    /** Completes parsing an int-u24-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic intU24(MFieldBasicInt field,
                                      OfPacketReader pkt,
                                      ProtocolVersion pv) {
        field.value = pkt.readU24();
        if (field.header.hasMask)
            field.mask = pkt.readU24();
        return field;
    }

    /** Completes parsing an int-u24-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @param maxValue maximum value
     * @param errMsg error message to be used
     * @return field element descriptor
     * @throws MessageParseException if match field cannot be parsed
     */
    private static MFieldBasic intU24(MFieldBasicInt field,
                                      OfPacketReader pkt,
                                      ProtocolVersion pv,
                                      int maxValue, String errMsg)
            throws MessageParseException {
        field.value = pkt.readU24();
        if (field.header.hasMask)
            field.mask = pkt.readU24();

        if (field.value > maxValue)
            throw FF.mpe(pkt, errMsg + " bad value: " + field.value);
        if (field.header.hasMask && field.mask > maxValue)
            throw FF.mpe(pkt, errMsg + " bad mask: " + field.mask);

        return field;
    }

    /** Completes parsing an int-u32-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @param maxValue maximum value
     * @param errMsg error message to be used
     * @return field element descriptor
     * @throws MessageParseException if match field cannot be parsed
     */
    private static MFieldBasic intU32(MFieldBasicInt field,
                                      OfPacketReader pkt,
                                      ProtocolVersion pv,
                                      int maxValue, String errMsg)
            throws MessageParseException {
        field.value = (int) pkt.readU32();
        if (field.header.hasMask)
            field.mask = (int) pkt.readU32();

        if (field.value > maxValue)
            throw FF.mpe(pkt, errMsg + " bad value: " + field.value);
        if (field.header.hasMask && field.mask > maxValue)
            throw FF.mpe(pkt, errMsg + " bad mask: " + field.mask);

        return field;
    }

    /** Completes parsing a long-based match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic longf(MFieldBasicLong field,
                                     OfPacketReader pkt,
                                     ProtocolVersion pv) {
        field.value = pkt.readLong();
        if (field.header.hasMask)
            field.mask = pkt.readLong();
        return field;
    }

    // =======================================================================
    // Unique parsers

    /** Completes parsing an ETH_TYPE match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic ethType(MfbEthType field,
                                       OfPacketReader pkt,
                                       ProtocolVersion pv) {
        field.ethType = pkt.readEthernetType();
        return field;
    }

    /** Completes parsing a VLAN_VID match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     * @throws MessageParseException if match field cannot be parsed
     */
    private static MFieldBasic vlanVid(MfbVlanVid field,
                                       OfPacketReader pkt,
                                       ProtocolVersion pv)
            throws MessageParseException {
        VId value = pkt.readVId();
        VId mask = null;
        if (field.header.hasMask)
            mask = pkt.readVId();

        // validate constraints of VLAN_VID match
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
         */

        // first, check value. if it is not zero (NONE flag), then the
        //  present bit MUST be set.
        final int valInt = value.toInt();
        if (valInt > 0 && (valInt & PRESENT.toInt()) == 0)
            throw FF.mpe(pkt, E_VLAN_NO_PRESENT);

        // TODO: check none of top 3 bits are set if factory in STRICT mode

        // having got that out of the way,
        // if mask is present, it and the value both need to be 0x1000.
        if (mask != null) {
            if (!(mask.equals(PRESENT) && value.equals(PRESENT)))
                throw FF.mpe(pkt, E_VLAN_BAD_PRESENT);
            field.vid = VId.PRESENT;
        } else {
            // explicitly NONE match, or EXACT match?
            if (value.equals(NONE)) {
                field.vid = VId.NONE;
            } else {
                // strip off the "present" bit...
                field.vid = VId.valueOf(valInt & TWELVE_BITS);
            }
        }
        return field;
    }

    private static final int TWELVE_BITS = 0xfff;

    /** Completes parsing an IP_PROTO match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic ipProto(MfbIpProto field,
                                       OfPacketReader pkt,
                                       ProtocolVersion pv) {
        field.ipp = pkt.readIpProtocol();
        return field;
    }


    /** Completes parsing an ICMPV4_TYPE match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic icmpv4Type(MfbIcmpv4Type field,
                                          OfPacketReader pkt,
                                          ProtocolVersion pv) {
        field.type = pkt.readIcmpv4Type();
        return field;
    }

    /** Completes parsing an ICMPV6_TYPE match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic icmpv6Type(MfbIcmpv6Type field,
                                          OfPacketReader pkt,
                                          ProtocolVersion pv) {
        field.type = pkt.readIcmpv6Type();
        return field;
    }


    /** Completes parsing a IPV6_EXTHDR match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MFieldBasic ipv6Exthdr(MfbIpv6Exthdr field,
                                          OfPacketReader pkt,
                                          ProtocolVersion pv) {
        field.rawBits = pkt.readU16();
        if (field.header.hasMask)
            field.mask = pkt.readU16();

        // TODO: review- throw exception if any of upper 7 bits are non-zero?
        return field;
    }

    // =======================================================================
    // Non-Basic match fields

    /** Completes parsing an OXM experimenter match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MatchField experField(MFieldExperimenter field,
                                         OfPacketReader pkt,
                                         ProtocolVersion pv) {
        field.id = pkt.readInt();
        field.payload = pkt.readBytes(field.header.length - EXP_ID_LEN);
        return field;
    }

    /** Completes parsing a minimal match field.
     *
     * @param field match field
     * @param pkt packet reader
     * @param pv protocol version
     * @return field element descriptor
     */
    private static MatchField minimalField(MFieldMinimal field,
                                           OfPacketReader pkt,
                                           ProtocolVersion pv) {
        field.payload = pkt.readBytes(field.header.length);
        return field;
    }
}