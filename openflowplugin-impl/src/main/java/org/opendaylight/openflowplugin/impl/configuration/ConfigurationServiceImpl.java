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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationListener;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationProperty;
import org.opendaylight.openflowplugin.api.openflow.configuration.ConfigurationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServiceImpl implements ConfigurationService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationServiceImpl.class);
    private final Map<String, String> propertyMap = new HashMap<>();
    private final List<ConfigurationListener> listeners = new ArrayList<>();

    public ConfigurationServiceImpl(final OpenflowProviderConfig providerConfig, final BundleContext bundleContext) {
        LOG.info("Loading configuration from OpenflowProviderConfig");
        update(ImmutableMap
                .<String, String>builder()
                .put(ConfigurationProperty.RPC_REQUESTS_QUOTA.toString(),
                        providerConfig.getRpcRequestsQuota().getValue().toString())
                .put(ConfigurationProperty.GLOBAL_NOTIFICATION_QUOTA.toString(),
                        providerConfig.getGlobalNotificationQuota().toString())
                .put(ConfigurationProperty.SWITCH_FEATURES_MANDATORY.toString(),
                        providerConfig.isSwitchFeaturesMandatory().toString())
                .put(ConfigurationProperty.ENABLE_FLOW_REMOVED_NOTIFICATION.toString(),
                        providerConfig.isEnableFlowRemovedNotification().toString())
                .put(ConfigurationProperty.IS_STATISTICS_RPC_ENABLED.toString(),
                        providerConfig.isIsStatisticsRpcEnabled().toString())
                .put(ConfigurationProperty.BARRIER_COUNT_LIMIT.toString(),
                        providerConfig.getBarrierCountLimit().getValue().toString())
                .put(ConfigurationProperty.BARRIER_INTERVAL_TIMEOUT_LIMIT.toString(),
                        providerConfig.getBarrierIntervalTimeoutLimit().getValue().toString())
                .put(ConfigurationProperty.ECHO_REPLY_TIMEOUT.toString(),
                        providerConfig.getEchoReplyTimeout().getValue().toString())
                .put(ConfigurationProperty.IS_STATISTICS_POLLING_ON.toString(),
                        providerConfig.isIsStatisticsPollingOn().toString())
                .put(ConfigurationProperty.SKIP_TABLE_FEATURES.toString(),
                        providerConfig.isSkipTableFeatures().toString())
                .put(ConfigurationProperty.BASIC_TIMER_DELAY.toString(),
                        providerConfig.getBasicTimerDelay().getValue().toString())
                .put(ConfigurationProperty.MAXIMUM_TIMER_DELAY.toString(),
                        providerConfig.getMaximumTimerDelay().getValue().toString())
                .put(ConfigurationProperty.USE_SINGLE_LAYER_SERIALIZATION.toString(),
                        providerConfig.isUseSingleLayerSerialization().toString())
                .put(ConfigurationProperty.THREAD_POOL_MIN_THREADS.toString(),
                        providerConfig.getThreadPoolMinThreads().toString())
                .put(ConfigurationProperty.THREAD_POOL_MAX_THREADS.toString(),
                        providerConfig.getThreadPoolMaxThreads().getValue().toString())
                .put(ConfigurationProperty.THREAD_POOL_TIMEOUT.toString(),
                        providerConfig.getThreadPoolTimeout().toString())
                .build());

        LOG.info("Loading configuration from {}", OFConstants.CONFIG_FILE_ID);
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

                    update(mapProperties);
                });
            } catch (IOException e) {
                LOG.debug("Failed to load {} configuration file. Error {}", OFConstants.CONFIG_FILE_ID, e);
            }
        });
    }

    @Override
    public void update(@Nonnull final Map<String, String> properties) {
        properties.forEach((propertyName, newValue) -> {
            final String originalValue = propertyMap.get(propertyName);

            if (Objects.nonNull(originalValue)) {
                if (originalValue.equals(newValue)) {
                    return;
                }

                LOG.info("{} configuration property was changed from '{}' to '{}'", propertyName, originalValue, newValue);
            } else {
                if (Objects.isNull(newValue)) {
                    return;
                }

                LOG.info("{} configuration property was changed to '{}'", propertyName, newValue);
            }

            propertyMap.put(propertyName, newValue);
            listeners.forEach(listener -> listener.onPropertyChanged(propertyName, newValue));
        });
    }

    @Nonnull
    @Override
    public <T> T getProperty(@Nonnull final String key, @Nonnull final Function<String, T> transformer) {
        return transformer.apply(propertyMap.get(key));
    }

    @Nonnull
    @Override
    public AutoCloseable registerListener(@Nonnull final ConfigurationListener listener) {
        Verify.verify(!listeners.contains(listener));
        LOG.info("{} was registered as configuration listener to OpenFlowPlugin configuration service", listener);
        listeners.add(listener);
        propertyMap.forEach(listener::onPropertyChanged);
        return () -> listeners.remove(listener);
    }

    @Override
    public void close() throws Exception {
        propertyMap.clear();
        listeners.clear();
    }
}
