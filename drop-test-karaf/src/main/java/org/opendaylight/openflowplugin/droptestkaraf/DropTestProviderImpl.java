/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import static java.util.Objects.requireNonNull;

import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.openflowplugin.testcommon.DropTestDsProvider;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.4.2015.
 */
public class DropTestProviderImpl implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DropTestProviderImpl.class);

    private static DropTestDsProvider dropDsProvider = new DropTestDsProvider();

    private static DropTestRpcProvider dropRpcProvider = new DropTestRpcProvider();

    public DropTestProviderImpl(final DataBroker dataBroker,
                                final NotificationService notificationService,
                                final SalFlowService salFlowService) {
        requireNonNull(dataBroker, "Data broker can't be empty");
        requireNonNull(notificationService, "NotificationProviderService can't be empty");
        requireNonNull(salFlowService, "SalFlowService can't be empty");

        LOG.debug("Activator DropAllPack INIT");

        dropDsProvider.setDataService(dataBroker);
        dropDsProvider.setNotificationService(notificationService);

        dropRpcProvider.setNotificationService(notificationService);
        dropRpcProvider.setFlowService(salFlowService);

        LOG.debug("Activator DropAllPack END");
    }

    public static DropTestDsProvider getDropDsProvider() {
        return dropDsProvider;
    }

    public static DropTestRpcProvider getDropRpcProvider() {
        return dropRpcProvider;
    }

    @Override
    public void close() {
        dropDsProvider.close();
        dropRpcProvider.close();
    }
}
