/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.lifecycle;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceRemovedHandler;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChain;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainMastershipWatcher;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainState;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ContextChainStateListener;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.GuardedContext;
import org.opendaylight.openflowplugin.api.openflow.lifecycle.ReconciliationFrameworkStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextChainImpl implements ContextChain {
    private static final Logger LOG = LoggerFactory.getLogger(ContextChainImpl.class);

    private final AtomicBoolean masterStateOnDevice = new AtomicBoolean(false);
    private final AtomicBoolean initialGathering = new AtomicBoolean(false);
    private final AtomicBoolean initialSubmitting = new AtomicBoolean(false);
    private final AtomicBoolean registryFilling = new AtomicBoolean(false);
    private final AtomicBoolean rpcRegistration = new AtomicBoolean(false);
    private final List<DeviceRemovedHandler> deviceRemovedHandlers = new CopyOnWriteArrayList<>();
    private final List<GuardedContext> contexts = new CopyOnWriteArrayList<>();
    private final List<ConnectionContext> auxiliaryConnections = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService;
    private final ContextChainMastershipWatcher contextChainMastershipWatcher;
    private final DeviceInfo deviceInfo;
    private final ConnectionContext primaryConnection;
    private final AtomicReference<ContextChainState> contextChainState =
            new AtomicReference<>(ContextChainState.UNDEFINED);
    private AutoCloseable registration;

    ContextChainImpl(@Nonnull final ContextChainMastershipWatcher contextChainMastershipWatcher,
                     @Nonnull final ConnectionContext connectionContext,
                     @Nonnull final ExecutorService executorService) {
        this.contextChainMastershipWatcher = contextChainMastershipWatcher;
        this.primaryConnection = connectionContext;
        this.deviceInfo = connectionContext.getDeviceInfo();
        this.executorService = executorService;
    }

    @Override
    public <T extends OFPContext> void addContext(@Nonnull final T context) {
        contexts.add(new GuardedContextImpl(context));
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void instantiateServiceInstance() {
        try {
            contexts.forEach(OFPContext::instantiateServiceInstance);
            LOG.info("Started clustering services for node {}", deviceInfo);
        } catch (final Exception ex) {
            LOG.warn("Not able to start clustering services for node {}", deviceInfo);
            executorService.execute(() -> contextChainMastershipWatcher
                    .onNotAbleToStartMastershipMandatory(deviceInfo, ex.toString()));
        }
    }

    @Override
    public ListenableFuture<?> closeServiceInstance() {

        contextChainMastershipWatcher.onSlaveRoleAcquired(deviceInfo);

        final ListenableFuture<?> servicesToBeClosed = Futures.allAsList(Lists.reverse(contexts).stream()
            .map(OFPContext::closeServiceInstance)
            .collect(Collectors.toList()));

        return Futures.transform(servicesToBeClosed, (input) -> {
            LOG.info("Closed clustering services for node {}", deviceInfo);
            return null;
        }, executorService);
    }

    @Nonnull
    @Override
    public ServiceGroupIdentifier getIdentifier() {
        return deviceInfo.getServiceIdentifier();
    }

    @Override
    @SuppressWarnings("checkstyle:IllegalCatch")
    public void close() {
        if (ContextChainState.CLOSED.equals(contextChainState.get())) {
            LOG.debug("ContextChain for node {} is already in TERMINATION state.", deviceInfo);
            return;
        }

        contextChainState.set(ContextChainState.CLOSED);
        unMasterMe();

        // Close all connections to devices
        auxiliaryConnections.forEach(connectionContext -> connectionContext.closeConnection(false));
        auxiliaryConnections.clear();

        // If we are still registered and we are not already closing, then close the registration
        if (Objects.nonNull(registration)) {
            try {
                registration.close();
                registration = null;
                LOG.info("Closed clustering services registration for node {}", deviceInfo);
            } catch (final Exception e) {
                LOG.warn("Failed to close clustering services registration for node {} with exception: ",
                        deviceInfo, e);
            }
        }


        // Close all contexts (device, statistics, rpc)
        contexts.forEach(OFPContext::close);
        contexts.clear();

        // We are closing, so cleanup all managers now
        deviceRemovedHandlers.forEach(h -> h.onDeviceRemoved(deviceInfo));
        deviceRemovedHandlers.clear();

        primaryConnection.closeConnection(false);

    }

    @Override
    public void makeContextChainStateSlave() {
        unMasterMe();
        changeMastershipState(ContextChainState.WORKING_SLAVE);
    }

    @Override
    public void registerServices(final ClusterSingletonServiceProvider clusterSingletonServiceProvider) {
        registration = Objects.requireNonNull(clusterSingletonServiceProvider
                .registerClusterSingletonService(this));
        LOG.debug("Registered clustering services for node {}", deviceInfo);
    }

    @Override
    public boolean isMastered(@Nonnull ContextChainMastershipState mastershipState,
                              boolean inReconciliationFrameworkStep) {
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
                break;
            case CHECK:
                // no operation
                break;
            default:
                // no operation
                break;
        }

        final boolean result = initialGathering.get() && masterStateOnDevice.get() && rpcRegistration.get()
                && inReconciliationFrameworkStep || initialSubmitting.get();

        if (!inReconciliationFrameworkStep && result && mastershipState != ContextChainMastershipState.CHECK) {
            LOG.info("Device {} is able to work as master{}", deviceInfo,
                     registryFilling.get() ? "." : " WITHOUT flow registry !!!");
            changeMastershipState(ContextChainState.WORKING_MASTER);
        }

        return result;
    }

    @Override
    public boolean isClosing() {
        return ContextChainState.CLOSED.equals(contextChainState.get());
    }

    @Override
    public void continueInitializationAfterReconciliation() {
        contexts.forEach(context -> {
            if (context.map(ReconciliationFrameworkStep.class::isInstance)) {
                context.map(ReconciliationFrameworkStep.class::cast).continueInitializationAfterReconciliation();
            }
        });
    }

    @Override
    public boolean addAuxiliaryConnection(@Nonnull ConnectionContext connectionContext) {
        return connectionContext.getFeatures().getAuxiliaryId() != 0
                && !ConnectionContext.CONNECTION_STATE.RIP.equals(primaryConnection.getConnectionState())
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

    private void changeMastershipState(final ContextChainState newContextChainState) {
        if (ContextChainState.CLOSED.equals(this.contextChainState.get())) {
            return;
        }

        boolean propagate = ContextChainState.UNDEFINED.equals(this.contextChainState.get());
        this.contextChainState.set(newContextChainState);

        if (propagate) {
            contexts.forEach(context -> {
                if (context.map(ContextChainStateListener.class::isInstance)) {
                    context.map(ContextChainStateListener.class::cast).onStateAcquired(newContextChainState);
                }
            });
        }
    }

    private void unMasterMe() {
        registryFilling.set(false);
        initialSubmitting.set(false);
        initialGathering.set(false);
        masterStateOnDevice.set(false);
        rpcRegistration.set(false);
    }
}
