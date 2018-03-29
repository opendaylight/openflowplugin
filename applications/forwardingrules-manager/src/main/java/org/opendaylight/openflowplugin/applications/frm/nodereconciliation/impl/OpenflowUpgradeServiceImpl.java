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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.frm.nodereconciliation.api.UpgradeManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.openflow.upgrade.service.rev180227.FinishUpgradeReconciliationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.openflow.upgrade.service.rev180227.FinishUpgradeReconciliationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.openflow.upgrade.service.rev180227.OpenflowUpgradeService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowUpgradeServiceImpl implements OpenflowUpgradeService {

    private static final Logger LOG = LoggerFactory.getLogger(OpenflowUpgradeServiceImpl.class);

    private final UpgradeManager upgradeManager;

    public OpenflowUpgradeServiceImpl(UpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    @Override
    public Future<RpcResult<FinishUpgradeReconciliationOutput>> finishUpgradeReconciliation(
            FinishUpgradeReconciliationInput input) {
        Long nodeId = input.getNode().longValue();
        LOG.info("Stopping upgrade reconciliation for node {}", nodeId);
        Node nodeDpn = buildNode(nodeId);
        InstanceIdentifier<FlowCapableNode> connectedNode = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeDpn.getKey()).augmentation(FlowCapableNode.class).build();
        ListenableFuture<RpcResult<Void>> rpcResult = JdkFutureAdapters
                .listenInPoolThread(this.upgradeManager.commitActiveBundle(connectedNode));

        ListenableFuture<RpcResult<FinishUpgradeReconciliationOutput>> result = Futures.transform(
                rpcResult,
                this.<Void>createRpcResultCondenser("commit active bundle"),
                MoreExecutors.directExecutor());
        return result;
    }

    private Node buildNode(long nodeIid) {
        NodeId nodeId = new NodeId("openflow:" + nodeIid);
        Node nodeDpn = new NodeBuilder().setId(nodeId).setKey(new NodeKey(nodeId)).build();
        return nodeDpn;
    }

    private Future<RpcResult<FinishUpgradeReconciliationOutput>> buildErrorResponse(String msg) {
        SettableFuture<RpcResult<FinishUpgradeReconciliationOutput>> result = SettableFuture.create();
        LOG.error(msg);
        RpcError error = RpcResultBuilder.newError(RpcError.ErrorType.PROTOCOL, "stopUpgradeReconciliation", msg);
        List<RpcError> errors = Collections.singletonList(error);
        result.set(RpcResultBuilder.<FinishUpgradeReconciliationOutput>failed().withRpcErrors(errors).build());
        return result;
    }

    public static <D> Function<RpcResult<D>,
        RpcResult<FinishUpgradeReconciliationOutput>> createRpcResultCondenser(final String action) {
        return input -> {
            final RpcResultBuilder<FinishUpgradeReconciliationOutput> resultSink;
            if (input != null) {
                List<RpcError> errors = new ArrayList<>();
                if (!input.isSuccessful()) {
                    errors.addAll(input.getErrors());
                    resultSink = RpcResultBuilder.<FinishUpgradeReconciliationOutput>failed().withRpcErrors(errors);
                } else {
                    resultSink = RpcResultBuilder.success();
                }
            } else {
                resultSink = RpcResultBuilder.<FinishUpgradeReconciliationOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "action of " + action + " failed");
            }
            return resultSink.build();
        };
    }
}
