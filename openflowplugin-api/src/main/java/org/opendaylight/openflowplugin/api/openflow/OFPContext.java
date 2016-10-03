/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.RejectedExecutionException;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterLifecycleSupervisor;

/**
 * General API for all OFP Context
 */
public interface OFPContext extends AutoCloseable, ClusterLifecycleSupervisor, ClusterInitializationPhaseHandler {
    /**
     * Context state
     */
    enum CONTEXT_STATE {
        /* Initialization phase, context not yet fully initialized */
        INITIALIZATION,
        /* Standard working phase everything is fine */
        WORKING,
        /* Termination phase context is being shutting down */
        TERMINATION
    }

    /**
     * Get actual context state
     * @return actual context state
     */
    CONTEXT_STATE getState();

    /**
     * About to stop services in cluster not master anymore or going down
     * @return Future most of services need time to be closed
     * @param connectionInterrupted true if clustering services stopping by device disconnect
     */
    default ListenableFuture<Void> stopClusterServices(boolean connectionInterrupted) {
        return Futures.immediateFailedFuture(new RejectedExecutionException("Cannot stop abstract services, check implementation of cluster services"));
    }

    /**
     * Get cluster singleton service identifier
     * @return cluster singleton service identifier
     */
    ServiceGroupIdentifier getServiceIdentifier();

    /**
     * Get device info
     * @return device info
     */
    DeviceInfo getDeviceInfo();

    @Override
    void close();
}
