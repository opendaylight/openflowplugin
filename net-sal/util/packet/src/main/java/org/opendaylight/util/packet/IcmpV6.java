/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.StringUtils.spaces;
import static org.opendaylight.util.packet.IcmpTypeCodeV6.NONE;
import static org.opendaylight.util.packet.ProtocolUtils.hex;

import java.util.Arrays;

import org.opendaylight.util.net.IpAddress;


/**
 * ICMPv6 data store (immutable) and associated {@link Builder} (mutable).
 * <p>
 * Includes support for the following OpenFlow match fields:
 * <p>
 * <ul>
 * <li>ICMPv6 type</li>
 * <li>ICMPv6 code</li>
 * <li>target address for Neighbor Discovery</li>
 * <li>source link-layer for Neighbor Discovery</li>
 * <li>target link-layer for Neighbor Discovery</li>
 * </ul>
 * @author Frank Wood
 */
public class IcmpV6 implements Protocol {

    /** Neighbor Discovery - Router Advertisement data store (immutable). */
    public static class RouterAdvertiseData {
        
        private int hopLimit;
        private boolean managedAddrConfig;
        private boolean otherConfig; 
        private int routerLifetime;
        private long reachableTime;
        private long retransTimer;
        
        /**
         * Create a new Router Advertisement data store.
         * 
         * @param hopLimit default hop counter field of IP header
         * @param managedAddrConfig addresses are available via DHCP
         * @param otherConfig other configuration info available via DHCPv6
         * @param routerLifetime default router lifetime in seconds
         * @param reachableTime milliseconds node assumes neighbor is reachable
         * @param retransTimer milliseconds between retransmitted solicitations
         */
        public RouterAdvertiseData(int hopLimit, boolean managedAddrConfig,
                                   boolean otherConfig, int routerLifetime,
                                   long reachableTime, long retransTimer) {
            
            this.hopLimit = hopLimit;
            this.managedAddrConfig = managedAddrConfig;
            this.otherConfig = otherConfig;
            this.routerLifetime = routerLifetime;
            this.reachableTime = reachableTime;
            this.retransTimer = retransTimer;
        }
        
        @Override
        public String toString() {
            return "[hl=" + hopLimit + ",M=" + managedAddrConfig +
                    ",O=" + otherConfig + ",rlt=" + routerLifetime +
                    ",rt=" + reachableTime + ",rtimer=" + retransTimer + "]";
        }
        
        /**
         * Returns the default hop counter limit (for field in the IP header).
         * 
         * @return the default hop counter limit
         */
        public int hopLimit() {
            return hopLimit;
        }
        
        /**
         * Flag that is true if addresses are available via DHCP.
         * 
         * @return true if addresses are available via DHCP.
         */
        public boolean managedAddrConfig() {
            return managedAddrConfig;
        }
        
        /**
         * Flag that is true if other configuration information is available
         * via DHCPv6.
         * 
         * @return true if other configuration information is available
         */
        public boolean otherConfig() {
            return otherConfig;
        }
        
        /**
         * Default router lifetime in seconds.
         * 
         * @return the lifetime in seconds
         */
        public int routerLifetime() {
            return routerLifetime;
        }
        
        /**
         * The time in milliseconds a node assumes its neighbor is reachable.
         * 
         * @return the time in milliseconds
         */
        public long reachableTime() {
            return reachableTime;
        }
        
        /**
         * The time in milliseconds between retransmitted Neighbor Solicitation
         * messages.
         * 
         * @return the time in milliseconds
         */
        public long retransTimer() {
            return retransTimer;
        }
    }
    
    /** Neighbor Discovery - Neighbor Solicitation data store (immutable). */
    public static class NeighborSolicitData {
        
        private IpAddress targetAddr;
        
        /**
         * Create a new Neighbor Solicitation data store.
         * 
         * @param targetAddr target address
         */
        public NeighborSolicitData(IpAddress targetAddr) {
            this.targetAddr = targetAddr;
        }
        
        @Override
        public String toString() {
            return "[" + targetAddr + "]";
        }
        
        /**
         * Returns the target address.
         * 
         * @return the IPv6 address
         */
        public IpAddress targetAddr() {
            return targetAddr;
        }
    }

    /** Neighbor Discovery - Neighbor Advertisement data store (immutable). */
    public static class NeighborAdvertiseData {
        
        private boolean isSenderRouter;
        private boolean isSolicitResponse;
        private boolean override;
        private IpAddress targetAddr;
        
        /**
         * Create a new Neighbor Solicitation data store.
         * 
         * @param isSenderRouter true if the sender is a router 
         * @param isSolicitResponse  true if this is a solicitation response
         * @param override true if this should override cached address
         * @param targetAddr target address
         */
        public NeighborAdvertiseData(boolean isSenderRouter,
                                     boolean isSolicitResponse,
                                     boolean override, IpAddress targetAddr) {
            
            this.isSenderRouter = isSenderRouter;
            this.isSolicitResponse = isSolicitResponse;
            this.override = override;
            this.targetAddr = targetAddr;
        }
        
        @Override
        public String toString() {
            return "[R=" + isSenderRouter + ",S=" + isSolicitResponse +
                    ",O=" + override + ",t=" + targetAddr + "]";
        }
        
        /**
         * Returns true if the sender is a router.
         * 
         * @return true if the sender is a router
         */
        public boolean isSenderRouter() {
            return isSenderRouter;
        }        
        
        /**
         * Returns true if this is a response to a solicitation.
         * 
         * @return true if this is a solicitation response
         */
        public boolean isSolicitResponse() {
            return isSolicitResponse;
        }
        
        /**
         * Returns true if this should override the existing cached address.
         * 
         * @return true if this should override the cached address
         */
        public boolean override() {
            return override;
        }     
        
        /**
         * Returns the target address.
         * 
         * @return the IPv6 address
         */
        public IpAddress targetAddr() {
            return targetAddr;
        }        
        
    }

    /** Neighbor Discovery - Redirect message data store (immutable). */
    public static class RedirectData {
        
        private IpAddress targetAddr;
        private IpAddress dstAddr;
        
        /**
         * Create a new Redirect message data store.
         * 
         * @param targetAddr target address
         * @param dstAddr destination address
         */
        public RedirectData(IpAddress targetAddr, IpAddress dstAddr) {
            this.targetAddr = targetAddr;
            this.dstAddr = dstAddr;
        }
        
        @Override
        public String toString() {
            return "[t=" + targetAddr + ",d=" + dstAddr + "]";
        }
        
        /**
         * Returns the target address.
         * 
         * @return the IPv6 address
         */
        public IpAddress targetAddr() {
            return targetAddr;
        }          
        
        /**
         * Returns the destination address.
         * 
         * @return the IPv6 address
         */
        public IpAddress dstAddr() {
            return dstAddr;
        }          
    }
    
    /** Internally used to indicate no options. */
    static final IcmpOptionV6[] NO_OPTIONS = new IcmpOptionV6[0];
    
    private static final String E_BAD_DATA = "No data for type/code: ";
    
    /** Internal private data store. */
    private static class Data implements ProtocolData {

        private IcmpTypeCodeV6 typeCode = NONE;
        private int checkSum;
        private byte[] bytes = ProtocolUtils.EMPTY_BYTES;
        
        private RouterAdvertiseData routerAdvertiseData;
        private NeighborSolicitData neighborSolicitData;
        private NeighborAdvertiseData neighborAdvertiseData;
        private RedirectData redirectData;
        
        private IcmpOptionV6[] options = NO_OPTIONS;
        
        private Data() {}
        
        private Data(Data data) {
            typeCode = data.typeCode;
            checkSum = data.checkSum;
            bytes = Arrays.copyOf(data.bytes, data.bytes.length);
            routerAdvertiseData = data.routerAdvertiseData;
            neighborSolicitData = data.neighborSolicitData;
            neighborAdvertiseData = data.neighborAdvertiseData;
            redirectData = data.redirectData;
            options = Arrays.copyOf(data.options, data.options.length);
        }

        @Override
        public void verify() {
            ProtocolUtils.verifyNotNull(typeCode, bytes, options);
            
            if (typeCode == IcmpTypeCodeV6.ROUTER_ADVERTISE_NDP &&
                    routerAdvertiseData == null)
                throw new ProtocolException(E_BAD_DATA + typeCode);
            
            if (typeCode == IcmpTypeCodeV6.NEIGHBOR_SOLICIT_NDP &&
                    neighborSolicitData == null)
                throw new ProtocolException(E_BAD_DATA + typeCode);

            if (typeCode == IcmpTypeCodeV6.NEIGHBOR_ADVERTISE_NDP &&
                    neighborAdvertiseData == null)
                throw new ProtocolException(E_BAD_DATA + typeCode);

            if (typeCode == IcmpTypeCodeV6.REDIRECT_MSG_NDP &&
                    redirectData == null)
                throw new ProtocolException(E_BAD_DATA + typeCode);
        }
    }

    /** Builder (mutable) used to create new protocol instances. */
    public static class Builder {
        
        private Data data;
        
        /**
         * Create a new builder using the defaults:
         * <pre>
         * typeCode = NONE
         * bytes = EMPTY_BYTES
         * routerAdvertiseData = null
         * neighborSolicitData = null
         * neighborAdvertiseData = null
         * redirectData = null
         * options = NO_OPTIONS
         * </pre>
         */
        public Builder() {
            this.data = new Data();
        }
        
        /**
         * Copy constructor builder initialized from the passed in protocol.
         * 
         * @param icmp builder is initialed from this protocol's data
         */
        public Builder(IcmpV6 icmp) {
            this.data = new Data(icmp.data);
        }
        
        /**
         * Creates a new protocol instance from this builder.
         * 
         * @return the new protocol instance
         */
        public IcmpV6 build() {
            return new IcmpV6(data, true);
        }
        
        /**
         * Internally used by the package to create a new protocol instance
         * from this builder when an error occurs. No verification is performed.
         * 
         * @return the new protocol instance
         */
        IcmpV6 buildNoVerify() {
            return new IcmpV6(data, false);
        }
        
        /**
         * Internally used by the package to set the checksum.
         * 
         * @param checkSum check sum
         * @return this instance
         */
        Builder checkSum(int checkSum) {
            data.checkSum = checkSum;
            return this;
        }
        
        /**
         * Sets the type/code enumeration..
         *  
         * @param typeCode type/code enumeration
         * @return this instance
         */
        public Builder typeCode(IcmpTypeCodeV6 typeCode) {
            data.typeCode = typeCode;
            return this;
        }
        
        /**
         * Sets the payload bytes.
         * 
         * @param bytes payload bytes
         * @return this instance
         */
        public Builder bytes(byte[] bytes) {
            data.bytes = bytes;
            return this;
        }
        
        /**
         * Sets the router advertise data and the appropriate type/code.
         * 
         * @param raData router advertise data
         * @return this instance
         */
        public Builder routerAdvertiseData(RouterAdvertiseData raData) {
            typeCode(IcmpTypeCodeV6.ROUTER_ADVERTISE_NDP);
            data.routerAdvertiseData = raData;
            return this;
        }        
        
        /**
         * Sets the neighbor solicitation data and the appropriate type/code.
         * 
         * @param nsData neighbor solicitation data
         * @return this instance
         */
        public Builder neighborSolicitData(NeighborSolicitData nsData) {
            typeCode(IcmpTypeCodeV6.NEIGHBOR_SOLICIT_NDP);
            data.neighborSolicitData = nsData;
            return this;
        }        
        
        /**
         * Sets the neighbor advertise data and the appropriate type/code.
         * 
         * @param naData neighbor advertise data
         * @return this instance
         */
        public Builder neighborAdvertiseData(NeighborAdvertiseData naData) {
            typeCode(IcmpTypeCodeV6.NEIGHBOR_ADVERTISE_NDP);
            data.neighborAdvertiseData = naData;
            return this;
        }        
        
        /**
         * Sets the redirect message data and the appropriate type/code.
         * 
         * @param rData redirect message data
         * @return this instance
         */
        public Builder redirectData(RedirectData rData) {
            typeCode(IcmpTypeCodeV6.REDIRECT_MSG_NDP);
            data.redirectData = rData;
            return this;
        }
        
        /**
         * Sets the options.
         * 
         * @param options option array
         * @return this instance
         */
        public Builder options(IcmpOptionV6[] options) {
            data.options = options;
            return this;
        }
    }    
    
    private Data data;

    private IcmpV6(Data data, boolean verify) {
        this.data = new Data(data);
        ProtocolUtils.verify(verify, this, this.data);
    }
    
    @Override
    public ProtocolId id() {
        return ProtocolId.ICMPV6;
    }

    /**
     * Returns the type/code enumeration.
     * 
     * @return the type/code enumeration.
     */
    public IcmpTypeCodeV6 typeCode() {
        return data.typeCode;
    }
    
    /**
     * Returns the decoded check sum.
     * 
     * @return the check sum
     */
    public int checkSum() {
        return data.checkSum;
    }
    
    /**
     * Returns the router advertise data or null.
     * 
     * @return the router advertise data
     */
    public RouterAdvertiseData routerAdvertiseData() {
        return data.routerAdvertiseData;
    }        
    
    /**
     * Returns the neighbor solicitation data.
     * 
     * @return the neighbor solicitation data
     */
    public NeighborSolicitData neighborSolicitData() {
        return data.neighborSolicitData;
    }        
    
    /**
     * Returns the neighbor advertise data.
     * 
     * @return the neighbor advertise data
     */
    public NeighborAdvertiseData neighborAdvertiseData() {
        return data.neighborAdvertiseData;
    }        
    
    /**
     * Returns the redirect message data.
     * 
     * @return the redirect message data
     */
    public RedirectData redirectData() {
        return data.redirectData;
    }     
    
    /**
     * Internally used to return the payload bytes.
     * 
     * @return the payload bytes
     */
    byte[] bytesArray() {
        return data.bytes;
    }
    
    /**
     * Returns a copy of the payload bytes.
     * 
     * @return the payload bytes
     */
    public byte[] bytes() {
        return Arrays.copyOf(data.bytes, data.bytes.length);
    }
    
    /**
     * Internally used by the packet to access the options array.
     * 
     * @return the option array
     */
    IcmpOptionV6[] optionsArray() {
        return data.options;
    }
    
    /**
     * Returns a copy of the option array.
     * 
     * @return the options array
     */
    public IcmpOptionV6[] options() {
        return Arrays.copyOf(data.options, data.options.length);
    }    
    
    @Override
    public String toString() {
        return id() + "," + typeCode();
    }
    
    @Override
    public String toDebugString() {
        String eoli = ProtocolUtils.EOLI + spaces(ProtocolUtils.INDENT_SIZE);
        StringBuilder sb = new StringBuilder().append(id()).append(":")
            .append(eoli).append("typeCode: ").append(typeCode())
            .append(eoli).append("checkSum (decode-only): ")
                         .append(ProtocolUtils.hex(checkSum()))
            .append(eoli).append("bytes: ").append(ProtocolUtils.hex(bytesArray()))
            .append(eoli).append("routerAdvertiseData: ")
                         .append(routerAdvertiseData())
            .append(eoli).append("neighborSolicitData: ")
                         .append(neighborSolicitData())
            .append(eoli).append("neighborAdvertiseData: ")
                         .append(neighborAdvertiseData())
            .append(eoli).append("redirectData: ")
                         .append(redirectData())
            ;
        
        for (IcmpOptionV6 o: optionsArray())
            sb.append(eoli).append("option: ").append(o);
        
        return sb.toString();
    }      

}
