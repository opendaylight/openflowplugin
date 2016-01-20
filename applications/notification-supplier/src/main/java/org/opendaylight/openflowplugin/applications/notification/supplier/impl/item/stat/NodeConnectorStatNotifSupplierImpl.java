/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link FlowCapableNodeConnectorStatistics} data object
 * and {@link NodeConnectorStatisticsUpdate} notification.
 */
public class NodeConnectorStatNotifSupplierImpl extends
        AbstractNotifSupplierForItemStat<FlowCapableNodeConnectorStatistics, NodeConnectorStatisticsUpdate> {

    private static final InstanceIdentifier<FlowCapableNodeConnectorStatistics> wildCardedInstanceIdent =
            getNodeWildII().child(NodeConnector.class)
                    .augmentation(FlowCapableNodeConnectorStatisticsData.class)
                    .child(FlowCapableNodeConnectorStatistics.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db                   - {@link DataBroker}
     */
    public NodeConnectorStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowCapableNodeConnectorStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnectorStatistics> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public NodeConnectorStatisticsUpdate createNotification(final FlowCapableNodeConnectorStatistics o,
                                                            final InstanceIdentifier<FlowCapableNodeConnectorStatistics> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);

        final NodeConnectorBuilder ncBuilder = new NodeConnectorBuilder();
        final NodeConnectorKey ncKey = path.firstKeyOf(NodeConnector.class, NodeConnectorKey.class);
        ncBuilder.setId(ncKey.getId());
        ncBuilder.setKey(ncKey);

        final NodeConnectorStatisticsUpdateBuilder builder = new NodeConnectorStatisticsUpdateBuilder();
        builder.setId(getNodeId(path));
        builder.setMoreReplies(Boolean.FALSE);
        builder.setNodeConnector(Collections.singletonList(ncBuilder.build()));
        builder.setNodeConnectorStatisticsAndPortNumberMap(Collections
                .singletonList(new NodeConnectorStatisticsAndPortNumberMapBuilder(o).build()));
        return builder.build();
    }
}

