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
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.framework.BundleContext;

/**
 * Factory for creating OpenFlowPluginProvider instances.
 */
public interface OpenFlowPluginProviderFactory {
    OpenFlowPluginProvider newInstance(OpenflowProviderConfig providerConfig,
                                       DataBroker dataBroker,
                                       RpcProviderRegistry rpcRegistry,
                                       NotificationPublishService notificationPublishService,
                                       EntityOwnershipService entityOwnershipService,
                                       List<SwitchConnectionProvider> switchConnectionProviders,
                                       ClusterSingletonServiceProvider singletonServiceProvider,
                                       BundleContext bundleContext);
}
