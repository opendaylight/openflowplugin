/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Listener if able to start mastership for device
 */
public interface MastershipChangeListener {

    /**
     * Event occurs if there was a try to acquire MASTER role
     * but it was not possible to start this MASTER role on device
     * @param deviceInfo for this device
     */
    void onNotAbleToStartMastership(final DeviceInfo deviceInfo);

    /**
     * changed to MASTER role on device
     * @param deviceInfo device
     */
    void onMasterRoleAcquired(final DeviceInfo deviceInfo);

    /**
     * changed to SLAVE role on device
     * @param deviceInfo device
     */
    void onSlaveRoleAcquired(final DeviceInfo deviceInfo);

    /**
     * change to SLAVE role on device was not able
     * @param deviceInfo
     */
    void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo);
}
