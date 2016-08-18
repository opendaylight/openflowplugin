/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.ClusterLifecycleSupervisor;
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

/**
 * Service for starting or stopping all services in plugin in cluster
 */
public interface LifecycleService extends ClusterSingletonService, AutoCloseable, ClusterLifecycleSupervisor, ClusterInitializationPhaseHandler {

    /**
     * This method registers lifecycle service to the given provider
     * @param singletonServiceProvider from md-sal binding
     */
    void registerService(final ClusterSingletonServiceProvider singletonServiceProvider);

    /**
     * Setter for device context
     * @param deviceContext actual device context created per device
     */
    void setDeviceContext(final DeviceContext deviceContext);

    /**
     * Setter for rpc context
     * @param rpcContext actual rpc context created per device
     */
    void setRpcContext(final RpcContext rpcContext);

    /**
     * Setter for role context
     * @param roleContext actual role context created per device
     */
    void setRoleContext(final RoleContext roleContext);

    /**
     * Setter for statistics context
     * @param statContext actual statistics context created per device
     */
    void setStatContext(final StatisticsContext statContext);

    /**
     * Some services, contexts etc. still need to have access to device context,
     * instead to push into them, here is the getter
     * @return device context for this device
     */
    DeviceContext getDeviceContext();

    /**
     * if some services not started properly need to close connection
     */
    void closeConnection();
}
