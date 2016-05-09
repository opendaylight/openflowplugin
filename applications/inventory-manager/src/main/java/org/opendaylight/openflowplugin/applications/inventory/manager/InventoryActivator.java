/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.inventory.manager;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryActivator implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryActivator.class);

    private final FlowCapableInventoryProvider provider;

    public InventoryActivator(DataBroker dataBroker, NotificationProviderService notificationService,
            EntityOwnershipService eos) {
        provider = new FlowCapableInventoryProvider(dataBroker, notificationService, eos);
    }

    public void start() {
        provider.start();
    }

    @Override
    public void close() {
        try {
            provider.close();
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for shutdown", e);
        }
    }
}
