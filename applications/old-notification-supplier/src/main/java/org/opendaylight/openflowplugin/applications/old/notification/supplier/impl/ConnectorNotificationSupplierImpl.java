/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class ConnectorNotificationSupplierImpl extends
        AbstractNofitSupplierDefinition<FlowCapableNodeConnector, NodeConnectorUpdated, NodeConnectorUpdated, NodeConnectorRemoved> {

    private final InstanceIdentifier<FlowCapableNodeConnector> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public ConnectorNotificationSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowCapableNodeConnector.class);
        wildCardedInstanceIdent = getNodeWildII().child(NodeConnector.class).augmentation(FlowCapableNodeConnector.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnector> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public NodeConnectorUpdated createNotification(final FlowCapableNodeConnector o,
            final InstanceIdentifier<FlowCapableNodeConnector> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final NodeConnectorUpdatedBuilder notifBuilder = new NodeConnectorUpdatedBuilder();
        final FlowCapableNodeConnectorUpdatedBuilder connNotifBuilder = new FlowCapableNodeConnectorUpdatedBuilder(o);
        notifBuilder.setId(path.firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId());
        notifBuilder.setNodeConnectorRef(new NodeConnectorRef(path));
        notifBuilder.addAugmentation(FlowCapableNodeConnectorUpdated.class, connNotifBuilder.build());
        return notifBuilder.build();
    }

    @Override
    public NodeConnectorUpdated updateNotification(final FlowCapableNodeConnector o,
            final InstanceIdentifier<FlowCapableNodeConnector> path) {
        // NOOP
        return null;
    }

    @Override
    public NodeConnectorRemoved deleteNotification(final InstanceIdentifier<FlowCapableNodeConnector> path) {
        Preconditions.checkArgument(path != null);
        final NodeConnectorRemovedBuilder notifBuilder = new NodeConnectorRemovedBuilder();
        notifBuilder.setNodeConnectorRef(new NodeConnectorRef(path));
        return notifBuilder.build();
    }
}

