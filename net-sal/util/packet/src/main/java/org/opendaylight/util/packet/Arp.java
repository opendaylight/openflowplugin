/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.Arp.OpCode.REQ;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.IpAddress.Family;
import org.opendaylight.util.net.MacAddress;


/**
 * ARP data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>ARP opcode</li>
 * <li>ARP source IPv4 address</li>
 * <li>ARP target IPv4 address</li>
 * <li>ARP source hardware address</li>
 * <li>ARP target hardware address</li>
 * </ul>
 * @author Frank Wood
 */
public class Arp implements Protocol {

    /** Operation code. */
    public enum OpCode implements ProtocolEnum {
        
        /** Request operation code. */
        REQ(1),
        /** Reply operation code. */
        REPLY(2),
        /** Reverse request operation code. */
        REV_REQ(3),
        /** Reverse reply operation code. */
        REV_REPLY(4),
        /** Dynamic reverse request operation code. */
        DYN_REV_REQ(5),
        /** Dynamic reverse reply operation code. */
        DYN_REV_REPLY(6),
        /** Dynamic reverse error operation code. */
        DYN_REV_ERR(7),
        /** Inverse request operation code. */
        INV_REQ(8),
        /** Inverse reply operation code. */
        INV_REPLY(9),
        ;
        
        private int code;
        
        private OpCode(int code) {
            this.code = code;
        }
        
        @Override
        public int code() {
            return code;
        }
        
        static OpCode get(int code) {
            return ProtocolUtils.getEnum(OpCode.class, code, REQ);
        }
    }
    
    private static final String E_UNS_IPV6 = "Unsupported IPv6 addresses";
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private HardwareType hwType = HardwareType.ETHERNET;
        private OpCode opCode = REQ;
        private MacAddress senderMacAddr;
        private IpAddress senderIpAddr;
        private MacAddress targetMacAddr;
        private IpAddress targetIpAddr;
        
        private Data() {}
        
        private Data(Data data) {
            hwType = data.hwType;
            opCode = data.opCode;
            senderMacAddr = data.senderMacAddr;
            senderIpAddr = data.senderIpAddr;
            targetMacAddr = data.targetMacAddr;
            targetIpAddr = data.targetIpAddr;
        }
        
        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(hwType, opCode, senderMacAddr, senderIpAddr,
                                        targetMacAddr, targetIpAddr);

            if (senderIpAddr.getFamily() != Family.IPv4 ||
                    targetIpAddr.getFamily() != Family.IPv4) {
                throw new ProtocolException(E_UNS_IPV6);
            }
        }
    }
    
    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * hwType = ETHERNET
         * opCode = REQ
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param arp builder is initialed from this protocol's data
         */
        public Builder(Arp arp) {
            this.data = new Data(arp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public Arp build() {
            return new Arp(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        Arp buildNoVerify() {
            return new Arp(data, false);
        }
        
        /**
         * Sets the hardware type.
         * 
         * @param hwType hardware type enumeration
         * @return this instance
         */
        public Builder hwType(HardwareType hwType) {
            data.hwType = hwType;
            return this;
        }
        
        /**
         * Sets the operation code.
         * 
         * @param opCode operation code enumeration
         * @return this instance
         */
        public Builder opCode(OpCode opCode) {
            data.opCode = opCode;
            return this;
        }
        
        /**
         * Sets the sender MAC address.
         * 
         * @param senderMacAddr sender MAC address
         * @return this instance
         */
        public Builder senderMacAddr(MacAddress senderMacAddr) {
            data.senderMacAddr = senderMacAddr;
            return this;
        }
        
        /**
         * Sets the sender IP address.
         * 
         * @param senderIpAddr sender IP address
         * @return this instance
         */
        public Builder senderIpAddr(IpAddress senderIpAddr) {
            data.senderIpAddr = senderIpAddr;
            return this;
        }
        
        /**
         * Sets the target MAC address.
         * 
         * @param targetMacAddr target MAC address
         * @return this instance
         */
        public Builder targetMacAddr(MacAddress targetMacAddr) {
            data.targetMacAddr = targetMacAddr;
            return this;
        }
        
        /**
         * Sets the target IP address.
         * 
         * @param targetIpAddr target IP address
         * @return this instance
         */
        public Builder targetIpAddr(IpAddress targetIpAddr) {
            data.targetIpAddr = targetIpAddr;
            return this;
        }
    }
    
    private Data data;
    
    private Arp(Data data, boolean verify) {
        this.data = data;
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.ARP;
    }

    /**
     * Returns the hardware type.
     * 
     * @return the hardware type
     */
    public HardwareType hwType() {
        return data.hwType;
    }
    
    /**
     * Returns the operation code.
     * 
     * @return the operation code
     */
    public OpCode opCode() {
        return data.opCode;
    }
    
    /**
     * Returns the sender MAC address.
     * 
     * @return the sender MAC address
     */
    public MacAddress senderMacAddr() {
        return data.senderMacAddr;
    }
    
    /**
     * Returns the sender IP address.
     * 
     * @return the sender IP address
     */
    public IpAddress senderIpAddr() {
        return data.senderIpAddr;
    }
    
    /**
     * Returns the target MAC address.
     * 
     * @return the target MAC address
     */
    public MacAddress targetMacAddr() {
        return data.targetMacAddr;
    }
    
    /**
     * Returns the target IP address.
     * 
     * @return the target IP address
     */
    public IpAddress targetIpAddr() {
        return data.targetIpAddr;
    }
 
    @Override
    public String toString() {
        return id() + "," + opCode() +
                ",sMAC=" + senderMacAddr() + 
                ",sIP=" + senderIpAddr() +
                ",tMAC=" + targetMacAddr() + 
                ",tIP=" + targetIpAddr(); 
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("hwType: ").append(hwType())
            .append(eoli).append("opCode: ").append(opCode())
            .append(eoli).append("senderMacAddr: ").append(senderMacAddr())
            .append(eoli).append("senderIpAddr: ").append(senderIpAddr())
            .append(eoli).append("targetMacAddr: ").append(targetMacAddr())
            .append(eoli).append("targetIpAddr: ").append(targetIpAddr())
            ;
        return sb.toString();
    }
    
}
