/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * This API is for all listeners who wish to know about device context in cluster
 */
public interface DeviceContextChangeListener {

    /**
     * Notification about start phase in device context, right after successful handshake
     * @param deviceInfo
     * @param success or failure
     */
    void deviceStartInitializationDone(final DeviceInfo deviceInfo, final boolean success);

    /**
     * Notification about start phase in device context, after all other contexts initialized properly
     * @param deviceInfo
     * @param success
     */
    void deviceInitializationDone(final DeviceInfo deviceInfo, final boolean success);

}
