/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Listener if able to start mastership for device.
 */
public interface ContextChainMastershipWatcher {

    /**
     * Event occurs if there was a try to acquire MASTER role.
     * But it was not possible to start this MASTER role on device.
     * @param deviceInfo for this device
     * @param reason reason
     * @param mandatory if it is mandatory connection will be dropped
     */
    void onNotAbleToStartMastership(DeviceInfo deviceInfo, @Nonnull String reason, boolean mandatory);

    /**
     * Event occurs if there was a try to acquire MASTER role.
     * But it was not possible to start this MASTER role on device.
     * @param deviceInfo for this device
     * @param reason reason
     */
    default void onNotAbleToStartMastershipMandatory(DeviceInfo deviceInfo, @Nonnull String reason) {
        onNotAbleToStartMastership(deviceInfo, reason, true);
    }

    /**
     * Changed to MASTER role on device.
     * @param deviceInfo device
     * @param mastershipState state
     */
    void onMasterRoleAcquired(DeviceInfo deviceInfo, @Nonnull ContextChainMastershipState mastershipState);

    /**
     * Change to SLAVE role on device was successful.
     * @param deviceInfo device
     */
    void onSlaveRoleAcquired(DeviceInfo deviceInfo);

    /**
     * Change to SLAVE role on device was not able.
     * @param deviceInfo device
     */
    void onSlaveRoleNotAcquired(DeviceInfo deviceInfo);
}
