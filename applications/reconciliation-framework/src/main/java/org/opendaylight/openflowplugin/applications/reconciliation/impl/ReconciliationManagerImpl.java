/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(service = ReconciliationManager.class, immediate = true)
public final class ReconciliationManagerImpl
        implements ReconciliationManager, ReconciliationFrameworkEvent, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManagerImpl.class);

    private final AtomicReference<ResultState> decidedResultState = new AtomicReference<>(ResultState.DONOTHING);
    private final ConcurrentMap<Integer, List<ReconciliationNotificationListener>> registeredServices =
            new ConcurrentSkipListMap<>();
    private final ConcurrentMap<DeviceInfo, ListenableFuture<ResultState>> futureMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<ResultState, Integer> resultStateMap = new ConcurrentHashMap<>();
    private final Registration reg;

    @Inject
    @Activate
    public ReconciliationManagerImpl(@Reference final MastershipChangeServiceManager mastershipChangeServiceManager)
            throws MastershipChangeException {
        reg = mastershipChangeServiceManager.reconciliationFrameworkRegistration(this);
        LOG.info("ReconciliationManager started");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        reg.close();
        LOG.info("ReconciliationManager stopped");
    }

    @Override
    public NotificationRegistration registerService(final ReconciliationNotificationListener reconciliationTask) {
        LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getName(),
                  reconciliationTask.getPriority(), reconciliationTask.getResultState());
        registeredServices.computeIfAbsent(reconciliationTask.getPriority(), services -> new ArrayList<>())
                .add(reconciliationTask);
        final var registration = new ReconciliationServiceDelegate(() -> {
            LOG.debug("Service un-registered from Reconciliation framework {}", reconciliationTask.getName());
            registeredServices.computeIfPresent(reconciliationTask.getPriority(), (priority, services) -> services)
                    .remove(reconciliationTask);
            decideResultState(reconciliationTask.getResultState());
        });
        decideResultState(reconciliationTask.getResultState());
        return registration;
    }

    private void decideResultState(final ResultState resultState) {
        resultStateMap.compute(resultState, (unused, count) -> count == null ? 1 : count + 1);

        // FIXME: can we do this more efficiently?
        Entry<ResultState, Integer> maxEntry = null;
        for (var entry : resultStateMap.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        decidedResultState.set(maxEntry.getKey());
    }

    @Override
    public Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices() {
        return ImmutableMap.copyOf(registeredServices);
    }

    @Override
    public ListenableFuture<ResultState> onDevicePrepared(final DeviceInfo node) {
        LOG.debug("Triggering reconciliation for node : {}", node.getNodeId());
        return futureMap.computeIfAbsent(node, value -> reconcileNode(node));
    }

    @Override
    public ListenableFuture<Void> onDeviceDisconnected(final DeviceInfo node) {
        LOG.info("Stopping reconciliation for node {}", node.getNodeId());
        return futureMap.containsKey(node) ? cancelNodeReconciliation(node) : Futures.immediateVoidFuture();
    }

    private ListenableFuture<ResultState> reconcileNode(final DeviceInfo node) {
        var lastFuture = Futures.<ResultState>immediateFuture(null);
        for (var services : registeredServices.values()) {
            lastFuture = reconcileServices(lastFuture, services, node);
        }
        return lastFuture;
    }

    private ListenableFuture<ResultState> reconcileServices(final ListenableFuture<ResultState> prevFuture,
            final List<ReconciliationNotificationListener> servicesForPriority, final DeviceInfo node) {
        return Futures.transformAsync(prevFuture,
            prevResult -> Futures.transform(Futures.allAsList(servicesForPriority.stream()
                .map(service -> service.startReconciliation(node)).collect(Collectors.toList())),
                results -> decidedResultState.get(),
                MoreExecutors.directExecutor()),
            MoreExecutors.directExecutor());
    }

    private ListenableFuture<Void> cancelNodeReconciliation(final DeviceInfo node) {
        var lastFuture = Futures.immediateVoidFuture();
        futureMap.get(node).cancel(true);
        futureMap.remove(node);
        for (var services : registeredServices.values()) {
            lastFuture = cancelServiceReconciliation(lastFuture, services, node);
        }
        return lastFuture;
    }

    private static ListenableFuture<Void> cancelServiceReconciliation(final ListenableFuture<Void> prevFuture,
            final List<ReconciliationNotificationListener> servicesForPriority, final DeviceInfo node) {
        return Futures.transformAsync(prevFuture,
            prevResult -> Futures.transform(Futures.allAsList(servicesForPriority.stream()
                .map(service -> service.endReconciliation(node))
                .collect(Collectors.toList())),
                results -> null,
                MoreExecutors.directExecutor()),
            MoreExecutors.directExecutor());
    }
}
