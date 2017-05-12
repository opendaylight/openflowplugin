/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;

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
     * @return Future
     */
    ListenableFuture<Void> stopChain();

    @Override
    void close();

    /**
     * Method need to be called if connection is dropped to stop the chain.
     * @return future
     */
    ListenableFuture<Void> connectionDropped();

    /**
     * Slave was successfully set.
     */
    void makeContextChainStateSlave();

    /**
     * Registers context chain into cluster singleton service.
     * @param clusterSingletonServiceProvider provider
     */
    void registerServices(final ClusterSingletonServiceProvider clusterSingletonServiceProvider);

    /**
     * After connect of device make this device SLAVE.
     */
    void makeDeviceSlave();

    /**
     * Check all needed to be master.
     * @param mastershipState - state master on device, initial gather, initial submit, initial registry fill
     * @return true if everything done fine
     */
    boolean isMastered(@Nonnull final ContextChainMastershipState mastershipState);

    /**
     * Device need to be in state SLAVE or MASTER.
     * @return false if in undefined state
     */
    boolean hasState();

    /**
     * Add new auxiliary connection if primary is ok.
     * @param connectionContext new connection to the device.
     * @return false if primary connection is broken
     */
    boolean addAuxiliaryConnection(@Nonnull final ConnectionContext connectionContext);

    /**
     * Check if connection is auxiliary and if yes then continue working.
     * @param connectionContext connection to the device
     * @return false if this is primary connection
     */
    boolean auxiliaryConnectionDropped(@Nonnull final ConnectionContext connectionContext);
}
