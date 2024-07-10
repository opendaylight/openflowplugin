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
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.NotificationService.CompositeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkOverutilized;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkUtilizationNormal;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = { })
public final class OpenflowpluginTestTopologyNotification implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OpenflowpluginTestTopologyNotification.class);

    private final Registration reg;

    @Activate
    @Inject
    public OpenflowpluginTestTopologyNotification(@Reference final NotificationService notificationService) {
        // For switch events
        reg = notificationService.registerCompositeListener(new CompositeListener(Set.of(
            new CompositeListener.Component<>(LinkDiscovered.class,
                OpenflowpluginTestTopologyNotification::onNotification),
            new CompositeListener.Component<>(LinkOverutilized.class,
                OpenflowpluginTestTopologyNotification::onNotification),
            new CompositeListener.Component<>(LinkRemoved.class,
                OpenflowpluginTestTopologyNotification::onNotification),
            new CompositeListener.Component<>(LinkUtilizationNormal.class,
                OpenflowpluginTestTopologyNotification::onNotification))));
    }

    @Override
    @PreDestroy
    @Deactivate
    public void close() {
        reg.close();
    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    private static void onNotification(final Notification<?> notification) {
        LOG.debug("-------------------------------------------");
        LOG.debug("{} notification ........", notification.getClass().getSimpleName());
        LOG.debug("-------------------------------------------");
    }
}
