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
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleServiceImpl implements LifecycleService {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleServiceImpl.class);
    private DeviceContext deviceContext;
    private RpcContext rpcContext;
    private StatisticsContext statContext;
    private ClusterSingletonServiceRegistration registration;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;
    private final List<DeviceRemovedHandler> deviceRemovedHandlers = new ArrayList<>();
    private volatile CONTEXT_STATE state = CONTEXT_STATE.INITIALIZATION;


    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting clustering MASTER services for node {}", getDeviceInfo().getLOGValue());

        if (!clusterInitializationPhaseHandler.onContextInstantiateService(null)) {
            closeConnection();
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
                LOG.debug("Closed clustering MASTER services for node {}", getDeviceInfo().getLOGValue());
                return null;
            }
        });
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return getServiceIdentifier();
    }

    @Override
    public CONTEXT_STATE getState() {
        return this.state;
    }

    @Override
    public ServiceGroupIdentifier getServiceIdentifier() {
        return deviceContext.getServiceIdentifier();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceContext.getDeviceInfo();
    }

    @Override
    public void close() {
        if (CONTEXT_STATE.TERMINATION.equals(getState())){
            if (LOG.isDebugEnabled()) {
                LOG.debug("LifecycleService is already in TERMINATION state.");
            }
        } else {
            this.state = CONTEXT_STATE.TERMINATION;

            // We are closing, so cleanup all managers now
            deviceRemovedHandlers.forEach(h -> h.onDeviceRemoved(getDeviceInfo()));

            // If we are still registered and we are not already closing, then close the registration
            if (Objects.nonNull(registration)) {
                try {
                    LOG.debug("Closing clustering MASTER services for node {}", getDeviceInfo().getLOGValue());
                    registration.close();
                } catch (Exception e) {
                    LOG.debug("Failed to close clustering MASTER services for node {} with exception: ",
                            getDeviceInfo().getLOGValue(), e);
                }
            }
        }
    }

    @Override
    public void registerService(final ClusterSingletonServiceProvider singletonServiceProvider) {
        LOG.debug("Registered clustering MASTER services for node {}", getDeviceInfo().getLOGValue());

        // lifecycle service -> device context -> statistics context -> rpc context -> role context -> lifecycle service
        this.clusterInitializationPhaseHandler = deviceContext;
        this.deviceContext.setLifecycleInitializationPhaseHandler(this.statContext);
        this.statContext.setLifecycleInitializationPhaseHandler(this.rpcContext);
        this.rpcContext.setLifecycleInitializationPhaseHandler(this);
        //Set initial submit handler
        this.statContext.setInitialSubmitHandler(this.deviceContext);

        // Register cluster singleton service
        try {
            this.registration = Verify.verifyNotNull(singletonServiceProvider.registerClusterSingletonService(this));
            LOG.info("Registered clustering MASTER services for node {}", getDeviceInfo().getLOGValue());
        } catch (Exception e) {
            LOG.warn("Failed to register cluster singleton service for node {}, with exception: {}", getDeviceInfo(), e);
            closeConnection();
        }
    }

    @Override
    public void registerDeviceRemovedHandler(final DeviceRemovedHandler deviceRemovedHandler) {
        if (!deviceRemovedHandlers.contains(deviceRemovedHandler)) {
            deviceRemovedHandlers.add(deviceRemovedHandler);
        }
    }

    @Override
    public void setDeviceContext(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
    }

    @Override
    public void setRpcContext(final RpcContext rpcContext) {
        this.rpcContext = rpcContext;
    }

    @Override
    public void setStatContext(final StatisticsContext statContext) {
        this.statContext = statContext;
    }

    @Override
    public DeviceContext getDeviceContext() {
        return this.deviceContext;
    }

    @Override
    public void closeConnection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing connection for node {}.", getDeviceInfo().getLOGValue());
        }

        this.deviceContext.shutdownConnection();
    }

    private void fillDeviceFlowRegistry() {
        final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill = deviceContext.getDeviceFlowRegistry().fill();
        Futures.addCallback(deviceFlowRegistryFill, new DeviceFlowRegistryCallback(deviceFlowRegistryFill));
    }

    @Override
    public void setLifecycleInitializationPhaseHandler(final ClusterInitializationPhaseHandler handler) {
        this.clusterInitializationPhaseHandler = handler;
    }

    @Override
    public boolean onContextInstantiateService(final ConnectionContext connectionContext) {
        if (CONNECTION_STATE.RIP.equals(connectionContext.getConnectionState())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connection to the device {} was interrupted.", getDeviceInfo().getLOGValue());
            }

            return false;
        }

        fillDeviceFlowRegistry();
        return true;
    }

    private class DeviceFlowRegistryCallback implements FutureCallback<List<Optional<FlowCapableNode>>> {
        private final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill;

        DeviceFlowRegistryCallback(ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill) {
            this.deviceFlowRegistryFill = deviceFlowRegistryFill;
        }

        @Override
        public void onSuccess(@Nullable List<Optional<FlowCapableNode>> result) {
            if (LOG.isDebugEnabled()) {
                // Count all flows we read from datastore for debugging purposes.
                // This number do not always represent how many flows were actually added
                // to DeviceFlowRegistry, because of possible duplicates.
                long flowCount = Optional.fromNullable(result).asSet().stream()
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .flatMap(flowCapableNodeOptional -> flowCapableNodeOptional.asSet().stream())
                        .filter(Objects::nonNull)
                        .filter(flowCapableNode -> Objects.nonNull(flowCapableNode.getTable()))
                        .flatMap(flowCapableNode -> flowCapableNode.getTable().stream())
                        .filter(Objects::nonNull)
                        .filter(table -> Objects.nonNull(table.getFlow()))
                        .flatMap(table -> table.getFlow().stream())
                        .filter(Objects::nonNull)
                        .count();

                LOG.debug("Finished filling flow registry with {} flows for node: {}", flowCount, getDeviceInfo().getLOGValue());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (deviceFlowRegistryFill.isCancelled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cancelled filling flow registry with flows for node: {}", getDeviceInfo().getLOGValue());
                }
            } else {
                LOG.warn("Failed filling flow registry with flows for node: {} with exception: {}", getDeviceInfo().getLOGValue(), t);
            }
        }
    }
}
