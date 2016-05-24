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
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
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
    public OpenFlowPluginProvider newInstance(OpenflowProviderConfig providerConfig, DataBroker dataBroker,
            RpcProviderRegistry rpcRegistry, NotificationService notificationService,
            NotificationPublishService notificationPublishService,
            EntityOwnershipService entityOwnershipService,
            List<SwitchConnectionProvider> switchConnectionProviders) {

        LOG.info("Initializing new OFP southbound.");

        OpenflowPortsUtil.init();
        OpenFlowPluginProvider openflowPluginProvider = new OpenFlowPluginProviderImpl(
                providerConfig.getRpcRequestsQuota(),
                providerConfig.getGlobalNotificationQuota(),
                providerConfig.getThreadPoolMinThreads(),
                providerConfig.getThreadPoolMaxThreads().getValue(),
                providerConfig.getThreadPoolTimeout());

        openflowPluginProvider.setSwitchConnectionProviders(switchConnectionProviders);
        openflowPluginProvider.setDataBroker(dataBroker);
        openflowPluginProvider.setRpcProviderRegistry(rpcRegistry);
        openflowPluginProvider.setNotificationProviderService(notificationService);
        openflowPluginProvider.setNotificationPublishService(notificationPublishService);
        openflowPluginProvider.setEntityOwnershipService(entityOwnershipService);
        openflowPluginProvider.setSwitchFeaturesMandatory(providerConfig.isSwitchFeaturesMandatory());
        openflowPluginProvider.setIsStatisticsPollingOff(providerConfig.isIsStatisticsPollingOff());
        openflowPluginProvider.setIsStatisticsRpcEnabled(providerConfig.isIsStatisticsRpcEnabled());
        openflowPluginProvider.setBarrierCountLimit(providerConfig.getBarrierCountLimit().getValue());
        openflowPluginProvider.setBarrierInterval(providerConfig.getBarrierIntervalTimeoutLimit().getValue());
        openflowPluginProvider.setEchoReplyTimeout(providerConfig.getEchoReplyTimeout().getValue());

        openflowPluginProvider.initialize();

        LOG.info("Configured values, " +
                "StatisticsPollingOff:{}, " +
                "SwitchFeaturesMandatory:{}, " +
                "BarrierCountLimit:{}, " +
                "BarrierTimeoutLimit:{}, " +
                "EchoReplyTimeout:{}, " +
                "ThreadPoolMinThreads:{}, " +
                "ThreadPoolMaxThreads:{}, " +
                "ThreadPoolTimeout:{}",
                providerConfig.isIsStatisticsPollingOff(),
                providerConfig.isSwitchFeaturesMandatory(),
                providerConfig.getBarrierCountLimit().getValue(),
                providerConfig.getBarrierIntervalTimeoutLimit().getValue(),
                providerConfig.getEchoReplyTimeout().getValue(),
                providerConfig.getThreadPoolMinThreads(),
                providerConfig.getThreadPoolMaxThreads().getValue(),
                providerConfig.getThreadPoolTimeout());

        return openflowPluginProvider;
    }
}
