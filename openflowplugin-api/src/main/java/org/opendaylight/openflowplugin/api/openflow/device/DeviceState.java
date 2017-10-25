/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.device;

/**
 * Holder of device's structure.
 */
public interface DeviceState {

    /**
     * Return true if we have relevant meter information
     * from device.
     */
    boolean isMetersAvailable();

    /**
     * Set information about meter statistics availability.
     */
    void setMeterAvailable(boolean available);

    /**
     * Return true if we have relevant group information
     * from device.
     */
    boolean isGroupAvailable();

    /**
     * Set information about group statistics availability.
     */
    void setGroupAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides flow statistics.
     */
    boolean isFlowStatisticsAvailable();

    void setFlowStatisticsAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides table statistics.
     */
    boolean isTableStatisticsAvailable();

    void setTableStatisticsAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides flow statistics.
     */
    boolean isPortStatisticsAvailable();

    void setPortStatisticsAvailable(boolean available);

    /**
     * Method returns true, if device capabilities provides queue statistics.
     */
    boolean isQueueStatisticsAvailable();

    void setQueueStatisticsAvailable(boolean available);
}
