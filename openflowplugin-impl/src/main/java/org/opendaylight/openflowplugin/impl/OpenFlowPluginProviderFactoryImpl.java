/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginConfigurationService.PropertyType;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProvider;
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProviderFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
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
    public OpenFlowPluginProvider newInstance(final OpenflowProviderConfig providerConfig,
                                              final DataBroker dataBroker,
                                              final RpcProviderRegistry rpcRegistry,
                                              final NotificationPublishService notificationPublishService,
                                              final EntityOwnershipService entityOwnershipService,
                                              final List<SwitchConnectionProvider> switchConnectionProviders,
                                              final ClusterSingletonServiceProvider singletonServiceProvider,
                                              final BundleContext bundleContext) {

        LOG.info("Initializing new OFP southbound.");

        final OpenFlowPluginProviderImpl openflowPluginProvider = new OpenFlowPluginProviderImpl(
                switchConnectionProviders,
                dataBroker,
                rpcRegistry,
                notificationPublishService,
                singletonServiceProvider,
                entityOwnershipService);

        LOG.info("Loading configuration from YANG file");
        openflowPluginProvider.updateProperty(PropertyType.RPC_REQUESTS_QUOTA, providerConfig.getRpcRequestsQuota().getValue());
        openflowPluginProvider.updateProperty(PropertyType.GLOBAL_NOTIFICATION_QUOTA, providerConfig.getGlobalNotificationQuota());
        openflowPluginProvider.updateProperty(PropertyType.SWITCH_FEATURES_MANDATORY, providerConfig.isSwitchFeaturesMandatory());
        openflowPluginProvider.updateProperty(PropertyType.ENABLE_FLOW_REMOVED_NOTIFICATION, providerConfig.isEnableFlowRemovedNotification());
        openflowPluginProvider.updateProperty(PropertyType.IS_STATISTICS_RPC_ENABLED, providerConfig.isIsStatisticsRpcEnabled());
        openflowPluginProvider.updateProperty(PropertyType.BARRIER_COUNT_LIMIT, providerConfig.getBarrierCountLimit().getValue());
        openflowPluginProvider.updateProperty(PropertyType.BARRIER_INTERVAL_TIMEOUT_LIMIT, providerConfig.getBarrierIntervalTimeoutLimit().getValue());
        openflowPluginProvider.updateProperty(PropertyType.ECHO_REPLY_TIMEOUT, providerConfig.getEchoReplyTimeout().getValue());
        openflowPluginProvider.updateProperty(PropertyType.IS_STATISTICS_POLLING_ON, providerConfig.isIsStatisticsPollingOn());
        openflowPluginProvider.updateProperty(PropertyType.SKIP_TABLE_FEATURES, providerConfig.isSkipTableFeatures());
        openflowPluginProvider.updateProperty(PropertyType.BASIC_TIMER_DELAY, providerConfig.getBasicTimerDelay().getValue());
        openflowPluginProvider.updateProperty(PropertyType.MAXIMUM_TIMER_DELAY, providerConfig.getMaximumTimerDelay().getValue());
        openflowPluginProvider.updateProperty(PropertyType.USE_SINGLE_LAYER_SERIALIZATION, providerConfig.isUseSingleLayerSerialization());
        openflowPluginProvider.updateProperty(PropertyType.THREAD_POOL_MIN_THREADS, providerConfig.getThreadPoolMinThreads());
        openflowPluginProvider.updateProperty(PropertyType.THREAD_POOL_MAX_THREADS, providerConfig.getThreadPoolMaxThreads().getValue());
        openflowPluginProvider.updateProperty(PropertyType.THREAD_POOL_TIMEOUT, providerConfig.getThreadPoolTimeout());

        LOG.info("Loading configuration from properties file");
        Optional.ofNullable(bundleContext.getServiceReference(ConfigurationAdmin.class.getName())).ifPresent(serviceReference -> {
            final ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(serviceReference);

            try {
                final Configuration configuration = configurationAdmin.getConfiguration(OFConstants.CONFIG_FILE_ID);

                Optional.ofNullable(configuration.getProperties()).ifPresent(properties -> {
                    final Enumeration<String> keys = properties.keys();
                    final Map<String, Object> mapProperties = new HashMap<>(properties.size());

                    while (keys.hasMoreElements()) {
                        final String key = keys.nextElement();
                        final Object value = properties.get(key);
                        mapProperties.put(key, value);
                    }

                    openflowPluginProvider.update(mapProperties);
                });
            } catch (IOException e) {
                LOG.debug("Failed to load " + OFConstants.CONFIG_FILE_ID + " configuration file", e);
            }
        });

        openflowPluginProvider.initialize();
        return openflowPluginProvider;
    }
}
