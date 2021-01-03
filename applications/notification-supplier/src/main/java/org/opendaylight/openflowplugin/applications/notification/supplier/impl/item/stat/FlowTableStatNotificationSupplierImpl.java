/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

/**
 * Implementation define a contract between {@link FlowTableStatistics} data object
 * and {@link FlowTableStatisticsUpdate} notification.
 */
public class FlowTableStatNotificationSupplierImpl extends
        AbstractNotificationSupplierForItemStat<FlowTableStatistics, FlowTableStatisticsUpdate> {

    private static final InstanceIdentifier<FlowTableStatistics> FLOW_TABLE_STATISTICS_INSTANCE_IDENTIFIER
            = getNodeWildII().augmentation(FlowCapableNode.class).child(Table.class)
            .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationPublishService}
     * @param db                   - {@link DataBroker}
     */
    public FlowTableStatNotificationSupplierImpl(final NotificationPublishService notifProviderService,
                                                 final DataBroker db) {
        super(notifProviderService, db, FlowTableStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowTableStatistics> getWildCardPath() {
        return FLOW_TABLE_STATISTICS_INSTANCE_IDENTIFIER;
    }

    @Override
    public FlowTableStatisticsUpdate createNotification(final FlowTableStatistics flowTableStatistics,
                                                        final InstanceIdentifier<FlowTableStatistics> path) {
        checkArgument(flowTableStatistics != null);
        checkArgument(path != null);

        return new FlowTableStatisticsUpdateBuilder()
            .setId(getNodeId(path))
            .setMoreReplies(Boolean.FALSE)
            // NOTE : fix if it needs, but we have to ask DataStore for the NodeConnector list
            .setNodeConnector(BindingMap.of())
            .setFlowTableAndStatisticsMap(BindingMap.of(new FlowTableAndStatisticsMapBuilder(flowTableStatistics)
                .withKey(new FlowTableAndStatisticsMapKey(new TableId(path.firstKeyOf(Table.class).getId())))
                .build()))
            .build();
    }
}

