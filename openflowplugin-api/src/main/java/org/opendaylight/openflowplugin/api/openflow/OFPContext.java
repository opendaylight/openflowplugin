/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;

/**
 * General API for all OFP Context.
 */
public interface OFPContext extends AutoCloseable, ClusterSingletonService {
    /**
     * Get device info.
     * @return device info
     */
    DeviceInfo getDeviceInfo();

    /**
     * Registers mastership change listener to context.
     * @param contextChainMastershipWatcher mastership change listener
     */
    void registerMastershipWatcher(@NonNull ContextChainMastershipWatcher contextChainMastershipWatcher);

    @Override
    void close();
}
