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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapListBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link FlowStatistics} data object
 * and {@link FlowsStatisticsUpdate} notification.
 */
public class FlowStatNotifSupplierImpl extends AbstractNotifSupplierForItemStat<FlowStatistics, FlowsStatisticsUpdate> {

    private static final InstanceIdentifier<FlowStatistics> wildCardedInstanceIdent =
            getNodeWildII().augmentation(FlowCapableNode.class).child(Table.class)
                    .child(Flow.class).augmentation(FlowStatisticsData.class).child(FlowStatistics.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db - {@link DataBroker}
     */
    public FlowStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowStatistics> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public FlowsStatisticsUpdate createNotification(final FlowStatistics o, final InstanceIdentifier<FlowStatistics> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);

        final FlowAndStatisticsMapListBuilder fsmlBuilder = new FlowAndStatisticsMapListBuilder(o);
        fsmlBuilder.setFlowId(new FlowId(path.firstKeyOf(Flow.class, FlowKey.class).getId().getValue()));

        final FlowsStatisticsUpdateBuilder builder = new FlowsStatisticsUpdateBuilder();
        builder.setId(getNodeId(path));
        builder.setMoreReplies(Boolean.FALSE);
        // NOTE : fix if it needs, but we have to ask DataStore for the NodeConnector list
        builder.setNodeConnector(Collections.<NodeConnector> emptyList());
        builder.setFlowAndStatisticsMapList(Collections.singletonList(fsmlBuilder.build()));
        return builder.build();
    }
}

