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
import java.util.Objects;
import java.util.concurrent.Semaphore;
import org.opendaylight.openflowplugin.applications.frsync.SemaphoreKeeper;
import org.opendaylight.openflowplugin.applications.frsync.SyncReactor;
import org.opendaylight.openflowplugin.applications.frsync.util.SemaphoreKeeperGuavaImpl;
import org.opendaylight.openflowplugin.applications.frsync.util.SyncupEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Enriches {@link SyncReactorFutureDecorator} with state compression.
 */
public class SyncReactorFutureZipDecorator extends SyncReactorFutureDecorator {

    private final Map<InstanceIdentifier<FlowCapableNode>, SyncupEntry> compressionQueue = new HashMap<>();
    private final SemaphoreKeeper<InstanceIdentifier<FlowCapableNode>> semaphoreKeeper =
            new SemaphoreKeeperGuavaImpl<>(1, true);

    public SyncReactorFutureZipDecorator(final SyncReactor delegate, final ListeningExecutorService executorService) {
        super(delegate, executorService);
    }

    public ListenableFuture<Boolean> syncup(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry syncupEntry) {
        Semaphore guard = null;
        try {
            guard = semaphoreKeeper.summonGuardAndAcquire(flowcapableNodePath);
            if (Objects.isNull(guard)) {
                return Futures.immediateFuture(Boolean.FALSE);
            }
            final boolean newTaskNecessary = updateCompressionState(flowcapableNodePath, syncupEntry);
            if (newTaskNecessary) {
                super.syncup(flowcapableNodePath, syncupEntry);
            }
            return Futures.immediateFuture(Boolean.TRUE);
        } finally {
            semaphoreKeeper.releaseGuard(guard);
        }
    }

    protected ListenableFuture<Boolean> doSyncupInFuture(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                                         final SyncupEntry syncupEntry) {
        final SyncupEntry lastCompressionState = removeLastCompressionState(flowcapableNodePath);

        if (lastCompressionState == null) {
            return Futures.immediateFuture(Boolean.TRUE);
        } else {
            return super.doSyncupInFuture(flowcapableNodePath, lastCompressionState);
        }
    }

    /**
     * If a syncup entry for corresponding the device is present in compression queue and new configuration diff is
     * coming - update the entry in compression queue (zip). Create new (no entry in queue for device) or replace
     * entry (config vs. operational is coming) in queue otherwise.
     */
    private boolean updateCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                           final SyncupEntry syncupEntry) {
        final SyncupEntry previousEntry = compressionQueue.get(flowcapableNodePath);

        if (previousEntry != null && syncupEntry.isOptimizedConfigDelta()) {
            updateOptimizedConfigDelta(flowcapableNodePath, syncupEntry, previousEntry);
        } else {
            compressionQueue.put(flowcapableNodePath, syncupEntry);
        }
        return previousEntry == null;
    }

    private void updateOptimizedConfigDelta(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath,
                                            final SyncupEntry actual,
                                            final SyncupEntry previous) {
        final SyncupEntry updatedEntry = new SyncupEntry(actual.getAfter(), actual.getDsTypeAfter(),
                                                         previous.getBefore(), previous.getDsTypeBefore());
        compressionQueue.put(flowcapableNodePath, updatedEntry);
    }

    private SyncupEntry removeLastCompressionState(final InstanceIdentifier<FlowCapableNode> flowcapableNodePath) {
        Semaphore guard = null;
        try {
            guard = semaphoreKeeper.summonGuardAndAcquire(flowcapableNodePath);
            if (Objects.isNull(guard)) {
                return null;
            }
            return compressionQueue.remove(flowcapableNodePath);
        } finally {
            semaphoreKeeper.releaseGuard(guard);
        }
    }
}