/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.topology.lldp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.topology.lldp.discovery.config.rev160511.TopologyLldpDiscoveryConfig;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LLDPActivator implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(LLDPActivator.class);

    private static String lldpSecureKey;

    private final ListenerRegistration<NotificationListener> lldpNotificationRegistration;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Inject
    public LLDPActivator(@Reference NotificationService notificationService,
                         LLDPDiscoveryListener lldpDiscoveryListener,
                         TopologyLldpDiscoveryConfig topologyLldpDiscoveryConfig) {
        lldpSecureKey = topologyLldpDiscoveryConfig.getLldpSecureKey();

        LOG.info("Starting LLDPActivator with lldpSecureKey: {}", lldpSecureKey);

        lldpNotificationRegistration = notificationService.registerNotificationListener(lldpDiscoveryListener);

        LOG.info("LLDPDiscoveryListener started.");
    }

    @Override
    @PreDestroy
    public void close() {
        lldpNotificationRegistration.close();

        LOG.info("LLDPDiscoveryListener stopped.");
    }

    public static String getLldpSecureKey() {
        return lldpSecureKey;
    }
}
