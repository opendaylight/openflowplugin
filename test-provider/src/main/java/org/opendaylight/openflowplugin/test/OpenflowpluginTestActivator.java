/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestActivator implements AutoCloseable {
    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginTestActivator.class);

    private DataBroker dataBroker;
    private NotificationProviderService notificationService;
    private RpcProviderRegistry rpcRegistry;
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

    public OpenflowpluginTestActivator(DataBroker dataBroker, NotificationProviderService notificationService,
            BundleContext ctx) {
        provider = new OpenflowpluginTestServiceProvider(dataBroker, notificationService);
        OpenflowpluginTestCommandProvider openflowpluginTestCommandProvider = new OpenflowpluginTestCommandProvider(
                dataBroker, notificationService, ctx);
        this.cmdProvider = openflowpluginTestCommandProvider;
        OpenflowpluginGroupTestCommandProvider openflowpluginGroupTestCommandProvider =
                new OpenflowpluginGroupTestCommandProvider(dataBroker, ctx);
        this.cmdGroupProvider = openflowpluginGroupTestCommandProvider;
        OpenflowpluginMeterTestCommandProvider openflowpluginMeterTestCommandProvider =
                new OpenflowpluginMeterTestCommandProvider(dataBroker, notificationService, ctx);
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
                new OpenflowPluginBulkTransactionProvider(dataBroker, notificationService, ctx);
        this.bulkCmdProvider = openflowPluginBulkTransactionProvider;
        OpenflowPluginBulkGroupTransactionProvider openflowPluginBulkGroupTransactionProvider =
                new OpenflowPluginBulkGroupTransactionProvider(dataBroker, notificationService, ctx);
        this.groupCmdProvider = openflowPluginBulkGroupTransactionProvider;
    }

    public void init() {
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
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void close() {
        try {
            provider.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.error("Stopping bundle OpenflowpluginTestActivator failed.", e);
        }
    }
}
