/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.applications.southboundcli.alarm.AlarmAgent;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.FrmReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.frm.reconciliation.service.rev180227.ReconcileNodeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationServiceImpl implements ReconciliationService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationServiceImpl.class);
    private final DataBroker broker;
    private final FrmReconciliationService frmReconciliationService;
    private final Long startCount = 1L;
    private final AlarmAgent alarmAgent;
    private static final int THREAD_POOL_SIZE = 10;
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public ReconciliationServiceImpl(final DataBroker broker, final FrmReconciliationService frmReconciliationService,
                                     final AlarmAgent alarmAgent) {
        this.broker = broker;
        this.frmReconciliationService = frmReconciliationService;
        this.alarmAgent = alarmAgent;
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public ListenableFuture<RpcResult<ReconcileOutput>> reconcile(ReconcileInput input) {
        boolean reconcileAllNodes = input.isReconcileAllNodes();
        List<BigInteger> inputNodes = input.getNodes();
        if (inputNodes == null) {
            inputNodes = new ArrayList<>();
        }
        if (reconcileAllNodes && inputNodes.size() > 0) {
            return buildErrorResponse("Error executing command reconcile. "
                    + "If 'all' option is enabled, no Node must be specified as input parameter.");
        }
        if (!reconcileAllNodes && inputNodes.size() == 0) {
            return buildErrorResponse("Error executing command reconcile. No Node information was specified.");
        }
        SettableFuture<RpcResult<ReconcileOutput>> result = SettableFuture.create();
        List<Long> nodeList = getAllNodes();
        List<Long> nodesToReconcile = reconcileAllNodes ? nodeList :
                inputNodes.stream().distinct().map(node -> node.longValue()).collect(Collectors.toList());
        if (nodesToReconcile.size() > 0) {
            List<Long> unresolvedNodes =
                    nodesToReconcile.stream().filter(node -> !nodeList.contains(node)).collect(Collectors.toList());
            if (!unresolvedNodes.isEmpty()) {
                return buildErrorResponse("Error executing command reconcile. "
                        + "Node(s) not found: " + String.join(", ", unresolvedNodes.toString()));
            }
            nodesToReconcile.parallelStream().forEach(nodeId -> {
                alarmAgent.raiseNodeReconciliationAlarm(nodeId);
                LOG.info("Executing reconciliation for node {}", nodeId);
                NodeKey nodeKey = new NodeKey(new NodeId("openflow:" + nodeId));
                ReconciliationTask reconcileTask = new ReconciliationTask(nodeId, nodeKey);
                executor.execute(reconcileTask);
            });
        } else {
            return buildErrorResponse("Error executing command reconcile. "
                    + "No node information is found for reconciliation");
        }
        result.set(RpcResultBuilder.<ReconcileOutput>success().build());
        return result;
    }

    private ListenableFuture<RpcResult<ReconcileOutput>> buildErrorResponse(String msg) {
        SettableFuture<RpcResult<ReconcileOutput>> result = SettableFuture.create();
        LOG.error(msg);
        RpcError error = RpcResultBuilder.newError(RpcError.ErrorType.PROTOCOL, "reconcile", msg);
        result.set(RpcResultBuilder.<ReconcileOutput>failed().withRpcError(error).build());
        return result;
    }

    public List<Long> getAllNodes() {
        List<OFNode> nodeList = ShellUtil.getAllNodes(broker);
        List<Long> nodes = nodeList.stream().distinct().map(node -> node.getNodeId()).collect(Collectors.toList());
        return nodes;
    }

    private void increaseReconcileCount(BigInteger nodeId, Boolean reconcileState) {
        InstanceIdentifier<ReconcileCounter> instanceIdentifier = InstanceIdentifier
                .builder(ReconciliationCounter.class).child(ReconcileCounter.class,
                        new ReconcileCounterKey(nodeId)).build();
        ReadWriteTransaction tx = broker.newReadWriteTransaction();
        Optional<ReconcileCounter> optional = readReconcileCounterFromDS(tx, instanceIdentifier, nodeId);
        ReconcileCounterBuilder counterBuilder = new ReconcileCounterBuilder()
                .withKey(new ReconcileCounterKey(nodeId)).setNodeId(nodeId)
                .setLastRequestTime(LocalDateTime.now().toString());
        if (reconcileState) {
            counterBuilder.setSuccessCount(startCount);
            if (optional.isPresent()) {
                ReconcileCounter counter = optional.get();
                Long successCount = counter.getSuccessCount();
                counterBuilder.setSuccessCount(++successCount);
                LOG.debug("Reconcile Success count {} for the node: {} ", successCount, nodeId);
            }
        } else {
            counterBuilder.setFailureCount(startCount);
            if (optional.isPresent()) {
                ReconcileCounter counter = optional.get();
                Long failureCount = counter.getFailureCount();
                counterBuilder.setFailureCount(++failureCount);
                LOG.debug("Reconcile Failure count {} for the node: {} ", failureCount, nodeId);
            }
        }
        try {
            tx.merge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier, counterBuilder.build(), true);
            tx.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception while submitting counter {}", nodeId, e);
        }
    }

    private Optional<ReconcileCounter> readReconcileCounterFromDS(ReadWriteTransaction tx,
                InstanceIdentifier<ReconcileCounter> instanceIdentifier, BigInteger nodeId) {
        try {
            return tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception while reading counter for node: {}", nodeId, e);
        }
        return Optional.absent();
    }

    private final class ReconciliationTask implements Runnable {
        private final NodeKey nodeKey;
        private final Long nodeId;

        private ReconciliationTask(Long nodeId, NodeKey nodeKey) {
            this.nodeId = nodeId;
            this.nodeKey = nodeKey;
        }

        @Override
        public void run() {
            BigInteger node = new BigInteger(String.valueOf(nodeId));
            ReconcileNodeInput reconInput = new ReconcileNodeInputBuilder()
                    .setNodeId(node).setNode(new NodeRef(InstanceIdentifier.builder(Nodes.class)
                            .child(Node.class, nodeKey).build())).build();
            Future<RpcResult<ReconcileNodeOutput>> reconOutput = frmReconciliationService
                    .reconcileNode(reconInput);
            try {
                RpcResult<ReconcileNodeOutput> rpcResult = reconOutput.get();
                if (rpcResult.isSuccessful()) {
                    increaseReconcileCount(node, true);
                    LOG.info("Reconciliation successfully completed for node {}", nodeId);
                } else {
                    increaseReconcileCount(node, false);
                    LOG.error("Reconciliation failed for node {} with error {}", nodeId, rpcResult.getErrors());
                }
            } catch (ExecutionException | InterruptedException e) {
                increaseReconcileCount(false);
                updateReconciliationState(FAILED);
                LOG.error("Error occurred while invoking reconcile RPC for node {}", this.nodeId, e);
            } finally {
                alarmAgent.clearNodeReconciliationAlarm(nodeId.longValue());
            }
        }

        private void increaseReconcileCount(final boolean isSuccess) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
            InstanceIdentifier<ReconcileCounter> instanceIdentifier = InstanceIdentifier
                    .builder(ReconciliationCounter.class).child(ReconcileCounter.class,
                            new ReconcileCounterKey(nodeId)).build();
            ReadWriteTransaction tx = broker.newReadWriteTransaction();
            Optional<ReconcileCounter> count = getReconciliationCount(tx, instanceIdentifier);
            ReconcileCounterBuilder counterBuilder = new ReconcileCounterBuilder()
                    .withKey(new ReconcileCounterKey(nodeId))
                    .setLastRequestTime(new DateAndTime(simpleDateFormat.format(new Date())));

            if (isSuccess) {
                if (count.isPresent()) {
                    Long successCount = count.get().getSuccessCount();
                    counterBuilder.setSuccessCount(++successCount);
                    LOG.debug("Reconcile success count {} for the node: {} ", successCount, nodeId);
                } else {
                    counterBuilder.setSuccessCount(startCount);
                }
            } else {
                if (count.isPresent()) {
                    Long failureCount = count.get().getFailureCount();
                    counterBuilder.setFailureCount(++failureCount);
                    LOG.debug("Reconcile failure count {} for the node: {} ", failureCount, nodeId);
                } else {
                    counterBuilder.setFailureCount(startCount);
                }
            }
            try {
                tx.merge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier, counterBuilder.build(), true);
                tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while submitting counter for {}", nodeId, e);
            }
        }

        private Optional<ReconcileCounter> getReconciliationCount(ReadWriteTransaction tx,
                                InstanceIdentifier<ReconcileCounter> instanceIdentifier) {
            try {
                return tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier).get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while reading counter for node: {}", nodeId, e);
            }
            return Optional.absent();
        }

        private void updateReconciliationState(State state) {
            ReadWriteTransaction tx = broker.newReadWriteTransaction();
            InstanceIdentifier<ReconciliationStateList> instanceIdentifier = InstanceIdentifier
                    .builder(ReconciliationState.class).child(ReconciliationStateList.class,
                            new ReconciliationStateListKey(nodeId)).build();
            ReconciliationStateListBuilder stateBuilder = new ReconciliationStateListBuilder()
                    .withKey(new ReconciliationStateListKey(nodeId))
                    .setState(state);
            try {
                tx.merge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier, stateBuilder.build(), true);
                tx.submit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while updating reconciliation state: {}", nodeId, e);
            }
>>>>>>> 9198accbc... Refactoring the code for updating the reconciliation state
        }
    }
}

