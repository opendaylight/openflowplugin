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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleServiceImpl implements LifecycleService {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleServiceImpl.class);

    private boolean inClosing = false;
    private DeviceContext deviceContext;
    private RpcContext rpcContext;
    private StatisticsContext statContext;
    private ClusterSingletonServiceRegistration registration;
    private ClusterInitializationPhaseHandler clusterInitializationPhaseHandler;


    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting clustering MASTER services for node {}", this.deviceContext.getDeviceInfo().getLOGValue());

        if (!this.clusterInitializationPhaseHandler.onContextInstantiateService(null)) {
            this.closeConnection();
        }

    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("Closing clustering MASTER services for node {}", this.deviceContext.getDeviceInfo().getLOGValue());

        final boolean connectionInterrupted =
                this.deviceContext
                        .getPrimaryConnectionContext()
                        .getConnectionState()
                        .equals(ConnectionContext.CONNECTION_STATE.RIP);

        // If connection was interrupted and we are not trying to close service, then we received something
        // we do not wanted to receive, so do not continue
        if (connectionInterrupted && !inClosing) {
            LOG.warn("Failed to close clustering MASTER services for node {} because they are already closed",
                    LifecycleServiceImpl.this.deviceContext.getDeviceInfo().getLOGValue());

            return Futures.immediateCancelledFuture();
        }

        // Chain all jobs that will stop our services
        final List<ListenableFuture<Void>> futureList = new ArrayList<>();
        futureList.add(statContext.stopClusterServices(connectionInterrupted));
        futureList.add(rpcContext.stopClusterServices(connectionInterrupted));
        futureList.add(deviceContext.stopClusterServices(connectionInterrupted));

        // When we stopped all jobs then we are not in closing state anymore (at least from plugin perspective)
        return Futures.transform(Futures.successfulAsList(futureList), new Function<List<Void>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable List<Void> input) {
                LOG.debug("Closed clustering MASTER services for node {}",
                        LifecycleServiceImpl.this.deviceContext.getDeviceInfo().getLOGValue());
                return null;
            }
        });
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceContext.getServiceIdentifier();
    }


    @Override
    public void close() throws Exception {
        // If we are still registered and we are not already closing, then close the registration
        if (Objects.nonNull(registration) && !inClosing) {
            inClosing = true;
            registration.close();
            registration = null;
        }
    }

    @Override
    public void registerService(final ClusterSingletonServiceProvider singletonServiceProvider) {
        LOG.info("Registering clustering MASTER services for node {}", this.deviceContext.getDeviceInfo().getLOGValue());

        //lifecycle service -> device context -> statistics context -> rpc context -> role context -> lifecycle service
        this.clusterInitializationPhaseHandler = deviceContext;
        this.deviceContext.setLifecycleInitializationPhaseHandler(this.statContext);
        this.statContext.setLifecycleInitializationPhaseHandler(this.rpcContext);
        this.rpcContext.setLifecycleInitializationPhaseHandler(this);
        //Set initial submit handler
        this.statContext.setInitialSubmitHandler(this.deviceContext);
        //Register cluster singleton service
        this.registration = singletonServiceProvider.registerClusterSingletonService(this);
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

        if (ConnectionContext.CONNECTION_STATE.RIP.equals(connectionContext.getConnectionState())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Connection to the device {} was interrupted.", this.deviceContext.getDeviceInfo().getLOGValue());
            }
            return false;
        }

        fillDeviceFlowRegistry();
        return true;
    }

    private class DeviceFlowRegistryCallback implements FutureCallback<List<Optional<FlowCapableNode>>> {
        private final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill;

        public DeviceFlowRegistryCallback(ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill) {
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

                LOG.debug("Finished filling flow registry with {} flows for node: {}", flowCount, deviceContext.getDeviceInfo().getLOGValue());
            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (deviceFlowRegistryFill.isCancelled()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Cancelled filling flow registry with flows for node: {}", deviceContext.getDeviceInfo().getLOGValue());
                }
            } else {
                LOG.warn("Failed filling flow registry with flows for node: {} with exception: {}", deviceContext.getDeviceInfo().getLOGValue(), t);
            }
        }
    }
}
