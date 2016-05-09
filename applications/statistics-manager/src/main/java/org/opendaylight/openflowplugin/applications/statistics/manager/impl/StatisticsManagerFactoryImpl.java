/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManager;
import org.opendaylight.openflowplugin.applications.statistics.manager.StatisticsManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.statistics.manager.config.rev160509.StatisticsManagerAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of StatisticsManagerFactory.
 *
 * @author Thomas Pantelis
 */
public class StatisticsManagerFactoryImpl implements StatisticsManagerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsManagerFactoryImpl.class);

    @Override
    public StatisticsManager newInstance(StatisticsManagerAppConfig statsManagerAppConfig, DataBroker dataBroker,
            NotificationProviderService notifService, RpcConsumerRegistry rpcRegistry,
            EntityOwnershipService entityOwnershipService) {
        LOG.info("StatisticsManager module initialization.");

        StatisticsManagerConfig.StatisticsManagerConfigBuilder configBuilder = StatisticsManagerConfig.builder();
        configBuilder.setMaxNodesForCollector(statsManagerAppConfig.getMaxNodesForCollector());
        configBuilder.setMinRequestNetMonitorInterval(statsManagerAppConfig.getMinRequestNetMonitorInterval());

        StatisticsManager statisticsManager = new StatisticsManagerImpl(dataBroker, configBuilder.build());
        statisticsManager.setOwnershipService(entityOwnershipService);
        statisticsManager.start(notifService, rpcRegistry);

        LOG.info("StatisticsManager started successfully.");

        return statisticsManager;
    }
}
