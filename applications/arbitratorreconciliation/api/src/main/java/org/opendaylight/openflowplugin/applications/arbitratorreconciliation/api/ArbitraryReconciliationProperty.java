/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.arbitratorreconciliation.api;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;

public enum ArbitraryReconciliationProperty {
        ARBITRARY_RECONCILIATION_ENABLED;


        private static final Map<String, ArbitraryReconciliationProperty> KEY_VALUE_MAP;

        /**
         * Get property type from property key.
         *
         * @param key the property key
         * @return the property type
         */
        public static ArbitraryReconciliationProperty forValue(final String key) {
                return KEY_VALUE_MAP.get(key);
        }

        static {
                final ArbitraryReconciliationProperty[] values = values();
                final ImmutableMap.Builder<String, ArbitraryReconciliationProperty> builder = ImmutableMap.builder();

                for (final ArbitraryReconciliationProperty value : values) {
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
