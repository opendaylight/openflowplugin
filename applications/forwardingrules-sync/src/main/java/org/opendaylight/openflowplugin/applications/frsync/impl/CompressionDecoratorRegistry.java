/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.annotation.concurrent.GuardedBy;
import org.apache.commons.lang3.tuple.Pair;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SnapshotElicitRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CompressionDecoratorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(CompressionDecoratorRegistry.class);

    @GuardedBy("compressionGuard")
    final Map<InstanceIdentifier<FlowCapableNode>, Pair<FlowCapableNode, FlowCapableNode>> compressionQueue =
            new HashMap<>();
    final Semaphore compressionGuard = new Semaphore(1, false);
    private SnapshotElicitRegistry snapshotElicitRegistry;

    public SyncReactorFutureWithCompressionDecoratorConfig createFutureZipDecoratorConfig(SyncReactor delegate,
                            ListeningExecutorService executorService, SnapshotElicitRegistry snapshotElicitRegistry) {
        this.snapshotElicitRegistry = snapshotElicitRegistry;
        return new SyncReactorFutureWithCompressionDecoratorConfig(delegate, executorService);
    }

    public SyncReactorFutureWithCompressionDecoratorOperational createFutureZipDecoratorOperational(SyncReactor delegate,
                            ListeningExecutorService executorService, SnapshotElicitRegistry snapshotElicitRegistry) {
        this.snapshotElicitRegistry = snapshotElicitRegistry;
        return new SyncReactorFutureWithCompressionDecoratorOperational(delegate, executorService);
    }

    /**
     * Enriches {@link SyncReactorFutureDecorator} with state compression.
     */
    private class SyncReactorFutureWithCompressionDecorator extends SyncReactorFutureDecorator {

        public SyncReactorFutureWithCompressionDecorator(SyncReactor delegate, ListeningExecutorService executorService) {
            super(delegate, executorService);
        }

        public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                                final FlowCapableNode configTree, final FlowCapableNode operationalTree) throws InterruptedException {
            final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
            LOG.trace("syncup {}", nodeId.getValue());

            try {
                compressionGuard.acquire();

                final boolean newFutureNecessary = updateCompressionState(flowcapableNodePath, configTree, operationalTree);
                if (newFutureNecessary) {
                    super.syncup(flowcapableNodePath, configTree, operationalTree);
                }
                return Futures.immediateFuture(true);
            } finally {
                compressionGuard.release();
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
            final Pair<FlowCapableNode, FlowCapableNode> previous = compressionQueue.get(flowcapableNodePath);
            if (previous != null) {
                final FlowCapableNode previousOperational = previous.getRight();
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, previousOperational));
                return false;
            } else {
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
                return true;
            }
        }

        protected Pair<FlowCapableNode/* config */, FlowCapableNode/* operational */> removeLastCompressionState(
                final InstanceIdentifier<FlowCapableNode> flowcapableNodePath) {
            try {
                try {
                    compressionGuard.acquire();
                } catch (InterruptedException e) {
                    return null;
                }

                return compressionQueue.remove(flowcapableNodePath);
            } finally {
                compressionGuard.release();
            }
        }
    }

    private class SyncReactorFutureWithCompressionDecoratorConfig extends SyncReactorFutureWithCompressionDecorator {

        public SyncReactorFutureWithCompressionDecoratorConfig(SyncReactor delegate, ListeningExecutorService executorService) {
            super(delegate, executorService);
        }

        protected boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                                 final FlowCapableNode configTree, final FlowCapableNode operationalTree) {
            NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
            final Pair<FlowCapableNode, FlowCapableNode> previous = compressionQueue.get(flowcapableNodePath);
            if (previous != null || snapshotElicitRegistry.isRegistered(nodeId)) {
                final FlowCapableNode previousOperational = previous.getRight();
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, previousOperational));
                return false;
            } else {
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
                return true;
            }
        }
    }

    private class SyncReactorFutureWithCompressionDecoratorOperational extends SyncReactorFutureWithCompressionDecorator {

        public SyncReactorFutureWithCompressionDecoratorOperational(SyncReactor delegate, ListeningExecutorService executorService) {
            super(delegate, executorService);
        }

        protected boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                                 final FlowCapableNode configTree, final FlowCapableNode operationalTree) {
            NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
            final Pair<FlowCapableNode, FlowCapableNode> previous = compressionQueue.get(flowcapableNodePath);
            if (previous != null && !snapshotElicitRegistry.isRegistered(nodeId)) {
                final FlowCapableNode previousOperational = previous.getRight();
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, previousOperational));
                return false;
            } else {
                compressionQueue.put(flowcapableNodePath, Pair.of(configTree, operationalTree));
                return true;
            }
        }
    }

}
