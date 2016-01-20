/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
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
public class FlowNotificationSupplierImpl extends
        AbstractNotifSupplierForItem<Flow, FlowAdded, FlowUpdated, FlowRemoved> {

    private static final InstanceIdentifier<Flow> wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db                   - {@link DataBroker}
     */
    public FlowNotificationSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, Flow.class);
    }

    @Override
    public InstanceIdentifier<Flow> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public FlowAdded createNotification(final Flow o, final InstanceIdentifier<Flow> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final FlowAddedBuilder builder = new FlowAddedBuilder(o);
        builder.setFlowRef(new FlowRef(path));
        builder.setNode(createNodeRef(path));
        return builder.build();
    }

    @Override
    public FlowUpdated updateNotification(final Flow o, final InstanceIdentifier<Flow> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);
        final FlowUpdatedBuilder builder = new FlowUpdatedBuilder(o);
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

