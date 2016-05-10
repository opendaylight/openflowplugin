/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.applications.topology.lldp.utils.LLDPDiscoveryUtils;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPDiscoveryProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPDiscoveryProvider.class);
    private NotificationProviderService notificationService;

    private ListenerRegistration<NotificationListener> listenerRegistration;
    private LLDPLinkAger lldpLinkAger;

    public void setNotificationService(final NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        lldpLinkAger = new LLDPLinkAger(LLDPDiscoveryUtils.LLDP_INTERVAL, LLDPDiscoveryUtils.LLDP_EXPIRATION_TIME);
        lldpLinkAger.setNotificationService(notificationService);

        LLDPDiscoveryListener committer = new LLDPDiscoveryListener(notificationService);
        committer.setLldpLinkAger(lldpLinkAger);

        ListenerRegistration<NotificationListener> registerNotificationListener =
                notificationService.registerNotificationListener(committer);
        this.listenerRegistration = registerNotificationListener;
        LOG.info("LLDPDiscoveryListener Started.");
    }

    @Override
    public void close() {
        try {
            LOG.info("LLDPDiscoveryListener stopped.");
            if (this.listenerRegistration!=null) {
                this.listenerRegistration.close();
            }
            lldpLinkAger.close();
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
