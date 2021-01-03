/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link FlowStatistics} data object
 * and {@link FlowsStatisticsUpdate} notification.
 */
public class FlowStatNotificationSupplierImpl extends AbstractNotificationSupplierForItemStat<FlowStatistics,
        FlowsStatisticsUpdate> {

    private static final InstanceIdentifier<FlowStatistics> FLOW_STATISTICS_INSTANCE_IDENTIFIER = getNodeWildII()
            .augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class)
            .augmentation(FlowStatisticsData.class).child(FlowStatistics.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationPublishService}
     * @param db                   - {@link DataBroker}
     */
    public FlowStatNotificationSupplierImpl(final NotificationPublishService notifProviderService,
                                            final DataBroker db) {
        super(notifProviderService, db, FlowStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowStatistics> getWildCardPath() {
        return FLOW_STATISTICS_INSTANCE_IDENTIFIER;
    }

    @Override
    public FlowsStatisticsUpdate createNotification(final FlowStatistics flowStatistics,
                                                    final InstanceIdentifier<FlowStatistics> path) {
        checkArgument(flowStatistics != null);
        checkArgument(path != null);

        return new FlowsStatisticsUpdateBuilder()
            .setId(getNodeId(path))
            .setMoreReplies(Boolean.FALSE)
            // NOTE : fix if it needs, but we have to ask DataStore for the NodeConnector list
            .setNodeConnector(Collections.emptyMap())
            .setFlowAndStatisticsMapList(Collections.singletonList(new FlowAndStatisticsMapListBuilder(flowStatistics)
                .setFlowId(new FlowId(path.firstKeyOf(Flow.class).getId().getValue()))
                .build()))
            .build();
    }
}

