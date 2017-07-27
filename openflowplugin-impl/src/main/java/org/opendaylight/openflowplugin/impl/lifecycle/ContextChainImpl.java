/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.base.Function;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
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
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainStateListener;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.SetRoleOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainImpl implements ContextChain {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainImpl.class);
    private final AtomicBoolean masterStateOnDevice = new AtomicBoolean(false);
    private final AtomicBoolean initialGathering = new AtomicBoolean(false);
    private final AtomicBoolean initialSubmitting = new AtomicBoolean(false);
    private final AtomicBoolean registryFilling = new AtomicBoolean(false);
    private final AtomicBoolean rpcRegistration = new AtomicBoolean(false);
    private final List<OFPContext> contexts = new CopyOnWriteArrayList<>();
    private final List<DeviceRemovedHandler> deviceRemovedHandlers = new CopyOnWriteArrayList<>();
    private final List<ConnectionContext> auxiliaryConnections = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService;
    private final ContextChainMastershipWatcher contextChainMastershipWatcher;
    private final DeviceInfo deviceInfo;
    private final ConnectionContext primaryConnection;
    private final AtomicReference<ContextChainState> contextChainState =
            new AtomicReference<>(ContextChainState.UNDEFINED);

    private AutoCloseable registration;
    private Supplier<List<OFPContext>> contextsSupplier;
    private boolean closing;

    ContextChainImpl(@Nonnull final ContextChainMastershipWatcher contextChainMastershipWatcher,
                     @Nonnull final ConnectionContext connectionContext,
                     @Nonnull final ExecutorService executorService) {
        this.contextChainMastershipWatcher = contextChainMastershipWatcher;
        this.primaryConnection = connectionContext;
        this.deviceInfo = connectionContext.getDeviceInfo();
        this.executorService = executorService;
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("Starting clustering services for node {}", deviceInfo);
        Verify.verify(contexts.isEmpty());
        contexts.addAll(contextsSupplier.get());
        contexts.forEach(context -> {
            context.addListener(new Service.Listener() {
                @Override
                public void failed(final Service.State from, final Throwable failure) {
                    super.failed(from, failure);
                    executorService.submit(() -> contextChainMastershipWatcher.onNotAbleToStartMastershipMandatory(
                            deviceInfo,
                            failure.getMessage()));
                }
            }, executorService);

            context.start();
        });

        LOG.info("Started clustering services for node {}", deviceInfo);
    }

    @Override
    public ListenableFuture<Void> closeServiceInstance() {
        LOG.info("Closing clustering services for node {}", deviceInfo);

        final ListenableFuture<List<Void>> listListenableFuture = Futures.allAsList(
                Lists.reverse(contexts).stream().map(context -> {
                    final SettableFuture<Void> future = SettableFuture.create();

                    context.addListener(new Service.Listener() {
                        @Override
                        public void terminated(final Service.State from) {
                            super.terminated(from);
                            future.set(null);
                        }

                        @Override
                        public void failed(final Service.State from, final Throwable failure) {
                            super.failed(from, failure);
                            future.setException(failure);
                        }
                    }, executorService);

                    context.stopAsync();
                    return future;
                }).collect(Collectors.toList()));

        return Futures.transform(listListenableFuture, new Function<List<Void>, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable final List<Void> input) {
                contextChainMastershipWatcher.onSlaveRoleAcquired(deviceInfo);
                contexts.clear();
                LOG.info("Closed clustering services for node {}", deviceInfo);
                return null;
            }
        }, executorService);
    }

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    public void registerSupplier(@Nonnull final Supplier<List<OFPContext>> contextsSupplier) {
        this.contextsSupplier = contextsSupplier;
    }

    @Override
    public void close() throws Exception {
        closing = true;
        contextChainMastershipWatcher.onSlaveRoleAcquired(deviceInfo);

        // Close all connections to devices
        auxiliaryConnections.forEach(connectionContext -> connectionContext.closeConnection(false));
        auxiliaryConnections.clear();
        primaryConnection.closeConnection(true);

        // Close all contexts (device, statistics, rpc)
        contexts.forEach(OFPContext::stop);
        contexts.clear();
        contextsSupplier = Collections::emptyList;

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
                                new DeviceSlaveCallback(),
                                executorService));
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
                break;
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
        return closing;
    }

    @Override
    public boolean isPrepared() {
        return this.initialGathering.get() &&
                this.masterStateOnDevice.get() &&
                this.rpcRegistration.get();
    }

    @Override
    public boolean continueInitializationAfterReconciliation() {
        return isMastered(ContextChainMastershipState.INITIAL_SUBMIT) && contexts.stream()
                .filter(StatisticsContext.class::isInstance)
                .map(StatisticsContext.class::cast)
                .findAny()
                .map(StatisticsContext::initialSubmitAfterReconciliation)
                .orElse(false);
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
        boolean propagate = this.contextChainState.get().equals(ContextChainState.UNDEFINED);
        this.contextChainState.set(contextChainState);

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
            contextChainMastershipWatcher.onSlaveRoleAcquired(deviceInfo);
        }

        @Override
        public void onFailure(@Nonnull final Throwable t) {
            contextChainMastershipWatcher.onSlaveRoleNotAcquired(deviceInfo);
        }
    }
}
