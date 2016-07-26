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
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.PathUtil;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enriches {@link SyncReactorFutureDecorator} with state compression.
 */
public class SyncReactorFutureZipDecorator extends SyncReactorFutureDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(SyncReactorFutureZipDecorator.class);

    @GuardedBy("compressionGuard")
    private final Map<InstanceIdentifier<FlowCapableNode>, SyncupEntry> compressionQueue = new HashMap<>();
    private final Semaphore compressionGuard = new Semaphore(1, false);

    public SyncReactorFutureZipDecorator(SyncReactor delegate, ListeningExecutorService executorService) {
        super(delegate, executorService);
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry syncupEntry) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("syncup zip decorator: {}", nodeId.getValue());

        try {
            compressionGuard.acquire();

            final boolean newFutureNecessary = updateCompressionState(flowcapableNodePath, syncupEntry);
            if (newFutureNecessary) {
                super.syncup(flowcapableNodePath, syncupEntry);
            }
            return Futures.immediateFuture(true);
        } finally {
            compressionGuard.release();
        }
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                                         final SyncupEntry syncupEntry) throws InterruptedException {
        final NodeId nodeId = PathUtil.digNodeId(flowcapableNodePath);
        LOG.trace("doSyncupInFuture zip decorator: {}", nodeId.getValue());

        final SyncupEntry lastCompressionState = removeLastCompressionState(flowcapableNodePath);
        if (lastCompressionState == null) {
            return Futures.immediateFuture(true);
        } else {
            return super.doSyncupInFuture(flowcapableNodePath, lastCompressionState);
        }
    }

    /**
     * If there is config delta in compression queue for the device and new configuration is coming,
     * update its zip queue entry. Create/replace zip queue entry for the device with operational delta otherwise.
     */
    private boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                           final SyncupEntry syncupEntry) {
        final SyncupEntry previousEntry = compressionQueue.get(flowcapableNodePath);

        if (previousEntry != null && syncupEntry.isOptimizedConfigDelta() && previousEntry.isOptimizedConfigDelta()) {
            updateOptimizedConfigDelta(flowcapableNodePath, syncupEntry, previousEntry);
        } else {
            compressionQueue.put(flowcapableNodePath, syncupEntry);
        }
        return previousEntry == null;
    }

    private void updateOptimizedConfigDelta(InstanceIdentifier<FlowCapableNode> flowcapableNodePath, SyncupEntry actual,
                                            SyncupEntry previous) {
        compressionQueue.put(flowcapableNodePath, new SyncupEntry(actual.getAfter(), actual.getDsTypeAfter(),
                                                                  previous.getBefore(), previous.getDsTypeBefore()));
    }

    private SyncupEntry removeLastCompressionState(
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