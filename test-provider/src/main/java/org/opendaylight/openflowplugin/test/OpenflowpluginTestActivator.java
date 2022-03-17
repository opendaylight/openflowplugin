/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Singleton
@Component(service = { })
public class OpenflowpluginTestActivator implements AutoCloseable {
    private final OpenflowpluginTestServiceProvider provider;
    private final OpenflowpluginGroupTestServiceProvider groupProvider = new OpenflowpluginGroupTestServiceProvider();
    private final OpenflowpluginMeterTestServiceProvider meterProvider = new OpenflowpluginMeterTestServiceProvider();
    private final OpenflowpluginTableFeaturesTestServiceProvider tableProvider =
            new OpenflowpluginTableFeaturesTestServiceProvider();

    private final OpenflowpluginTestCommandProvider cmdProvider;

    private final OpenflowpluginGroupTestCommandProvider cmdGroupProvider;

    private final OpenflowpluginMeterTestCommandProvider cmdMeterProvider;

    private final OpenflowpluginTableFeaturesTestCommandProvider cmdTableProvider;

    private final OpenflowpluginStatsTestCommandProvider cmdStatsProvider;

    private final OpenflowpluginTestNodeConnectorNotification cmdNodeConnectorNotification;

    private final OpenflowpluginTestTopologyNotification cmdTopologyNotification;

    private final OpenflowPluginBulkTransactionProvider bulkCmdProvider;

    private final OpenflowPluginBulkGroupTransactionProvider groupCmdProvider;

    public static final String NODE_ID = "foo:node:1";

    @Activate
    @Inject
    public OpenflowpluginTestActivator(@Reference DataBroker dataBroker,
            @Reference RpcProviderService rpcRegistry,
            @Reference NotificationService notificationService,
            @Reference NotificationPublishService notificationPublishService, BundleContext ctx) {
        provider = new OpenflowpluginTestServiceProvider(dataBroker, notificationPublishService);
        cmdProvider = new OpenflowpluginTestCommandProvider(dataBroker, notificationService, ctx);
        cmdGroupProvider = new OpenflowpluginGroupTestCommandProvider(dataBroker, ctx);
        cmdMeterProvider = new OpenflowpluginMeterTestCommandProvider(dataBroker, notificationService, ctx);
        cmdTableProvider = new OpenflowpluginTableFeaturesTestCommandProvider(dataBroker, ctx);
        cmdStatsProvider = new OpenflowpluginStatsTestCommandProvider(dataBroker, ctx);
        cmdNodeConnectorNotification = new OpenflowpluginTestNodeConnectorNotification(notificationService);
        cmdTopologyNotification = new OpenflowpluginTestTopologyNotification(notificationService);
        bulkCmdProvider = new OpenflowPluginBulkTransactionProvider(dataBroker, notificationService, ctx);
        groupCmdProvider = new OpenflowPluginBulkGroupTransactionProvider(dataBroker, notificationService, ctx);

        provider.register(rpcRegistry);

        groupProvider.register(rpcRegistry);
        meterProvider.register(rpcRegistry);
        tableProvider.register(rpcRegistry);

        cmdProvider.init();
        cmdGroupProvider.init();
        cmdMeterProvider.init();
        cmdTableProvider.init();
        cmdStatsProvider.init();
        cmdNodeConnectorNotification.init();
        cmdTopologyNotification.init();
        bulkCmdProvider.init();
        groupCmdProvider.init();
    }

    @Override
    @PreDestroy
    @Deactivate
    public void close() {
        provider.close();
    }
}
