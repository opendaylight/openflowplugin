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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;

/**
 * General API for all OFP Context
 */
public interface OFPContext {

    default void setState(CONTEXT_STATE contextState) {
        //NOOP
    }

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
     * @return actual context state
     */
    CONTEXT_STATE getState();

    /**
     * @param state set state
     */
    void setState(final CONTEXT_STATE state);

    /**
     * Starting cluster services for context becoming master
     */
    default void startupClusterServices() throws ExecutionException, InterruptedException {
        throw new InterruptedException("Cannot start abstract service, check implementation of cluster services");
    }

    /**
     * About to stop services in cluster not master anymore or going down
     * @return Future most of services need time to be closed
     */
    default ListenableFuture<Void> stopClusterServices(){
        return Futures.immediateFailedFuture(new RejectedExecutionException("Cannot stop abstract services, check implementation of cluster services"));
    }

    /**
     * @return cluster singleton service identifier
     */
    ServiceGroupIdentifier getServiceIdentifier();

    /**
     * @return device info
     */
    DeviceInfo getDeviceInfo();

}
