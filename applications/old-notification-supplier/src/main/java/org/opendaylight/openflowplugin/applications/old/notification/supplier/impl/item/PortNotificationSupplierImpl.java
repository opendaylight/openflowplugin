/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNofitSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.PortRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.PortRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.PortUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.PortUpdatedBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class PortNotificationSupplierImpl extends
        AbstractNofitSupplierDefinition<FlowCapableNodeConnector, PortUpdated, PortUpdated, PortRemoved> {

    private final InstanceIdentifier<FlowCapableNodeConnector> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public PortNotificationSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowCapableNodeConnector.class);
        wildCardedInstanceIdent = getNodeWildII().child(NodeConnector.class).augmentation(FlowCapableNodeConnector.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnector> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public PortUpdated createNotification(final FlowCapableNodeConnector o,
            final InstanceIdentifier<FlowCapableNodeConnector> ii) {

        final PortUpdatedBuilder builder = new PortUpdatedBuilder();
        builder.setNode(new NodeRef(new NodeRef(ii.firstIdentifierOf(Node.class))));
        // TODO :
        return builder.build();
    }

    @Override
    public PortUpdated updateNotification(final FlowCapableNodeConnector o,
            final InstanceIdentifier<FlowCapableNodeConnector> ii) {
        return null;
    }

    @Override
    public PortRemoved deleteNotification(final InstanceIdentifier<FlowCapableNodeConnector> path) {
        final PortRemovedBuilder builder = new PortRemovedBuilder();
        builder.setNode(new NodeRef(new NodeRef(path.firstIdentifierOf(Node.class))));
        // TODO :
        return builder.build();
    }
}

