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
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.rpc.listener.ItemLifecycleListener;

/**
 * Context for statistics
 */
public interface StatisticsContext extends RequestContextStack, AutoCloseable, OFPContext {

    /**
     * Gather data from device
     * @return true if gathering was successful
     */
    ListenableFuture<Boolean> gatherDynamicData();

    /**
     * Initial data gathering
     * @return true if gathering was successful
     */
    ListenableFuture<Boolean> initialGatherDynamicData();

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
     * @return handle to currently scheduled statistics polling
     */
    Optional<Timeout> getPollTimeout();

    /**
     * @return dedicated item life cycle change listener (per device)
     */
    ItemLifecycleListener getItemLifeCycleListener();

    @Override
    void close();

    void setSchedulingEnabled(boolean schedulingEnabled);
    boolean isSchedulingEnabled();
}
