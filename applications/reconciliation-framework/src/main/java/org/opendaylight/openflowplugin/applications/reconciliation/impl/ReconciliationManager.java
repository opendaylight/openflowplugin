/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationTaskFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class ReconciliationManager implements IReconciliationManager {

        private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManager.class);

        private static Map<Integer, List<IReconciliationTaskFactory>> registeredServices = new TreeMap<>();
        private static Map<NodeId, SettableFuture<NodeId>> futureMap = new HashMap<>();
        private static final int THREAD_POOL_SIZE = 10;

        private final DataBroker dataService;
        private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        public ReconciliationManager(DataBroker dataService) {
                this.dataService = dataService;
        }

        private class ReconciliationTask implements Callable<SettableFuture<NodeId>> {

                NodeId nodeId;
                SettableFuture<NodeId> future;

                public ReconciliationTask(final NodeId nodeId, SettableFuture<NodeId> future) {
                        this.nodeId = nodeId;
                        this.future = future;
                }

                @Override
                public SettableFuture<NodeId> call() {
                        List<Future> list = new ArrayList<>();
                        for (Map.Entry<Integer, List<IReconciliationTaskFactory>> registeredService : registeredServices
                                .entrySet()) {
                                List<IReconciliationTaskFactory> services = registeredService.getValue();
                                for (IReconciliationTaskFactory service : services) {
                                        final ExecutorService exService = Executors.newSingleThreadExecutor();
                                        CreateReconcileTasks task = new CreateReconcileTasks(service, nodeId);
                                        LOG.info("Calling startReconcileTask with node{} for service{} with priority{}",
                                                nodeId, service.getServiceName(), service.getPriority());
                                        list.add(exService.submit(task));
                                }
                                waitForTasks(list);
                        }
                        future.set(nodeId);
                        return future;
                }
        }

        public class CreateReconcileTasks implements Callable<Future> {

                private IReconciliationTaskFactory service;
                private NodeId nodeId;

                public CreateReconcileTasks(IReconciliationTaskFactory service, NodeId nodeId) {
                        this.service = service;
                        this.nodeId = nodeId;
                }

                @Override public Future call() {

                        return service.startReconcileTask(nodeId);
                }
        }

        public void waitForTasks(List<Future> futures) {
                while (!futures.isEmpty()) {

                        Iterator<Future> itr = futures.iterator();
                        while (itr.hasNext()) {
                                if (itr.next().isDone()) {
                                        itr.remove();
                                }
                        }
                }

        }

        @Override
        public void registerService(IReconciliationTaskFactory reconciliationTask) {
                LOG.debug("Registered service {} with priority {} and intent {}", reconciliationTask.getServiceName(),
                        reconciliationTask.getPriority(), reconciliationTask.getIntent());
                if (registeredServices.containsKey(reconciliationTask.getPriority())) {
                        registeredServices.get(reconciliationTask.getPriority()).add(reconciliationTask);
                } else {
                        List<IReconciliationTaskFactory> temp = new ArrayList<>();
                        temp.add(reconciliationTask);
                        registeredServices.put(reconciliationTask.getPriority(), temp);
                }
        }

        @Override
        public SettableFuture<NodeId> startReconciliation(NodeId nodeId) {

                LOG.debug("In startReconciliation for node {}", nodeId);
                SettableFuture<NodeId> future;
                if (futureMap.containsKey(nodeId)) {
                        future = futureMap.get(nodeId);
                } else {
                        future = SettableFuture.create();
                        futureMap.put(nodeId, future);
                }
                ReconciliationTask reconciliationJob = new ReconciliationTask(nodeId, future);
                Future<SettableFuture<NodeId>> futureTask = executor.submit(reconciliationJob);
                try {
                        future = futureTask.get();
                } catch (InterruptedException e) {
                        e.printStackTrace();
                } catch (ExecutionException e) {
                        e.printStackTrace();
                }
                return future;
        }

        @Override
        public void cancelReconciliation(NodeId nodeId) {
                LOG.info("In cancelReconciliation for node {}", nodeId);
                if (futureMap.containsKey(nodeId)) {
                        CancelReconciliationTask cancelReconciliationJob = new CancelReconciliationTask(nodeId,
                                futureMap.get(nodeId));
                        executor.submit(cancelReconciliationJob);
                        futureMap.get(nodeId).cancel(true);
                        LOG.info("cancelled the future for node {}", nodeId);
                        futureMap.remove(nodeId);
                }
        }

        @Override
        public Map<Integer, List<IReconciliationTaskFactory>> getRegisteredServices() {
                return registeredServices;
        }

        public class CancelReconciliationTask implements Runnable {

                NodeId nodeId;
                SettableFuture<NodeId> future;

                public CancelReconciliationTask(final NodeId nodeId, SettableFuture<NodeId> future) {
                        this.nodeId = nodeId;
                        this.future = future;
                }

                @Override
                public void run() {
                        for (Map.Entry<Integer, List<IReconciliationTaskFactory>> registeredService : registeredServices
                                .entrySet()) {
                                List<IReconciliationTaskFactory> services = registeredService.getValue();
                                for (IReconciliationTaskFactory service : services) {
                                        LOG.info("Calling cancelReconcileTask with node{}", nodeId);
                                        service.cancelReconcileTask(nodeId);
                                }
                        }

                }
        }

}