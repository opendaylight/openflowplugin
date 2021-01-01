/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationServiceFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServiceFactoryImpl implements ConfigurationServiceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceFactoryImpl.class);

    @Override
    public ConfigurationService newInstance(
            final OpenflowProviderConfig providerConfig) {
        return new ConfigurationServiceImpl(providerConfig);
    }

    private static final class ConfigurationServiceImpl implements ConfigurationService {
        private final Map<String, String> propertyMap = new HashMap<>();
        private final List<ConfigurationListener> listeners = new ArrayList<>();

        ConfigurationServiceImpl(final OpenflowProviderConfig providerConfig) {
            LOG.info("Loading properties from '{}' YANG file", OpenflowProviderConfig.QNAME);
            update(ImmutableMap
                    .<String, String>builder()
                    .put(ConfigurationProperty.RPC_REQUESTS_QUOTA.toString(),
                            providerConfig.getRpcRequestsQuota().getValue().toString())
                    .put(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString(),
                            providerConfig.getGlobalNotificationQuota().toString())
                    .put(ConfigurationProperty.SWITCH_FEATURES_MANDATORY.toString(),
                            providerConfig.getSwitchFeaturesMandatory().toString())
                    .put(ConfigurationProperty.ENABLE_FLOW_REMOVED_NOTIFICATION.toString(),
                            providerConfig.getEnableFlowRemovedNotification().toString())
                    .put(ConfigurationProperty.IS_STATISTICS_RPC_ENABLED.toString(),
                            providerConfig.getIsStatisticsRpcEnabled().toString())
                    .put(ConfigurationProperty.BARRIER_COUNT_LIMIT.toString(),
                            providerConfig.getBarrierCountLimit().getValue().toString())
                    .put(ConfigurationProperty.BARRIER_INTERVAL_TIMEOUT_LIMIT.toString(),
                            providerConfig.getBarrierIntervalTimeoutLimit().getValue().toString())
                    .put(ConfigurationProperty.ECHO_REPLY_TIMEOUT.toString(),
                            providerConfig.getEchoReplyTimeout().getValue().toString())
                    .put(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.IS_TABLE_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsTableStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.IS_FLOW_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsFlowStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.IS_GROUP_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsGroupStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.IS_METER_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsMeterStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.IS_PORT_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsPortStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.IS_QUEUE_STATISTICS_POLLING_ON.toString(),
                            providerConfig.getIsQueueStatisticsPollingOn().toString())
                    .put(ConfigurationProperty.SKIP_TABLE_FEATURES.toString(),
                            providerConfig.getSkipTableFeatures().toString())
                    .put(ConfigurationProperty.BASIC_TIMER_DELAY.toString(),
                            providerConfig.getBasicTimerDelay().getValue().toString())
                    .put(ConfigurationProperty.MAXIMUM_TIMER_DELAY.toString(),
                            providerConfig.getMaximumTimerDelay().getValue().toString())
                    .put(ConfigurationProperty.USE_SINGLE_LAYER_SERIALIZATION.toString(),
                            providerConfig.getUseSingleLayerSerialization().toString())
                    .put(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(),
                            providerConfig.getThreadPoolMinThreads().toString())
                    .put(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(),
                            providerConfig.getThreadPoolMaxThreads().getValue().toString())
                    .put(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString(),
                            providerConfig.getThreadPoolTimeout().toString())
                    .put(ConfigurationProperty.DEVICE_CONNECTION_RATE_LIMIT_PER_MIN.toString(),
                            providerConfig.getDeviceConnectionRateLimitPerMin().toString())
                    .put(ConfigurationProperty.DEVICE_CONNECTION_HOLD_TIME_IN_SECONDS.toString(),
                            providerConfig.getDeviceConnectionHoldTimeInSeconds().toString())
                    .put(ConfigurationProperty.DEVICE_DATASTORE_REMOVAL_DELAY.toString(),
                            providerConfig.getDeviceDatastoreRemovalDelay().getValue().toString())
                    .build());
        }

        @Override
        public void update(@NonNull final Map<String, String> properties) {
            properties.forEach((propertyName, newValue) -> {
                final String originalValue = propertyMap.get(propertyName);

                if (originalValue != null) {
                    if (originalValue.equals(newValue)) {
                        return;
                    }

                    LOG.info("{} configuration property was changed from '{}' to '{}'",
                            propertyName,
                            originalValue,
                            newValue);
                } else {
                    if (newValue == null) {
                        return;
                    }

                    LOG.info("{} configuration property was changed to '{}'", propertyName, newValue);
                }

                propertyMap.put(propertyName, newValue);
                listeners.forEach(listener -> listener.onPropertyChanged(propertyName, newValue));
            });
        }

        @NonNull
        @Override
        public <T> T getProperty(@NonNull final String key, @NonNull final Function<String, T> transformer) {
            return transformer.apply(propertyMap.get(key));
        }

        @NonNull
        @Override
        public AutoCloseable registerListener(@NonNull final ConfigurationListener listener) {
            Verify.verify(!listeners.contains(listener));
            LOG.info("{} was registered as configuration listener to OpenFlowPlugin configuration service", listener);
            listeners.add(listener);
            propertyMap.forEach(listener::onPropertyChanged);
            return () -> listeners.remove(listener);
        }

        @Override
        public void close() {
            propertyMap.clear();
            listeners.clear();
        }
    }
}
