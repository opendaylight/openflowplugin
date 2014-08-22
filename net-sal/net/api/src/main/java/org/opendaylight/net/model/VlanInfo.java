/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import java.util.Set;

import org.opendaylight.util.net.VlanId;


/**
 * Collection of vlan attributes.
 *
 * @author Steve Dean
 */
public interface VlanInfo {
    
    /**
     * Returns the vlan identifier.
     * 
     * @return vlan identifier.
     */
    VlanId vid();
    
    /**
     * Returns the name assigned to the vlan.
     * 
     * @return vlan name.
     */
    String name();
    
    /**
     * Returns the description assigned to the vlan.
     * 
     * @return vlan description
     */
    String description();
    
    /**
     * Returns  set of tagged ports associated with the vlan.
     * 
     * @return set of tagged ports.
     */
    Set<InterfaceId> taggedPorts();
    
    
    
    /**
     * Returns set of untagged ports associated with the vlan.
     * 
     * @return set of untagged ports.
     */
    Set<InterfaceId> untaggedPorts();
    
    /**
     * Returns set of IP address and subnet mask pairs associated with the vlan.
     * 
     * @return set of IP address/subnet mask pairs.
     */
    Set<SubnetIp> ips();

}
