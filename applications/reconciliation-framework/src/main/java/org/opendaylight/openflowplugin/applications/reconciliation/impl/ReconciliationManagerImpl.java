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
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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

public class ReconciliationManagerImpl implements ReconciliationManager, ReconciliationFrameworkEvent {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManagerImpl.class);

    private MastershipChangeServiceManager mastershipChangeServiceManager;
    private Map<Integer, List<ReconciliationNotificationListener>> registeredServices = new TreeMap<>();
    private Map<DeviceInfo, ListenableFuture<ResultState>> futureMap = new HashMap<>();

    public ReconciliationManagerImpl(MastershipChangeServiceManager mastershipChangeServiceManager) {
        this.mastershipChangeServiceManager = mastershipChangeServiceManager;
    }

    public void start() throws MastershipChangeException {
        mastershipChangeServiceManager.reconciliationFrameworkRegistration(this);
        LOG.info("ReconciliationManager has started successfully.");
    }

    @Override
    public NotificationRegistration registerService(ReconciliationNotificationListener reconciliationTask) {
        LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getName(),
                reconciliationTask.getPriority(), reconciliationTask.getResultState());
        registeredServices.computeIfAbsent(reconciliationTask.getPriority(), services -> new ArrayList<>())
        .add(reconciliationTask);
        ReconciliationServiceDelegate registration = new ReconciliationServiceDelegate(reconciliationTask, () -> {
            LOG.debug("Service un-registered from Reconciliation framework {}", reconciliationTask.getName());
            registeredServices.computeIfPresent(reconciliationTask.getPriority(), (priority, services) -> services)
            .remove(reconciliationTask);
        });
        return registration;
    }

    @Override
    public Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices() {
        ImmutableMap.Builder<Integer, List<ReconciliationNotificationListener>> builder = ImmutableMap.builder();
        builder.putAll(registeredServices);
        return builder.build();
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public ListenableFuture<ResultState> onDevicePrepared(@Nonnull DeviceInfo node) {
        LOG.debug("Triggering reconciliation for node : {}", node.getNodeId());
        //ReconciliationTask reconciliationJob = new ReconciliationTask(deviceInfo);
        return futureMap.computeIfAbsent(node, value -> reconcileNode(node));
    }

    @Override
    public ListenableFuture<Void> onDeviceDisconnected(@Nonnull DeviceInfo node) {
        LOG.info("Stopping reconciliation for node {}", node.getNodeId());
        if (futureMap.containsKey(node)) {
            return cancelNodeReconciliation(node);
        }
        return Futures.immediateFuture(null);
    }

    ListenableFuture<ResultState> reconcileNode(DeviceInfo node) {
        ListenableFuture<ResultState> lastFuture = Futures.immediateFuture(null);
        for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
            lastFuture = reconcileServices(lastFuture, services, node);
        }
        return lastFuture;
    }

    ListenableFuture<ResultState> reconcileServices(ListenableFuture<ResultState> prevFuture, 
            List<ReconciliationNotificationListener> servicesForPriority, DeviceInfo node) {
        return Futures.transformAsync(prevFuture, prevResult -> {
            return Futures.transform(Futures.allAsList(servicesForPriority.stream().map(service -> service.startReconciliation(node)).collect(Collectors.toList())),
                    results->ResultState.DONOTHING);
        });
    }

    ListenableFuture<Void> cancelNodeReconciliation(DeviceInfo node) {
        ListenableFuture<Void> lastFuture = Futures.immediateFuture(null);
        for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
            lastFuture = cancelServiceReconciliation(lastFuture, services, node);
        }
        futureMap.get(node).cancel(true);
        futureMap.remove(node);
        return lastFuture;
    }

    ListenableFuture<Void> cancelServiceReconciliation(ListenableFuture<Void> prevFuture, 
            List<ReconciliationNotificationListener> servicesForPriority, DeviceInfo node) {
        return Futures.transformAsync(prevFuture, prevResult -> {
            return Futures.transform(Futures.allAsList(servicesForPriority.stream().map(service -> service.endReconciliation(node)).collect(Collectors.toList())),
                    results->null);
        });
    }
}