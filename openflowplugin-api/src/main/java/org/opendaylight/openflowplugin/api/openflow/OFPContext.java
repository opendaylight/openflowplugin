/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;

/**
 * General API for all OFP Context.
 */
public interface OFPContext extends AutoCloseable, ClusterInitializationPhaseHandler {

    /**
     * Context state.
     */
    enum ContextState {
        /* Initialization phase, context not yet fully initialized */
        INITIALIZATION,
        /* Standard working phase everything is fine */
        WORKING,
        /* Termination phase context is being shutting down */
        TERMINATION
    }

    /**
     * About to stop services in cluster not master anymore or going down.
     * @return Future most of services need time to be closed.
     */
    ListenableFuture<Void> stopClusterServices();

    /**
     * Get device info.
     * @return device info
     */
    DeviceInfo getDeviceInfo();

    @Override
    void close();
}
