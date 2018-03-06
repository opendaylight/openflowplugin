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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.applications.southboundcli.util.OFNode;
import org.opendaylight.openflowplugin.applications.southboundcli.util.ShellUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.AdminReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ExecReconciliationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ExecReconciliationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.ReconciliationCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.admin.reconciliation.service.rev180227.reconciliation.counter.ReconcileCounterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.InitReconciliationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.InitReconciliationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.InitReconciliationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminReconciliationServiceImpl implements AdminReconciliationService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminReconciliationServiceImpl.class);
    private static final Long INITIAL_ID = 1L;
    private final DataBroker broker;
    private final ReconciliationService reconciliationService;

    public AdminReconciliationServiceImpl(DataBroker broker, ReconciliationService reconciliationService) {
        this.broker = broker;
        this.reconciliationService = reconciliationService;
    }


    @Override
    public Future<RpcResult<ExecReconciliationOutput>> execReconciliation(ExecReconciliationInput input) {
        boolean reconcileAllNodes = input.isReconcileAllNodes();
        List<BigInteger> inputNodes = input.getNodes();
        if (inputNodes == null) {
            inputNodes = new ArrayList<>();
        }
        if (reconcileAllNodes && inputNodes.size() > 0) {
            return buildErrorResponse("Error executing command execReconciliation."
                    + "If 'all' option is enabled, no Node must be specified as input parameter.");
        }
        if (!reconcileAllNodes && inputNodes.size() == 0) {
            return buildErrorResponse("Error executing command execReconciliation. No Node information was specified.");
        }
        SettableFuture<RpcResult<ExecReconciliationOutput>> result = SettableFuture.create();
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
                LOG.info("Executing admin reconciliation for node {}", nodeId);
                BigInteger node = new BigInteger(String.valueOf(nodeId));
                increaseNumberAdminReconcile(node);
                NodeKey nodeKey = new NodeKey(new NodeId("openflow:" + nodeId));
                InitReconciliationInput initReconInput = new InitReconciliationInputBuilder()
                        .setNodeId(node).setNode(new NodeRef(InstanceIdentifier.builder(Nodes.class)
                                .child(Node.class, nodeKey).build())).build();
                Future<RpcResult<InitReconciliationOutput>> initReconOutput = reconciliationService
                        .initReconciliation(initReconInput);
                try {
                    RpcResult<InitReconciliationOutput> rpcResult = initReconOutput.get();
                    if (rpcResult.isSuccessful()) {
                        LOG.info("Reconciliation successfully completed for node {}", nodeId);
                    } else {
                        LOG.error("Reconciliation failed for node {} with error {}", nodeId, rpcResult.getErrors());
                    }
                } catch (ExecutionException | InterruptedException e) {
                    LOG.error("Error occurred while invoking execReconciliation RPC for node {}", nodeId, e);
                }
            }
        } else {
            return buildErrorResponse("No node found");
        }
        result.set(RpcResultBuilder.<ExecReconciliationOutput>success().build());
        return result;
    }

    private Future<RpcResult<ExecReconciliationOutput>> buildErrorResponse(String msg) {
        SettableFuture<RpcResult<ExecReconciliationOutput>> result = SettableFuture.create();
        LOG.error(msg);
        RpcError error = RpcResultBuilder.newError(RpcError.ErrorType.PROTOCOL, "execReconciliation", msg);
        List<RpcError> errors = Collections.singletonList(error);
        result.set(RpcResultBuilder.<ExecReconciliationOutput>failed().withRpcErrors(errors).build());
        return result;
    }

    public List<Long> getAllNodes() {
        List<OFNode> nodeList = ShellUtil.getAllNodes(broker);
        List<Long> nodes = nodeList.stream().distinct().map(node -> node.getNodeId()).collect(Collectors.toList());
        return nodes;
    }

    public void increaseNumberAdminReconcile(BigInteger nodeId) {
        Long counterValue;
        InstanceIdentifier<ReconcileCounter> instanceIdentifier = InstanceIdentifier
                .builder(ReconciliationCounter.class).child(ReconcileCounter.class,
                        new ReconcileCounterKey(nodeId)).build();
        ReconcileCounterBuilder counterBuilder = new ReconcileCounterBuilder()
                .setKey(new ReconcileCounterKey(nodeId)).setNodeId(nodeId).setNumberAdminReconciliation(INITIAL_ID);
        ReadWriteTransaction tx = broker.newReadWriteTransaction();
        Optional<ReconcileCounter> optional = readReconcileCounterFromDS(tx, instanceIdentifier, nodeId);
        if (optional.isPresent()) {
            ReconcileCounter count = optional.get();
            counterValue = count.getNumberAdminReconciliation();
            counterBuilder.setNumberAdminReconciliation(++counterValue);
            LOG.info("The vale of counter value: {}",counterValue);
        }
        try {
            tx.merge(LogicalDatastoreType.OPERATIONAL, instanceIdentifier, counterBuilder.build(), true);
            tx.submit().checkedGet();
        } catch (TransactionCommitFailedException e) {
            LOG.error("Exception while processing node {}", nodeId, e);
        }
    }

    private Optional<ReconcileCounter> readReconcileCounterFromDS(ReadWriteTransaction tx,
                InstanceIdentifier<ReconcileCounter> instanceIdentifier, BigInteger nodeId) {
        try {
            return tx.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception while processing node : {} {}", nodeId, e);
        }
        return Optional.absent();
    }
}

