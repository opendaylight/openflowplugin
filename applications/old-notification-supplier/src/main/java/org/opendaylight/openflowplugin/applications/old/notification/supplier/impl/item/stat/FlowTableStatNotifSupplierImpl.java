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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class FlowTableStatNotifSupplierImpl
        extends
        AbstractNofitSupplierDefinition<FlowTableStatistics, FlowTableStatisticsUpdate, FlowTableStatisticsUpdate, FlowTableStatisticsUpdate> {

    private final InstanceIdentifier<FlowTableStatistics> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public FlowTableStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowTableStatistics.class);
        wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class).child(Table.class)
                .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowTableStatistics> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public FlowTableStatisticsUpdate createNotification(final FlowTableStatistics o,
            final InstanceIdentifier<FlowTableStatistics> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);

        final FlowTableStatisticsUpdateBuilder builder = new FlowTableStatisticsUpdateBuilder();
        builder.setId(path.firstKeyOf(Node.class, NodeKey.class).getId());
        builder.setMoreReplies(Boolean.FALSE);
        // TODO : fix if it needs, but we have to ask DataStore for the NodeConnector list
        builder.setNodeConnector(Collections.<NodeConnector> emptyList());
        builder.setFlowTableAndStatisticsMap(Collections.singletonList(new FlowTableAndStatisticsMapBuilder(o).build()));
        return builder.build();
    }

    @Override
    public FlowTableStatisticsUpdate updateNotification(final FlowTableStatistics o,
            final InstanceIdentifier<FlowTableStatistics> path) {
        // NOOP
        return null;
    }

    @Override
    public FlowTableStatisticsUpdate deleteNotification(final InstanceIdentifier<FlowTableStatistics> path) {
        // NOOP
        return null;
    }

}

