/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public enum ForwardingRulesProperty {
    DISABLE_RECONCILIATION,
    STALE_MARKING_ENABLED,
    RECONCILIATION_RETRY_COUNT;


    private static final Map<String, ForwardingRulesProperty> KEY_VALUE_MAP;

    /**
     * Get property type from property key
     *
     * @param key the property key
     * @return the property type
     */
    public static ForwardingRulesProperty forValue(final String key) {
        return KEY_VALUE_MAP.get(key);
    }

    static {
        final ForwardingRulesProperty[] values = values();
        final ImmutableMap.Builder<String, ForwardingRulesProperty> builder = ImmutableMap.builder();

        for (final ForwardingRulesProperty value : values) {
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
