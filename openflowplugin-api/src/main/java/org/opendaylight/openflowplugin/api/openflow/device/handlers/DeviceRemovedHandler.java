/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device.handlers;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Represents handler for device that was disconnected but needs to be removed from it's manager.
 */
public interface DeviceRemovedHandler {

    /**
     * Method is used to propagate information about device being removed from manager.
     */
    void onDeviceRemoved(final DeviceInfo deviceInfo);
}
