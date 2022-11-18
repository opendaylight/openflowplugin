/*
 * Copyright (c) 2014, 2015 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener.Component;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkOverutilized;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkUtilizationNormal;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowpluginTestTopologyNotification {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestTopologyNotification.class);

    private final NotificationService notificationService;

    public OpenflowpluginTestTopologyNotification(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void init() {
        // For switch events
        notificationService.registerCompositeListener(new CompositeListener(Set.of(
            new Component<>(LinkDiscovered.class, this::onNotification),
            new Component<>(LinkOverutilized.class, this::onNotification),
            new Component<>(LinkRemoved.class, this::onNotification),
            new Component<>(LinkUtilizationNormal.class, this::onNotification))));
    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    private void onNotification(Notification<?> notification) {
        LOG.debug("-------------------------------------------");
        LOG.debug("{} notification ........", notification.getClass().getSimpleName());
        LOG.debug("-------------------------------------------");
    }
}
