/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.nodereconciliation.impl;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.opendaylight.openflowplugin.applications.frm.nodereconciliation.api.UpgradeManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.upgrade.rev180328.FinishUpgradeReconciliationNodesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.upgrade.rev180328.FinishUpgradeReconciliationNodesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.upgrade.rev180328.OpenflowUpgradeService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FinishUpgradeReconciliationImpl implements OpenflowUpgradeService {

    private static final Logger LOG = LoggerFactory.getLogger(FinishUpgradeReconciliationImpl.class);
    private static final String SEPARATOR = ":";

    private final UpgradeManager upgradeManager;

    public FinishUpgradeReconciliationImpl(UpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    @Override
    public Future<RpcResult<FinishUpgradeReconciliationNodesOutput>> finishUpgradeReconciliationNodes(
            FinishUpgradeReconciliationNodesInput input) {
        boolean finishUpgradeAllNodes = input.isFinishUpgradeAllNodes();
        final List<ListenableFuture<RpcResult<Void>>> allResults = new ArrayList<>();
        List<BigInteger> inputNodes = input.getNodes();
        if (inputNodes == null) {
            inputNodes = new ArrayList<>();
        }
        if (!finishUpgradeAllNodes && inputNodes.size() == 0) {
            return buildErrorResponse("Error executing command finishUpgradeReconciliationNodes."
                    + " No Node information was specified.");
        }
        if (finishUpgradeAllNodes) {
            if (inputNodes.size() > 0) {
                return buildErrorResponse("Error executing command finishUpgradeReconciliationNodes."
                        + "If 'all' option is enabled, no Node must be specified as input parameter.");
            } else {
                allResults.addAll(this.upgradeManager.commitAllActiveBundles());
            }
        } else {
            List<Long> nodes = inputNodes.stream().distinct().map(node -> node.longValue())
                    .collect(Collectors.toList());
            if (nodes.size() > 0) {
                for (Long nodeId : nodes) {
                    LOG.info("Executing admin reconciliation for node c{}", nodeId);
                    Node nodeDpn = buildNode(nodeId);
                    InstanceIdentifier<FlowCapableNode> connectedNode = InstanceIdentifier.builder(Nodes.class)
                            .child(Node.class, nodeDpn.getKey()).augmentation(FlowCapableNode.class).build();
                    allResults.add(JdkFutureAdapters
                            .listenInPoolThread(this.upgradeManager.commitActiveBundle(connectedNode)));
                }
            } else {
                return buildErrorResponse("No node found");
            }
        }
        ListenableFuture<RpcResult<FinishUpgradeReconciliationNodesOutput>> singleVoidAddResult = Futures.transform(
                Futures.allAsList(allResults),
                this.<Void>createRpcResultCondenser("commit active bundles"),
                MoreExecutors.directExecutor());
        return singleVoidAddResult;
    }

    private Node buildNode(long nodeIid) {
        NodeId nodeId = new NodeId("openflow:" + nodeIid);
        Node nodeDpn = new NodeBuilder().setId(nodeId).setKey(new NodeKey(nodeId)).build();
        return nodeDpn;
    }

    private Future<RpcResult<FinishUpgradeReconciliationNodesOutput>> buildErrorResponse(String msg) {
        SettableFuture<RpcResult<FinishUpgradeReconciliationNodesOutput>> result = SettableFuture.create();
        LOG.error(msg);
        RpcError error = RpcResultBuilder.newError(RpcError.ErrorType.PROTOCOL, "execReconciliation", msg);
        List<RpcError> errors = Collections.singletonList(error);
        result.set(RpcResultBuilder.<FinishUpgradeReconciliationNodesOutput>failed().withRpcErrors(errors).build());
        return result;
    }

    public static <D> Function<List<RpcResult<D>>,
            RpcResult<FinishUpgradeReconciliationNodesOutput>> createRpcResultCondenser(final String action) {
        return input -> {
            final RpcResultBuilder<FinishUpgradeReconciliationNodesOutput> resultSink;
            if (input != null) {
                List<RpcError> errors = new ArrayList<>();
                for (RpcResult<D> rpcResult : input) {
                    if (!rpcResult.isSuccessful()) {
                        errors.addAll(rpcResult.getErrors());
                    }
                }
                if (errors.isEmpty()) {
                    resultSink = RpcResultBuilder.success();
                } else {
                    resultSink = RpcResultBuilder.<FinishUpgradeReconciliationNodesOutput>failed()
                            .withRpcErrors(errors);
                }
            } else {
                resultSink = RpcResultBuilder.<FinishUpgradeReconciliationNodesOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "previous " + action + " failed");
            }
            return resultSink.build();
        };
    }
}
