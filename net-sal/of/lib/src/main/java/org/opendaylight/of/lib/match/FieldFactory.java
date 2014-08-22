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
import org.opendaylight.of.lib.instr.ActionFactory;
import org.opendaylight.of.lib.instr.ActionType;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.util.PrimitiveUtils;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.net.*;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.util.net.IpAddress.*;

/**
 * Provides facilities for parsing, creating and encoding {@link MatchField}
 * instances.
 * <p>
 * Used by the {@link MatchFactory} in its loftier goal of creating
 * Match structures.
 * <p>
 * Also used to create basic fields for
 * {@link ActionFactory#createAction(ProtocolVersion, ActionType, MFieldBasic)
 * Set-Field actions}.
 * <p>
 * To create basic match fields, one of the overloaded
 * {@code createBasicField(pv, fieldType, ...)} methods can be invoked.
 * The following list documents the mapping of field types to payload types:
 * <ul>
 *     <li>{@link OxmBasicFieldType#IN_PORT IN_PORT} - {@link BigPortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IN_PHY_PORT IN_PHY_PORT} -
 *          {@code BigPortNumber} <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#METADATA METADATA} - long</li>
 *     <li>{@link OxmBasicFieldType#ETH_DST ETH_DST} - {@link MacAddress}</li>
 *     <li>{@link OxmBasicFieldType#ETH_SRC ETH_SRC} - {@code MacAddress}</li>
 *     <li>{@link OxmBasicFieldType#ETH_TYPE ETH_TYPE} - {@link EthernetType}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#VLAN_VID VLAN_VID} - {@link VlanId}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#VLAN_PCP VLAN_PCP} - int
 *          <em>(u3, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IP_DSCP IP_DSCP} - int
 *          <em>(u6, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IP_ECN IP_ECN} -
 *          int <em>(u2, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IP_PROTO IP_PROTO} - {@link IpProtocol}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IPV4_SRC IPV4_SRC} - {@link IpAddress}</li>
 *     <li>{@link OxmBasicFieldType#IPV4_DST IPV4_DST} - {@code IpAddress}</li>
 *     <li>{@link OxmBasicFieldType#TCP_SRC TCP_SRC} - {@link PortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#TCP_DST TCP_DST} - {@code PortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#UDP_SRC UDP_SRC} - {@code PortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#UDP_DST UDP_DST} - {@code PortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#SCTP_SRC SCTP_SRC} - {@code PortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#SCTP_DST SCTP_DST} - {@code PortNumber}
 *          <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#ICMPV4_TYPE ICMPV4_TYPE} -
 *          {@link ICMPv4Type} <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#ICMPV4_CODE ICMPV4_CODE} - int
 *          <em>(u8, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#ARP_OP ARP_OP} - int
 *          <em>(u16, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#ARP_SPA ARP_SPA} - {@code IpAddress}</li>
 *     <li>{@link OxmBasicFieldType#ARP_TPA ARP_TPA} - {@code IpAddress}</li>
 *     <li>{@link OxmBasicFieldType#ARP_SHA ARP_SHA} - {@code MacAddress}</li>
 *     <li>{@link OxmBasicFieldType#ARP_THA ARP_THA} - {@code MacAddress}</li>
 *     <li>{@link OxmBasicFieldType#IPV6_SRC IPV6_SRC} - {@code IpAddress}</li>
 *     <li>{@link OxmBasicFieldType#IPV6_DST IPV6_DST} - {@code IpAddress}</li>
 *     <li>{@link OxmBasicFieldType#IPV6_FLABEL IPV6_FLABEL} - int
 *          <em>(u20)</em></li>
 *     <li>{@link OxmBasicFieldType#ICMPV6_TYPE ICMPV6_TYPE} -
 *          {@link ICMPv6Type} <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#ICMPV6_CODE ICMPV6_CODE} - int
 *          <em>(u8, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IPV6_ND_TARGET IPV6_ND_TARGET} -
 *          {@code IpAddress} <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IPV6_ND_SLL IPV6_ND_SLL} -
 *          {@code MacAddress} <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#IPV6_ND_TLL IPV6_ND_TLL} -
 *          {@code MacAddress} <em>(not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#MPLS_LABEL MPLS_LABEL} - int
 *          <em>(u20, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#MPLS_TC MPLS_TC} - int
 *          <em>(u3, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#MPLS_BOS MPLS_BOS} - int
 *          <em>(u1, not maskable)</em></li>
 *     <li>{@link OxmBasicFieldType#PBB_ISID PBB_ISID} - int <em>(u24)</em></li>
 *     <li>{@link OxmBasicFieldType#TUNNEL_ID TUNNEL_ID} - long</li>
 *     <li>{@link OxmBasicFieldType#IPV6_EXTHDR IPV6_EXTHDR} -
 *          Set<{@link IPv6ExtHdr}></li>
 * </ul>
 *
 * @author Simon Hunt
 */
public class FieldFactory extends AbstractFactory {
    /* Normally, we'll encode MPLS_LABEL (20 bits) in 3 bytes. But to allow for
     * (incorrectly) interpreted implementations that encode into 4 bytes, this
     * static flag can be set to true.
     */
    static boolean _MPLS_LABEL_ENCODE_IN_4_BYTES = false;


    static final int EXP_ID_LEN = 4;

    static final int LEN_U8 = 1;
    static final int LEN_U16 = 2;
    static final int LEN_U24 = 3;
    static final int LEN_U32 = 4;

    // VLAN special values
    static final VId PRESENT = VId.valueOf(OxmVlanId.PRESENT.getValue());
    static final VId NONE = VId.valueOf(OxmVlanId.NONE.getValue());

    // Length in Bytes values
    // TODO - consider updating hp-util types with LENGTH_IN_BYTES constants
    static final int LIB_ETHERNET_TYPE = 2;
    static final int LIB_TUNNEL_ID = 8;
    static final int LIB_METADATA = 8;
    static final int LIB_IPV6_EXTHDR= 2;

    // Some constraints on bit field sizes...
    static final int MAX_1_BIT = 0x01;
    static final int MAX_2_BITS = 0x03;
    static final int MAX_3_BITS = 0x07;
    static final int MAX_6_BITS = 0x3f;
    static final int MAX_7_BITS = 0x7f;
    static final int MAX_20_BITS = 0x0fffff;
    static final int MAX_24_BITS = 0xffffff;

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            FieldFactory.class, "fieldFactory");

    static final String E_VLAN_NO_PRESENT = RES.getString("e_vlan_no_present");
    static final String E_VLAN_BAD_PRESENT = RES
            .getString("e_vlan_bad_present");

    static final String E_BAD_VAL_VLANP = RES.getString("e_bad_val_vlanp");
    static final String E_BAD_VAL_IPDSCP = RES.getString("e_bad_val_ipdscp");
    static final String E_BAD_VAL_IPECN = RES.getString("e_bad_val_ipecn");
    static final String E_BAD_VAL_FLABEL = RES.getString("e_bad_val_flabel");
    static final String E_BAD_VAL_MPLSLABEL = RES
            .getString("e_bad_val_mplslabel");
    static final String E_BAD_VAL_MPLSTC = RES.getString("e_bad_val_mplstc");
    static final String E_BAD_VAL_MPLSBOS = RES.getString("e_bad_val_mplsbos");

    static final FieldFactory FF = new FieldFactory();

    // No instantiation except here
    private FieldFactory() { }

    /** Returns an identifying tag for the field factory.
     *
     * @return an identifying tag
     */
    @Override
    protected String tag() {
        return "FF";
    }

    // =======================================================================
    // === Delegate to the FieldParser to parse fields.

    /** Parses a single OXM TLV match field from the supplied buffer.
     * Note that this method causes the reader index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the field.
     *
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a parsed match field
     * @throws MessageParseException if unable to parse the field
     */
    public static MatchField parseField(OfPacketReader pkt, ProtocolVersion pv)
            throws MessageParseException {
        return FieldParser.parseField(pkt, pv);
    }

    /** Parses a list of match field header structures from the supplied
     * buffer. This method is provided to support the parsing of an "OXM"
     * table feature property.
     * This list returned contains either {@link MFieldBasicHeader} instances
     * or {@link MFieldExperimenter} instances.
     *
     * @see org.opendaylight.of.lib.msg.TableFeatureFactory
     * @see org.opendaylight.of.lib.msg.TableFeaturePropOxm
     *
     * @param targetRi the target reader index
     * @param pkt the data buffer
     * @param pv the protocol version
     * @return a list of parsed match fields (header info only)
     * @throws MessageParseException if there is an issue parsing the structure
     */
    public static List<MatchField> parseFieldHeaders(int targetRi,
                                                    OfPacketReader pkt,
                                                    ProtocolVersion pv)
            throws MessageParseException {
        return FieldParser.parseFieldHeaders(targetRi, pkt, pv);
    }

    // =======================================================================
    // === Create match fields from the given parameters

    private static final String E_UNEX_TYPE = RES.getString("e_unex_type");
    private static final String E_WR_IP_FAM = RES.getString("e_wr_ip_fam");
    private static final String E_FAM_MISMATCH = RES
            .getString("e_fam_mismatch");
    private static final String E_NEG = RES.getString("e_neg");
    private static final String E_OOR = RES.getString("e_oor");
    private static final String E_MASK = RES.getString("e_mask");
    private static final int NO_MASK = 0;
    private static final int NO_MASK_L = 0;

    /** Creates a match field header.
     *
     * @param pv the protocol version
     * @param clazz match class
     * @param rawClazz raw OXM class
     * @param ft the field type
     * @param rawFieldType raw field type
     * @return the header
     */
    private static MatchField.Header createHeader(ProtocolVersion pv,
                                                  OxmClass clazz,
                                                  int rawClazz,
                                                  OxmFieldType ft,
                                                  int rawFieldType) {
        MatchField.Header header = new MatchField.Header();
        header.clazz = clazz;
        header.fieldType = ft;
        header.rawClazz = rawClazz;
        header.rawFieldType = rawFieldType;
        header.rawOxmType = MatchField.calcRawOxmType(rawClazz, rawFieldType);
        header.length = 0; // payload length only (excludes header)
        return header;
    }

    /** Creates a basic match field header.
     *
     * @param pv the protocol version
     * @param ft the basic field type
     * @return the header
     */
    private static MatchField.Header createHeader(ProtocolVersion pv,
                                                  OxmBasicFieldType ft) {
        return createHeader(pv, OxmClass.OPENFLOW_BASIC,
                OxmClass.OPENFLOW_BASIC.getCode(),
                ft, ft.getCode());
    }


    // =======================================================================
    // === Create BASIC Match Fields

    /** Creates a basic match field for a big-port-number-based match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#IN_PORT IN_PORT}</li>
     *     <li>{@link OxmBasicFieldType#IN_PHY_PORT IN_PHY_PORT}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param port the port number
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               BigPortNumber port) {
        notNull(pv, ft, port);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MFieldBasicBigPort mf;
        switch (ft) {
            case IN_PORT:
                mf = new MfbInPort(pv, hdr);
                break;
            case IN_PHY_PORT:
                mf = new MfbInPhyPort(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.port = port;
        mf.header.length = BigPortNumber.LENGTH_IN_BYTES;
        return mf;
    }

    /** Creates a basic match field for a mac-address-based match field
     * with a mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ETH_DST ETH_DST}</li>
     *     <li>{@link OxmBasicFieldType#ETH_SRC ETH_SRC}</li>
     *     <li>{@link OxmBasicFieldType#ARP_SHA ARP_SHA}</li>
     *     <li>{@link OxmBasicFieldType#ARP_THA ARP_THA}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_ND_SLL IPV6_ND_SLL}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_ND_TLL IPV6_ND_TLL}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param mac the MAC address
     * @param mask the MAC address mask (may be null)
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               MacAddress mac,
                                               MacAddress mask) {
        notNull(pv, ft, mac);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MFieldBasicMac mf;
        boolean hasMask = mask != null;
        switch (ft) {
            case ETH_DST:
                mf = new MfbEthDst(pv, hdr);
                break;
            case ETH_SRC:
                mf = new MfbEthSrc(pv, hdr);
                break;
            case ARP_SHA:
                mf = new MfbArpSha(pv, hdr);
                break;
            case ARP_THA:
                mf = new MfbArpTha(pv, hdr);
                break;
            case IPV6_ND_SLL:
                mf = new MfbIpv6NdSll(pv, hdr);
                break;
            case IPV6_ND_TLL:
                mf = new MfbIpv6NdTll(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.mac = mac;
        mf.mask = mask;
        mf.header.hasMask = hasMask;
        mf.header.length = hasMask
                ? MacAddress.MAC_ADDR_SIZE * 2 : MacAddress.MAC_ADDR_SIZE;
        return mf;
    }

    /** Creates a basic match field for a mac-address-based match field
     * without a mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ETH_DST ETH_DST}</li>
     *     <li>{@link OxmBasicFieldType#ETH_SRC ETH_SRC}</li>
     *     <li>{@link OxmBasicFieldType#ARP_SHA ARP_SHA}</li>
     *     <li>{@link OxmBasicFieldType#ARP_THA ARP_THA}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_ND_SLL IPV6_ND_SLL}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_ND_TLL IPV6_ND_TLL}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param mac the MAC address
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               MacAddress mac) {
        return createBasicField(pv, ft, mac, null);
    }


    /** Creates a basic match field for an ip-address-based match field
     * with a mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#IPV4_SRC IPV4_SRC}</li>
     *     <li>{@link OxmBasicFieldType#IPV4_DST IPV4_DST}</li>
     *     <li>{@link OxmBasicFieldType#ARP_SPA ARP_SPA}</li>
     *     <li>{@link OxmBasicFieldType#ARP_TPA ARP_TPA}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_SRC IPV6_SRC}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_DST IPV6_DST}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_ND_TARGET IPV6_ND_TARGET}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param ip the IP address
     * @param mask the IP address mask (may be null)
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     *          or IP address family is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               IpAddress ip,
                                               IpAddress mask) {
        notNull(pv, ft, ip);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MFieldBasicIp mf;
        Family fam = ip.getFamily();
        int len = fam.equals(Family.IPv4) ? IP_V4_ADDR_SIZE : IP_V6_ADDR_SIZE;
        boolean bad;
        switch (ft) {
            case IPV4_SRC:
                mf = new MfbIpv4Src(pv, hdr);
                bad = fam == Family.IPv6;
                break;
            case IPV4_DST:
                mf = new MfbIpv4Dst(pv, hdr);
                bad = fam == Family.IPv6;
                break;
            case ARP_SPA:
                mf = new MfbArpSpa(pv, hdr);
                bad = fam == Family.IPv6;
                break;
            case ARP_TPA:
                mf = new MfbArpTpa(pv, hdr);
                bad = fam == Family.IPv6;
                break;
            case IPV6_SRC:
                mf = new MfbIpv6Src(pv, hdr);
                bad = fam == Family.IPv4;
                break;
            case IPV6_DST:
                mf = new MfbIpv6Dst(pv, hdr);
                bad = fam == Family.IPv4;
                break;
            case IPV6_ND_TARGET:
                mf = new MfbIpv6NdTarget(pv, hdr);
                bad = fam == Family.IPv4;
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.ip = ip;
        mf.mask = mask;
        boolean hasMask = mask != null;
        if (bad)
            throw new IllegalArgumentException(E_WR_IP_FAM + fam);
        if (hasMask && mask.getFamily() != fam)
            throw new IllegalArgumentException(E_FAM_MISMATCH);
        mf.header.hasMask = hasMask;
        mf.header.length = hasMask ? len * 2 : len;
        return mf;
    }

    /** Creates a basic match field for an ip-address-based match field
     * without a mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#IPV4_SRC IPV4_SRC}</li>
     *     <li>{@link OxmBasicFieldType#IPV4_DST IPV4_DST}</li>
     *     <li>{@link OxmBasicFieldType#ARP_SPA ARP_SPA}</li>
     *     <li>{@link OxmBasicFieldType#ARP_TPA ARP_TPA}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_SRC IPV6_SRC}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_DST IPV6_DST}</li>
     *     <li>{@link OxmBasicFieldType#IPV6_ND_TARGET IPV6_ND_TARGET}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param ip the IP address
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     *          or IP address family is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               IpAddress ip) {
        return createBasicField(pv, ft, ip, null);
    }

    /** Creates a basic match field for a port-number-based match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#TCP_SRC TCP_SRC}</li>
     *     <li>{@link OxmBasicFieldType#TCP_DST TCP_DST}</li>
     *     <li>{@link OxmBasicFieldType#UDP_SRC UDP_SRC}</li>
     *     <li>{@link OxmBasicFieldType#UDP_DST UDP_DST}</li>
     *     <li>{@link OxmBasicFieldType#SCTP_SRC SCTP_SRC}</li>
     *     <li>{@link OxmBasicFieldType#SCTP_DST SCTP_DST}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param port the port number
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               PortNumber port) {
        notNull(pv, ft, port);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MFieldBasicPort mf;
        switch(ft) {
            case TCP_SRC:
                mf = new MfbTcpSrc(pv, hdr);
                break;
            case TCP_DST:
                mf = new MfbTcpDst(pv, hdr);
                break;
            case UDP_SRC:
                mf = new MfbUdpSrc(pv, hdr);
                break;
            case UDP_DST:
                mf = new MfbUdpDst(pv, hdr);
                break;
            case SCTP_SRC:
                mf = new MfbSctpSrc(pv, hdr);
                break;
            case SCTP_DST:
                mf = new MfbSctpDst(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.port = port;
        mf.header.length = U16Id.LENGTH_IN_BYTES;
        return mf;
    }

    /** Creates a basic match field for an ICMPv4 Type match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ICMPV4_TYPE ICMPV4_TYPE}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type (ICMPV4_TYPE)
     * @param icmpv4Type the ICMPv4 Type
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               ICMPv4Type icmpv4Type) {
        notNull(pv, ft, icmpv4Type);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MfbIcmpv4Type mf;
        switch (ft) {
            case ICMPV4_TYPE:
                mf = new MfbIcmpv4Type(pv, hdr);
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.type = icmpv4Type;
        mf.header.length = LEN_U8;
        return mf;
    }

    /** Creates a basic match field for an ICMPv6 Type match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ICMPV6_TYPE ICMPV6_TYPE}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type (ICMPV6_TYPE)
     * @param icmpv6Type the ICMPv6 Type
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               ICMPv6Type icmpv6Type) {
        notNull(pv, ft, icmpv6Type);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MfbIcmpv6Type mf;
        switch (ft) {
            case ICMPV6_TYPE:
                mf = new MfbIcmpv6Type(pv, hdr);
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.type = icmpv6Type;
        mf.header.length = LEN_U8;
        return mf;
    }

    /** Creates a basic match field for an int-payload-based match field,
     * with a mask. A mask value of 0 is interpreted as no mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ARP_OP ARP_OP}
     *          <em>(u16, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#IPV6_FLABEL IPV6_FLABEL}
     *          <em>(u20)</em></li>
     *     <li>{@link OxmBasicFieldType#MPLS_LABEL MPLS_LABEL}
     *          <em>(u20, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#PBB_ISID PBB_ISID}
     *          <em>(u24)</em></li>
     *     <li>{@link OxmBasicFieldType#VLAN_PCP VLAN_PCP}
     *          <em>(u3, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#IP_DSCP IP_DSCP}
     *          <em>(u6, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#IP_ECN IP_ECN}
     *          <em>(u2, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#ICMPV4_CODE ICMPV4_CODE}
     *          <em>(u8, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#ICMPV6_CODE ICMPV6_CODE}
     *          <em>(u8, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#MPLS_TC MPLS_TC}
     *          <em>(u3, not maskable)</em></li>
     *     <li>{@link OxmBasicFieldType#MPLS_BOS MPLS_BOS}
     *          <em>(u1, not maskable)</em></li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param value the value
     * @param mask the value mask (or 0 for no mask)
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate or
     *          the value is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               int value,
                                               int mask) {
        notNull(pv, ft);
        MessageFactory.checkVersionSupported(pv);
        if (value < 0)
            throw new IllegalArgumentException(E_NEG + "value: " + value);
        if (mask < 0)
            throw new IllegalArgumentException(E_NEG + "mask: " + mask);


        MatchField.Header hdr = createHeader(pv, ft);
        MFieldBasicInt mf;
        boolean oor;
        int len;
        boolean hasMask = mask != NO_MASK;
        boolean badMask = false;
        switch (ft) {
            case ARP_OP:
                mf = new MfbArpOp(pv, hdr);
                len = LEN_U16;
                oor = value > U16Id.MAX_VALUE;
                badMask = mask != NO_MASK;
                break;
            case IPV6_FLABEL:
                mf = new MfbIpv6Flabel(pv, hdr);
                // TODO: Review, should this be U32 or U24?
                len = LEN_U24;
                oor = value > MAX_20_BITS ||
                        (mask != NO_MASK && mask > MAX_20_BITS);
                break;
            case MPLS_LABEL:
                mf = new MfbMplsLabel(pv, hdr);
                // TODO: Review, should this be U32 or U24?
                len = _MPLS_LABEL_ENCODE_IN_4_BYTES ? LEN_U32 : LEN_U24;
                oor = value > MAX_20_BITS;
                badMask = mask != NO_MASK;
                break;
            case PBB_ISID:
                mf = new MfbPbbIsid(pv, hdr);
                // TODO: Review, should this be U32 or U24?
                len = LEN_U24;
                oor = value > MAX_24_BITS ||
                        (mask != NO_MASK && mask > MAX_24_BITS);
                break;
            case VLAN_PCP:
                mf = new MfbVlanPcp(pv, hdr);
                len = LEN_U8;
                oor = value > MAX_3_BITS;
                badMask = mask != NO_MASK;
                break;
            case IP_DSCP:
                mf = new MfbIpDscp(pv, hdr);
                len = LEN_U8;
                oor = value > MAX_6_BITS;
                badMask = mask != NO_MASK;
                break;
            case IP_ECN:
                mf = new MfbIpEcn(pv, hdr);
                len = LEN_U8;
                oor = value > MAX_2_BITS;
                badMask = mask != NO_MASK;
                break;
            case ICMPV4_CODE:
                mf = new MfbIcmpv4Code(pv, hdr);
                len = LEN_U8;
                oor = value > U8Id.MAX_VALUE;
                badMask = mask != NO_MASK;
                break;
            case ICMPV6_CODE:
                mf = new MfbIcmpv6Code(pv, hdr);
                len = LEN_U8;
                oor = value > U8Id.MAX_VALUE;
                badMask = mask != NO_MASK;
                break;
            case MPLS_TC:
                mf = new MfbMplsTc(pv, hdr);
                len = LEN_U8;
                oor = value > MAX_3_BITS;
                badMask = mask != NO_MASK;
                break;
            case MPLS_BOS:
                mf = new MfbMplsBos(pv, hdr);
                len = LEN_U8;
                oor = value > MAX_1_BIT;
                badMask = mask != NO_MASK;
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        if (oor) {
            String msk = mask == NO_MASK ? "" : ",m=" + mask;
            throw new IllegalArgumentException(E_OOR + ft + ":v=" + value + msk);
        }
        if (badMask)
            throw new IllegalArgumentException(E_MASK + ft + ":m=" + mask);

        mf.value = value;
        mf.mask = mask;
        mf.header.hasMask = hasMask;
        mf.header.length = hasMask ? len * 2 : len;
        return mf;
    }

    /** Creates a basic match field for an int-payload-based match field,
     * without a mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ARP_OP ARP_OP}
     *          <em>(u16)</em></li>
     *     <li>{@link OxmBasicFieldType#IPV6_FLABEL IPV6_FLABEL}
     *          <em>(u20)</em></li>
     *     <li>{@link OxmBasicFieldType#MPLS_LABEL MPLS_LABEL}
     *          <em>(u20)</em></li>
     *     <li>{@link OxmBasicFieldType#PBB_ISID PBB_ISID}
     *          <em>(u24)</em></li>
     *     <li>{@link OxmBasicFieldType#VLAN_PCP VLAN_PCP}
     *          <em>(u3)</em></li>
     *     <li>{@link OxmBasicFieldType#IP_DSCP IP_DSCP}
     *          <em>(u6)</em></li>
     *     <li>{@link OxmBasicFieldType#IP_ECN IP_ECN}
     *          <em>(u2)</em></li>
     *     <li>{@link OxmBasicFieldType#ICMPV4_CODE ICMPV4_CODE}
     *          <em>(u8)</em></li>
     *     <li>{@link OxmBasicFieldType#ICMPV6_CODE ICMPV6_CODE}
     *          <em>(u8)</em></li>
     *     <li>{@link OxmBasicFieldType#MPLS_TC MPLS_TC}
     *          <em>(u3)</em></li>
     *     <li>{@link OxmBasicFieldType#MPLS_BOS MPLS_BOS}
     *          <em>(u1)</em></li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param value the value
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate or
     *          the value is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               int value) {
        return createBasicField(pv, ft, value, NO_MASK);
    }

    /** Creates a basic match field for an ETH_TYPE match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#ETH_TYPE ETH_TYPE}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param ethType the Ethernet Type
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               EthernetType ethType) {
        notNull(pv, ft, ethType);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MfbEthType mf;
        switch (ft) {
            case ETH_TYPE:
                mf = new MfbEthType(pv, hdr);
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.ethType = ethType;
        mf.header.length = LIB_ETHERNET_TYPE;
        return mf;
    }

    /**
     * Creates a basic match field for a VLAN_VID. Note that the special
     * values {@link VlanId#NONE} and {@link VlanId#PRESENT} can be used
     * to specify the absence or presence (without specifying the value)
     * of a VLAN tag.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#VLAN_VID VLAN_VID}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type (VLAN_VID)
     * @param vlanId the VLAN vid to match
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               VlanId vlanId) {
        notNull(pv, ft, vlanId);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MfbVlanVid mf;
        switch (ft) {
            case VLAN_VID:
                mf = new MfbVlanVid(pv, hdr);
                break;
            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.vid = MatchFactory.equivVid(vlanId);
        mf.header.hasMask = mf.vid.equals(VId.PRESENT);
        mf.header.length = mf.header.hasMask ? U16Id.LENGTH_IN_BYTES * 2
                                             : U16Id.LENGTH_IN_BYTES;
        return mf;
    }


    /** Creates a basic match field for an IP Protocol match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#IP_PROTO IP_PROTO}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type (IP_PROTO)
     * @param ipp the IP protocol
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               IpProtocol ipp) {
        notNull(pv, ft, ipp);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MfbIpProto mf;
        switch (ft) {
            case IP_PROTO:
                mf = new MfbIpProto(pv, hdr);
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.ipp = ipp;
        mf.header.length = LEN_U8;
        return mf;
    }


    /** Creates a basic match field for a long-payload-based match field,
     * with a mask. A mask value of 0 is interpreted to mean no mask.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#TUNNEL_ID TUNNEL_ID}</li>
     *     <li>{@link OxmBasicFieldType#METADATA METADATA}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param value the value
     * @param mask the value mask
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     *          or the value is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               long value,
                                               long mask) {
        notNull(pv, ft);
        MessageFactory.checkVersionSupported(pv);
        MatchField.Header hdr = createHeader(pv, ft);
        MFieldBasicLong mf;
        boolean hasMask = mask != NO_MASK_L;
        int len;
        switch (ft) {
            case TUNNEL_ID:
                mf = new MfbTunnelId(pv, hdr);
                len = LIB_TUNNEL_ID;
                break;

            case METADATA:
                mf = new MfbMetadata(pv, hdr);
                len = LIB_METADATA;
                break;

            default:
                throw new IllegalArgumentException(E_UNEX_TYPE + ft);
        }
        mf.value = value;
        mf.mask = mask;
        mf.header.hasMask = hasMask;
        mf.header.length = hasMask ? len * 2 : len;
        return mf;
    }


    /** Creates a basic match field for a long-payload-based match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#TUNNEL_ID TUNNEL_ID}</li>
     *     <li>{@link OxmBasicFieldType#METADATA METADATA}</li>
     * </ul>
     *
     * @param pv the protocol version
     * @param ft the field type
     * @param value the value
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     *          or the value is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               long value) {
        return createBasicField(pv, ft, value, NO_MASK_L);
    }

    /**
     * Creates a basic match field for an IPV6_EXTHDR match field.
     * <p>
     * Supported basic field types for this method are:
     * <ul>
     *     <li>{@link OxmBasicFieldType#IPV6_EXTHDR IPV6_EXTHDR}</li>
     * </ul>
     * <p>
     * The provided map specifies those extension header flags that should
     * be matched on. If the value is {@code true}, the header flag must be
     * present; if the value is {@code false}, the header flag must be absent.
     * Those flags not included in the map are "don't care" values.
     *
     * @param pv the protocol version
     * @param ft the field type (IPV6_EXTHDR)
     * @param flags the map of IPv6 Extension Header flags to match
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if field type is not appropriate
     */
    public static MFieldBasic createBasicField(ProtocolVersion pv,
                                               OxmBasicFieldType ft,
                                               Map<IPv6ExtHdr, Boolean> flags) {
        notNull(pv, ft, flags);
        MessageFactory.checkVersionSupported(pv);
        if (ft != OxmBasicFieldType.IPV6_EXTHDR)
            throw new IllegalArgumentException(E_UNEX_TYPE + ft);

        MatchField.Header hdr = createHeader(pv, ft);
        MfbIpv6Exthdr mf = new MfbIpv6Exthdr(pv, hdr);

        // convert map to two sets
        Set<IPv6ExtHdr> values = EnumSet.noneOf(IPv6ExtHdr.class);
        Set<IPv6ExtHdr> mask = EnumSet.noneOf(IPv6ExtHdr.class);
        for (Map.Entry<IPv6ExtHdr, Boolean> entry: flags.entrySet()) {
            IPv6ExtHdr eh = entry.getKey();
            mask.add(eh);
            if (entry.getValue())
                values.add(eh);
        }

        // then encode the sets
        mf.rawBits = IPv6ExtHdr.encodeBitmap(values, pv);
        if (ALL_I6EH_FLAGS.equals(mask)) {
            // all flags are present, so the mask can be dropped
            mf.header.hasMask = false;
            mf.header.length = LIB_IPV6_EXTHDR;
        } else {
            // the mask needs to be included in the encoding
            mf.header.hasMask = true;
            mf.mask = IPv6ExtHdr.encodeBitmap(mask, pv);
            mf.header.length = LIB_IPV6_EXTHDR * 2;
        }
        return mf;
    }

    private static final Set<IPv6ExtHdr> ALL_I6EH_FLAGS =
            EnumSet.allOf(IPv6ExtHdr.class);


    // =======================================================================
    // === Create Experimenter and Minimal Match Fields

    private static void verifyU7(int i) {
        if (i < 0 || i > MAX_7_BITS)
            throw new IllegalArgumentException("value not U7: " + i);
    }

    /** Creates an experimenter match field.
     *
     * @param pv the protocol version
     * @param rawFieldType the experimenter-defined field type
     * @param experId the encoded experimenter ID
     * @param payload the experimenter-defined payload
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if rawFieldType is not U7
     */
    public static MFieldExperimenter
    createExperimenterField(ProtocolVersion pv, int rawFieldType, int experId,
                            byte[] payload) {
        notNull(pv);
        MessageFactory.checkVersionSupported(pv);
        verifyU7(rawFieldType);

        MatchField.Header hdr = createHeader(pv, OxmClass.EXPERIMENTER,
                OxmClass.EXPERIMENTER.getCode(), null, rawFieldType);
        MFieldExperimenter mf = new MFieldExperimenter(pv, hdr);
        mf.id = experId;
        mf.payload = payload == null ? null : payload.clone();
        int payloadLen = payload == null ? 0 : payload.length;
        mf.header.length = EXP_ID_LEN + payloadLen;
        return mf;
    }

    /** Creates an experimenter match field.
     *
     * @param pv the protocol version
     * @param rawFieldType the experimenter-defined field type
     * @param eid the experimenter ID
     * @param payload the experimenter-defined payload
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if rawFieldType is not U7
     */
    public static MFieldExperimenter
    createExperimenterField(ProtocolVersion pv, int rawFieldType,
                            ExperimenterId eid, byte[] payload) {
        return createExperimenterField(pv, rawFieldType, eid.encodedId(),
                payload);
    }


    /** Creates a minimal match field for a known OXM Class.
     * If the class is unknown, use
     * {@link #createMinimalField(ProtocolVersion, OxmClass, int, int, byte[])}
     * instead.
     *
     * @param pv the protocol version
     * @param clazz the OXM class
     * @param rawFieldType the elsewhere-defined field type
     * @param payload the elsewhere-defined payload
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if clazz is UNKNOWN or if
     *          rawFieldType is not U7
     */
    public static MFieldMinimal createMinimalField(ProtocolVersion pv,
                                                   OxmClass clazz,
                                                   int rawFieldType,
                                                   byte[] payload) {
        notNull(pv, clazz, payload);
        MessageFactory.checkVersionSupported(pv);
        if (clazz == OxmClass.UNKNOWN)
            throw new IllegalArgumentException("Unknown OXM class should use" +
                " createMinimalField(pv,clazz,rawClazz,rawFieldType,payload)");
        verifyU7(rawFieldType);

        MatchField.Header hdr = createHeader(pv, clazz, clazz.getCode(),
                null, rawFieldType);
        MFieldMinimal mf = new MFieldMinimal(pv, hdr);
        mf.payload = payload.clone();
        mf.header.length = payload.length;
        return mf;
    }

    /** Creates a minimal match field for an unknown OXM Class.
     *
     * If the class is known, use
     * {@link #createMinimalField(ProtocolVersion, OxmClass, int, byte[])}
     * instead.
     *
     * @param pv the protocol version
     * @param clazz the OXM class (UNKNOWN)
     * @param rawClazz the elsewhere-defined OXM class
     * @param rawFieldType the elsewhere-defined field type
     * @param payload the elsewhere-defined payload
     * @return the match field
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     * @throws IllegalArgumentException if clazz is not UNKNOWN or if
     *          rawClazz is not U16, or if rawFieldType is not U7.
     */
    public static MFieldMinimal createMinimalField(ProtocolVersion pv,
                                                   OxmClass clazz,
                                                   int rawClazz,
                                                   int rawFieldType,
                                                   byte[] payload) {
        notNull(pv, clazz, payload);
        MessageFactory.checkVersionSupported(pv);
        if (clazz != OxmClass.UNKNOWN)
            throw new IllegalArgumentException("Known OXM classes should use" +
                    " createMinimalField(pv,clazz,rawFieldType,payload)");
        PrimitiveUtils.verifyU16(rawClazz);
        verifyU7(rawFieldType);

        MatchField.Header hdr = createHeader(pv, clazz, rawClazz,
                null, rawFieldType);
        MFieldMinimal mf = new MFieldMinimal(pv, hdr);
        mf.payload = payload.clone();
        mf.header.length = payload.length;
        return mf;
    }

    // ====

    /** Creates minimal match field headers to be used in encoding a table
     * features OXM property. The map provided indicates whether or not the
     * hasMask bit should be set for each field.
     *
     * @param pv the protocol version
     * @param map the map of fields to create
     * @return the list of minimal fields
     * @throws VersionNotSupportedException if the version is not supported
     * @throws NullPointerException if any required parameter is null
     */
    public static List<MatchField>
    createFieldHeaders(ProtocolVersion pv, Map<OxmBasicFieldType, Boolean> map) {
        notNull(pv, map);
        MessageFactory.checkVersionSupported(pv);
        List<MatchField> fields = new ArrayList<>(map.size());
        for (Map.Entry<OxmBasicFieldType, Boolean> me : map.entrySet()) {
            OxmBasicFieldType ft = me.getKey();
            MatchField.Header hdr = createHeader(pv, ft);
            hdr.hasMask = me.getValue();
            fields.add(new MFieldBasicHeader(pv, hdr));
        }
        return fields;
    }

    // =======================================================================
    // === Delegate to the FieldEncoder to encode fields. (package private)

    /** Encodes a single OXM TLV match field, writing it into the supplied
     * buffer. Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the field.
     *
     * @param mf the match field to encode
     * @param pkt the buffer into which the match field is to be written
     */
    // NOTE: needs to be public so ActionEncoder can access
    public static void encodeField(MatchField mf, OfPacketWriter pkt) {
        FieldEncoder.encodeField(mf, pkt);
    }

    /** Encodes a list of match fields, writing them into the supplied buffer.
     * Note that this method causes the writer index of the underlying
     * {@code PacketBuffer} to be advanced by the length of the written fields.
     *
     * @param fields the list of fields
     * @param pkt the buffer into which the fields are to be written
     */
    public static void encodeFieldList(List<MatchField> fields,
                                       OfPacketWriter pkt) {
        for (MatchField mf: fields)
            encodeField(mf, pkt);
    }

    /** Encodes a list of experimenter match fields, writing them into the
     * supplied buffer. Note that this method causes the writer index of the
     * underlying {@code PacketBuffer} to be advanced by the length of the
     * written fields.
     *
     * @param fields the experimenter fields
     * @param pkt the buffer into which the fields are to be written
     */
    public static void encodeFieldExperList(List<MFieldExperimenter> fields,
                                            OfPacketWriter pkt) {
        for (MatchField f: fields)
            encodeField(f, pkt);
    }
}