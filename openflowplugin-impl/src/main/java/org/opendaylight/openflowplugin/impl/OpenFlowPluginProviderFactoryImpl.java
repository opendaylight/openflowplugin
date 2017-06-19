/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProviderFactory;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of OpenFlowPluginProviderFactory.
 *
 * @author Thomas Pantelis
 */
public class OpenFlowPluginProviderFactoryImpl implements OpenFlowPluginProviderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderFactoryImpl.class);

    @Override
    public OpenFlowPluginProvider newInstance(final ConfigurationService configurationService,
                                              final DataBroker dataBroker,
                                              final RpcProviderRegistry rpcRegistry,
                                              final NotificationPublishService notificationPublishService,
                                              final EntityOwnershipService entityOwnershipService,
                                              final List<SwitchConnectionProvider> switchConnectionProviders,
                                              final ClusterSingletonServiceProvider singletonServiceProvider) {
        LOG.info("Initializing new OFP southbound.");
        final OpenFlowPluginProvider openflowPluginProvider = new OpenFlowPluginProviderImpl(
                configurationService,
                switchConnectionProviders,
                dataBroker,
                rpcRegistry,
                notificationPublishService,
                singletonServiceProvider,
                entityOwnershipService);

        openflowPluginProvider.initialize();
        return openflowPluginProvider;
    }
}
