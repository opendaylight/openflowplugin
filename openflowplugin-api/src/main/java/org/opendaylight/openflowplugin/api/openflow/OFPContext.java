/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import com.google.common.util.concurrent.Service;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;

/**
 * General API for all OFP Context.
 */
public interface OFPContext extends AutoCloseable, Service {
    /**
     * Get device info.
     * @return device info
     */
    DeviceInfo getDeviceInfo();

    /**
     * Registers mastership change listener to context.
     * @param contextChainMastershipWatcher mastership change listener
     */
    void registerMastershipWatcher(@Nonnull ContextChainMastershipWatcher contextChainMastershipWatcher);

    /**
     * If the service state is {@link State#NEW}, this initiates service startup and waits
     * until it was started. A stopped service may not be restarted.
     *
     * @throws IllegalStateException if the service is not {@link State#NEW}
     */
    default void start() {
        startAsync();
        awaitRunning();
    }

    /**
     * If the service is {@linkplain State#STARTING starting} or {@linkplain State#RUNNING running},
     * this initiates service shutdown and waits until it was terminated. If the service is
     * {@linkplain State#NEW new}, it is {@linkplain State#TERMINATED terminated} without having been
     * started nor stopped. If the service has already been stopped, this method returns immediately
     * without taking action.
     */
    default void stop() {
        stopAsync();
        awaitTerminated();
    }

    @Override
    default void close() throws Exception {
        stop();
    }
}