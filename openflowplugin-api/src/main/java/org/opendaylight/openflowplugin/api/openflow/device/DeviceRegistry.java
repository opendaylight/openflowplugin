/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;

/**
 * Flows, Groups and Meter registry.
 */
public interface DeviceRegistry {

    /**
     * Method exposes flow registry used for storing flow ids identified by calculated flow hash.
     * @return DeviceFlowRegistry
     */
    DeviceFlowRegistry getDeviceFlowRegistry();

    /**
     * Method exposes device group registry used for storing group ids.
     * @return DeviceGroupRegistry
     */
    DeviceGroupRegistry getDeviceGroupRegistry();

    /**
     * Method exposes device meter registry used for storing meter ids.
     * @return DeviceMaterRegistry
     */
    DeviceMeterRegistry getDeviceMeterRegistry();

}
