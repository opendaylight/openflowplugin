/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.model;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.Vni;

import java.util.Set;

import static org.opendaylight.util.CommonUtils.notNull;

/**
 * Default implementation of {@link Vxlan Tunnel}.
 * The values are discovered from the device  and none are configurable.
 *  
 * @author Anuradha Musunuri
 *
 */
public class DefaultVxlan implements Vxlan {
   
    private static final long serialVersionUID = 1575880966096404521L;
    private TunnelIndex index;
    private VxlanInfo info;
    
    /**
     * Constructor to combine index and info.
     * 
     * @param index tunnel index
     * @param info Vxlan info from device
     */
    public DefaultVxlan(TunnelIndex index, VxlanInfo info) {
        notNull(index, info);
        this.index = index;
        this.info = info;
    }
    
    @Override
    public TunnelIndex index() {
        return TunnelIndex.valueOf(info.index());
    }

    @Override
    public IpAddress localAddress() {
        return info.localAddress();
    }

    @Override
    public IpAddress remoteAddress() {        
        return info.remoteAddress();
    }

    @Override
    public Set<Vni> vnis() {       
        return info.vnis();
    }

    public VxlanInfo getInfo() {
        return this.info;
    }
    
    @Override
    public String toString() {
        return "DefaultVxlan{" +
                "index=" + index +
                ", " + info +
                '}';
    }
}
