/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipListener;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;

/**
 * Generic interface for context chain holder, hold all created context chains.
 */
public interface ContextChainHolder extends
        DeviceConnectedHandler,
        MastershipChangeListener,
        DeviceDisconnectedHandler,
        EntityOwnershipListener,
        AutoCloseable{

    /**
     * Managers need to be added before.
     * {@link DeviceManager}
     * {@link RpcManager}
     * {@link StatisticsManager}
     * @param manager a child class of {@link OFPManager}
     * @param <T> {@link OFPManager}
     */
    <T extends OFPManager> void addManager(final T manager);

    /**
     * Create a new context chain.
     * @param connectionContext new connection
     * @return {@link ContextChain}
     */
    ContextChain createContextChain(final ConnectionContext connectionContext);

    /**
     * Called if connection needs to be destroyed.
     * @param deviceInfo {@link DeviceInfo}
     */
    ListenableFuture<Void> destroyContextChain(final DeviceInfo deviceInfo);

    /**
     * Provider is needed to register cluster singleton service.
     * @param singletonServicesProvider provider
     */
    void addSingletonServicesProvider(final ClusterSingletonServiceProvider singletonServicesProvider);

    /**
     * Register EOS listener.
     * @param entityOwnershipService EOS services
     */
    void changeEntityOwnershipService(final EntityOwnershipService entityOwnershipService);

    @Override
    void close() throws Exception;

}
