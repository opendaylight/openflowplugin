/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAddedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowRef;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link Flow} data object
 * and {@link FlowAdded}, {@link FlowUpdated} and {@link FlowRemoved} notifications.
 */
public class FlowNotificationSupplierImpl extends AbstractNotificationSupplierForItem<Flow, FlowAdded, FlowUpdated,
        FlowRemoved> {

    private static final InstanceIdentifier<Flow> FLOW_INSTANCE_IDENTIFIER = getNodeWildII()
            .augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationPublishService}
     * @param db                   - {@link DataBroker}
     */
    public FlowNotificationSupplierImpl(final NotificationPublishService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, Flow.class);
    }

    @Override
    public InstanceIdentifier<Flow> getWildCardPath() {
        return FLOW_INSTANCE_IDENTIFIER;
    }

    @Override
    public FlowAdded createNotification(final Flow dataTreeItemObject, final InstanceIdentifier<Flow> path) {
        Preconditions.checkArgument(dataTreeItemObject != null);
        Preconditions.checkArgument(path != null);
        final FlowAddedBuilder builder = new FlowAddedBuilder(dataTreeItemObject);
        builder.setFlowRef(new FlowRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }

    @Override
    public FlowUpdated updateNotification(final Flow flow, final InstanceIdentifier<Flow> path) {
        Preconditions.checkArgument(flow != null);
        Preconditions.checkArgument(path != null);
        final FlowUpdatedBuilder builder = new FlowUpdatedBuilder(flow);
        builder.setFlowRef(new FlowRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }

    @Override
    public FlowRemoved deleteNotification(final InstanceIdentifier<Flow> path) {
        Preconditions.checkArgument(path != null);
        final FlowRemovedBuilder builder = new FlowRemovedBuilder();
        builder.setFlowRef(new FlowRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }
}

