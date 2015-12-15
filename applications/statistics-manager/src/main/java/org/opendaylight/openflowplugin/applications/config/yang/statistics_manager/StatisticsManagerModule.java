/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.config.yang.statistics_manager;

import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.openflowplugin.applications.statistics.manager.impl.StatisticsManagerConfig;
import org.opendaylight.openflowplugin.applications.statistics.manager.impl.StatisticsManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsManagerModule extends org.opendaylight.openflowplugin.applications.config.yang.statistics_manager.AbstractStatisticsManagerModule {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerModule.class);

    private static final int MAX_NODES_FOR_COLLECTOR_DEFAULT = 16;
    private static final int MIN_REQUEST_NET_MONITOR_INTERVAL_DEFAULT = 3000;

    public StatisticsManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public StatisticsManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.openflowplugin.applications.config.yang.statistics_manager.StatisticsManagerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("StatisticsManager module initialization.");
        final StatisticsManagerConfig config = createConfig();
        final StatisticsManager statisticsManagerProvider = new StatisticsManagerImpl(getDataBrokerDependency(), config);
        statisticsManagerProvider.start(getNotificationServiceDependency(), getRpcRegistryDependency());

        final StatisticsManager statisticsManagerProviderExposed = statisticsManagerProvider;
        LOG.info("StatisticsManager started successfully.");
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                try {
                    statisticsManagerProviderExposed.close();
                }
                catch (final Exception e) {
                    LOG.error("Unexpected error by stopping StatisticsManager module", e);
                }
                LOG.info("StatisticsManager module stopped.");
            }
        };
    }

    private StatisticsManagerConfig createConfig() {
        final StatisticsManagerConfig.StatisticsManagerConfigBuilder builder = StatisticsManagerConfig.builder();
        if (getStatisticsManagerSettings() != null && getStatisticsManagerSettings().getMaxNodesForCollector() != null) {
            builder.setMaxNodesForCollector(getStatisticsManagerSettings().getMaxNodesForCollector());
        } else {
            LOG.warn("Load the xml ConfigSubsystem input value fail! MaxNodesForCollector value is set to {} ",
                    MAX_NODES_FOR_COLLECTOR_DEFAULT);
            builder.setMaxNodesForCollector(MAX_NODES_FOR_COLLECTOR_DEFAULT);
        }
        if (getStatisticsManagerSettings() != null &&
                getStatisticsManagerSettings().getMinRequestNetMonitorInterval() != null) {
            builder.setMinRequestNetMonitorInterval(getStatisticsManagerSettings().getMinRequestNetMonitorInterval());
        } else {
            LOG.warn("Load the xml CofnigSubsystem input value fail! MinRequestNetMonitorInterval value is set to {} ",
                    MIN_REQUEST_NET_MONITOR_INTERVAL_DEFAULT);
            builder.setMinRequestNetMonitorInterval(MIN_REQUEST_NET_MONITOR_INTERVAL_DEFAULT);
        }
        return builder.build();
    }
}
