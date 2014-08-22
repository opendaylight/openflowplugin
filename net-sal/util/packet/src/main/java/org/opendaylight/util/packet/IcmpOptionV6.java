/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import java.util.Arrays;

import org.opendaylight.util.net.MacAddress;


/**
 * ICMPv6 option data store (immutable).
 * 
 * @author Frank Wood
 */
public class IcmpOptionV6 {
    
    /** ICMPv6 option types. */
    public enum Type implements ProtocolEnum {
        
        /** Source link-layer address. */
        SRC_LL_ADDR(1),
        
        /** Target link-layer address. */
        TARGET_LL_ADDR(2),
        
        /** Prefix information. */
        PREFIX_INFO(3),
        
        /** Redirected header. */
        REDIRECT_HDR(4),
        
        /** MTU. */
        MTU(5),
        ;
        
        /**
         * Internally used to return the enumeration constant.
         * 
         * @param code 1-byte code
         * @return the enumeration constant
         */
        static Type get(int code) {
            return ProtocolUtils.getEnum(Type.class, code, Type.SRC_LL_ADDR);
        }
        
        private int code;
        
        private Type(int code) {
            this.code = code;
        }

        @Override
        public int code() {
            return code;
        }            
    }
    
    private Type type;
    private long mtu;
    
    /** The source or target link layer address. */
    private MacAddress linkLayerAddr;
    
    /** Holds prefix information or the redirected header bytes. */
    private byte[] bytes = ProtocolUtils.EMPTY_BYTES;
    
    /**
     * Create a new ICMPv6 option data store for
     * {@link IcmpTypeCodeV6#ROUTER_SOLICT_NDP}, 
     * {@link IcmpTypeCodeV6#ROUTER_ADVERTISE_NDP}, or
     * {@link IcmpTypeCodeV6#NEIGHBOR_SOLICIT_NDP} messages.
     * 
     * @param type this option's type
     * @param linkLayerAddr link layer address
     */
    public IcmpOptionV6(Type type, MacAddress linkLayerAddr) {
        this.type = type;
        this.linkLayerAddr = linkLayerAddr;
    }
    
    /**
     * Create a new ICMPv6 option data store for
     * {@link IcmpTypeCodeV6#ROUTER_ADVERTISE_NDP} messages.
     * 
     * @param type this option's type
     * @param mtu MTU value
     */
    public IcmpOptionV6(Type type, long mtu) {
        this.type = type;
        this.mtu = mtu;
    }
    
    /**
     * Create a new ICMPv6 option data store for
     * {@link IcmpTypeCodeV6#ROUTER_ADVERTISE_NDP} and
     * {@link IcmpTypeCodeV6#REDIRECT_MSG_NDP} messages.
     * 
     * @param type this option's type
     * @param bytes prefix information or redirect header/payload bytes
     */
    public IcmpOptionV6(Type type, byte[] bytes) {
        this.type = type;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }
    
    /**
     * Returns this option's type.
     * 
     * @return this option's type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the MTU value.
     * 
     * @return the MTU value
     */
    public long mtu() {
        return mtu;
    }
    
    /**
     * Returns the link-layer address or null.
     * 
     * @return the link-layer address
     */
    public MacAddress linkLayerAddr() {
        return linkLayerAddr;
    }
    
    /**
     * Internally used by the package to access the option payload bytes.
     * 
     * @return the option payload bytes
     */
    byte[] bytesArray() {
        return bytes;
    }
    
    /**
     * Returns a copy of the option payload bytes.
     * 
     * @return the option payload bytes
     */
    public byte[] bytes() {
        return Arrays.copyOf(bytes,  bytes.length);
    }
    
    @Override
    public String toString() {
        return "[" + type + ",lla=" + linkLayerAddr + ",mtu=" + mtu +
                ",nBytes=" + bytes.length + "]";
    }        
    
}
