/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import java.util.List;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Service;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.infrautils.ready.SystemReadyMonitor;
import org.opendaylight.mdsal.eos.binding.api.EntityOwnershipService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.diagstatus.OpenflowPluginDiagStatusProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProviderFactory;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of OpenFlowPluginProviderFactory.
 *
 * @author Thomas Pantelis
 */
@Singleton
@Service(classes = OpenFlowPluginProviderFactory.class)
public class OpenFlowPluginProviderFactoryImpl implements OpenFlowPluginProviderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowPluginProviderFactoryImpl.class);

    @Override
    public OpenFlowPluginProvider newInstance(final ConfigurationService configurationService,
                                              final DataBroker dataBroker,
                                              final RpcProviderRegistry rpcRegistry,
                                              final NotificationPublishService notificationPublishService,
                                              final EntityOwnershipService entityOwnershipService,
                                              final List<SwitchConnectionProvider> switchConnectionProviders,
                                              final ClusterSingletonServiceProvider singletonServiceProvider,
                                              final MastershipChangeServiceManager mastershipChangeServiceManager,
                                              final OpenflowPluginDiagStatusProvider ofPluginDiagstatusProvider,
                                              final SystemReadyMonitor systemReadyMonitor) {
        LOG.info("Initializing new OFP southbound.");
        final OpenFlowPluginProvider openflowPluginProvider = new OpenFlowPluginProviderImpl(
                configurationService,
                switchConnectionProviders,
                dataBroker,
                rpcRegistry,
                notificationPublishService,
                singletonServiceProvider,
                entityOwnershipService,
                mastershipChangeServiceManager,
                ofPluginDiagstatusProvider,
                systemReadyMonitor);

        openflowPluginProvider.initialize();
        return openflowPluginProvider;
    }
}
