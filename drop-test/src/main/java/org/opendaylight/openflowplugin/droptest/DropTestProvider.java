/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.data.DataProviderService;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("all")
public class DropTestProvider implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private DataProviderService _dataService;
    private NotificationProviderService _notificationService;
    private Registration<NotificationListener> listenerRegistration;
    private final DropTestCommiter commiter = new DropTestCommiter(this);

    public DataProviderService getDataService() {
        return this._dataService;
    }

    public void setDataService(final DataProviderService dataService) {
        this._dataService = dataService;
    }


    public NotificationProviderService getNotificationService() {
        return this._notificationService;
    }

    public void setNotificationService(final NotificationProviderService notificationService) {
        this._notificationService = notificationService;
    }

    public void start() {
        this.listenerRegistration = this.getNotificationService().registerNotificationListener(commiter);
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
