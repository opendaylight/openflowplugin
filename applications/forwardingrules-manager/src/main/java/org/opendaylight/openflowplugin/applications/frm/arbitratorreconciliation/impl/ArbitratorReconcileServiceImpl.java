/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.arbitratorreconciliation.impl;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.openflowplugin.applications.frm.arbitratorreconciliation.ArbitratorReconciliationManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.ArbitratorReconcileService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitBundleNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.arbitrator.reconcile.service.rev180227.CommitBundleNodeOutput;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArbitratorReconcileServiceImpl implements ArbitratorReconcileService {

    private static final Logger LOG = LoggerFactory.getLogger(ArbitratorReconcileServiceImpl.class);
    private static final String OPENFLOW_PREFIX = "openflow:";

    private final ArbitratorReconciliationManager arbitratorReconciliationManager;

    public ArbitratorReconcileServiceImpl(ArbitratorReconciliationManager arbitratorReconciliationManager) {
        this.arbitratorReconciliationManager = Preconditions.checkNotNull(arbitratorReconciliationManager,
                "ArbitratorReconciliationManager cannot be null!");
    }

    @Override
    public Future<RpcResult<CommitBundleNodeOutput>> commitBundleNode(CommitBundleNodeInput input) {
        Long nodeId = input.getNodeId().longValue();
        LOG.trace("Committing active bundles for node {}", nodeId);
        InstanceIdentifier<FlowCapableNode> connectedNode = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(OPENFLOW_PREFIX + String.valueOf(nodeId))))
                .augmentation(FlowCapableNode.class)
                .build();
        ListenableFuture<RpcResult<CommitBundleNodeOutput>> result = Futures.transform(
                JdkFutureAdapters
                        .listenInPoolThread(arbitratorReconciliationManager.commitActiveBundle(connectedNode)),
                this.<Void>createRpcResultCondenser("committed active bundle"),
                MoreExecutors.directExecutor());
        return result;
    }

    public static <D> Function<RpcResult<D>,
            RpcResult<CommitBundleNodeOutput>> createRpcResultCondenser(final String action) {
        return input -> {
            final RpcResultBuilder<CommitBundleNodeOutput> resultSink;
            if (input != null) {
                List<RpcError> errors = new ArrayList<>();
                if (!input.isSuccessful()) {
                    errors.addAll(input.getErrors());
                    resultSink = RpcResultBuilder.<CommitBundleNodeOutput>failed().withRpcErrors(errors);
                } else {
                    resultSink = RpcResultBuilder.success();
                }
            } else {
                resultSink = RpcResultBuilder.<CommitBundleNodeOutput>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "action of " + action + " failed");
            }
            return resultSink.build();
        };
    }
}
