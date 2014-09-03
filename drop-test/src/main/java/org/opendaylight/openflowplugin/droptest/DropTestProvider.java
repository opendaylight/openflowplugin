/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.openflowplugin.test.common.DropTestCommiter;
import org.opendaylight.openflowplugin.test.common.DropTestStats;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestProvider implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private DataBroker dataService;
    private NotificationProviderService notificationService;
    private Registration listenerRegistration;
    private final DropTestCommiter commiter = new DropTestCommiter();

    public DropTestStats getStats() {
        return this.commiter.getStats();
    }

    public void clearStats() {
        this.commiter.clearStats();
    }

    public DataBroker getDataService() {
        return this.dataService;
    }

    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }


    public NotificationProviderService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(final NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    public void start() {
        commiter.setDataService(dataService);
        listenerRegistration = notificationService.registerNotificationListener(commiter);
        LOG.debug("DropTestProvider Started.");
    }

    public void close() {
        try {
            LOG.debug("DropTestProvider stopped.");
            if (this.listenerRegistration != null) {
                this.listenerRegistration.close();
            }
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
