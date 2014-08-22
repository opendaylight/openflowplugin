/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.HandlerFacet;
import org.opendaylight.util.net.IpAddress;

/**
 * Configure the device to send events back to the controller
 *
 */
public interface NotificationHandler extends HandlerFacet {

    /**
     * Set the host IP address for the events to be sent.
     * Typically this will be the controller's IP.
     *
     * @param ip address for events
     */
    public void setHostIp(IpAddress ip);

    /**
     * Discover the host IP to communicate with the device.
     *
     * @return true if an address was discovered
     */
    public boolean discoverHostIp();

    /**
     * Is the supplied host ip already configured as a destination for
     * notifications on this device?
     *
     * @return true if the host is already set on the device
     */
    public boolean alreadyConfigured();

    /**
     * Delete the host notification entry from the device.
     */
    public void delete();

    /**
     * Add the host notification entry to the device.
     */
    public void add();
}
