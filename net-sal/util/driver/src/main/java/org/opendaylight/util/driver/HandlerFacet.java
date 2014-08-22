/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import org.opendaylight.util.net.IpAddress;

/**
 * Abstraction of a facet returned from a device handler. Handler facet
 * implementations may be just read-only in nature or read-write, but the
 * point is, they potentially have access to the live device. Thus they are
 * suitable for query or configuration of a device managed through some form
 * of IP-based management protocol.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface HandlerFacet extends Facet {
    
    /**
     * Gets the IP address of the device with which this facet is associated.
     * 
     * @return device IP address
     */
    public IpAddress getIpAddress();
    
    /**
     * Set the device IP address with which this facet is to be associated.
     * 
     * @param ip IP address of the device with which this facet is to be
     *        associated
     */
    public void setIpAddress(IpAddress ip);

    /**
     * Fetch information pertaining to this facet from the device. 
     */
    public void fetch();
    
    /**
     * Apply any pending configuration changes to the device.
     */
    public void apply();

}
