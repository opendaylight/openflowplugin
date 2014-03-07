/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test

import org.opendaylight.controller.sal.binding.api.AbstractBindingAwareProvider
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext
import org.opendaylight.controller.sal.binding.api.NotificationProviderService
import org.opendaylight.controller.sal.binding.api.data.DataProviderService
import org.osgi.framework.BundleContext

class OpenflowpluginTestActivator extends AbstractBindingAwareProvider {

    static var OpenflowpluginTestServiceProvider provider = new OpenflowpluginTestServiceProvider();
    static var OpenflowpluginGroupTestServiceProvider groupProvider = new OpenflowpluginGroupTestServiceProvider();
    static var OpenflowpluginMeterTestServiceProvider meterProvider = new OpenflowpluginMeterTestServiceProvider();
    static var OpenflowpluginTableFeaturesTestServiceProvider tableProvider = new OpenflowpluginTableFeaturesTestServiceProvider();
    var OpenflowpluginTestCommandProvider cmdProvider;
    var OpenflowpluginGroupTestCommandProvider cmdGroupProvider;
    var OpenflowpluginMeterTestCommandProvider cmdMeterProvider;
    var OpenflowpluginTableFeaturesTestCommandProvider cmdTableProvider;
    var OpenflowpluginStatsTestCommandProvider cmdStatsProvider;
    var OpenflowpluginTestNodeConnectorNotification cmdNodeConnectorNotification;
    var OpenflowpluginTestTopologyNotification cmdTopologyNotification;
    var OpenflowPluginBulkTransactionProvider bulkCmdProvider;
    var OpenflowPluginBulkGroupTransactionProvider groupCmdProvider;
    public static final String NODE_ID =  "foo:node:1";

    override onSessionInitiated(ProviderContext session) {
        provider.dataService = session.getSALService(DataProviderService)
        provider.notificationService = session.getSALService(NotificationProviderService)
        provider.start();
        provider.register(session);
        groupProvider.register(session);
        meterProvider.register(session);
        tableProvider.register(session);
        cmdProvider.onSessionInitiated(session);
        cmdGroupProvider.onSessionInitiated(session);
        cmdMeterProvider.onSessionInitiated(session);
        cmdTableProvider.onSessionInitiated(session);
        cmdStatsProvider.onSessionInitiated(session);      
        cmdNodeConnectorNotification.onSessionInitiated(session);
        cmdTopologyNotification.onSessionInitiated(session);
        bulkCmdProvider.onSessionInitiated(session);
        groupCmdProvider.onSessionInitiated(session);
    }
    
    override startImpl(BundleContext ctx) {
        super.startImpl(ctx);
        cmdProvider = new OpenflowpluginTestCommandProvider(ctx);
        cmdGroupProvider = new OpenflowpluginGroupTestCommandProvider(ctx);
        cmdMeterProvider = new OpenflowpluginMeterTestCommandProvider(ctx);
        cmdTableProvider = new OpenflowpluginTableFeaturesTestCommandProvider(ctx);
        cmdStatsProvider = new OpenflowpluginStatsTestCommandProvider(ctx);
        cmdNodeConnectorNotification = new OpenflowpluginTestNodeConnectorNotification(ctx);
    	cmdTopologyNotification = new OpenflowpluginTestTopologyNotification(ctx);
    	bulkCmdProvider = new OpenflowPluginBulkTransactionProvider(ctx);
    	groupCmdProvider = new OpenflowPluginBulkGroupTransactionProvider(ctx);
    }

    override protected stopImpl(BundleContext context) {
        provider.close();
    }

}
