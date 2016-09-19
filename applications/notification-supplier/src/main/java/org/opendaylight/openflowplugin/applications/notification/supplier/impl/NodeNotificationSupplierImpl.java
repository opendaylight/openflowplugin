/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link FlowCapableNode} data object
 * and {@link NodeUpdated} and {@link NodeRemoved} notifications.
 */
public class NodeNotificationSupplierImpl extends
        AbstractNotificationSupplierForItemRoot<FlowCapableNode, NodeUpdated, NodeRemoved> {

    private static final InstanceIdentifier<FlowCapableNode> wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db                   - {@link DataBroker}
     */
    public NodeNotificationSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowCapableNode.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNode> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public NodeUpdated createNotification(final FlowCapableNode o, final InstanceIdentifier<FlowCapableNode> ii) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(ii != null);
        final FlowCapableNodeUpdatedBuilder flowNodeNotifBuilder = new FlowCapableNodeUpdatedBuilder(o);
        final NodeUpdatedBuilder notifBuilder = new NodeUpdatedBuilder();
        notifBuilder.setId(ii.firstKeyOf(Node.class, NodeKey.class).getId());
        notifBuilder.setNodeRef(new NodeRef(getNodeII(ii)));
        notifBuilder.addAugmentation(FlowCapableNodeUpdated.class, flowNodeNotifBuilder.build());
        return notifBuilder.build();
    }

    @Override
    public NodeRemoved deleteNotification(final InstanceIdentifier<FlowCapableNode> path) {
        Preconditions.checkArgument(path != null);
        final NodeRemovedBuilder delNodeNotifBuilder = new NodeRemovedBuilder();
        delNodeNotifBuilder.setNodeRef(new NodeRef(path));
        return delNodeNotifBuilder.build();
    }
}

