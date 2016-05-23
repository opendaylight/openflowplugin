/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.annotation.concurrent.GuardedBy;

import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Enriches {@link SyncReactorFutureDecorator} with state compression.
 */
public class SyncReactorFutureWithCompressionDecorator extends SyncReactorFutureDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureWithCompressionDecorator.class);

    @GuardedBy("beforeCompressionGuard")
    final Map<InstanceIdentifier<FlowCapableNode>, Pair<FlowCapableNode, FlowCapableNode>> beforeCompression =
            new HashMap<>();
    final Semaphore beforeCompressionGuard = new Semaphore(1, false);

    public SyncReactorFutureWithCompressionDecorator(SyncReactorRetryDecorator delegate, ListeningExecutorService executorService) {
        super(delegate, executorService);
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup {}", nodeId.getValue());

        try {
            beforeCompressionGuard.acquire();


            final boolean newFutureNecessary = updateCompressionState(flowcapableNodePath, configTree, operationalTree);
            if (newFutureNecessary && !delegate.inRetry()) {
                super.syncup(flowcapableNodePath, configTree, operationalTree);
            }
            return Futures.immediateFuture(true);
        } finally {
            beforeCompressionGuard.release();
        }
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree)
                    throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("doSyncupInFuture {}", nodeId.getValue());

        final Pair<FlowCapableNode, FlowCapableNode> lastCompressionState =
                removeLastCompressionState(flowcapableNodePath);
        if (lastCompressionState == null) {
            return Futures.immediateFuture(true);
        } else {
            return super.doSyncupInFuture(flowcapableNodePath,
                    lastCompressionState.getLeft(), lastCompressionState.getRight());
        }
    }

    protected boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
            final FlowCapableNode configTree, final FlowCapableNode operationalTree) {
        final Pair<FlowCapableNode, FlowCapableNode> previous = beforeCompression.get(flowcapableNodePath);
        if (previous != null) {
            final FlowCapableNode previousOperational = previous.getRight();
            beforeCompression.put(flowcapableNodePath, Pair.of(configTree, previousOperational));
            return false;
        } else {
            beforeCompression.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
            return true;
        }
    }

    protected Pair<FlowCapableNode/* config */, FlowCapableNode/* operational */> removeLastCompressionState(
            final InstanceIdentifier<FlowCapableNode> flowcapableNodePath) {
        try {
            try {
                beforeCompressionGuard.acquire();
            } catch (InterruptedException e) {
                return null;
            }

            return beforeCompression.remove(flowcapableNodePath);
        } finally {
            beforeCompressionGuard.release();
        }
    }
}
