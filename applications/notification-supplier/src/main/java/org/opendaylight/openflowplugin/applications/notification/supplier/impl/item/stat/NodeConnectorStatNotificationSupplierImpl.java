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
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

/**
 * Implementation define a contract between {@link FlowCapableNodeConnectorStatistics} data object
 * and {@link NodeConnectorStatisticsUpdate} notification.
 */
public class NodeConnectorStatNotificationSupplierImpl extends
        AbstractNotificationSupplierForItemStat<FlowCapableNodeConnectorStatistics, NodeConnectorStatisticsUpdate> {

    private static final InstanceIdentifier<FlowCapableNodeConnectorStatistics>
            FLOW_CAPABLE_NODE_CONNECTOR_STATISTICS_INSTANCE_IDENTIFIER = getNodeWildII().child(NodeConnector.class)
            .augmentation(FlowCapableNodeConnectorStatisticsData.class).child(FlowCapableNodeConnectorStatistics.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationPublishService}
     * @param db                   - {@link DataBroker}
     */
    public NodeConnectorStatNotificationSupplierImpl(final NotificationPublishService notifProviderService,
                                                     final DataBroker db) {
        super(notifProviderService, db, FlowCapableNodeConnectorStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnectorStatistics> getWildCardPath() {
        return FLOW_CAPABLE_NODE_CONNECTOR_STATISTICS_INSTANCE_IDENTIFIER;
    }

    @Override
    public NodeConnectorStatisticsUpdate createNotification(
            final FlowCapableNodeConnectorStatistics flowCapableNodeConnectorStatistics,
            final InstanceIdentifier<FlowCapableNodeConnectorStatistics> path) {
        Preconditions.checkArgument(flowCapableNodeConnectorStatistics != null);
        Preconditions.checkArgument(path != null);

        final NodeConnectorBuilder ncBuilder = new NodeConnectorBuilder();
        final NodeConnectorKey ncKey = path.firstKeyOf(NodeConnector.class);
        ncBuilder.setId(ncKey.getId());
        ncBuilder.withKey(ncKey);

        return new NodeConnectorStatisticsUpdateBuilder()
            .setId(getNodeId(path))
            .setMoreReplies(Boolean.FALSE)
            .setNodeConnector(BindingMap.of(ncBuilder.build()))
            .setNodeConnectorStatisticsAndPortNumberMap(BindingMap.of(
                new NodeConnectorStatisticsAndPortNumberMapBuilder(flowCapableNodeConnectorStatistics)
                .setNodeConnectorId(ncKey.getId()).build()))
            .build();
    }
}

