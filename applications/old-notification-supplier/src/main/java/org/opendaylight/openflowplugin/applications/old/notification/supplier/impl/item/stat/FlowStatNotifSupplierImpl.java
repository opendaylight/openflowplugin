/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.stat;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.AbstractNofitSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 *
 */
public class FlowStatNotifSupplierImpl extends AbstractNofitSupplierDefinition<FlowStatistics, FlowsStatisticsUpdate, FlowsStatisticsUpdate, FlowsStatisticsUpdate> {

    private final InstanceIdentifier<FlowStatistics> wildCardedInstanceIdent;

    /**
     * @param notifProviderService
     * @param db
     */
    public FlowStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, FlowStatistics.class);
        wildCardedInstanceIdent = getNodeWildII().augmentation(FlowCapableNode.class).child(Table.class)
                .child(Flow.class).augmentation(FlowStatisticsData.class).child(FlowStatistics.class);
    }

    @Override
    public InstanceIdentifier<FlowStatistics> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public FlowsStatisticsUpdate createNotification(final FlowStatistics o, final InstanceIdentifier<FlowStatistics> path) {
        final FlowsStatisticsUpdateBuilder builder = new FlowsStatisticsUpdateBuilder();

        // FlowStatisticsUpdate is notification from device statistics request and it represent multipart msg (so thing about it)
        
        return builder.build();
    }

    @Override
    public FlowsStatisticsUpdate updateNotification(final FlowStatistics o, final InstanceIdentifier<FlowStatistics> path) {
        // NOOP
        return null;
    }

    @Override
    public FlowsStatisticsUpdate deleteNotification(final InstanceIdentifier<FlowStatistics> path) {
        // NOOP
        return null;
    }
}

