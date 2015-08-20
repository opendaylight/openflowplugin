/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.stat;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNofitSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConnectorStatNotifSupplierImpl extends
        AbstractNofitSupplierDefinition<Node, NodeConnectorStatisticsUpdate, NodeConnectorStatisticsUpdate, NodeConnectorStatisticsUpdate> {

    public ConnectorStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, Node.class);

    }

    @Override
    public InstanceIdentifier<Node> getWildCardPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeConnectorStatisticsUpdate createNotification(final Node o, final InstanceIdentifier<Node> ii) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeConnectorStatisticsUpdate updateNotification(final Node o, final InstanceIdentifier<Node> ii) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeConnectorStatisticsUpdate deleteNotification(final InstanceIdentifier<Node> path) {
        // TODO Auto-generated method stub
        return null;
    }
}

