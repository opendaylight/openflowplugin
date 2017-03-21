/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProviderFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
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
    public OpenFlowPluginProvider newInstance(OpenflowProviderConfig providerConfig,
                                              DataBroker dataBroker,
                                              RpcProviderRegistry rpcRegistry,
                                              NotificationService notificationService,
                                              NotificationPublishService notificationPublishService,
                                              EntityOwnershipService entityOwnershipService,
                                              List<SwitchConnectionProvider> switchConnectionProviders,
                                              ClusterSingletonServiceProvider singletonServiceProvider) {

        LOG.info("Initializing new OFP southbound.");

        final OpenFlowPluginProviderImpl openflowPluginProvider = new OpenFlowPluginProviderImpl(
            switchConnectionProviders,
            dataBroker,
            rpcRegistry,
            notificationService,
            notificationPublishService,
            singletonServiceProvider);

        openflowPluginProvider.updateRpcRequestsQuota(providerConfig.getRpcRequestsQuota().getValue());
        openflowPluginProvider.updateGlobalNotificationQuota(providerConfig.getGlobalNotificationQuota());
        openflowPluginProvider.updateSwitchFeaturesMandatory(providerConfig.isSwitchFeaturesMandatory());
        openflowPluginProvider.updateEnableFlowRemovedNotification(providerConfig.isEnableFlowRemovedNotification());
        openflowPluginProvider.updateIsStatisticsRpcEnabled(providerConfig.isIsStatisticsRpcEnabled());
        openflowPluginProvider.updateBarrierCountLimit(providerConfig.getBarrierCountLimit().getValue());
        openflowPluginProvider.updateBarrierIntervalTimeoutLimit(providerConfig.getBarrierIntervalTimeoutLimit().getValue());
        openflowPluginProvider.updateEchoReplyTimeout(providerConfig.getEchoReplyTimeout().getValue());
        openflowPluginProvider.updateIsStatisticsPollingOn(providerConfig.isIsStatisticsPollingOn());
        openflowPluginProvider.updateSkipTableFeatures(providerConfig.isSkipTableFeatures());
        openflowPluginProvider.updateBasicTimerDelay(providerConfig.getBasicTimerDelay().getValue());
        openflowPluginProvider.updateMaximumTimerDelay(providerConfig.getMaximumTimerDelay().getValue());
        openflowPluginProvider.updateUseSingleLayerSerialization(providerConfig.isUseSingleLayerSerialization());
        openflowPluginProvider.updateThreadPoolMinThreads(providerConfig.getThreadPoolMinThreads());
        openflowPluginProvider.updateThreadPoolMaxThreads(providerConfig.getThreadPoolMaxThreads().getValue());
        openflowPluginProvider.updateThreadPoolTimeout(providerConfig.getThreadPoolTimeout());
        openflowPluginProvider.initialize();

        return openflowPluginProvider;
    }
}
