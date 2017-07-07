/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import static org.opendaylight.openflowplugin.api.openflow.OFPContext.CONTEXT_STATE;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.util.HashedWheelTimer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainStateListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.MastershipChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainImpl implements ContextChain {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainImpl.class);
    private static final long CLOSE_SERVICE_TIMEOUT = 10000L;
    private final AtomicBoolean masterStateOnDevice = new AtomicBoolean(false);
    private final AtomicBoolean initialGathering = new AtomicBoolean(false);
    private final AtomicBoolean initialSubmitting = new AtomicBoolean(false);
    private final AtomicBoolean registryFilling = new AtomicBoolean(false);
    private final AtomicBoolean rpcRegistration = new AtomicBoolean(false);
    private final List<DeviceRemovedHandler> deviceRemovedHandlers = new CopyOnWriteArrayList<>();
    private final List<OFPContext> contexts = new CopyOnWriteArrayList<>();
    private final List<ConnectionContext> auxiliaryConnections = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService;
    private final MastershipChangeListener mastershipChangeListener;
    private final DeviceInfo deviceInfo;
    private final ConnectionContext primaryConnection;
    private AutoCloseable registration;
    private CONTEXT_STATE state = CONTEXT_STATE.INITIALIZATION;
    private Future<?> initFuture;

    private volatile ContextChainState contextChainState = ContextChainState.UNDEFINED;

    ContextChainImpl(@Nonnull final MastershipChangeListener mastershipChangeListener,
                     @Nonnull final ConnectionContext connectionContext,
                     @Nonnull final ExecutorService executorService,
                     @Nonnull final HashedWheelTimer timer) {
        this.mastershipChangeListener = mastershipChangeListener;
        this.primaryConnection = connectionContext;
        this.deviceInfo = connectionContext.getDeviceInfo();
        this.executorService = executorService;
    }

    @Override
    public <T extends OFPContext> void addContext(@Nonnull final T context) {
        contexts.add(context);
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting clustering services for node {}", deviceInfo);

        initFuture = executorService.submit(() -> {
            try {
                contexts.forEach(context -> {
                    if (ConnectionContext.CONNECTION_STATE.WORKING.equals(primaryConnection.getConnectionState())) {
                        context.instantiateServiceInstance();
                    }
                });
                LOG.info("Started clustering services for node {}", deviceInfo);
            } catch (final Exception ex) {
                mastershipChangeListener.onNotAbleToStartMastershipMandatory(deviceInfo, ex.getMessage());
            }
        });
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("Closing clustering services for node {}", deviceInfo);
        mastershipChangeListener.onSlaveRoleAcquired(deviceInfo);

        final ListenableFuture<List<Void>> servicesToBeClosed = Futures
                .successfulAsList(Lists.reverse(contexts)
                        .stream()
                        .map(OFPContext::closeServiceInstance)
                        .collect(Collectors.toList()));


        return Futures.transform(servicesToBeClosed, new Function<List<Void>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable final List<Void> input) {
                LOG.info("Closed clustering services for node {}", deviceInfo);
                return null;
            }
        });
    }

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void close() {
        if (CONTEXT_STATE.TERMINATION.equals(state)) {
            LOG.debug("ContextChain for node {} is already in TERMINATION state.", deviceInfo);
            return;
        }

        state = CONTEXT_STATE.TERMINATION;
        mastershipChangeListener.onSlaveRoleAcquired(deviceInfo);

        // If we somehow have initialization still running, cancel it
        if (Objects.nonNull(initFuture)) {
            if (!initFuture.isCancelled() && !initFuture.isDone()) {
                LOG.info("Waiting for finishing the running initialization process for node {}", deviceInfo);

                try {
                    initFuture.get(CLOSE_SERVICE_TIMEOUT, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    LOG.warn("Failed to await running initialization for node {}: {}", deviceInfo, e);
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    initFuture.cancel(true);
                }
            }

            initFuture = null;
        }

        // Close all connections to devices
        auxiliaryConnections.forEach(connectionContext -> connectionContext.closeConnection(false));
        auxiliaryConnections.clear();
        primaryConnection.closeConnection(true);


        // Close all contexts (device, statistics, rpc)
        contexts.forEach(OFPContext::close);
        contexts.clear();

        // If we are still registered and we are not already closing, then close the registration
        if (Objects.nonNull(registration)) {
            try {
                LOG.info("Closing clustering services registration for node {}", deviceInfo);
                registration.close();
                registration = null;
                LOG.info("Closed clustering services registration for node {}", deviceInfo);
            } catch (final Exception e) {
                LOG.warn("Failed to close clustering services registration for node {} with exception: ",
                        deviceInfo, e);
            }
        }

        // We are closing, so cleanup all managers now
        deviceRemovedHandlers.forEach(h -> h.onDeviceRemoved(deviceInfo));
        deviceRemovedHandlers.clear();
    }

    @Override
    public void makeContextChainStateSlave() {
        unMasterMe();
        changeState(ContextChainState.WORKING_SLAVE);
    }

    @Override
    public void registerServices(final ClusterSingletonServiceProvider clusterSingletonServiceProvider) {
        LOG.info("Registering clustering services for node {}", deviceInfo);
        state = CONTEXT_STATE.WORKING;
        registration = Objects.requireNonNull(clusterSingletonServiceProvider
                .registerClusterSingletonService(this));
        LOG.info("Registered clustering services for node {}", deviceInfo);
    }

    @Override
    public void makeDeviceSlave() {
        unMasterMe();

        contexts.stream()
                .filter(DeviceContext.class::isInstance)
                .map(DeviceContext.class::cast)
                .findAny()
                .ifPresent(deviceContext -> Futures
                        .addCallback(
                                deviceContext.makeDeviceSlave(),
                                new DeviceSlaveCallback()));
    }

    @Override
    public boolean isMastered(@Nonnull ContextChainMastershipState mastershipState) {
        switch (mastershipState) {
            case INITIAL_SUBMIT:
                LOG.debug("Device {}, initial submit OK.", deviceInfo);
                this.initialSubmitting.set(true);
                break;
            case MASTER_ON_DEVICE:
                LOG.debug("Device {}, master state OK.", deviceInfo);
                this.masterStateOnDevice.set(true);
                break;
            case INITIAL_GATHERING:
                LOG.debug("Device {}, initial gathering OK.", deviceInfo);
                this.initialGathering.set(true);
                break;
            case RPC_REGISTRATION:
                LOG.debug("Device {}, RPC registration OK.", deviceInfo);
                this.rpcRegistration.set(true);
            case INITIAL_FLOW_REGISTRY_FILL:
                // Flow registry fill is not mandatory to work as a master
                LOG.debug("Device {}, initial registry filling OK.", deviceInfo);
                this.registryFilling.set(true);
            case CHECK:
            default:
        }

        final boolean result = initialGathering.get() &&
                masterStateOnDevice.get() &&
                initialSubmitting.get() &&
                rpcRegistration.get();

        if (result && mastershipState != ContextChainMastershipState.CHECK) {
            LOG.info("Device {} is able to work as master{}",
                    deviceInfo,
                    registryFilling.get() ? "." : " WITHOUT flow registry !!!");
            changeState(ContextChainState.WORKING_MASTER);
        }

        return result;
    }

    @Override
    public boolean isClosing() {
        return CONTEXT_STATE.TERMINATION.equals(state);
    }

    @Override
    public boolean addAuxiliaryConnection(@Nonnull ConnectionContext connectionContext) {
        return (connectionContext.getFeatures().getAuxiliaryId() != 0)
                && (!ConnectionContext.CONNECTION_STATE.RIP.equals(primaryConnection.getConnectionState()))
                && auxiliaryConnections.add(connectionContext);
    }

    @Override
    public boolean auxiliaryConnectionDropped(@Nonnull ConnectionContext connectionContext) {
        return auxiliaryConnections.remove(connectionContext);
    }

    @Override
    public void registerDeviceRemovedHandler(@Nonnull final DeviceRemovedHandler deviceRemovedHandler) {
        deviceRemovedHandlers.add(deviceRemovedHandler);
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

    private void unMasterMe() {
        registryFilling.set(false);
        initialSubmitting.set(false);
        initialGathering.set(false);
        masterStateOnDevice.set(false);
        rpcRegistration.set(false);
    }

    private final class DeviceSlaveCallback implements FutureCallback<RpcResult<SetRoleOutput>> {
        @Override
        public void onSuccess(@Nullable final RpcResult<SetRoleOutput> result) {
            mastershipChangeListener.onSlaveRoleAcquired(deviceInfo);
        }

        @Override
        public void onFailure(@Nonnull final Throwable t) {
            mastershipChangeListener.onSlaveRoleNotAcquired(deviceInfo);
        }
    }
}
