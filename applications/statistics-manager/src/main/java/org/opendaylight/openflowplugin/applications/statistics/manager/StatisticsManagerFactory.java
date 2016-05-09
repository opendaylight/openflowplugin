/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.statistics.manager.config.rev160509.StatisticsManagerAppConfig;

/**
 * Factory for creating StatisticsManager instances.
 *
 * @author Thomas Pantelis
 */
public interface StatisticsManagerFactory {
    StatisticsManager newInstance(StatisticsManagerAppConfig statsManagerAppConfig, DataBroker dataBroker,
            NotificationProviderService notifService, RpcConsumerRegistry rpcRegistry,
            EntityOwnershipService entityOwnershipService);

}
