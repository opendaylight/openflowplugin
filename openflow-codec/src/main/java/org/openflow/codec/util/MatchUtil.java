package org.openflow.codec.util;

import java.util.Map;

import org.openflow.codec.protocol.OFBMatchFields;
import org.openflow.codec.protocol.OFPMatch;
import org.openflow.codec.protocol.OXMField;

/**
 * utility class for match field
 *
 * @author AnilGujele
 *
 */

public class MatchUtil {

    // ofp_vlan_id constant
    private static int OFPVID_NONE = 0x0000; /* No VLAN id was set. */
    private static int OFPVID_PRESENT = 0x1000; /*
                                                 * Bit that indicate that a VLAN
                                                 * id is set
                                                 */

    // Ethernet type constant
    private static final int ETH_TYPE_IPV4 = 0x0800;
    private static final int ETH_TYPE_IPV6 = 0x86dd;
    private static final int ETH_TYPE_MPLS_UNICAST = 0x8847;
    private static final int ETH_TYPE_MPLS_MULITCAST = 0x8848;
    private static final int ETH_TYPE_ISID = 0x88E7;
    private static final int ETH_TYPE_ARP = 0x0806;
    // protocol type constant
    private static final short IP_PROTO_TCP = 6;
    private static final short IP_PROTO_UDP = 17;
    private static final short IP_PROTO_SCTP = 132;
    private static final short IP_PROTO_ICMP = 1;
    private static final short IP_PROTO_ICMPV6 = 58;
    // ICMPV6 type constant
    private static final short ICMPV6_TYPE_NDP_NS = 135;
    private static final short ICMPV6_TYPE_NDP_NA = 136;

    /**
     * to check if prerequisite is matching for all the field
     *
     * @param field
     * @param map
     * @return
     */
    public static boolean hasPrerequisite(OFPMatch match) {
        Map<String, OXMField> map = match.getMatchFieldMap();
        boolean result = true;
        for (Map.Entry<String, OXMField> entry : map.entrySet()) {
            OFBMatchFields field = entry.getValue().getMatchField();
            OXMField value;
            switch (field.getValue()) {
            case 1: // OXM_OF_IN_PHY_PORT
                result = (null != map.get(OFBMatchFields.IN_PORT.name()));
                break;
            case 7: // OXM_OF_VLAN_PCP
                value = map.get(OFBMatchFields.VLAN_VID.name());
                result = (null != value) && (U16.f(getShort(value.getData())) != MatchUtil.OFPVID_NONE);

            case 8: // OXM_OF_IP_DSCP
            case 9: // OXM_OF_IP_ECN
            case 10: // OXM_OF_IP_PROTO
                value = map.get(OFBMatchFields.ETH_TYPE.name());
                boolean isMatched = (null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_IPV4);
                result = isMatched
                        || ((null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_IPV6));
                break;
            case 11: // OXM_OF_IPV4_SRC
            case 12: // OXM_OF_IPV4_DST
                value = map.get(OFBMatchFields.ETH_TYPE.name());
                result = (null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_IPV4);
                break;
            case 13: // OXM_OF_TCP_SRC
            case 14: // OXM_OF_TCP_DST
                value = map.get(OFBMatchFields.IP_PROTO.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.IP_PROTO_TCP);
                break;
            case 15: // OXM_OF_UDP_SRC
            case 16: // OXM_OF_UDP_DST
                value = map.get(OFBMatchFields.IP_PROTO.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.IP_PROTO_UDP);
                break;
            case 17: // OXM_OF_SCTP_SRC
            case 18: // OXM_OF_SCTP_DST
                value = map.get(OFBMatchFields.IP_PROTO.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.IP_PROTO_SCTP);
                break;
            case 19: // OXM_OF_ICMPV4_TYPE
            case 20: // OXM_OF_ICMPV4_CODE
                value = map.get(OFBMatchFields.IP_PROTO.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.IP_PROTO_ICMP);
                break;
            case 21: // OXM_OF_ARP_OP
            case 22: // OXM_OF_ARP_SPA
            case 23: // OXM_OF_ARP_TPA
            case 24: // OXM_OF_ARP_SHA
            case 25: // OXM_OF_ARP_THA
                value = map.get(OFBMatchFields.ETH_TYPE.name());
                result = (null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_ARP);
                break;

            case 26: // OXM_OF_IPV6_SRC
            case 27: // OXM_OF_IPV6_DST
            case 28: // OXM_OF_IPV6_FLABEL
            case 39: // OXM_OF_IPV6_EXTHDR
                value = map.get(OFBMatchFields.ETH_TYPE.name());
                result = (null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_IPV6);
                break;
            case 29: // OXM_OF_ICMPV6_TYPE
            case 30: // OXM_OF_ICMPV6_CODE
                value = map.get(OFBMatchFields.IP_PROTO.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.IP_PROTO_ICMPV6);
                break;
            case 31: // OXM_OF_IPV6_ND_TARGET
                value = map.get(OFBMatchFields.ICMPV6_TYPE.name());
                boolean success = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.ICMPV6_TYPE_NDP_NS);
                result = success || ((null != value) && (U8.f(value.getData()[0]) == MatchUtil.ICMPV6_TYPE_NDP_NA));
                break;
            case 32: // OXM_OF_IPV6_ND_SLL
                value = map.get(OFBMatchFields.ICMPV6_TYPE.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.ICMPV6_TYPE_NDP_NS);
                break;
            case 33: // OXM_OF_IPV6_ND_TLL
                value = map.get(OFBMatchFields.ICMPV6_TYPE.name());
                result = (null != value) && (U8.f(value.getData()[0]) == MatchUtil.ICMPV6_TYPE_NDP_NA);
                break;
            case 34: // OXM_OF_MPLS_LABEL
            case 35: // OXM_OF_MPLS_TC
            case 36: // OXM_OF_MPLS_BOS
                value = map.get(OFBMatchFields.ETH_TYPE.name());
                boolean isSuccess = (null != value)
                        && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_MPLS_UNICAST);
                result = isSuccess
                        || ((null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_MPLS_MULITCAST));
                break;
            case 37: // OXM_OF_PBB_ISID
                value = map.get(OFBMatchFields.ETH_TYPE.name());
                result = (null != value) && (U16.f(getShort(value.getData())) == MatchUtil.ETH_TYPE_ISID);
                break;
            default:
                result = true;
                break;

            }
            if (!result) {
                break; // break for loop
            }
        }
        return result;
    }

    /**
     * get short value from byte array
     *
     * @param byteArray
     * @return
     */
    public static short getShort(byte[] byteArray) {
        return (short) ((byteArray[1] << 8) | (byteArray[0] & 0xff));
    }

}
