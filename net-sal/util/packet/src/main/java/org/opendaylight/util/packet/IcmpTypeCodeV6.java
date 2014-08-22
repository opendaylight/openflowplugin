/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * ICMPv6 Message type (high byte) and code (low byte) combinations.
 * 
 * @author Frank Wood
 */
public enum IcmpTypeCodeV6 implements ProtocolEnum {

    /** No type/code. */
    NONE(0x000),
    
    /** ICMPv6 Destination unreachable - no route to destination. */
    DST_UNREACH_NO_ROUTE_DST(0x0100),
    /**
     * ICMPv6 Destination unreachable - communication with destination
     * administratively prohibited.
     */
    DST_UNREACH_ADMIN_PROHIBIT(0x0101),
    /** ICMPv6 Destination unreachable - beyond scope of source address. */
    DST_UNREACH_BEYOND_SCOPE_SRC_ADDR(0x0102),
    /** ICMPv6 Destination unreachable - address unreachable. */
    DST_UNREACH_ADDR(0x0103),
    /** ICMPv6 Destination unreachable - port unreachable. */
    DST_UNREACH_PORT(0x0104),
    /**
     * ICMPv6 Destination unreachable - source address failed
     * ingress/egress policy.
     */
    DST_UNREACH_SRC_ADDR_FAIL_POLICY(0x0105),
    /** ICMPv6 Destination unreachable - reject route to destination. */
    DST_UNREACH_REJECT_ROUTE(0x0104),
    /** ICMPv6 Destination unreachable - error in source routing header. */
    DST_UNREACH_ERROR(0x0104),

    /** ICMPv6 Packet too big. */
    PKT_TOO_BIG(0x0200),

    /** ICMPv6 Time exceeded - hop limit exceeded in transit. */
    TIME_EXCEED_HOP_LIMIT(0x0300),
    /** ICMPv6 Time exceeded - fragment re-assembly time exceeded. */
    TIME_EXCEED_FRAG_REASS(0x0301),
    
    /** ICMPv6 Parameter problem - error header field. */
    PARAM_PROB_ERR_HDR_FIELD(0x0400),
    /** ICMPv6 Parameter problem - unknown next header type. */
    PARAM_PROB_UNK_NEXT_HDR(0x0401),
    /** ICMPv6 Parameter problem - unknown IPv6 option. */
    PARAM_PROB_UNK_IPV6_OPTION(0x0402),

    /** ICMPv6 Echo request. */
    ECHO_REQ(0x08000),
    /** ICMPv6 Echo reply. */
    ECHO_REPLY(0x08100),
    
    /** ICMPv6 Multicast listener query. */
    MULTICAST_LISTENER_QUERY(0x08200),
    /** ICMPv6 Multicast listener report. */
    MULTICAST_LISTENER_REPORT(0x08300),
    /** ICMPv6 Multicast listener report. */
    MULTICAST_LISTENER_DONE(0x08400),
    
    /** ICMPv6 Router solicitation (Neighbor Discovery Protocol). */
    ROUTER_SOLICT_NDP(0x08500),
    /** ICMPv6 Router advertisement (Neighbor Discovery Protocol). */
    ROUTER_ADVERTISE_NDP(0x08600),
    /** ICMPv6 Neighbor solicitation (Neighbor Discovery Protocol). */
    NEIGHBOR_SOLICIT_NDP(0x08700),
    /** ICMPv6 Neighbor advertisement (Neighbor Discovery Protocol). */
    NEIGHBOR_ADVERTISE_NDP(0x08800),
    /** ICMPv6 Redirect message (Neighbor Discovery Protocol). */
    REDIRECT_MSG_NDP(0x08900),
    
    /** ICMPv6 Router renumbering - command. */
    ROUTER_RENUM_CMD(0x08a00),
    /** ICMPv6 Router renumbering - result. */
    ROUTER_RENUM_RESULT(0x08a01),
    /** ICMPv6 Router renumbering - sequence number reset. */
    ROUTER_RENUM_RESET(0x08aff),

    /** ICMPv6 Node information query - IPv6 address. */
    NIQ_V6_ADDR(0x08b00),
    /** ICMPv6 Node information query - name. */
    NIQ_NAME(0x08b01),
    /** ICMPv6 Node information query - IPv4 address. */
    NIQ_V4_ADDR(0x08b02),
    
    /** ICMPv6 Node information response - successful reply. */
    NIR_REPLY_OK(0x08c00),
    /** ICMPv6 Node information response - responder refuses to answer. */
    NIR_REFUSE(0x08c01),
    /** ICMPv6 Node information response - query unknown. */
    NIR_UNK(0x08c02),
    
    /** ICMPv6 Inverse neighbor discovery solicitation Message. */
    INV_NEIGHBOR_SOLICIT_NDP(0x08d00),
    /** ICMPv6 Inverse neighbor advertise solicitation Message. */
    INV_NEIGHBOR_ADVERTISE_NDP(0x08e00),
    
    /** ICMPv6 Inverse neighbor advertise solicitation message. */
    MULTICAST_LISTENER_DISCO_REPORTS(0x08f00),

    /** ICMPv6 Home agent address discovery request message. */
    HOME_AGENT_DISCO_REQ(0x09000),
    /** ICMPv6 Home agent address discovery reply message. */
    HOME_AGENT_DISCO_REPLY(0x09100),

    /** ICMPv6 Mobile prefix solicitation. */
    MOBILE_PREFIX_SOLICIT(0x09200),
    /** ICMPv6 Mobile prefix advertisement. */
    MOBILE_PREFIX_ADVERTISE(0x09300),
    
    /** ICMPv6 Certification path solicitation (SEND). */
    CERT_PATH_SOLICIT(0x09400),
    /** ICMPv6 Certification path advertisement (SEND). */
    CERT_PATH_ADVERTISE(0x09500),
    
    /** ICMPv6 Multicast router advertisement (MRD). */
    MULTICAST_ROUTER_ADVERTISE(0x09700),
    /** ICMPv6 Multicast router advertisement (MRD). */
    MULTICAST_ROUTER_SOLICIT(0x09800),
    /** ICMPv6 Multicast router termination (MRD). */
    MULTICAST_ROUTER_TERM(0x09900),
    
    /** ICMPv6 RPL control message. */
    RPL_CTRL(0x09b00),
    ;
    
    private static final int TYPE_MASK = 0x0ff;
    private static final int CODE_MASK = 0x0ff;
    private static final int TYPE_BIT_SHIFT = 8;

    /**
     * Internally used to return the enumeration constant for the 2-byte
     * type/code.
     * 
     * @param typeCode 2-byte type/code
     * @return the enumeration constant
     */
    static IcmpTypeCodeV6 get(int typeCode) {
        return ProtocolUtils.getEnum(IcmpTypeCodeV6.class, typeCode, IcmpTypeCodeV6.NONE);
    }
    
    private int typeCode;
    
    private IcmpTypeCodeV6(int typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    public int code() {
        return typeCode;
    }
    
    /**
     * Internally used to return the combined 2-byte type/code for the
     * enumeration constant.
     * 
     * @return the combined 2-byte type/code
     */
    int typeCode() {
        return code();
    }
    
    /**
     * Returns the ICMPv6 type number.
     * 
     * @return the type number
     */
    int icmpType() {
        return TYPE_MASK & (typeCode >> TYPE_BIT_SHIFT);
    }

    /**
     * Returns the ICMPv6 code number
     * 
     * @return the code number
     */
    int icmpCode() {
        return CODE_MASK & typeCode;
    }
    
}
