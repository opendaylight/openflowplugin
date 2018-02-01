/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides activation and deactivation of drop responder service - responds on packetIn.
 */
public class DropTestDsProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestDsProvider.class);

    private DataBroker dataService;
    private NotificationService notificationService;
    private final DropTestCommiter commiter = new DropTestCommiter();
    private boolean active = false;

    /**
     * Returns the message counts.
     */
    public DropTestStats getStats() {
        return commiter.getStats();
    }

    /**
     * Reset message counts.
     */
    public void clearStats() {
        commiter.clearStats();
    }

    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Activates the drop responder.
     */
    public void start() {
        commiter.setDataService(dataService);
        commiter.setNotificationService(notificationService);
        commiter.start();
        active = true;
        LOG.debug("DropTestProvider Started.");
    }

    @Override
    public void close() {
        LOG.debug("DropTestProvider stopped.");
        if (commiter != null) {
            commiter.close();
            active = false;
        }
    }

    /**
     * Returns the active state.
     */
    public boolean isActive() {
        return active;
    }
}
