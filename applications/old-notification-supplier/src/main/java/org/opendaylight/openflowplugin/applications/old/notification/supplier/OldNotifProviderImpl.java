/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.NodeConnectorNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.NodeNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.FlowNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.GroupNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.MeterNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.tools.OldNotifProviderConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.GroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.MeterUpdated;

/**
 * Provider Implementation
 */
public class OldNotifProviderImpl implements OldNotifProvider {

    private final DataBroker db;
    private final OldNotifProviderConfig config;
    private final NotificationProviderService nps;

    /* Supplier List property help for easy close method implementation and testing */
    private List<OldNotifSupplierDefinition<?>> supplierList;
    private OldNotifSupplierForItemRoot<FlowCapableNode, NodeUpdated, NodeRemoved> nodeSupp;
    private OldNotifSupplierForItemRoot<FlowCapableNodeConnector, NodeConnectorUpdated, NodeConnectorRemoved> connectorSupp;
    private OldNotifSupplierForItem<Flow, FlowAdded, FlowUpdated, FlowRemoved> flowSupp;
    private OldNotifSupplierForItem<Meter, MeterAdded, MeterUpdated, MeterRemoved> meterSupp;
    private OldNotifSupplierForItem<Group, GroupAdded, GroupUpdated, GroupRemoved> groupSupp;

    /**
     * Provider constructor set all needed final parameters
     *
     * @param config - Configuration Object
     * @param nps - notifProviderService
     * @param db - dataBroker
     */
    public OldNotifProviderImpl(final OldNotifProviderConfig config,
            final NotificationProviderService nps, final DataBroker db) {
        this.config = Preconditions.checkNotNull(config);
        this.db = Preconditions.checkNotNull(db);
        this.nps = Preconditions.checkNotNull(nps);
    }

    @Override
    public void start() {
        nodeSupp = new NodeNotificationSupplierImpl(nps, db);
        connectorSupp = new NodeConnectorNotificationSupplierImpl(nps, db);
        flowSupp = config.isFlowSupport() ? new FlowNotificationSupplierImpl(nps, db) : null;
        meterSupp = config.isMeterSupport() ? new MeterNotificationSupplierImpl(nps, db) : null;
        groupSupp = config.isGroupSupport() ? new GroupNotificationSupplierImpl(nps, db) : null;

        supplierList = new ArrayList<>(Arrays.asList(nodeSupp, connectorSupp, flowSupp, meterSupp, groupSupp));
    }

    @Override
    public void close() throws Exception {
        for (OldNotifSupplierDefinition<?> supplier : supplierList) {
            if (supplier != null) {
                supplier.close();
                supplier = null;
            }
        }
    }

    @VisibleForTesting
    List<OldNotifSupplierDefinition<?>> getSupplierList() {
        return supplierList;
    }
}

