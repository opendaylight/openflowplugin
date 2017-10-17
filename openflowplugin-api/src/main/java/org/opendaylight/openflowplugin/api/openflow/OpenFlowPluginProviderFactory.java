/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow;

import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.infrautils.diagstatus.DiagStatusService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;

/**
 * Factory for creating OpenFlowPluginProvider instances.
 */
public interface OpenFlowPluginProviderFactory {
    OpenFlowPluginProvider newInstance(ConfigurationService configurationService,
                                       DataBroker dataBroker,
                                       RpcProviderRegistry rpcRegistry,
                                       NotificationPublishService notificationPublishService,
                                       EntityOwnershipService entityOwnershipService,
                                       List<SwitchConnectionProvider> switchConnectionProviders,
                                       ClusterSingletonServiceProvider singletonServiceProvider,
                                       MastershipChangeServiceManager mastershipChangeServiceManager,
                                       DiagStatusService diagStatusService);
}
