/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.old.notification.supplier;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.ConnectorNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.NodeNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.FlowNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.GroupNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.MeterNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.impl.item.PortNotificationSupplierImpl;
import org.opendaylight.openflowplugin.applications.old.notification.supplier.tools.OldNotifProviderConfig;

/**
 *
 */
public class OldNotifProviderImpl implements OldNotifProvider {

    private final DataBroker dataBroker;
    private final OldNotifProviderConfig config;
    private final NotificationProviderService notifProviderService;

    private OldNotifSupplierDefinition nodeSupp, connectorSupp;
    private OldNotifSupplierDefinition flowSupp, meterSupp, groupSupp, portSupp;
    private OldNotifSupplierDefinition connectorStatSupp, flowStatSupp, flowTableStatSupp, meterStatSupp,
            groupStatSupp, queueStatSupp;

    /**
     * @param config
     * @param notifProviderService
     * @param dataBroker
     */
    public OldNotifProviderImpl(final OldNotifProviderConfig config,
            final NotificationProviderService notifProviderService, final DataBroker dataBroker) {
        this.config = Preconditions.checkNotNull(config);
        this.dataBroker = Preconditions.checkNotNull(dataBroker);
        this.notifProviderService = Preconditions.checkNotNull(notifProviderService);
    }

    @Override
    public void start() {
        nodeSupp = new NodeNotificationSupplierImpl(notifProviderService, dataBroker);
        connectorSupp = new ConnectorNotificationSupplierImpl(notifProviderService, dataBroker);
        if (config.isFlowSupport()) {
            flowSupp = new FlowNotificationSupplierImpl(notifProviderService, dataBroker);
        }
        if (config.isMeterSupport()) {
            meterSupp = new MeterNotificationSupplierImpl(notifProviderService, dataBroker);
        }
        if (config.isGroupSupport()) {
            groupSupp = new GroupNotificationSupplierImpl(notifProviderService, dataBroker);
        }
        if (config.isPortSupport()) {
            portSupp = new PortNotificationSupplierImpl(notifProviderService, dataBroker);
        }
        if (config.isFlowStatSupport()) {

        }
    }

    @Override
    public void close() throws Exception {
        if (nodeSupp != null) {
            nodeSupp.close();
            nodeSupp = null;
        }
        if (connectorSupp != null) {
            connectorSupp.close();
            connectorSupp = null;
        }
    }
}

