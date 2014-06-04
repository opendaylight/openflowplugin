/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestRpcProvider implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private SalFlowService _flowService;
    private NotificationProviderService _notificationService;
    private DropTestRpcSender commiter;
    private Registration<NotificationListener> listenerRegistration;

    public SalFlowService getFlowService() {
        return this._flowService;
    }

    public void setFlowService(final SalFlowService flowService) {
        this._flowService = flowService;
    }

    public NotificationProviderService getNotificationService() {
        return this._notificationService;
    }

    public void setNotificationService(final NotificationProviderService notificationService) {
        this._notificationService = notificationService;
    }

    public void start() {
        this.commiter = new DropTestRpcSender(this.getFlowService());
        this.listenerRegistration = this.getNotificationService().registerNotificationListener(this.commiter);
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
