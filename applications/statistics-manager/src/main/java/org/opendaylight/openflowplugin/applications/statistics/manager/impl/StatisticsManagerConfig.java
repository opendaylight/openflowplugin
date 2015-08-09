/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

public final class StatisticsManagerConfig {
    private final int maxNodesForCollector;
    private final int minRequestNetMonitorInterval;

    private StatisticsManagerConfig(StatisticsManagerConfigBuilder builder) {
        this.maxNodesForCollector = builder.getMaxNodesForCollector();
        this.minRequestNetMonitorInterval = builder.getMinRequestNetMonitorInterval();
    }

    public int getMaxNodesForCollector() {
        return maxNodesForCollector;
    }

    public int getMinRequestNetMonitorInterval() {
        return minRequestNetMonitorInterval;
    }

    public static StatisticsManagerConfigBuilder builder() {
        return new StatisticsManagerConfigBuilder();
    }

    public static class StatisticsManagerConfigBuilder {
        private int maxNodesForCollector;
        private int minRequestNetMonitorInterval;

        public int getMaxNodesForCollector() {
            return maxNodesForCollector;
        }

        public void setMaxNodesForCollector(int maxNodesForCollector) {
            this.maxNodesForCollector = maxNodesForCollector;
        }

        public int getMinRequestNetMonitorInterval() {
            return minRequestNetMonitorInterval;
        }

        public void setMinRequestNetMonitorInterval(int minRequestNetMonitorInterval) {
            this.minRequestNetMonitorInterval = minRequestNetMonitorInterval;
        }

        public StatisticsManagerConfig build() {
            return new StatisticsManagerConfig(this);
        }
    }
}
