/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainStateListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.LifecycleService;
import org.opendaylight.openflowplugin.api.openflow.rpc.RpcContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainImpl implements ContextChain {

    private static final Logger LOG = LoggerFactory.getLogger(ContextChainImpl.class);

    private Set<OFPContext> contexts = new ConcurrentSet<>();
    private StatisticsContext statisticsContext;
    private DeviceContext deviceContext;
    private RpcContext rpcContext;
    private LifecycleService lifecycleService;
    private DeviceInfo deviceInfo;
    private ConnectionContext primaryConnection;
    private Set<ConnectionContext> auxiliaryConnections = new ConcurrentSet<>();

    private volatile ContextChainState contextChainState;

    private AtomicBoolean masterStateOnDevice;
    private AtomicBoolean initialGathering;
    private AtomicBoolean initialSubmitting;
    private AtomicBoolean registryFilling;

    ContextChainImpl(final ConnectionContext connectionContext) {
        this.primaryConnection = connectionContext;
        this.contextChainState = ContextChainState.UNDEFINED;
        this.masterStateOnDevice = new AtomicBoolean(false);
        this.initialGathering = new AtomicBoolean(false);
        this.initialSubmitting = new AtomicBoolean(false);
        this.registryFilling = new AtomicBoolean(false);
        this.deviceInfo = connectionContext.getDeviceInfo();
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
    public ListenableFuture<Void> stopChain() {
        //TODO: stopClusterServices change parameter
        final List<ListenableFuture<Void>> futureList = new ArrayList<>();
        futureList.add(statisticsContext.stopClusterServices());
        futureList.add(rpcContext.stopClusterServices());
        futureList.add(deviceContext.stopClusterServices());
        this.unMasterMe();
        return Futures.transform(Futures.successfulAsList(futureList), new Function<List<Void>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable List<Void> input) {
                LOG.info("Closed clustering MASTER services for node {}", deviceContext.getDeviceInfo().getLOGValue());
                return null;
            }
        });
    }

    private void unMasterMe() {
        this.registryFilling.set(false);
        this.initialSubmitting.set(false);
        this.initialGathering.set(false);
        this.masterStateOnDevice.set(false);
    }

    @Override
    public void close() {
        this.auxiliaryConnections.forEach(connectionContext -> connectionContext.closeConnection(false));
        if (this.primaryConnection.getConnectionState() != ConnectionContext.CONNECTION_STATE.RIP) {
            this.primaryConnection.closeConnection(true);
        }
        lifecycleService.close();
        deviceContext.close();
        rpcContext.close();
        statisticsContext.close();
    }

    @Override
    public void makeContextChainStateSlave() {
        this.unMasterMe();
        changeState(ContextChainState.WORKING_SLAVE);
    }

    @Override
    public ListenableFuture<Void> connectionDropped() {
        if (this.contextChainState == ContextChainState.WORKING_MASTER) {
            return this.stopChain();
        }
        this.unMasterMe();
        return Futures.immediateFuture(null);
    }

    @Override
    public void registerServices(final ClusterSingletonServiceProvider clusterSingletonServiceProvider) {
        this.lifecycleService.registerService(
                clusterSingletonServiceProvider,
                this.deviceContext);
    }

    @Override
    public void makeDeviceSlave() {
        this.unMasterMe();
        this.lifecycleService.makeDeviceSlave(this.deviceContext);
    }

    @Override
    public boolean isMastered(@Nonnull ContextChainMastershipState mastershipState) {
        switch (mastershipState) {
            case INITIAL_SUBMIT:
                LOG.debug("Device {}, initial submit OK.", deviceInfo.getLOGValue());
                this.initialSubmitting.set(true);
                break;
            case MASTER_ON_DEVICE:
                LOG.debug("Device {}, master state OK.", deviceInfo.getLOGValue());
                this.masterStateOnDevice.set(true);
                break;
            case INITIAL_GATHERING:
                LOG.debug("Device {}, initial gathering OK.", deviceInfo.getLOGValue());
                this.initialGathering.set(true);
                break;
            //Flow registry fill is not mandatory to work as a master
            case INITIAL_FLOW_REGISTRY_FILL:
                LOG.debug("Device {}, initial registry filling OK.", deviceInfo.getLOGValue());
                this.registryFilling.set(true);
            case CHECK:
            default:
        }
        final boolean result =
                this.initialGathering.get() &&
                this.masterStateOnDevice.get() &&
                this.initialSubmitting.get();

        if (result && mastershipState != ContextChainMastershipState.CHECK) {
            LOG.info("Device {} is able to work as master{}",
                    deviceInfo.getLOGValue(),
                    this.registryFilling.get() ? " WITHOUT flow registry !!!" : ".");
            changeState(ContextChainState.WORKING_MASTER);
        }
        return result;
    }

    @Override
    public boolean hasState() {
        return contextChainState == ContextChainState.WORKING_MASTER
                || contextChainState == ContextChainState.WORKING_SLAVE;
    }

    @Override
    public boolean addAuxiliaryConnection(@Nonnull ConnectionContext connectionContext) {
        if ((connectionContext.getFeatures().getAuxiliaryId() != 0) &&
                (this.primaryConnection.getConnectionState() != ConnectionContext.CONNECTION_STATE.RIP)) {
            this.auxiliaryConnections.add(connectionContext);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean auxiliaryConnectionDropped(@Nonnull ConnectionContext connectionContext) {
        if (this.auxiliaryConnections.isEmpty()) {
            return false;
        }
        if (!this.auxiliaryConnections.contains(connectionContext)) {
            return false;
        }
        this.auxiliaryConnections.remove(connectionContext);
        return true;
    }

    private void changeState(final ContextChainState contextChainState) {
        boolean propagate = this.contextChainState == ContextChainState.UNDEFINED;
        this.contextChainState = contextChainState;

        if (propagate) {
            contexts.stream()
                    .filter(ContextChainStateListener.class::isInstance)
                    .map(ContextChainStateListener.class::cast)
                    .forEach(listener -> listener.onStateAcquired(contextChainState));
        }
    }

    @Override
    public boolean connectionIsHealthy() {
        return ConnectionContext.CONNECTION_STATE.WORKING.equals(primaryConnection.getConnectionState());
    }
}
