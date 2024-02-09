/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.southboundcli;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.COMPLETED;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.FAILED;
import static org.opendaylight.openflowplugin.api.openflow.ReconciliationState.ReconciliationStatus.STARTED;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.FlowGroupCacheManager;
import org.opendaylight.openflowplugin.api.openflow.ReconciliationState;
import org.opendaylight.openflowplugin.applications.frm.FlowNodeReconciliation;
import org.opendaylight.openflowplugin.applications.frm.ForwardingRulesManager;
import org.opendaylight.openflowplugin.applications.southboundcli.alarm.AlarmAgent;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.ErrorTag;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationServiceImpl implements ReconciliationService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationServiceImpl.class);

    private final DataBroker broker;
    private final FlowNodeReconciliation flowNodeReconciliation;
    private final AlarmAgent alarmAgent;
    private final NodeListener nodeListener;
    private final Map<String, ReconciliationState> reconciliationStates;

    private ExecutorService executor = Executors.newWorkStealingPool(10);

    public ReconciliationServiceImpl(final DataBroker broker, final ForwardingRulesManager frm,
            final AlarmAgent alarmAgent, final NodeListener nodeListener,
            final FlowGroupCacheManager flowGroupCacheManager) {
        this.broker = requireNonNull(broker);
        flowNodeReconciliation = frm.getFlowNodeReconciliation();
        this.alarmAgent = requireNonNull(alarmAgent);
        this.nodeListener = requireNonNull(nodeListener);
        reconciliationStates = flowGroupCacheManager.getReconciliationStates();
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    @Override
    public ListenableFuture<RpcResult<ReconcileOutput>> reconcile(final ReconcileInput input) {
        boolean reconcileAllNodes = input.getReconcileAllNodes();
        Set<Uint64> inputNodes = input.getNodes();
        if (inputNodes == null) {
            inputNodes = Set.of();
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
                inputNodes.stream().distinct().map(Uint64::longValue).collect(Collectors.toList());
        if (nodesToReconcile.size() > 0) {
            List<Long> unresolvedNodes =
                    nodesToReconcile.stream().filter(node -> !nodeList.contains(node)).collect(Collectors.toList());
            if (!unresolvedNodes.isEmpty()) {
                return buildErrorResponse("Error executing command reconcile. "
                        + "Node(s) not found: " + String.join(", ", unresolvedNodes.toString()));
            }
            ImmutableSet.Builder<Uint64> inprogressNodes = ImmutableSet.builder();
            nodesToReconcile.parallelStream().forEach(nodeId -> {
                ReconciliationState state = getReconciliationState(nodeId);
                if (state != null && state.getState().equals(STARTED)) {
                    inprogressNodes.add(Uint64.valueOf(nodeId));
                } else {
                    alarmAgent.raiseNodeReconciliationAlarm(nodeId);
                    LOG.info("Executing reconciliation for node {} with state ", nodeId);
                    NodeKey nodeKey = new NodeKey(new NodeId("openflow:" + nodeId));
                    ReconciliationTask reconcileTask = new ReconciliationTask(Uint64.valueOf(nodeId), nodeKey);
                    executor.execute(reconcileTask);
                }
            });
            ReconcileOutput reconcilingInProgress = new ReconcileOutputBuilder()
                    .setInprogressNodes(inprogressNodes.build())
                    .build();
            result.set(RpcResultBuilder.success(reconcilingInProgress).build());
            return result;
        } else {
            return buildErrorResponse("Error executing command reconcile. "
                    + "No node information is found for reconciliation");
        }
    }

    private ReconciliationState getReconciliationState(final Long nodeId) {
        return reconciliationStates.get(nodeId.toString());
    }

    private static ListenableFuture<RpcResult<ReconcileOutput>> buildErrorResponse(final String msg) {
        LOG.error("Error {}", msg);
        return RpcResultBuilder.<ReconcileOutput>failed()
                .withError(ErrorType.PROTOCOL, new ErrorTag("reconcile"), msg)
                .buildFuture();
    }

    private List<Long> getAllNodes() {
        List<OFNode> nodeList = ShellUtil.getAllNodes(nodeListener);
        List<Long> nodes = nodeList.stream().distinct().map(OFNode::getNodeId).collect(Collectors.toList());
        return nodes;
    }

    private final class ReconciliationTask implements Runnable {
        private static final String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        private final NodeKey nodeKey;
        private final Uint64 nodeId;

        private ReconciliationTask(final Uint64 nodeId, final NodeKey nodeKey) {
            this.nodeId = nodeId;
            this.nodeKey = nodeKey;
        }

        @Override
        public void run() {
            updateReconciliationState(STARTED);
            final var reconOutput = flowNodeReconciliation.reconcileConfiguration(
                InstanceIdentifier.create(Nodes.class)
                    .child(Node.class, nodeKey)
                    .augmentation(FlowCapableNode.class));
            try {
                final var rpcResult = reconOutput.get();
                if (rpcResult) {
                    increaseReconcileCount(true);
                    updateReconciliationState(COMPLETED);
                    LOG.info("Reconciliation successfully completed for node {}", nodeId);
                } else {
                    increaseReconcileCount(false);
                    updateReconciliationState(FAILED);
                    LOG.error("Reconciliation failed for node {} with error {}", nodeId);
                }
            } catch (ExecutionException | InterruptedException e) {
                increaseReconcileCount(false);
                updateReconciliationState(FAILED);
                LOG.error("Error occurred while invoking reconcile RPC for node {}", nodeId, e);
            } finally {
                alarmAgent.clearNodeReconciliationAlarm(nodeId.longValue());
            }
        }

        private void increaseReconcileCount(final boolean isSuccess) {
            // FIXME: do not use SimpleDateFormat
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
                    long successCount = count.orElseThrow().getSuccessCount().toJava();
                    counterBuilder.setSuccessCount(Uint32.valueOf(++successCount));
                    LOG.debug("Reconcile success count {} for the node: {} ", successCount, nodeId);
                } else {
                    counterBuilder.setSuccessCount(Uint32.ONE);
                }
            } else if (count.isPresent()) {
                long failureCount = count.orElseThrow().getFailureCount().toJava();
                counterBuilder.setFailureCount(Uint32.valueOf(++failureCount));
                LOG.debug("Reconcile failure count {} for the node: {} ", failureCount, nodeId);
            } else {
                counterBuilder.setFailureCount(Uint32.ONE);
            }
            try {
                tx.mergeParentStructureMerge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier,
                        counterBuilder.build());
                tx.commit().get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while submitting counter for {}", nodeId, e);
            }
        }

        private Optional<ReconcileCounter> getReconciliationCount(final ReadWriteTransaction tx,
                final InstanceIdentifier<ReconcileCounter> instanceIdentifier) {
            try {
                return tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier).get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("Exception while reading counter for node: {}", nodeId, e);
            }
            return Optional.empty();
        }

        private void updateReconciliationState(final ReconciliationState.ReconciliationStatus status) {
            ReconciliationState state = new ReconciliationState(status, LocalDateTime.now());
            reconciliationStates.put(nodeId.toString(),state);
        }
    }
}

