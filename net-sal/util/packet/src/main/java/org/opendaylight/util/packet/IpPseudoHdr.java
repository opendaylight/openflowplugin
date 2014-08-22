/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.IpAddress;


/**
 * Internally used by the packet to encapsulate the IPv4/IPv6 pseudo header
 * for checksum calculations.
 *
 * @author Frank Wood
 */
class IpPseudoHdr {

    private static final String E_BAD_ADDR =
            "Addresses must be of the same version";
    
    /** Source address IPv4/IPv6. */
    private IpAddress srcAddr;
    
    /** Destination address IPv4/IPv6. */
    private IpAddress dstAddr;
    
    /** The upper layer protocol header and payload length. */
    private int len;
    
    /** The upper layer protocol type. */
    private IpType type;
    
    /**
     * Constructor to create the pseudo header. IPv4/IPv6 version is determined
     * from the {@link IpAddress} type.
     * 
     * @param srcAddr source address, destination must be of the same version 
     * @param dstAddr destination address, source must be of the same version
     * @param type upper layer protocol type
     */
    IpPseudoHdr(IpAddress srcAddr, IpAddress dstAddr, IpType type) {
        
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
        
        if (srcAddr.getFamily() != dstAddr.getFamily())
            throw new ProtocolException(E_BAD_ADDR);
        
        this.type = type;
    }
    
    /**
     * Returns the version of the pseudo header (V4 or V6).
     * 
     * @return the IP version of the pseudo header
     */
    IpVersion version() {
        return (srcAddr.getFamily() == IpAddress.Family.IPv6)
                ? IpVersion.V6 : IpVersion.V4;
    }
    
    /**
     * Sets the upper layer protocol header and payload length.
     * 
     * @param len upper layer protocol header and payload length
     * @return this instance
     */
    IpPseudoHdr len(int len) {
        this.len = len;
        return this;
    }
    
    /**
     * Returns the source IPv4/IPv6 address.
     * 
     * @return the IP address
     */
    IpAddress srcAddr() {
        return srcAddr;
    }
    
    /**
     * Returns the destination IPv4/IPv6 address.
     * 
     * @return the IP address
     */
    IpAddress dstAddr() {
        return dstAddr;
    }
    
    /**
     * Returns the upper layer protocol header and payload length.
     * 
     * @return the length in bytes
     */
    int len() {
        return len;
    }

    /**
     * Returns the upper layer protocol type.
     * 
     * @return the protocol type
     */
    IpType type() {
        return type;
    }    
    
}
