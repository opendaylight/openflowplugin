/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides activation and deactivation of drop responder service - responds on packetIn.
 */
public class DropTestRpcProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestRpcProvider.class);

    private final DropTestRpcSender commiter;

    private boolean active = false;

    public DropTestRpcProvider(final NotificationService notificationService, final AddFlow addFlow) {
        commiter = new DropTestRpcSender(notificationService, addFlow);
    }

    public void start() {
        commiter.start();
        active = true;
        LOG.debug("DropTestProvider started");
    }

    public void stop() {
        commiter.stop();
        active = true;
        LOG.debug("DropTestProvider stopped");
    }


    /**
     * Returns the message counts.
     */
    public DropTestStats getStats() {
        if (commiter != null) {
            return commiter.getStats();
        } else {
            return new DropTestStats("Not initialized yet.");
        }
    }

    /**
     * Reset message counts.
     */
    public void clearStats() {
        if (commiter != null) {
            commiter.clearStats();
        }
    }

    @Override
    public void close() {
        commiter.close();
        LOG.debug("DropTestProvider terminated");
    }

    /**
     * Returns the active state.
     */
    public boolean isActive() {
        return active;
    }
}
