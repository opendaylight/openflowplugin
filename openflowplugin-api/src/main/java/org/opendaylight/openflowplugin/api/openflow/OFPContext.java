/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import javax.annotation.Nonnull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;

/**
 * General API for all OFP Context.
 */
public interface OFPContext extends AutoCloseable, ClusterSingletonService {

    /**
     * Context state.
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
     * Get device info.
     * @return device info
     */
    DeviceInfo getDeviceInfo();

    /**
     * Registers mastership change listener to context.
     * @param mastershipChangeListener mastership change listener
     */
    void registerMastershipChangeListener(@Nonnull MastershipChangeListener mastershipChangeListener);

    @Override
    void close();
}