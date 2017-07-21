/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.configuration;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * Enum of property keys. All keys from OpenFlowPlugin configuration file are parsed to this enum.
 * Each enum value represents one working configuration key in format
 * ENUM.name().toLowerCase().replace('_', '-'), so for example ConfigurationProperty.IS_STATISTICS_POLLING_ON
 * represents 'is-statistics-polling-on' configuration key.
 */
public enum ConfigurationProperty {
    /**
     * Is statistics polling on property type.
     */
    IS_STATISTICS_POLLING_ON,
    /**
     * Barrier count limit property type.
     */
    BARRIER_COUNT_LIMIT,
    /**
     * Barrier interval timeout limit property type.
     */
    BARRIER_INTERVAL_TIMEOUT_LIMIT,
    /**
     * Echo reply timeout property type.
     */
    ECHO_REPLY_TIMEOUT,
    /**
     * Enable flow removed notification property type.
     */
    ENABLE_FLOW_REMOVED_NOTIFICATION,
    /**
     * Skip table features property type.
     */
    SKIP_TABLE_FEATURES,
    /**
     * Basic timer delay property type.
     */
    BASIC_TIMER_DELAY,
    /**
     * Maximum timer delay property type.
     */
    MAXIMUM_TIMER_DELAY,
    /**
     * Switch features mandatory property type.
     */
    SWITCH_FEATURES_MANDATORY,
    /**
     * Is statistics rpc enabled property type.
     */
    @Deprecated
    IS_STATISTICS_RPC_ENABLED,
    /**
     * Use single layer serialization property type.
     */
    USE_SINGLE_LAYER_SERIALIZATION,
    /**
     * Rpc requests quota property type.
     */
    RPC_REQUESTS_QUOTA,
    /**
     * Global notification quota property type.
     */
    GLOBAL_NOTIFICATION_QUOTA,
    /**
     * Thread pool min threads property type.
     */
    THREAD_POOL_MIN_THREADS,
    /**
     * Thread pool max threads property type.
     */
    THREAD_POOL_MAX_THREADS,
    /**
     * Thread pool timeout property type.
     */
    THREAD_POOL_TIMEOUT,
    /**
     * True if plugin is using reconciliation framework.
     */
    USING_RECONCILIATION_FRAMEWORK;

    private static final Map<String, ConfigurationProperty> KEY_VALUE_MAP;

    /**
     * Get property type from property key
     *
     * @param key the property key
     * @return the property type
     */
    public static ConfigurationProperty forValue(final String key) {
        return KEY_VALUE_MAP.get(key);
    }

    static {
        final ConfigurationProperty[] values = values();
        final ImmutableMap.Builder<String, ConfigurationProperty> builder = ImmutableMap.builder();

        for (final ConfigurationProperty value : values) {
            builder.put(value.toString(), value);
        }

        KEY_VALUE_MAP = builder.build();
    }

    /**
     * Converts enum name to property key
     *
     * @return the property key
     */
    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', '-');
    }
}
