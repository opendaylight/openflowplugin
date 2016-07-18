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
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LifecycleServiceImpl implements LifecycleService {

    private static final Logger LOG = LoggerFactory.getLogger(LifecycleServiceImpl.class);

    private final DeviceContext deviceContext;
    private final RpcContext rpcContext;
    private final RoleContext roleContext;
    private final StatisticsContext statContext;

    public LifecycleServiceImpl(
            final DeviceContext deviceContext,
            final RpcContext rpcContext,
            final RoleContext roleContext,
            final StatisticsContext statContext) {
        this.deviceContext = deviceContext;
        this.rpcContext = rpcContext;
        this.roleContext = roleContext;
        this.statContext = statContext;
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting device context cluster services for node {}", this.deviceContext.getServiceIdentifier());
        try {
            this.deviceContext.startupClusterServices();
        } catch (ExecutionException | InterruptedException e) {
            LOG.warn("Cluster service {} was unable to start.", this.getIdentifier());
        }
        LOG.info("Starting statistics context cluster services for node {}", this.deviceContext.getServiceIdentifier());
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        return deviceContext.stopClusterServices();
    }

    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceContext.getServiceIdentifier();
    }
}
