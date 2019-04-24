/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.OwnershipChangeListener;

/**
 * This service is for registering any application intent to be informed about device mastership changes.
 * Service provides three events.
 * <ul>
 *     <li><i>{@link #onBecomeOwner(DeviceInfo)}</i> is called when device is fully mastered by controller</li>
 *     <li><i>{@link #onLoseOwnership(DeviceInfo)}</i>
 *     is called when device is disconnected or is being to be slave</li>
 * </ul>
 * There is no need to have two different method for slave or disconnect because application just have to stop working
 * with the device in both cases.
 * @since 0.5.0 Nitrogen
 */
public interface MastershipChangeService extends AutoCloseable {

    /**
     * Event when device is ready as a master. This event is evoked by
     * {@link OwnershipChangeListener#becomeMaster(DeviceInfo)}
     * @param deviceInfo connected switch identification
     */
    void onBecomeOwner(@NonNull DeviceInfo deviceInfo);

    /**
     * Event when device disconnected or become slave. This event is evoked by
     * {@link OwnershipChangeListener#becomeSlaveOrDisconnect(DeviceInfo)}
     * @param deviceInfo connected switch identification
     */
    void onLoseOwnership(@NonNull DeviceInfo deviceInfo);

}