/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.json;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.match.*;
import org.opendaylight.util.net.*;

import java.util.*;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_WRCL;
import static org.opendaylight.util.net.EthernetType.IPv6;
import static org.opendaylight.util.net.ICMPv6Type.NBR_SOL;

/**
 * Uitlity methods for {@link MatchFieldCodecTest}.
 *
 * @author Shaila Shree
 * @author Simon Hunt
 */
public class MatchFieldCodecTestUtils extends AbstractCodecTest {

    private static final String AM_DUP = "Duplicate MatchField : ";
    private static final String AM_UNKNOWN_MF = "MatchField of unknown type : ";
    private static final String AM_BAD_VER_MF = "MatchField not supported in 1.0 type : ";
    private static final String AM_UNKNOWN_VER = "Action of protocol version : ";
    private static final String AM_UNSUP_VER = "Version not supported : ";

    private static final BigPortNumber EXP_IN_PORT = bpn(15);
    private static final BigPortNumber EXP_PHY_PORT = bpn(12);

    private static final long EXP_META_LONG_V = 0x01f2f3f4f5f6f7f8L;
    private static final long EXP_TUNNEL_LONG_V = 0x01f2f3f4f5f6f723L;
    private static final long EXP_LONG_M = 0x0f00ff00ff00ff00L;

    private static final MacAddress EXP_ETH_MAC_MASK = mac("ffffff:000001");
    private static final MacAddress EXP_ETH_DST_MAC = mac("665544:322211");
    private static final MacAddress EXP_ETH_SRC_MAC = mac("665555:322211");
    private static final MacAddress EXP_ARP_SHA_MAC = mac("622555:322211");
    private static final MacAddress EXP_ARP_THA_MAC = mac("655555:322211");
    private static final MacAddress EXP_IPV6_ND_SLL_MAC = mac("655522:322211");
    private static final MacAddress EXP_IPV6_ND_SLL__MAC = mac("655522:355211");

    private static final VlanId EXP_VLAN = VlanId.valueOf(42);

    private static final int EXP_VLAN_PCP = 5;
    private static final int EXP_IP_DSCP = 33;
    private static final int EXP_IP_ECN = 3;
    private static final int EXP_ICMP4CODE = 17;
    private static final int EXP_ICMP6CODE = 13;
    private static final int EXP_MPLS_TC = 4;
    private static final int EXP_MPLS_BOS = 1;
    private static final int EXP_ARP_OP = 123;
    private static final int EXP_IP6_FLABEL = 0x54413;
    private static final int EXP_MPLS_LABEL = 0xaaaaa;
    private static final int EXP_PBB_ISID_VALUE = 0x987654;

    private static final ICMPv6Type EXP_ICMP6_TYPE = NBR_SOL;

    private static final IpAddress EXP_ARP_IP = ip("15.255.124.13");
    private static final IpAddress EXP_IP4_SRC = ip("15.255.124.23");
    private static final IpAddress EXP_IP4_DST = ip("15.255.124.53");
    private static final IpAddress EXP_IP6_ADDR = ip("cafe::babe");
    private static final IpAddress EXP_ND_TARG = ip("2222::bad:b0b");

    private static final PortNumber EXP_TCP_SRC = pn(75);
    private static final PortNumber EXP_TCP_DST = pn(76);
    private static final PortNumber EXP_UDP_SRC = pn(77);
    private static final PortNumber EXP_UDP_DST = pn(78);
    private static final PortNumber EXP_SCTP_SRC = pn(79);
    private static final PortNumber EXP_SCTP_DST = pn(80);

    private static final ICMPv4Type EXP_ICMP4_TYPE = icmpv4Type(5);

    private static final Map<IPv6ExtHdr, Boolean> EXP_I6EH_FLAGS = new HashMap<>();
    static {
        EXP_I6EH_FLAGS.put(IPv6ExtHdr.AUTH, true);
        EXP_I6EH_FLAGS.put(IPv6ExtHdr.FRAG, false);
        EXP_I6EH_FLAGS.put(IPv6ExtHdr.ROUTER, true);
        EXP_I6EH_FLAGS.put(IPv6ExtHdr.HOP, false);
    }

    private static final int NUM_OF_V10_MATCH_FIELDS = 19;
    private static final int NUM_OF_V13_MATCH_FIELDS = 40;


    public static MatchField createMatchField() {
         return createBasicField(
                V_1_3,
                IPV4_DST,
                EXP_IP4_DST
        );
    }

    public static void verifyMatchField(MfbIpv4Dst matchField) {
        assertEquals(AM_NEQ, matchField.getVersion(), V_1_3);
        assertEquals(AM_NEQ, matchField.getFieldType(), IPV4_DST);
        assertEquals(AM_NEQ, matchField.getIpAddress(), EXP_IP4_DST);
    }

    public static Match createSampleMatchA(ProtocolVersion pv) {
        Match match = null;

        switch (pv) {
            case V_1_0:
                match = createSampleMatchV10A();
                break;

            case V_1_1:
            case V_1_2:
                fail(AM_UNSUP_VER + pv);
                break;

            case V_1_3:
                match = createSampleMatchV13A();
                break;
        }

        return match;
    }

    public static void verifySampleMatchA(ProtocolVersion pv, Match match) {
        switch (pv) {
            case V_1_0:
                verifySampleMatchV10A(match);
                break;

            case V_1_1:
            case V_1_2:
                fail(AM_UNSUP_VER + pv);
                break;

            case V_1_3:
                verifySampleMatchV13A(match);
                break;

            default:
                fail(AM_UNKNOWN_VER + pv);
        }
    }

    public static Match createSampleMatchB(ProtocolVersion pv) {
        Match match = null;

        switch (pv) {
            case V_1_0:
                match = createSampleMatchV10B();
                break;

            case V_1_1:
            case V_1_2:
                fail(AM_UNSUP_VER + pv);
                break;

            case V_1_3:
                match = createSampleMatchV13B();
                break;
        }

        return match;
    }

    public static void verifySampleMatchB(ProtocolVersion pv, Match match) {
        switch (pv) {
            case V_1_0:
                verifySampleMatchV10B(match);
                break;

            case V_1_1:
            case V_1_2:
                fail(AM_UNSUP_VER + pv);
                break;

            case V_1_3:
                verifySampleMatchV13B(match);
                break;

            default:
                fail(AM_UNKNOWN_VER + pv);
        }
    }

    private static Match createSampleMatchV10A() {
        MutableMatch mm = MatchFactory.createMatch(V_1_0);

        mm.addField(createBasicField(V_1_0, ETH_SRC, EXP_ETH_SRC_MAC,
                EXP_ETH_MAC_MASK));
        mm.addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv6));

        return (Match) mm.toImmutable();
    }

    private static void verifySampleMatchV10A(Match match) {
        List<MatchField> matchFields = match.getMatchFields();
        Set<OxmBasicFieldType> hit = new HashSet<>();

        assertEquals(AM_NEQ, 2, matchFields.size());

        for (MatchField matchField: matchFields) {
            OxmBasicFieldType type = (OxmBasicFieldType)
                    matchField.getFieldType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_0, matchField.getVersion());

            switch (type) {
                case ETH_SRC:
                    assertTrue(AM_WRCL, matchField instanceof MfbEthSrc);
                    validateMac(matchField, EXP_ETH_SRC_MAC, EXP_ETH_MAC_MASK);
                    break;
                case ETH_TYPE:
                    assertTrue(AM_WRCL, matchField instanceof MfbEthType);
                    validateEthType(matchField, IPv6);
                    break;
                default:
                    fail(AM_UNKNOWN_MF + type.name());
                    break;
            }
        }
    }

    private static Match createSampleMatchV10B() {
        MutableMatch mm = MatchFactory.createMatch(V_1_0);

        mm.addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv6));
        mm.addField(createBasicField(V_1_0, IP_PROTO, IpProtocol.TCP));
        mm.addField(createBasicField(V_1_0, TCP_DST, EXP_TCP_DST));

        return (Match) mm.toImmutable();
    }

    private static void verifySampleMatchV10B(Match match) {
        List<MatchField> matchFields = match.getMatchFields();
        Set<OxmBasicFieldType> hit = new HashSet<>();

        assertEquals(AM_NEQ, 3, matchFields.size());

        for (MatchField matchField: matchFields) {
            OxmBasicFieldType type = (OxmBasicFieldType)
                    matchField.getFieldType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_0, matchField.getVersion());

            switch (type) {
                case ETH_TYPE:
                    assertTrue(AM_WRCL, matchField instanceof MfbEthType);
                    validateEthType(matchField, IPv6);
                    break;
                case IP_PROTO:
                    assertTrue(AM_WRCL, matchField instanceof MfbIpProto);
                    validateProto(matchField, IpProtocol.TCP);
                    break;
                case TCP_DST:
                    assertTrue(AM_WRCL, matchField instanceof MfbTcpDst);
                    validatePort(matchField, EXP_TCP_DST);
                    break;
                default:
                    fail(AM_UNKNOWN_MF + type.name());
                    break;
            }
        }
    }

    private static Match createSampleMatchV13A() {
        MutableMatch mm = MatchFactory.createMatch(V_1_3);

        mm.addField(createBasicField(V_1_3, METADATA,
                EXP_META_LONG_V, EXP_LONG_M));
        mm.addField(createBasicField(V_1_3, TUNNEL_ID,
                EXP_TUNNEL_LONG_V, EXP_LONG_M));

        return (Match) mm.toImmutable();
    }


    private static void verifySampleMatchV13A(Match match) {
        List<MatchField> matchFields = match.getMatchFields();
        Set<OxmBasicFieldType> hit = new HashSet<>();

        assertEquals(AM_NEQ, 2, matchFields.size());

        for (MatchField matchField: matchFields) {
            OxmBasicFieldType type = (OxmBasicFieldType)
                    matchField.getFieldType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, matchField.getVersion());

            switch (type) {
                case METADATA:
                    assertTrue(AM_WRCL, matchField instanceof MfbMetadata);
                    validateLong(matchField, EXP_META_LONG_V, EXP_LONG_M);
                    break;
                case TUNNEL_ID:
                    assertTrue(AM_WRCL, matchField instanceof MfbTunnelId);
                    validateLong(matchField, EXP_TUNNEL_LONG_V, EXP_LONG_M);
                    break;
                default:
                    fail(AM_UNKNOWN_MF + type.name());
                    break;
            }
        }
    }

    private static Match createSampleMatchV13B() {
        MutableMatch mm = MatchFactory.createMatch(V_1_3);

        mm.addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv6));
        mm.addField(createBasicField(V_1_3, VLAN_VID, EXP_VLAN));
        mm.addField(createBasicField(V_1_3, VLAN_PCP, EXP_VLAN_PCP));
        mm.addField(createBasicField(V_1_3, IP_DSCP, EXP_IP_DSCP));

        return (Match) mm.toImmutable();
    }

    private static void verifySampleMatchV13B(Match match) {
        List<MatchField> matchFields = match.getMatchFields();
        Set<OxmBasicFieldType> hit = new HashSet<>();

        assertEquals(AM_NEQ, 4, matchFields.size());

        for (MatchField matchField: matchFields) {
            OxmBasicFieldType type = (OxmBasicFieldType)
                    matchField.getFieldType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, matchField.getVersion());

            switch (type) {
                case ETH_TYPE:
                    assertTrue(AM_WRCL, matchField instanceof MfbEthType);
                    validateEthType(matchField, IPv6);
                    break;
                case VLAN_VID:
                    assertTrue(AM_WRCL, matchField instanceof MfbVlanVid);
                    validateVlanID(matchField, EXP_VLAN);
                    break;
                case VLAN_PCP:
                    assertTrue(AM_WRCL, matchField instanceof MfbVlanPcp);
                    validateInt(matchField, EXP_VLAN_PCP);
                    break;
                case IP_DSCP:
                    assertTrue(AM_WRCL, matchField instanceof MfbIpDscp);
                    validateInt(matchField, EXP_IP_DSCP);
                    break;
                default:
                    fail(AM_UNKNOWN_MF + type.name());
                    break;
            }
        }
    }

    public static List<MatchField> createMatchFields(ProtocolVersion version) {
        List<MatchField> matchFields = null;
        switch (version) {
            case V_1_0:
                matchFields = createMatchFieldsV10();
                break;

            case V_1_1:
            case V_1_2:
                fail(AM_UNSUP_VER + version);
                break;

            case V_1_3:
                matchFields = createMatchFieldsV13();
                break;
        }

        return matchFields;
    }

    public static void verifyMatchFields(ProtocolVersion version,
                                         List<MatchField> matchFields) {
        switch (version) {
            case V_1_0:
                verifyMatchFieldsV10(matchFields);
                break;

            case V_1_1:
            case V_1_2:
                fail(AM_UNSUP_VER + version);
                break;

            case V_1_3:
                verifyMatchFieldsV13(matchFields);
                break;
        }
    }

    private static List<MatchField> createMatchFieldsV10() {
        List<MatchField> matchFields = new ArrayList<>();

        matchFields.add(createBasicField(V_1_0, IN_PORT, EXP_IN_PORT));
        matchFields.add(createBasicField(V_1_0, ETH_DST,
                EXP_ETH_DST_MAC, EXP_ETH_MAC_MASK));
        matchFields.add(createBasicField(V_1_0, ETH_SRC,
                EXP_ETH_SRC_MAC, EXP_ETH_MAC_MASK));
        matchFields.add(createBasicField(V_1_0, ETH_TYPE, IPv6));
        matchFields.add(createBasicField(V_1_0, VLAN_VID, EXP_VLAN));
        matchFields.add(createBasicField(V_1_0, VLAN_PCP, EXP_VLAN_PCP));
        matchFields.add(createBasicField(V_1_0, IP_DSCP, EXP_IP_DSCP));
        matchFields.add(createBasicField(V_1_0, IP_PROTO, IpProtocol.UDP));
        matchFields.add(createBasicField(V_1_0, IPV4_SRC, EXP_IP4_SRC));
        matchFields.add(createBasicField(V_1_0, IPV4_DST, EXP_IP4_DST));
        matchFields.add(createBasicField(V_1_0, TCP_SRC, EXP_TCP_SRC));
        matchFields.add(createBasicField(V_1_0, TCP_DST, EXP_TCP_DST));
        matchFields.add(createBasicField(V_1_0, UDP_SRC, EXP_UDP_SRC));
        matchFields.add(createBasicField(V_1_0, UDP_DST, EXP_UDP_DST));
        matchFields.add(createBasicField(V_1_0, SCTP_SRC, EXP_SCTP_SRC));
        matchFields.add(createBasicField(V_1_0, SCTP_DST, EXP_SCTP_DST));
        matchFields.add(createBasicField(V_1_0, ICMPV4_TYPE, EXP_ICMP4_TYPE));
        matchFields.add(createBasicField(V_1_0, ICMPV4_CODE, EXP_ICMP4CODE));
        matchFields.add(createBasicField(V_1_0, ARP_OP, EXP_ARP_OP));

        return matchFields;
    }

    private static void verifyMatchFieldsV10(List<MatchField> mfList) {
        Set<OxmBasicFieldType> hit = new HashSet<>();

        assertEquals(AM_NEQ, NUM_OF_V10_MATCH_FIELDS, mfList.size());

        for (MatchField mf: mfList) {
            OxmBasicFieldType type = (OxmBasicFieldType) mf.getFieldType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_0, mf.getVersion());

            switch (type) {
                case IN_PORT:
                    assertTrue(AM_WRCL, mf instanceof MfbInPort);
                    validateBigPort(mf, EXP_IN_PORT);
                    break;
                case ETH_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbEthDst);
                    validateMac(mf, EXP_ETH_DST_MAC, EXP_ETH_MAC_MASK);
                    break;
                case ETH_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbEthSrc);
                    validateMac(mf, EXP_ETH_SRC_MAC, EXP_ETH_MAC_MASK);
                    break;
                case ETH_TYPE:
                    assertTrue(AM_WRCL, mf instanceof MfbEthType);
                    validateEthType(mf, IPv6);
                    break;
                case VLAN_VID:
                    assertTrue(AM_WRCL, mf instanceof MfbVlanVid);
                    validateVlanID(mf, EXP_VLAN);
                    break;
                case VLAN_PCP:
                    assertTrue(AM_WRCL, mf instanceof MfbVlanPcp);
                    validateInt(mf, EXP_VLAN_PCP);
                    break;
                case IP_DSCP:
                    assertTrue(AM_WRCL, mf instanceof MfbIpDscp);
                    validateInt(mf, EXP_IP_DSCP);
                    break;
                case IP_PROTO:
                    assertTrue(AM_WRCL, mf instanceof MfbIpProto);
                    validateProto(mf, IpProtocol.UDP);
                    break;
                case IPV4_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv4Src);
                    validateIP(mf, EXP_IP4_SRC);
                    break;
                case IPV4_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv4Dst);
                    validateIP(mf, EXP_IP4_DST);
                    break;
                case TCP_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbTcpSrc);
                    validatePort(mf, EXP_TCP_SRC);
                    break;
                case TCP_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbTcpDst);
                    validatePort(mf, EXP_TCP_DST);
                    break;
                case UDP_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbUdpSrc);
                    validatePort(mf, EXP_UDP_SRC);
                    break;
                case UDP_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbUdpDst);
                    validatePort(mf, EXP_UDP_DST);
                    break;
                case SCTP_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbSctpSrc);
                    validatePort(mf, EXP_SCTP_SRC);
                    break;
                case SCTP_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbSctpDst);
                    validatePort(mf, EXP_SCTP_DST);
                    break;
                case ICMPV4_TYPE:
                    assertTrue(AM_WRCL, mf instanceof MfbIcmpv4Type);
                    validateICMP4Type(mf, EXP_ICMP4_TYPE);
                    break;
                case ICMPV4_CODE:
                    assertTrue(AM_WRCL, mf instanceof MfbIcmpv4Code);
                    validateInt(mf, EXP_ICMP4CODE);
                    break;
                case ARP_OP:
                    assertTrue(AM_WRCL, mf instanceof MfbArpOp);
                    validateInt(mf, EXP_ARP_OP);
                    break;

                //=====================================
                // below were not defined in OF 1.0 ...
                case IN_PHY_PORT:
                case METADATA:
                case IP_ECN:
                case ARP_SPA:
                case ARP_TPA:
                case ARP_SHA:
                case ARP_THA:
                case IPV6_SRC:
                case IPV6_DST:
                case IPV6_FLABEL:
                case ICMPV6_TYPE:
                case ICMPV6_CODE:
                case IPV6_ND_TARGET:
                case IPV6_ND_SLL:
                case IPV6_ND_TLL:
                case MPLS_LABEL:
                case MPLS_TC:
                case MPLS_BOS:
                case PBB_ISID:
                case TUNNEL_ID:
                case IPV6_EXTHDR:
                    fail(AM_BAD_VER_MF + type.name());
                    break;

                default:
                    fail(AM_UNKNOWN_MF + type.name());
                    break;
            }
        }
    }

    private static List<MatchField> createMatchFieldsV13() {
        List<MatchField> mfs = new ArrayList<>();

        mfs.add(createBasicField(V_1_3, IN_PORT, EXP_IN_PORT));
        mfs.add(createBasicField(V_1_3, IN_PHY_PORT, EXP_PHY_PORT));
        mfs.add(createBasicField(V_1_3, METADATA,
                EXP_META_LONG_V, EXP_LONG_M));
        mfs.add(createBasicField(V_1_3, TUNNEL_ID,
                EXP_TUNNEL_LONG_V, EXP_LONG_M));
        mfs.add(createBasicField(V_1_3, ETH_DST,
                EXP_ETH_DST_MAC, EXP_ETH_MAC_MASK));
        mfs.add(createBasicField(V_1_3, ETH_SRC,
                EXP_ETH_SRC_MAC, EXP_ETH_MAC_MASK));
        mfs.add(createBasicField(V_1_3, ETH_TYPE, IPv6));
        mfs.add(createBasicField(V_1_3, ARP_SHA,
                EXP_ARP_SHA_MAC, EXP_ETH_MAC_MASK));
        mfs.add(createBasicField(V_1_3, ARP_THA,
                EXP_ARP_THA_MAC, EXP_ETH_MAC_MASK));
        mfs.add(createBasicField(V_1_3, IP_PROTO, IpProtocol.UDP));
        mfs.add(createBasicField(V_1_3, ICMPV6_TYPE, NBR_SOL));
        mfs.add(createBasicField(V_1_3, IPV6_ND_SLL,
                EXP_IPV6_ND_SLL_MAC, EXP_ETH_MAC_MASK));
        mfs.add(createBasicField(V_1_3, IPV6_ND_TLL,
                EXP_IPV6_ND_SLL__MAC, EXP_ETH_MAC_MASK));
        mfs.add(createBasicField(V_1_3, VLAN_VID, EXP_VLAN));
        mfs.add(createBasicField(V_1_3, VLAN_PCP, EXP_VLAN_PCP));
        mfs.add(createBasicField(V_1_3, IP_DSCP, EXP_IP_DSCP));
        mfs.add(createBasicField(V_1_3, IP_ECN, EXP_IP_ECN));
        mfs.add(createBasicField(V_1_3, ICMPV4_CODE, EXP_ICMP4CODE));
        mfs.add(createBasicField(V_1_3, ICMPV6_CODE, EXP_ICMP6CODE));
        mfs.add(createBasicField(V_1_3, MPLS_TC, EXP_MPLS_TC));
        mfs.add(createBasicField(V_1_3, MPLS_BOS, EXP_MPLS_BOS));
        mfs.add(createBasicField(V_1_3, ARP_OP, EXP_ARP_OP));
        mfs.add(createBasicField(V_1_3, IPV6_FLABEL, EXP_IP6_FLABEL));
        mfs.add(createBasicField(V_1_3, MPLS_LABEL, EXP_MPLS_LABEL));
        mfs.add(createBasicField(V_1_3, PBB_ISID, EXP_PBB_ISID_VALUE));
        mfs.add(createBasicField(V_1_3, IPV4_SRC, EXP_IP4_SRC));
        mfs.add(createBasicField(V_1_3, IPV4_DST, EXP_IP4_DST));
        mfs.add(createBasicField(V_1_3, ARP_SPA, EXP_ARP_IP));
        mfs.add(createBasicField(V_1_3, ARP_TPA, EXP_ARP_IP));
        mfs.add(createBasicField(V_1_3, IPV6_SRC, EXP_IP6_ADDR));
        mfs.add(createBasicField(V_1_3, IPV6_DST, EXP_IP6_ADDR));
        mfs.add(createBasicField(V_1_3, IPV6_ND_TARGET, EXP_ND_TARG));
        mfs.add(createBasicField(V_1_3, TCP_SRC, EXP_TCP_SRC));
        mfs.add(createBasicField(V_1_3, TCP_DST, EXP_TCP_DST));
        mfs.add(createBasicField(V_1_3, UDP_SRC, EXP_UDP_SRC));
        mfs.add(createBasicField(V_1_3, UDP_DST, EXP_UDP_DST));
        mfs.add(createBasicField(V_1_3, SCTP_SRC, EXP_SCTP_SRC));
        mfs.add(createBasicField(V_1_3, SCTP_DST, EXP_SCTP_DST));
        mfs.add(createBasicField(V_1_3, ICMPV4_TYPE, EXP_ICMP4_TYPE));
        mfs.add(createBasicField(V_1_3, IPV6_EXTHDR, EXP_I6EH_FLAGS));

        return mfs;
    }

    private static void verifyMatchFieldsV13(List<MatchField> mfList) {
        Set<OxmBasicFieldType> hit = new HashSet<>();

        assertEquals(AM_NEQ, NUM_OF_V13_MATCH_FIELDS, mfList.size());

        for (MatchField mf: mfList) {
            OxmBasicFieldType type = (OxmBasicFieldType)
                    mf.getFieldType();

            if (hit.contains(type))
                fail(AM_DUP + type.name());

            hit.add(type);

            assertEquals(AM_NEQ, V_1_3, mf.getVersion());

            switch (type) {
                case IN_PORT:
                    assertTrue(AM_WRCL, mf instanceof MfbInPort);
                    validateBigPort(mf, EXP_IN_PORT);
                    break;
                case IN_PHY_PORT:
                    assertTrue(AM_WRCL, mf instanceof MfbInPhyPort);
                    validateBigPort(mf, EXP_PHY_PORT);
                    break;
                case METADATA:
                    assertTrue(AM_WRCL, mf instanceof MfbMetadata);
                    validateLong(mf, EXP_META_LONG_V, EXP_LONG_M);
                    break;
                case ETH_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbEthDst);
                    validateMac(mf, EXP_ETH_DST_MAC, EXP_ETH_MAC_MASK);
                    break;
                case ETH_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbEthSrc);
                    validateMac(mf, EXP_ETH_SRC_MAC, EXP_ETH_MAC_MASK);
                    break;
                case ETH_TYPE:
                    assertTrue(AM_WRCL, mf instanceof MfbEthType);
                    validateEthType(mf, IPv6);
                    break;
                case VLAN_VID:
                    assertTrue(AM_WRCL, mf instanceof MfbVlanVid);
                    validateVlanID(mf, EXP_VLAN);
                    break;
                case VLAN_PCP:
                    assertTrue(AM_WRCL, mf instanceof MfbVlanPcp);
                    validateInt(mf, EXP_VLAN_PCP);
                    break;
                case IP_DSCP:
                    assertTrue(AM_WRCL, mf instanceof MfbIpDscp);
                    validateInt(mf, EXP_IP_DSCP);
                    break;
                case IP_ECN:
                    assertTrue(AM_WRCL, mf instanceof MfbIpEcn);
                    validateInt(mf, EXP_IP_ECN);
                    break;
                case IP_PROTO:
                    assertTrue(AM_WRCL, mf instanceof MfbIpProto);
                    validateProto(mf, IpProtocol.UDP);
                    break;
                case IPV4_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv4Src);
                    validateIP(mf, EXP_IP4_SRC);
                    break;
                case IPV4_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv4Dst);
                    validateIP(mf, EXP_IP4_DST);
                    break;
                case TCP_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbTcpSrc);
                    validatePort(mf, EXP_TCP_SRC);
                    break;
                case TCP_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbTcpDst);
                    validatePort(mf, EXP_TCP_DST);
                    break;
                case UDP_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbUdpSrc);
                    validatePort(mf, EXP_UDP_SRC);
                    break;
                case UDP_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbUdpDst);
                    validatePort(mf, EXP_UDP_DST);
                    break;
                case SCTP_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbSctpSrc);
                    validatePort(mf, EXP_SCTP_SRC);
                    break;
                case SCTP_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbSctpDst);
                    validatePort(mf, EXP_SCTP_DST);
                    break;
                case ICMPV4_TYPE:
                    assertTrue(AM_WRCL, mf instanceof MfbIcmpv4Type);
                    validateICMP4Type(mf, EXP_ICMP4_TYPE);
                    break;
                case ICMPV4_CODE:
                    assertTrue(AM_WRCL, mf instanceof MfbIcmpv4Code);
                    validateInt(mf, EXP_ICMP4CODE);
                    break;
                case ARP_OP:
                    assertTrue(AM_WRCL, mf instanceof MfbArpOp);
                    validateInt(mf, EXP_ARP_OP);
                    break;
                case ARP_SPA:
                    assertTrue(AM_WRCL, mf instanceof MfbArpSpa);
                    validateIP(mf, EXP_ARP_IP);
                    break;
                case ARP_TPA:
                    assertTrue(AM_WRCL, mf instanceof MfbArpTpa);
                    validateIP(mf, EXP_ARP_IP);
                    break;
                case ARP_SHA:
                    assertTrue(AM_WRCL, mf instanceof MfbArpSha);
                    validateMac(mf, EXP_ARP_SHA_MAC, EXP_ETH_MAC_MASK);
                    break;
                case ARP_THA:
                    assertTrue(AM_WRCL, mf instanceof MfbArpTha);
                    validateMac(mf, EXP_ARP_THA_MAC, EXP_ETH_MAC_MASK);
                    break;
                case IPV6_SRC:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6Src);
                    validateIP(mf, EXP_IP6_ADDR);
                    break;
                case IPV6_DST:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6Dst);
                    validateIP(mf, EXP_IP6_ADDR);
                    break;
                case IPV6_FLABEL:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6Flabel);
                    validateInt(mf, EXP_IP6_FLABEL);
                    break;
                case ICMPV6_TYPE:
                    assertTrue(AM_WRCL, mf instanceof MfbIcmpv6Type);
                    validateICMP6Type(mf, EXP_ICMP6_TYPE);
                    break;
                case ICMPV6_CODE:
                    assertTrue(AM_WRCL, mf instanceof MfbIcmpv6Code);
                    validateInt(mf, EXP_ICMP6CODE);
                    break;
                case IPV6_ND_TARGET:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6NdTarget);
                    validateIP(mf, EXP_ND_TARG);
                    break;
                case IPV6_ND_SLL:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6NdSll);
                    validateMac(mf, EXP_IPV6_ND_SLL_MAC, EXP_ETH_MAC_MASK);
                    break;
                case IPV6_ND_TLL:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6NdTll);
                    validateMac(mf, EXP_IPV6_ND_SLL__MAC, EXP_ETH_MAC_MASK);
                    break;
                case MPLS_LABEL:
                    assertTrue(AM_WRCL, mf instanceof MfbMplsLabel);
                    validateInt(mf, EXP_MPLS_LABEL);
                    break;
                case MPLS_TC:
                    assertTrue(AM_WRCL, mf instanceof MfbMplsTc);
                    validateInt(mf, EXP_MPLS_TC);
                    break;
                case MPLS_BOS:
                    assertTrue(AM_WRCL, mf instanceof MfbMplsBos);
                    validateInt(mf, EXP_MPLS_BOS);
                    break;
                case PBB_ISID:
                    assertTrue(AM_WRCL, mf instanceof MfbPbbIsid);
                    validateInt(mf, EXP_PBB_ISID_VALUE);
                    break;
                case TUNNEL_ID:
                    assertTrue(AM_WRCL, mf instanceof MfbTunnelId);
                    validateLong(mf, EXP_TUNNEL_LONG_V, EXP_LONG_M);
                    break;
                case IPV6_EXTHDR:
                    assertTrue(AM_WRCL, mf instanceof MfbIpv6Exthdr);
                    validateIPV6ExtHdr(mf, EXP_I6EH_FLAGS);
                    break;

                default:
                    fail(AM_UNKNOWN_MF + type.name());
                    break;
            }
        }
    }

    private static void validateBigPort(MatchField matchField,
                                        BigPortNumber bigPortNumber) {
        MFieldBasicBigPort bigPort = (MFieldBasicBigPort) matchField;
        assertEquals(AM_NEQ, bigPortNumber, bigPort.getPort());
    }

    private static void validateLong(MatchField matchField,
                                     long value, long mask) {
        MFieldBasicLong basicLong = (MFieldBasicLong) matchField;
        assertEquals(AM_NEQ, value, basicLong.getValue());
        assertEquals(AM_NEQ, mask, basicLong.getMask());
    }

    private static void validateMac(MatchField matchField,
                                    MacAddress address, MacAddress mask) {
        MFieldBasicMac basicMac = (MFieldBasicMac) matchField;
        assertEquals(AM_NEQ, address, basicMac.getMacAddress());
        assertEquals(AM_NEQ, mask, basicMac.getMask());
    }

    private static void validateEthType(MatchField matchField,
                                        EthernetType type) {
        MfbEthType ethType = (MfbEthType) matchField;
        assertEquals(AM_NEQ, type, ethType.getEthernetType());
    }

    private static void validateVlanID(MatchField matchField, VlanId vlanId) {
        MfbVlanVid mf = (MfbVlanVid) matchField;
        assertEquals(AM_NEQ, vlanId, mf.getVlanId());
    }

    private static void validateInt(MatchField matchField, int value) {
        MFieldBasicInt basicInt = (MFieldBasicInt) matchField;
        assertEquals(AM_NEQ, value, basicInt.getValue());
    }

    private static void validateProto(MatchField matchField,
                                      IpProtocol protocol) {
        MfbIpProto ipProto = (MfbIpProto) matchField;
        assertEquals(AM_NEQ, protocol, ipProto.getIpProtocol());
    }

    private static void validateIP(MatchField matchField, IpAddress address) {
        MFieldBasicIp basicIp = (MFieldBasicIp) matchField;
        assertEquals(AM_NEQ, address, basicIp.getIpAddress());
    }

    private static void validatePort(MatchField matchField,
                                     PortNumber number) {
        MFieldBasicPort basicPort = (MFieldBasicPort) matchField;
        assertEquals(AM_NEQ, number, basicPort.getPort());
    }

    private static void validateICMP4Type(MatchField matchField,
                                          ICMPv4Type type) {
        MfbIcmpv4Type icmpv4Type = (MfbIcmpv4Type) matchField;
        assertEquals(AM_NEQ, type, icmpv4Type.getICMPv4Type());
    }

    private static void validateICMP6Type(MatchField matchField,
                                          ICMPv6Type type) {
        MfbIcmpv6Type icmpv6Type = (MfbIcmpv6Type) matchField;
        assertEquals(AM_NEQ, type, icmpv6Type.getICMPv6Type());
    }

    private static void validateIPV6ExtHdr(MatchField matchField,
                                           Map<IPv6ExtHdr, Boolean> flags) {
        MfbIpv6Exthdr ipv6Exthdr = (MfbIpv6Exthdr) matchField;
        assertEquals(AM_NEQ, flags, ipv6Exthdr.getFlags());
    }
}

