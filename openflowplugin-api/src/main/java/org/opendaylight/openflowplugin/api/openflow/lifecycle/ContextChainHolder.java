/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipListener;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;

/**
 * Generic interface for context chain holder, hold all created context chains.
 * {@link ContextChain} is context that suppose to hold old information about device such as
 * <ul>
 *     <li>{@link DeviceContext}</li>
 *     <li>{@link RpcContext}</li>
 *     <li>{@link StatisticsContext}</li>
 * </ul>
 * Each context is created right after device connect and hold information about particular part of device.
 * @since 0.4.0 Carbon
 * @see StatisticsContext
 * @see RpcContext
 * @see DeviceContext
 */
public interface ContextChainHolder extends
        DeviceConnectedHandler,
        ContextChainMastershipWatcher,
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

    /**
     * Return the {@link ContextChain} for a given {@link DeviceInfo}.
     * @return {@link ContextChain}
     */
    ContextChain getContextChain(DeviceInfo deviceInfo);

    @Override
    void close() throws Exception;

}
