/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLDPActivator implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPActivator.class);

    private static String lldpSecureKey;

    private final NotificationProviderService notificationService;
    private LLDPDiscoveryProvider provider;

    public LLDPActivator(NotificationProviderService notificationService, String secureKey) {
        this.notificationService = notificationService;
        lldpSecureKey = secureKey;
    }

    public void start() {
        LOG.info("Starting LLDPActivator with lldpSecureKey: {}", lldpSecureKey);

        provider = new LLDPDiscoveryProvider();
        provider.setNotificationService(notificationService);
        provider.start();
    }

    @Override
    public void close() {
        provider.close();
    }

    public static String getLldpSecureKey() {
        return lldpSecureKey;
    }
}
