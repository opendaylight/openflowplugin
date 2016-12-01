/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceConnectedHandler;

/**
 * Generic interface for context chain holder, hold all created context chains.
 */
public interface ContextChainHolder extends DeviceConnectedHandler, MastershipChangeListener {

    <T extends OFPManager> void addManager(final T manager);
    ContextChain createContextChain(final ConnectionContext connectionContext);
    ListenableFuture<Void> connectionLost(final DeviceInfo deviceInfo);
    void destroyContextChain(final DeviceInfo deviceInfo);

    /**
     * This method will pair up connection with existing context chain.
     * If context chain doesn't exist will create context chain a
     * set this connection as primary to the new created context chain.
     * If context chain cannot be created close connection and destroy context chain.
     * @param connectionContext new connection
     */
    void pairConnection(final ConnectionContext connectionContext);

    void addSingletonServicesProvider(final ClusterSingletonServiceProvider singletonServicesProvider);
}
