/**
 * Copyright (c) 2014, 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestNodeConnectorNotification {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestNodeConnectorNotification.class);

    private DataBroker dataBroker;
    private ProviderContext pc;
    private final BundleContext ctx;
    private final PortEventListener portEventListener = new PortEventListener();
    private static NotificationService notificationService;
    private Registration listenerReg;

    public OpenflowpluginTestNodeConnectorNotification(BundleContext ctx) {
        this.ctx = ctx;
    }

    public void onSessionInitiated(ProviderContext session) {
        pc = session;
        notificationService = session.getSALService(NotificationService.class);
        // For switch events
        listenerReg = notificationService.registerNotificationListener(portEventListener);
        dataBroker = session.getSALService(DataBroker.class);
    }

    final class PortEventListener implements OpendaylightInventoryListener {

        List<NodeUpdated> nodeUpdated = new ArrayList<>();
        List<NodeRemoved> nodeRemoved = new ArrayList<>();
        List<NodeConnectorUpdated> nodeConnectorUpdated = new ArrayList<>();
        List<NodeConnectorRemoved> nodeConnectorRemoved = new ArrayList<>();

        @Override
        public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
            LOG.debug("NodeConnectorRemoved Notification ...................");
            LOG.debug("NodeConnectorRef " + notification.getNodeConnectorRef());
            LOG.debug("----------------------------------------------------------------------");
            nodeConnectorRemoved.add(notification);
        }

        @Override
        public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
            LOG.debug("NodeConnectorUpdated Notification...................");
            LOG.debug("NodeConnectorRef " + notification.getNodeConnectorRef());
            LOG.debug("----------------------------------------------------------------------");
            nodeConnectorUpdated.add(notification);
        }

        @Override
        public void onNodeRemoved(NodeRemoved notification) {
            LOG.debug("NodeRemoved Notification ...................");
            LOG.debug("NodeRef " + notification.getNodeRef());
            LOG.debug("----------------------------------------------------------------------");
            nodeRemoved.add(notification);
        }

        @Override
        public void onNodeUpdated(NodeUpdated notification) {
            LOG.debug("NodeUpdated Notification ...................");
            LOG.debug("NodeRef " + notification.getNodeRef());
            LOG.debug("----------------------------------------------------------------------");
            nodeUpdated.add(notification);
        }
    }
}
