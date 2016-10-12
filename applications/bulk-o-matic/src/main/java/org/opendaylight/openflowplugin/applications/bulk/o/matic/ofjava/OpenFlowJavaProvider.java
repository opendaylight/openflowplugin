/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.bulk.o.matic.ofjava;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionManager;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionStatus;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceDisconnectedHandler;
import org.opendaylight.openflowplugin.impl.connection.ConnectionManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.ThreadPoolLoggingExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.bulk.flow.direct.service.rev161011.SalBulkFlowDirectService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OpenFlowJavaProvider implements BindingAwareProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenFlowJavaProvider.class);
    private final Set<SwitchConnectionProvider> switchConnectionProviders;
    private final DataBroker dataBroker;

    public OpenFlowJavaProvider(final Set<SwitchConnectionProvider> switchConnectionProviders,
                                final DataBroker dataBroker) {
        this.switchConnectionProviders = switchConnectionProviders;
        this.dataBroker = dataBroker;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        final ThreadPoolExecutor executor =  new ThreadPoolLoggingExecutor(
                1, 32000, 60, TimeUnit.SECONDS, new SynchronousQueue<>(), "ofppool");
        final ConnectionManager connectionManager = new ConnectionManagerImpl(2000, executor);

        connectionManager.setDeviceConnectedHandler(connectionContext -> {
            final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = connectionContext
                    .getDeviceInfo()
                    .getNodeInstanceIdentifier();

            final DeviceDisconnectedHandler disconnectedHandler = RpcRegistrationContext.of(
                    nodeInstanceIdentifier, session,
                    Stream.of(ImmutablePair.of(
                            SalBulkFlowDirectService.class,
                            new SalBulkFlowDirectServiceImpl(dataBroker, connectionContext.getOutboundQueueProvider()))));

            connectionContext.setDeviceDisconnectedHandler(disconnectedHandler);
            return ConnectionStatus.MAY_CONTINUE;
        });

        switchConnectionProviders.forEach(switchConnectionProvider -> {
            switchConnectionProvider.setSwitchConnectionHandler(connectionManager);
            Futures.transform(switchConnectionProvider.startup(), (Function<Boolean, Void>) input -> {
                LOG.info("Switch connection provider {} ({}).", switchConnectionProvider.hashCode(), input);
                return null;
            });
        });
    }
}
