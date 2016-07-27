/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifecycleServiceImpl implements LifecycleService {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleServiceImpl.class);

    private DeviceContext deviceContext;
    private RpcContext rpcContext;
    private RoleContext roleContext;
    private StatisticsContext statContext;
    private ClusterSingletonServiceRegistration registration;


    @Override
    public void instantiateServiceInstance() {
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Starting clustering MASTER services for node {}", this.deviceContext.getDeviceInfo().getNodeId().getValue());
                LOG.debug("===============================================");
            }

            if (connectionInterrupted()) {
                return;
            }

            LOG.info("Starting device context cluster services for node {}", getIdentifier());
            this.deviceContext.startupClusterServices();

            if (connectionInterrupted()) {
                return;
            }

            LOG.info("Starting statistics context cluster services for node {}", getIdentifier());
            this.statContext.startupClusterServices();

            if (connectionInterrupted()) {
                return;
            }

            LOG.info("Statistics initial gathering OK, submitting data for node {}", getIdentifier());
            this.deviceContext.initialSubmitTransaction();

            if (connectionInterrupted()) {
                return;
            }

            LOG.info("Starting rpc context cluster services for node {}", getIdentifier());
            this.rpcContext.startupClusterServices();

            if (connectionInterrupted()) {
                return;
            }

            LOG.info("Starting role context cluster services for node {}", getIdentifier());
            this.roleContext.startupClusterServices();

            if (connectionInterrupted()) {
                return;
            }

            LOG.info("Caching flows IDs ...");
            fillDeviceFlowRegistry();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Cluster service {} was unable to start.", this.getIdentifier());
            this.deviceContext.shutdownConnection();
        }
    }

    private boolean connectionInterrupted() {
        if (this.deviceContext.getPrimaryConnectionContext().getConnectionState().equals(ConnectionContext.CONNECTION_STATE.RIP)) {
            LOG.warn("Node {} was disconnected, will stop starting MASTER services.", this.deviceContext.getDeviceInfo().getNodeId().getValue());
            return true;
        }
        return false;
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        roleContext.stopClusterServices();
        statContext.stopClusterServices();
        rpcContext.stopClusterServices();
        return deviceContext.stopClusterServices();
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceContext.getServiceIdentifier();
    }


    @Override
    public void close() throws Exception {
        if (registration != null) {
            registration.close();
            registration = null;
        }
    }

    @Override
    public void registerService(final ClusterSingletonServiceProvider singletonServiceProvider) {
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
    public void setRoleContext(final RoleContext roleContext) {
        this.roleContext = roleContext;
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
        // Fill device flow registry with flows from datastore
        final ListenableFuture<List<Optional<FlowCapableNode>>> deviceFlowRegistryFill = deviceContext.getDeviceFlowRegistry().fill();

        // Start statistics scheduling only after we finished initializing device flow registry
        Futures.addCallback(deviceFlowRegistryFill, new FutureCallback<List<Optional<FlowCapableNode>>>() {
            @Override
            public void onSuccess(@Nullable List<Optional<FlowCapableNode>> result) {
                if (LOG.isDebugEnabled()) {
                    // Count all flows we read from datastore for debugging purposes.
                    // This number do not always represent how many flows were actually added
                    // to DeviceFlowRegistry, because of possible duplicates.
                    long flowCount = Optional.fromNullable(result).asSet().stream()
                            .flatMap(Collection::stream)
                            .flatMap(flowCapableNodeOptional -> flowCapableNodeOptional.asSet().stream())
                            .flatMap(flowCapableNode -> flowCapableNode.getTable().stream())
                            .flatMap(table -> table.getFlow().stream())
                            .count();

                    LOG.debug("Finished filling flow registry with {} flows for node: {}", flowCount, getIdentifier());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (deviceFlowRegistryFill.isCancelled()) {
                    LOG.debug("Cancelled filling flow registry with flows for node: {}", getIdentifier());
                } else {
                    LOG.warn("Failed filling flow registry with flows for node: {} with exception: {}", getIdentifier(), t);
                }
            }
        });
    }

}
