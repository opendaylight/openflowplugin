/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.SubnetMask;

/**
 * Implements and Ip Address and Subnet Mask pair.
 *
 * @author Steve Dean
 */
public class SubnetIp {
    IpAddress ip;
    SubnetMask mask;
    
    /**
     * Construct a IP address/subnet mask object.
     * 
     * @param ip IP Address.
     * @param mask Subnet mask.
     */
    public SubnetIp(IpAddress ip, SubnetMask mask) {
        this.ip = ip;
        this.mask = mask;
    }
    
    /**
     * Returns the IP address.
     * 
     * @return IP address.
     */
    public IpAddress ip() {
        return ip;
    }
    
    /**
     * Set the IP address.
     * 
     * @param ip IP address.
     */
    public void setIp(IpAddress ip) {
        this.ip = ip;
    }
    
    /**
     * Returns the subnet mask.
     * 
     * @return subnet mask.
     */
    public SubnetMask mask() {
        return mask;
    }
    
    /**
     * Set the subnetm mask.
     * 
     * @param mask subnet mask
     */
    public void setMask(SubnetMask mask) {
        this.mask = mask;
    }

}
