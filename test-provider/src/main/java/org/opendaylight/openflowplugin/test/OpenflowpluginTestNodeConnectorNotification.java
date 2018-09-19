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
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestNodeConnectorNotification {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestNodeConnectorNotification.class);

    private final PortEventListener portEventListener = new PortEventListener();
    private final NotificationService notificationService;

    public OpenflowpluginTestNodeConnectorNotification(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void init() {
        // For switch events
        notificationService.registerNotificationListener(portEventListener);
    }

    private static final class PortEventListener implements OpendaylightInventoryListener {

        List<NodeUpdated> nodeUpdated = new ArrayList<>();
        List<NodeRemoved> nodeRemoved = new ArrayList<>();
        List<NodeConnectorUpdated> nodeConnectorUpdated = new ArrayList<>();
        List<NodeConnectorRemoved> nodeConnectorRemoved = new ArrayList<>();

        @Override
        public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
            LOG.debug("NodeConnectorRemoved Notification");
            LOG.debug("NodeConnectorRef {}", notification.getNodeConnectorRef());
            nodeConnectorRemoved.add(notification);
        }

        @Override
        public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
            LOG.debug("NodeConnectorUpdated Notification");
            LOG.debug("NodeConnectorRef {}", notification.getNodeConnectorRef());
            nodeConnectorUpdated.add(notification);
        }

        @Override
        public void onNodeRemoved(NodeRemoved notification) {
            LOG.debug("NodeRemoved Notification");
            LOG.debug("NodeRef {}", notification.getNodeRef());
            nodeRemoved.add(notification);
        }

        @Override
        public void onNodeUpdated(NodeUpdated notification) {
            LOG.debug("NodeUpdated Notification");
            LOG.debug("NodeRef {}", notification.getNodeRef());
            nodeUpdated.add(notification);
        }
    }
}
