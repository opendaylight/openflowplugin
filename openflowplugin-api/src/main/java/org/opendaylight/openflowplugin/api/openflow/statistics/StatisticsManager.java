/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceTerminationPhaseHandler;

/**
 * Manager to start or stop scheduling statistics
 */
public interface StatisticsManager extends DeviceLifecycleSupervisor, DeviceInitializationPhaseHandler,
        DeviceTerminationPhaseHandler, AutoCloseable {

    /**
     * Start scheduling statistic gathering for given device info
     * @param deviceInfo for this device should be running statistics gathering
     */
    void startScheduling(final DeviceInfo deviceInfo);

    /**
     * Stop scheduling statistic gathering for given device info
     * @param deviceInfo for this device should be stopped statistics gathering
     */
    void stopScheduling(final DeviceInfo deviceInfo);

    @Override
    void close();

    void setIsStatisticsPollingOn(boolean isStatisticsPollingOn);

}
