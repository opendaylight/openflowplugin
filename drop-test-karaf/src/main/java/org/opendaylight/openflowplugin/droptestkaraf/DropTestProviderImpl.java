/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptestkaraf;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.openflowplugin.testcommon.DropTestDsProvider;
import org.opendaylight.openflowplugin.testcommon.DropTestRpcProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlow;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 26.4.2015.
 */
@Singleton
@Component(service = DropTestProviderImpl.class, immediate = true)
public class DropTestProviderImpl implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DropTestProviderImpl.class);

    private final DropTestDsProvider dropDsProvider;
    private final DropTestRpcProvider dropRpcProvider;

    @Inject
    @Activate
    public DropTestProviderImpl(@Reference final DataBroker dataBroker,
            @Reference final NotificationService notificationService, @Reference final RpcConsumerRegistry rpcService) {
        LOG.debug("Activator DropAllPack INIT");

        dropDsProvider = new DropTestDsProvider(dataBroker, notificationService);
        dropRpcProvider = new DropTestRpcProvider(notificationService, rpcService.getRpc(AddFlow.class));
        LOG.debug("Activator DropAllPack END");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        dropDsProvider.close();
        dropRpcProvider.close();
    }

    public DropTestDsProvider getDropDsProvider() {
        return dropDsProvider;
    }

    public DropTestRpcProvider getDropRpcProvider() {
        return dropRpcProvider;
    }
}
