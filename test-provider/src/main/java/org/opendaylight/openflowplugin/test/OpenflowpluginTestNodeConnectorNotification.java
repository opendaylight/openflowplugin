/*
 * Copyright (c) 2014, 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener;
//import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener.Component;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class OpenflowpluginTestNodeConnectorNotification implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestNodeConnectorNotification.class);

    private final PortEventListener portEventListener = new PortEventListener();
    private final Registration reg;

    @Inject
    @Activate
    public OpenflowpluginTestNodeConnectorNotification(@Reference final NotificationService notificationService) {
        // For switch events
        reg = notificationService.registerCompositeListener(portEventListener.toListener());
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        reg.close();
    }

    private static final class PortEventListener {
        List<NodeUpdated> nodeUpdated = new ArrayList<>();
        List<NodeRemoved> nodeRemoved = new ArrayList<>();
        List<NodeConnectorUpdated> nodeConnectorUpdated = new ArrayList<>();
        List<NodeConnectorRemoved> nodeConnectorRemoved = new ArrayList<>();

        CompositeListener toListener() {
            return new CompositeListener(Set.of(
                new CompositeListener.Component<>(NodeConnectorRemoved.class, notification -> {
                    LOG.debug("NodeConnectorRemoved Notification");
                    LOG.debug("NodeConnectorRef {}", notification.getNodeConnectorRef());
                    nodeConnectorRemoved.add(notification);
                }),
                new CompositeListener.Component<>(NodeConnectorUpdated.class, notification -> {
                    LOG.debug("NodeConnectorUpdated Notification");
                    LOG.debug("NodeConnectorRef {}", notification.getNodeConnectorRef());
                    nodeConnectorUpdated.add(notification);
                }),
                new CompositeListener.Component<>(NodeRemoved.class, notification -> {
                    LOG.debug("NodeRemoved Notification");
                    LOG.debug("NodeRef {}", notification.getNodeRef());
                    nodeRemoved.add(notification);
                }),
                new CompositeListener.Component<>(NodeUpdated.class, notification -> {
                    LOG.debug("NodeUpdated Notification");
                    LOG.debug("NodeRef {}", notification.getNodeRef());
                    nodeUpdated.add(notification);
                })));
        }
    }
}
