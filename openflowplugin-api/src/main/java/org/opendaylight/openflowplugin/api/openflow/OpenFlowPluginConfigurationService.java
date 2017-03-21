/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

/**
 * Manages OpenFlowPlugin configuration
 */
public interface OpenFlowPluginConfigurationService {

    /**
     * The enum Property type.
     */
    enum PropertyType {
        /**
         * Is statistics polling on property type.
         */
        IS_STATISTICS_POLLING_ON,
        /**
         * Barrier count limit property type.
         */
        BARRIER_COUNT_LIMIT,
        /**
         * Barrier interval timeout property type.
         */
        BARRIER_INTERVAL_TIMEOUT,
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
        THREAD_POOL_TIMEOUT;

        private static final Map<String, PropertyType> VALUE_MAP;

        /**
         * For value property type.
         *
         * @param name the name
         * @return the property type
         */
        public static PropertyType forValue(final String name) {
            return VALUE_MAP.get(name);
        }

        static {
            final PropertyType[] values = values();
            final ImmutableMap.Builder<String, PropertyType> builder = ImmutableMap.builder();

            for (final PropertyType value : values) {
                builder.put(value.getValue(), value);
            }

            VALUE_MAP = builder.build();
        }

        /**
         * Gets value.
         *
         * @return the value
         */
        public String getValue() {
            return this.name().toLowerCase().replace('_', '-');
        }
    }

    /**
     * Parses key-value pairs of properties and updates them
     *
     * @param properties properties
     */
    void update(Map<String,Object> properties);

    /**
     * Parse and update single property key-value pair
     *
     * @see org.opendaylight.openflowplugin.api.openflow.OpenFlowPluginConfigurationService.PropertyType
     * @param key   property type
     * @param value property value
     */
    void updateProperty(PropertyType key, Object value);

}
