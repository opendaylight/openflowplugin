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
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.framework.config.rev170712.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReconciliationManagerImpl implements ReconciliationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManagerImpl.class);

    private Map<Integer, List<ReconciliationNotificationListener>> registeredServices = new TreeMap<>();
    private Map<NodeId, SettableFuture<Intent>> futureMap = new HashMap<>();
    private final int THREAD_POOL_SIZE = 10;

    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public ReconciliationManagerImpl() {
    }

    public void start() {
        LOG.info("ReconciliationManager has started successfully.");
    }

    @Override
    public void registerService(ReconciliationNotificationListener reconciliationTask) {
        LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getName(),
                reconciliationTask.getPriority(), reconciliationTask.getIntent());
        registeredServices.computeIfAbsent(reconciliationTask.getPriority(), services -> new ArrayList<>())
        .add(reconciliationTask);
    }

    @Override
    public Future<Intent> triggerReconciliation(NodeId nodeId) {
        LOG.debug("Triggering reconciliation for node : {}", nodeId);
        SettableFuture<Intent> future = futureMap.computeIfAbsent(nodeId, value -> SettableFuture.create());
        ReconciliationTask reconciliationJob = new ReconciliationTask(nodeId, future);
        executor.submit(reconciliationJob);
        return future;
    }

    @Override
    public void haltReconciliation(NodeId nodeId) {
        LOG.info("Stopping reconciliation for node {}", nodeId);
        if (futureMap.containsKey(nodeId)) {
            CancelReconciliationTask cancelReconciliationJob = new CancelReconciliationTask(nodeId);
            executor.submit(cancelReconciliationJob);
            futureMap.get(nodeId).cancel(true);
            LOG.info("cancelled the future for node {}", nodeId);
            futureMap.remove(nodeId);
        }
    }

    @Override
    public Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices() {
        ImmutableMap.Builder<Integer, List<ReconciliationNotificationListener>> builder = ImmutableMap.builder();
        builder.putAll(registeredServices);
        return builder.build();
    }

    private class ReconciliationTask implements Callable<SettableFuture<Intent>> {

        final NodeId nodeId;
        final SettableFuture<Intent> future;

        public ReconciliationTask(final NodeId nodeId, final SettableFuture<Intent> future) {
            this.nodeId = nodeId;
            this.future = future;
        }

        @Override
        public SettableFuture<Intent> call() {
            List<ListenableFuture<Boolean>> list = new ArrayList<>();
            for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
                for (ReconciliationNotificationListener service : services) {
                    CreateReconcileTasks task = new CreateReconcileTasks(service, nodeId);
                    LOG.info("Calling startReconciliation with node {} for service {} with priority {}",
                            nodeId, service.getName(), service.getPriority());
                    try {
                        list.add(executor.submit(task).get());
                    } catch (InterruptedException | ExecutionException e) {
                        LOG.error(
                                "Error while calling startReconciliation with node {} for service {} with priority {}",
                                nodeId, service.getName(), service.getPriority(), e);
                    }
                }
                try {
                    Futures.successfulAsList(list).get();
                } catch (InterruptedException | ExecutionException e) {
                    LOG.error("Error while waiting for the reconciliation jobs to complete for node {}", nodeId, e);
                }
            }
            //The logic to set decide the intent to return is to be decided
            future.set(Intent.DONOTHING);
            return future;
        }
    }

    private class CreateReconcileTasks implements Callable<ListenableFuture<Boolean>> {

        private ReconciliationNotificationListener service;
        private NodeId nodeId;

        public CreateReconcileTasks(ReconciliationNotificationListener service, NodeId nodeId) {
            this.service = service;
            this.nodeId = nodeId;
        }

        @Override
        public ListenableFuture<Boolean> call() {

            return service.startReconciliation(nodeId);
        }
    }

    private class CancelReconciliationTask implements Runnable {

        final NodeId nodeId;

        public CancelReconciliationTask(final NodeId nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public void run() {
            for (List<ReconciliationNotificationListener> services : registeredServices.values()) {
                for (ReconciliationNotificationListener service : services) {
                    LOG.info("Calling endReconciliation with node{}", nodeId);
                    service.endReconciliation(nodeId);
                }
            }

        }
    }

}