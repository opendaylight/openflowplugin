/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat;

import com.google.common.base.Preconditions;
import java.util.Collections;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;

/**
 * Implementation define a contract between {@link MeterStatistics} data object
 * and {@link MeterStatisticsUpdated} notification.
 */
public class MeterStatNotificationSupplierImpl extends AbstractNotificationSupplierForItemStat<MeterStatistics,
        MeterStatisticsUpdated> {

    private static final InstanceIdentifier<MeterStatistics> METER_STATISTICS_INSTANCE_IDENTIFIER = getNodeWildII()
            .augmentation(FlowCapableNode.class).child(Meter.class).augmentation(NodeMeterStatistics.class)
            .child(MeterStatistics.class);

    /**
     * Constructor register supplier as DataTreeChangeListener and create wildCarded InstanceIdentifier.
     *
     * @param notifProviderService - {@link NotificationPublishService}
     * @param db                   - {@link DataBroker}
     */
    public MeterStatNotificationSupplierImpl(final NotificationPublishService notifProviderService,
                                             final DataBroker db) {
        super(notifProviderService, db, MeterStatistics.class);
    }

    @Override
    public InstanceIdentifier<MeterStatistics> getWildCardPath() {
        return METER_STATISTICS_INSTANCE_IDENTIFIER;
    }

    @Override
    public MeterStatisticsUpdated createNotification(final MeterStatistics meterStatistics,
                                                     final InstanceIdentifier<MeterStatistics> path) {
        Preconditions.checkArgument(meterStatistics != null);
        Preconditions.checkArgument(path != null);

        final MeterStatisticsUpdatedBuilder builder = new MeterStatisticsUpdatedBuilder();
        builder.setId(getNodeId(path));
        builder.setMoreReplies(Boolean.FALSE);
        // TODO : fix if it needs, but we have to ask DataStore for the NodeConnector list
        builder.setNodeConnector(Collections.emptyMap());
        builder.setMeterStats(BindingMap.of(new MeterStatsBuilder(meterStatistics)
            .setMeterId(path.firstKeyOf(Meter.class).getMeterId())
            .build()));
        return builder.build();
    }
}

