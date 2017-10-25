/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.test;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestActivator extends AbstractBindingAwareProvider {
    private static final Logger LOG = LoggerFactory
            .getLogger(OpenflowpluginTestActivator.class);
    
    private static OpenflowpluginTestServiceProvider provider = new OpenflowpluginTestServiceProvider();
    private static OpenflowpluginGroupTestServiceProvider groupProvider = new OpenflowpluginGroupTestServiceProvider();
    private static OpenflowpluginMeterTestServiceProvider meterProvider = new OpenflowpluginMeterTestServiceProvider();
    private static OpenflowpluginTableFeaturesTestServiceProvider tableProvider = new OpenflowpluginTableFeaturesTestServiceProvider();

    private OpenflowpluginTestCommandProvider cmdProvider;

    private OpenflowpluginGroupTestCommandProvider cmdGroupProvider;

    private OpenflowpluginMeterTestCommandProvider cmdMeterProvider;

    private OpenflowpluginTableFeaturesTestCommandProvider cmdTableProvider;

    private OpenflowpluginStatsTestCommandProvider cmdStatsProvider;

    private OpenflowpluginTestNodeConnectorNotification cmdNodeConnectorNotification;

    private OpenflowpluginTestTopologyNotification cmdTopologyNotification;

    private OpenflowPluginBulkTransactionProvider bulkCmdProvider;

    private OpenflowPluginBulkGroupTransactionProvider groupCmdProvider;

    public static final String NODE_ID = "foo:node:1";

    /*
     * (non-Javadoc)
     * 
     * @see org.opendaylight.controller.sal.binding.api.BindingAwareProvider#
     * onSessionInitiated
     * (org.opendaylight.controller.sal.binding.api.BindingAwareBroker
     * .ProviderContext)
     */
    @Override
    public void onSessionInitiated(ProviderContext session) {
        DataBroker salService = session
                .<DataBroker> getSALService(DataBroker.class);
        OpenflowpluginTestActivator.provider.setDataService(salService);

        NotificationProviderService salService1 = session
                .<NotificationProviderService> getSALService(NotificationProviderService.class);

        OpenflowpluginTestActivator.provider
                .setNotificationService(salService1);
        OpenflowpluginTestActivator.provider.start();
        OpenflowpluginTestActivator.provider.register(session);

        OpenflowpluginTestActivator.groupProvider.register(session);
        OpenflowpluginTestActivator.meterProvider.register(session);
        OpenflowpluginTestActivator.tableProvider.register(session);

        this.cmdProvider.onSessionInitiated(session);
        this.cmdGroupProvider.onSessionInitiated(session);
        this.cmdMeterProvider.onSessionInitiated(session);
        this.cmdTableProvider.onSessionInitiated(session);
        this.cmdStatsProvider.onSessionInitiated(session);
        this.cmdNodeConnectorNotification.onSessionInitiated(session);
        this.cmdTopologyNotification.onSessionInitiated(session);
        this.bulkCmdProvider.onSessionInitiated(session);
        this.groupCmdProvider.onSessionInitiated(session);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator
     * #startImpl(org.osgi.framework.BundleContext)
     */
    public void startImpl(final BundleContext ctx) {
        super.startImpl(ctx);

        OpenflowpluginTestCommandProvider openflowpluginTestCommandProvider = new OpenflowpluginTestCommandProvider(
                ctx);
        this.cmdProvider = openflowpluginTestCommandProvider;
        OpenflowpluginGroupTestCommandProvider openflowpluginGroupTestCommandProvider = new OpenflowpluginGroupTestCommandProvider(
                ctx);
        this.cmdGroupProvider = openflowpluginGroupTestCommandProvider;
        OpenflowpluginMeterTestCommandProvider openflowpluginMeterTestCommandProvider = new OpenflowpluginMeterTestCommandProvider(
                ctx);
        this.cmdMeterProvider = openflowpluginMeterTestCommandProvider;
        OpenflowpluginTableFeaturesTestCommandProvider openflowpluginTableFeaturesTestCommandProvider = new OpenflowpluginTableFeaturesTestCommandProvider(
                ctx);
        this.cmdTableProvider = openflowpluginTableFeaturesTestCommandProvider;
        OpenflowpluginStatsTestCommandProvider openflowpluginStatsTestCommandProvider = new OpenflowpluginStatsTestCommandProvider(
                ctx);
        this.cmdStatsProvider = openflowpluginStatsTestCommandProvider;
        OpenflowpluginTestNodeConnectorNotification openflowpluginTestNodeConnectorNotification = new OpenflowpluginTestNodeConnectorNotification(
                ctx);
        this.cmdNodeConnectorNotification = openflowpluginTestNodeConnectorNotification;
        OpenflowpluginTestTopologyNotification openflowpluginTestTopologyNotification = new OpenflowpluginTestTopologyNotification(
                ctx);
        this.cmdTopologyNotification = openflowpluginTestTopologyNotification;
        OpenflowPluginBulkTransactionProvider openflowPluginBulkTransactionProvider = new OpenflowPluginBulkTransactionProvider(
                ctx);
        this.bulkCmdProvider = openflowPluginBulkTransactionProvider;
        OpenflowPluginBulkGroupTransactionProvider openflowPluginBulkGroupTransactionProvider = new OpenflowPluginBulkGroupTransactionProvider(
                ctx);
        this.groupCmdProvider = openflowPluginBulkGroupTransactionProvider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.opendaylight.controller.sal.binding.api.AbstractBrokerAwareActivator
     * #stopImpl(org.osgi.framework.BundleContext)
     */
    protected void stopImpl(final BundleContext context) {

        try {
            OpenflowpluginTestActivator.provider.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            LOG.error("Stopping bundle OpenflowpluginTestActivator failed.", e);
        }
    }

}
