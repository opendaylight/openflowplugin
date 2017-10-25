/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.testcommon;

import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides activation and deactivation of drop responder service - responds on packetIn
 */
public class DropTestRpcProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestRpcProvider.class);

    private SalFlowService flowService;
    private NotificationService notificationService;
    private DropTestRpcSender commiter = new DropTestRpcSender();
    private boolean active = false;

    /**
     * @param flowService value for setter
     */
    public void setFlowService(final SalFlowService flowService) {
        this.flowService = flowService;
    }

    /**
     * @param notificationService value for setter
     */
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * activates drop responder
     */
    public void start() {
        commiter.setFlowService(flowService);
        commiter.setNotificationService(notificationService);
        commiter.start();
        active = true;
        LOG.debug("DropTestProvider Started.");
    }

    /**
     * @return message counts
     */
    public DropTestStats getStats() {
        if (this.commiter != null) {
            return commiter.getStats();
        } else {
            return new DropTestStats("Not initialized yet.");
        }
    }

    /**
     * reset message counts
     */
    public void clearStats() {
        if (commiter != null) {
            commiter.clearStats();
        }
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
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
}
