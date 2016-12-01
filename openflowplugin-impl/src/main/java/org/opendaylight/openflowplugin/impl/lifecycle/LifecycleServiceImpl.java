/**
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Verify;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainHolder;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleServiceImpl implements LifecycleService {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleServiceImpl.class);
    private static final String INITIALIZATION_PHASE_HANDLER_CANNOT_BE_NULL
            = "Initialization phase handler cannot be null.";
    private static final String SINGLETON_SERVICE_PROVIDER_CANNOT_BE_NULL
            = "Singleton service provider cannot be null.";
    private static final String SERVICE_GROUP_IDENTIFIER_CANNOT_BE_NULL
            = "Service group identifier cannot be null.";
    private static final String DEVICE_INFO_CANNOT_BE_NULL
            = "Device info cannot be null.";
    private static final String MASTERSHIP_LISTENER_CANNOT_BE_NULL
            = "Mastership listener cannot be null.";

    private ClusterSingletonServiceRegistration registration;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;
    private final List<DeviceRemovedHandler> deviceRemovedHandlers = new ArrayList<>();
    private ServiceGroupIdentifier serviceGroupIdentifier;
    private DeviceInfo deviceInfo;
    private boolean terminationState = false;
    private final MastershipChangeListener mastershipChangeListener;


    public LifecycleServiceImpl(final MastershipChangeListener mastershipChangeListener) {
        Objects.requireNonNull(mastershipChangeListener, MASTERSHIP_LISTENER_CANNOT_BE_NULL);
        this.mastershipChangeListener = mastershipChangeListener;
    }

    @Override
    public void instantiateServiceInstance() {

        LOG.info("Starting clustering MASTER services for node {}", deviceInfo.getLOGValue());
        if (!clusterInitializationPhaseHandler.onContextInstantiateService(null)) {
            mastershipChangeListener.onNotAbleToStartMastership(deviceInfo);
        }

    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        final boolean connectionInterrupted =
                this.deviceContext
                        .getPrimaryConnectionContext()
                        .getConnectionState()
                        .equals(ConnectionContext.CONNECTION_STATE.RIP);

        // Chain all jobs that will stop our services
        final List<ListenableFuture<Void>> futureList = new ArrayList<>();
        futureList.add(statContext.stopClusterServices(connectionInterrupted));
        futureList.add(rpcContext.stopClusterServices(connectionInterrupted));
        futureList.add(deviceContext.stopClusterServices(connectionInterrupted));

        return Futures.transform(Futures.successfulAsList(futureList), new Function<List<Void>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable List<Void> input) {
                LOG.debug("Closed clustering MASTER services for node {}", deviceInfo.getLOGValue());
                return null;
            }
        });
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return this.serviceGroupIdentifier;
    }

    @Override
    public void close() {
        if (terminationState){
            if (LOG.isDebugEnabled()) {
                LOG.debug("LifecycleService is already in TERMINATION state.");
            }
        } else {
            this.terminationState = true;

            // We are closing, so cleanup all managers now
            deviceRemovedHandlers.forEach(h -> h.onDeviceRemoved(deviceInfo));

            // If we are still registered and we are not already closing, then close the registration
            if (Objects.nonNull(registration)) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Closing clustering services for node {}", deviceInfo.getLOGValue());
                    }
                    registration.close();
                } catch (Exception e) {
                    LOG.debug("Failed to close clustering services for node {} with exception: ",
                            deviceInfo.getLOGValue(), e);
                }
            }
        }
    }

    @Override
    public void registerService(@CheckForNull final ClusterSingletonServiceProvider singletonServiceProvider,
                                @CheckForNull final ClusterInitializationPhaseHandler initializationPhaseHandler,
                                @CheckForNull final ServiceGroupIdentifier serviceGroupIdentifier,
                                @CheckForNull final DeviceInfo deviceInfo) {

        Objects.requireNonNull(singletonServiceProvider, SINGLETON_SERVICE_PROVIDER_CANNOT_BE_NULL);
        this.clusterInitializationPhaseHandler
                = Objects.requireNonNull(initializationPhaseHandler, INITIALIZATION_PHASE_HANDLER_CANNOT_BE_NULL);
        this.serviceGroupIdentifier
                = Objects.requireNonNull(serviceGroupIdentifier, SERVICE_GROUP_IDENTIFIER_CANNOT_BE_NULL);
        this.registration = Verify.verifyNotNull(
                singletonServiceProvider.registerClusterSingletonService(this));
        this.deviceInfo = Objects.requireNonNull(deviceInfo, DEVICE_INFO_CANNOT_BE_NULL);

        LOG.info("Registered clustering services for node {}", deviceInfo.getLOGValue());

    }

    @Override
    public void registerDeviceRemovedHandler(final DeviceRemovedHandler deviceRemovedHandler) {
        if (!deviceRemovedHandlers.contains(deviceRemovedHandler)) {
            deviceRemovedHandlers.add(deviceRemovedHandler);
        }
    }
}
