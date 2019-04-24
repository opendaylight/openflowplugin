/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Watcher if able to start mastership for device.
 * @since 0.4.0 Carbon
 */
public interface ContextChainMastershipWatcher {

    /**
     * Event occurs if there was a try to acquire MASTER role.
     * But it was not possible to start this MASTER role on device.
     * @param deviceInfo connected switch identification
     * @param reason reason
     * @param mandatory if it is mandatory connection will be dropped
     */
    void onNotAbleToStartMastership(DeviceInfo deviceInfo, String reason, boolean mandatory);

    /**
     * Event occurs if there was a try to acquire MASTER role.
     * But it was not possible to start this MASTER role on device.
     * @param deviceInfo connected switch identification
     * @param reason reason
     */
    default void onNotAbleToStartMastershipMandatory(DeviceInfo deviceInfo, String reason) {
        onNotAbleToStartMastership(deviceInfo, reason, true);
    }

    /**
     * Changed to MASTER role on device.
     * @param deviceInfo connected switch identification
     * @param mastershipState state
     */
    void onMasterRoleAcquired(DeviceInfo deviceInfo, @NonNull ContextChainMastershipState mastershipState);

    /**
     * Change to SLAVE role on device was successful.
     * @param deviceInfo connected switch identification
     */
    void onSlaveRoleAcquired(DeviceInfo deviceInfo);

    /**
     * Change to SLAVE role on device was not able.
     * @param deviceInfo connected switch identification
     * @param reason reason
     */
    void onSlaveRoleNotAcquired(DeviceInfo deviceInfo, String reason);
}
