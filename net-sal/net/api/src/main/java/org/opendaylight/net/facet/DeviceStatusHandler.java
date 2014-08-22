/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.facet;

import org.opendaylight.util.driver.HandlerFacet;

/**
 * Query the device to determine it status (online/offline).
 *
 * @author Steve Dean
 */
public interface DeviceStatusHandler extends HandlerFacet {

    /**
     * Check the status of a manually discovered device.
     * 
     * @return true if the device is online, false if offline
     */
    public boolean isOnline();
}
