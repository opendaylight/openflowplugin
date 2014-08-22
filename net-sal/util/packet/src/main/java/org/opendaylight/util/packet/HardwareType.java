/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;


/**
 * Hardware types (used in ARP, DHCP, ...).
 * 
 * @author Frank Wood
 */
public enum HardwareType implements ProtocolEnum {
    
    /** No hardware type. */
    NONE(0),
    /** Ethernet2. */
    ETHERNET(1),
    /** Token Ring. */
    TOKEN_RING(4),
    /** IEEE 802 Networks. */
    IEEE802(6),
    /** ARCNeT. */
    ARCNET(7),
    /** Hyper Channel. */
    HYPER_CHANNEL(8),
    /** Frame Relay. */
    FRAME_RELAY(15),
    /** ATM. */
    ATM(16),
    /** HDLC. */
    HDLC(17),
    /** Fiber Channel. */
    FIBER_CHANNEL(18),
    /** ATM2 */
    ATM2(19),
    /** Serial line. */
    SERIAL(20),
    ;
    
    private int code;
    
    private HardwareType(int code) {
        this.code = code;
    }
    
    @Override
    public int code() {
        return code;
    }
    
    static HardwareType get(int code) {
        return ProtocolUtils.getEnum(HardwareType.class, code, NONE);
    }
}
