/**
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;

public enum TopologyLLDPDiscoveryProperty {
    LLDP_SECURE_KEY,
    TOPOLOGY_LLDP_INTERVAL,
    TOPOLOGY_LLDP_EXPIRATION_INTERVAL;

    private static final Map<String, TopologyLLDPDiscoveryProperty> KEY_VALUE_MAP;

    /**
     * Get property type from property key.
     *
     * @param key the property key
     * @return the property type
     */
    public static TopologyLLDPDiscoveryProperty forValue(final String key) {
        return KEY_VALUE_MAP.get(key);
    }

    static {
        final TopologyLLDPDiscoveryProperty[] values = values();
        final ImmutableMap.Builder<String, TopologyLLDPDiscoveryProperty> builder = ImmutableMap.builder();

        for (final TopologyLLDPDiscoveryProperty value : values) {
            builder.put(value.toString(), value);
        }

        KEY_VALUE_MAP = builder.build();
    }

    /**
     * Converts enum name to property key.
     *
     * @return the property key
     */
    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
