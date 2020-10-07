/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

/**
 * Implementation define a contract between {@link FlowCapableNodeConnectorQueueStatisticsData} data object
 * and {@link QueueStatisticsUpdate} notification.
 */
public class QueueStatNotificationSupplierImpl extends
        AbstractNotificationSupplierForItemStat<FlowCapableNodeConnectorQueueStatisticsData, QueueStatisticsUpdate> {

    private static final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData>
            FLOW_CAPABLE_NODE_CONNECTOR_QUEUE_STATISTICS_DATA_INSTANCE_IDENTIFIER = getNodeWildII()
            .child(NodeConnector.class).augmentation(FlowCapableNodeConnector.class).child(Queue.class)
            .augmentation(FlowCapableNodeConnectorQueueStatisticsData.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationPublishService}
     * @param db                   - {@link DataBroker}
     */
    public QueueStatNotificationSupplierImpl(final NotificationPublishService notifProviderService,
                                             final DataBroker db) {
        super(notifProviderService, db, FlowCapableNodeConnectorQueueStatisticsData.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> getWildCardPath() {
        return FLOW_CAPABLE_NODE_CONNECTOR_QUEUE_STATISTICS_DATA_INSTANCE_IDENTIFIER;
    }

    @Override
    public QueueStatisticsUpdate createNotification(
            final FlowCapableNodeConnectorQueueStatisticsData statisticsDataTreeItem,
            final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> path) {
        Preconditions.checkArgument(statisticsDataTreeItem != null);
        Preconditions.checkArgument(path != null);

        final NodeConnectorBuilder connBuilder = new NodeConnectorBuilder();
        final NodeConnectorKey key = path.firstKeyOf(NodeConnector.class);
        connBuilder.setId(key.getId());
        connBuilder.withKey(key);

        final QueueIdAndStatisticsMapBuilder queueStatMapBuilder =
            new QueueIdAndStatisticsMapBuilder(statisticsDataTreeItem.getFlowCapableNodeConnectorQueueStatistics())
                .setNodeConnectorId(key.getId()).setQueueId(path.firstKeyOf(Queue.class).getQueueId());

        return new QueueStatisticsUpdateBuilder()
            .setId(getNodeId(path))
            .setMoreReplies(Boolean.FALSE)
            .setQueueIdAndStatisticsMap(BindingMap.of(queueStatMapBuilder.build()))
            .setNodeConnector(BindingMap.of(connBuilder.build()))
            .build();
    }
}

