/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import static org.opendaylight.util.CommonUtils.notNull;

import java.util.Set;

import org.opendaylight.util.net.VlanId;


public class DefaultVlanInfo implements VlanInfo {

    private VlanId vid = null;
    private Set<InterfaceId> taggedPorts = null;
    private Set<InterfaceId> untaggedPorts = null;
    private Set<SubnetIp> ips = null;
    private String name = null;
    private String description = null;
    
    @Override
    public VlanId vid() {
        return  vid;
    }
    
    /**
     * Constructs a vlan Info object.
     * @param vid vlan identifier
     */
    public DefaultVlanInfo(VlanId vid) {
        this.vid = vid;
    }
    
    /**
     * Set the vlan identifier.
     * 
     * @param vid vlan identifier.
     * @return self, for chaining
     */
    public DefaultVlanInfo setVid(VlanId vid) {
        notNull(vid);
        this.vid = vid;
        return this;
    }
    
    @Override
    public String name() {
        return name;
    }
    
    /**
     * Set the name assigned to the vlan.
     * 
     * @param name vlan name.
     * @return self, for chaining
     */
    public DefaultVlanInfo setName(String name) {
        notNull(name);
        this.name = name;
        return this;
    }
    
    @Override
    public String description() {
        return description;
    }
    
    /**
     * Set the description assigned to the vlan.
     * 
     * @param description vlan description.
     * @return self, for chaining
     */
    public DefaultVlanInfo setDescription(String description) {
        notNull(description);
        this.description = description;
        return this;
    }

    @Override
    public Set<InterfaceId> taggedPorts() {
        return taggedPorts;
    }
    
    /**
     * Set tagged ports.
     * 
     * @param taggedPorts Set tagged port associated with the vlan.
     * @return self, for chaining
     */
    public DefaultVlanInfo setTaggedPorts(Set<InterfaceId> taggedPorts) {
        notNull(taggedPorts);
        this.taggedPorts = taggedPorts;
        return this;
    }

    @Override
    public Set<InterfaceId> untaggedPorts() {
        return untaggedPorts;
    }
    
    /**
     * Set untagged ports.
     * 
     * @param untaggedPorts Set untagged port associated with the vlan.
     * @return self, for chaining
     */
    public DefaultVlanInfo setUntaggedPorts(Set<InterfaceId> untaggedPorts) {
        notNull(untaggedPorts);
        this.untaggedPorts = untaggedPorts;
        return this;
    }

    @Override
    public Set<SubnetIp> ips() {
        return ips;
    }
    
    /**
     * Set IP address/subnet mask pairs.
     * 
     * @param ips Set of IP address/subnet mask pairs associated with the vlan.
     * @return self, for chaining
     */
    public DefaultVlanInfo setIps(Set<SubnetIp> ips) {
        notNull(ips);
        this.ips = ips;
        return this;
    }

}
