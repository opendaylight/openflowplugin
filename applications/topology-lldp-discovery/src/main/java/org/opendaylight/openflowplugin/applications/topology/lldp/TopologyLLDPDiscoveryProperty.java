package org.opendaylight.openflowplugin.applications.topology.lldp;

import com.google.common.collect.ImmutableMap;

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
        return this.name().toLowerCase().replace('_', '-');
    }
}
