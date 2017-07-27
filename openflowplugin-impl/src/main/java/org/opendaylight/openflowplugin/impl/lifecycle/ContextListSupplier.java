/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceManager;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcManager;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsManager;

public class ContextListSupplier implements Supplier<List<OFPContext>> {
    private final DeviceManager deviceManager;
    private final RpcManager rpcManager;
    private final StatisticsManager statisticsManager;
    private final ConnectionContext connectionContext;
    private final ContextChainMastershipWatcher contextChainMastershipWatcher;
    private final ExecutorService executorService;

    ContextListSupplier(@Nonnull final DeviceManager deviceManager,
                        @Nonnull final RpcManager rpcManager,
                        @Nonnull final StatisticsManager statisticsManager,
                        @Nonnull final ConnectionContext connectionContext,
                        @Nonnull final ContextChainMastershipWatcher contextChainMastershipWatcher,
                        @Nonnull final ExecutorService executorService) {
        this.deviceManager = deviceManager;
        this.rpcManager = rpcManager;
        this.statisticsManager = statisticsManager;
        this.connectionContext = connectionContext;
        this.contextChainMastershipWatcher = contextChainMastershipWatcher;
        this.executorService = executorService;
    }

    @Override
    public List<OFPContext> get() {
        connectionContext.getConnectionAdapter().setPacketInFiltering(true);
        final List<OFPContext> contexts = new ArrayList<>();
        final DeviceContext deviceContext = deviceManager.createContext(connectionContext);
        contexts.add(deviceContext);
        contexts.add(rpcManager.createContext(deviceContext));
        contexts.add(statisticsManager.createContext(deviceContext));

        contexts.forEach(context -> {
            context.registerMastershipWatcher(contextChainMastershipWatcher);
            context.addListener(new Service.Listener() {
                @Override
                public void running() {
                    super.running();
                }

                @Override
                public void terminated(final Service.State from) {
                    super.terminated(from);
                }

                @Override
                public void failed(final Service.State from, final Throwable failure) {
                    super.failed(from, failure);
                }
            }, executorService);
        });

        deviceContext.onPublished();
        return ImmutableList.copyOf(contexts);
    }
}
