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
public interface MastershipChangeListener {

    /**
     * Event occurs if there was a try to acquire MASTER role.
     * But it was not possible to start this MASTER role on device.
     * @param deviceInfo for this device
     * @param reason reason
     * @param mandatory if it is mandatory connection will be dropped
     */
    void onNotAbleToStartMastership(final DeviceInfo deviceInfo, @Nonnull final String reason, final boolean mandatory);

    /**
     * Event occurs if there was a try to acquire MASTER role.
     * But it was not possible to start this MASTER role on device.
     * @param deviceInfo for this device
     * @param reason reason
     */
    default void onNotAbleToStartMastershipMandatory(final DeviceInfo deviceInfo, @Nonnull final String reason) {
        onNotAbleToStartMastership(deviceInfo, reason, true);
    }

    /**
     * Changed to MASTER role on device.
     * @param deviceInfo device
     * @param mastershipState
     */
    void onMasterRoleAcquired(final DeviceInfo deviceInfo, @Nonnull final ContextChainMastershipState mastershipState);

    /**
     * Change to SLAVE role on device was successful.
     * @param deviceInfo device
     */
    void onSlaveRoleAcquired(final DeviceInfo deviceInfo);

    /**
     * Change to SLAVE role on device was not able.
     * @param deviceInfo device
     */
    void onSlaveRoleNotAcquired(final DeviceInfo deviceInfo);
}
