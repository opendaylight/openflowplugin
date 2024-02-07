/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides activation and deactivation of drop responder service - responds on packetIn.
 */
public class DropTestDsProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestDsProvider.class);

    private final DropTestCommiter commiter;

    private boolean active = false;

    public DropTestDsProvider(final DataBroker dataBroker, final NotificationService notificationService) {
        commiter = new DropTestCommiter(dataBroker, notificationService);
    }

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

    /**
     * Activates the drop responder.
     */
    public void start() {
        commiter.start();
        active = true;
        LOG.debug("DropTestProvider Started");
    }

    public void stop() {
        commiter.stop();
        active = false;
        LOG.debug("DropTestProvider stopped");

    }

    @Override
    public void close() {
        LOG.debug("DropTestProvider terminated");
        commiter.close();
    }

    /**
     * Returns the active state.
     */
    public boolean isActive() {
        return active;
    }
}
