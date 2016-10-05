/*
 * Copyright (c) 2016 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.facade;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.controller.md.sal.common.api.clustering.EntityOwnershipService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.IJobCoordinator;
import org.opendaylight.openflowplugin.applications.jobcoordinator.api.SuccessCallable;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.IPriorityTaskManager;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityLifecycleState;
import org.opendaylight.openflowplugin.applications.prioritytaskmgr.api.task.EntityState;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.ErrorCallable;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.IOpenflowFacade;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.api.OpenflowErrorCause;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.eos.EosOFListener;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTracker;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.transactions.api.TransactionTrackerFactory;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.util.InputBuilder;
import org.opendaylight.openflowplugin.applications.southboundmanager.openflowservice.util.SouthboundManagerUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.RemoveFlowOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionAware;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class OpenflowFacade implements BindingAwareProvider, AutoCloseable, IOpenflowFacade {

    private final Logger LOG = LoggerFactory.getLogger(OpenflowFacade.class);

    private final SalFlowService salFlowService;
    private final SalGroupService salGroupService;
    private final EntityOwnershipService entityOwnershipService;
    private final TransactionTrackerFactory transactionTrackerFactory;
    private final IJobCoordinator jobCoordinator;
    private final IPriorityTaskManager resyncTaskManager;


    public OpenflowFacade(RpcProviderRegistry rpcProviderRegistry,
                          EntityOwnershipService entityOwnershipService,
                          IJobCoordinator jobCoordinator,
                          TransactionTrackerFactory txTrackerFactory,
                          IPriorityTaskManager resyncTaskManager) {
        this.salFlowService = rpcProviderRegistry.getRpcService(SalFlowService.class);
        this.salGroupService = rpcProviderRegistry.getRpcService(SalGroupService.class);
        this.entityOwnershipService = entityOwnershipService;
        this.transactionTrackerFactory = txTrackerFactory;
        this.jobCoordinator = jobCoordinator;
        this.resyncTaskManager = resyncTaskManager;

    }

    @Override
    public void close() throws Exception {
        LOG.info("OpenFlowFacade shutting down");
        try {
            transactionTrackerFactory.deregisterNotificationListener();
        } catch (Exception e) {
            LOG.error("Cannot deregister notification service: {}", e.getMessage());
        }
    }

    @Override
    public void onSessionInitiated(BindingAwareBroker.ProviderContext providerContext) {
        LOG.info("OpenFlowFacade Initiated");
        EosOFListener eosOFListener = new EosOFListener(transactionTrackerFactory,
                resyncTaskManager);
        try {
            this.entityOwnershipService.registerListener(SouthboundManagerUtil.SWITCH_APPENDER,
                    eosOFListener);
            LOG.info("Registered notification listener.");
        } catch (Exception e) {
            LOG.error("Exception caught while EOS Listener registration: {}", e.getMessage());
        }

        try {
            NotificationService notificationService = providerContext.getSALService(NotificationService.class);
            transactionTrackerFactory.registerNotificationListener(notificationService);
        } catch (Exception e) {
            LOG.error("Unable to register Notification Listener. Exception: {}", e.getMessage());
        }
    }

    /*
     * The following is the implementation of OpenflowFacade API
     */

    @Override
    public void modifyFlow(NodeId nodeId, Flow flow, ErrorCallable callable,
                           boolean isDirectRPC) {
	    if (!isDirectRPC) {
            LOG.info("User trying to use isDirectRPC: false. BAD USER");
            throw new UnsupportedOperationException();
        }
        modifyFlow(nodeId, flow, callable);
    }

    @Override
    public void deleteFlow(NodeId nodeId, Flow flow, ErrorCallable callable,
                           boolean isDirectRPC) {
	    if (!isDirectRPC) {
            LOG.info("User trying to use isDirectRPC: false. BAD USER");
            throw new UnsupportedOperationException();
        }
        deleteFlow(nodeId, flow, callable);
    }

    @Override
    public void modifyGroup(NodeId nodeId, Group group, ErrorCallable callable,
                            boolean isDirectRPC) {
        if (!isDirectRPC) {
            LOG.info("User trying to use isDirectRPC: false. BAD USER");
            throw new UnsupportedOperationException();
        }
        modifyGroup(nodeId, group,  callable);
    }

    @Override
    public void deleteGroup(NodeId nodeId, Group group, ErrorCallable callable,
                            boolean isDirectRPC) {
        if (!isDirectRPC) {
            LOG.info("User trying to use isDirectRPC: false. BAD USER");
            throw new UnsupportedOperationException();
        }
        deleteGroup(nodeId, group, callable);
    }

    @Override
    public void modifyBundle(NodeId nodeId, TreeMap<Integer, List<Group>> groups, List<Flow> flows,
                             ErrorCallable callable, boolean isDirectRPC) {
        if (!isDirectRPC) {
            LOG.info("User trying to use isDirectRPC: false. BAD USER");
            throw new UnsupportedOperationException();
        }
        modifyBundle(nodeId, groups, flows, callable);
    }

    @Override
    public void deleteBundle(NodeId nodeId, TreeMap<Integer, List<Group>> groups, List<Flow> flows,
                             ErrorCallable callable, boolean isDirectRPC) {
        if (!isDirectRPC) {
            LOG.info("User trying to use isDirectRPC: false. BAD USER");
            throw new UnsupportedOperationException();
        }
        deleteBundle(nodeId, groups, flows, callable);
    }

    @Override
    public void executeResync(NodeId nodeId) {
        if (!Boolean.getBoolean(SouthboundManagerUtil.TRIGGER_RESYNC_VIA_EOS)){
            EntityState entityState = new EntityState(SouthboundManagerUtil.getStringForm(nodeId),
                    EntityLifecycleState.ASSOCIATED);

            resyncTaskManager.sendEntityState(entityState, SouthboundManagerUtil.PRIORITY_TASK_ACTION_TYPE);
        }

    }

    @Override
    public void cancelResync(NodeId nodeId) {
        if (!Boolean.getBoolean(SouthboundManagerUtil.TRIGGER_RESYNC_VIA_EOS)){
            EntityState entityState = new EntityState(SouthboundManagerUtil.getStringForm(nodeId),
                    EntityLifecycleState.DISSOCIATED);

            resyncTaskManager.sendEntityState(entityState, SouthboundManagerUtil.PRIORITY_TASK_ACTION_TYPE);
        }

    }

    private void modifyFlow(NodeId nodeId, Flow flow, ErrorCallable callable) {
        LOG.debug("modifyFlow nodeId: {}, flow: {}", nodeId, flow);
        TransactionTracker txTracker = transactionTrackerFactory.getCacheEntry(nodeId);
        if (txTracker == null) {
            LOG.trace("Transaction Tracker entry not found for nodeId: {}", nodeId);
            return;
        }
        if (callable == null) {
            callable = new DefaultErrorCallable("modifyFlow nodeId: " + nodeId.toString() +
                    ", flow: " + flow.toString());
        }
        if (txTracker.getIsOwner() && txTracker.getHasOwner()) {
            ListenableFuture<RpcResult<AddFlowOutput>> future =
                    (ListenableFuture<RpcResult<AddFlowOutput>>) salFlowService.
                            addFlow(InputBuilder.ADD_FLOW(nodeId, flow).build());
            Futures.addCallback(future, new RpcFutureCallback(txTracker, callable));
        }
    }

    private void deleteFlow(NodeId nodeId, Flow flow, ErrorCallable callable) {
        LOG.debug("deleteFlow nodeId: {}, flow: {}", nodeId, flow);
        TransactionTracker txTracker = transactionTrackerFactory.getCacheEntry(nodeId);
        if (txTracker == null) {
            LOG.trace("Transaction Tracker entry not found for nodeId: {}", nodeId);
            return;
        }
        if (callable == null) {
            callable = new DefaultErrorCallable("deleteFlow nodeId: " + nodeId.toString() +
                    ", flow: " + flow.toString());
        }
        if (txTracker.getIsOwner() && txTracker.getHasOwner()) {
            ListenableFuture<RpcResult<RemoveFlowOutput>> future =
                    (ListenableFuture<RpcResult<RemoveFlowOutput>>) salFlowService.
                            removeFlow(InputBuilder.REMOVE_FLOW(nodeId, flow).build());
            Futures.addCallback(future, new RpcFutureCallback(txTracker, callable));
        }
    }

    private void modifyGroup(NodeId nodeId, Group group, ErrorCallable callable) {
        LOG.debug("modifyGroup nodeId: {}, group: {}", nodeId, group);
        TransactionTracker txTracker = transactionTrackerFactory.getCacheEntry(nodeId);
        if (txTracker == null) {
            LOG.trace("Transaction Tracker entry not found for nodeId: {}", nodeId);
            return;
        }
        if (callable == null) {
            callable = new DefaultErrorCallable("modifyGroup nodeId: " + nodeId.toString() +
                    ", group: " + group.toString());
        }
        if (txTracker.getIsOwner() && txTracker.getHasOwner()) {
            ListenableFuture<RpcResult<AddGroupOutput>> future =
                    (ListenableFuture<RpcResult<AddGroupOutput>>) salGroupService
                            .addGroup(InputBuilder.ADD_GROUP(nodeId, group).build());
            Futures.addCallback(future, new RpcFutureCallback(txTracker, callable));
        }
    }

    private void deleteGroup(NodeId nodeId, Group group, ErrorCallable callable) {
        LOG.debug("deleteGroup nodeId: {}, group: {}", nodeId, group);
        TransactionTracker txTracker = transactionTrackerFactory.getCacheEntry(nodeId);
        if (txTracker == null) {
            LOG.trace("Transaction Tracker entry not found for nodeId: {}", nodeId);
            return;
        }
        if (callable == null) {
            callable = new DefaultErrorCallable("deleteGroup nodeId: " + nodeId.toString() +
                    ", group: " + group.toString());
        }
        if (txTracker.getIsOwner() && txTracker.getHasOwner()) {
            ListenableFuture<RpcResult<RemoveGroupOutput>> future =
                    (ListenableFuture<RpcResult<RemoveGroupOutput>>) salGroupService
                            .removeGroup(InputBuilder.REMOVE_GROUP(nodeId, group).build());
            Futures.addCallback(future, new RpcFutureCallback(txTracker, callable));
        }
    }

    private void modifyBundle(NodeId nodeId, final TreeMap<Integer, List<Group>> groups, final List<Flow> flows,
                              ErrorCallable callable) {
        LOG.debug("modifyBundle nodeId: {} ", nodeId);
        TransactionTracker txTracker = transactionTrackerFactory.getCacheEntry(nodeId);
        if (txTracker == null) {
            LOG.trace("Transaction Tracker entry not found for nodeId: {}", nodeId);
            return;
        }
        if (callable == null) {
            callable = new DefaultErrorCallable("modifyBundle nodeId: " + nodeId.toString());
        }
        if (txTracker.getIsOwner() && txTracker.getHasOwner()) {
            String bundleId = txTracker.generateBundleId();
            if (groups != null) {
                for (Map.Entry<Integer, List<Group>> entry : groups.descendingMap().entrySet()) {
                    jobCoordinator.enqueueJob(bundleId,
                            new MainWorkerForGroup(entry.getValue(), nodeId, true),
                            new SuccessWorker(callable, txTracker),
                            new ErrorWorker(callable), 0);
                }
            }
            if (flows == null || flows.isEmpty()) {
                return;
            }
            jobCoordinator.enqueueJob(bundleId,
                    new MainWorkerForFlow(flows, nodeId, true),
                    new SuccessWorker(callable, txTracker),
                    new ErrorWorker(callable), 0);
        }
    }

    private void deleteBundle(NodeId nodeId, TreeMap<Integer, List<Group>> groups, List<Flow> flows,
                              ErrorCallable callable) {
        LOG.debug("deleteBundle nodeId: {} ", nodeId);
        TransactionTracker txTracker = transactionTrackerFactory.getCacheEntry(nodeId);
        if (txTracker == null) {
            LOG.trace("Transaction Tracker entry not found for nodeId: {}", nodeId);
            return;
        }
        if (callable == null) {
            callable = new DefaultErrorCallable("deleteBundle nodeId: " + nodeId.toString());
        }
        if (txTracker.getIsOwner() && txTracker.getHasOwner()) {
            String bundleId = txTracker.generateBundleId();
            if (groups != null) {
                for (Map.Entry<Integer, List<Group>> entry : groups.entrySet()) {
                    jobCoordinator.enqueueJob(bundleId,
                            new MainWorkerForGroup(entry.getValue(), nodeId, false),
                            new SuccessWorker(callable, txTracker),
                            new ErrorWorker(callable), 0);
                }
            }
            if (flows == null || flows.isEmpty()) {
                return;
            }
            jobCoordinator.enqueueJob(bundleId,
                    new MainWorkerForFlow(flows, nodeId, false),
                    new SuccessWorker(callable, txTracker),
                    new ErrorWorker(callable), 0);
        }
    }

    private class RpcFutureCallback<T extends RpcResult<TransactionAware>> implements FutureCallback<T> {

        private TransactionTracker txTracker;
        private ErrorCallable errorCallable;

        public RpcFutureCallback(TransactionTracker txTracker, ErrorCallable callable) {
            this.txTracker = txTracker;
            this.errorCallable = callable;
        }

        @Override
        public void onSuccess(T result) {
            TransactionId txId = result.getResult().getTransactionId();
            LOG.debug("RPC future success: txId: {}", txId);
            if (txTracker.getTransactionEntry(txId) != null) {
                // Error occurred before the RPC Success callback.
                txTracker.removeTransactionEntry(txId);
                errorCallable.setCause(OpenflowErrorCause.OF_ERROR);
                try {
                    errorCallable.call();
                } catch (Exception e) {
                    LOG.error("Exception caught while executing error rollback (due to RPC failure). Exception: {}",
                            e.getStackTrace());
                }
            } else {
                // It may happen that OF_ERROR comes exactly at this point.
                // In such a case, the following will update the cache entry when it should be executing
                // the above block.
                txTracker.addTransactionEntry(txId, errorCallable);
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            LOG.debug("RPC future failure");
            try {
                errorCallable.setCause(OpenflowErrorCause.OFRPC_ERROR);
                errorCallable.call();
            } catch (Exception e) {
                LOG.error("Exception caught while executing error rollback (due to RPC failure). Exception: {}",
                        e.getStackTrace());
            }
        }
    }

    private class MainWorkerForGroup implements Callable<List<ListenableFuture<? extends Object>>> {

        private final List<Group> groups;
        private final NodeId nodeId;
        private final boolean isAdd;

        public MainWorkerForGroup(List<Group> groups, NodeId nodeId, boolean isAdd) {
            this.groups = groups;
            this.nodeId = nodeId;
            this.isAdd = isAdd;
        }

        @Override
        public List<ListenableFuture<? extends Object>> call() throws Exception {
            List<ListenableFuture<? extends Object>> futures = new ArrayList<>();
            if (isAdd) {
                for (Group group : groups) {
                    ListenableFuture<RpcResult<AddGroupOutput>> future =
                            (ListenableFuture<RpcResult<AddGroupOutput>>) salGroupService
                                    .addGroup(InputBuilder.ADD_GROUP(nodeId, group).build());
                    futures.add(future);
                }
            } else {
                for (Group group : groups) {
                    ListenableFuture<RpcResult<RemoveGroupOutput>> future =
                            (ListenableFuture<RpcResult<RemoveGroupOutput>>) salGroupService
                                    .removeGroup(InputBuilder.REMOVE_GROUP(nodeId, group).build());
                    futures.add(future);
                }
            }
            return futures;
        }
    }

    private class MainWorkerForFlow implements Callable<List<ListenableFuture<? extends Object>>> {

        private final List<Flow> flows;
        private final NodeId nodeId;
        private final boolean isAdd;

        public MainWorkerForFlow(List<Flow> flows, NodeId nodeId, boolean isAdd) {
            this.flows = flows;
            this.nodeId = nodeId;
            this.isAdd = isAdd;
        }

        @Override
        public List<ListenableFuture<? extends Object>> call() throws Exception {
            List<ListenableFuture<? extends Object>> futures = new ArrayList<>();
            if (isAdd) {
                for (Flow flow : flows) {
                    ListenableFuture<RpcResult<AddFlowOutput>> future =
                            (ListenableFuture<RpcResult<AddFlowOutput>>) salFlowService
                                    .addFlow(InputBuilder.ADD_FLOW(nodeId, flow).build());
                    futures.add(future);
                }
            } else {
                for (Flow flow : flows) {
                    ListenableFuture<RpcResult<RemoveFlowOutput>> future =
                            (ListenableFuture<RpcResult<RemoveFlowOutput>>) salFlowService
                                    .removeFlow(InputBuilder.REMOVE_FLOW(nodeId, flow).build());
                    futures.add(future);
                }
            }
            return futures;
        }
    }

    private class SuccessWorker extends SuccessCallable {

        private ErrorCallable errorCallable;
        private TransactionTracker txTracker;

        public SuccessWorker(ErrorCallable errorCallable, TransactionTracker txTracker) {
            this.errorCallable = errorCallable;
            this.txTracker = txTracker;
        }

        @Override
        public List<ListenableFuture<? extends Object>> call() throws Exception {
            List<RpcResult<TransactionAware>> results = (List<RpcResult<TransactionAware>>)getResults();
            for (RpcResult<TransactionAware> result: results) {
                TransactionId txId = result.getResult().getTransactionId();
                LOG.debug("RPC future success: txId: {}", txId);
                if (txTracker.getTransactionEntry(txId) != null) {
                    // Error occurred before the RPC Success callback.
                    if (!errorCallable.getIsCalled()) {
                        errorCallable.setIsCalled(true);
                        txTracker.removeTransactionEntry(txId);
                        errorCallable.setCause(OpenflowErrorCause.OF_ERROR);
                        errorCallable.call();
                    }
                } else {
                    txTracker.addTransactionEntry(txId, errorCallable);
                }
            }
            return null;
        }
    }

    private class ErrorWorker extends org.opendaylight.openflowplugin.applications.jobcoordinator.api.RollbackCallable {

        private ErrorCallable errorCallable;

        public ErrorWorker(ErrorCallable errorCallable) {
            this.errorCallable = errorCallable;
        }

        @Override
        public List<ListenableFuture<? extends Object>> call() throws Exception {
            if (errorCallable.getIsCalled()) {
                return null;
            }
            errorCallable.setIsCalled(true);
            errorCallable.setCause(OpenflowErrorCause.OFRPC_ERROR);
            errorCallable.call();
            return null;
        }
    }

    private class DefaultErrorCallable extends ErrorCallable {

        private final String when;

        public DefaultErrorCallable(String message) {
            this.when = message;
        }

        @Override
        public Object call() throws Exception {
            LOG.error("Error occurred: {}, during: {}", this.getCause(), when);
            return null;
        }
    }
}
