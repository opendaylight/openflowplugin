/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.ContextChainState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainImpl implements ContextChain {

    private static final Logger LOG = LoggerFactory.getLogger(ContextChainImpl.class);

    private Set<OFPContext> contexts = new HashSet<>();
    private StatisticsContext statisticsContext;
    private DeviceContext deviceContext;
    private RpcContext rpcContext;
    private volatile ContextChainState contextChainState;
    private volatile ContextChainState lastContextChainState;
    private LifecycleService lifecycleService;
    private ConnectionContext primaryConnectionContext;

    public ContextChainImpl(final ConnectionContext connectionContext) {
        this.contextChainState = ContextChainState.INITIALIZED;
        this.lastContextChainState = ContextChainState.INITIALIZED;
        this.primaryConnectionContext = connectionContext;
    }

    @Override
    public <T extends OFPContext> void addContext(final T context) {
        if (context instanceof StatisticsContext) {
            this.statisticsContext = (StatisticsContext) context;
        } else {
            if (context instanceof DeviceContext) {
                this.deviceContext = (DeviceContext) context;
            } else {
                if (context instanceof RpcContext) {
                    this.rpcContext = (RpcContext) context;
                }
            }
        }
        contexts.add(context);
    }

    @Override
    public void addLifecycleService(final LifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @Override
    public ListenableFuture<Void> stopChain(boolean connectionDropped) {
        //TODO: stopClusterServices change parameter
        final List<ListenableFuture<Void>> futureList = new ArrayList<>();
        futureList.add(statisticsContext.stopClusterServices());
        futureList.add(rpcContext.stopClusterServices());
        futureList.add(deviceContext.stopClusterServices(connectionDropped));

        return Futures.transform(Futures.successfulAsList(futureList), new Function<List<Void>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable List<Void> input) {
                LOG.info("Closed clustering MASTER services for node {}", deviceContext.getDeviceInfo().getLOGValue());
                return null;
            }
        });
    }

    @Override
    public ListenableFuture<Void> startChain() {
        if (ContextChainState.INITIALIZED.equals(this.contextChainState)) {
            return Futures.transform(this.statisticsContext.initialGatherDynamicData(), new Function<Boolean, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable Boolean gatheringSuccessful) {
                    contextChainState = ContextChainState.WORKINGMASTER;
                    return null;
                }
            });
        } else {
            this.contextChainState = ContextChainState.WORKINGMASTER;
        }
        return Futures.immediateFuture(null);
    }

    @Override
    public void close() {
        deviceContext.close();
        rpcContext.close();
        statisticsContext.close();
        lifecycleService.close();
    }

    @Override
    public void changePrimaryConnection(final ConnectionContext connectionContext) {
        this.primaryConnectionContext = connectionContext;
        this.contextChainState = ContextChainState.INITIALIZED;
        for (OFPContext context : contexts) {
            context.replaceConnection(connectionContext);
        }
    }

    @Override
    public ContextChainState getContextChainState() {
        return contextChainState;
    }

    @Override
    public ListenableFuture<Void> connectionDropped() {
        this.lastContextChainState = this.contextChainState;
        this.contextChainState = ContextChainState.SLEEPING;
        if (this.lastContextChainState.equals(ContextChainState.WORKINGMASTER)) {
            return this.stopChain(true);
        }
        return Futures.immediateFuture(null);
    }

    @Override
    public void sleepTheChainAndDropConnection() {
        this.contextChainState = ContextChainState.SLEEPING;
        this.primaryConnectionContext.closeConnection(true);
    }

    @Override
    public void registerServices(@NonNull final ClusterSingletonServiceProvider clusterSingletonServiceProvider) {
        this.contextChainState = ContextChainState.WORKINGSLAVE;
        this.lifecycleService.registerService(
                clusterSingletonServiceProvider,
                this.deviceContext,
                this.deviceContext.getServiceIdentifier(),
                this.deviceContext.getDeviceInfo());
    }

    @Override
    public void makeDeviceSlave() {
        this.lifecycleService.makeDeviceSlave(this.deviceContext);
    }

    @Override
    public void closePrimaryConnection() {
        this.primaryConnectionContext.closeConnection(true);
    }

    @Override
    public ConnectionContext getPrimaryConnectionContext() {
        return this.primaryConnectionContext;
    }

    @Override
    public boolean lastStateWasMaster() {
        return this.lastContextChainState.equals(ContextChainState.WORKINGMASTER);
    }

}
