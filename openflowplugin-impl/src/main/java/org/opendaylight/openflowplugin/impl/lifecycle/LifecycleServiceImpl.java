/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
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

            LOG.info("Starting device context cluster services for node {}", getIdentifier());
            this.deviceContext.startupClusterServices();

            LOG.info("Starting statistics context cluster services for node {}", getIdentifier());
            this.statContext.startupClusterServices();

            LOG.info("Starting rpc context cluster services for node {}", getIdentifier());
            this.rpcContext.startupClusterServices();

            LOG.info("Starting role context cluster services for node {}", getIdentifier());
            this.roleContext.startupClusterServices();

        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Cluster service {} was unable to start.", this.getIdentifier());
        }
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
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
}
