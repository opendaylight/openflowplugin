/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;

import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.Timeout;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;

/**
 * Context for statistics
 */
public interface StatisticsContext extends RequestContextStack, OFPContext {

    /**
     * Gather data from device
     * @return true if gathering was successful
     */
    ListenableFuture<Boolean> gatherDynamicData();

    /**
     * Method has to be called from DeviceInitialization Method, otherwise
     * we are not able to poll anything. Statistics Context normally initialize
     * this part by initialization process but we don't have this information
     * in initialization phase and we have to populate whole list after every
     * device future collecting. Because device future collecting set DeviceState
     * and we creating marks for the correct kind of stats from DeviceState.
     */
    void statListForCollectingInitialization();

    /**
     * @param pollTimeout handle to nearest scheduled statistics poll
     */
    void setPollTimeout(Timeout pollTimeout);

    /**
     * On / Off scheduling
     * @param schedulingEnabled true if scheduling should be enabled
     */
    void setSchedulingEnabled(final boolean schedulingEnabled);

    /**
     * Check status
     * @return true if scheduling is enabled
     */
    boolean isSchedulingEnabled();

    /**
     * Gain device state
     * @return device state from device context from lifecycle service
     */
    DeviceState gainDeviceState();

    /**
     * Gain device context
     * @return device context from lifecycle service
     */
    DeviceContext gainDeviceContext();

    /**
     * In case to change mastership to slave or connection interrupted stop the future and release thread
     */
    void stopGatheringData();
}
