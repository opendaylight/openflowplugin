/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import java.util.Set;

import org.opendaylight.util.driver.HandlerFacet;
import org.opendaylight.net.model.InterfaceId;
import org.opendaylight.net.model.VlanInfo;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.SubnetMask;
import org.opendaylight.util.net.VlanId;

/**
 * Abstraction for provisioning and managing VLANs on switches.
 * It provides ability to do the following:
 * <ul>
 *   <li>create vlan-port pairs
 *   <li>create and delete vlans
 *   <li>add and remove port pairs to/from a vlan
 *   <li>get a set of all vlans
 *   <li>get a vlan for a given port
 *   <li>get a set of VID for a given port
 *   <li>get a set of vlans for a given port
 * </ul>
 */
public interface VlanHandler extends HandlerFacet {
    
    /**
     * Creates a vlan
     * 
     * @param vid the vlan identifier (VID) to be assigned to the vlan
     * @param name the vlan name
     * @param description description of the vlan
     */
    void createVlan(VlanId vid, String name, String description);
    
    /**
     * Delete the vlan identified by the vlan identifier (VID). 
     * @param vid the vlan identifier (VID) assigned to the vlan to be deleted
     */
    void deleteVlan(VlanId vid);
    
    /**
     * Add a port to a vlan. The VID is specified in the VlanPortPair object.
     * @param port the port to be added
     */
    void addPort(VlanPortPair port);
    
    /**
     * Remove a port from a vlan. The VID is specified in the VlanPortPair 
     * object.
     * @param port the port to be removed
     */
    void removePort(VlanPortPair port);
    
    /**
     * Add an IP address to the vlan.
     * @param vid vlan identifier to add IP address
     * @param ip IP address to be added to the vlan
     * @param mask subnet mask for the IP address
     */
    void addIpAddress(VlanId vid, IpAddress ip, SubnetMask mask);
    
    /**
     * Remove an IP address from the vlan.
     * @param vid vlan identifier to remove the IP address from
     * @param ip IP address to be removed from the vlan
     * @param mask subnet mask for the IP address
     */
    void removeIpAddress(VlanId vid, IpAddress ip, SubnetMask mask);
    
    /**
     * Set or change the vlan name.
     * @param vid vlan identifier for the vlan whose name is to be set.
     */
    void setName(VlanId vid);
    
    /**
     * Set or change the vlan description.
     * @param vid vlan identifier for the vlan whose description is to be set.
     */
    void setDescription(VlanId vid);
    
    /**
     * Get a set of all Vlans that have been created.
     * @return a set of Vlan objects
     */
    Set<VlanInfo> getVlans();
    
    /**
     * Get the Vlan object for the specified VID.  The Vlan object will contain 
     * the set of VlanPortPorts assigned to the vlan.
     * @param vid the vlan identifier (VID) assigned to the vlan 
     * @return Vlan object
     */
    VlanInfo getVlan(VlanId vid);
    
    /**
     * Get the set of vlan identifiers (VID) that the specified port is a
     * member of.
     * @param port the port to return VIDs for
     * @param tag the tag type of the port
     * @return VlanIds containing the specified port
     */
    Set<VlanId> getVIdsContainingPort(InterfaceId port, VlanTagType tag);
    
    /**
     * Get the set of Vlans that  the specified port is a member of.
     * @param port the port to return Vlans for
     * @param tag the tag type of the port
     * @return Vlans containing the specified port
     */
    Set<VlanInfo> getVlansContainingPort(InterfaceId port, VlanTagType tag);
    

}
