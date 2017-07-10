/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * Internal OFP interface.
 */
public interface MasterChecker {

    /**
     * Mastered devices.
     * @return list of mastered devices
     */
    List<DeviceInfo> listOfMasteredDevices();

    /**
     * Check if any device is mastered by controller.
     * @return true if there is at least one device mastered by controller
     */
    boolean isAnyDeviceMastered();

}