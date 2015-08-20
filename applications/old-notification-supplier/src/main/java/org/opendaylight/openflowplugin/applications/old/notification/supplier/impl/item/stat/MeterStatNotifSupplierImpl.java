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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link MeterStatistics} data object
 * and {@link MeterStatisticsUpdated} notification.
 */
public class MeterStatNotifSupplierImpl extends
        AbstractNotifSupplierForItemStat<MeterStatistics, MeterStatisticsUpdated> {

    private static final InstanceIdentifier<MeterStatistics> wildCardedInstanceIdent =
            getNodeWildII().augmentation(FlowCapableNode.class).child(Meter.class)
                    .augmentation(NodeMeterStatistics.class).child(MeterStatistics.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationProviderService}
     * @param db - {@link DataBroker}
     */
    public MeterStatNotifSupplierImpl(final NotificationProviderService notifProviderService, final DataBroker db) {
        super(notifProviderService, db, MeterStatistics.class);
    }

    @Override
    public InstanceIdentifier<MeterStatistics> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public MeterStatisticsUpdated createNotification(final MeterStatistics o,
            final InstanceIdentifier<MeterStatistics> path) {
        Preconditions.checkArgument(o != null);
        Preconditions.checkArgument(path != null);

        final MeterStatisticsUpdatedBuilder builder = new MeterStatisticsUpdatedBuilder();
        builder.setId(getNodeId(path));
        builder.setMoreReplies(Boolean.FALSE);
        // TODO : fix if it needs, but we have to ask DataStore for the NodeConnector list
        builder.setNodeConnector(Collections.<NodeConnector> emptyList());
        builder.setMeterStats(Collections.singletonList(new MeterStatsBuilder(o).build()));
        return builder.build();
    }
}

