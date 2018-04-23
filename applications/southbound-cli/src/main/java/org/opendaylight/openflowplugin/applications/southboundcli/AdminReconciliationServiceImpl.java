/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.southboundcli;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.AdminReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ReconcileInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ReconcileOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileNodeInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileNodeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminReconciliationServiceImpl implements AdminReconciliationService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminReconciliationServiceImpl.class);
    private final DataBroker broker;
    private final ReconciliationService reconciliationService;
    private final AlarmAgent alarmAgent;
    private final Long startCount = 1L;

    public AdminReconciliationServiceImpl(final DataBroker broker, final ReconciliationService reconciliationService,
                                          final AlarmAgent alarmAgent) {
        this.broker = broker;
        this.reconciliationService = reconciliationService;
        this.alarmAgent = alarmAgent;
    }


    @Override
    public Future<RpcResult<ReconcileOutput>> reconcile(ReconcileInput input) {
        boolean reconcileAllNodes = input.isReconcileAllNodes();
        List<BigInteger> inputNodes = input.getNodes();
        if (inputNodes == null) {
            inputNodes = new ArrayList<>();
        }
        if (reconcileAllNodes && inputNodes.size() > 0) {
            return buildErrorResponse("Error executing command reconcile."
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
                return buildErrorResponse("Node(s) not found: " + String.join(", ", unresolvedNodes.toString()));
            }
            for (Long nodeId : nodesToReconcile) {
                alarmAgent.raiseAdminReconciliationAlarm(nodeId);
                LOG.info("Executing admin reconciliation for node {}", nodeId);
                BigInteger node = new BigInteger(String.valueOf(nodeId));
                NodeKey nodeKey = new NodeKey(new NodeId("openflow:" + nodeId));
                ReconcileNodeInput reconInput = new ReconcileNodeInputBuilder()
                        .setNodeId(node).setNode(new NodeRef(InstanceIdentifier.builder(Nodes.class)
                                .child(Node.class, nodeKey).build())).build();
                Future<RpcResult<ReconcileNodeOutput>> reconOutput = reconciliationService
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
                    LOG.error("Error occurred while invoking reconcile RPC for node {}", nodeId, e);
                }
                alarmAgent.clearAdminReconciliationAlarm(nodeId);
            }
        } else {
            return buildErrorResponse("No node found");
        }
        result.set(RpcResultBuilder.<ReconcileOutput>success().build());
        return result;
    }

    private Future<RpcResult<ReconcileOutput>> buildErrorResponse(String msg) {
        SettableFuture<RpcResult<ReconcileOutput>> result = SettableFuture.create();
        LOG.error(msg);
        RpcError error = RpcResultBuilder.newError(RpcError.ErrorType.PROTOCOL, "reconcile", msg);
        List<RpcError> errors = Collections.singletonList(error);
        result.set(RpcResultBuilder.<ReconcileOutput>failed().withRpcErrors(errors).build());
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
                .setKey(new ReconcileCounterKey(nodeId)).setNodeId(nodeId)
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
}

