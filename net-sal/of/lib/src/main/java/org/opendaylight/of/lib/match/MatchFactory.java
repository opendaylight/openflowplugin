/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.dt.VId;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.PortFactory;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.*;

import java.util.ResourceBundle;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.E_DEPRECATED;
import static org.opendaylight.of.lib.CommonUtils.notMutable;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;

/**
 * Provides facilities for parsing, creating and encoding {@link Match}
 * instances.
 *
 * @author Simon Hunt
 */
public class MatchFactory extends AbstractFactory {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MatchFactory.class, "matchFactory");

    private static final String E_UNEX_STANDARD_FIELD_TYPE = RES
            .getString("e_unex_standard_field_type");
    private static final String E_UNEX_MATCH_TYPE = RES
            .getString("e_unex_match_type");

    /** The length of a match header in bytes. */
    static final int MATCH_HEADER_LEN = 4;
    /** The length of a match field header in bytes. */
    static final int FIELD_HEADER_LEN = 4;

    /** Length of a 1.0 match structure. */
    static final int STANDARD_LENGTH_10 = 40;
    /** Length of a 1.1 match structure. */
    static final int STANDARD_LENGTH_11 = 88;

    private static final MatchFactory MF = new MatchFactory();

    // No instantiation except here
    private MatchFactory() { }

    /** Returns an identifying tag for the match factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "MF";
    }

    //======================================================================
    // === Parsing Matches

    /** Parses a Match structure from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the structure
     * (including the padding).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed match structure
     * @throws MessageParseException if unable to parse the structure
     */
    public static Match parseMatch(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {

        try {
            // have to do some chicanery if it is the old-style match structure
            if (pv.lt(V_1_2))
                return fabricateMatch(pkt, pv);

            Match.Header header = Match.parseHeader(pkt, pv);
            return createParsedMatchInstance(header, pkt, pv);
        } catch (MessageParseException mpe) {
            // rethrow MPE
            throw mpe;
        } catch (Exception e) {
            // wrap any unexpected exception in an MPE
            throw MF.mpe(pkt, e);
        }
    }

    /** Parses a 1.0 or 1.1 match structure, but codifies it as an
     * OXM TLV structure (with the V_1_0 or V_1_1 version tag).
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed match structure
     * @throws MessageParseException if unable to parse the structure
     * @throws DecodeException if the match type cannot be decoded
     */
    private static Match fabricateMatch(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException, DecodeException {
        // start by parsing the data buffer, to create a DTO
        OldMatch old = readOldMatch(pkt, pv);
        Match.Header hdr = new Match.Header();
        hdr.type = MatchType.decode(old.type, pv);
        hdr.length = old.length;
        MutableMatch mm = new MutableMatch(pv, hdr);

        /* Fabricate TLVs from old format data (see OldMatch fields).
         * Remember that an all-wild match field means that the TLV
         * is omitted from the structure.
         */
        Set<Wildcard> wild = old.wildcards;
        // figure out what is in the nwProto field
        IpProtocol ipp = null;
        int arpOp = 0;
        // TODO: Review - should we take DL_TYPE wild flag into account?
        if (old.dlType == EthernetType.ARP)
            arpOp = old.nwProto;
        else
            ipp = IpProtocol.valueOf(old.nwProto);

        if (!wild.contains(Wildcard.IN_PORT))
            mm.addField(createBasicField(pv,
                    OxmBasicFieldType.IN_PORT, old.inPort));
        if (!wild.contains(Wildcard.DL_SRC) &&
                !Wildcard.ALL_WILD_MAC_MASK.equals(old.dlSrcMask))
            mm.addField(createBasicField(pv, OxmBasicFieldType.ETH_SRC,
                    old.dlSrc, old.dlSrcMask));
        if (!wild.contains(Wildcard.DL_DST) &&
                !Wildcard.ALL_WILD_MAC_MASK.equals(old.dlDstMask))
            mm.addField(createBasicField(pv, OxmBasicFieldType.ETH_DST,
                    old.dlDst, old.dlDstMask));
        if (!wild.contains(Wildcard.DL_VLAN))  // note: not maskable in 1.0/1.1
            mm.addField(createBasicField(pv, OxmBasicFieldType.VLAN_VID,
                    equivVlanId(old.dlVlan)));
        if (!wild.contains(Wildcard.DL_VLAN_PCP))
            mm.addField(createBasicField(pv, OxmBasicFieldType.VLAN_PCP,
                    old.dlVlanPcp));
        if (!wild.contains(Wildcard.DL_TYPE))
            mm.addField(createBasicField(pv, OxmBasicFieldType.ETH_TYPE,
                    old.dlType));
        if (!wild.contains(Wildcard.NW_TOS))
            mm.addField(createBasicField(pv, OxmBasicFieldType.IP_DSCP,
                    old.nwTos)); // TODO: Verify

        // nwProto field processing // TODO: Verify
        if (arpOp != 0)
            mm.addField(createBasicField(pv, OxmBasicFieldType.ARP_OP, arpOp));
        if (ipp != null && !wild.contains(Wildcard.NW_PROTO))
            mm.addField(createBasicField(pv, OxmBasicFieldType.IP_PROTO, ipp));

        if (!old.nwSrcMask.equals(Wildcard.ALL_WILD_IP_MASK))
            mm.addField(createBasicField(pv, OxmBasicFieldType.IPV4_SRC,
                    old.nwSrc, old.nwSrcMask));
        if (!old.nwDstMask.equals(Wildcard.ALL_WILD_IP_MASK))
            mm.addField(createBasicField(pv, OxmBasicFieldType.IPV4_DST,
                    old.nwDst, old.nwDstMask));
        if (!wild.contains(Wildcard.TP_SRC)) {
            if (ipp == IpProtocol.TCP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.TCP_SRC,
                        old.tpSrc));
            else if (ipp == IpProtocol.UDP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.UDP_SRC,
                        old.tpSrc));
            else if (ipp == IpProtocol.SCTP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.SCTP_SRC,
                        old.tpSrc));
            else if (ipp == IpProtocol.ICMP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.ICMPV4_TYPE,
                        ICMPv4Type.valueOf(old.tpSrc.toInt())));
        }
        if (!wild.contains(Wildcard.TP_DST)) {
            if (ipp == IpProtocol.TCP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.TCP_DST,
                        old.tpDst));
            else if (ipp == IpProtocol.UDP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.UDP_DST,
                        old.tpDst));
            else if (ipp == IpProtocol.SCTP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.SCTP_DST,
                        old.tpDst));
            else if (ipp == IpProtocol.ICMP)
                mm.addField(createBasicField(pv, OxmBasicFieldType.ICMPV4_CODE,
                        old.tpDst.toInt()));
        }

        // 1.1 matching only
        if (pv == V_1_1) {
            if (!wild.contains(Wildcard.MPLS_LABEL))
                mm.addField(createBasicField(pv, OxmBasicFieldType.MPLS_LABEL,
                        old.mplsLabel));
            if (!wild.contains(Wildcard.MPLS_TC))
                mm.addField(createBasicField(pv, OxmBasicFieldType.MPLS_TC,
                        old.mplsTc));
            if (old.metadataMask != Wildcard.ALL_WILD_METADATA_MASK)
                mm.addField(createBasicField(pv, OxmBasicFieldType.METADATA,
                        old.metadata, old.metadataMask));
        }

        return (Match) mm.toImmutable();
    }

    /**
     * Returns the VId instance equivalent to the specified Vlan ID.
     *
     * @param vlanId the vlan ID
     * @return the corresponding VId
     * @throws NullPointerException if vlanId is null
     */
    public static VId equivVid(VlanId vlanId) {
        if (vlanId.equals(VlanId.NONE))
            return VId.NONE;
        if (vlanId.equals(VlanId.PRESENT))
            return VId.PRESENT;
        return VId.valueOf(vlanId.toInt());
    }

    /**
     * Returns the VlanId instance equivalent to the specified VId.
     *
     * @param vid the VId
     * @return the equivalent vlan ID
     * @throws NullPointerException if vid is null
     * @throws IllegalArgumentException if vid is out of range (not u12)
     */
    public static VlanId equivVlanId(VId vid) {
        if (vid.equals(VId.NONE))
            return VlanId.NONE;
        if (vid.equals(VId.PRESENT))
            return VlanId.PRESENT;
        if ((vid.toInt() & ~TWELVE_BITS) > 0)
            throw new IllegalArgumentException(E_NOT_U12 + vid.toInt());
        return VlanId.valueOf(vid.toInt());
    }

    private static final String E_NOT_U12 = "Not u12: ";

    static final int TWELVE_BITS = 0xfff;

    static final int PAD_OLD_MATCH_1 = 1;
    static final int PAD_OLD_MATCH_2 = 2;
    static final int PAD_OLD_MATCH_3 = 3;

    /** Parses an old-style match structure.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return an old-match DTO with the parsed data
     */
    private static OldMatch readOldMatch(OfPacketReader pkt,
                                         ProtocolVersion pv) {
        if (pv.gt(V_1_1))
            throw new VersionMismatchException(E_DEPRECATED + V_1_2);
        OldMatch om = new OldMatch(pv);
        if (pv == V_1_1) {
            om.type = pkt.readU16();
            om.length = pkt.readU16();
            om.inPort = PortFactory.parsePortNumber(pkt, pv);
            om.wildcards = Wildcard.decodeBitmap(pkt.readInt(), pv);
        } else {
            om.type = MatchType.STANDARD.getCode(pv);
            om.length = STANDARD_LENGTH_10;
            int wildBits = pkt.readInt();
            om.wildcards = Wildcard.decodeV10Bitmap(wildBits, pv);
            om.nwSrcMask = Wildcard.decodeNetmask(Wildcard.NW_SRC, wildBits, pv);
            om.nwDstMask = Wildcard.decodeNetmask(Wildcard.NW_DST, wildBits, pv);
            om.inPort = PortFactory.parsePortNumber(pkt, pv);
        }
        om.dlSrc = pkt.readMacAddress();
        if (pv == V_1_1)
            om.dlSrcMask = pkt.readMacAddress();
        om.dlDst = pkt.readMacAddress();
        if (pv == V_1_1)
            om.dlDstMask = pkt.readMacAddress();
        om.dlVlan = pkt.readVId();
        om.dlVlanPcp = pkt.readU8();
        pkt.skip(PAD_OLD_MATCH_1);
        om.dlType = pkt.readEthernetType();
        om.nwTos = pkt.readU8();
        om.nwProto = pkt.readU8();
        if (pv == V_1_0)
            pkt.skip(PAD_OLD_MATCH_2);
        om.nwSrc = pkt.readIPv4Address();
        if (pv == V_1_1)
            om.nwSrcMask = pkt.readIPv4Address();
        om.nwDst = pkt.readIPv4Address();
        if (pv == V_1_1)
            om.nwDstMask = pkt.readIPv4Address();
        om.tpSrc = pkt.readPortNumber();
        om.tpDst = pkt.readPortNumber();
        if (pv == V_1_1) {
            om.mplsLabel = pkt.readInt();
            om.mplsTc = pkt.readU8();
            pkt.skip(PAD_OLD_MATCH_3);
            om.metadata = pkt.readLong();
            om.metadataMask = pkt.readLong();
        }
        return om;
    }

    /** Uses Match header to instantiate the appropriate class of
     * Match structure.
     *
     * @param header the header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the Match structure
     * @throws MessageParseException if there is an issue parsing the match
     */
    private static Match createParsedMatchInstance(Match.Header header,
                                                   OfPacketReader pkt,
                                                   ProtocolVersion pv)
            throws MessageParseException {
        switch (header.type) {
            case OXM:
                return createParsedOxmMatch(header, pkt, pv);
            default:
                throw new IllegalStateException(pv + E_UNEX_MATCH_TYPE +
                        header.type);
        }
    }

    /** Instantiates the appropriate OXM match instance.
     *
     * @param header the match header
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return the fully parsed match structure
     * @throws MessageParseException if there is an issue parsing the match
     */
    private static Match createParsedOxmMatch(Match.Header header,
                                              OfPacketReader pkt,
                                              ProtocolVersion pv)
            throws MessageParseException {
        Match match = new Match(pv, header);
        int bytesLeft = header.length - MATCH_HEADER_LEN;
        while (bytesLeft > 0) {
            MatchField mf = FieldFactory.parseField(pkt, pv);
            match.fields.add(mf);
            bytesLeft -= (FIELD_HEADER_LEN + mf.header.length);
        }
        if (bytesLeft != 0)
            throw MF.mpe(pkt, "bad Match len (too small): " +
            header.length + " (bytes left: " + bytesLeft + ")");
        pkt.skip(calcPadding(header.length));
        return match;
    }


    //======================================================================
    // === Creating Matches

    /** Creates a mutable match instance.
     *
     * @param pv the protocol version
     * @return a mutable match
     * @throws VersionNotSupportedException if the version is not supported
     */
    public static MutableMatch createMatch(ProtocolVersion pv) {
        MessageFactory.checkVersionSupported(pv);
        Match.Header hdr = new Match.Header();
        if (pv.lt(V_1_2)) {
            hdr.type = MatchType.STANDARD;
            hdr.length = pv == V_1_0 ? STANDARD_LENGTH_10 : STANDARD_LENGTH_11;
        } else {
            hdr.type = MatchType.OXM;
            hdr.length = MATCH_HEADER_LEN;
        }
        return new MutableMatch(pv, hdr);
    }

    //======================================================================
    // === Encoding Matches

    /** Encodes a match, writing it into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the match.
     *
     * @param match the match
     * @param pkt the buffer into which the match is to be written
     */
    public static void encodeMatch(Match match, OfPacketWriter pkt) {
        notMutable(match);
        final ProtocolVersion pv = match.getVersion();
        if (pv.lt(V_1_2)) {
            encodeOldMatch(match, pkt);
            return;
        }

        int wi = pkt.wi();
        // first, write out the header
        pkt.writeU16(match.header.type.getCode(pv));
        pkt.writeU16(match.header.length);
        // now output the list of match fields
        for (MatchField mf: match.fields)
            FieldFactory.encodeField(mf, pkt);
        // finally, we have to pad out to a 64-bit boundary
        int bytesWritten = pkt.wi() - wi;
        int zeroFill = (bytesWritten+7)/8*8 - bytesWritten;
        pkt.writeZeros(zeroFill);
    }

    /** Encodes a 1.0 or 1.1 match structure, converting the OXM TLV structure
     * to the fixed format of the legacy match.
     *
     * @param match the match to encode
     * @param pkt the buffer into which the match is to be written
     */
    private static void encodeOldMatch(Match match, OfPacketWriter pkt) {
        final ProtocolVersion pv = match.getVersion();
        OldMatch old = convertToOldMatch(match);
        if (pv == V_1_1) {
            pkt.writeU16(old.type);
            pkt.writeU16(old.length);
            PortFactory.encodePortNumber(old.inPort, pkt, pv);
            pkt.writeInt(Wildcard.encodeBitmap(old.wildcards, pv));
        } else {
            pkt.writeInt(Wildcard.encodeV10Bitmap(old.wildcards,
                                        old.nwSrcMask, old.nwDstMask, pv));
            PortFactory.encodePortNumber(old.inPort, pkt, pv);
        }

        if (old.dlSrc == null)
            pkt.writeZeros(MacAddress.MAC_ADDR_SIZE);
        else
            pkt.write(old.dlSrc);
        if (pv == V_1_1) {
            if (old.dlSrcMask == null)
                pkt.writeZeros(MacAddress.MAC_ADDR_SIZE);
            else
                pkt.write(old.dlSrcMask);
        }
        if (old.dlDst == null)
            pkt.writeZeros(MacAddress.MAC_ADDR_SIZE);
        else
            pkt.write(old.dlDst);
        if (pv == V_1_1) {
            if (old.dlDstMask == null)
                pkt.writeZeros(MacAddress.MAC_ADDR_SIZE);
            else
                pkt.write(old.dlDstMask);
        }

        if (old.dlVlan == null)
            pkt.writeZeros(VId.LENGTH_IN_BYTES);
        else
            pkt.write(old.dlVlan);
        pkt.writeU8(old.dlVlanPcp);
        pkt.writeZeros(PAD_OLD_MATCH_1);
        if (old.dlType == null)
            pkt.writeZeros(EthernetType.LENGTH_IN_BYTES);
        else
            pkt.write(old.dlType);
        pkt.writeU8(old.nwTos);
        pkt.writeU8(old.nwProto); // could also be ArpOp lower-8 bits
        pkt.writeZeros(PAD_OLD_MATCH_2);

        if (old.nwSrc == null)
            pkt.writeZeros(IpAddress.IP_V4_ADDR_SIZE);
        else
            pkt.write(old.nwSrc);
        if (pv == V_1_1) {
            if (old.nwSrcMask == null)
                pkt.writeZeros(IpAddress.IP_V4_ADDR_SIZE);
            else
                pkt.write(old.nwSrcMask);
        }
        if (old.nwDst == null)
            pkt.writeZeros(IpAddress.IP_V4_ADDR_SIZE);
        else
            pkt.write(old.nwDst);
        if (pv == V_1_1) {
            if (old.nwDstMask == null)
                pkt.writeZeros(IpAddress.IP_V4_ADDR_SIZE);
            else
                pkt.write(old.nwDstMask);
        }
        if (old.tpSrc == null)
            pkt.writeZeros(PortNumber.LENGTH_IN_BYTES);
        else
            pkt.write(old.tpSrc);
        if (old.tpDst == null)
            pkt.writeZeros(PortNumber.LENGTH_IN_BYTES);
        else
            pkt.write(old.tpDst);
        if (pv == V_1_1) {
            pkt.writeInt(old.mplsLabel);
            pkt.writeU8(old.mplsTc);
            pkt.writeZeros(PAD_OLD_MATCH_3);
            pkt.writeLong(old.metadata);
            pkt.writeLong(old.metadataMask);
        }
    }

    /** Converts an OXM TLV style match to an old match structure.
     *
     * @param match the match to convert
     * @return the old match
     */
    private static OldMatch convertToOldMatch(Match match) {
        final ProtocolVersion pv = match.getVersion();
        OldMatch old = new OldMatch(pv);
        old.type = match.getMatchType().getCode(pv);
        old.length = match.header.length;

        // start with everything wildcarded...
        old.wildcards = Wildcard.allWild(pv);
        old.nwSrcMask = Wildcard.ALL_WILD_IP_MASK;
        old.nwDstMask = Wildcard.ALL_WILD_IP_MASK;

        // then for every TLV, remove the corresponding wildcard flag...
        for (MatchField mf: match.getMatchFields()) {
            // IMPLEMENTATION NOTE:
            //  we don't need to worry about testing for duplicate match
            //  fields here, because the MutableMatch prevents duplicate
            //  field types from ever being created.

            switch ((OxmBasicFieldType) mf.getFieldType()) {

                case IN_PORT:
                    old.wildcards.remove(Wildcard.IN_PORT);
                    MfbInPort mip = (MfbInPort) mf;
                    old.inPort = mip.getPort();
                    break;
                case METADATA:
                    MfbMetadata mmd = (MfbMetadata) mf;
                    old.metadata = mmd.getValue();
                    old.metadataMask = mmd.getMask();
                    break;
                case ETH_DST:
                    old.wildcards.remove(Wildcard.DL_DST);
                    MfbEthDst med = (MfbEthDst) mf;
                    old.dlDst = med.getMacAddress();
                    if (pv == V_1_1)
                        old.dlDstMask = med.getMask();
                    break;
                case ETH_SRC:
                    old.wildcards.remove(Wildcard.DL_SRC);
                    MfbEthSrc mes = (MfbEthSrc) mf;
                    old.dlSrc = mes.getMacAddress();
                    if (pv == V_1_1)
                        old.dlSrcMask = mes.getMask();
                    break;
                case ETH_TYPE:
                    old.wildcards.remove(Wildcard.DL_TYPE);
                    MfbEthType met = (MfbEthType) mf;
                    old.dlType = met.getEthernetType();
                    break;
                case VLAN_VID:
                    old.wildcards.remove(Wildcard.DL_VLAN);
                    MfbVlanVid mvv = (MfbVlanVid) mf;
                    old.dlVlan = mvv.vid; // note - field access here
                    break;
                case VLAN_PCP:
                    old.wildcards.remove(Wildcard.DL_VLAN_PCP);
                    MfbVlanPcp mvp = (MfbVlanPcp) mf;
                    old.dlVlanPcp = mvp.getValue();
                    break;
                case IP_DSCP:
                    old.wildcards.remove(Wildcard.NW_TOS);
                    MfbIpDscp mid = (MfbIpDscp) mf;
                    old.nwTos = mid.getValue();
                    break;
                case IP_PROTO:
                    old.wildcards.remove(Wildcard.NW_PROTO);
                    MfbIpProto mipr = (MfbIpProto) mf;
                    old.nwProto = mipr.getIpProtocol().getNumber();
                    break;
                case IPV4_SRC:
                    MfbIpv4Src mi4s = (MfbIpv4Src) mf;
                    old.nwSrc = mi4s.getIpAddress();
                    old.nwSrcMask = mi4s.getMask();
                    // need to set up 6 bit nw-src wild bits when encoding
                    break;
                case IPV4_DST:
                    MfbIpv4Dst mi4d = (MfbIpv4Dst) mf;
                    old.nwDst= mi4d.getIpAddress();
                    old.nwDstMask = mi4d.getMask();
                    // need to set up 6 bit nw-dst wild bits when encoding
                    break;
                case TCP_SRC:
                    old.wildcards.remove(Wildcard.TP_SRC);
                    MfbTcpSrc mts = (MfbTcpSrc) mf;
                    old.tpSrc = mts.getPort();
                    break;
                case UDP_SRC:
                    old.wildcards.remove(Wildcard.TP_SRC);
                    MfbUdpSrc mus = (MfbUdpSrc) mf;
                    old.tpSrc = mus.getPort();
                    break;
                case SCTP_SRC:
                    old.wildcards.remove(Wildcard.TP_SRC);
                    MfbSctpSrc mss = (MfbSctpSrc) mf;
                    old.tpSrc = mss.getPort();
                    break;
                case TCP_DST:
                    old.wildcards.remove(Wildcard.TP_DST);
                    MfbTcpDst mtd = (MfbTcpDst) mf;
                    old.tpDst = mtd.getPort();
                    break;
                case UDP_DST:
                    old.wildcards.remove(Wildcard.TP_DST);
                    MfbUdpDst mud = (MfbUdpDst) mf;
                    old.tpDst = mud.getPort();
                    break;
                case SCTP_DST:
                    old.wildcards.remove(Wildcard.TP_DST);
                    MfbSctpDst msd = (MfbSctpDst) mf;
                    old.tpDst = msd.getPort();
                    break;

                // special handling of ICMP Type and Code in 1.0, 1.1
                case ICMPV4_TYPE:
                    // value encoded in TP_SRC
                    old.wildcards.remove(Wildcard.TP_SRC);
                    MfbIcmpv4Type mi4t = (MfbIcmpv4Type) mf;
                    old.tpSrc = PortNumber.valueOf(mi4t.getICMPv4Type().getCode());
                    break;
                case ICMPV4_CODE:
                    // value encoded in TP_DST
                    old.wildcards.remove(Wildcard.TP_DST);
                    MfbIcmpv4Code mi4c = (MfbIcmpv4Code) mf;
                    old.tpDst = PortNumber.valueOf(mi4c.getValue());
                    break;

                case ARP_OP:
                    // TODO: Review - does ARP_OP share NW_PROTO wildcard bit?
                    old.wildcards.remove(Wildcard.NW_PROTO);
                    MfbArpOp mao = (MfbArpOp) mf;
                    old.nwProto = mao.getValue();
                    break;
                case MPLS_LABEL:
                    old.wildcards.remove(Wildcard.MPLS_LABEL);
                    MfbMplsLabel mml = (MfbMplsLabel) mf;
                    old.mplsLabel = mml.getValue();
                    break;
                case MPLS_TC:
                    old.wildcards.remove(Wildcard.MPLS_TC);
                    MfbMplsTc mmt = (MfbMplsTc) mf;
                    old.mplsTc = mmt.getValue();
                    break;
                default:
                    // Should never happen, since we prevent creation of
                    // incongruous match fields per version, right?
                    throw new IllegalStateException(pv +
                            E_UNEX_STANDARD_FIELD_TYPE + mf.getFieldType());
            }
        }
        return old;
    }

    //======================================================================
    // === Utilities

    /** Calculates how many zero-filled bytes of padding are required at the
     * end of the match structure, so that the end of the structure lands
     * on an 8-byte boundary.
     *
     * @param len unpadded length
     * @return the number of padding bytes required
     */
    static int calcPadding(int len) {
        // See section A.2.3.1 (p.39) of the 1.3 spec for details...
        return ((len + 7) / 8 * 8 - len);
    }


    /** Examines the specified match to confirm that:
     * <ul>
     *     <li>there is no more than one of each match field type</li>
     *     <li>the match field pre-requisites are correctly satisfied</li>
     * </ul>
     * If all is well, the method silently returns.
     *
     * @param match the match structure to validate
     * @throws org.opendaylight.util.ValidationException if we have one or more issues
     */
    static void validatePreRequisites(Match match) {
        MatchValidator.validateMatch(match);
    }
}