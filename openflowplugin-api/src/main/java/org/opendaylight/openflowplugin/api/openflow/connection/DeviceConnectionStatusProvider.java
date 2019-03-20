/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.connection;

import java.math.BigInteger;
import java.time.LocalDateTime;

public interface DeviceConnectionStatusProvider {

    /**
     * Initialize the DeviceConnectionStatusProvider.
     */
    void init();

    /**
     * Get the last connection time of device.
     * @param nodeId nodeId of node
     */
    LocalDateTime getDeviceLastConnectionTime(BigInteger nodeId);

    /**
     * Update the last connection time of device.
     * @param nodeId nodeId of node
     * @param time last connected time of device
     */
    void addDeviceLastConnectionTime(BigInteger nodeId, LocalDateTime time);

    /**
      * Clear the last connection time of device.
      * @param nodeId nodeId of node
      */
    void removeDeviceLastConnectionTime(BigInteger nodeId);
}
