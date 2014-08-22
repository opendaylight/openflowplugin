/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 *  ICMPv4 Message type (high byte) and code (low byte) combinations.
 *
 * @author Frank Wood
 */
public enum IcmpTypeCode implements ProtocolEnum {
    
    /** Echo reply (ping). */
    ECHO_REPLY(0x0000),
    
    /** Destination unreachable - Network. */
    DST_UNREACH_NET(0x0300),
    /** Destination unreachable - Host. */
    DST_UNREACH_HOST(0x0301),
    /** Destination unreachable - Protocol. */
    DST_UNREACH_PROTOCOL(0x0302),
    /** Destination unreachable - Port. */
    DST_UNREACH_PORT(0x0303),
    /** Destination unreachable - Fragment Required. */
    DST_UNREACH_FRAG_REQUIRED(0x0304),
    /** Destination unreachable - Route Failed. */
    DST_UNREACH_ROUTE_FAIL(0x0305),
    /** Destination unreachable - Network Unknown. */
    DST_UNREACH_NET_UNKNOWN(0x0306),
    /** Destination unreachable - Host Unknown. */
    DST_UNREACH_HOST_UNKNOWN(0x0307),
    /** Destination unreachable - Host Isolated. */
    DST_UNREACH_HOST_ISOLATED(0x0308),
    /** Destination unreachable - Network Administration Prohibited. */
    DST_UNREACH_NET_ADMIN_PROHIBIT(0x0309),
    /** Destination unreachable - Host Administration Prohibited. */
    DST_UNREACH_HOST_ADMIN_PROHIBIT(0x030a),
    /** Destination unreachable - Network TOS. */
    DST_UNREACH_NET_TOS(0x030b),
    /** Destination unreachable - Host TOS. */
    DST_UNREACH_HOST_TOS(0x030c),
    /** Destination unreachable - Communication Administrator Prohibited. */
    DST_UNREACH_COMM_ADMIN_PROHIBIT(0x030d),
    /** Destination unreachable - Host Precedence Violation. */
    DST_UNREACH_HOST_PRECED_VIOLATION(0x030e),
    /** Destination unreachable - Precedence Cutoff in Effect. */
    DST_UNREACH_PRECED_CUTOFF(0x030e),
    
    /** Redirect message - Datagram Network. */
    REDIRECT_MSG_DG_NET(0x0500),
    /** Redirect message - Datagram Host. */
    REDIRECT_MSG_DG_HOST(0x0501),
    /** Redirect message - TOS Host. */
    REDIRECT_MSG_TOS_NET(0x0502),
    /** Redirect message - TOS Host. */
    REDIRECT_MSG_TOS_HOST(0x0503),
    
    /** Echo request (ping). */
    ECHO_REQ(0x0800),
    
    /** Router advertise. */
    ROUTER_ADVERTISE(0x0900),
    /** Router solicit. */
    ROUTER_SOLICIT(0x0a00),
    
    /** Time exceeded - TTL. */
    TIME_EXCEED_TTL(0x0b00),
    /** Time exceeded - Fragment Re-assembly. */
    TIME_EXCEED_FRAG_REASS(0x0b01),
    
    /** Parameter problem - Pointer. */
    PARAM_PROB_PTR(0x0c00),
    /** Parameter problem - Missing Request Option. */
    PARAM_PROB_MISS_REQ_OPT(0x0c01),
    /** Parameter problem - Bad Length. */
    PARAM_PROB_MISS_BAD_LEN(0x0c02),
    
    /** Time stamp. */
    TIMESTAMP(0x0d00),
    /** Time stamp reply. */
    TIMESTAMP_REPLY(0x0e00),
    
    /** Address mask request. */
    ADDR_MASK_REQ(0x1100),
    /** Address mask reply. */
    ADDR_MASK_REPLY(0x1200),
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
    static IcmpTypeCode get(int typeCode) {
        return ProtocolUtils.getEnum(IcmpTypeCode.class, typeCode, IcmpTypeCode.ECHO_REQ);
    }
    
    private int typeCode;
    
    private IcmpTypeCode(int typeCode) {
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
     * Returns the ICMPv4 type code.
     * 
     * @return the type code
     */
    public int icmpType() {
        return TYPE_MASK & (typeCode >> TYPE_BIT_SHIFT);
    }

    /**
     * Returns the ICMPv4 code number
     * 
     * @return the code number
     */
    public int icmpCode() {
        return CODE_MASK & typeCode;
    }

}
