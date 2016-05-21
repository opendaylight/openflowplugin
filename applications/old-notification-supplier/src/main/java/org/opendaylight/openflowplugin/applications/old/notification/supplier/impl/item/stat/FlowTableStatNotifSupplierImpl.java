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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link FlowTableStatistics} data object
 * and {@link FlowTableStatisticsUpdate} notification.
 */
public class FlowTableStatNotifSupplierImpl extends
        AbstractNotifSupplierForItemStat<FlowTableStatistics, FlowTableStatisticsUpdate> {

    private static final InstanceIdentifier<FlowTableStatistics> wildCardedInstanceIdent =
            getNodeWildII().augmentation(FlowCapableNode.class).child(Table.class)
                    .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db - {@link DataBroker}
     */
    public FlowTableStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowTableStatistics.class);
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

        final FlowTableAndStatisticsMapBuilder ftsmBuilder = new FlowTableAndStatisticsMapBuilder(o);
        ftsmBuilder.setKey(new FlowTableAndStatisticsMapKey(new TableId(path.firstKeyOf(Table.class, TableKey.class).getId())));

        final FlowTableStatisticsUpdateBuilder builder = new FlowTableStatisticsUpdateBuilder();
        builder.setId(getNodeId(path));
        builder.setMoreReplies(Boolean.FALSE);
        // NOTE : fix if it needs, but we have to ask DataStore for the NodeConnector list
        builder.setNodeConnector(Collections.<NodeConnector> emptyList());
        builder.setFlowTableAndStatisticsMap(Collections.singletonList(ftsmBuilder.build()));
        return builder.build();
    }
}

