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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by eknnosd on 5/24/2017.
 */
public class ReconciliationManager implements IReconciliationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationManager.class);

    static Map<Integer,List<IReconciliationTaskFactory>> registeredServices = new TreeMap<>();
    private final DataBroker dataService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private  SettableFuture<NodeId> future = SettableFuture.create();


    public ReconciliationManager(DataBroker dataService) {
        this.dataService = dataService;
    }


    public void start() {
        LOG.info("{} start", getClass().getSimpleName());
    }


    private class ReconciliationTask implements Runnable {

        NodeId nodeId;
        public ReconciliationTask(final NodeId nodeId) {
            this.nodeId = nodeId;
        }
        @Override
        public void run() {
            LOG.info("In ReconciliationTask for services{}",registeredServices);
            List <Future> list = new ArrayList<>();
            for(Map.Entry<Integer,List<IReconciliationTaskFactory>> registeredService : registeredServices.entrySet())
            {
                List<IReconciliationTaskFactory> services = registeredService.getValue();
                for(IReconciliationTaskFactory service  : services)
                {
                    LOG.info("Calling createReconcileTask with node{}",nodeId);
                    list.add(service.createReconcileTask(nodeId));
                }
                waitfortasks(list);
            }
            future.set(nodeId);
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

    @Override
    public void registerService(IReconciliationTaskFactory reconciliationTask) {
        LOG.info("Forwarding rules mgr registered");
        if (registeredServices.get(reconciliationTask.getpriority()) == null) { //gets the value for an id)
            registeredServices.put(reconciliationTask.getpriority(), new ArrayList<IReconciliationTaskFactory>()); //no ArrayList assigned, create new ArrayList
            registeredServices.get(reconciliationTask.getpriority()).add(reconciliationTask); //adds value to list.
//            FlowCapableNode node = new FlowCapableNodeBuilder().build();
//            final InstanceIdentifier<FlowCapableNode> fcnIID;
//            NodeId nodeId = new NodeId("123");
//            fcnIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)).augmentation
//                    (FlowCapableNode.class);
//            ReconciliationTask reconciliationJob = new ReconciliationTask(fcnIID);
        }
    }
        @Override
        public ListenableFuture<NodeId> startReconciliationTask(NodeId nodeId) {

            LOG.info("In startReconciliationTask for node {}", nodeId);
            ReconciliationTask reconciliationJob = new ReconciliationTask(nodeId);
            Future futureTask = executor.submit(reconciliationJob);
//            ListenableFuture<NodeId> listenable = JdkFutureAdapters.listenInPoolThread(futureTask);
            return future;
        }

    @Override
    public void cancelReconciliationTask(NodeId nodeId) {
        LOG.info("In cancelReconciliationTask for node {}", nodeId);
        CancelReconciliationTask cancelReconciliationJob= new CancelReconciliationTask(nodeId);
        future.cancel(true);
    }

    @Override
    public Map<Integer, List<IReconciliationTaskFactory>> getRegisteredServices() {
        return registeredServices;
    }

    public class CancelReconciliationTask implements Runnable{

        NodeId nodeId;
        public CancelReconciliationTask(final NodeId nodeId) {
            this.nodeId = nodeId;
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

