/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPActivator implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPActivator.class);

    private static String lldpSecureKey;

    private final ListenerRegistration<NotificationListener> lldpNotificationRegistration;

    public LLDPActivator(NotificationProviderService notificationService, LLDPDiscoveryListener lldpDiscoveryListener,
            String secureKey) {
        lldpSecureKey = secureKey;

        LOG.info("Starting LLDPActivator with lldpSecureKey: {}", lldpSecureKey);

        lldpNotificationRegistration = notificationService.registerNotificationListener(lldpDiscoveryListener);

        LOG.info("LLDPDiscoveryListener started.");
    }

    @Override
    public void close() {
        lldpNotificationRegistration.close();

        LOG.info("LLDPDiscoveryListener stopped.");
    }

    public static String getLldpSecureKey() {
        return lldpSecureKey;
    }
}
