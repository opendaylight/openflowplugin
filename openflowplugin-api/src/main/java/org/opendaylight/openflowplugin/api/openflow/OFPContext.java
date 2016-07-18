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

    /**
     * distinguished device context states
     */
    enum CONTEXT_STATE {
        /**
         * initial phase
         */
        INITIALIZATION,
        /**
         * standard phase
         */
        WORKING,
        /**
         * termination phase
         */
        TERMINATION
    }

    CONTEXT_STATE getState();

    void setState(final CONTEXT_STATE state);

    default void startupClusterServices() throws ExecutionException, InterruptedException {
        throw new InterruptedException("Cannot start abstract service, check implementation of cluster services");
    }

    default ListenableFuture<Void> stopClusterServices(){
        return Futures.immediateFailedFuture(new RejectedExecutionException("Cannot stop abstract services, check implementation of cluster services"));
    }

    ServiceGroupIdentifier getServiceIdentifier();

    DeviceInfo getDeviceInfo();

}
