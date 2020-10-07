/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.aries.blueprint.annotation.service.Reference;
import org.apache.aries.blueprint.annotation.service.Service;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeException;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.applications.reconciliation.NotificationRegistration;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Service(classes = ReconciliationManager.class)
public class ReconciliationManagerImpl implements ReconciliationManager, ReconciliationFrameworkEvent {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManagerImpl.class);

    private final MastershipChangeServiceManager mastershipChangeServiceManager;
    private final Map<Integer, List<ReconciliationNotificationListener>> registeredServices =
            new ConcurrentSkipListMap<>();
    private final Map<DeviceInfo, ListenableFuture<ResultState>> futureMap = new ConcurrentHashMap<>();
    private final Map<ResultState, Integer> resultStateMap = new ConcurrentHashMap<>();
    private final AtomicReference<ResultState> decidedResultState = new AtomicReference<>(ResultState.DONOTHING);

    @Inject
    public ReconciliationManagerImpl(@Reference final MastershipChangeServiceManager mastershipChangeServiceManager) {
        this.mastershipChangeServiceManager = requireNonNull(mastershipChangeServiceManager,
            "MastershipChangeServiceManager can not be null!");
    }

    @PostConstruct
    public void start() throws MastershipChangeException {
        mastershipChangeServiceManager.reconciliationFrameworkRegistration(this);
        LOG.info("ReconciliationManager has started successfully.");
    }

    @Override
    public NotificationRegistration registerService(final ReconciliationNotificationListener reconciliationTask) {
        LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getName(),
                  reconciliationTask.getPriority(), reconciliationTask.getResultState());
        registeredServices.computeIfAbsent(reconciliationTask.getPriority(), services -> new ArrayList<>())
                .add(reconciliationTask);
        ReconciliationServiceDelegate registration = new ReconciliationServiceDelegate(() -> {
            LOG.debug("Service un-registered from Reconciliation framework {}", reconciliationTask.getName());
            registeredServices.computeIfPresent(reconciliationTask.getPriority(), (priority, services) -> services)
                    .remove(reconciliationTask);
            decideResultState(reconciliationTask.getResultState());
        });
        decideResultState(reconciliationTask.getResultState());
        return registration;
    }

    private void decideResultState(final ResultState resultState) {
        Integer count = resultStateMap.get(resultState);
        resultStateMap.put(resultState, count = count == null ? 1 : count + 1);
        Map.Entry<ResultState, Integer> maxEntry = null;
        for (Map.Entry<ResultState, Integer> entry : resultStateMap.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }
        decidedResultState.set(maxEntry.getKey());
    }

    @Override
    public Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices() {
        ImmutableMap.Builder<Integer, List<ReconciliationNotificationListener>> builder = ImmutableMap.builder();
        builder.putAll(registeredServices);
        return builder.build();
    }

    @Override
    @PreDestroy
    public void close() {
    }

    @Override
    public ListenableFuture<ResultState> onDevicePrepared(@NonNull final DeviceInfo node) {
        LOG.debug("Triggering reconciliation for node : {}", node.getNodeId());
        return futureMap.computeIfAbsent(node, value -> reconcileNode(node));
    }

    @Override
    public ListenableFuture<Void> onDeviceDisconnected(@NonNull final DeviceInfo node) {
        LOG.info("Stopping reconciliation for node {}", node.getNodeId());
        if (futureMap.containsKey(node)) {
            return cancelNodeReconciliation(node);
        }
        return Futures.immediateFuture(null);
    }

    private ListenableFuture<ResultState> reconcileNode(final DeviceInfo node) {
        ListenableFuture<ResultState> lastFuture = Futures.immediateFuture(null);
        for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
            lastFuture = reconcileServices(lastFuture, services, node);
        }
        return lastFuture;
    }

    private ListenableFuture<ResultState> reconcileServices(final ListenableFuture<ResultState> prevFuture,
                                                            final List<ReconciliationNotificationListener>
                                                                    servicesForPriority,
                                                            final DeviceInfo node) {
        return Futures.transformAsync(prevFuture, prevResult -> Futures.transform(Futures.allAsList(
                servicesForPriority.stream().map(service -> service.startReconciliation(node))
                        .collect(Collectors.toList())), results -> decidedResultState.get(),
                                                                                  MoreExecutors.directExecutor()),
                                      MoreExecutors.directExecutor());
    }

    private ListenableFuture<Void> cancelNodeReconciliation(final DeviceInfo node) {
        ListenableFuture<Void> lastFuture = Futures.immediateFuture(null);
        futureMap.get(node).cancel(true);
        futureMap.remove(node);
        for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
            lastFuture = cancelServiceReconciliation(lastFuture, services, node);
        }
        return lastFuture;
    }

    private ListenableFuture<Void> cancelServiceReconciliation(final ListenableFuture<Void> prevFuture,
                                                               final List<ReconciliationNotificationListener>
                                                                       servicesForPriority,
                                                               final DeviceInfo node) {
        return Futures.transformAsync(prevFuture, prevResult -> Futures.transform(Futures.allAsList(
                servicesForPriority.stream().map(service -> service.endReconciliation(node))
                        .collect(Collectors.toList())), results -> null, MoreExecutors.directExecutor()),
                                      MoreExecutors.directExecutor());
    }
}