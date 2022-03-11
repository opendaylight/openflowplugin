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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.osgi.framework.BundleContext;

@Singleton
@Component(service = { })
public class OpenflowpluginTestActivator implements AutoCloseable {
    private RpcProviderService rpcRegistry;
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
            @Reference NotificationService notificationService,
            @Reference NotificationPublishService notificationPublishService, BundleContext ctx) {
        provider = new OpenflowpluginTestServiceProvider(dataBroker, notificationPublishService);
        OpenflowpluginTestCommandProvider openflowpluginTestCommandProvider = new OpenflowpluginTestCommandProvider(
                dataBroker, notificationService, ctx);
        this.cmdProvider = openflowpluginTestCommandProvider;
        OpenflowpluginGroupTestCommandProvider openflowpluginGroupTestCommandProvider =
                new OpenflowpluginGroupTestCommandProvider(dataBroker, ctx);
        this.cmdGroupProvider = openflowpluginGroupTestCommandProvider;
        OpenflowpluginMeterTestCommandProvider openflowpluginMeterTestCommandProvider =
                new OpenflowpluginMeterTestCommandProvider(dataBroker, ctx);
        this.cmdMeterProvider = openflowpluginMeterTestCommandProvider;
        OpenflowpluginTableFeaturesTestCommandProvider openflowpluginTableFeaturesTestCommandProvider =
                new OpenflowpluginTableFeaturesTestCommandProvider(dataBroker, ctx);
        this.cmdTableProvider = openflowpluginTableFeaturesTestCommandProvider;
        OpenflowpluginStatsTestCommandProvider openflowpluginStatsTestCommandProvider =
                new OpenflowpluginStatsTestCommandProvider(dataBroker, ctx);
        this.cmdStatsProvider = openflowpluginStatsTestCommandProvider;
        OpenflowpluginTestNodeConnectorNotification openflowpluginTestNodeConnectorNotification =
                new OpenflowpluginTestNodeConnectorNotification(notificationService);
        this.cmdNodeConnectorNotification = openflowpluginTestNodeConnectorNotification;
        OpenflowpluginTestTopologyNotification openflowpluginTestTopologyNotification =
                new OpenflowpluginTestTopologyNotification(notificationService);
        this.cmdTopologyNotification = openflowpluginTestTopologyNotification;
        OpenflowPluginBulkTransactionProvider openflowPluginBulkTransactionProvider =
                new OpenflowPluginBulkTransactionProvider(dataBroker, ctx);
        this.bulkCmdProvider = openflowPluginBulkTransactionProvider;
        OpenflowPluginBulkGroupTransactionProvider openflowPluginBulkGroupTransactionProvider =
                new OpenflowPluginBulkGroupTransactionProvider(dataBroker, ctx);
        this.groupCmdProvider = openflowPluginBulkGroupTransactionProvider;

        provider.register(rpcRegistry);

        groupProvider.register(rpcRegistry);
        meterProvider.register(rpcRegistry);
        tableProvider.register(rpcRegistry);

        this.cmdProvider.init();
        this.cmdGroupProvider.init();
        this.cmdMeterProvider.init();
        this.cmdTableProvider.init();
        this.cmdStatsProvider.init();
        this.cmdNodeConnectorNotification.init();
        this.cmdTopologyNotification.init();
        this.bulkCmdProvider.init();
        this.groupCmdProvider.init();
    }

    @Override
    @PreDestroy
    @Deactivate
    public void close() {
        provider.close();
    }
}
