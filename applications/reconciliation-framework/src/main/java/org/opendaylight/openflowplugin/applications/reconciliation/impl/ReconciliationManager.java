package org.opendaylight.openflowplugin.applications.reconciliation.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationTaskFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by eknnosd on 5/24/2017.
 */
public class ReconciliationManager implements IReconciliationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManager.class);

    static Map<Integer,List<IReconciliationTaskFactory>> registeredServices = new TreeMap<>();
    static Map<NodeId,SettableFuture<NodeId>> futureMap = new HashMap<>();

    private final DataBroker dataService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private  SettableFuture<NodeId> future = SettableFuture.create();


    public ReconciliationManager(DataBroker dataService) {
        this.dataService = dataService;
    }


    public void start() {
        LOG.info("{} start", getClass().getSimpleName());
    }


    private class ReconciliationTask implements Callable<SettableFuture<NodeId>> {

        NodeId nodeId;
        SettableFuture<NodeId> future;

        public ReconciliationTask(final NodeId nodeId, SettableFuture<NodeId> future) {
            this.nodeId = nodeId;
            this.future = future;
        }

        @Override public SettableFuture<NodeId> call() {
            LOG.info("In ReconciliationTask for services{}", registeredServices);
            List<Future> list = new ArrayList<>();
            for (Map.Entry<Integer, List<IReconciliationTaskFactory>> registeredService : registeredServices
                    .entrySet()) {
                List<IReconciliationTaskFactory> services = registeredService.getValue();
                for (IReconciliationTaskFactory service : services) {
                    LOG.info("Calling createReconcileTask with node{} for service{} with priority{}", nodeId, service.getServiceName(),
                            service.getPriority());
                    list.add(service.createReconcileTask(nodeId));
                    final ExecutorService exService = Executors.newSingleThreadExecutor();
                    CreateReconcileTasks task = new CreateReconcileTasks(service, nodeId);
                    list.add(exService.submit(task));
                }
                waitfortasks(list);
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

                return service.createReconcileTask(nodeId);
            }
        }

    public void waitfortasks (List <Future> futures) {
        while (!futures.isEmpty()) {

           Iterator<Future> itr = futures.iterator();
               while(itr.hasNext()){
               if(itr.next().isDone()){
                 itr.remove();
           }
             }
        }

    }

    @Override public void registerService(IReconciliationTaskFactory reconciliationTask) {
        LOG.info("Forwarding rules mgr registered");
        if (registeredServices.get(reconciliationTask.getPriority()) == null) { //gets the value for an id)
            List<IReconciliationTaskFactory> temp = new ArrayList<>();
            temp.add(reconciliationTask);
            registeredServices.put(reconciliationTask.getPriority(),
                    temp); //no ArrayList assigned, create new ArrayList
        } else {
            registeredServices.get(reconciliationTask.getPriority())
                    .add(reconciliationTask);
        }
    }
    @Override public SettableFuture<NodeId> startReconciliationTask(NodeId nodeId) {

        LOG.info("In startReconciliationTask for node {}", nodeId);
        SettableFuture<NodeId> future;
        if(futureMap.containsKey(nodeId)) {
            future = futureMap.get(nodeId);
        } else {
            future = SettableFuture.create();
            futureMap.put(nodeId,future);
        }
        ReconciliationTask reconciliationJob = new ReconciliationTask(nodeId, future);
        Future<SettableFuture<NodeId>> futureTask = executor.submit(reconciliationJob);
        //            ListenableFuture<NodeId> listenable = JdkFutureAdapters.listenInPoolThread(futureTask);
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
    public void cancelReconciliationTask(NodeId nodeId) {
        LOG.info("In cancelReconciliationTask for node {}", nodeId);
        CancelReconciliationTask cancelReconciliationJob = new CancelReconciliationTask(nodeId, futureMap.get(nodeId));
        if (futureMap.containsKey(nodeId)) {
            futureMap.get(nodeId).cancel(true);
            LOG.info("cancelled the future for node {}", nodeId);
            futureMap.remove(nodeId);
        }
    }

    @Override
    public Map<Integer, List<IReconciliationTaskFactory>> getRegisteredServices() {
        return registeredServices;
    }

    public class CancelReconciliationTask implements Runnable{

        NodeId nodeId;
        SettableFuture<NodeId> future;

        public CancelReconciliationTask(final NodeId nodeId, SettableFuture<NodeId> future) {
            this.nodeId = nodeId;
            this.future = future;
        }
        @Override
        public void run() {
            for(Map.Entry<Integer,List<IReconciliationTaskFactory>> registeredService : registeredServices.entrySet())
            {
                List<IReconciliationTaskFactory> services = registeredService.getValue();
                for(IReconciliationTaskFactory service  : services)
                {
                    LOG.info("Calling createReconcileTask with node{}",nodeId);
                    service.cancelReconcileTask(nodeId);
                }
            }

        }
    }


}

