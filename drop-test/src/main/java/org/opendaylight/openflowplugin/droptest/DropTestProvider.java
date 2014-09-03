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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * provides activation and deactivation of drop responder service - responds on packetIn
 */
public class DropTestProvider implements AutoCloseable {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private DataBroker dataService;
    private NotificationProviderService notificationService;
    private final DropTestCommiter commiter = new DropTestCommiter();

    /**
     * @return message counts
     */
    public DropTestStats getStats() {
        return commiter.getStats();
    }

    /**
     * reset message counts
     */
    public void clearStats() {
        commiter.clearStats();
    }

    /**
     * @param dataService value for setter
     */
    public void setDataService(final DataBroker dataService) {
        this.dataService = dataService;
    }


    /**
     * @param notificationService value for setter
     */
    public void setNotificationService(final NotificationProviderService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * activates the drop responder
     */
    public void start() {
        commiter.setDataService(dataService);
        commiter.setNotificationService(notificationService);
        commiter.start();
        LOG.debug("DropTestProvider Started.");
    }

    @Override
    public void close() {
        LOG.debug("DropTestProvider stopped.");
        if (commiter != null) {
            commiter.close();
        }
    }
}
