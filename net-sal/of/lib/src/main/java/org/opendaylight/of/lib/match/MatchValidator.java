/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.util.AbstractValidator;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.ValidationException;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.ICMPv6Type;
import org.opendaylight.util.net.IpProtocol;
import org.opendaylight.util.net.VlanId;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Validates a match structure to ensure:
 * <ul>
 *     <li>there is no more than one of each match field type</li>
 *     <li>the match field pre-requisites are correctly satisfied</li>
 * </ul>
 * Note that any fields that are not {@link OxmBasicFieldType Basic} fields
 * are assumed to have no pre-requisites, and are simply tested to ensure
 * they appear only once.
 * <p>
 * See Table 11 in section A.2.3.7 (p.44-45) of the OpenFlow 1.3 Specification
 * for more details.
 *
 * @author Simon Hunt
 */
class MatchValidator extends AbstractValidator {

    // package private for unit test access
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MatchValidator.class, "matchValidator");

    static final String E_DUP = RES.getString("e_dup");
    static final String REQ_IN_PORT = " requires IN_PORT present";
    static final String REQ_VLAN_VID_NOT_NONE =
            " requires VLAN_VID present and != NONE";
    static final String REQ_ETH_TYPE_IP4IP6 =
            " requires ETH_TYPE = 0x0800(IPv4) or 0x86dd(IPv6)";
    static final String REQ_ETH_TYPE_IP4 = " requires ETH_TYPE = 0x0800(IPv4)";
    static final String REQ_ETH_TYPE_IP6 = " requires ETH_TYPE = 0x86dd(IPv6)";
    static final String REQ_ETH_TYPE_ARP = " requires ETH_TYPE = 0x0806(ARP)";
    static final String REQ_ETH_TYPE_MPLS =
            " requires ETH_TYPE = 0x8847(MPLS_U) or 0x8848(MPLS_M)";
    static final String REQ_ETH_TYPE_PBB = " requires ETH_TYPE = 0x88e7(PBB)";
    static final String REQ_IP_PROTO_ICMP = " requires IP_PROTO = 1(ICMP)";
    static final String REQ_IP_PROTO_TCP = " requires IP_PROTO = 6(TCP)";
    static final String REQ_IP_PROTO_UDP = " requires IP_PROTO = 17(UDP)";
    static final String REQ_IP_PROTO_ICMPv6 = " requires IP_PROTO = 58(ICMPv6)";
    static final String REQ_IP_PROTO_SCTP = " requires IP_PROTO = 132(SCTP)";
    static final String REQ_ICMPV6_SOLADV =
            " requires ICMPV6_TYPE = 135(NBR_SOL) or 136(NBR_ADV)";
    static final String REQ_ICMPV6_SOL = " requires ICMPV6_TYPE = 135(NBR_SOL)";
    static final String REQ_ICMPV6_ADV = " requires ICMPV6_TYPE = 136(NBR_ADV)";


    /** Basic fields (which we are primarily interested in) can be keyed
     * off the OxmBasicFieldType enum.
     */
    private Map<OxmFieldType, MatchField> basicFields = new HashMap<>();

    /** All other fields are keyed off the rawOxmType */
    private Map<Integer, MatchField> otherFields = new HashMap<>();

    // no instantiation outside the class
    private MatchValidator() { }


    /** Do the validation.
     *
     * @param match the match to validate
     * @throws ValidationException if there is one or more validation issues.
     */
    private void validate(Match match) {
        for (MatchField mf: match.getMatchFields()) {
            switch (mf.getOxmClass()) {
                case OPENFLOW_BASIC:
                    if (basicFields.containsKey(mf.getFieldType()))
                        addError(E_DUP + mf.header);
                    else {
                        basicFields.put(mf.getFieldType(), mf);
                        checkPreRequisites(mf);
                    }
                    break;
                default:
                    if (otherFields.containsKey(mf.header.rawOxmType))
                        addError(E_DUP + mf.header);
                    else
                        otherFields.put(mf.header.rawOxmType, mf);
                    break;
            }
        }
        throwExceptionIfMessages();
    }


    /** Checks that the pre-requisites are satisfied for the given
     * match field. If they are not met, an error message is added
     * to the list.
     *
     * @param mf the matchfield to check
     */
    private void checkPreRequisites(MatchField mf) {
        /*
         * Implementation note:
         *    Case statement Fall-Through is intentional!
         *    If you have to modify this section, please take extra care!!
         */
        OxmBasicFieldType fieldType = (OxmBasicFieldType) mf.getFieldType();
        MatchField preReq;
        switch (fieldType) {

            case IN_PHY_PORT:
                // pre-req: IN_PORT present
                preReq = basicFields.get(OxmBasicFieldType.IN_PORT);
                if (preReq == null)
                    addError(fieldType + REQ_IN_PORT);
                break;

            case VLAN_PCP:
                // pre-req: VLAN_VID is present and is != NONE
                preReq = basicFields.get(OxmBasicFieldType.VLAN_VID);
                if (preReq == null)
                    addError(fieldType + REQ_VLAN_VID_NOT_NONE);
                else {
                    MfbVlanVid primp = (MfbVlanVid) preReq;
                    if (primp.getVlanId().equals(VlanId.NONE))
                        addError(fieldType + REQ_VLAN_VID_NOT_NONE);
                }
                break;

            case IP_DSCP:
            case IP_ECN:
            case IP_PROTO:
                // pre-req: ETH_TYPE == 0x0800(IPv4) or 0x86dd(IPv6)
                verifyEthType(fieldType, REQ_ETH_TYPE_IP4IP6,
                              EthernetType.IPv4, EthernetType.IPv6);
                break;

            case IPV4_SRC:
            case IPV4_DST:
                // pre-req: ETH_TYPE == 0x0800(IPv4)
                verifyEthType(fieldType, REQ_ETH_TYPE_IP4, EthernetType.IPv4);
                break;

            case TCP_SRC:
            case TCP_DST:
                // pre-req: IP_PROTO == 6(TCP)
                verifyIpProto(fieldType, REQ_IP_PROTO_TCP, IpProtocol.TCP);
                break;

            case UDP_SRC:
            case UDP_DST:
                // pre-req: IP_PROTO == 17(UDP)
                verifyIpProto(fieldType, REQ_IP_PROTO_UDP, IpProtocol.UDP);
                break;

            case SCTP_SRC:
            case SCTP_DST:
                // pre-req: IP_PROTO == 132(SCTP)
                verifyIpProto(fieldType, REQ_IP_PROTO_SCTP, IpProtocol.SCTP);
                break;

            case ICMPV4_TYPE:
            case ICMPV4_CODE:
                // pre-req: IP_PROTO == 1(ICMP)
                verifyIpProto(fieldType, REQ_IP_PROTO_ICMP, IpProtocol.ICMP);
                break;

            case ARP_OP:
            case ARP_SPA:
            case ARP_TPA:
            case ARP_SHA:
            case ARP_THA:
                // pre-req: ETH_TYPE == 0x0806(ARP)
                verifyEthType(fieldType, REQ_ETH_TYPE_ARP, EthernetType.ARP);
                break;

            case IPV6_SRC:
            case IPV6_DST:
            case IPV6_FLABEL:
            case IPV6_EXTHDR:
                // pre-req: ETH_TYPE == 0x86dd(IPv6)
                verifyEthType(fieldType, REQ_ETH_TYPE_IP6, EthernetType.IPv6);
                break;

            case ICMPV6_TYPE:
            case ICMPV6_CODE:
                // pre-req: IP_PROTO == 58(ICMPv6)
                verifyIpProto(fieldType, REQ_IP_PROTO_ICMPv6, IpProtocol.ICMPv6);
                break;

            case IPV6_ND_TARGET:
                // pre-req: ICMPV6_TYPE == 135(NBR_SOL) or 136(NBR_ADV)
                verifyIcmpv6Type(fieldType, REQ_ICMPV6_SOLADV,
                                 ICMPv6Type.NBR_SOL, ICMPv6Type.NBR_ADV);
                break;

            case IPV6_ND_SLL:
                // pre-req: ICMPV6_TYPE == 135(NBR_SOL)
                verifyIcmpv6Type(fieldType, REQ_ICMPV6_SOL, ICMPv6Type.NBR_SOL);
                break;

            case IPV6_ND_TLL:
                // pre-req: ICMPV6_TYPE == 136(NBR_ADV)
                verifyIcmpv6Type(fieldType, REQ_ICMPV6_ADV, ICMPv6Type.NBR_ADV);
                break;

            case MPLS_LABEL:
            case MPLS_TC:
            case MPLS_BOS:
                // pre-req: ETH_TYPE == 0x8847(MPLS_U) or 0x8848(MPLS_M)
                verifyEthType(fieldType, REQ_ETH_TYPE_MPLS,
                              EthernetType.MPLS_U, EthernetType.MPLS_M);
                break;

            case PBB_ISID:
                // pre-req: ETH_TYPE == 0x88e7(PBB)
                verifyEthType(fieldType, REQ_ETH_TYPE_PBB, EthernetType.PBB);
                break;

            default:
                // any other field type does not have pre-requisites
                break;
        }
    }

    /** Verifies that an ICMPv6_TYPE match field exists, and that its value
     * matches one of the specified parameters.
     *
     * @param dep the dependent field
     * @param err the error message if the check fails
     * @param values the values to check against
     */
    private void verifyIcmpv6Type(OxmBasicFieldType dep, String err,
                                  ICMPv6Type... values) {
        MatchField preReq = basicFields.get(OxmBasicFieldType.ICMPV6_TYPE);
        if (preReq == null)
            addError(dep + err);
        else {
            ICMPv6Type type = ((MfbIcmpv6Type) preReq).getICMPv6Type();
            boolean good = false;
            for (ICMPv6Type t: values)
                if (type == t)
                    good = true;
            if (!good)
                addError(dep + err);
        }
    }

    /** Verifies that an ETH_TYPE match field exists, and that its value
     * matches one of the specified parameters.
     *
     * @param dep the dependent field
     * @param err the error message if the check fails
     * @param values the values to check against
     */
    private void verifyEthType(OxmBasicFieldType dep, String err,
                               EthernetType... values) {
        MatchField preReq = basicFields.get(OxmBasicFieldType.ETH_TYPE);
        if (preReq == null)
            addError(dep + err);
        else {
            EthernetType et = ((MfbEthType) preReq).getEthernetType();
            boolean good = false;
            for (EthernetType t: values)
                if (et == t)
                    good = true;
            if (!good)
                addError(dep + err);
        }
    }

    /** Verifies that an IP_PROTO match field exists, and that its value
     * matches one of the specified parameters.
     *
     * @param dep the dependent field
     * @param err the error message if the check fails
     * @param values the values to check against
     */
    private void verifyIpProto(OxmBasicFieldType dep, String err,
                               IpProtocol... values) {
        MatchField preReq = basicFields.get(OxmBasicFieldType.IP_PROTO);
        if (preReq == null)
            addError(dep + err);
        else {
            IpProtocol ipp = ((MfbIpProto) preReq).getIpProtocol();
            boolean good = false;
            for (IpProtocol p: values)
                if (ipp == p)
                    good = true;
            if (!good)
                addError(dep + err);
        }
    }


    /** Validates a match structure.
     *
     * @param match the match to validate
     * @throws org.opendaylight.util.ValidationException if issues were found
     * @throws NullPointerException if parameter is null
     */
    static void validateMatch(Match match) {
        new MatchValidator().validate(match);
    }
}