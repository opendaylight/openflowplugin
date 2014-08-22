/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import java.util.Arrays;


/**
 * DHCPv6 option data store (immutable).
 *
 * @author Frank Wood
 */
public class DhcpOptionV6 {

    /** Option types. */
    public enum Type implements ProtocolEnum {
        
        /** Unknown option. */
        UNKNOWN(0),
        /** Client ID option. */
        CLIENT_ID(1),
        /** Server ID option. */
        SERVER_ID(2),
        /** Identity association for non-temporary addresses option. */
        IA_NA(3),
        /** Identity association for temporary addresses option. */
        IA_TA(4),
        /** Identity association address option. */
        IA_ADDR(5),
        /** Option request option. */
        OPTION_REQ(6),
        /** Preference option. */
        PREF(7),
        /** Elapsed time option. */
        ELAPSED_TIME(8),
        /** Relay message option. */
        RELAY_MSG(9),
        /** Authentication option. */
        AUTH(11),
        /** Unicast option. */
        UNICAST(12),
        /** Status code option. */
        STATUS_CODE(13),
        /** Rapid commit option. */
        RAPID_COMMIT(14),
        /** User class option. */
        USER_CLASS(15),
        /** Vender class option. */
        VENDOR_CLASS(16),
        /** Vender-specific information option. */
        VENDOR_INFO(17),
        /** Interface ID option. */
        INTERFACE_ID(18),
        /** Reconfigure option. */
        RECONFIG(19),
        /** Reconfigure accept option. */
        RECONFIG_ACCEPT(20),
        /** SIP servers domain name list option. */
        SIP_SERVERS_DOMAIN_NAMES(21),
        /** SIP servers IPv6 address list option. */
        SIP_SERVERS_IPV6_ADDR(22),
        /** Identity association for prefix delegation option. */
        IDENT_ASSOC_PREFIX_DELEG(25),
        ;

        private int code;
        
        private Type(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static Type get(int code) {
            return ProtocolUtils.getEnum(Type.class, code, UNKNOWN);
        }
    }    
    
    private Type type;
    private byte[] bytes = ProtocolUtils.EMPTY_BYTES;
        
    /**
     * Constructor that takes an option type.
     * 
     * @param type option type
     */
    public DhcpOptionV6(Type type) {
        this.type = type;
    }    
    
    /**
     * Constructor that takes an option type and payload.
     * 
     * @param type option type
     * @param bytes payload bytes
     */
    public DhcpOptionV6(Type type, byte[] bytes) {
        this.type = type;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Returns the option type.
     * 
     * @return the option type
     */
    public Type type() {
        return type;
    }
    
    /**
     * Returns a copy of the byte array.
     *  
     * @return the byte array or null
     */
    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Internally used by the package to get the byte array for this option.
     * 
     * @return the payload byte array
     */
    byte[] bytesArray() {
        return bytes;
    }
    
    @Override
    public String toString() {
        return "[" + type + "]";
    }
    
}
