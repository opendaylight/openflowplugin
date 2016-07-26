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
import org.opendaylight.openflowplugin.api.openflow.role.RoleContext;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;

/**
 * Service for starting or stopping all services in plugin in cluster
 */
public interface LifecycleService extends ClusterSingletonService, AutoCloseable {

    void registerService(ClusterSingletonServiceProvider singletonServiceProvider);

    void setDeviceContext(DeviceContext deviceContext);

    void setRpcContext(RpcContext rpcContext);

    void setRoleContext(RoleContext roleContext);

    void setStatContext(StatisticsContext statContext);

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
