/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.test.common.DropTestRpcSender;
import org.opendaylight.openflowplugin.test.common.DropTestStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestRpcProvider implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private SalFlowService flowService;
    private NotificationProviderService notificationService;
    private DropTestRpcSender commiter = new DropTestRpcSender();
    private Registration listenerRegistration;

    public SalFlowService getFlowService() {
        return this.flowService;
    }

    public void setFlowService(final SalFlowService flowService) {
        this.flowService = flowService;
    }

    public NotificationProviderService getNotificationService() {
        return this.notificationService;
    }

    public void setNotificationService(final NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        commiter.setFlowService(flowService);
        listenerRegistration = this.getNotificationService().registerNotificationListener(this.commiter);
        LOG.debug("DropTestProvider Started.");
    }

    public DropTestStats getStats() {
        if(this.commiter != null) {
            return this.commiter.getStats();
        } else {
            return new DropTestStats("Not initialized yet.");
        }
    }

    public void clearStats() {
        if(this.commiter != null) {
            this.commiter.clearStats();
        }
    }

    @Override
    public void close() {
        try {
            LOG.debug("DropTestProvider stopped.");
            if (this.listenerRegistration != null) {
                this.listenerRegistration.close();
            }
        } catch (Exception _e) {
            throw new Error(_e);
        }
    }
}
