/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNofitSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class QueueStatNotifSupplierImpl extends
        AbstractNofitSupplierDefinition<FlowCapableNodeConnectorQueueStatisticsData, QueueStatisticsUpdate, QueueStatisticsUpdate, QueueStatisticsUpdate> {

    private final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public QueueStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowCapableNodeConnectorQueueStatisticsData.class);
        wildCardedInstanceIdent = getNodeWildII().child(NodeConnector.class)
                .augmentation(FlowCapableNodeConnector.class).child(Queue.class)
                .augmentation(FlowCapableNodeConnectorQueueStatisticsData.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public QueueStatisticsUpdate createNotification(final FlowCapableNodeConnectorQueueStatisticsData o,
            final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);

        final NodeConnectorBuilder connBuilder = new NodeConnectorBuilder();
        final NodeConnectorKey key = path.firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
        connBuilder.setId(key.getId());
        connBuilder.setKey(key);

        final QueueIdAndStatisticsMapBuilder queueStatMapBuilder =
                new QueueIdAndStatisticsMapBuilder(o.getFlowCapableNodeConnectorQueueStatistics());

        final QueueStatisticsUpdateBuilder builder = new QueueStatisticsUpdateBuilder();
        builder.setId(path.firstKeyOf(Node.class, NodeKey.class).getId());
        builder.setMoreReplies(Boolean.FALSE);
        builder.setQueueIdAndStatisticsMap(Collections.singletonList(queueStatMapBuilder.build()));
        builder.setNodeConnector(Collections.singletonList(connBuilder.build()));
        return builder.build();
    }

    @Override
    public QueueStatisticsUpdate updateNotification(final FlowCapableNodeConnectorQueueStatisticsData o,
            final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> path) {
        // NOOP - statistics are not react for update
        return null;
    }

    @Override
    public QueueStatisticsUpdate deleteNotification(
            final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> path) {
        // NOOP - statistics are not react for delete
        return null;
    }

}

