/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.notification.supplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.NodeConnectorNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.NodeNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.FlowNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.GroupNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.MeterNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat.FlowStatNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat.FlowTableStatNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat.GroupStatNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat.MeterStatNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat.NodeConnectorStatNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.impl.item.stat.QueueStatNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.notification.supplier.tools.NotificationProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;

/**
 * Provider Implementation
 */
public class NotificationProvider implements AutoCloseable {

    private final DataBroker db;
    private final NotificationProviderConfig config;
    private final NotificationProviderService nps;

    /* Supplier List property help for easy close method implementation and testing */
    private List<NotificationSupplierDefinition<?>> supplierList;
    private NotificationSupplierForItemRoot<FlowCapableNode, NodeUpdated, NodeRemoved> nodeSupp;
    private NotificationSupplierForItemRoot<FlowCapableNodeConnector, NodeConnectorUpdated, NodeConnectorRemoved> connectorSupp;
    private NotificationSupplierForItem<Flow, FlowAdded, FlowUpdated, FlowRemoved> flowSupp;
    private NotificationSupplierForItem<Meter, MeterAdded, MeterUpdated, MeterRemoved> meterSupp;
    private NotificationSupplierForItem<Group, GroupAdded, GroupUpdated, GroupRemoved> groupSupp;
    private NotificationSupplierForItemStat<FlowCapableNodeConnectorStatistics, NodeConnectorStatisticsUpdate> connectorStatSupp;
    private NotificationSupplierForItemStat<FlowStatistics, FlowsStatisticsUpdate> flowStatSupp;
    private NotificationSupplierForItemStat<FlowTableStatistics, FlowTableStatisticsUpdate> flowTableStatSupp;
    private NotificationSupplierForItemStat<MeterStatistics, MeterStatisticsUpdated> meterStatSupp;
    private NotificationSupplierForItemStat<GroupStatistics, GroupStatisticsUpdated> groupStatSupp;
    private NotificationSupplierForItemStat<FlowCapableNodeConnectorQueueStatisticsData, QueueStatisticsUpdate> queueStatSupp;

    /**
     * Provider constructor set all needed final parameters
     * @param nps - notifProviderService
     * @param db - dataBroker
     * @param flowSupp - Flow Support Flag
     * @param meterSupp - Meter Support Flag
     * @param groupSupp - Group Support Flag
     * @param connectorStatSupp - Connector Stat Support Flag
     * @param flowStatSupp - Flow Stat Support Flag
     * @param flowTableStatSupp - Flow Table Stat Support Flag
     * @param meterStatSupp - Meter Stat Support Flag
     * @param groupStatSupp - Group Stat Support Flag
     * @param queueStatSupp - Queue Stat Support Flag
     */
    public NotificationProvider(final NotificationProviderService nps, final DataBroker db,
                                boolean flowSupp, boolean meterSupp, boolean groupSupp,
                                boolean connectorStatSupp, boolean flowStatSupp, boolean flowTableStatSupp,
                                boolean meterStatSupp, boolean groupStatSupp, boolean queueStatSupp) {
        this.nps = Preconditions.checkNotNull(nps);
        this.db = Preconditions.checkNotNull(db);
        this.config = initializeNotificationProviderConfig(flowSupp, meterSupp, groupSupp, connectorStatSupp, flowStatSupp,
                flowTableStatSupp, meterStatSupp, groupStatSupp, queueStatSupp);
    }

    /**
     * Method to initialize NotificationProviderConfig
     */
    private NotificationProviderConfig initializeNotificationProviderConfig(boolean flowSupp, boolean meterSupp, boolean groupSupp,
                                                      boolean connectorStatSupp, boolean flowStatSupp, boolean flowTableStatSupp,
                                                      boolean meterStatSupp, boolean groupStatSupp, boolean queueStatSupp){
        NotificationProviderConfig.NotificationProviderConfigBuilder  notif =
                new NotificationProviderConfig.NotificationProviderConfigBuilder();
        notif.setFlowSupport(flowSupp);
        notif.setMeterSupport(meterSupp);
        notif.setGroupSupport(groupSupp);
        notif.setNodeConnectorStatSupport(connectorStatSupp);
        notif.setFlowStatSupport(flowStatSupp);
        notif.setFlowTableStatSupport(flowTableStatSupp);
        notif.setMeterStatSupport(meterStatSupp);
        notif.setGroupStatSupport(groupStatSupp);
        notif.setQueueStatSupport(queueStatSupp);
        return notif.build();
    }

    public void start() {
        nodeSupp = new NodeNotificationSupplierImpl(nps, db);
        connectorSupp = new NodeConnectorNotificationSupplierImpl(nps, db);
        flowSupp = config.isFlowSupport() ? new FlowNotificationSupplierImpl(nps, db) : null;
        meterSupp = config.isMeterSupport() ? new MeterNotificationSupplierImpl(nps, db) : null;
        groupSupp = config.isGroupSupport() ? new GroupNotificationSupplierImpl(nps, db) : null;
        connectorStatSupp = config.isNodeConnectorStatSupport() ? new NodeConnectorStatNotificationSupplierImpl(nps, db) : null;
        flowStatSupp = config.isFlowStatSupport() ? new FlowStatNotificationSupplierImpl(nps, db) : null;
        flowTableStatSupp = config.isFlowTableStatSupport() ? new FlowTableStatNotificationSupplierImpl(nps, db) : null;
        meterStatSupp = config.isMeterStatSupport() ? new MeterStatNotificationSupplierImpl(nps, db) : null;
        groupStatSupp = config.isGroupStatSupport() ? new GroupStatNotificationSupplierImpl(nps, db) : null;
        queueStatSupp = config.isQueueStatSupport() ? new QueueStatNotificationSupplierImpl(nps, db) : null;

        supplierList = new ArrayList<>(Arrays.asList(nodeSupp, connectorSupp, flowSupp, meterSupp, groupSupp,
                connectorStatSupp, flowStatSupp, flowTableStatSupp, meterStatSupp, groupStatSupp, queueStatSupp));
    }

    @Override
    public void close() throws Exception {
        for (NotificationSupplierDefinition<?> supplier : supplierList) {
            if (supplier != null) {
                supplier.close();
                supplier = null;
            }
        }
    }

    @VisibleForTesting
    List<NotificationSupplierDefinition<?>> getSupplierList() {
        return supplierList;
    }
}

