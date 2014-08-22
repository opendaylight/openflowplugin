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
 * A set of behaviors specific to a type of device; facilitating communication with an actual (live) instance
 * of that device type.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public interface DeviceHandler extends FacetProvider {

    /** Returns the DeviceInfo instance associated with this handler.
     *
     * @return the device info instance
     */
    public DeviceInfo getDeviceInfo();

    /** Returns the IP address of the device to which this handler is bound.
     *
     * @return the device IP address
     */
    public IpAddress getIpAddress();
    
}
