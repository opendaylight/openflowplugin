/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver;

import org.opendaylight.util.driver.DeviceDriverProvider;

/**
 * Provides device driver services to external applications.
 * <p/>
 * Services of this interface provide the ability to:
 * <ul>
 * <li>lookup driver for a specific device</li>
 * <li>determine the facets that the driver provides</li>
 * <li>access the exposed facets</li>
 * </ul>
 */

public interface DeviceDriverService extends DeviceDriverProvider {

    /**
     * Find the best match Device Type Name for the given device strings.
     *
     * @param mfr device manufacturer
     * @param hw  device hardware model
     * @param fw  device firmware revision
     * @return device type name
     */
    String getTypeName(String mfr, String hw, String fw);

}
