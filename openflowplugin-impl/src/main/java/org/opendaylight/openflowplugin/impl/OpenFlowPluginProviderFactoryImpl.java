/*
 * Copyright (c) 2016, 2017 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import com.google.common.collect.ImmutableMap;
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
import org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginProperty;
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
        openflowPluginProvider.update(ImmutableMap
                .<String, String>builder()
                .put(OpenFlowPluginProperty.RPC_REQUESTS_QUOTA.toString(),
                        providerConfig.getRpcRequestsQuota().getValue().toString())
                .put(OpenFlowPluginProperty.GLOBAL_NOTIFICATION_QUOTA.toString(),
                        providerConfig.getGlobalNotificationQuota().toString())
                .put(OpenFlowPluginProperty.SWITCH_FEATURES_MANDATORY.toString(),
                        providerConfig.isSwitchFeaturesMandatory().toString())
                .put(OpenFlowPluginProperty.ENABLE_FLOW_REMOVED_NOTIFICATION.toString(),
                        providerConfig.isEnableFlowRemovedNotification().toString())
                .put(OpenFlowPluginProperty.IS_STATISTICS_RPC_ENABLED.toString(),
                        providerConfig.isIsStatisticsRpcEnabled().toString())
                .put(OpenFlowPluginProperty.BARRIER_COUNT_LIMIT.toString(),
                        providerConfig.getBarrierCountLimit().getValue().toString())
                .put(OpenFlowPluginProperty.BARRIER_INTERVAL_TIMEOUT_LIMIT.toString(),
                        providerConfig.getBarrierIntervalTimeoutLimit().getValue().toString())
                .put(OpenFlowPluginProperty.ECHO_REPLY_TIMEOUT.toString(),
                        providerConfig.getEchoReplyTimeout().getValue().toString())
                .put(OpenFlowPluginProperty.IS_STATISTICS_POLLING_ON.toString(),
                        providerConfig.isIsStatisticsPollingOn().toString())
                .put(OpenFlowPluginProperty.SKIP_TABLE_FEATURES.toString(),
                        providerConfig.isSkipTableFeatures().toString())
                .put(OpenFlowPluginProperty.BASIC_TIMER_DELAY.toString(),
                        providerConfig.getBasicTimerDelay().getValue().toString())
                .put(OpenFlowPluginProperty.MAXIMUM_TIMER_DELAY.toString(),
                        providerConfig.getMaximumTimerDelay().getValue().toString())
                .put(OpenFlowPluginProperty.USE_SINGLE_LAYER_SERIALIZATION.toString(),
                        providerConfig.isUseSingleLayerSerialization().toString())
                .put(OpenFlowPluginProperty.THREAD_POOL_MIN_THREADS.toString(),
                        providerConfig.getThreadPoolMinThreads().toString())
                .put(OpenFlowPluginProperty.THREAD_POOL_MAX_THREADS.toString(),
                        providerConfig.getThreadPoolMaxThreads().getValue().toString())
                .put(OpenFlowPluginProperty.THREAD_POOL_TIMEOUT.toString(),
                        providerConfig.getThreadPoolTimeout().toString())
                .build());

        LOG.info("Loading configuration from properties file");
        Optional.ofNullable(bundleContext.getServiceReference(ConfigurationAdmin.class.getName())).ifPresent(serviceReference -> {
            final ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bundleContext.getService(serviceReference);

            try {
                final Configuration configuration = configurationAdmin.getConfiguration(OFConstants.CONFIG_FILE_ID);

                Optional.ofNullable(configuration.getProperties()).ifPresent(properties -> {
                    final Enumeration<String> keys = properties.keys();
                    final Map<String, String> mapProperties = new HashMap<>(properties.size());

                    while (keys.hasMoreElements()) {
                        final String key = keys.nextElement();
                        final String value = properties.get(key).toString();
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
