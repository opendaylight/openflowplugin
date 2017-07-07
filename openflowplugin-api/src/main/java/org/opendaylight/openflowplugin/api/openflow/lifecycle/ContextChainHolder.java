/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;

/**
 * Generic interface for context chain holder, hold all created context chains.
 */
public interface ContextChainHolder extends
        DeviceConnectedHandler,
        MastershipChangeListener,
        DeviceDisconnectedHandler,
        DeviceRemovedHandler,
        EntityOwnershipListener,
        AutoCloseable {

    /**
     * Managers need to be added before.
     * {@link DeviceManager}
     * {@link RpcManager}
     * {@link StatisticsManager}
     * @param manager a child class of {@link OFPManager}
     * @param <T> {@link OFPManager}
     */
    <T extends OFPManager> void addManager(T manager);

    @Override
    void close() throws Exception;

}
