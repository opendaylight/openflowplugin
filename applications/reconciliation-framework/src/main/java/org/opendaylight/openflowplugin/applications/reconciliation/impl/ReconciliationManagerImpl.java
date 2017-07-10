/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.ReconciliationTaskFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.framework.config.rev170712.IntentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.SettableFuture;


public class ReconciliationManagerImpl implements ReconciliationManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManagerImpl.class);

    private static Map<Integer, List<ReconciliationTaskFactory>> registeredServices = new TreeMap<>();
    private static Map<NodeId, SettableFuture<NodeId>> futureMap = new HashMap<>();
    private static final int THREAD_POOL_SIZE = 10;

    private final DataBroker dataService;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public ReconciliationManagerImpl(DataBroker dataService) {
        this.dataService = dataService;
    }

    private class ReconciliationTask implements Callable<SettableFuture<IntentType>> {

        final NodeId nodeId;
        final SettableFuture<IntentType> future;

        public ReconciliationTask(final NodeId nodeId, final SettableFuture<IntentType> future) {
            this.nodeId = nodeId;
            this.future = future;
        }

        @Override
        public SettableFuture<IntentType> call() {
            List<Future<Future<NodeId>>> list = new ArrayList<>();
            for (List<ReconciliationTaskFactory> services : registeredServices.values()) {
                for (ReconciliationTaskFactory service : services) {
                    CreateReconcileTasks task = new CreateReconcileTasks(service, nodeId);
                    LOG.info("Calling startReconcileTask with node{} for service{} with priority{}",
                            nodeId, service.getServiceName(), service.getPriority());
                    list.add(executor.submit(task));
                }
                waitForTasks(list);
            }
            //The logic to set decide the intent to return is to be decided
            future.set(IntentType.DONOTHING);
            return future;
        }
    }

    private class CreateReconcileTasks implements Callable<Future<NodeId>> {

        private ReconciliationTaskFactory service;
        private NodeId nodeId;

        public CreateReconcileTasks(ReconciliationTaskFactory service, NodeId nodeId) {
            this.service = service;
            this.nodeId = nodeId;
        }

        @Override 
        public Future<NodeId> call() {

            return service.startReconcileTask(nodeId);
        }
    }

    private void waitForTasks(List<Future<Future<NodeId>>> futures) {
        while (!futures.isEmpty()) {

            Iterator<Future<Future<NodeId>>> itr = futures.iterator();
            while (itr.hasNext()) {
                if (itr.next().isDone()) {
                    itr.remove();
                }
            }
        }

    }

    @Override
    public void registerService(ReconciliationTaskFactory reconciliationTask) {
        LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getServiceName(),
                reconciliationTask.getPriority(), reconciliationTask.getIntent());
        registeredServices.computeIfAbsent(reconciliationTask.getPriority(), services -> new ArrayList<>()).add(reconciliationTask);
    }

    @Override
    public Future<IntentType> startReconciliation(NodeId nodeId) {

        LOG.debug("In startReconciliation for node {}", nodeId);
        SettableFuture<IntentType> future = SettableFuture.create();
        futureMap.computeIfAbsent(nodeId, value -> SettableFuture.create());
        ReconciliationTask reconciliationJob = new ReconciliationTask(nodeId, future);
        Future<SettableFuture<IntentType>> futureTask = executor.submit(reconciliationJob);
        try {
            future = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("The reconciliation job failed for the node {}", nodeId, e);
        }
        return future;
    }

    @Override
    public void cancelReconciliation(NodeId nodeId) {
        LOG.info("In cancelReconciliation for node {}", nodeId);
        if (futureMap.containsKey(nodeId)) {
            CancelReconciliationTask cancelReconciliationJob = new CancelReconciliationTask(nodeId);
            executor.submit(cancelReconciliationJob);
            futureMap.get(nodeId).cancel(true);
            LOG.info("cancelled the future for node {}", nodeId);
            futureMap.remove(nodeId);
        }
    }

    @Override
    public Map<Integer, List<ReconciliationTaskFactory>> getRegisteredServices() {
        return registeredServices;
    }

    private class CancelReconciliationTask implements Runnable {

        final NodeId nodeId;

        public CancelReconciliationTask(final NodeId nodeId) {
            this.nodeId = nodeId;
        }

        @Override
        public void run() {
            for (List<ReconciliationTaskFactory> services : registeredServices.values()) {
                for (ReconciliationTaskFactory service : services) {
                    LOG.info("Calling cancelReconcileTask with node{}", nodeId);
                    service.cancelReconcileTask(nodeId);
                }
            }

        }
    }

}