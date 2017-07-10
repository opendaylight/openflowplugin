/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.mastership.MastershipChangeServiceManager;
import org.opendaylight.openflowplugin.api.openflow.mastership.ReconciliationFrameworkEvent;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationManagerImpl implements ReconciliationManager, ReconciliationFrameworkEvent {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManagerImpl.class);

    private MastershipChangeServiceManager mastershipChangeServiceManager;
    private Map<Integer, List<ReconciliationNotificationListener>> registeredServices = new TreeMap<>();
    private Map<DeviceInfo, SettableFuture<ResultState>> futureMap = new HashMap<>();
    private final int THREAD_POOL_SIZE = 10;

    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public ReconciliationManagerImpl(MastershipChangeServiceManager mastershipChangeServiceManager) {
        this.mastershipChangeServiceManager = mastershipChangeServiceManager;
    }

    public void start() {
        mastershipChangeServiceManager.reconciliationFrameworkRegistration(this);
        LOG.info("ReconciliationManager has started successfully.");
    }

    @Override
    public void registerService(ReconciliationNotificationListener reconciliationTask) {
        LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getName(),
                reconciliationTask.getPriority(), reconciliationTask.getResultState());
        registeredServices.computeIfAbsent(reconciliationTask.getPriority(), services -> new ArrayList<>())
        .add(reconciliationTask);
    }

    @Override public void unregisterService(ReconciliationNotificationListener service) {
        LOG.debug("Unregistered service {} from reconciliation framework", service.getResultState());
        registeredServices.remove(service);
    }

    @Override
    public void haltReconciliation(DeviceInfo node) {
        LOG.info("Stopping reconciliation for node {}", node.getNodeId());
        if (futureMap.containsKey(node)) {
            CancelReconciliationTask cancelReconciliationJob = new CancelReconciliationTask(node);
            executor.submit(cancelReconciliationJob);
            futureMap.get(node).cancel(true);
            LOG.info("cancelled the future for node {}", node.getNodeId());
            futureMap.remove(node);
        }
    }

    @Override
    public Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices() {
        ImmutableMap.Builder<Integer, List<ReconciliationNotificationListener>> builder = ImmutableMap.builder();
        builder.putAll(registeredServices);
        return builder.build();
    }

    @Override public void close() throws Exception {
        executor.shutdown();
    }

    @Override public void onDevicePrepared(@Nonnull DeviceInfo node,
            @Nonnull FutureCallback<ResultState> callback) {

        LOG.debug("Triggering reconciliation for node : {}", node.getNodeId());
        SettableFuture<ResultState> future = futureMap.computeIfAbsent(node, value -> SettableFuture.create());
        ReconciliationTask reconciliationJob = new ReconciliationTask(node, future);
        executor.submit(reconciliationJob);
        try {
            callback.onSuccess(future.get());
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error in processing reconciliation for the node {}", node, e);
            callback.onFailure(e);
        }
    }

    private class ReconciliationTask implements Callable<SettableFuture<ResultState>> {

        final DeviceInfo node;
        final SettableFuture<ResultState> future;

        public ReconciliationTask(final DeviceInfo node, final SettableFuture<ResultState> future) {
            this.node = node;
            this.future = future;
        }

        @Override
        public SettableFuture<ResultState> call() {
            List<ListenableFuture<Boolean>> list = new ArrayList<>();
            for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
                for (ReconciliationNotificationListener service : services) {
                    CreateReconcileTasks task = new CreateReconcileTasks(service, node);
                    LOG.info("Calling startReconciliation with node {} for service {} with priority {}",
                            node, service.getName(), service.getPriority());
                    try {
                        list.add(executor.submit(task).get());
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error(
                                "Error while calling startReconciliation with node {} for service {} with priority {}",
                                node, service.getName(), service.getPriority(), e);
                    }
                }
                try {
                    Futures.successfulAsList(list).get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error while waiting for the reconciliation jobs to complete for node {}", node.getNodeId(), e);
                }
            }
            //The logic to set decide the intent to return is to be decided
            future.set(ResultState.DONOTHING);
            return future;
        }
    }

    private class CreateReconcileTasks implements Callable<ListenableFuture<Boolean>> {

        private ReconciliationNotificationListener service;
        private DeviceInfo node;

        public CreateReconcileTasks(ReconciliationNotificationListener service, DeviceInfo node) {
            this.service = service;
            this.node = node;
        }

        @Override
        public ListenableFuture<Boolean> call() {

            return service.startReconciliation(node);
        }
    }

    private class CancelReconciliationTask implements Runnable {

        final DeviceInfo node;

        public CancelReconciliationTask(final DeviceInfo node) {
            this.node = node;
        }

        @Override
        public void run() {
            for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
                for (ReconciliationNotificationListener service : services) {
                    LOG.info("Calling endReconciliation with node{}", node);
                    service.endReconciliation(node);
                }
            }

        }
    }

}