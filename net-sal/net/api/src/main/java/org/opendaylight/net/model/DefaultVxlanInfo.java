/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.Vni;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Default implementation of {@link VxlanInfo}. Information is discovered
 * from the device and not configurable.
 * 
 * @author Anuradha Musunuri
 *
 */
public class DefaultVxlanInfo implements VxlanInfo {
    
    private static final long serialVersionUID = 3387746572234213317L;
    private BigPortNumber tunnelIndex = null;
    private transient Set<Vni> vnis = null;
    private IpAddress localIp = null;
    private IpAddress remoteIp = null;
        
    /**
     * Constructs an Vxlan Info object.
     * @param tunnelIndex tunnel index
     */
    public DefaultVxlanInfo(BigPortNumber tunnelIndex) {
        this.tunnelIndex = tunnelIndex;
    }

    @Override
    public BigPortNumber index() {
        return tunnelIndex;
    }
    
    @Override
    public Set<Vni> vnis() {
        //TODO: Modify to return Tunnel Vnis
        return Collections.unmodifiableSet(new HashSet<Vni>());
    }

    @Override
    public IpAddress localAddress() {
        return localIp;
    }
    
    @Override
    public IpAddress remoteAddress() {
        return remoteIp;
    }
    
    /**
     * Modify the element tunnelIndex
     *
     * @param index tunnel Index to be set
     * @return self, for chaining
     */
    public DefaultVxlanInfo index(BigPortNumber index) {
        notNull(tunnelIndex);
        this.tunnelIndex = index;
        return this;
    }
    
    /**
     * Modify the element localIP
     *
     * @param localIp tunnel source Ip to be set
     * @return self, for chaining
     */
    public DefaultVxlanInfo localIp(IpAddress localIp) {
        notNull(localIp);
        this.localIp = localIp;
        return this;
    }
    
    /**
     * Modify the element remoteIP
     *
     * @param remoteIp tunnel remote Ip to be set
     * @return self, for chaining
     */
    public DefaultVxlanInfo remoteIp(IpAddress remoteIp) {
        notNull(remoteIp);
        this.remoteIp = remoteIp;
        return this;
    }
    
    /**
     * Modify the element tunnel Vnis
     *
     * @param vnis tunnel Vnis to be set
     * @return self, for chaining
     */
    public DefaultVxlanInfo vnis(Set<Vni> vnis) {
        notNull(vnis);
        this.vnis = vnis;
        return this;
    }
    
}
