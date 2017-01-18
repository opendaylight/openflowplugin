/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;

/**
 * Chain of contexts, hold references to the contexts.
 */
public interface ContextChain extends AutoCloseable {

    /**
     * Add context to the chain, if reference already exist ignore it.
     * @param context child of OFPContext
     */
    <T extends OFPContext> void addContext(final T context);

    void addLifecycleService(final LifecycleService lifecycleService);

    /**
     * Stop the working contexts, but not release them.
     * @param connectionDropped true if stop the chain due to connection drop
     * @return Future
     */
    ListenableFuture<Void> stopChain(boolean connectionDropped);

    /**
     * Start the contexts, if some context is missing or cant be started returns failed future.
     * @return Future
     */
    ListenableFuture<Void> startChain();

    @Override
    void close();

    /**
     * Change connection if connection were drop and rebuild.
     * @param connectionContext new connection
     */
    void changePrimaryConnection(final ConnectionContext connectionContext);

    /**
     * Method need to be called if connection is dropped to stop the chain.
     * @return future
     */
    ListenableFuture<Void> connectionDropped();

    /**
     * Returns context chain state.
     * @return state
     */
    ContextChainState getContextChainState();

    /**
     * Sleep the chain and drop connection.
     */
    void sleepTheChainAndDropConnection();

    /**
     * Registers context chain into cluster singleton service.
     * @param clusterSingletonServiceProvider provider
     */
    void registerServices(@NonNull final ClusterSingletonServiceProvider clusterSingletonServiceProvider);

    /**
     * After connect of device make this device SLAVE.
     */
    void makeDeviceSlave();

    /**
     * if something goes wrong close the connection.
     */
    void closePrimaryConnection();

    /**
     * Getter.
     * @return actual primary connection
     */
    ConnectionContext getPrimaryConnectionContext();
}
